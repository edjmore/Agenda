package com.ifthenelse.ejmoore2.agenda.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.ifthenelse.ejmoore2.agenda.R;

/**
 * Created by ejmoore2 on 1/15/17.
 */

public class AgendaWidgetProvider extends AppWidgetProvider {

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
        String updateAgendaAction = context.getString(R.string.action_agenda_update);
        if (intent.getAction().equals(updateAgendaAction)) {

            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            ComponentName widgetName = new ComponentName(context, AgendaWidgetProvider.class);
            int[] widgetIds = manager.getAppWidgetIds(widgetName);

            manager.notifyAppWidgetViewDataChanged(widgetIds, R.id.listview_days);
        } else {
            super.onReceive(context, intent);
        }
    }
}
