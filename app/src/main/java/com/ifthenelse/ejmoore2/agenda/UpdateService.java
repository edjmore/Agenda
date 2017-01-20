package com.ifthenelse.ejmoore2.agenda;

import android.app.Service;
import android.content.BroadcastReceiver;
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
            }
        }
    };

    public UpdateService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        IntentFilter screenStateFilter = new IntentFilter();
        screenStateFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(SCREEN_UPDATE_RECEIVER, screenStateFilter);
    }

    @Override
    public void onDestroy() {

        unregisterReceiver(SCREEN_UPDATE_RECEIVER);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
