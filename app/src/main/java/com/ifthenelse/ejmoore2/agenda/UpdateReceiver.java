package com.ifthenelse.ejmoore2.agenda;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ifthenelse.ejmoore2.agenda.widget.AgendaWidgetProvider;

public class UpdateReceiver extends BroadcastReceiver {

    private static final String PACKAGE = "com.droid.mooresoft.agenda.";

    public static final String ACTION_RESTART_UPDATE_SERVICE = PACKAGE + "ACTION_RESTART_UPDATE_SERVICE";

    public UpdateReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        boolean shouldStartUpdateService = false;

        if (ACTION_RESTART_UPDATE_SERVICE.equals(action) ||
                Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            //Log.i("UpdateReceiver", "Broadcast received: " + action);

            shouldStartUpdateService = true;

        } else if (Intent.ACTION_PROVIDER_CHANGED.equals(action)) {
            //Log.i("UpdateReceiver", "Calendar provider info changed, refreshing widgets");

            AgendaWidgetProvider.refreshAllWidgets(context);
            shouldStartUpdateService = true;
        }

        if (shouldStartUpdateService) {
            UpdateService.start(context);
        }
    }

    /**
     * @return True iff an instance of the given service is running.
     */
    private boolean isServiceRunning(Context context, Class<?> serviceCls) {
        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo info : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceCls.getName().equals(info.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
