package com.honeycomb.colorphone.gdpr;

import android.app.Activity;

import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.honeycomb.colorphone.util.Utils;
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

    public static boolean showGdprAlertIfNeeded(final Activity context) {
        if (!GdprUtils.isGdprNewUser()) {
            return false;
        }

        if (!GdprUtils.isGdprUser()) {
            return false;
        }

        HSGdprConsent.ConsentState consentState = HSGdprConsent.getConsentState();
        if (consentState == HSGdprConsent.ConsentState.TO_BE_CONFIRMED) {
            return Utils.doLimitedTimes(new Runnable() {
                @Override
                public void run() {
                    HSGdprConsent.showConsentAlert(context, HSGdprConsent.AlertStyle.AGREE_STYLE,
                            Constants.URL_PRIVACY, new HSGdprConsent.GDPRAlertListener() {
                                @Override
                                public void onAccept() {
                                    LauncherAnalytics.logEvent("GDPR_Access_Gain");
                                    GdprUtils.setDataUsageUserEnabled(true);
                                }

                                @Override
                                public void onDecline() {
                                    LauncherAnalytics.logEvent("GDPR_Access_Decline");

                                }
                            });

                    LauncherAnalytics.logEvent("GDPR_Access_Alert_Shown");

                }
            }, GdprConsts.PREFS_KEY_CONSTENT_ALERT_SHOW_TIMES, 1);

        }
        return false;
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

