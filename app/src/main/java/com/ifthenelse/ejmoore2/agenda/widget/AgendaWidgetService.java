package com.ifthenelse.ejmoore2.agenda.widget;

import android.Manifest;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.ifthenelse.ejmoore2.agenda.PermissionHelper;
import com.ifthenelse.ejmoore2.agenda.R;
import com.ifthenelse.ejmoore2.agenda.model.Agenda;

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

        private PermissionHelper ph;

        private Agenda agenda;

        AgendaViewFactory(Context context, Intent intent) {
            this.context = context;
            this.widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            this.ph = new PermissionHelper();
            this.agenda = Agenda.empty();
        }

        @Override
        public int getCount() {
            return agenda.getDays().length;
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
            RemoteViews rv = new RemoteViews(getPackageName(), R.layout.listitem_day);

            Agenda.Day day = agenda.getDays()[position];
            rv.setTextViewText(R.id.day_text, day.toString());

            return rv;
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
            refreshAgenda();
        }

        @Override
        public void onDataSetChanged() {
            refreshAgenda();
        }

        @Override
        public void onDestroy() {
        }

        private void refreshAgenda() {
            if (ph.checkPermission(context, Manifest.permission.READ_CALENDAR)) {
                agenda = Agenda.getAgendaForPeriod(context, Agenda.ONE_WEEK);
            } else {
                ph.notifyUserOfMissingPermission(context, Manifest.permission.READ_CALENDAR);
            }
        }
    }
}
