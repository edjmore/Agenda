package com.ifthenelse.ejmoore2.agenda.model;

import android.content.Context;
import android.database.Cursor;
import android.provider.CalendarContract;

import java.util.Map;

/**
 * Created by edward on 1/16/17.
 */

class Event {

    private static final String[] PROJECTION = new String[]{
            CalendarContract.Events.CALENDAR_ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.EVENT_COLOR
    };
    private static final String SELECTION = CalendarContract.Events._ID + " = ?";

    private static final int NO_COLOR = -1;

    private String title;
    private int color;

    private Calendar calendar;

    private Event(String title, int color, Calendar calendar) {
        this.title = title;
        this.color = color;
        this.calendar = calendar;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public String getTitle() {
        return title;
    }

    public int getColor() {
        return color != NO_COLOR ? color : getCalendar().getColor();
    }

    static Event getById(Context context, int id, Map<Integer, Calendar> idToCalendarMap) {
        //noinspection MissingPermission
        Cursor cursor =
                context.getContentResolver()
                        .query(CalendarContract.Events.CONTENT_URI, PROJECTION, SELECTION, new String[]{id + ""}, null);
        if (cursor == null) {
            return null;
        }

        cursor.moveToFirst();
        int calendarId = cursor.getInt(0);
        String title = cursor.getString(1);
        int color = cursor.isNull(2) ? NO_COLOR : cursor.getInt(2);

        Calendar calendar = idToCalendarMap.get(calendarId);
        if (calendar == null) {
            return null;
        }

        cursor.close();
        return new Event(title, color, calendar);
    }
}
