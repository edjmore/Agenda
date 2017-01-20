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
import android.widget.Toast;

import com.ifthenelse.ejmoore2.agenda.ConfigManager;
import com.ifthenelse.ejmoore2.agenda.R;
import com.ifthenelse.ejmoore2.agenda.view.ConfigActivity;

/**
 * Created by ejmoore2 on 1/15/17.
 */

public class AgendaWidgetProvider extends AppWidgetProvider {

    public static final String EXTRA_ACTION = "extra_action";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {

            ConfigActivity.performInitialWidgetSetup(context, widgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // Remove any saved user preferences for the given widgets.
        for (int widgetId : appWidgetIds) {
            ConfigManager.removeAllConfigsForWidget(context, widgetId);
        }
        super.onDeleted(context, appWidgetIds);
    }

    public static void setNextUpdateAlarmExact(Context context, long alarmTime) {
        PendingIntent pendingIntent = getUpdateAlarmIntent(context);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, alarmTime, pendingIntent);
    }

    public static void setNextUpdateAlarmInexact(Context context) {
        setNextUpdateAlarmInexact(context, System.currentTimeMillis());
    }

    private static void setNextUpdateAlarmInexact(Context context, long firstAlarm) {
        PendingIntent pendingIntent = getUpdateAlarmIntent(context);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC, firstAlarm,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
    }

    private static PendingIntent getUpdateAlarmIntent(Context context) {
        Intent intent = new Intent(context, AgendaWidgetProvider.class);
        String updateAgendaAction = context.getString(R.string.action_agenda_update);
        intent.setAction(updateAgendaAction);

        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String updateAgendaAction = context.getString(R.string.action_agenda_update),
                eventClickAction = context.getString(R.string.action_event_click),
                editNewEventAction = context.getString(R.string.action_edit_new_event),
                openConfigAction = context.getString(R.string.action_open_config);

        if (intent.getAction().equals(updateAgendaAction)) {

            // Request view refreshes for all widget IDs.
            refreshAllWidgets(context);

        } else if (eventClickAction.equals(intent.getStringExtra(EXTRA_ACTION))) {

            // Details for the clicked-on event are encoded in the intent's action.
            String encodedEvent = intent.getAction();
            String[] tokens = encodedEvent.split("-");
            long eventId = Long.parseLong(tokens[0]);
            long actualBeginTime = Long.parseLong(tokens[1]);
            long actualEndTime = Long.parseLong(tokens[2]);
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
                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, actualBeginTime)
                    .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, actualEndTime)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            /* Double check that there is a calendar app; if not we show a semi-useful message. */
            PackageManager packageManager = context.getPackageManager();
            if (viewEventIntent.resolveActivity(packageManager) != null) {

                context.startActivity(viewEventIntent);
            } else {

                /* Let the user know why nothing happened. */
                String finalMsg = "Error: no calendar application installed";
                Toast.makeText(context, finalMsg, Toast.LENGTH_SHORT).show();
            }

        } else if (editNewEventAction.equals(intent.getAction())) {

            /* Bring user to calendar application to insert a new event. */
            Intent insertIntent = new Intent(Intent.ACTION_INSERT)
                    .setData(CalendarContract.Events.CONTENT_URI)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PackageManager packageManager = context.getPackageManager();
            if (insertIntent.resolveActivity(packageManager) != null) {

                context.startActivity(insertIntent);
            }
        } else if (openConfigAction.equals(intent.getAction())) {

            int widgetId =
                    intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            Intent openConfigIntent = new Intent(context, ConfigActivity.class)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(openConfigIntent);
        } else {
            super.onReceive(context, intent);
        }
    }

    public static void refreshAllWidgets(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName widgetName = new ComponentName(context, AgendaWidgetProvider.class);
        int[] widgetIds = manager.getAppWidgetIds(widgetName);
        manager.notifyAppWidgetViewDataChanged(widgetIds, R.id.listview_days);
    }
}
