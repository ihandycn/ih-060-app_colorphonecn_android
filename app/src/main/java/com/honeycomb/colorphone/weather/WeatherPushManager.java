package com.honeycomb.colorphone.weather;

import android.content.Context;

import java.util.Calendar;

/**
 * Created by zqs on 2019/4/9.
 */
public class WeatherPushManager {

    private void WeatherPushManager(){}

    public static WeatherPushManager getInstance() {
        return Inner.mInstance;
    }

    static class Inner {
        static WeatherPushManager mInstance = new WeatherPushManager();
    }

    public void push(Context context) {
//        if (inValidTime()) {
            WeatherVideoActivity.start(context);
//        }
    }

    public static boolean inValidTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        return (6 <= hourOfDay && hourOfDay < 9) || (18 <= hourOfDay && hourOfDay < 21) ;
    }

}
