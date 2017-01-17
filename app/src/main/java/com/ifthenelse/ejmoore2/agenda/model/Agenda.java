package com.ifthenelse.ejmoore2.agenda.model;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by edward on 1/16/17.
 */

public class Agenda {

    public static long ONE_DAY = 1000 * 60 * 60 * 24;
    public static long ONE_WEEK = ONE_DAY * 7;
    public static long ONE_MONTH = ONE_DAY * 31;

    public class Day {
        private List<Instance> instances;
        private Instance[] sortedInstances;

        private Day() {
            this.instances = new ArrayList<>();
            this.sortedInstances = null;
        }

        public Instance[] getInstances() {
            if (sortedInstances == null) {
                sortedInstances = instances.toArray(new Instance[instances.size()]);
                Arrays.sort(sortedInstances, Instance.getComparator());
            }
            return sortedInstances;
        }

        private void addInstance(Instance instance) {
            instances.add(instance);
            sortedInstances = null;
        }

        private String getDateString() {
            if (instances.isEmpty()) {
                return "";
            } else {
                return instances.get(0).getStartDateString();
            }
        }

        public Date getDate() {
            String dateString = getDateString();
            try {
                SimpleDateFormat sdf = Instance.getDateFormat();
                return sdf.parse(dateString);
            } catch (ParseException e) {
                return new Date(0);
            }
        }
    }

    private static final Comparator<Day> DAY_COMPARATOR = new Comparator<Day>() {
        @Override
        public int compare(Day a, Day b) {
            return a.getDate().compareTo(b.getDate());
        }
    };

    private Map<String, Day> dateToDayMap;
    private Day[] sortedDays;

    private Agenda() {
        this.dateToDayMap = new HashMap<>();
        this.sortedDays = null;
    }

    public Day[] getDays() {
        if (sortedDays == null) {
            sortedDays = dateToDayMap.values().toArray(new Day[dateToDayMap.size()]);
            Arrays.sort(sortedDays, DAY_COMPARATOR);
        }
        return sortedDays;
    }

    private void addInstance(Instance instance) {
        String date = instance.getStartDateString();
        Day day = dateToDayMap.get(date);
        if (day == null) {
            day = new Day();
            dateToDayMap.put(date, day);
            sortedDays = null;
        }
        day.addInstance(instance);
    }

    public static Agenda getAgendaForPeriod(Context context, long period) {
        /* First, query calendar provider for all instances within the period. */
        String[] projection = new String[]{
                CalendarContract.Instances.EVENT_ID,
                CalendarContract.Instances.BEGIN,
                CalendarContract.Instances.END
        };
        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        long startTime = System.currentTimeMillis();
        ContentUris.appendId(builder, startTime);
        ContentUris.appendId(builder, startTime + period);

        Cursor cursor =
                context.getContentResolver().query(builder.build(), projection, null, null, null);
        Agenda agenda = Agenda.empty();
        if (cursor == null) {
            return agenda;
        }

        // One-to-many relationship from events to instances, and from calendars to events.
        Map<Long, Event> idToEventMap = new HashMap<>();
        Map<Integer, Calendar> idToCalendarMap = Calendar.getVisibleCalendars(context);

        /* Construct instance objects from the cursor, fetching event data as needed. */
        while (cursor.moveToNext()) {
            long eventId = cursor.getLong(0);
            long beginTime = cursor.getLong(1);
            long endTime = cursor.getLong(2);

            Event event = idToEventMap.get(eventId);
            if (event == null) {
                event = Event.getById(context, eventId, idToCalendarMap);

                /* The returned event will be null if its calendar has been
                 * hidden by the user, in which case we should ignore this instance. */
                if (event == null) {
                    continue;
                } else {
                    idToEventMap.put(eventId, event);
                }
            }

            Instance instance = new Instance(beginTime, endTime, event);
            agenda.addInstance(instance);
        }

        cursor.close();
        return agenda;
    }

    public static Agenda empty() {
        return new Agenda();
    }
}
