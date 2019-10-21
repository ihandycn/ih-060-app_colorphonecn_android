package com.honeycomb.colorphone.wallpaper.util;

import android.annotation.SuppressLint;

import com.ihs.commons.utils.HSLog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUtils {

    private static final String TAG = TimeUtils.class.getSimpleName();

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static long strToMills(String dateStr) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        try {
            Date date = dateFormat.parse(dateStr);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String milllsToStr(long mills) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        Date date = new Date();
        date.setTime(mills);
        return dateFormat.format(date);
    }

    public static long getMillsDistance24() {
        String timeStr = getCurrentYMD() + " 24:00:00";
        return strToMills(timeStr) - System.currentTimeMillis();
    }

    /**
     * Get current year/month/day
     *
     * @return
     */
    public static String getCurrentYMD() {
        long currentMills = System.currentTimeMillis();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        date.setTime(currentMills);
        return dateFormat.format(date);
    }

    /**
     * Get current hour in 24 format
     *
     * @return
     */
    public static int getHourInDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        return calendar.get(Calendar.HOUR_OF_DAY);
    }


    public static int getHourInDay(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    public static boolean isMorning() {
        int hour = TimeUtils.getHourInDay();
        boolean enable = hour >= 5 && hour <= 11;
        HSLog.d(TAG, "isMorning enable = " + enable);
        return enable;
    }

    public static boolean isAfternoon() {
        int hour = TimeUtils.getHourInDay();
        boolean enable = hour >= 12 && hour <= 17;
        HSLog.d(TAG, "isAfternoon enable = " + enable);
        return enable;
    }

    public static boolean isEvening() {
        int hour = TimeUtils.getHourInDay();
        boolean enable = hour >= 18 && hour <= 21;
        HSLog.d(TAG, "isEvening enable = " + enable);
        return enable;
    }

    public static boolean isNight() {
        int hour = TimeUtils.getHourInDay();
        boolean enable = hour >= 22 || hour <= 4;
        HSLog.d(TAG, "isNight enable = " + enable);
        return enable;
    }

    public static boolean inSleepTime() {
        long time = System.currentTimeMillis();
        int hour = TimeUtils.getHourInDay(time);
        return 6 > hour || hour >= 23;
    }

    public static boolean isSameDay(long day1, long day2) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        return fmt.format(day1).equals(fmt.format(day2));
    }
}
