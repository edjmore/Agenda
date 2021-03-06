package com.ifthenelse.ejmoore2.agenda.model;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ifthenelse.ejmoore2.agenda.util.DatetimeUtils;

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
        private long timestamp;

        private Day(long timestamp) {
            this.instances = new ArrayList<>();
            this.sortedInstances = null;
            this.timestamp = timestamp;
        }

        public boolean isEmpty() {
            return instances.isEmpty();
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
            // We may already have an instance with the same exact title (for some holidays). In that
            // case we collapse all the "duplicates" into one.
            for (Instance inst : instances) {
                if (inst.getEvent().getTitle().equals(instance.getTitle())) {
                    inst.setDupCount(inst.getDupCount() + 1);
                    return; // No need to add duplicate, and still sorted.
                }
            }
            instances.add(instance);
            sortedInstances = null;
        }

        @Override
        public int compareTo(@NonNull Object obj) {
            if (obj instanceof Day) {
                Day other = (Day) obj;
                return (int) (this.getTimestamp() - other.getTimestamp());
            }
            return 0;
        }

        public Date getDate() {
            return new Date(getTimestamp());
        }

        private long getTimestamp() {
            return timestamp;
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
        Day day = getDay(instance.getBeginTime());
        day.addInstance(instance);
    }

    /**
     * Returns the Day associated with the given date
     * timestamp (in millis), creating the Day if necessary.
     */
    private Day getDay(long date) {
        date = DatetimeUtils.roundDown(date);

        Day day = dateToDayMap.get(date);
        if (day == null) {
            day = new Day(date);
            dateToDayMap.put(date, day);
            sortedDays = null;
        }
        return day;
    }

    public static Agenda getAgendaForPeriod(Context context, long period, boolean allowEmptyDays) {
        /* First, query calendar provider for all instances within the period. */
        final long nowTime = System.currentTimeMillis();
        String[] projection = new String[]{
                CalendarContract.Instances.EVENT_ID,
                CalendarContract.Instances.BEGIN,
                CalendarContract.Instances.END
        };
        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        long startTime = DatetimeUtils.roundDown(nowTime),
                finishTime = DatetimeUtils.roundUp(startTime + period);
        ContentUris.appendId(builder, startTime);
        ContentUris.appendId(builder, finishTime);

        Cursor cursor =
                context.getContentResolver().query(builder.build(), projection, null, null, null);
        Agenda agenda = Agenda.empty();
        if (cursor == null) {
            return agenda;
        }

        // Initialize Day objects for each date iff we want to allow empty days.
        for (long date = startTime; allowEmptyDays && date < finishTime; date += DatetimeUtils.ONE_DAY) {
            agenda.getDay(date);
        }

        // One-to-many relationship from events to instances, and from calendars to events.
        Map<Long, Event> idToEventMap = new HashMap<>();
        Map<Long, Calendar> idToCalendarMap = Calendar.getVisibleCalendars(context);

        /* Construct Instance objects from the cursor, fetching Event data as needed. */
        while (cursor.moveToNext()) {
            long eventId = cursor.getLong(0);
            long trueBeginTime = cursor.getLong(1);
            long trueEndTime = cursor.getLong(2);

            // One minute is the smallest time unit you can enter in the calendar app.
            trueBeginTime -= trueBeginTime % DatetimeUtils.ONE_MINUTE;
            trueEndTime -= trueEndTime % DatetimeUtils.ONE_MINUTE;

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

            // We filter out events that are not all day today and have already finished.
            if (trueEndTime < nowTime && !event.isAllDay()) {
                continue;
            }

            // All day events need to be translated from GMT.
            long offset = 0;
            if (event.isAllDay()) {
                offset = -1 *
                        DatetimeUtils.getLocalTimeZone().getOffset(System.currentTimeMillis());
                Log.e("all-day", String.format("%s: %d - %d (%d)",
                        event.getTitle(), trueBeginTime, trueEndTime, (trueEndTime - trueBeginTime) / (1000 * 60 * 60)));
                trueBeginTime += offset;
                trueEndTime += offset;
            } else {
                //Log.e("not all-day", String.format("%s: %d - %d (%d)",
                //        event.getTitle(), trueBeginTime, trueEndTime, (trueEndTime - trueBeginTime) / (1000 * 60 * 60)));
            }

            //Log.e("Agenda", String.format("%d, %s, %s, %d, %d",
            //        eventId, event.getTitle(), event.getCalendar().getDisplayName(), trueBeginTime, trueEndTime));

            /* Events may span multiple days, in which case we create
             * a separate instance for each day the event occurs during. */
            long beginTime = trueBeginTime;
            long endTime;
            do {
                endTime = Math.min(trueEndTime, DatetimeUtils.roundUp(beginTime));
                if (event.isAllDay()) Log.e("loop1",
                        event.getTitle() + ": " + beginTime + " - " + endTime +
                                " (" + (endTime - beginTime) / (1000 * 60) + ")");

                if (endTime > nowTime) { // Skip instances that have already passed.
                    Instance instance = new Instance(beginTime, endTime, trueBeginTime, trueEndTime, event);
                    agenda.addInstance(instance);
                }

                beginTime = endTime;

                if (event.isAllDay()) Log.e("loop2",
                        event.getTitle() + ": " + beginTime + " - " + endTime +
                                " (" + (endTime - beginTime) / (1000 * 60) + ")");
            } while (endTime < trueEndTime);
        }

        cursor.close();
        return agenda;
    }

    public static Agenda empty() {
        return new Agenda();
    }
}
