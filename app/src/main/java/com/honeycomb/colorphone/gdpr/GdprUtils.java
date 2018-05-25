package com.honeycomb.colorphone.gdpr;

import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSGdprConsent;

import java.util.Calendar;

public class GdprUtils {
    private static final int GDPR_VERSION_CODE = 29;

    public static void setDataUsageUserEnabled(boolean enabled) {
        HSGdprConsent.setGranted(enabled);
    }

    public static boolean isDataUsageUserEnabled() {
        return HSGdprConsent.getConsentState() == HSGdprConsent.ConsentState.ACCEPTED;
    }

    public static boolean isNeedToAccessDataUsage() {
        return (isDataUsageUserEnabled()
                || !isGdprUser()
                || !isGdprNewUser()
                || isBeforeDeadline())
                && !isDataUsageUserDisabled();
    }

    public static boolean isGdprNewUser() {
        return HSApplication.getFirstLaunchInfo().appVersionCode >= GDPR_VERSION_CODE;
    }

    public static boolean isGdprUser() {
        return HSGdprConsent.isGdprUser();
    }

    private static boolean isBeforeDeadline() {
        Calendar dueDate = Calendar.getInstance();
        dueDate.set(2018, Calendar.MAY, 25, 6, 0, 0);
        Calendar currentDate = Calendar.getInstance();
        return currentDate.before(dueDate);
    }

    private static boolean isDataUsageUserDisabled() {
        return HSGdprConsent.getConsentState() == HSGdprConsent.ConsentState.DECLINED;
    }
}

