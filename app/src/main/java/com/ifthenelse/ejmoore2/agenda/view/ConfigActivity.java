package com.ifthenelse.ejmoore2.agenda.view;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.RemoteViews;

import com.ifthenelse.ejmoore2.agenda.ConfigManager;
import com.ifthenelse.ejmoore2.agenda.R;
import com.ifthenelse.ejmoore2.agenda.model.Agenda;
import com.ifthenelse.ejmoore2.agenda.widget.AgendaWidgetService;

public class ConfigActivity extends AppCompatActivity implements View.OnClickListener {

    private int widgetId;
    private Intent resultValue;
    private AppWidgetManager widgetManager;

    private ConfigManager configManager;
    private boolean wasConfigChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        Intent intent = getIntent();
        widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        configManager = new ConfigManager(this, widgetId);

        // Widget will not be finalized unless user presses the save configuration button.
        resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        setResult(RESULT_CANCELED, resultValue);

        /* Initial widget setup, linking update service and initializing views. */
        Intent serviceIntent = new Intent(this, AgendaWidgetService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);

        RemoteViews rv = new RemoteViews(getPackageName(), R.layout.widget);
        rv.setRemoteAdapter(R.id.listview_days, serviceIntent);
        rv.setEmptyView(R.id.listview_days, R.id.empty_view);

        // Setup settings button on widget to open configuration activity.
        Intent openConfigIntent = new Intent(this, ConfigActivity.class)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, openConfigIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.button_open_config, pendingIntent);

        widgetManager = AppWidgetManager.getInstance(this);
        widgetManager.updateAppWidget(widgetId, rv);

        /* Initialize all the configuration views to default/current values. */
        setupTimePeriodRadioGroup();
    }

    public void onRadioButtonClick(View v) {
        long agendaTimePeriod;
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
            default:
                agendaTimePeriod = Agenda.ONE_WEEK;
        }

        if (configManager.setLong(R.string.config_time_period_key, agendaTimePeriod)) {
            wasConfigChanged = true;
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.save_config_button) {
            setResult(RESULT_OK, resultValue);

            if (wasConfigChanged) {
                widgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.listview_days);
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
}
