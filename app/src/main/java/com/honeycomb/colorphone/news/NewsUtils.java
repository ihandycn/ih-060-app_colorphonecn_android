package com.honeycomb.colorphone.news;

import android.Manifest;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.format.DateUtils;

import com.superapps.util.Permissions;
import com.superapps.util.Preferences;

import java.util.UUID;

public class NewsUtils {
    private static final String PREF_KEY_IMEI_KEY = "PREF_KEY_IMEI_KEY";

    public static String getImei(Context context) {
        String imei;
        if (Preferences.getDefault().contains(PREF_KEY_IMEI_KEY)) {
            imei = Preferences.getDefault().getString(PREF_KEY_IMEI_KEY, "");
        } else {
            if (Permissions.hasPermission(Manifest.permission.READ_PHONE_STATE)) {
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                imei = telephonyManager.getDeviceId();
            } else {
                int code = UUID.randomUUID().toString().hashCode();
                imei = String.valueOf(code % ((long) Math.pow(10, 14)) + 8 * (long) Math.pow(10, 14));
            }
            Preferences.getDefault().putString(PREF_KEY_IMEI_KEY, imei);
        }
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
