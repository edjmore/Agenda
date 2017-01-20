package com.ifthenelse.ejmoore2.agenda.model;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

/**
 * Created by edward on 1/16/17.
 */

public class Instance {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("MM-dd-yyyy", Locale.US);

    private static final Comparator<Instance> COMPARATOR = new Comparator<Instance>() {
        @Override
        public int compare(Instance a, Instance b) {
            int diff = (int) (a.getActualBeginTime() - b.getActualBeginTime());
            if (diff == 0) {
                return a.getTitle().compareTo(b.getTitle());
            } else {
                return diff;
            }
        }
    };

    private long beginTime;
    private long endTime;
    private long actualBeginTime;
    private long actualEndTime;

    private Event event;

    Instance(long beginTime, long endTime, long actualBeginTime, Event event) {
        this(beginTime, endTime, actualBeginTime, endTime, event);
    }

    Instance(long beginTime, long endTime, long actualBeginTime, long actualEndTime, Event event) {
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.actualBeginTime = actualBeginTime;
        this.actualEndTime = actualEndTime;
        this.event = event;

        Log.e("Instance", getTitle() + " " + getStartDateString());
    }

    private Event getEvent() {
        return event;
    }

    public long getBeginTime() {
        return beginTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public long getActualBeginTime() {
        return actualBeginTime;
    }

    public long getActualEndTime() {
        return actualEndTime;
    }

    public boolean isAllDay() {
        return getEvent().isAllDay();
    }

    public boolean isMultiDay() {
        return getActualEndTime() - getActualBeginTime() > Agenda.ONE_DAY;
    }

    public long getEventId() {
        return getEvent().getId();
    }

    public String getTitle() {
        return getEvent().getTitle();
    }

    public int getColor() {
        return getEvent().getColor();
    }

    String getStartDateString() {
        return SDF.format(new Date(getBeginTime()));
    }

    static SimpleDateFormat getDateFormat() {
        return SDF;
    }

    static Comparator<Instance> getComparator() {
        return COMPARATOR;
    }
}
