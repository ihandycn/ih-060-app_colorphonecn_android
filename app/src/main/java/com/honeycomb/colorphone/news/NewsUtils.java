package com.honeycomb.colorphone.news;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.format.DateUtils;

public class NewsUtils {
    public static String getImei(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String  imei = telephonyManager.getDeviceId();
        return imei;
    }

    public static String getNewsDate(long time) {
        return DateUtils.getRelativeTimeSpanString(time).toString();
    }

    public static String getNewsVideoLength(long length) {
        long hour = length / DateUtils.HOUR_IN_MILLIS;
        long minute = (length % DateUtils.HOUR_IN_MILLIS) / DateUtils.MINUTE_IN_MILLIS;
        long second = (length % DateUtils.MINUTE_IN_MILLIS) / DateUtils.SECOND_IN_MILLIS;

        StringBuilder sb = new StringBuilder();
        if (hour > 0) {
            sb.append(hour).append(":");
        }

        if (minute >= 10) {
            sb.append(minute).append(":");
        } else {
            sb.append("0").append(minute).append(":");
        }

        if (second >= 10) {
            sb.append(second);
        } else {
            sb.append("0").append(second);
        }

        return sb.toString();
    }
}
