package com.ifthenelse.ejmoore2.agenda;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

/**
 * Created by ejmoore2 on 1/15/17.
 */

public class AgendaWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new AgendaViewFactory(this, intent);
    }

    private class AgendaViewFactory implements RemoteViewsFactory {

        private Context context;
        private int widgetId;

        AgendaViewFactory(Context context, Intent intent) {
            this.context = context;
            this.widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public RemoteViews getViewAt(int position) {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public void onCreate() {

        }

        @Override
        public void onDataSetChanged() {

        }

        @Override
        public void onDestroy() {

        }
    }
}
