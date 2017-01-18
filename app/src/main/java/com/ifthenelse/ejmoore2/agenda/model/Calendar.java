package com.ifthenelse.ejmoore2.agenda.model;

import android.content.Context;
import android.database.Cursor;
import android.provider.CalendarContract;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by edward on 1/16/17.
 */

class Calendar {

    private String displayName;
    private int color;

    Calendar(String displayName, int color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getColor() {
        return color;
    }

    public static Map<Long, Calendar> getVisibleCalendars(Context context) {
        String[] projecton = new String[]{
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
                CalendarContract.Calendars.CALENDAR_COLOR
        };
        String selection = CalendarContract.Calendars.VISIBLE + " = ?";
        String[] selectionArgs = new String[]{"1"};

        //noinspection MissingPermission
        Cursor cursor =
                context.getContentResolver()
                        .query(CalendarContract.Calendars.CONTENT_URI, projecton, selection, selectionArgs, null);
        Map<Long, Calendar> idToCalendarMap = new HashMap<>();
        if (cursor == null) {
            return idToCalendarMap;
        }

        while (cursor.moveToNext()) {
            long id = cursor.getLong(0);
            String displayName = cursor.getString(1);
            int color = cursor.getInt(2);
            idToCalendarMap.put(id, new Calendar(displayName, color));
        }

        cursor.close();
        return idToCalendarMap;
    }
}
