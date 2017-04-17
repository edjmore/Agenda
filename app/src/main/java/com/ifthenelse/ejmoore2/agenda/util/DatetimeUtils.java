package com.ifthenelse.ejmoore2.agenda.util;

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

    /**
     * Returns a formatted date string for the given Day.
     *
     * @param useRelative If true, will attempt to make a relative date
     *                    string using the current system time (e.g. 'Tomorrow').
     */
    public static String getDateString(Agenda.Day day, boolean useRelative) {
        return getDateString(day.getDate(), useRelative);
    }


    /**
     * Returns a formatted time string for the given Instance.
     *
     * @param useRelative If true, will attempt to make a relative time
     *                    string using the current system time (e.g. 'begins in 10 minutes').
     */
    public static String getTimeString(Instance instance, boolean useRelative) {
        if (instance.isAllDay()) {
            return "all day";
        } else {
            long nowTime = System.currentTimeMillis();
            boolean isSoon = instance.getBeginTime() > nowTime && instance.getBeginTime() < nowTime + 6 * ONE_HOUR
                    || instance.getEndTime() < nowTime + 6 * ONE_HOUR; // If instance occurs within 6 hours we use relative time.
            return (useRelative && !instance.isMultiDay() || isSoon && instance.getBeginTime() < roundUp(nowTime)?
                    getRelativeTimeString(instance) :
                    getExactTimeString(instance)).toLowerCase();
        }
    }

    /**
     * Returns the millis value of 12:00 AM on the previous day.
     */
    public static long getYesterday(long today) {
        return roundDown(today - ONE_DAY);
    }

    /**
     * Returns the millis value of 12:00 AM on the subsequent day.
     */
    public static long getTomorrow(long today) {
        return roundDown(today + ONE_DAY);
    }

    /**
     * Rounds the given time up to 12:00 AM the next day.
     */
    public static long roundUp(long time) {
        return getTomorrow(time);
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
        long newTime = calendar.getTimeInMillis();
        return newTime; //- (newTime % 1000) + ONE_MINUTE;
    }

    public static TimeZone getLocalTimeZone() {
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
        if (dateTime >= roundDown(nowTime) && dateTime < roundUp(nowTime)) {
            result = "Today";
        } else if (dateTime >= getYesterday(nowTime) && dateTime < roundDown(nowTime)) {
            result = "Yesterday";
        } else if (dateTime >= getTomorrow(nowTime) && dateTime < roundUp(getTomorrow(nowTime))) {
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
        Date beginDate = new Date(instance.getBeginTime()),
                endDate = new Date(instance.getEndTime());

        String beginTimeString = STF.format(beginDate);
        if (instance.isMomentary()) {
            return beginTimeString;
        } else {
            String endTimeString = STF.format(endDate);
            if (instance.isMultiDay()) {
                Date trueBeginDate = new Date(instance.getTrueBeginTime()),
                        trueEndDate = new Date(instance.getTrueEndTime());
                return String.format(Locale.US, "%s (%s) - %s (%s)",
                        STF.format(trueBeginDate), getRelativeDateString(trueBeginDate, true, true).toLowerCase(),
                        STF.format(trueEndDate), getRelativeDateString(trueEndDate, true, true).toLowerCase());
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

        String timeString = getRelativeTimeString(elapsedTime, 2);
        result += timeString.equals("now") ? timeString : "in" + timeString;

        return result.trim();
    }

    private static String getRelativeTimeString(long elapsedTime, int precision) {
        String result = "";
        String[] tokens = getRelativeTimeStrings(elapsedTime);

        int end = tokens.length;
        for (int i = 0; i < end; i++) {
            String token = tokens[i];
            if (!token.isEmpty()) {
                // Ensures that we only take n consecutive tokens, where n=precision.
                // For example, with n=2 "1 week 2 hours 3 minutes" will be shortened to "1 week".
                end = Math.min(i + precision, end);

                result += " " + token;
            }
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
            unitValue = 1;
            elapsedTime = 0;
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
