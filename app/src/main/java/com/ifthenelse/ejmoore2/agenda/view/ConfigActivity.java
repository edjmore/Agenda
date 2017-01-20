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
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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

public class ConfigActivity extends AppCompatActivity {

    private int widgetId;
    private Intent resultValue;

    private ConfigManager configManager;
    private boolean wasConfigChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        // Setup the action bar with confirm and cancel changes button.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Configure your agenda");
        toolbar.setTitleTextAppearance(this, R.style.AppTheme_Text_Headline);
        toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        handleNewIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.config_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.action_item_done:
                setResult(RESULT_OK, resultValue);

                // Only request a widget update if preference data was changed.
                if (wasConfigChanged) {
                    configManager.commitChanges();

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
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
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

        // Setup intent template for individual list items (days).
        Intent viewDateIntent = new Intent(context, AgendaWidgetProvider.class)
                .setAction(AgendaWidgetProvider.ACTION_VIEW_DATE)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        PendingIntent viewDatePendingIntent =
                PendingIntent.getBroadcast(context, 3, viewDateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setPendingIntentTemplate(R.id.listview_days, viewDatePendingIntent);

        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        widgetManager.updateAppWidget(widgetId, rv);

        /* Setup a repeating alarm to update the widget content every ~5 minutes. */
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
