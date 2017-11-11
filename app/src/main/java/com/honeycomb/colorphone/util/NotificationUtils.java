package com.honeycomb.colorphone.util;

import android.content.Context;
import android.os.Build;

import com.acb.utils.PermissionUtils;
import com.ihs.app.framework.inner.SessionMgr;

/**
 * Created by ihandysoft on 2017/11/11.
 */

public class NotificationUtils {

    public static boolean isShowNotificationGuideAlert(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return false;
        }
        if (SessionMgr.getInstance().getCurrentSessionId() <= 1 && !PermissionUtils.isNotificationAccessGranted(context)) {
            return true;
        }
        return false;
    }
}
