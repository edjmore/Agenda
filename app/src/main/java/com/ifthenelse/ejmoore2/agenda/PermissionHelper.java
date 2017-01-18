package com.ifthenelse.ejmoore2.agenda;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.ifthenelse.ejmoore2.agenda.view.ConfigActivity;

/**
 * Created by ejmoore2 on 1/15/17.
 */

public class PermissionHelper {

    public static final String EXTRA_PERMISSION = "extra_permission";
    public static final int REQUEST_PERMISSION = 0;

    public boolean checkPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermission(Activity activity, String permission, int requestCode) {
        ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
    }

    public void notifyUserOfMissingPermission(Context context, String permission) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.calendar)
                .setContentTitle("Missing permission")
                .setContentText("Tap to view")
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true);
        if (Build.VERSION.SDK_INT >= 16) {
            builder.setPriority(Notification.PRIORITY_MAX); // banner popup
        }

        Log.e(getClass().getCanonicalName(), "SENDING PERMISSION NOTIFICATION");

        Intent intent = new Intent(context, ConfigActivity.class)
                .putExtra(EXTRA_PERMISSION, permission);

        /*
        // Artificial back-stack ensures backing out of
        // permission activity will bring user to home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(PermissionActivity.class);
        stackBuilder.addNextIntent(intent);

        PendingIntent pendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        */

        PendingIntent pendingIntent =
                PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(pendingIntent);

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }
}
