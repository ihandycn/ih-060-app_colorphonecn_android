package com.honeycomb.colorphone.util;

import com.honeycomb.colorphone.BuildConfig;
import com.superapps.util.Toasts;
import com.tencent.bugly.crashreport.CrashReport;

public class ColorPhoneException {

    public static void handleException(String errorMsg) {
        RuntimeException exception = new RuntimeException(errorMsg);
        handleException(exception);
    }

    public static void handleException(RuntimeException e) {
        if (BuildConfig.DEBUG) {
            throw e;
        } else {
            CrashReport.postCatchedException(e);
        }
    }

    public static void handleExceptionByToast(String errorMsg) {
        RuntimeException exception = new RuntimeException(errorMsg);
        if (BuildConfig.DEBUG) {
            Toasts.showToast(errorMsg);
        } else {
            CrashReport.postCatchedException(exception);
        }
    }

    public static void logException(String msg) {
        RuntimeException exception = new RuntimeException(msg);
        CrashReport.postCatchedException(exception);
    }

    public static void logException(Exception exception) {
        CrashReport.postCatchedException(exception);
    }
}
