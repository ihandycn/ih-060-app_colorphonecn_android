package com.honeycomb.colorphone.util;

import com.crashlytics.android.core.CrashlyticsCore;
import com.honeycomb.colorphone.ColorPhoneApplication;

public class ColorPhoneCrashlytics extends CrashlyticsCore {
    @Override
    public void log(String msg) {
        if (ColorPhoneApplication.isFabricInitted()) {
            super.log(msg);
        }
    }

    @Override
    public void logException(Throwable throwable) {
        if (ColorPhoneApplication.isFabricInitted()) {
            super.logException(throwable);
        }
    }
}
