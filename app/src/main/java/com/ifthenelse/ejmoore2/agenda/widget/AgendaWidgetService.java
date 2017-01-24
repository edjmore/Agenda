package com.ifthenelse.ejmoore2.agenda.widget;

import android.Manifest;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.ifthenelse.ejmoore2.agenda.util.ArtStudent;
import com.ifthenelse.ejmoore2.agenda.util.ConfigManager;
import com.ifthenelse.ejmoore2.agenda.util.PermissionHelper;
import com.ifthenelse.ejmoore2.agenda.R;
import com.ifthenelse.ejmoore2.agenda.util.DatetimeUtils;
import com.ifthenelse.ejmoore2.agenda.model.Agenda;
import com.ifthenelse.ejmoore2.agenda.model.Instance;

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
        private boolean useRelativeTime;

        private Agenda agenda;

        AgendaViewFactory(Context context, Intent intent) {
            this.context = context;
            this.widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            this.ph = new PermissionHelper();
            this.configManager = new ConfigManager(context, widgetId);
            this.agenda = Agenda.empty();
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
            rv.setTextViewText(R.id.day_text, DatetimeUtils.getDateString(day, false));
            rv.setOnClickFillInIntent(R.id.day_text,
                    new Intent()
                            .putExtra(AgendaWidgetProvider.EXTRA_DATE, day.getDate().getTime()));

            // Construct an embedded list view by appending views to the inner linear layout
            for (int i = 0; i < day.getSortedInstances().length; i++) {
                Instance instance = day.getSortedInstances()[i];

                RemoteViews listItem = new RemoteViews(getPackageName(), R.layout.listitem_event);

                // Fill the list item with data from the given instance, including: title, time, and color.
                listItem.setTextViewText(R.id.event_title_text, instance.getTitle());
                listItem.setTextViewText(R.id.event_subtitle_text,
                        DatetimeUtils.getTimeString(instance, useRelativeTime));
                listItem.setImageViewBitmap(R.id.event_color_indicator,
                        ArtStudent.getInstance(context)
                                .getColoredCircle(instance.getColor()));

                // Clicking on this list item will open the calendar application to the corresponding event instance.
                listItem.setOnClickPendingIntent(R.id.event_container,
                        getEventClickBroadcast(context, instance));

                rv.addView(R.id.linearlayout_events, listItem);
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
                // Load user preferences for widget.
                long timePeriod =
                        configManager.getLong(R.string.config_time_period_key, DatetimeUtils.ONE_WEEK);
                useRelativeTime = configManager.getBoolean(R.string.config_relative_time_key, false);

                // Load current agenda data.
                agenda = Agenda.getAgendaForPeriod(context, timePeriod);
            } else {

                ph.notifyUserOfMissingPermission(context, Manifest.permission.READ_CALENDAR);
            }
        }
    }
}
