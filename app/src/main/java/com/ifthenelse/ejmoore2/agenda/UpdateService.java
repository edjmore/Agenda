package com.ifthenelse.ejmoore2.agenda;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import com.ifthenelse.ejmoore2.agenda.widget.AgendaWidgetProvider;

public class UpdateService extends Service {

    public static final String ACTION_RESTART_UPDATE_SERVICE =
            "com.droid.mooresoft.agenda.ACTION_RESTART_UPDATE_SERVICE";

    private static final BroadcastReceiver SCREEN_UPDATE_RECEIVER = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {

                Log.e("agenda", "SCREEN ON");
                AgendaWidgetProvider.refreshAllWidgets(context);
            }
        }
    };

    public UpdateService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IntentFilter screenStateFilter = new IntentFilter();
        screenStateFilter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(SCREEN_UPDATE_RECEIVER, screenStateFilter);

        Log.e("agenda", "ON START COMMAND");

        return START_STICKY;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.e("agenda", "LOW MEMORY");
        stopSelf();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.e("agenda", "TASK REMOVED");

        stopSelf();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Log.e("agena", "TRIM MEMORY");

        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.e("agenda", "SERVICE DESTROYED");

        //unregisterReceiver(SCREEN_UPDATE_RECEIVER);

        Intent restartBroadcast = new Intent(ACTION_RESTART_UPDATE_SERVICE);
        sendBroadcast(restartBroadcast);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
