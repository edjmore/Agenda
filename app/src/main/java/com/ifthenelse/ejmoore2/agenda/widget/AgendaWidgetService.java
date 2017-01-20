package com.ifthenelse.ejmoore2.agenda.widget;

import android.Manifest;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.ifthenelse.ejmoore2.agenda.ArtStudent;
import com.ifthenelse.ejmoore2.agenda.ConfigManager;
import com.ifthenelse.ejmoore2.agenda.PermissionHelper;
import com.ifthenelse.ejmoore2.agenda.R;
import com.ifthenelse.ejmoore2.agenda.Utils;
import com.ifthenelse.ejmoore2.agenda.model.Agenda;
import com.ifthenelse.ejmoore2.agenda.model.Instance;

import java.util.Date;

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
            return agenda.getDays().length;
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
            RemoteViews rv = new RemoteViews(getPackageName(), R.layout.listitem_day);

            // Set date indicator (e.g. "Tuesday, July 3")
            Agenda.Day day = agenda.getDays()[position];
            Date date = day.getDate();
            String dateString = Utils.getRelativeDateString(date);
            rv.setTextViewText(R.id.day_text, dateString);

            // TODO: Day click should bring user to that day in the calendar application.

            // List view items are recycled, so the inner layout may not be empty.
            rv.removeAllViews(R.id.linearlayout_events);

            /* Construct an inner list view by appending views to the linear layout. */
            for (int i = 0; i < day.getInstances().length; i++) {
                Instance instance = day.getInstances()[i];
                // For the first instance on the agenda, we set an alarm to update the
                // widget after the event ends (i.e. to remove this instance from view).
                if (position == 0 && i == 0) {
                    AgendaWidgetProvider.setNextUpdateAlarmExact(context, instance.getEndTime());
                }

                RemoteViews listItem = new RemoteViews(getPackageName(), R.layout.listitem_event);

                /* Set event title and subtitle (e.g. "Going shopping\n3:00 PM"),
                 * and also add the little colored circle corresponding to event color. */
                String title = instance.getTitle();
                listItem.setTextViewText(R.id.event_title_text, title);

                // Subtitle is either exact time or relative (depending on user preference).
                String subtitle = !useRelativeTime ?
                        Utils.getExactTimeString(instance) :
                        Utils.getRelativeEventTimeString(instance);
                listItem.setTextViewText(R.id.event_subtitle_text, subtitle);

                int color = instance.getColor();
                Bitmap coloredCircle = ArtStudent.getInstance(context).getColoredCircle(color);
                listItem.setImageViewBitmap(R.id.event_color_indicator, coloredCircle);

                // We generate a unique action for each intent b/c otherwise the
                // system will merge all pending intents into one. The action string
                // also provides all information necessary to open the correct calendar entry.
                String uniqueAction =
                        instance.getEventId() + "-" + instance.getBeginTime() + "-" +
                                instance.getActualEndTime() + "-" + instance.getTitle();

                Intent onClickIntent = new Intent(context, AgendaWidgetProvider.class)
                        .setAction(uniqueAction)
                        .putExtra(AgendaWidgetProvider.EXTRA_ACTION,
                                context.getString(R.string.action_event_click));
                PendingIntent pendingIntent =
                        PendingIntent.getBroadcast(context, 0, onClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                listItem.setOnClickPendingIntent(R.id.event_container, pendingIntent);

                rv.addView(R.id.linearlayout_events, listItem);
            }

            return rv;
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
                long timePeriod =
                        configManager.getLong(R.string.config_time_period_key, Agenda.ONE_WEEK);
                agenda = Agenda.getAgendaForPeriod(context, timePeriod);
                useRelativeTime = configManager.getBoolean(R.string.config_relative_time_key, false);
            } else {
                ph.notifyUserOfMissingPermission(context, Manifest.permission.READ_CALENDAR);
            }
        }
    }
}
