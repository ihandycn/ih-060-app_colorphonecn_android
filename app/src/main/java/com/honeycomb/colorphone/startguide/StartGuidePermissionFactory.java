package com.honeycomb.colorphone.startguide;

import android.Manifest;
import android.app.Activity;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.StringRes;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.activity.StartGuideActivity;
import com.honeycomb.colorphone.autopermission.AutoLogger;
import com.honeycomb.colorphone.autopermission.AutoPermissionChecker;
import com.honeycomb.colorphone.autopermission.AutoRequestManager;
import com.ihs.app.framework.HSApplication;
import com.ihs.permission.HSPermissionRequestMgr;
import com.ihs.permission.Utils;
import com.superapps.util.RuntimePermissions;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class StartGuidePermissionFactory {
    public static final int TYPE_PERMISSION_TYPE_SCREEN_FLASH = 1;
    public static final int TYPE_PERMISSION_TYPE_ON_LOCK = 2;
    public static final int TYPE_PERMISSION_TYPE_NOTIFICATION = 3;
    public static final int TYPE_PERMISSION_TYPE_BG_POP = 4;
    public static final int TYPE_PERMISSION_TYPE_PHONE = 5;
    public static final int TYPE_PERMISSION_TYPE_WRITE_SETTINGS = 6;

    @IntDef({TYPE_PERMISSION_TYPE_SCREEN_FLASH,
            TYPE_PERMISSION_TYPE_ON_LOCK,
            TYPE_PERMISSION_TYPE_NOTIFICATION,
            TYPE_PERMISSION_TYPE_BG_POP,
            TYPE_PERMISSION_TYPE_PHONE,
            TYPE_PERMISSION_TYPE_WRITE_SETTINGS})

    @Retention(RetentionPolicy.SOURCE)
    @interface PERMISSION_TYPES {
    }

    @StringRes static int getItemTitle(@PERMISSION_TYPES int type) {
        int id = 0;
        switch (type) {
            case TYPE_PERMISSION_TYPE_BG_POP:
                id = R.string.start_guide_permission_bg_pop;
                break;
            case TYPE_PERMISSION_TYPE_NOTIFICATION:
                id = R.string.start_guide_permission_call;
                break;
            case TYPE_PERMISSION_TYPE_ON_LOCK:
                id = R.string.start_guide_permission_onlocker;
                break;
            case TYPE_PERMISSION_TYPE_SCREEN_FLASH:
                id = R.string.start_guide_permission_auto_start;
                break;
            case TYPE_PERMISSION_TYPE_PHONE:
                id = R.string.start_guide_permission_call_log;
                break;
            case TYPE_PERMISSION_TYPE_WRITE_SETTINGS:
                id = R.string.start_guide_permission_write_settings;
                break;
            default:
                break;
        }
        return id;
    }

    @DrawableRes static int getItemDrawable(@PERMISSION_TYPES int type) {
        int id = 0;
        switch (type) {
            case TYPE_PERMISSION_TYPE_BG_POP:
                id = R.drawable.start_guide_confirm_image_bg_pop;
                break;
            case TYPE_PERMISSION_TYPE_NOTIFICATION:
                id = R.drawable.start_guide_confirm_image_notification;
                break;
            case TYPE_PERMISSION_TYPE_ON_LOCK:
                id = R.drawable.start_guide_confirm_image_onlocker;
                break;
            case TYPE_PERMISSION_TYPE_SCREEN_FLASH:
                id = R.drawable.start_guide_confirm_image_screen_flash;
                break;
            case TYPE_PERMISSION_TYPE_PHONE:
                id = R.drawable.start_guide_confirm_image_phone;
                break;
            case TYPE_PERMISSION_TYPE_WRITE_SETTINGS:
                id = R.drawable.start_guide_confirm_image_write_setttings;
                break;
            default:
                break;
        }
        return id;
    }

    public static boolean getItemGrant(@PERMISSION_TYPES int type) {
        boolean ret = false;
        switch (type) {
            case TYPE_PERMISSION_TYPE_BG_POP:
                ret = AutoPermissionChecker.hasBgPopupPermission();
                break;
            case TYPE_PERMISSION_TYPE_NOTIFICATION:
                ret = Utils.isNotificationListeningGranted();
                break;
            case TYPE_PERMISSION_TYPE_ON_LOCK:
                ret = AutoPermissionChecker.hasShowOnLockScreenPermission();
                break;
            case TYPE_PERMISSION_TYPE_SCREEN_FLASH:
                ret = AutoPermissionChecker.hasAutoStartPermission();
                break;
            case TYPE_PERMISSION_TYPE_PHONE:
                ret = RuntimePermissions.checkSelfPermission(HSApplication.getContext(), Manifest.permission.READ_PHONE_STATE) == RuntimePermissions.PERMISSION_GRANTED;
                break;
            case TYPE_PERMISSION_TYPE_WRITE_SETTINGS:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ret = Settings.System.canWrite(HSApplication.getContext());
                } else {
                    ret = true;
                }
                break;
            default:
                break;
        }
        return ret;
    }

    public static void fixPermission(@PERMISSION_TYPES int type, Activity activity) {
        switch (type) {
            case TYPE_PERMISSION_TYPE_BG_POP:
                AutoRequestManager.getInstance().openPermission(AutoRequestManager.TYPE_CUSTOM_BACKGROUND_POPUP);
                AutoLogger.logEventWithBrandAndOS("FixALert_BgPop_Click");
                break;
            case TYPE_PERMISSION_TYPE_NOTIFICATION:
                AutoRequestManager.getInstance().openPermission(HSPermissionRequestMgr.TYPE_NOTIFICATION_LISTENING);
                AutoLogger.logEventWithBrandAndOS("FixALert_NA_Click");
                break;
            case TYPE_PERMISSION_TYPE_ON_LOCK:
                AutoRequestManager.getInstance().openPermission(HSPermissionRequestMgr.TYPE_SHOW_ON_LOCK);
                AutoLogger.logEventWithBrandAndOS("FixALert_Lock_Click");
                break;
            case TYPE_PERMISSION_TYPE_SCREEN_FLASH:
                AutoRequestManager.getInstance().openPermission(HSPermissionRequestMgr.TYPE_AUTO_START);
                AutoLogger.logEventWithBrandAndOS("FixALert_AutoStart_Click");
                break;
            case TYPE_PERMISSION_TYPE_PHONE:
                if (activity != null) {
                    if (RuntimePermissions.checkSelfPermission(HSApplication.getContext(), Manifest.permission.READ_PHONE_STATE) != RuntimePermissions.PERMISSION_PERMANENTLY_DENIED) {
                        RuntimePermissions.requestPermissions(activity, new String[] { Manifest.permission.READ_PHONE_STATE }, StartGuideActivity.CONFIRM_PAGE_PERMISSION_REQUEST);
                        return;
                    }
                }
                AutoRequestManager.getInstance().openPermission(HSPermissionRequestMgr.TYPE_PHONE);
                AutoLogger.logEventWithBrandAndOS("FixALert_Phone_Click");
                break;
            case TYPE_PERMISSION_TYPE_WRITE_SETTINGS:
                AutoRequestManager.getInstance().openPermission(HSPermissionRequestMgr.TYPE_WRITE_SETTINGS);
                AutoLogger.logEventWithBrandAndOS("FixALert_WriteSettings_Click");
                break;
            default:
                break;
        }
    }
}
