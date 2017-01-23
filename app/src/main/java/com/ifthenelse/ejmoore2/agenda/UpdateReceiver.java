package com.ifthenelse.ejmoore2.agenda;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class UpdateReceiver extends BroadcastReceiver {

    private static final String PACKAGE = "com.droid.mooresoft.agenda.";

    public static final String ACTION_RESTART_UPDATE_SERVICE = PACKAGE + "ACTION_RESTART_UPDATE_SERVICE";

    public UpdateReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_RESTART_UPDATE_SERVICE.equals(intent.getAction())) {

            UpdateService.start(context);

        } else if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {

            UpdateService.start(context);
        }
    }
}
