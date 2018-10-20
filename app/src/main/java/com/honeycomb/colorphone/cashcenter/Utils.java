package com.honeycomb.colorphone.cashcenter;

import com.superapps.util.Preferences;

public class Utils {

    public static boolean hasUserEnterCrashCenter() {
        return Preferences.get("cash_center").getBoolean("user_visit", false);
    }
    public static void userEnterCashCenter() {
        Preferences.get("cash_center").putBoolean("user_visit", true);
    }


}
