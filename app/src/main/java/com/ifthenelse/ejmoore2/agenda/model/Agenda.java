package com.ifthenelse.ejmoore2.agenda.model;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;

import com.ifthenelse.ejmoore2.agenda.DatetimeUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by edward on 1/16/17.
 */

public class Agenda {

    /**
     * The Day class holds a list of Instances for a given date in the Agenda.
     */
    public class Day implements Comparable {
        private List<Instance> instances;
        private Instance[] sortedInstances;

        private Day() {
            this.instances = new ArrayList<>();
            this.sortedInstances = null;
        }

        /**
         * @return An Instance array sorted chronologically from earliest to latest.
         */
        public Instance[] getSortedInstances() {
            if (sortedInstances == null) {
                sortedInstances = instances.toArray(new Instance[instances.size()]);
                Arrays.sort(sortedInstances);
            }
            return sortedInstances;
        }

        private void addInstance(Instance instance) {
            instances.add(instance);
            sortedInstances = null;
        }

        private long getTimestamp() {
            return instances.isEmpty() ? 0 : DatetimeUtils.roundDown(getSortedInstances()[0].getBeginTime());
        }

        @Override
        public int compareTo(@NonNull Object obj) {
            if (obj instanceof Day) {
                Day other = (Day) obj;
                return (int) (this.getTimestamp() - other.getTimestamp());
            }
            return 0;
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

    private Map<Long, Day> dateToDayMap;
    private Day[] sortedDays;

    private Agenda() {
        this.dateToDayMap = new HashMap<>();
        this.sortedDays = null;
    }

    /**
     * @return A Day array sorted chronologically from earliest to latest.
     */
    public Day[] getSortedDays() {
        if (sortedDays == null) {
            sortedDays = dateToDayMap.values().toArray(new Day[dateToDayMap.size()]);
            Arrays.sort(sortedDays);
        }
        return sortedDays;
    }

    /**
     * Adds the given Instance to the Agenda by finding
     * the correct Day for the Instance and adding it there.
     */
    private void addInstance(Instance instance) {
        long date = DatetimeUtils.roundDown(instance.getBeginTime());
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
        Map<Long, Calendar> idToCalendarMap = Calendar.getVisibleCalendars(context);

        /* Construct Instance objects from the cursor, fetching Event data as needed. */
        while (cursor.moveToNext()) {
            long eventId = cursor.getLong(0);
            long trueBeginTime = cursor.getLong(1);
            long trueEndTime = cursor.getLong(2);

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

            // TODO: This code may be unnecessary??
            /*long actualBeginTime = trueBeginTime;
            if (event.isAllDay()) {
                TimeZone localTz = java.util.Calendar.getInstance().getTimeZone();
                actualBeginTime = convertToLocalTime(actualBeginTime, localTz);
                trueBeginTime = actualBeginTime;
                trueEndTime = convertToLocalTime(trueEndTime, localTz) - 1000;
            } */

            /* Events may span multiple days, in which case we create
             * a separate instance for each day the event occurs during. */
            long beginTime = trueBeginTime;
            long endTime;
            while (trueEndTime - beginTime > DatetimeUtils.ONE_DAY) {
                endTime = DatetimeUtils.roundUp(trueBeginTime);

                Instance instance = new Instance(beginTime, endTime, trueBeginTime, trueEndTime, event);
                agenda.addInstance(instance);

                beginTime = DatetimeUtils.getTomorrow(endTime);
            }
            // Add the final instance (with true end time).
            Instance instance = new Instance(beginTime, trueEndTime, trueBeginTime, trueEndTime, event);
            agenda.addInstance(instance);
        }

        cursor.close();
        return agenda;
    }

    private static long convertToLocalTime(long time, TimeZone localTz) {
        return time - localTz.getOffset(time);
    }

    public static Agenda empty() {
        return new Agenda();
    }
}
