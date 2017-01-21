package com.ifthenelse.ejmoore2.agenda;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class UpdateServiceReceiver extends BroadcastReceiver {

    public UpdateServiceReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (UpdateService.ACTION_RESTART_UPDATE_SERVICE.equals(intent.getAction())) {

            Log.e("agenda", "RECEIVED RESTART SERVICE BROADCAST");

            Intent startService = new Intent(context, UpdateService.class);
            context.startService(startService);
        } else if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {

            Log.e("agenda", "BOOT COMPLETED");

            Intent startService = new Intent(context, UpdateService.class);
            context.startService(startService);
        }
    }
}
