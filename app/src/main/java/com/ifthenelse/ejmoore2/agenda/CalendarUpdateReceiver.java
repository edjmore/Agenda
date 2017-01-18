package com.ifthenelse.ejmoore2.agenda;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ifthenelse.ejmoore2.agenda.widget.AgendaWidgetProvider;

public class CalendarUpdateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();

        if ("android.intent.action.PROVIDER_CHANGED".equals(intentAction)) {

            /* Calendar has been updated; request a widget refresh. */
            AgendaWidgetProvider.refreshAllWidgets(context);
        }
    }
}
