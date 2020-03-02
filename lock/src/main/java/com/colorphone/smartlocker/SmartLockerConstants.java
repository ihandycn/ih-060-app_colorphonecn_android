package com.colorphone.smartlocker;

import com.ihs.app.framework.HSApplication;

public class SmartLockerConstants {
    public static final String CONTENT_URI = "content://" + HSApplication.getContext().getPackageName() + ".smart_locker_fast_boost_ad_click";

    public static final int AIRPLANE_MODE_ON = 1;

    public static final int DBM_VALUE_NO_SIGNAL = -200; // any value smaller than -119
    public static final int DBM_VALUE_BEST_SIGNAL_THRESHOLD = -91;
    public static final int DBM_VALUE_BATTER_SIGNAL_THRESHOLD = -103;
    public static final int DBM_VALUE_LOWER_SIGNAL_THRESHOLD = -113;

    public static final int VALUE_IP_ADDRESS_NO_WIFI = 0;
    public static final int VALUE_BEST_WIFI_THRESHOLD = -50;
    public static final int VALUE_BATTER_WIFI_THRESHOLD = -70;
    public static final int VALUE_LOWER_WIFI_THRESHOLD = -100;

    public static final int LOW_BATTERY_LEVEL_THRESHOLD = 10;
    public static final int MIDDLE_BATTERY_LEVEL_THRESHOLD = 50;
    public static final int HIGH_BATTERY_LEVEL_THRESHOLD = 90;
}
