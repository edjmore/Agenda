package com.ifthenelse.ejmoore2.agenda;

import com.ifthenelse.ejmoore2.agenda.model.Agenda;
import com.ifthenelse.ejmoore2.agenda.model.Instance;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by edward on 1/18/17.
 */

public class Utils {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("EEEE, MMM d", Locale.US);
    private static final SimpleDateFormat STF = new SimpleDateFormat("h:mm a", Locale.US);
    private static final SimpleDateFormat SHORT_SDF = new SimpleDateFormat("M/dd", Locale.US);
    private static final SimpleDateFormat DAY_SDF = new SimpleDateFormat("EEEE", Locale.US);

    /* Rounds the given time up to 11:59 PM that day. */
    public static long roundUp(long time) {
        Calendar calendar = Calendar.getInstance(Calendar.getInstance().getTimeZone(), Locale.US);
        calendar.setTimeInMillis(time);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /* Rounds the given time down to 12:00 AM that day. */
    public static long roundDown(long time) {
        Calendar calendar = Calendar.getInstance(Calendar.getInstance().getTimeZone(), Locale.US);
        calendar.setTimeInMillis(time);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /* Returns the millis value of 12:00 AM on the previous day. */
    public static long getYesterday(long today) {
        return Utils.roundDown(today - Agenda.ONE_DAY);
    }

    /* Returns the millis value of 12:00 AM on the subsequent day. */
    public static long getTomorrow(long today) {
        return Utils.roundDown(today + Agenda.ONE_DAY);
    }

    public static String getExactTimeString(Instance instance) {
        if (instance.isAllDay()) {
            return "all day";
        }

        String beginString = STF.format(new Date(instance.getActualBeginTime()));
        if (instance.getActualBeginTime() == instance.getActualEndTime()) {
            return beginString;
        } else {
            if (instance.isMultiDay()) {
                // Need to give the date as well as time since instance spans multiple days.
                Date actualEndDate = new Date(instance.getActualEndTime());
                return String.format(Locale.US, "%s (%s) - %s (%s)",
                        beginString, Utils.getRelativeDateString(new Date(instance.getActualBeginTime()), true).toLowerCase(),
                        STF.format(actualEndDate), Utils.getRelativeDateString(actualEndDate, true).toLowerCase());
            }

            return beginString + " - " + STF.format(new Date(instance.getEndTime()));
        }
    }

    public static String getRelativeDateString(Date date) {
        return getRelativeDateString(date, false);
    }

    public static String getRelativeDateString(Date date, boolean compact) {
        String relString = getRelativeDateString(date, compact ? SHORT_SDF : SDF);
        if (relString.equals("Yesterday") || relString.equals("Today") || relString.equals("Tomorrow")) {
            return relString;
        } else {
            // If we want a compact date and the day in question
            // is within a week, we just use the day (e.g. "saturday").
            long timeDiff = date.getTime() - System.currentTimeMillis();
            if (compact && timeDiff < Agenda.ONE_WEEK - Agenda.ONE_DAY) {
                return DAY_SDF.format(date);
            } else {
                return relString;
            }
        }
    }

    private static String getRelativeDateString(Date date, SimpleDateFormat format) {
        String dateString = format.format(date);

        long currTime = System.currentTimeMillis();
        Date today = new Date(currTime),
                tomorrow = new Date(currTime + Agenda.ONE_DAY),
                yesterday = new Date(currTime - Agenda.ONE_DAY);

        String relativeDateString = dateString;
        if (format.format(today).equals(dateString)) {
            relativeDateString = "Today";
        } else if (format.format(tomorrow).equals(dateString)) {
            relativeDateString = "Tomorrow";
        } else if (format.format(yesterday).equals(dateString)) {
            relativeDateString = "Yesterday";
        }
        return relativeDateString;
    }

    public static String getRelativeEventTimeString(Instance instance) {
        if (instance.isAllDay()) {
            return "all day";
        }

        long currTime = System.currentTimeMillis();
        int mins = (int) ((instance.getActualBeginTime() - currTime) / (1000 * 60));
        String innerMsg = null;
        if (mins > 0) {
            innerMsg = instance.getActualBeginTime() == instance.getActualEndTime() ? "occurs" : "begins";
        } else {
            innerMsg = "ends";
            mins = (int) ((instance.getEndTime() - currTime) / (1000 * 60));

            // If the event is multi-day we just say the day it will end.
            if (instance.isMultiDay()) {
                return innerMsg + " " +
                        Utils.getRelativeDateString(new Date(instance.getActualEndTime()), true).toLowerCase();
            }
        }

        // Reduce the time unit to a sensible value and ensure grammatical correctness.
        int timePeriod = mins;
        String timeUnit = "minutes";
        if (timePeriod >= 60 * 24) {
            timePeriod /= (60 * 24);
            timeUnit = "days";
        } else if (timePeriod >= 60) {
            timePeriod /= 60;
            timeUnit = "hours";
        }
        if (timePeriod == 1) {
            timeUnit = timeUnit.substring(0, timeUnit.length() - 1); // removing the 's'
        }
        innerMsg += timePeriod == 0 ? " now" : " in";

        return String.format(Locale.US, "%s%s", innerMsg,
                timePeriod == 0 ? "" : " " + timePeriod + " " + timeUnit);
    }
}
