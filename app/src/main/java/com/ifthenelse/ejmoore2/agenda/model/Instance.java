package com.ifthenelse.ejmoore2.agenda.model;

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
            return (int) (a.getBeginTime() - b.getBeginTime());
        }
    };

    private long beginTime;
    private long endTime;

    private Event event;

    Instance(long beginTime, long endTime, Event event) {
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.event = event;
    }

    public Event getEvent() {
        return event;
    }

    public long getBeginTime() {
        return beginTime;
    }

    public long getEndTime() {
        return endTime;
    }

    String getStartDateString() {
        return SDF.format(new Date(beginTime));
    }

    static SimpleDateFormat getDateFormat() {
        return SDF;
    }

    static Comparator<Instance> getComparator() {
        return COMPARATOR;
    }
}
