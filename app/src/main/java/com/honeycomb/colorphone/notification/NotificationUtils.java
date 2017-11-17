package com.honeycomb.colorphone.notification;

import android.content.Context;
import android.os.Build;

import com.acb.notification.NotificationAccessGuideAlertActivity;
import com.acb.utils.PermissionUtils;
import com.honeycomb.colorphone.activity.GuideApplyThemeActivity;
import com.ihs.app.framework.inner.SessionMgr;
import com.ihs.commons.utils.HSPreferenceHelper;

public class NotificationUtils {

    public static final String PREFS_NOTIFICATION_GUIDE_ALERT_FIRST_SESSION_SHOWED = "PREFS_NOTIFICATION_GUIDE_ALERT_FIRST_SESSION_SHOWED";
    
    public static boolean isShowNotificationGuideAlertInFirstSession(Context context) {
        if (!isInsideAppAccessAlertEnabled(context)) {
            return false;
        }

        if (HSPreferenceHelper.getDefault().getBoolean(PREFS_NOTIFICATION_GUIDE_ALERT_FIRST_SESSION_SHOWED, false)) {
            return false;
        }
        return true;
    }

    public static boolean isShowNotificationGuideAlertWhenApplyTheme(Context context) {
        if (!isInsideAppAccessAlertEnabled(context)) {
            return false;
        }

        if (HSPreferenceHelper.getDefault().getInt(GuideApplyThemeActivity.PREFS_GUIDE_APPLY_ALERT_SHOW_SESSION_ID, 0)
                == SessionMgr.getInstance().getCurrentSessionId()) {
            return false;
        }

       if(HSPreferenceHelper.getDefault().getInt(NotificationAccessGuideAlertActivity.ACB_PHONE_NOTIFICATION_INSIDE_GUIDE_SHOW_COUNT, 0)
                >= NotificationConfig.getInsideAppAccessAlertShowMaxTime()) {
            return false;
       }

        if (HSPreferenceHelper.getDefault().getLong(NotificationAccessGuideAlertActivity.ACB_PHONE_NOTIFICATION_INSIDE_GUIDE_SHOW_TIME, 0)
                + NotificationConfig.getInsideAppAccessAlertInterval() > System.currentTimeMillis()) {
            return false;
        }

        if (SessionMgr.getInstance().getCurrentSessionId() <= 1 && HSPreferenceHelper.getDefault().getBoolean(PREFS_NOTIFICATION_GUIDE_ALERT_FIRST_SESSION_SHOWED, false)) {
            return false;
        }
        return true;
    }

    private static boolean isInsideAppAccessAlertEnabled(Context context) {
        if (!NotificationConfig.isInsideAppAccessAlertOpen()) {
            return false;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT || PermissionUtils.isNotificationAccessGranted(context)) {
            return false;
        }
        return true;
    }
}
