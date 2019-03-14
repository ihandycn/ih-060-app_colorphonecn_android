package com.honeycomb.colorphone;

import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import com.honeycomb.colorphone.gdpr.GdprUtils;
import com.honeycomb.colorphone.util.Analytics;
import com.ihs.app.push.HSPushMgr;
import com.ihs.app.push.impl.PushMgr;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;

import java.util.List;

public class PushManager {

    private static final String TAG = PushManager.class.getSimpleName();
    private static final String URL_PATH = "/ka/token";
    private static final String PROPERTY_TOKEN_SERVER_ID = "prefs_ka_token";
    private static final String PROPERTY_KA_ENABLE = "prefs_ka_enable";
    private static PushManager INSTANCE = new PushManager();

    private boolean pushModuleEnable = false;

    private INotificationObserver deviceTokenObserver = new INotificationObserver() {
        @Override
        public void onReceive(String eventName, HSBundle hsBundle) {
            if (HSPushMgr.HS_NOTIFICATION_DEVICETOKEN_RECEIVED.equals(eventName)) {
                String token = hsBundle.getString(HSPushMgr.HS_NOTIFICATION_DEVICETOKEN_RECEIVED_PARAM_TOKEN_STRING);
                onTokenFetch(token);
            } else if (eventName == HSPushMgr.HS_NOTIFICATION_DEVICETOKEN_REQUEST_FAILED) {
                String errMsg = hsBundle.getString(HSPushMgr.HS_NOTIFICATION_DEVICETOKEN_REQUEST_FAILED_PARAM_ERROR_STRING);
                HSLog.d(TAG,"DeviceToken failed: " + errMsg);
            }
        }
    };

    private INotificationObserver pushDataObserver = new INotificationObserver() {
        @Override
        public void onReceive(String eventName, HSBundle hsBundle) {
            if (PushMgr.HS_NOTIFICATION_PUSH_MSG_RECEIVED.equals(eventName)) {
                try {
                    Intent intent = (Intent) hsBundle.getObject(PushMgr.HS_NOTIFICATION_PUSH_MSG_RECEIVED_PARAM_MSG_INTENT);
                    Analytics.logEvent("ColorPhone_Push_Receive", Analytics.FLAG_LOG_FABRIC);
                    Analytics.logEvent("ColorPhone_Push_Receive_WhenLaunch", true,
                            "Time", formatTimes());
                    String msg = intent.getStringExtra("msg");
                    HSLog.d(TAG, "Receive message : " + msg);
                } catch (Exception e) {
                    HSLog.e(TAG, "Receive message error : " + e.getMessage());
                }
            }
        }
    };

    private String formatTimes() {
        long time = System.currentTimeMillis() - ColorPhoneApplication.launchTime;
        long second =  time / 1000;
        if (second <= 3) {
            return String.valueOf(second);
        } else if (second <= 10) {
            return "3-10s";
        } else if (second <= 60){
            return "10s-1m";
        } else if (second <= 60 * 60) {
            return "1m-1h";
        } else {
            return "1h+";
        }
    }

    private String mCurrentToken;
    private boolean mKaEnable;
    private boolean waitGdprFlag;

    public static PushManager getInstance() {
        return INSTANCE;
    }

    public void init() {
        mKaEnable = Preferences.get(Constants.DESKTOP_PREFS).getBoolean(PROPERTY_KA_ENABLE, false);
        String token = HSPushMgr.getDeviceToken();
        if (!TextUtils.isEmpty(token)) {
            onTokenFetch(token);
        }
        HSPushMgr.addObserver(HSPushMgr.HS_NOTIFICATION_DEVICETOKEN_RECEIVED, deviceTokenObserver);
        HSGlobalNotificationCenter.addObserver(PushMgr.HS_NOTIFICATION_PUSH_MSG_RECEIVED, pushDataObserver);
    }

    private void onTokenFetch(String token) {
        HSLog.d(TAG, "token : " + token);
        mCurrentToken = token;
        String oldToken = getOldToken();
        if (TextUtils.equals(mCurrentToken, oldToken)) {
            HSLog.d(TAG, "token is same as before!");
            return;
        }
        mKaEnable = isKaEnable();
        if (GdprUtils.isNeedToAccessDataUsage()) {
            request();
        } else {
            waitGdprFlag = true;
        }
    }

    public void onConfigChanged() {
        boolean kaEnable = isKaEnable();
        boolean isChange = kaEnable != mKaEnable;
        if (isChange) {
            mKaEnable = kaEnable;
            if (GdprUtils.isNeedToAccessDataUsage()) {
                request();
            } else {
                waitGdprFlag = true;
            }
        }
    }

    private void request() {
        if (!pushModuleEnable) {
            return;
        }
        Analytics.logEvent("ColorPhone_Push_Request", Analytics.FLAG_LOG_FABRIC, "Enable", String.valueOf(mKaEnable));
        Threads.postOnThreadPoolExecutor(new Runnable() {
            @Override
            public void run() {
                doRequestBackground();
            }
        });
    }

    private void doRequestBackground() {

    }

    private String getOldToken() {
        return Preferences.get(Constants.DESKTOP_PREFS).getString(PROPERTY_TOKEN_SERVER_ID, "");
    }

    private boolean isKaEnable() {
        return pushEnable();
    }

    private boolean pushEnable() {
        List<Integer> enableList = (List<Integer>) HSConfig.getList("Application", "PushEnabledVersionList");
        int versionCode = Build.VERSION.SDK_INT;
        if (enableList != null && enableList.contains(versionCode)) {
            return true;
        }
        return false;
    }

    public void onGdprGranted() {
        if (waitGdprFlag) {
            waitGdprFlag = false;
            request();
        }
    }
}
