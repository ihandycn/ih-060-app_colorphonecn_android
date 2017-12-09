package com.honeycomb.colorphone.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.acb.autopilot.AutopilotConfig;
import com.acb.call.CPSettings;
import com.colorphone.lock.lockscreen.chargingscreen.ChargingScreenSettings;
import com.colorphone.lock.lockscreen.locker.LockerSettings;
import com.colorphone.lock.util.PreferenceHelper;
import com.honeycomb.colorphone.activity.ShareAlertActivity;
import com.ihs.app.framework.inner.SessionMgr;
import com.ihs.commons.utils.HSPreferenceHelper;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by sundxing on 17/9/13.
 */

public class ModuleUtils {
    private static final String PREFS_FILE_NAME = "pref_file_colorphone";

    public static final String AUTO_KEY_APPLY_FINISH = "apply_finish_guide_enable";
    public static final String AUTO_KEY_SCREEN_SAVER = "colorscreensaver_enable";
    public static final String AUTO_KEY_CHARGING = "smart_charging_enable";
    public static final String AUTO_SMS_KEY_ASSISTANT = "sms_assistant_enable";
    public static final String AUTO_KEY_GUIDE_START = "start_guide_enable";

    public static boolean isNeedGuideAfterApply() {

        if (!isModuleConfigEnabled(AUTO_KEY_APPLY_FINISH)){
            return false;
        }

        long guideInterval = System.currentTimeMillis() - PreferenceHelper.get(PREFS_FILE_NAME).getLong("apply_guide_time", 0);
        int interval = (int) AutopilotConfig.getDoubleToTestNow("topic-1505294061097", "apply_finish_guide_show_interval", 6);
        if (guideInterval < interval * DateUtils.HOUR_IN_MILLIS) {
            return false;
        }

        int guideCount = PreferenceHelper.get(PREFS_FILE_NAME).getInt("apply_guide_count", 0);
        int max = (int) AutopilotConfig.getDoubleToTestNow("topic-1505294061097", "apply_finish_guide_max_show_time", 1);

        if (guideCount >= max) {
            return false;
        }

        if (isAllModuleEnabled()) {
            return false;
        }

        PreferenceHelper.get(PREFS_FILE_NAME).putLong("apply_guide_time", System.currentTimeMillis());
        PreferenceHelper.get(PREFS_FILE_NAME).putInt("apply_guide_count", ++guideCount);
        return true;
    }

    public static boolean isModuleConfigEnabled(String moduleKey) {
        return AutopilotConfig.getBooleanToTestNow("topic-1505290483207", moduleKey, false);
    }

    public static boolean isAllModuleEnabled() {
        if (CPSettings.isCallAssistantModuleEnabled()
                && CPSettings.isCallAssistantModuleEnabled()
                && ChargingScreenSettings.isChargingScreenEverEnabled()
                && LockerSettings.isLockerEnabled()) {
            return true;
        }
        return false;
    }

    public static boolean isShareAlertInsideAppShow() {
        PreferenceHelper helper = PreferenceHelper.get(ShareAlertActivity.PREFS_FILE);

        if (helper.getInt(ShareAlertActivity.SHARE_ALERT_IN_APP_SHOW_COUNT, 0)
                >= ShareAlertAutoPilotUtils.getInsideAppShareAlerShowMaxTime()) {
            return false;
        }

        if (helper.getLong(ShareAlertActivity.SHARE_ALERT_IN_APP_SHOW_TIME, 0)
                + ShareAlertAutoPilotUtils.getInsideAppShareAlertShowInterval() > System.currentTimeMillis()) {
            return false;
        }
        return true;
    }

    public static boolean isShareAlertOutsideAppShow(Context context, String phoneNumber) {

        // is in contact
        PreferenceHelper helper = PreferenceHelper.get(ShareAlertActivity.PREFS_FILE);
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
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            projection = new String[]{
                    ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NORMALIZED_NUMBER,
                    ContactsContract.PhoneLookup.DISPLAY_NAME};
        }
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
        boolean needShow = PreferenceHelper.get(PREFS_FILE_NAME).getBoolean("show_set_for_one_guide", true);
        if (needShow) {
            PreferenceHelper.get(PREFS_FILE_NAME).putBoolean("show_set_for_one_guide", false);
        }
        return needShow;
    }



}
