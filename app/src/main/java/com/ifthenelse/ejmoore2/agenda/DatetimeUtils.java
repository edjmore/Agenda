package com.ifthenelse.ejmoore2.agenda;

import android.support.v4.util.Pair;

import com.ifthenelse.ejmoore2.agenda.model.Agenda;
import com.ifthenelse.ejmoore2.agenda.model.Instance;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by edward on 1/18/17.
 */

public class DatetimeUtils {

    public static final long ONE_MINUTE = 1000 * 60;
    public static final long ONE_HOUR = ONE_MINUTE * 60;
    public static final long ONE_DAY = ONE_HOUR * 24;
    public static final long ONE_WEEK = ONE_DAY * 7;
    public static final long ONE_MONTH = ONE_DAY * 31;

    private static final SimpleDateFormat SDF = new SimpleDateFormat("EEEE, MMM d", Locale.US);
    private static final SimpleDateFormat WEEKDAY_SDF = new SimpleDateFormat("EEEE", Locale.US);
    private static final SimpleDateFormat COMPACT_SDF = new SimpleDateFormat("M/dd", Locale.US);

    private static final SimpleDateFormat STF = new SimpleDateFormat("h:mm a", Locale.US);

    public static String getDateString(Agenda.Day day, boolean useRelative) {
        return getDateString(day.getDate(), useRelative);
    }

    public static String getTimeString(Instance instance, boolean useRelative) {
        if (instance.isAllDay()) {
            return "all day";
        } else {
            return useRelative ? getRelativeTimeString(instance) : getExactTimeString(instance);
        }
    }

    /**
     * Returns the millis value of 12:00 AM on the previous day.
     */
    public static long getYesterday(long today) {
        return roundDown(today - ONE_DAY);
    }

    /* Returns the millis value of 12:00 AM on the subsequent day. */
    public static long getTomorrow(long today) {
        return roundDown(today + ONE_DAY);
    }

    /**
     * Rounds the given time up to 11:59 PM that day.
     */
    public static long roundUp(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * Rounds the given time down to 12:00 AM that day.
     */
    public static long roundDown(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private static TimeZone getLocalTimeZone() {
        return Calendar.getInstance().getTimeZone();
    }

    /* BEGIN date string methods. */

    private static String getDateString(Date date, boolean useRelative) {
        return useRelative ? getRelativeDateString(date) : getExactDateString(date, false);
    }

    private static String getExactDateString(Date date, boolean useCompact) {
        SimpleDateFormat sdf = useCompact ? COMPACT_SDF : SDF;
        sdf.setTimeZone(getLocalTimeZone());
        return sdf.format(date);
    }

    private static String getRelativeDateString(Date date) {
        return getRelativeDateString(date, false, false);
    }

    private static String getRelativeDateString(Date date, boolean useCompact, boolean useWeekday) {
        long nowTime = System.currentTimeMillis(),
                dateTime = date.getTime();

        String result = null;
        if (dateTime >= roundDown(nowTime) && dateTime <= roundUp(nowTime)) {
            result = "Today";
        } else if (dateTime >= getYesterday(nowTime) && dateTime <= roundUp(getYesterday(nowTime))) {
            result = "Yesterday";
        } else if (dateTime >= getTomorrow(nowTime) && dateTime <= roundUp(getTomorrow(nowTime))) {
            result = "Tomorrow";
        }

        if (result == null) {
            long elapsedTime = Math.abs(dateTime - nowTime);
            if (useWeekday && elapsedTime < ONE_WEEK - ONE_DAY) {
                WEEKDAY_SDF.setTimeZone(getLocalTimeZone());
                result = WEEKDAY_SDF.format(date);
            } else {
                result = getExactDateString(date, useCompact);
            }
        }

        return result;
    }

    /* END date string methods.   *
     * BEGIN time string methods. */

    private static String getExactTimeString(Instance instance) {
        STF.setTimeZone(getLocalTimeZone());
        Date trueBeginDate = new Date(instance.getTrueBeginTime()),
                trueEndDate = new Date(instance.getTrueEndTime());

        String beginTimeString = STF.format(trueBeginDate);
        if (instance.isMomentary()) {
            return beginTimeString;
        } else {
            String endTimeString = STF.format(trueEndDate);
            if (instance.isMultiDay()) {
                return String.format(Locale.US, "%s (%s) - %s (%s)",
                        beginTimeString, getRelativeDateString(trueBeginDate, true, true).toLowerCase(),
                        endTimeString, getRelativeDateString(trueEndDate, true, true).toLowerCase());
            } else {
                return beginTimeString + " - " + endTimeString;
            }
        }
    }

    private static String getRelativeTimeString(Instance instance) {
        return getRelativeTimeString(System.currentTimeMillis(), instance.getBeginTime(), instance.getEndTime());
    }

    private static String getRelativeTimeString(long nowTime, long beginTime, long endTime) {
        String result = "";

        long elapsedTime;
        if (nowTime < beginTime) {
            result = beginTime == endTime ? "occurs " : "begins ";
            elapsedTime = beginTime - nowTime;
        } else if (nowTime < endTime) {
            result = "ends ";
            elapsedTime = endTime - nowTime;
        } else {
            return result;
        }

        String timeString = getRelativeTimeString(elapsedTime, 4);
        result += timeString.equals("now") ? timeString : "in" + timeString;

        return result.trim();
    }

    private static String getRelativeTimeString(long elapsedTime, int precision) {
        String result = "";

        String[] tokens = getRelativeTimeStrings(elapsedTime);
        for (int i = 0; i < precision; i++) {
            result += " " + tokens[i];
        }

        return result.isEmpty() ? "now" : result;
    }

    private static String[] getRelativeTimeStrings(long elapsedTime) {
        String[] result = new String[4];

        for (int i = 0; i < result.length; i++) {
            Pair<String, Long> p = getApproxRelativeTimeString(elapsedTime);

            result[i] = p.first;
            elapsedTime = p.second;
        }

        return result;
    }

    private static Pair<String, Long> getApproxRelativeTimeString(long elapsedTime) {
        String unitStr;
        long unitValue;

        if (elapsedTime < ONE_MINUTE) {
            unitStr = "";
            unitValue = Math.max(elapsedTime, 1);
        } else if (elapsedTime < ONE_HOUR) {
            unitStr = " minute";
            unitValue = ONE_MINUTE;
        } else if (elapsedTime < ONE_DAY) {
            unitStr = " hour";
            unitValue = ONE_HOUR;
        } else if (elapsedTime < ONE_WEEK) {
            unitStr = " day";
            unitValue = ONE_DAY;
        } else {
            unitStr = " week";
            unitValue = ONE_WEEK;
        }

        long amount = elapsedTime / unitValue;
        long remainder = elapsedTime - amount * unitValue;

        String amountStr = "" + (amount != 0 ? amount : "");
        unitStr += (amount != 0 && amount != 1) ? "s" : "";

        return new Pair<>(amountStr + unitStr, remainder);
    }

    /* END time string methods. */
}
