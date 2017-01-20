package com.ifthenelse.ejmoore2.agenda.model;

import android.content.Context;
import android.database.Cursor;
import android.provider.CalendarContract;

import java.util.Map;
import java.util.TimeZone;

/**
 * Created by edward on 1/16/17.
 */

class Event {

    private static final String[] PROJECTION = new String[]{
            CalendarContract.Events.CALENDAR_ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.EVENT_COLOR,
            CalendarContract.Events.ALL_DAY,
            CalendarContract.Events.EVENT_TIMEZONE
    };
    private static final String SELECTION = CalendarContract.Events._ID + " = ?";

    private static final int NO_COLOR = -1;

    private long id;
    private String title;
    private int color;
    private boolean isAllDay;
    private String timezone;

    private Calendar calendar;

    private Event(long id, String title, int color, Calendar calendar, boolean isAllDay, String timezone) {
        this.id = id;
        this.title = title;
        this.color = color;
        this.calendar = calendar;
        this.isAllDay = isAllDay;
        this.timezone = timezone;
    }

    public long getId() {
        return id;
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

    boolean isAllDay() {
        return isAllDay;
    }

    TimeZone getTimezone() {
        TimeZone tz = TimeZone.getTimeZone(timezone);
        return tz;
    }

    static Event getById(Context context, long id, Map<Long, Calendar> idToCalendarMap) {
        //noinspection MissingPermission
        Cursor cursor =
                context.getContentResolver()
                        .query(CalendarContract.Events.CONTENT_URI, PROJECTION, SELECTION, new String[]{id + ""}, null);
        if (cursor == null) {
            return null;
        }

        cursor.moveToFirst();
        long calendarId = cursor.getLong(0);
        String title = cursor.getString(1);
        int color = cursor.isNull(2) ? NO_COLOR : cursor.getInt(2);
        boolean isAllDay = cursor.getInt(3) != 0;
        String timezone = cursor.getString(4);
        cursor.close();

        Calendar calendar = idToCalendarMap.get(calendarId);
        if (calendar == null) {
            return null;
        }

        return new Event(id, title, color, calendar, isAllDay, timezone);
    }
}
