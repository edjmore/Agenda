package com.ifthenelse.ejmoore2.agenda;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by ejmoore2 on 1/15/17.
 */

public class PermissionHelper {

    public boolean checkPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermission(Activity activity, String permission, int requestCode) {
        ActivityCompat.requestPermissions(activity, new String[]{ permission }, requestCode);
    }

    public void notifyUserOfMissingPermission(Context context, String permission) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle("Action required")
                .setContentText("\"Agenda\" would like the following permission: " + permission);
    }
}
