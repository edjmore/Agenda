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

    // If the "color" field is set to NO_COLOR the Event color will be used.
    private static final int NO_COLOR = -1;

    private long id;
    private String title;
    private int color;
    private boolean isAllDay;
    private String timezone;

    private Calendar calendar;

    private Event(long id, String title, int color, boolean isAllDay, String timezone, Calendar calendar) {
        this.id = id;
        this.title = title;
        this.color = color;
        this.isAllDay = isAllDay;
        this.timezone = timezone;
        this.calendar = calendar;
    }

    public long getId() {
        return id;
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

    public Calendar getCalendar() {
        return calendar;
    }

    TimeZone getTimezone() {
        return TimeZone.getTimeZone(timezone);
    }

    /**
     * Returns the Event with the given ID, or null if:
     * (1) the event doesn't exist
     * (2) the event exists but is part of a hidden calendar
     *
     * @param id              The event ID to look for.
     * @param idToCalendarMap A mapping of all visible calendars.
     */
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

        return new Event(id, title, color, isAllDay, timezone, calendar);
    }
}
