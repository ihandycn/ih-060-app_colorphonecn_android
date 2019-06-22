package com.honeycomb.colorphone.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.format.DateUtils;

import com.call.assistant.customize.CallAssistantSettings;
import com.colorphone.lock.lockscreen.chargingscreen.ChargingScreenSettings;
import com.colorphone.lock.lockscreen.chargingscreen.SmartChargingSettings;
import com.colorphone.lock.lockscreen.locker.LockerSettings;
import com.honeycomb.colorphone.activity.NotificationSettingsActivity;
import com.honeycomb.colorphone.activity.ShareAlertActivity;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSSessionMgr;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.messagecenter.customize.MessageCenterSettings;
import com.superapps.util.Preferences;

import java.util.Arrays;
import java.util.List;

/**
 * Created by sundxing on 17/9/13.
 */

public class ModuleUtils {
    private static final int SHOW_AD_VERSION_CODE = 26;

    private static final String PREFS_FILE_NAME = "pref_file_colorphone";

    public static final String AUTO_KEY_APPLY_FINISH = "apply_finish_guide_enable";
    public static final String AUTO_SMS_KEY_ASSISTANT = "sms_assistant_enable";
    public static final String AUTO_KEY_GUIDE_START = "start_guide_enable";
    public static final String AUTO_KEY_CALL_ASSISTANT = "call_assistant_enable";

    public static boolean isNeedGuideAfterApply() {
        return false;
    }

    public static boolean isModuleConfigEnabled(String moduleKey) {

        if (AUTO_SMS_KEY_ASSISTANT.equals(moduleKey)) {
            return (HSConfig.optBoolean(false, "Application", "ScreenFlash", "SmsAssistant", "Enable"))
                    && !isSMSInNewUserBlockStatus();
        } else if (AUTO_KEY_GUIDE_START.equals(moduleKey)) {
            return  HSConfig.optBoolean(false, "Application", "Guide", "StartGuideEnable");
        } else if (AUTO_KEY_APPLY_FINISH.equals(moduleKey)) {
            return HSConfig.optBoolean(false, "Application", "Guide", "ApplyFinishGuideEnable");
        } else if (AUTO_KEY_CALL_ASSISTANT.equals(moduleKey)) {
            return (HSConfig.optBoolean(false, "Application", "ScreenFlash", "CallAssistant", "Enable"));
        }
        return false;
    }

    public static boolean isAllModuleEnabled() {
        if (CallAssistantSettings.isCallAssistantModuleEnabled()
                && MessageCenterSettings.isSMSAssistantModuleEnabled()
                && ChargingScreenSettings.isChargingScreenEverEnabled()
                && LockerSettings.isLockerUserEnabled()) {
            return true;
        }
        return false;
    }
    public static void setAllModuleUserEnable() {
        MessageCenterSettings.setSMSAssistantModuleEnabled(true);
        CallAssistantSettings.setCallAssistantModuleEnabled(true);
        LockerSettings.setLockerEnabled(true);
        SmartChargingSettings.setModuleEnabled(true);
        NotificationSettingsActivity.setNotificationBoostOn(true);
    }

    public static boolean isShareAlertInsideAppShow() {

        if (!ShareAlertAutoPilotUtils.isInsideAppEnable()) {
            return false;
        }
        Preferences helper = Preferences.get(ShareAlertActivity.PREFS_FILE);

        if (helper.getInt(ShareAlertActivity.SHARE_ALERT_IN_APP_SHOW_COUNT, 0)
                >= ShareAlertAutoPilotUtils.getInsideAppShareAlertShowMaxTime()) {
            return false;
        }

        if (helper.getLong(ShareAlertActivity.SHARE_ALERT_IN_APP_SHOW_TIME, 0)
                + ShareAlertAutoPilotUtils.getInsideAppShareAlertShowInterval() > System.currentTimeMillis()) {
            return false;
        }
        return true;
    }

    public static boolean isShareAlertOutsideAppShow(Context context, String phoneNumber) {

        if (!ShareAlertAutoPilotUtils.isOutsideAppEnable()) {
            return false;
        }
        // is in contact
        Preferences helper = Preferences.get(ShareAlertActivity.PREFS_FILE);
        if (helper.getInt(ShareAlertActivity.SHARE_ALERT_OUT_APP_SHOW_COUNT, 0)
                >= ShareAlertAutoPilotUtils.getOutsideAppShareAlerShowMaxTime()) {
            return false;
        }

        if (helper.getLong(ShareAlertActivity.SHARE_ALERT_OUT_APP_SHOW_TIME, 0)
                + ShareAlertAutoPilotUtils.getOutsideAppShareAlertShowInterval() > System.currentTimeMillis()) {
            return false;
        }

        String callName = null;
        String photoUri = null;
        ContentResolver contentResolver = context.getContentResolver();
        Uri phonesUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        String[] projection = new String[]{
                ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER,
                ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup.PHOTO_URI};
        Cursor cursorLookup = null;
        try {
            cursorLookup = contentResolver.query(phonesUri,
                    projection, null, null, null);
            if (cursorLookup != null && cursorLookup.moveToFirst()) {
                callName = cursorLookup.getString(cursorLookup.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                photoUri = cursorLookup.getString(cursorLookup.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_URI));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursorLookup != null) {
                cursorLookup.close();
            }
        }

        if (callName != null) {
            ShareAlertActivity.UserInfo userInfo = new ShareAlertActivity.UserInfo(phoneNumber, callName, photoUri);
            ShareAlertActivity.startOutsideApp(context, userInfo);
            return true;
        }

        return false;
    }

    public static boolean needShowSetForOneGuide() {
        boolean needShow = Preferences.get(PREFS_FILE_NAME).getBoolean("show_set_for_one_guide", true);
        if (needShow) {
            Preferences.get(PREFS_FILE_NAME).putBoolean("show_set_for_one_guide", false);
        }
        return needShow;
    }

    public static boolean needShowRandomThemeGuide() {
        boolean needShow = Preferences.get(PREFS_FILE_NAME).getBoolean("show_random_theme_guide", true);
        if (needShow) {
            Preferences.get(PREFS_FILE_NAME).putBoolean("show_random_theme_guide", false);
        }
        return needShow;
    }


    public static boolean isNotificationToolBarEnabled() {
        return (HSConfig.optBoolean(false, "Application", "NotificationToolbar", "Enable")
                || versionNumberValid())
                && Utils.ATLEAST_JELLY_BEAN;
    }

    private static boolean versionNumberValid() {
        int versionCode = HSApplication.getFirstLaunchInfo().appVersionCode;
        List<Integer> list = null;
        try {
            list = (List<Integer>)
                    HSConfig.getList("Application", "NotificationToolbar", "ToolbarEnableVersionCode");
        } catch (Exception ignore) {
        }

        if (list != null) {
            HSLog.d("versionNumberValid" , Arrays.toString(list.toArray()));
            return list.contains(versionCode);
        }
        return false;
    }


    public static boolean isChargingImproverEnabled() {
        return HSConfig.optBoolean(false, "Application", "ChargingImprover", "Enabled")
                && isChargingImproverNewUser();
    }

    public static boolean isChargingImproverNewUser() {
        return HSApplication.getFirstLaunchInfo().appVersionCode
                >= HSConfig.optInteger(39, "Application", "ChargingImprover", "FirstVersionCode");
    }

    public static boolean isSMSInNewUserBlockStatus() {
        long currentTimeMills = System.currentTimeMillis();
        return currentTimeMills - HSSessionMgr.getFirstSessionStartTime() <
                HSConfig.optInteger(360, "Application", "ScreenFlash", "SmsAssistant", "ActiveAfterInstallMinutes")
                        * DateUtils.MINUTE_IN_MILLIS;
    }

}
