package com.honeycomb.colorphone.wallpaper.util;

import android.text.format.DateUtils;

import java.util.Calendar;
import java.util.Locale;


/**
 * Log flurry event. or do some conditional judgment.
 */

public class EventUtils {

    public static String getScreenIndexDescription(int pageIndex) {
        if (0 <= pageIndex && pageIndex <= 8) {
            return String.format(Locale.getDefault(), "Screen_%d", pageIndex + 1);
        } else if (pageIndex >= 9) {
            return "Screen_10_Or_After";
        }
        return "Invalid_Screen";
    }

    public static String getTimeOfDayDescription() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour >= 24) {
            return "23~24";
        } else if (hour <= 0) {
            return "0~1";
        } else {
            return String.valueOf(hour + "~" + (hour + 1));
        }
    }

    public static String getDurationDescription(long durationMillis) {
        String unit;
        int quantity;
        long durationAbs = Math.abs(durationMillis);
        if (durationAbs >= DateUtils.WEEK_IN_MILLIS) {
            unit = "weeks";
            quantity = (int) (durationMillis / DateUtils.WEEK_IN_MILLIS);
        } else if (durationAbs >= DateUtils.DAY_IN_MILLIS) {
            unit = "days";
            quantity = (int) (durationMillis / DateUtils.DAY_IN_MILLIS);
        } else if (durationAbs >= DateUtils.HOUR_IN_MILLIS) {
            unit = "hours";
            quantity = (int) (durationMillis / DateUtils.HOUR_IN_MILLIS);
        } else if (durationAbs >= DateUtils.MINUTE_IN_MILLIS) {
            unit = "min";
            quantity = (int) (durationMillis / DateUtils.MINUTE_IN_MILLIS);
        } else if (durationAbs >= DateUtils.SECOND_IN_MILLIS) {
            unit = "s";
            quantity = (int) (durationMillis / DateUtils.SECOND_IN_MILLIS);
        } else {
            unit = "ms";
            quantity = (int) durationMillis;
        }
        return getQuantityIntervalDescription(quantity) + " " + unit;
    }

    public static String getOpeningCategoryRate(int localCount, int onlineCount) {
        if (onlineCount == 0) {
            return "no_online_result";
        }
        int rate = (int) (localCount * 1.0f / onlineCount * 100);
        return (rate / 5 * 5) + "-" + (rate / 5 * 5 + 5) + "%";
    }

    public static String getQuantityIntervalDescription(int quantity) {
        if (quantity == 0) {
            return "0";
        }
        StringBuilder description = new StringBuilder();
        if (quantity < 0) {
            if (quantity == Integer.MIN_VALUE) {
                return "- 2000000000+"; // -2147483648 must be specially handled as +2147483648 overflows integer range
            }
            description.append("- ");
            quantity = -quantity;
        }
        if (quantity >= 2000000000) {
            // Overflow (implies hardcoded value of Integer.MAX_VALUE as 2<sup>31</sup>-1)
            return description.append("2000000000+").toString();
        }
        int coefficient = quantity;
        int tensOrder = 1;
        while (coefficient >= 10) {
            tensOrder *= 10;
            coefficient /= 10;
        }
        if (tensOrder == 1) {
            // eg. 1, 2, ..., 9
            description.append(coefficient);
        } else if (coefficient == 1) {
            // For a quantity with coefficient between 1 and 2, get us a description with more accuracy
            int fifteenThreshold = 15 * (tensOrder / 10);
            if (quantity < fifteenThreshold) {
                // eg. 100~150
                description.append(coefficient * tensOrder).append("~").append(fifteenThreshold);
            } else {
                // eg. 150~200
                description.append(fifteenThreshold).append("~").append((coefficient + 1) * tensOrder);
            }
        } else {
            // eg. 200~300, ..., 800~900
            description.append(coefficient * tensOrder).append("~").append((coefficient + 1) * tensOrder);
        }
        return description.toString();
    }
}
