package com.ifthenelse.ejmoore2.agenda.model;

import android.support.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

/**
 * Created by edward on 1/16/17.
 */

public class Instance implements Comparable {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("MM-dd-yyyy", Locale.US);

    /* beginTime and endTime mark the beginning and
     * ending timestamps for an instance on a given Day. */
    private long beginTime;
    private long endTime;

    /* If an Instance spans multiple Days, then there will be separate Instance
     * objects for each Day. The trueBeginTime and trueEndTime fields hold the
     * true timestamps for the instance (as retrieved from the CalendarProvider).
     */
    private long trueBeginTime;
    private long trueEndTime;

    private Event event;

    Instance(long beginTime, long endTime, long trueBeginTime, Event event) {
        this(beginTime, endTime, trueBeginTime, endTime, event);
    }

    Instance(long beginTime, long endTime, long trueBeginTime, long trueEndTime, Event event) {
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.trueBeginTime = trueBeginTime;
        this.trueEndTime = trueEndTime;
        this.event = event;
    }

    public long getBeginTime() {
        return beginTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public long getTrueBeginTime() {
        return trueBeginTime;
    }

    public long getTrueEndTime() {
        return trueEndTime;
    }

    public boolean isAllDay() {
        return getEvent().isAllDay();
    }

    public boolean isMultiDay() {
        return getTrueEndTime() - getTrueBeginTime() > Agenda.ONE_DAY;
    }

    private Event getEvent() {
        return event;
    }

    String getStartDateString() {
        return SDF.format(new Date(getBeginTime()));
    }

    static SimpleDateFormat getDateFormat() {
        return SDF;
    }

    @Override
    public int compareTo(@NonNull Object obj) {
        if (obj instanceof Instance) {
            Instance other = (Instance) obj;

            // Order chronologically, then alphabetically by event title.
            int diff = (int) (this.getTrueBeginTime() - other.getTrueBeginTime());
            if (diff == 0) {
                return this.getTitle().compareTo(other.getTitle());
            } else {
                return diff;
            }
        }
        return 0;
    }

    /* Properties inherited from the parent Event. */

    public long getEventId() {
        return getEvent().getId();
    }

    public String getTitle() {
        return getEvent().getTitle();
    }

    public int getColor() {
        return getEvent().getColor();
    }
}
