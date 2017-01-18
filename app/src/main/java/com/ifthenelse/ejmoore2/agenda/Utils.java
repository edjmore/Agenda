package com.ifthenelse.ejmoore2.agenda;

import com.ifthenelse.ejmoore2.agenda.model.Agenda;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by edward on 1/18/17.
 */

public class Utils {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("EEEE, MMM d", Locale.US);

    public static String getRelativeDateString(Date date) {
        String dateString = SDF.format(date);

        long currTime = System.currentTimeMillis();
        Date today = new Date(currTime),
                tomorrow = new Date(currTime + Agenda.ONE_DAY);

        String relativeDateString = dateString;
        if (SDF.format(today).equals(dateString)) {
            relativeDateString = "Today";
        } else if (SDF.format(tomorrow).equals(dateString)) {
            relativeDateString = "Tomorrow";
        }
        return relativeDateString;
    }

    public static String getRelativeEventTimeString(long beginTime, long endTime) {
        long currTime = System.currentTimeMillis();
        int mins = (int) ((beginTime - currTime) / (1000 * 60));
        String innerMsg = null;
        if (mins > 0) {
            innerMsg = "begins";
        } else {
            innerMsg = "ends";
            mins = (int) ((endTime - currTime) / (1000 * 60));
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
