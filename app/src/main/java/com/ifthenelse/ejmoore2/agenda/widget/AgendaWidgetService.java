package com.ifthenelse.ejmoore2.agenda.widget;

import android.Manifest;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.ifthenelse.ejmoore2.agenda.R;
import com.ifthenelse.ejmoore2.agenda.model.Agenda;
import com.ifthenelse.ejmoore2.agenda.model.Instance;
import com.ifthenelse.ejmoore2.agenda.util.ArtStudent;
import com.ifthenelse.ejmoore2.agenda.util.ConfigManager;
import com.ifthenelse.ejmoore2.agenda.util.DatetimeUtils;
import com.ifthenelse.ejmoore2.agenda.util.PermissionHelper;

/**
 * Created by ejmoore2 on 1/15/17.
 */

public class AgendaWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new AgendaViewFactory(this, intent);
    }

    private class AgendaViewFactory implements RemoteViewsFactory {

        private Context context;
        private int widgetId;

        private PermissionHelper ph;
        private ConfigManager configManager;

        /* User configurable settings. */
        private long timePeriod;
        private boolean useRelativeTime;
        private boolean allowEmptyDays;
        private int textColor, subtitleTextColor;

        private Agenda agenda;

        AgendaViewFactory(Context context, Intent intent) {
            this.context = context;
            this.widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            this.ph = new PermissionHelper();
            this.configManager = new ConfigManager(context, widgetId);
            this.agenda = Agenda.empty();
            loadPrefs();
        }

        @Override
        public int getCount() {
            return agenda.getSortedDays().length;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public RemoteViews getViewAt(int position) {
            Agenda.Day day = agenda.getSortedDays()[position];

            // List view items are recycled, so we must explicitly clear the inner layout.
            RemoteViews rv = new RemoteViews(getPackageName(), R.layout.listitem_day);
            rv.removeAllViews(R.id.linearlayout_events);

            // Setup the date text, and the fill-in intent, which will bring users to the
            // appropriate calendar day upon clicking on the date text view.
            rv.setTextViewText(R.id.day_text, DatetimeUtils.getDateString(day, true));
            rv.setTextColor(R.id.day_text, textColor);
            rv.setOnClickFillInIntent(R.id.day_text,
                    new Intent()
                            .putExtra(AgendaWidgetProvider.EXTRA_DATE, day.getDate().getTime()));

            if (allowEmptyDays && day.isEmpty()) {
                // Simply add one instance of the empty day view.
                RemoteViews emptyDayView = new RemoteViews(getPackageName(), R.layout.listitem_empty_day);
                emptyDayView.setTextColor(R.id.empty_day_text, subtitleTextColor);

                rv.addView(R.id.linearlayout_events, emptyDayView);
            } else {
                // Construct an embedded list view by appending views to the inner linear layout
                for (int i = 0; i < day.getSortedInstances().length; i++) {
                    Instance instance = day.getSortedInstances()[i];

                    RemoteViews listItem = new RemoteViews(getPackageName(), R.layout.listitem_event);

                    // Fill the list item with data from the given instance, including: title, time, and color.
                    listItem.setTextViewText(R.id.event_title_text, instance.getTitle());
                    listItem.setTextColor(R.id.event_title_text, textColor);
                    listItem.setTextViewText(R.id.event_subtitle_text,
                            DatetimeUtils.getTimeString(instance, useRelativeTime));
                    listItem.setTextColor(R.id.event_subtitle_text, subtitleTextColor);
                    listItem.setImageViewBitmap(R.id.event_color_indicator,
                            ArtStudent.getInstance(context)
                                    .getColoredCircle(instance.getColor()));

                    // Clicking on this list item will open the calendar application to the corresponding event instance.
                    listItem.setOnClickPendingIntent(R.id.event_container,
                            getEventClickBroadcast(context, instance));

                    rv.addView(R.id.linearlayout_events, listItem);
                }
            }

            return rv;
        }

        private PendingIntent getEventClickBroadcast(Context context, Instance instance) {
            Intent onClickIntent = new Intent(context, AgendaWidgetProvider.class)
                    .setAction(instance.encodeInstance())
                    .putExtra(AgendaWidgetProvider.EXTRA_ACTION,
                            AgendaWidgetProvider.ACTION_EVENT_CLICK);
            return PendingIntent.getBroadcast(context, widgetId, onClickIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public void onCreate() {
            refreshAgenda();
        }

        @Override
        public void onDataSetChanged() {
            refreshAgenda();
        }

        @Override
        public void onDestroy() {
        }

        private void refreshAgenda() {
            if (ph.checkPermission(context, Manifest.permission.READ_CALENDAR)) {

                // Load user preferences for widget, then current agenda data.
                loadPrefs();
                agenda = Agenda.getAgendaForPeriod(context, timePeriod, allowEmptyDays);

            } else {
                ph.notifyUserOfMissingPermission(context, Manifest.permission.READ_CALENDAR);
            }
        }

        /**
         * Load user preferences related to this widget, including: agenda time period,
         * whether or not to use relative time descriptions, and text color theme.
         */
        private void loadPrefs() {
            timePeriod =
                    configManager.getLong(R.string.config_time_period_key, DatetimeUtils.ONE_WEEK);

            useRelativeTime = configManager.getBoolean(R.string.config_relative_time_key, false);
            allowEmptyDays = configManager.getBoolean(R.string.config_allow_empty_days_key, false);

            if (configManager.getBoolean(R.string.config_text_color_key, true)) {
                textColor = getColor(R.color.text_title);
                subtitleTextColor = getColor(R.color.text_subtitle);
            } else {
                textColor = getColor(R.color.text_title_dark);
                subtitleTextColor = getColor(R.color.text_subtitle_dark);
            }
        }
    }
}
