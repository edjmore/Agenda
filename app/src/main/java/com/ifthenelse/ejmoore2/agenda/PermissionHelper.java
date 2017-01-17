package com.ifthenelse.ejmoore2.agenda;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;

import com.ifthenelse.ejmoore2.agenda.view.PermissionActivity;

/**
 * Created by ejmoore2 on 1/15/17.
 */

public class PermissionHelper {

    public static final String EXTRA_PERMISSION = "extra_permission";

    public boolean checkPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermission(Activity activity, String permission, int requestCode) {
        ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
    }

    public void notifyUserOfMissingPermission(Context context, String permission) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Missing permission")
                .setContentText("Tap to view")
                .setAutoCancel(true);

        Intent intent = new Intent(context, PermissionActivity.class);
        intent.putExtra(EXTRA_PERMISSION, permission);

        // Artificial back-stack ensures backing out of
        // permission activity will bring user to home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(PermissionActivity.class);
        stackBuilder.addNextIntent(intent);

        PendingIntent pendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }
}
