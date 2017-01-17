package com.ifthenelse.ejmoore2.agenda.view;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RemoteViews;

import com.ifthenelse.ejmoore2.agenda.R;
import com.ifthenelse.ejmoore2.agenda.widget.AgendaWidgetService;

public class ConfigActivity extends AppCompatActivity implements View.OnClickListener {

    private Intent resultValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        Intent intent = getIntent();
        int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        setResult(RESULT_CANCELED, resultValue);

        Intent serviceIntent = new Intent(this, AgendaWidgetService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);

        RemoteViews rv = new RemoteViews(getPackageName(), R.layout.widget);
        rv.setRemoteAdapter(R.id.listview_days, serviceIntent);

        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        manager.updateAppWidget(widgetId, rv);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.save_config_button) {
            setResult(RESULT_OK, resultValue);
            finish();
        }
    }
}
