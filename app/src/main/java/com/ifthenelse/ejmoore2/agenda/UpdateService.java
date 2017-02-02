package com.ifthenelse.ejmoore2.agenda;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.ifthenelse.ejmoore2.agenda.util.DatetimeUtils;
import com.ifthenelse.ejmoore2.agenda.widget.AgendaWidgetProvider;

public class UpdateService extends Service {

    private static final BroadcastReceiver SCREEN_UPDATE_RECEIVER = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                //Log.i("UpdateService", "Screen on: refreshing widgets and starting update alarms");

                AgendaWidgetProvider.refreshAllWidgets(context);
                startUpdateAlarms(context);

            } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                //Log.i("UpdateService", "Screen off: canceling pending update alarms");

                stopUpdateAlarms(context);
            }
        }

        /**
         * Begin scheduling update alarms to go off ~2 minutes. When the alarm goes off a
         * broadcast will be sent to the UpdateReceiver requesting a widget refresh.
         */
        private void startUpdateAlarms(Context context) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            long nowTime = System.currentTimeMillis(),
                    interval = DatetimeUtils.ONE_MINUTE * 2;
            PendingIntent operation = getUpdateAlarmOperation(context);
            alarmManager.setInexactRepeating(AlarmManager.RTC, nowTime, interval, operation);
        }

        /**
         * Cancel any pending update alarms and stop repeating.
         */
        private void stopUpdateAlarms(Context context) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            PendingIntent operation = getUpdateAlarmOperation(context);
            alarmManager.cancel(operation);
        }

        /**
         * Returns a pending broadcast intent, requesting a widget data refresh.
         */
        private PendingIntent getUpdateAlarmOperation(Context context) {
            Intent intent = new Intent(context, AgendaWidgetProvider.class).
                    setAction(AgendaWidgetProvider.ACTION_AGENDA_UPDATE);
            return PendingIntent.getBroadcast(context, R.string.request_code_update_alarm, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }
    };

    private static final IntentFilter SCREEN_UPDATE_INTENT_FILTER = new IntentFilter();

    static {
        SCREEN_UPDATE_INTENT_FILTER.addAction(Intent.ACTION_SCREEN_ON);
        SCREEN_UPDATE_INTENT_FILTER.addAction(Intent.ACTION_SCREEN_OFF);
    }

    private boolean shouldRestartSelf = true;

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
        Intent restartBroadcast = new Intent(this, UpdateReceiver.class)
                .setAction(UpdateReceiver.ACTION_RESTART_UPDATE_SERVICE);
        sendBroadcast(restartBroadcast);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Log.i("UpdateService", "Start command received");
        shouldRestartSelf = true;
        startRestartAlarms();

        if (!AgendaWidgetProvider.isAnyWidgetActive(this)) {
            //Log.i("UpdateService", "No active widgets: stopping self");

            shouldRestartSelf = false;
            stopRestartAlarms();

            stopSelf();
            return START_NOT_STICKY;
        }

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
        //Log.i("UpdateService", "Service destroyed");

        unregisterReceiver(SCREEN_UPDATE_RECEIVER);

        if (shouldRestartSelf) {
            //Log.i("UpdateService", "Requesting restart...");
            restartSelf();
        }
    }

    /**
     * Starts a series of inexact repeating alarms, sending the broadcast request that the service be restarted.
     */
    private void startRestartAlarms() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        long nowTime = System.currentTimeMillis(),
                interval = AlarmManager.INTERVAL_HOUR;
        PendingIntent operation = getRestartServiceOperation();
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, nowTime, interval, operation);
    }

    /**
     * Cancels any pending restart alarms.
     */
    private void stopRestartAlarms() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent operation = getRestartServiceOperation();
        alarmManager.cancel(operation);
    }

    /**
     * Returns a pending broadcast intent, requesting that the service be restarted.
     */
    private PendingIntent getRestartServiceOperation() {
        Intent intent = new Intent(this, UpdateReceiver.class)
                .setAction(UpdateReceiver.ACTION_RESTART_UPDATE_SERVICE);
        return PendingIntent.getBroadcast(
                this, R.string.request_code_service_restart, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
