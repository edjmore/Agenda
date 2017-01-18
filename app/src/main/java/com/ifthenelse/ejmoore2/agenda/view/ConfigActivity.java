package com.ifthenelse.ejmoore2.agenda.view;

import android.Manifest;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.RemoteViews;
import android.widget.Switch;

import com.ifthenelse.ejmoore2.agenda.ConfigManager;
import com.ifthenelse.ejmoore2.agenda.PermissionHelper;
import com.ifthenelse.ejmoore2.agenda.R;
import com.ifthenelse.ejmoore2.agenda.model.Agenda;
import com.ifthenelse.ejmoore2.agenda.widget.AgendaWidgetProvider;
import com.ifthenelse.ejmoore2.agenda.widget.AgendaWidgetService;

public class ConfigActivity extends AppCompatActivity implements View.OnClickListener {

    private int widgetId;
    private Intent resultValue;

    private ConfigManager configManager;
    private boolean wasConfigChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        Intent intent = getIntent();
        handleNewIntent(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        handleNewIntent(intent);
    }

    private void handleNewIntent(Intent intent) {
        if (intent.hasExtra(PermissionHelper.EXTRA_PERMISSION)) {
            String permission = intent.getStringExtra(PermissionHelper.EXTRA_PERMISSION);

            PermissionHelper ph = new PermissionHelper();
            if (!ph.checkPermission(this, permission)) {
                ph.requestPermission(this, permission, PermissionHelper.REQUEST_PERMISSION);
            }
        }

        widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            // Can't configure widget if we don't have a specific ID.
            finish();
        }

        configManager = new ConfigManager(this, widgetId);

        // Widget will not be finalized unless user presses the save configuration button.
        resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        setResult(RESULT_CANCELED, resultValue);

        performInitialWidgetSetup(this, widgetId);

        /* Initialize all the configuration views to default/current values. */
        setupTimePeriodRadioGroup();
        setupRelativeTimeSwitch();
    }

    /* Initial widget setup, linking update service and initializing views/buttons. */
    public static void performInitialWidgetSetup(Context context, int widgetId) {
        Intent serviceIntent = new Intent(context, AgendaWidgetService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);

        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget);
        rv.setRemoteAdapter(R.id.listview_days, serviceIntent);
        rv.setEmptyView(R.id.listview_days, R.id.empty_view);

        // Setup settings button on widget to open configuration activity.
        Intent openConfigIntent = new Intent(context, AgendaWidgetProvider.class)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                .setAction(context.getString(R.string.action_open_config));
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(context, widgetId, openConfigIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.button_open_config, pendingIntent);

        // Setup button for adding a new event.
        Intent newEventIntent = new Intent(context, AgendaWidgetProvider.class)
                .setAction(context.getString(R.string.action_edit_new_event));
        pendingIntent =
                PendingIntent.getBroadcast(context, 0, newEventIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.button_new_event, pendingIntent);

        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        widgetManager.updateAppWidget(widgetId, rv);

        /* Setup a repeating alarm to update the widget content every ~15 minutes. */
        AgendaWidgetProvider.setNextUpdateAlarmInexact(context);
    }

    public void onRadioButtonClick(View v) {
        long agendaTimePeriod = -1;
        switch (v.getId()) {
            case R.id.rbutton_one_day:
                agendaTimePeriod = Agenda.ONE_DAY;
                break;
            case R.id.rbutton_two_weeks:
                agendaTimePeriod = Agenda.ONE_WEEK * 2;
                break;
            case R.id.rbutton_one_month:
                agendaTimePeriod = Agenda.ONE_MONTH;
                break;
            case R.id.rbutton_one_week:
                agendaTimePeriod = Agenda.ONE_WEEK;
        }

        if (agendaTimePeriod > 0 && configManager.setLong(R.string.config_time_period_key, agendaTimePeriod)) {
            wasConfigChanged = true;
        }
    }

    public void onSwitchClick(View v) {
        if (v.getId() == R.id.switch_relative_time) {
            boolean useRelativeTime = ((Switch) v).isChecked();

            if (configManager.setBoolean(R.string.config_relative_time_key, useRelativeTime)) {
                wasConfigChanged = true;
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.save_config_button) {
            setResult(RESULT_OK, resultValue);

            // Only request a widget update if preference data was changed.
            if (wasConfigChanged) {
                AppWidgetManager widgetManager = AppWidgetManager.getInstance(this);
                widgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.listview_days);
            }

            // Request user permission for calendar access if not already granted; otw close.
            PermissionHelper ph = new PermissionHelper();
            String readCalendarPermission = Manifest.permission.READ_CALENDAR;
            if (!ph.checkPermission(this, readCalendarPermission)) {
                ph.requestPermission(this, readCalendarPermission, PermissionHelper.REQUEST_PERMISSION);
            } else {
                finish();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionHelper.REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                AgendaWidgetProvider.refreshAllWidgets(this);
            }
            finish();
        }
    }

    private void setupTimePeriodRadioGroup() {
        long timePeriod = configManager.getLong(R.string.config_time_period_key, Agenda.ONE_WEEK);

        int selected = R.id.rbutton_one_week;
        if (timePeriod == Agenda.ONE_DAY) {
            selected = R.id.rbutton_one_day;
        } else if (timePeriod == Agenda.ONE_WEEK * 2) {
            selected = R.id.rbutton_two_weeks;
        } else if (timePeriod == Agenda.ONE_MONTH) {
            selected = R.id.rbutton_one_month;
        }

        RadioGroup rGroup = (RadioGroup) findViewById(R.id.rgroup_time_period);
        rGroup.check(selected);
    }

    private void setupRelativeTimeSwitch() {
        boolean useRelativeTime = configManager.getBoolean(R.string.config_relative_time_key, false);
        Switch relTimeSwitch = (Switch) findViewById(R.id.switch_relative_time);

        relTimeSwitch.setChecked(useRelativeTime);
    }
}
