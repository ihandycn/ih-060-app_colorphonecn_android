package com.honeycomb.colorphone.factoryimpl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.text.format.DateUtils;
import android.view.View;

import com.acb.call.VideoManager;
import com.acb.call.service.InCallWindow;
import com.call.assistant.customize.ThemeViewConfig;
import com.call.assistant.receiver.IncomingCallReceiver;
import com.call.assistant.ui.CallIdleAlert;
import com.call.assistant.ui.CallIdleAlertActivity;
import com.call.assistant.ui.CallIdleAlertView;
import com.colorphone.lock.util.ConfigUtils;
import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.FlashManager;
import com.honeycomb.colorphone.Placements;
import com.honeycomb.colorphone.activity.NotificationAccessGuideAlertActivity;
import com.honeycomb.colorphone.activity.RateAlertActivity;
import com.honeycomb.colorphone.dialog.FiveStarRateTip;
import com.honeycomb.colorphone.notification.NotificationConfig;
import com.honeycomb.colorphone.themeselector.ThemeGuide;
import com.honeycomb.colorphone.util.ADAutoPilotUtils;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.CallFinishUtils;
import com.honeycomb.colorphone.util.ModuleUtils;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.flashlight.FlashlightManager;
import com.superapps.util.Compats;
import com.superapps.util.Permissions;
import com.superapps.util.Preferences;

import net.appcloudbox.autopilot.AutopilotEvent;

import static com.acb.call.activity.AcceptCallActivity.PREFS_ACCEPT_FAIL;
import static com.honeycomb.colorphone.activity.NotificationAccessGuideAlertActivity.ACB_PHONE_NOTIFICATION_ACCESS_GUIDE_OUT_APP_LAST_SHOW_TIME;
import static com.honeycomb.colorphone.activity.NotificationAccessGuideAlertActivity.ACB_PHONE_NOTIFICATION_ACCESS_GUIDE_OUT_APP_SHOW_COUNT;

/**
 * Created by jelly on 2018/3/17.
 */

public class CpCallAssistantFactoryImpl extends com.call.assistant.customize.CallAssistantFactoryImpl {

    @Override
    public boolean isCallAssistantOpenDefault() {
        return HSConfig.optBoolean(false, "Application", "ScreenFlash", "CallAssistant", "DefaultEnabled")
                && !(HSConfig.optBoolean(false, "Application", "AdProtection", "CallAssistantEnableAfterInstallMinutes")
                && ConfigUtils.isNewUserInAdBlockStatus());
    }

    @Override
    public boolean isCallAssistantConfigEnabled() {
        return ModuleUtils.isModuleConfigEnabled(ModuleUtils.AUTO_KEY_CALL_ASSISTANT)
                && !(HSConfig.optBoolean(false, "Application", "AdProtection", "CallAssistantEnableAfterInstallMinutes")
                && ConfigUtils.isNewUserInAdBlockStatus());
    }

    @Override
    public CallIdleAlert.Config getCallIdleConfig() {
        return new CPCallIdleConfig();
    }


    @Override
    public IncomingCallReceiver.Config getIncomingReceiverConfig() {
        return new IncomingCallReceiver.Config() {
            @Override
            public boolean isShowAlertBeforeCallAssistant(String number, int callType) {
                Context context = HSApplication.getContext();
                if (callType != IncomingCallReceiver.CALL_OUT
                        && HSPreferenceHelper.getDefault().getBoolean(InCallWindow.ACB_PHONE_REJECT_CALL_BY_USER, false)
                        && ModuleUtils.isShareAlertOutsideAppShow(context, number)) {
                    return true;
                }
                if (isShowNotificationAccessOutAppGuide(context)) {
                    NotificationAccessGuideAlertActivity.startOutAppGuide(context);
                    return true;
                }
                if (callType == IncomingCallReceiver.CALL_IN_SUCCESS && FiveStarRateTip.canShowWhenEndCall()) {
                    RateAlertActivity.showRateFrom(context, FiveStarRateTip.From.END_CALL);
                    return true;
                }
                return false;
            }

        };
    }

    private boolean isShowNotificationAccessOutAppGuide(Context context) {
        boolean isAcceptCallFailed = HSPreferenceHelper.getDefault().getBoolean(PREFS_ACCEPT_FAIL, false);
        boolean isEnabled = NotificationConfig.isOutsideAppAccessAlertOpen();
        boolean isAtValidTime =
                System.currentTimeMillis() - HSPreferenceHelper.getDefault().getLong(ACB_PHONE_NOTIFICATION_ACCESS_GUIDE_OUT_APP_LAST_SHOW_TIME, 0)
                        > NotificationConfig.getOutsideAppAccessAlertInterval();
        boolean beyondMaxCount = HSPreferenceHelper.getDefault().getInt(ACB_PHONE_NOTIFICATION_ACCESS_GUIDE_OUT_APP_SHOW_COUNT, 0)
                >= NotificationConfig.getOutsideAppAccessAlertShowMaxTime();

        return isAcceptCallFailed && isEnabled && isAtValidTime && !Permissions.isNotificationAccessGranted() && !beyondMaxCount;
    }

    private static volatile boolean isADShown = false;

    @Override
    public CallIdleAlert.Event getCallIdleEvent() {
        return new CallIdleAlert.FlurryEvent() {

            private long mTimeReadyToShow;


            @Override
            public void onShouldShow(int callType, boolean isLocked) {
                isADShown = false;
                mTimeReadyToShow = System.currentTimeMillis();
                Analytics.logEvent("CallFinished_View_Should_Show",
                        "callType", getCallTypeStr(callType),
                        "Brand", Build.BRAND.toLowerCase(),
                        "Lock", String.valueOf(isLocked));
                if (isTargetBrand() && Build.VERSION.SDK_INT >= 23) {
                    Analytics.logEvent("Test_CallAssistantShouldShow" + Build.BRAND.toUpperCase() + getDeviceInfo());
                }

            }

            private boolean isTargetBrand() {
                return Compats.IS_HUAWEI_DEVICE
                        || Compats.IS_XIAOMI_DEVICE
                        || Compats.IS_OPPO_DEVICE
                        || Compats.IS_VIVO_DEVICE;
            }

            private String getDeviceInfo() {
                return String.valueOf(Build.VERSION.SDK_INT);
            }

            @Override
            public void onShow(int callType, boolean isLocked) {
                Analytics.logEvent("CallFinished_View_Shown", "callType", getCallTypeStr(callType),
                        "Time", formatTime(System.currentTimeMillis() - mTimeReadyToShow),
                        "Brand", Build.BRAND.toLowerCase());
                if (isTargetBrand() && Build.VERSION.SDK_INT >= 23) {
                    Analytics.logEvent("Test_CallAssistantShow" + Build.BRAND.toUpperCase() + getDeviceInfo());
                }
//                HSAnalytics.logEventToAppsFlyer("Call_Assistant_Can_Show");
            }

            private String formatTime(long l) {
                if (l > 1000 * 60) {
                    return "1m+";
                }
                if (l > 8000 * 2) {
                    return "16s+";
                } else if (l > 8000) {
                    return "8-16s";
                } else if (l > 3000) {
                    return "3-8s";
                } else {
                    return "0-3s";
                }
            }

            @Override
            public void onCallFinished() {
                Analytics.logEvent("ColorPhone_Call_Finished");
            }

            @Override
            public void onAdShow(int callType) {
                Analytics.logEvent("CallFinished_View_AD_Shown", "callType", getCallTypeStr(callType));
                isADShown = true;
            }

            @Override
            public void onAlertDismiss(CallIdleAlertView.CallIdleAlertDismissType dismissType) {
                super.onAlertDismiss(dismissType);
                Analytics.logEvent("CallFinished_View_Closed", "DismissType", dismissType.name());

            }

            @Override
            public void onCallFinishedCallAssistantShow() {
                Analytics.logEvent("Call_Finished_Call_Assistant_Show");
            }

            @Override
            public void onFullScreenAdShouldShow() {
                Analytics.logEvent("ColorPhone_Call_Finished_Wire_Should_Show");
            }

            @Override
            public void onFullScreenAdShow() {
                Analytics.logEvent("ColorPhone_Call_Finished_Wire_Show");
                ADAutoPilotUtils.logCallFinishWireShow();
                if (Utils.isNewUser()) {
                    Analytics.logEvent("ColorPhone_CallFinishWire_Show");
                }
                isADShown = true;
            }

            @Override
            public void onInsteadViewShown(View view) {
                ThemeGuide.parser(view);
            }

            public void logEvent(String eventID, String... vars) {
                Analytics.logEvent(eventID, vars);
            }
        };

    }

    @Override
    public ThemeViewConfig getViewConfig() {
        return new ThemeViewConfig() {
            @Override
            public CallIdleAlertView getCallIdleAlertView(CallIdleAlertActivity callIdleAlertActivity, CallIdleAlertActivity.Data data) {
                return super.getCallIdleAlertView(callIdleAlertActivity, data);
            }
        };
    }

    @Override
    public void logAppEvent(String eventName, double value) {
        AutopilotEvent.logAppEvent(eventName, value);
    }

    @Override
    public IncomingCallReceiver.Event getCallStateEvent() {

        return new IncomingCallReceiver.Event() {
            @Override
            public void onRinging(String s) {
                VideoManager.get().mute(true);
                startFlashIfProper();
            }

            @Override
            public void onIdle(int i, String s) {
                stopFlashIfProper();
            }

            @Override
            public void onHookOff(String s) {
                stopFlashIfProper();
            }

            @Override
            public void onOutGoing(String s) {

            }

        };
    }

    public void startFlashIfProper() {
        if (Preferences.get(Constants.DESKTOP_PREFS).getBoolean(Constants.PREFS_LED_FLASH_ENABLE, false)
                && !FlashlightManager.getInstance().isOn()
                && !FlashManager.getInstance().isFlash()) {
            FlashManager.getInstance().startFlash();

            AudioManager audioMgr = (AudioManager) HSApplication.getContext().getSystemService(Context.AUDIO_SERVICE);
            if (audioMgr != null) {
                ringVolume = audioMgr.getStreamVolume(AudioManager.STREAM_RING);

                observer = new SettingsContentObserver(new Handler());
                HSApplication.getContext().getContentResolver().registerContentObserver(
                        android.provider.Settings.System.CONTENT_URI, true,
                        observer);
            }

            screenOffObserver = new INotificationObserver() {
                @Override
                public void onReceive(String s, HSBundle hsBundle) {
                    stopFlashIfProper();
                }
            };

            screenOffReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    stopFlashIfProper();
                }
            };
            HSApplication.getContext().registerReceiver(screenOffReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        }
    }

    public void stopFlashIfProper() {
        if (Preferences.get(Constants.DESKTOP_PREFS).getBoolean(Constants.PREFS_LED_FLASH_ENABLE, false)
                && FlashManager.getInstance().isFlash()) {
            FlashManager.getInstance().stopFlash();

            if (observer != null) {
                HSApplication.getContext().getContentResolver().unregisterContentObserver(observer);
                observer = null;
            }

            if (screenOffObserver != null) {
                HSGlobalNotificationCenter.removeObserver(screenOffObserver);
                screenOffObserver = null;
            }
            if (screenOffReceiver != null) {
                HSApplication.getContext().unregisterReceiver(screenOffReceiver);
                screenOffReceiver = null;
            }
        }
    }

    private int ringVolume = -1;
    private SettingsContentObserver observer;
    private INotificationObserver screenOffObserver;
    private BroadcastReceiver screenOffReceiver;

    private void updateStuff() {
        AudioManager audioMgr = (AudioManager) HSApplication.getContext().getSystemService(Context.AUDIO_SERVICE);
        int volume = audioMgr.getStreamVolume(AudioManager.STREAM_RING);
        if (volume != ringVolume) {
            stopFlashIfProper();
            ringVolume = -1;
        }
    }

    private class SettingsContentObserver extends ContentObserver {

        SettingsContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public boolean deliverSelfNotifications() {
            return super.deliverSelfNotifications();
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            HSLog.v("Settings change detected");
            updateStuff();
        }
    }


    private static class CPCallIdleConfig extends CallIdleAlert.PlistConfig {
        @Override
        public String getAdPlaceName() {
            return Placements.AD_CALL_OFF;
        }

        @Override
        public boolean showMarkAsSpam() {
            return true;
        }

        @Override
        public String getFullScreenAdPlacement() {
            return "";
        }

        @Override
        public boolean enableFullScreenAd() {
            return CallFinishUtils.isCallFinishFullScreenAdEnabled();
        }

        @Override
        public int getFullScreenAdShowTimesEachDay() {
            return ADAutoPilotUtils.getCallFinishWireShowMaxTime();
        }

        @Override
        public long getFullScreenAdShowIntervalTime() {
            return ADAutoPilotUtils.getCallFinishWireTimeInterval();
        }

        @Override
        public int getAdRefreshInterval() {
            return super.getAdRefreshInterval();
        }

        @Override
        public int getInsteadLayoutID() {
            return ThemeGuide.getInsteadLayoutID();
        }

        @Override
        public boolean isTextureWireEnable() {
            return showAd() && isTextureWireOnLockEnable() && HSConfig.optBoolean(true, "Application", "ScreenFlash", "CallAssistant", "CallFinishWireEnable");
        }

        @Override
        public boolean isTextureWireOnLockEnable() {
            boolean isLocked = Utils.isKeyguardLocked(HSApplication.getContext(), true);
            boolean config = HSConfig.optBoolean(true, "Application", "ScreenFlash", "CallAssistant", "CallFinishWireShowInLock");

            if (isLocked) {
                if (config) {
                    HSLog.i("CallIdleAlertActivity", "isTextureWireOnLockEnable locked and enable ");
                } else {
                    HSLog.i("CallIdleAlertActivity", "isTextureWireOnLockEnable locked and NOT enable ");
                }
                return config;
            } else {
                HSLog.i("CallIdleAlertActivity", "isTextureWireOnLockEnable NOT locked enable ");
            }
            return true;
        }

        @Override
        public String getTextureWirePlacement() {
            return "";
        }

        @Override
        public long getTextureWireInterval() {
            return DateUtils.MINUTE_IN_MILLIS * HSConfig.optInteger(0, "Application", "ScreenFlash", "CallAssistant", "CallFinishWireIntervalMinute");
        }
    }

}
