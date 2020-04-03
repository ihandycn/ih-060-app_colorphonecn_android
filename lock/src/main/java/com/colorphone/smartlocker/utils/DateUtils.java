package com.colorphone.smartlocker.utils;

import android.content.ContentResolver;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.connection.HSHttpConnection;
import com.ihs.commons.connection.httplib.HttpRequest;
import com.ihs.commons.utils.HSError;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    public interface ServerTimeCallback {
        void onFinish(long serverTime);
    }

    public static final String[] MONTH_FULL_LABELS = new String[]{"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
    public static final String[] MONTH_SHORT_LABELS = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    public static final String[] WEEK_SHORT_LABELS = new String[]{"Sun", "Mon", "Tue", "Wed", "Thr", "Fri", "Sat"};

    public static String getSystemDateStringSomeDaysAgo(int some) {
        Date daysAgoBeginDate = getDaysAgoBeginDate(some);
        return DateFormat.getDateInstance().format(daysAgoBeginDate);
    }

    public static void getServerTime(@NonNull final ServerTimeCallback callback) {
        String server = HSConfig.optString("", "Application", "Incentive", "Server");
        HSHttpConnection connection = new HSHttpConnection(server, HttpRequest.Method.POST);
        connection.setConnectionFinishedListener(new HSHttpConnection.OnConnectionFinishedListener() {
            @Override
            public void onConnectionFinished(HSHttpConnection hsHttpConnection) {
                try {
                    String dateStr = hsHttpConnection.getHeaderField("Date");
                    SimpleDateFormat sdf = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.UK);
                    Date date = sdf.parse(dateStr);

                    callback.onFinish(Math.abs(date.getTime()));
                } catch (Exception e) {
                    callback.onFinish(System.currentTimeMillis());
                }
            }

            @Override
            public void onConnectionFailed(HSHttpConnection hsHttpConnection, HSError hsError) {
                try {
                    String dateStr = hsHttpConnection.getHeaderField("Date");
                    SimpleDateFormat sdf = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.UK);
                    Date date = sdf.parse(dateStr);

                    callback.onFinish(Math.abs(date.getTime()));
                } catch (Exception e) {
                    callback.onFinish(System.currentTimeMillis());
                }
            }
        });
        connection.startAsync();
    }

    public static long getCurrentDayStartInMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static long getCurrentDayEndInMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }

    /**
     * 以0:00为起点，计算和现在的日期差了多少天
     */
    public static int getHowManyDaysAfter(long beforeTimeMills) {
        Date currentDate = new Date();
        Date endTimeOfDate = getEndTimeOfDate(currentDate);
        return (int) ((endTimeOfDate.getTime() - beforeTimeMills) / 86400000L);
    }

    /**
     * 得到amount天前凌晨0:00的Date
     */
    public static Date getDaysAgoBeginDate(int amount) {
        Date date = new Date();
        Date amountDayAgo = dateChange(Calendar.DAY_OF_YEAR, -amount, date);
        return getBeginTimeOfDate(amountDayAgo);
    }

    /**
     * 为日期增减时间
     */
    public static Date dateChange(int dateKind, int amount, Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(dateKind, amount);
        return calendar.getTime();
    }

    /**
     * 得到某日期的起始TimeStamp
     */
    public static Date getBeginTimeOfDate(Date date) {
        Calendar cal = Calendar.getInstance();
        Calendar ret = Calendar.getInstance();
        cal.setTime(date);
        ret.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal
                .get(Calendar.DATE), 0, 0, 0);
        return ret.getTime();
    }

    /**
     * 得到某日期的最后一刻TimeStamp
     */
    public static Date getEndTimeOfDate(Date date) {
        Calendar cal = Calendar.getInstance();
        Calendar ret = Calendar.getInstance();
        cal.setTime(date);
        ret.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal
                .get(Calendar.DATE), 23, 59, 59);
        return ret.getTime();
    }

    /**
     * 得到某月的最后一刻TimeStamp
     */
    public static Date getEndTimeOfMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        Calendar ret = Calendar.getInstance();
        cal.setTime(date);
        ret.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal
                .getActualMaximum(Calendar.DATE), 23, 59, 59);
        return ret.getTime();
    }

    public static boolean isSameDay(long lastDate) {
        return isSameDay(lastDate, System.currentTimeMillis());
    }

    public static boolean isSameDay(long firstDate, long secondDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        String firstDateStr = simpleDateFormat.format(firstDate);
        String secondDateStr = simpleDateFormat.format(secondDate);

        return firstDateStr.equals(secondDateStr);
    }

    public static boolean isSameWeek(long lastDate) {
        Calendar lastDateCalendar = Calendar.getInstance();
        lastDateCalendar.setTimeInMillis(lastDate);

        Calendar currentDateCalendar = Calendar.getInstance();
        currentDateCalendar.setTimeInMillis(System.currentTimeMillis());

        return currentDateCalendar.get(Calendar.WEEK_OF_YEAR)
                == lastDateCalendar.get(Calendar.WEEK_OF_YEAR);

    }

    public static boolean isSameWeek(long lastDate, long secondDate) {
        Calendar lastDateCalendar = Calendar.getInstance();
        lastDateCalendar.setTimeInMillis(lastDate);

        Calendar currentDateCalendar = Calendar.getInstance();
        currentDateCalendar.setTimeInMillis(secondDate);

        return currentDateCalendar.get(Calendar.WEEK_OF_YEAR)
                == lastDateCalendar.get(Calendar.WEEK_OF_YEAR);
    }

    public static boolean isSameMonth(long lastDate) {
        return isSameMonth(lastDate, System.currentTimeMillis());
    }

    public static boolean isSameMonth(long firstDate, long secondDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());

        String firstDateStr = simpleDateFormat.format(firstDate);
        String secondDateStr = simpleDateFormat.format(secondDate);

        return firstDateStr.equals(secondDateStr);
    }

    public static boolean isSameYear(long lastDate) {
        return isSameYear(lastDate, System.currentTimeMillis());
    }

    public static boolean isSameYear(long firstDate, long secondDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy", Locale.getDefault());

        String firstDateStr = simpleDateFormat.format(firstDate);
        String secondDateStr = simpleDateFormat.format(secondDate);

        return firstDateStr.equals(secondDateStr);
    }

    /**
     * @param time 输入时间
     * @return 输入时间所对应当天的开始时间
     */
    public static long calculateDateStartInMillis(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static long getCurrentDayStart() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static long getCurrentDayEnd() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }

    public static int getMonthDays(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        //设置年份
        calendar.set(Calendar.YEAR, year);
        //设置月份
        calendar.set(Calendar.MONTH, month);
        //获取某月最小天数
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    public static int getFirstSundayOfMonth(int year, int month) {
        int dayOfWeek = 1;
        Calendar calendar = Calendar.getInstance();
        //设置年份
        calendar.set(Calendar.YEAR, year);
        //设置月份
        calendar.set(Calendar.MONTH, month);
        //获取某月最小天数
        int firstDay = calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
        //设置日历中月份的最小天数
        calendar.set(Calendar.DAY_OF_MONTH, firstDay);
        //格式化日期

        switch (calendar.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SUNDAY:
                dayOfWeek = 1;
                break;
            case Calendar.MONDAY:
                dayOfWeek = 7;
                break;
            case Calendar.TUESDAY:
                dayOfWeek = 6;
                break;
            case Calendar.WEDNESDAY:
                dayOfWeek = 5;
                break;
            case Calendar.THURSDAY:
                dayOfWeek = 4;
                break;
            case Calendar.FRIDAY:
                dayOfWeek = 3;
                break;
            case Calendar.SATURDAY:
                dayOfWeek = 2;
                break;
            default:
                break;
        }
        return dayOfWeek;
    }

    public static boolean isSunday(int year, int month, int day) {

        Calendar calendar = Calendar.getInstance();
        //设置年月日
        if (getMonthDays(year, month) >= day) {
            calendar.set(year, month, day);
            //格式化日期
            return calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
        } else {
            return false;
        }
    }

    public static int getDaysInterval(long dateStart, long dateEnd) {
        Calendar startCalendar = Calendar.getInstance();
        Calendar endCalendar = Calendar.getInstance();

        startCalendar.setTimeInMillis(dateStart);
        endCalendar.setTimeInMillis(dateEnd);

        if (startCalendar.after(endCalendar)) {  // swap dates so that d1 is start and d2 is end
            Calendar swap = startCalendar;
            startCalendar = endCalendar;
            endCalendar = swap;
        }
        int days = endCalendar.get(Calendar.DAY_OF_YEAR) - startCalendar.get(Calendar.DAY_OF_YEAR);
        int y2 = endCalendar.get(Calendar.YEAR);
        if (startCalendar.get(Calendar.YEAR) != y2) {
            startCalendar = (Calendar) startCalendar.clone();
            do {
                days += startCalendar.getActualMaximum(Calendar.DAY_OF_YEAR);//得到当年的实际天数
                startCalendar.add(Calendar.YEAR, 1);
            }
            while (startCalendar.get(Calendar.YEAR) != y2);
        }
        return days;
    }

    public static String formatTimeToString(int hour, int minute) {
        String timeString;

        if (hour > 9) {
            timeString = String.valueOf(hour) + ":";
        } else {
            timeString = "0" + String.valueOf(hour) + ":";
        }

        if (minute > 9) {
            timeString += String.valueOf(minute);
        } else {
            timeString += "0" + String.valueOf(minute);
        }

        return timeString;
    }

    //时间戳 转化 为 形如 12:30、08:01 的字符串
    public static String formatTimeToString(Long timeMillis) {
        ContentResolver contentResolver = HSApplication.getContext().getContentResolver();
        String strTimeFormat = android.provider.Settings.System.getString(contentResolver,
                android.provider.Settings.System.TIME_12_24);

        String timeString;

        if (!TextUtils.isEmpty(strTimeFormat) && strTimeFormat.equals("24")) {
            SimpleDateFormat sd = new SimpleDateFormat("HH:mm");
            timeString = sd.format(new Date(timeMillis));
        } else {
            SimpleDateFormat sd = new SimpleDateFormat("hh:mm a");
            timeString = sd.format(new Date(timeMillis));
        }
        return timeString;
    }

    //时间戳 转化 为 形如 12:30、08:01 的24小时制的字符串
    public static String formatTimeTo24HourString(Long timeMillis) {
        String timeString;
        SimpleDateFormat sd = new SimpleDateFormat("HH:mm");
        timeString = sd.format(new Date(timeMillis));
        return timeString;
    }

    // 将时间戳转为数字，例如8:30转为8.5，8:45转为8.75
    public static float formatTimeToFloatNumber(Long timeMillis) {
        String time = DateUtils.formatTimeTo24HourString(timeMillis);

        if (TextUtils.isEmpty(time)) {
            return -1;
        }

        if (!time.contains(":") || time.split(":").length < 2) {
            return -1;
        }

        int hour = Integer.parseInt(time.split(":")[0]);
        float minute = Integer.parseInt(time.split(":")[1]) / 60f;
        return hour + minute;
    }

    public static String formatTimeToStringInEnglish(Long timeMillis) {
        ContentResolver contentResolver = HSApplication.getContext().getContentResolver();
        String strTimeFormat = android.provider.Settings.System.getString(contentResolver,
                android.provider.Settings.System.TIME_12_24);

        String timeString;

        if (!TextUtils.isEmpty(strTimeFormat) && strTimeFormat.equals("24")) {
            SimpleDateFormat sd = new SimpleDateFormat("HH:mm");
            timeString = sd.format(new Date(timeMillis));
        } else {
            SimpleDateFormat sd = new SimpleDateFormat("hh:mm a", Locale.US);
            timeString = sd.format(new Date(timeMillis));
        }
        return timeString;
    }

    public static long calculateTime(int hour, int minute) {
        return hour * 60 * 60 * 1000 + minute * 60 * 1000;
    }

    /**
     * 日期格式字符串转换成时间戳
     *
     * @param date_str 字符串日期
     * @param format   如：yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static long dateToTimeStamp(String date_str, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.parse(date_str).getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0L;
    }
}