package com.honeycomb.colorphone.startguide;

import android.Manifest;
import android.app.Activity;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.StringRes;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.activity.StartGuideActivity;
import com.honeycomb.colorphone.autopermission.AutoLogger;
import com.honeycomb.colorphone.autopermission.AutoPermissionChecker;
import com.honeycomb.colorphone.autopermission.AutoRequestManager;
import com.honeycomb.colorphone.util.Analytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.permission.HSPermissionRequestMgr;
import com.ihs.permission.Utils;
import com.superapps.util.RuntimePermissions;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

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
                id = R.string.start_guide_permission_phone;
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
                ret = AutoPermissionChecker.isPhonePermissionGranted();
                break;
            case TYPE_PERMISSION_TYPE_WRITE_SETTINGS:
                ret = AutoPermissionChecker.isWriteSettingsPermissionGranted();
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
                AutoLogger.logEventWithBrandAndOS("FixALert_Phone_Click");
                if (activity != null) {
                    List<String> permission = new ArrayList<>();
                    int state = RuntimePermissions.checkSelfPermission(HSApplication.getContext(), Manifest.permission.READ_PHONE_STATE);
                    if (state != RuntimePermissions.PERMISSION_PERMANENTLY_DENIED && state != RuntimePermissions.PERMISSION_GRANTED) {
                        Analytics.logEvent("FixAlert_ReadPhoneState_Request");
                        permission.add(Manifest.permission.READ_PHONE_STATE);
                    }

                    state = RuntimePermissions.checkSelfPermission(HSApplication.getContext(), Manifest.permission.CALL_PHONE);
                    if (state != RuntimePermissions.PERMISSION_PERMANENTLY_DENIED && state != RuntimePermissions.PERMISSION_GRANTED) {
                        Analytics.logEvent("FixAlert_CallPhone_Request");
                        permission.add(Manifest.permission.CALL_PHONE);
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        state = RuntimePermissions.checkSelfPermission(HSApplication.getContext(), Manifest.permission.ANSWER_PHONE_CALLS);
                        if (state != RuntimePermissions.PERMISSION_PERMANENTLY_DENIED
                                && state != RuntimePermissions.PERMISSION_GRANTED) {
                            permission.add(Manifest.permission.ANSWER_PHONE_CALLS);
                        }
                    }

                    if (permission.size() > 0) {
                        RuntimePermissions.requestPermissions(activity, permission.toArray(new String[0]), StartGuideActivity.CONFIRM_PAGE_PERMISSION_REQUEST);
                        return;
                    }
                }

                Analytics.logEvent("FixAlert_Phone_Settings_Request");
                AutoRequestManager.getInstance().openPermission(HSPermissionRequestMgr.TYPE_PHONE);
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
