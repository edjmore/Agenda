package com.ifthenelse.ejmoore2.agenda;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;

/**
 * Created by ejmoore2 on 1/15/17.
 */

public class AgendaWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.listview_days);
    }
}
