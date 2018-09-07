package com.honeycomb.colorphone.util;

import com.crashlytics.android.core.CrashlyticsCore;
import com.honeycomb.colorphone.ColorPhoneApplication;

public class ColorPhoneCrashlytics  {
    private static ColorPhoneCrashlytics INSTANCE = new ColorPhoneCrashlytics();
    private ColorPhoneCrashlytics() {}

    public static ColorPhoneCrashlytics getInstance() {
        return INSTANCE;
    }

    public void log(String msg) {
        if (ColorPhoneApplication.isFabricInitted()) {
            CrashlyticsCore.getInstance().log(msg);
        }
    }

    public void logException(Throwable throwable) {
        if (ColorPhoneApplication.isFabricInitted()) {
            CrashlyticsCore.getInstance().logException(throwable);
        }
    }
}
