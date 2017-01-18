package com.ifthenelse.ejmoore2.agenda.view;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.ifthenelse.ejmoore2.agenda.PermissionHelper;
import com.ifthenelse.ejmoore2.agenda.R;
import com.ifthenelse.ejmoore2.agenda.widget.AgendaWidgetProvider;

public class PermissionActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Intent intent = getIntent();
            String permission = intent.getStringExtra(PermissionHelper.EXTRA_PERMISSION);

            PermissionHelper ph = new PermissionHelper();
            if (!ph.checkPermission(this, permission)) {
                ph.requestPermission(this, permission, REQUEST_PERMISSION);
            } else {
                finish();
            }
        } catch (Exception e) {
            Log.e(getClass().getCanonicalName(), e.toString());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                AppWidgetManager manager = AppWidgetManager.getInstance(this);
                ComponentName widgetName = new ComponentName(this, AgendaWidgetProvider.class);
                int[] widgetIds = manager.getAppWidgetIds(widgetName);

                manager.notifyAppWidgetViewDataChanged(widgetIds, R.id.listview_days);
            }
            finish();
        }
    }
}
