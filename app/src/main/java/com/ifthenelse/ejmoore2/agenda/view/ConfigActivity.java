package com.ifthenelse.ejmoore2.agenda.view;

import android.Manifest;
import android.appwidget.AppWidgetManager;
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
import android.widget.Switch;

import com.ifthenelse.ejmoore2.agenda.R;
import com.ifthenelse.ejmoore2.agenda.util.ConfigManager;
import com.ifthenelse.ejmoore2.agenda.util.DatetimeUtils;
import com.ifthenelse.ejmoore2.agenda.util.PermissionHelper;
import com.ifthenelse.ejmoore2.agenda.widget.AgendaWidgetProvider;

public class ConfigActivity extends AppCompatActivity {

    private int widgetId;
    private Intent resultValue;

    private ConfigManager configManager;

    // We only refresh the widget if the user confirms some configuration change.
    private boolean wasConfigChanged = false;
    private boolean wasThemeChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        // Setup the action bar with confirm and cancel changes buttons.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Configure your agenda");
        toolbar.setTitleTextAppearance(this, R.style.AppTheme_Text_Headline);
        toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);
        setSupportActionBar(toolbar);

        // Read data from the intent and perform the necessary setup actions.
        handleNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleNewIntent(intent);
    }

    /**
     * Performs all the necessary actions to deal with a
     * new (or initial) intent being delivered to the activity.
     */
    private void handleNewIntent(Intent intent) {
        // If the intent has the permission extra, then the activity was
        // opened solely to display the permission request dialog to the user.
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
            // If we get here it's probably a bug.
            //Log.i("ConfigActivity", "ConfigActivity received intent with invalid widget ID");

            finish();
        }


        // Initialize all the configuration views to default/current values.
        configManager = new ConfigManager(this, widgetId);
        setupTimePeriodRadioGroup();
        setupRelativeTimeSwitch();
        setupTextColorSwitch();
        setupEmptyDaysSwitch();

        /* The activity may have been launched by the system because the given widget
         * was just added. In this case, we need to perform some extra steps and setup
         * the widget. If the activity was simply opened by the user to configure an already
         * placed widget these actions will have no effect. */

        // Widget will not be finalized unless user presses the save configuration button.
        resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        setResult(RESULT_CANCELED, resultValue);

        // Perform all necessary initialization for the given widget.
        AgendaWidgetProvider.setupWidgets(this, new int[]{widgetId});
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionHelper.REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // Now that we have the new permission, we may have new data for the widgets.
                AgendaWidgetProvider.refreshAllWidgets(this);
            }
            finish();
        }
    }

    /* BEGIN configuration view handlers and setup methods. */

    /**
     * Setup the time period radio group to display either the default
     * time period value or the user selected time period (when available).
     */
    private void setupTimePeriodRadioGroup() {
        long timePeriod = configManager.getLong(R.string.config_time_period_key, DatetimeUtils.ONE_WEEK);

        int selected = R.id.rbutton_one_week;
        if (timePeriod == DatetimeUtils.ONE_DAY) {
            selected = R.id.rbutton_one_day;
        } else if (timePeriod == DatetimeUtils.ONE_WEEK * 2) {
            selected = R.id.rbutton_two_weeks;
        } else if (timePeriod == DatetimeUtils.ONE_MONTH) {
            selected = R.id.rbutton_one_month;
        }

        RadioGroup rGroup = (RadioGroup) findViewById(R.id.rgroup_time_period);
        rGroup.check(selected);
    }

    /**
     * Setup the relative time switch to display the default
     * value or the user selected preference (when available).
     */
    private void setupRelativeTimeSwitch() {
        boolean useRelativeTime = configManager.getBoolean(R.string.config_relative_time_key, false);
        Switch relTimeSwitch = (Switch) findViewById(R.id.switch_relative_time);

        relTimeSwitch.setChecked(useRelativeTime);
    }

    /**
     * Setup the text color switch to display the default
     * value or the user selected preference (when available).
     */
    private void setupTextColorSwitch() {
        boolean useWhiteTextTheme = configManager.getBoolean(R.string.config_text_color_key, true);
        Switch txtColorSwitch = (Switch) findViewById(R.id.switch_text_color);

        txtColorSwitch.setChecked(useWhiteTextTheme);
    }

    private void setupEmptyDaysSwitch() {
        boolean allowEmptyDays = configManager.getBoolean(R.string.config_allow_empty_days_key, false);
        Switch emptyDaySwitch = (Switch) findViewById(R.id.switch_empty_days);

        emptyDaySwitch.setChecked(allowEmptyDays);
    }

    /**
     * Handles selection of agenda time period (e.g. one day, one week, two weeks, or one month).
     */
    public void onRadioButtonClick(View v) {
        long agendaTimePeriod = -1;
        switch (v.getId()) {
            case R.id.rbutton_one_day:
                agendaTimePeriod = DatetimeUtils.ONE_DAY;
                break;
            case R.id.rbutton_two_weeks:
                agendaTimePeriod = DatetimeUtils.ONE_WEEK * 2;
                break;
            case R.id.rbutton_one_month:
                agendaTimePeriod = DatetimeUtils.ONE_MONTH;
                break;
            case R.id.rbutton_one_week:
                agendaTimePeriod = DatetimeUtils.ONE_WEEK;
        }

        if (agendaTimePeriod > 0 && configManager.setLong(R.string.config_time_period_key, agendaTimePeriod)) {
            wasConfigChanged = true;
        }
    }

    /**
     * Handles the two toggles (relative time and text color).
     */
    public void onSwitchClick(View v) {
        int key = 0;
        boolean value = false;

        switch (v.getId()) {
            case R.id.switch_relative_time:
                key = R.string.config_relative_time_key;
                value = ((Switch) v).isChecked();
                break;

            case R.id.switch_text_color:
                key = R.string.config_text_color_key;
                value = ((Switch) v).isChecked();
                break;

            case R.id.switch_empty_days:
                key = R.string.config_allow_empty_days_key;
                value = ((Switch) v).isChecked();
                break;
        }

        if (key != 0 && configManager.setBoolean(key, value)) {
            wasConfigChanged = true;

            if (key == R.string.config_text_color_key) {
                wasThemeChanged = true;
            }
        }
    }

    /* END configuration view handlers and setup methods. *
     * BEGIN action bar methods.                          */

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
                    if (wasThemeChanged) {
                        // Theme change requires completely new view group (for colored buttons).
                        AgendaWidgetProvider.setupWidgets(this, new int[]{widgetId});
                    }
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

    /* END action bar methods. */
}
