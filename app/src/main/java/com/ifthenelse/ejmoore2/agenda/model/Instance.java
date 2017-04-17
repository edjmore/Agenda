package com.ifthenelse.ejmoore2.agenda.model;

import android.support.annotation.NonNull;

import com.ifthenelse.ejmoore2.agenda.util.DatetimeUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by edward on 1/16/17.
 */

public class Instance implements Comparable {

    /* beginTime and endTime mark the beginning and
     * ending timestamps for an instance on a given Day. */
    private long beginTime;
    private long endTime;

    /* If an Instance spans multiple Days, then there will be separate Instance
     * objects for each Day. The trueBeginTime and trueEndTime fields hold the
     * true timestamps for the instance (as retrieved from the CalendarProvider). */
    private long trueBeginTime;
    private long trueEndTime;

    private Event event;
    private int dupCount;

    Instance(long beginTime, long endTime, long trueBeginTime, long trueEndTime, Event event) {
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.trueBeginTime = trueBeginTime;
        this.trueEndTime = trueEndTime;
        this.event = event;
        this.dupCount = 1;
        //init();
    }

    /* private void init() {
        if (isAllDay()) {
            long offset = -1 *
                    DatetimeUtils.getLocalTimeZone().getOffset(System.currentTimeMillis());
            beginTime += offset;
            endTime += offset;
            trueBeginTime += offset;
            trueEndTime += offset;
        }
    } */

    /**
     * @return A string with the true event time values and event ID encoded within.
     */
    public String encodeInstance() {
        return getTrueBeginTime() + "-" + getTrueEndTime() + "-" + getEventId();
    }

    /**
     * @param encodedInstance A string encoded by Instance.encodeInstance().
     * @return An array of instance data ordered as follows: trueBeginTime, trueEndTime, eventId.
     */
    public static long[] decodeInstance(String encodedInstance) {
        String[] tokens = encodedInstance.split("-");
        return new long[]{
                Long.parseLong(tokens[0]),
                Long.parseLong(tokens[1]),
                Long.parseLong(tokens[2])
        };
    }

    void setDupCount(int dupCount) {
        this.dupCount = dupCount;
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
        return getTrueEndTime() > DatetimeUtils.roundUp(getTrueBeginTime());
    }

    public boolean isMomentary() {
        return getTrueBeginTime() == getTrueEndTime();
    }

    Event getEvent() {
        return event;
    }

    int getDupCount() { return dupCount; }

    @Override
    public int compareTo(@NonNull Object obj) {
        if (obj instanceof Instance) {
            Instance other = (Instance) obj;

            if ((this.isAllDay() || other.isAllDay()) && !(this.isAllDay() && other.isAllDay())) {
                // Exactly  one is all-day.
                return this.isAllDay() ? -1 : 1; // All-day event goes first.
            }

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
        return getEvent().getTitle() + (getDupCount() > 1 ? String.format(Locale.US, " (x%d)", getDupCount()) : "");
    }

    public int getColor() {
        return getEvent().getColor();
    }
}
