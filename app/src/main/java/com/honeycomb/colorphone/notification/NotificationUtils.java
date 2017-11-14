package com.honeycomb.colorphone.notification;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.acb.utils.PermissionUtils;
import com.honeycomb.colorphone.activity.GuideApplyThemeActivity;
import com.ihs.app.framework.inner.SessionMgr;
import com.ihs.app.utils.HSVersionControlUtils;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;

public class NotificationUtils {

    public static final String PREFS_NOTIFICATION_GUIDE_ALERT_SHOW_TIME = "PREFS_NOTIFICATION_GUIDE_ALERT_SHOW_TIME";
    public static final String PREFS_NOTIFICATION_INSIDE_GUIDE_SHOW_COUNT = "PREFS_NOTIFICATION_INSIDE_GUIDE_SHOW_COUNT";

//    public static final String PREFS_NOTIFICATION_GUIDE_HAS_SHOWED_IN_FIRST_SESSION = "PREFS_NOTIFICATION_GUIDE_HAS_SHOWED_IN_FIRST_SESSION";
    
    public static boolean isShowNotificationGuideAlert(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT || PermissionUtils.isNotificationAccessGranted(context)) {
            return false;
        }

        if (HSPreferenceHelper.getDefault().getLong(PREFS_NOTIFICATION_GUIDE_ALERT_SHOW_TIME, 0) != 0) {
            return false;
        }
        return true;
    }

    public static boolean isShowNotificationGuideAlertWhenApplyTheme(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT || PermissionUtils.isNotificationAccessGranted(context)) {
            return false;
        }

        if (HSPreferenceHelper.getDefault().getInt(GuideApplyThemeActivity.PREFS_GUIDE_APPLY_ALERT_SHOW_SESSION_ID, 0)
                == SessionMgr.getInstance().getCurrentSessionId()) {
            return false;
        }

        if (HSPreferenceHelper.getDefault().getLong(PREFS_NOTIFICATION_GUIDE_ALERT_SHOW_TIME, 0)
                + NotificationConfig.getInsideAppAccessAlertInterval() > System.currentTimeMillis()) {
            return false;
        }
        return true;
    }
}
