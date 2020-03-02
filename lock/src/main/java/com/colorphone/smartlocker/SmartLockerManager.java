package com.colorphone.smartlocker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.PowerManager;
import android.support.annotation.IntDef;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.colorphone.lock.LockerCustomConfig;
import com.colorphone.lock.R;
import com.colorphone.smartlocker.baidu.BaiduFeedManager;
import com.colorphone.smartlocker.bean.BaiduFeedBean;
import com.colorphone.smartlocker.bean.BaiduFeedItemsBean;
import com.colorphone.smartlocker.utils.DailyNewsUtils;
import com.colorphone.smartlocker.utils.NetworkStatusUtils;
import com.colorphone.smartlocker.utils.TouTiaoFeedUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.libcharging.HSChargingManager;
import com.superapps.util.Toasts;

import net.appcloudbox.autopilot.AutopilotEvent;

import org.json.JSONObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by zhouzhenliang on 16/11/3.
 */

public class SmartLockerManager {

    private static final String TAG = "CHARGING_SCREEN_MANAGER";

    private static final long DELAY_NOTIFICATION_CHARGING_FULL_POWER = 10 * 60 * 1000;
    private static final long INTERVAL_RESPOND_POWER_CHANGED = 1000;

    private static final int ALARM_ALLOWED_START_HOUR_OF_DAY = 8;
    private static final int ALARM_ALLOWED_END_HOUR_OF_DAY = 22;

    // private long lastStartChargingScreenTime;
    private boolean isRegisteredScreenOff;

    private long lastPowerConnectedDisconnectedTime;
    private boolean isPowerConnected;

    private Handler workHandler;

    @IntDef({EXTRA_VALUE_START_BY_CHARGING_SCREEN_OFF, EXTRA_VALUE_START_BY_CHARGING_PLUG_IN, EXTRA_VALUE_START_BY_LOCKER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface StartType {
    }

    public static final int EXTRA_VALUE_START_BY_CHARGING_SCREEN_OFF = 0;
    public static final int EXTRA_VALUE_START_BY_CHARGING_PLUG_IN = 1;
    public static final int EXTRA_VALUE_START_BY_LOCKER = 2;

    public static final String EXTRA_START_TYPE = "EXTRA_START_TYPE";

    private BroadcastReceiver screenOffReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TextUtils.isEmpty(intent.getAction())) {
                return;
            }

            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                HSLog.d(TAG, "Screen Off : Open Charging Screen");
                tryToPreLoadBaiduNews();
                if (HSChargingManager.getInstance().isCharging()) {
                    tryToStartChargingScreenActivity(EXTRA_VALUE_START_BY_CHARGING_SCREEN_OFF);

                } else {
                    tryToStartChargingScreenActivity(EXTRA_VALUE_START_BY_LOCKER);
                }
            }
        }
    };

    private HSChargingManager.IChargingListener chargingListener = new HSChargingManager.IChargingListener() {
        @Override
        public void onBatteryLevelChanged(int preBatteryLevel, int curBatteryLevel) {
        }

        @Override
        public void onChargingStateChanged(HSChargingManager.HSChargingState preChargingState, HSChargingManager.HSChargingState curChargingState) {
//            if (!SettingProvider.isSmartChargingOpened(HSApplication.getContext())) {
//                return;
//            }

            /*充满电*/
            if (curChargingState == HSChargingManager.HSChargingState.STATE_CHARGING_FULL) {
                HSLog.d(TAG, "DELAY Full Charging Toast");

                workHandler.removeCallbacks(abuseChargingNotificationRunnable);
                workHandler.postDelayed(abuseChargingNotificationRunnable, DELAY_NOTIFICATION_CHARGING_FULL_POWER);
            }
        }

        @Override
        public void onChargingRemainingTimeChanged(int chargingRemainingMinutes) {
        }

        @Override
        public void onBatteryTemperatureChanged(float preBatteryTemperature, float batteryTemperature) {
        }
    };

    private Runnable abuseChargingNotificationRunnable = new Runnable() {
        @Override
        public void run() {
            if (!HSChargingManager.getInstance().isCharging()) {
                return;
            }

            if (HSChargingManager.getInstance().getChargingState() != HSChargingManager.HSChargingState.STATE_CHARGING_FULL) {
                return;
            }
//
//            if (!SettingProvider.isSmartChargingOpened(HSApplication.getContext())) {
//                return;
//            }
//
//            if (!SettingProvider.isChargingFullPowerNoticeOpened(HSApplication.getContext())) {
//                return;
//            }

            Context context = HSApplication.getContext();
            Toasts.showToast(context.getString(R.string.charging_screen_fully_charged_notification));

            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
                    | PowerManager.SCREEN_DIM_WAKE_LOCK, context.getPackageName());

            try {
                wakeLock.acquire();
                wakeLock.release();

            } catch (SecurityException e) {
                //某些机型会报 java.lang.SecurityException: Requires MANAGE_APP_TOKENS permission
                e.printStackTrace();

                if (wakeLock != null) {
                    wakeLock.release();
                }
            }

            int hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

            HSLog.d(TAG, "abuseChargingNotificationRunnable, hourOfDay = " + hourOfDay);

            if (hourOfDay >= ALARM_ALLOWED_START_HOUR_OF_DAY && hourOfDay <= ALARM_ALLOWED_END_HOUR_OF_DAY) {

                HSLog.d(TAG, "abuseChargingNotificationRunnable, play Sound");

                try {
                    MediaPlayer mediaPlayer = MediaPlayer.create(HSApplication.getContext(), R.raw.charging_power_full_alerm);
                    if (mediaPlayer != null) {

                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                mp.release();
                            }
                        });
                        mediaPlayer.start();
                    }
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }

            }

            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            Date date = new Date(System.currentTimeMillis());
            LockerCustomConfig.getLogger().logEvent("Charge_FullyCharged_Viewed", "time", sdf.format(date));
        }
    };

    public SmartLockerManager() {
        HandlerThread handlerThread = new HandlerThread("SLM Thread");
        handlerThread.start();
        workHandler = new Handler(handlerThread.getLooper());

        ContentObserver chargingScreenSwitchContentObserver = new ContentObserver(workHandler) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);

//                if (SettingProvider.isSmartChargingOpened(HSApplication.getContext()) || SettingProvider.isSmartLockerOpened(HSApplication.getContext())) {
//                    if (SettingProvider.isSmartChargingOpened(HSApplication.getContext())) {
                enableChargingScreen();
//                    }
                registerScreenOffReceiver();
//                } else {
//                    unregisterScreenOffReceiver();
//                }
            }
        };

        ContentObserver smartLockSwitchContentObserver = new ContentObserver(workHandler) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);

//                if (SettingProvider.isSmartChargingOpened(HSApplication.getContext()) || SettingProvider.isSmartLockerOpened(HSApplication.getContext())) {
                registerScreenOffReceiver();
//                } else {
//                    unregisterScreenOffReceiver();
//                }
            }
        };
//
//        HSApplication.getContext().getContentResolver().registerContentObserver(
//                SettingProvider.createSettingContentUri(HSApplication.getContext(),
//                        SettingProvider.PATH_SMART_CHARGING_SWITCH),
//                true, chargingScreenSwitchContentObserver);
//
//        HSApplication.getContext().getContentResolver().registerContentObserver(
//                SettingProvider.createSettingContentUri(HSApplication.getContext(),
//                        SettingProvider.PATH_SMART_LOCK_SWITCH),
//                true, smartLockSwitchContentObserver);

        workHandler.post(new Runnable() {
            @Override
            public void run() {
//                if (SettingProvider.isSmartChargingOpened(HSApplication.getContext()) || SettingProvider.isSmartLockerOpened(HSApplication.getContext())) {
                registerScreenOffReceiver();
//                }
            }
        });
    }

    public void onPowerConnected() {
        workHandler.post(new Runnable() {
            @Override
            public void run() {
                onPowerConnectedInner();
            }
        });
    }

    private void onPowerConnectedInner() {
        HSLog.d(TAG, "onPowerConnected()");

        if (isPowerConnected) {
            return;
        }
        isPowerConnected = true;

        if (System.currentTimeMillis() - lastPowerConnectedDisconnectedTime < INTERVAL_RESPOND_POWER_CHANGED) {
            return;
        }
        lastPowerConnectedDisconnectedTime = System.currentTimeMillis();

        HSChargingManager.getInstance().addChargingListener(chargingListener, workHandler);

//        if (SettingProvider.isSmartChargingOpened(HSApplication.getContext())) {

        HSLog.d(TAG, "Open ChargingScreen");
        tryToStartChargingScreenActivity(EXTRA_VALUE_START_BY_CHARGING_PLUG_IN);

        if (HSChargingManager.getInstance().getChargingState() == HSChargingManager.HSChargingState.STATE_CHARGING_FULL) {
            HSLog.d(TAG, "onPowerConnected() DELAY Full Charging Toast");

            workHandler.removeCallbacks(abuseChargingNotificationRunnable);
            workHandler.postDelayed(abuseChargingNotificationRunnable, DELAY_NOTIFICATION_CHARGING_FULL_POWER);
        }
//        }
    }

    public void onPowerDisconnected() {
        workHandler.post(new Runnable() {
            @Override
            public void run() {
                onPowerDisconnectedInner();
            }
        });
    }

    private void onPowerDisconnectedInner() {
        HSLog.d(TAG, "onPowerDisconnected()");

        lastPowerConnectedDisconnectedTime = System.currentTimeMillis();

        if (!isPowerConnected) {
            return;
        }
        isPowerConnected = false;

        HSChargingManager.getInstance().removeChargingListener(chargingListener);

        workHandler.removeCallbacks(abuseChargingNotificationRunnable);
    }

    private void enableChargingScreen() {
        HSPreferenceHelper preferenceHelper = HSPreferenceHelper.create(
                HSApplication.getContext(), "optimizer_battery_monitor");
        preferenceHelper.putInt("PREF_KEY_PROMOTED_CHARGING_SCREEN_COUNT", 0);

        if (HSChargingManager.getInstance().isCharging()
                && isOpenChargingScreenWhenSwitchChanged()) {
            tryToStartChargingScreenActivity(EXTRA_VALUE_START_BY_CHARGING_PLUG_IN);
        }

        if (HSChargingManager.getInstance().getChargingState() == HSChargingManager.HSChargingState.STATE_CHARGING_FULL) {
            workHandler.removeCallbacks(abuseChargingNotificationRunnable);
            workHandler.postDelayed(abuseChargingNotificationRunnable, DELAY_NOTIFICATION_CHARGING_FULL_POWER);
        }
    }

    private void tryToStartChargingScreenActivity(@StartType int startType) {
        TelephonyManager telephonyManager = (TelephonyManager) HSApplication.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        switch (telephonyManager.getCallState()) {
            case TelephonyManager.CALL_STATE_OFFHOOK:
            case TelephonyManager.CALL_STATE_RINGING:
                return;
        }

        LockerCustomConfig.getLogger().logEvent(startType != EXTRA_VALUE_START_BY_LOCKER ? "ChargingPage_News_Chance" : "CablePage_News_Chance");
        tryToStartSmartLockerFeeds(startType);
    }

    private void tryToStartSmartLockerFeeds(@StartType int startType) {
        if (!NetworkStatusUtils.isNetworkConnected(HSApplication.getContext())) {
            if (startType == EXTRA_VALUE_START_BY_LOCKER) {
                LockerCustomConfig.getLogger().logEvent("CablePage_News", "news_nofill", "network_disconnected");
            } else {
                LockerCustomConfig.getLogger().logEvent("ChargingPage_News", "news_nofill", "network_disconnected");
            }
            return;
        }
        JSONObject jsonObject = DailyNewsUtils.getLastNews(BaiduFeedManager.CATEGORY_ALL);
        BaiduFeedItemsBean baiduFeedItemsBean = new BaiduFeedItemsBean(jsonObject);
        List<BaiduFeedBean> baiduFeedBeanList = baiduFeedItemsBean.getBaiduFeedBeans();
        int newsCount = 0;
        for (BaiduFeedBean baiduNewsItemData : baiduFeedBeanList) {
            if (baiduNewsItemData.getNewsType() == TouTiaoFeedUtils.COVER_MODE_THREE_IMAGE
                    || baiduNewsItemData.getNewsType() == TouTiaoFeedUtils.COVER_MODE_RIGHT_IMAGE) {
                newsCount++;
            }
        }
        if (newsCount < 5) {
            if (startType == EXTRA_VALUE_START_BY_LOCKER) {
                LockerCustomConfig.getLogger().logEvent("CablePage_News", "news_nofill", "Load_failed");
            } else {
                LockerCustomConfig.getLogger().logEvent("ChargingPage_News", "news_nofill", "Load_failed");
            }
            return;
        }

        Intent intent = new Intent(HSApplication.getContext(), SmartLockerFeedsActivity.class);
        intent.putExtra(SmartLockerFeedsActivity.EXTRA_INT_BATTERY_LEVEL_PERCENT,
                HSChargingManager.getInstance().getBatteryRemainingPercent());
        intent.putExtra(EXTRA_START_TYPE, startType);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);

        try {
            HSApplication.getContext().startActivity(intent);
            LockerCustomConfig.getLogger().logEvent("SmartLockerPage_Should_Viewed",
                    "DeviceInfo", Build.MODEL, "SystemVersion", Build.VERSION.RELEASE);
            AutopilotEvent.logAppEvent("smartlock_should_viewed");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void tryToPreLoadBaiduNews() {
        if (!HSConfig.optBoolean(false, "Application", "CableFeedTest", "IFCableFeed")) {
            return;
        }

        BaiduFeedManager.getInstance().loadNews(BaiduFeedManager.CATEGORY_ALL, BaiduFeedManager.LOAD_FIRST, new BaiduFeedManager.DataBackListener() {
            @Override
            public void onDataBack(JSONObject response) {
                HSLog.d(TAG, "tryToPreLoadBaiduNews onDataBack response success? " + (response != null));
                if (response != null) {
                    DailyNewsUtils.saveNews(BaiduFeedManager.CATEGORY_ALL, response.toString());
                }
            }
        });
    }

    private boolean isOpenChargingScreenWhenSwitchChanged() {
//        switch (SettingProvider.getSmartChargingSwitchLastSettingModule(HSApplication.getContext())) {
//            case SettingProvider.MODULE_CHARGING_SCREEN:
//            case SettingProvider.MODULE_SETTINGS:
//                return true;
//            default:
//                return false;
//        }
        return false;
    }

    private void registerScreenOffReceiver() {
        if (isRegisteredScreenOff) {
            return;
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        HSApplication.getContext().registerReceiver(screenOffReceiver, intentFilter, null, workHandler);

        isRegisteredScreenOff = true;
    }

    private void unregisterScreenOffReceiver() {
        if (!isRegisteredScreenOff) {
            return;
        }

        HSApplication.getContext().unregisterReceiver(screenOffReceiver);
        HSLog.d(TAG, "unregisterScreenOffReceiver() unregisterReceiver:screenOffReceiver");

        isRegisteredScreenOff = false;
    }
}
