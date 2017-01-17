package com.ifthenelse.ejmoore2.agenda.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.CalendarContract;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.ifthenelse.ejmoore2.agenda.R;

import java.util.Locale;

/**
 * Created by ejmoore2 on 1/15/17.
 */

public class AgendaWidgetProvider extends AppWidgetProvider {

    public static final String EXTRA_ACTION = "extra_action";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {

            Intent serviceIntent = new Intent(context, AgendaWidgetService.class);
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);

            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget);
            rv.setRemoteAdapter(R.id.listview_days, serviceIntent);

            AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
            widgetManager.updateAppWidget(widgetId, rv);
        }

        Intent intent = new Intent(context, AgendaWidgetProvider.class);
        String updateAgendaAction = context.getString(R.string.action_agenda_update);
        intent.setAction(updateAgendaAction);

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        long firstAlarm = System.currentTimeMillis();
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC, firstAlarm,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String updateAgendaAction = context.getString(R.string.action_agenda_update),
                eventClickAction = context.getString(R.string.action_event_click);

        if (intent.getAction().equals(updateAgendaAction)) {

            // Request view refreshes for all widget IDs.
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            ComponentName widgetName = new ComponentName(context, AgendaWidgetProvider.class);
            int[] widgetIds = manager.getAppWidgetIds(widgetName);
            manager.notifyAppWidgetViewDataChanged(widgetIds, R.id.listview_days);

        } else if (eventClickAction.equals(intent.getStringExtra(EXTRA_ACTION))) {

            // Details for the clicked-on event are encoded in the intent's action.
            String encodedEvent = intent.getAction();
            String[] tokens = encodedEvent.split("-");
            long eventId = Long.parseLong(tokens[0]);
            long beginTime = Long.parseLong(tokens[1]);
            long endTime = Long.parseLong(tokens[2]);
            String title = "";
            for (int i = 3; i < tokens.length; i++) {
                // The separating character '-' may have been part of the title.
                title += tokens[i];
                if (i != tokens.length - 1) {
                    title += "-";
                }
            }

            // Use the event ID, plus begin and end times to get the exact clicked-on instance.
            Uri uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId);
            Intent viewEventIntent = new Intent(Intent.ACTION_VIEW)
                    .setData(uri)
                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime)
                    .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            /* Double check that there is a calendar app; if not we show a semi-useful message. */
            PackageManager packageManager = context.getPackageManager();
            if (viewEventIntent.resolveActivity(packageManager) != null) {

                context.startActivity(viewEventIntent);

            } else {

                /* Tell the user how long until this event starts (or finishes if in progress). */
                int mins = (int) ((beginTime - System.currentTimeMillis()) / (1000 * 60));
                String innerMsg = null;
                if (mins > 0) {
                    innerMsg = "begins";
                } else {
                    innerMsg = "ends";
                    mins = (int) ((endTime - System.currentTimeMillis()) / (1000 * 60));
                }

                // Reduce the time unit to a sensible value and ensure grammatical correctness.
                int timePeriod = mins;
                String timeUnit = "minutes";
                if (timePeriod >= 60 * 24) {
                    timePeriod /= (60 * 24);
                    timeUnit = "days";
                } else if (timePeriod >= 60) {
                    timePeriod /= 60;
                    timeUnit = "hours";
                }
                if (timePeriod == 1) {
                    timeUnit = timeUnit.substring(0, timeUnit.length() - 1); // removing the 's'
                }
                innerMsg += timePeriod == 0 ? " now" : " in";

                String finalMsg = String.format(Locale.US, "\"%s\" %s", title, innerMsg) +
                        (timePeriod == 0 ? "" : " " + timePeriod + " " + timeUnit);
                Toast.makeText(context, finalMsg, Toast.LENGTH_SHORT).show();
            }

        } else {
            super.onReceive(context, intent);
        }
    }
}
