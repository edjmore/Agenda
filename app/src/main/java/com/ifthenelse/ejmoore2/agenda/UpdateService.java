package com.ifthenelse.ejmoore2.agenda;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.ifthenelse.ejmoore2.agenda.widget.AgendaWidgetProvider;

public class UpdateService extends Service {

    private static final BroadcastReceiver SCREEN_UPDATE_RECEIVER = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {

                AgendaWidgetProvider.refreshAllWidgets(context);
                startUpdateAlarms(context);

            } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {

                stopUpdateAlarms(context);
            }
        }

        private void startUpdateAlarms(Context context) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            long nowTime = System.currentTimeMillis(),
                    interval = DatetimeUtils.ONE_MINUTE * 5;
            PendingIntent operation = getUpdateAlarmOperation(context);
            alarmManager.setInexactRepeating(AlarmManager.RTC, nowTime, interval, operation);
        }

        private void stopUpdateAlarms(Context context) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            PendingIntent operation = getUpdateAlarmOperation(context);
            alarmManager.cancel(operation);
        }

        private PendingIntent getUpdateAlarmOperation(Context context) {
            String updateAgendaAction = context.getString(R.string.action_agenda_update);
            Intent intent = new Intent(context, AgendaWidgetProvider.class).
                    setAction(updateAgendaAction);
            return PendingIntent.getBroadcast(context, R.string.request_code_update_alarm, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }
    };

    private static final IntentFilter SCREEN_UPDATE_INTENT_FILTER = new IntentFilter();

    static {
        SCREEN_UPDATE_INTENT_FILTER.addAction(Intent.ACTION_SCREEN_ON);
        SCREEN_UPDATE_INTENT_FILTER.addAction(Intent.ACTION_SCREEN_OFF);
    }

    public UpdateService() {
    }

    public static void start(Context context) {
        Intent updateService = new Intent(context, UpdateService.class);
        context.startService(updateService);
    }

    /**
     * Send a broadcast to the UpdateReceiver indicating that the service needs to be restarted.
     */
    private void restartSelf() {
        Intent restartBroadcast = new Intent(UpdateReceiver.ACTION_RESTART_UPDATE_SERVICE);
        sendBroadcast(restartBroadcast);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        registerReceiver(SCREEN_UPDATE_RECEIVER, SCREEN_UPDATE_INTENT_FILTER);
        return START_STICKY;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        stopSelf();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(SCREEN_UPDATE_RECEIVER);

        restartSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
