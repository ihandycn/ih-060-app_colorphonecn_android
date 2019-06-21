package com.honeycomb.colorphone.util;

import com.crashlytics.android.core.CrashlyticsCore;
import com.honeycomb.colorphone.ColorPhoneApplication;
import com.ihs.commons.config.HSConfig;

public class ColorPhoneCrashlytics  {
    private static ColorPhoneCrashlytics INSTANCE = new ColorPhoneCrashlytics();
    private ColorPhoneCrashlytics() {}

    public static ColorPhoneCrashlytics getInstance() {
        return INSTANCE;
    }

    public void log(String msg) {
        if (ColorPhoneApplication.isFabricInited()) {
            CrashlyticsCore.getInstance().log(msg);
        }
    }

    public void logException(Throwable throwable) {
        if (ColorPhoneApplication.isFabricInited() && HSConfig.optBoolean(true, "Application", "EnableCrashLog")) {
            CrashlyticsCore.getInstance().logException(throwable);
        }
    }
}
