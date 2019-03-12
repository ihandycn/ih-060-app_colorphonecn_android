package com.honeycomb.colorphone.factoryimpl;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;

import com.acb.call.customize.ScreenFlashManager;
import com.acb.call.customize.ScreenFlashSettings;
import com.acb.call.service.InCallWindow;
import com.call.assistant.customize.ThemeViewConfig;
import com.call.assistant.receiver.IncomingCallReceiver;
import com.call.assistant.ui.CallIdleAlert;
import com.call.assistant.ui.CallIdleAlertActivity;
import com.call.assistant.ui.CallIdleAlertView;
import com.honeycomb.colorphone.AdPlacements;
import com.honeycomb.colorphone.Ap;
import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.FlashManager;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.activity.NotificationAccessGuideAlertActivity;
import com.honeycomb.colorphone.activity.RateAlertActivity;
import com.honeycomb.colorphone.cashcenter.CashUtils;
import com.honeycomb.colorphone.cashcenter.CustomCallIdleAlert;
import com.honeycomb.colorphone.contact.ContactManager;
import com.honeycomb.colorphone.contact.SimpleContact;
import com.honeycomb.colorphone.dialog.FiveStarRateTip;
import com.honeycomb.colorphone.notification.NotificationConfig;
import com.honeycomb.colorphone.permission.OutsidePermissionGuideActivity;
import com.honeycomb.colorphone.resultpage.ResultPageManager;
import com.honeycomb.colorphone.themerecommend.ThemeRecommendActivity;
import com.honeycomb.colorphone.themerecommend.ThemeRecommendManager;
import com.honeycomb.colorphone.themeselector.ThemeGuide;
import com.honeycomb.colorphone.triviatip.TriviaTip;
import com.honeycomb.colorphone.util.ADAutoPilotUtils;
import com.honeycomb.colorphone.util.CallFinishUtils;
import com.honeycomb.colorphone.util.ColorPhoneCrashlytics;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.honeycomb.colorphone.util.ModuleUtils;
import com.honeycomb.colorphone.util.PermissionTestUtils;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.flashlight.FlashlightManager;
import com.ihs.libcharging.ScreenStateMgr;
import com.superapps.util.Compats;
import com.superapps.util.Permissions;
import com.superapps.util.Preferences;
import com.superapps.util.RuntimePermissions;
import com.superapps.util.Threads;

import static com.acb.call.activity.AcceptCallActivity.PREFS_ACCEPT_FAIL;
import static com.honeycomb.colorphone.activity.NotificationAccessGuideAlertActivity.ACB_PHONE_NOTIFICATION_ACCESS_GUIDE_OUT_APP_LAST_SHOW_TIME;
import static com.honeycomb.colorphone.activity.NotificationAccessGuideAlertActivity.ACB_PHONE_NOTIFICATION_ACCESS_GUIDE_OUT_APP_SHOW_COUNT;

/**
 * Created by jelly on 2018/3/17.
 */

public class CpCallAssistantFactoryImpl extends com.call.assistant.customize.CallAssistantFactoryImpl {

    @Override
    public boolean isCallAssistantOpenDefault() {
        return HSConfig.optBoolean(false, "Application", "ScreenFlash", "CallAssistant", "DefaultEnabled");
    }

    @Override
    public boolean isCallAssistantConfigEnabled() {
        return ModuleUtils.isModuleConfigEnabled(ModuleUtils.AUTO_KEY_CALL_ASSISTANT);
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
            final Runnable mDisplayTimeoutRunnable = new Runnable() {
                @Override
                public void run() {
                    ColorPhoneCrashlytics.getInstance().logException(new IllegalArgumentException("TimeOutNotShowCallAssistant"));
                }
            };

            final Runnable mDisplayTimeoutRunnable2 = new Runnable() {
                @Override
                public void run() {
                    ColorPhoneCrashlytics.getInstance().logException(new IllegalStateException("TimeOutNotShowCallAssistantTarget, contact : " +
                            RuntimePermissions.checkSelfPermission(HSApplication.getContext(), Manifest.permission.READ_CONTACTS)));
                }
            };

            @Override
            public void onShouldShow(int callType, boolean isLocked) {
                isADShown = false;
                mTimeReadyToShow = System.currentTimeMillis();
                LauncherAnalytics.logEvent("CallFinished_View_Should_Show", "callType", getCallTypeStr(callType));
                if (isTargetBrand() && Build.VERSION.SDK_INT >= 23) {
                    LauncherAnalytics.logEvent("Test_CallAssistantShouldShow" + Build.BRAND + getDeviceInfo());
                    Threads.removeOnMainThread(mDisplayTimeoutRunnable2);
                    Threads.postOnMainThreadDelayed(mDisplayTimeoutRunnable2, 8000);
                } else {
                    Threads.removeOnMainThread(mDisplayTimeoutRunnable);
                    Threads.postOnMainThreadDelayed(mDisplayTimeoutRunnable, 8000);
                }

            }

            private boolean isTargetBrand() {
                return Compats.IS_HUAWEI_DEVICE
                        || Compats.IS_XIAOMI_DEVICE
                        || Compats.IS_OPPO_DEVICE
                        || Compats.IS_VIVO_DEVICE;
            }

            private String getDeviceInfo() {
                if (Build.VERSION.SDK_INT >= 26) {
                    return "8";
                } else if (Build.VERSION.SDK_INT >= 24) {
                    return "7";
                } else if (Build.VERSION.SDK_INT >= 23) {
                    return "6";
                }
                return "";
            }

            @Override
            public void onShow(int callType, boolean isLocked) {
                Threads.removeOnMainThread(mDisplayTimeoutRunnable);
                Threads.removeOnMainThread(mDisplayTimeoutRunnable2);
                LauncherAnalytics.logEvent("CallFinished_View_Shown", "callType", getCallTypeStr(callType),
                        "Time", formatTime(System.currentTimeMillis() - mTimeReadyToShow));
                if (isTargetBrand() && Build.VERSION.SDK_INT >= 23) {
                    LauncherAnalytics.logEvent("Test_CallAssistantShow" + Build.BRAND + getDeviceInfo());
                }
                HSAnalytics.logEventToAppsFlyer("Call_Assistant_Can_Show");
                ResultPageManager.getInstance().preloadThemeRecommendAds();
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
                LauncherAnalytics.logEvent( "ColorPhone_Call_Finished");
                if (TriviaTip.isModuleEnable()
                        && Ap.TriviaTip.enableAdShowBeforeTrivia()) {
                    TriviaTip.getInstance().preloadAd();
                }

                if (PermissionTestUtils.getAlertOutSideApp()
                        && Permissions.hasPermission(Manifest.permission.READ_PHONE_STATE)
                        && (ScreenFlashManager.getInstance().getAcbCallFactory().isConfigEnabled()
                        && ScreenFlashSettings.isScreenFlashModuleEnabled()
                        && (!Permissions.hasPermission(Manifest.permission.READ_CONTACTS)
                        || !Permissions.isNotificationAccessGranted()))) {

                    Threads.postOnMainThreadDelayed(() -> {
                        if (!isADShown) {
                            Preferences.get(Constants.DESKTOP_PREFS).doLimitedTimes(() ->
                                            OutsidePermissionGuideActivity.start(HSApplication.getContext()),
                                    "alert_show_maxtime", PermissionTestUtils.getAlertShowMaxTime());
                        }
                    }, 1000);
                } else {
                    HSLog.i("PermissionNewUI", "outside enable: " + PermissionTestUtils.getAlertOutSideApp()
                            + "  Phone: " + Permissions.hasPermission(Manifest.permission.READ_PHONE_STATE)
                            + "  sf enable: " + ScreenFlashManager.getInstance().getAcbCallFactory().isConfigEnabled()
                            + "  setting: " + ScreenFlashSettings.isScreenFlashModuleEnabled()
                            + "  permission: " + (!Permissions.hasPermission(Manifest.permission.READ_CONTACTS)
                            || !Permissions.isNotificationAccessGranted()));
                }
            }

            @Override
            public void onAdShow(int callType) {
                super.onAdShow(callType);
                isADShown = true;
                HSGlobalNotificationCenter.sendNotification(OutsidePermissionGuideActivity.EVENT_DISMISS);
            }

            @Override
            public void onCallFinishedCallAssistantShow(String number) {
                ThemeRecommendManager.getInstance().increaseCallTimes(number);
                LauncherAnalytics.logEvent("ColorPhone_Call_Finished_Call_Assistant_Show");
                ThemeRecommendManager.getInstance().getRecommendThemeIdAndRecord(number);
            }

            @Override
            public void onFullScreenAdShouldShow() {
                LauncherAnalytics.logEvent( "ColorPhone_Call_Finished_Wire_Should_Show");
            }

            @Override
            public void onFullScreenAdShow() {
                LauncherAnalytics.logEvent( "ColorPhone_Call_Finished_Wire_Show");
                ADAutoPilotUtils.logCallFinishWireShow();
                if (Utils.isNewUser()) {
                    LauncherAnalytics.logEvent("ColorPhone_CallFinishWire_Show");
                }
                isADShown = true;
                HSGlobalNotificationCenter.sendNotification(OutsidePermissionGuideActivity.EVENT_DISMISS);
            }

            @Override public void onInsteadViewShown(View view) {
                ThemeGuide.parser(view);
            }

            @Override
            public void onAlertDismiss(CallIdleAlertView.CallIdleAlertDismissType dismissType, String phoneNumber, int callType) {
                HSLog.d("ThemeRecommendManager", "phoneNumber = " + phoneNumber + ", dismissType = " + dismissType);
                if (dismissType == CallIdleAlertView.CallIdleAlertDismissType.CLOSE
                        || dismissType == CallIdleAlertView.CallIdleAlertDismissType.MENU_CLOSE
                        || dismissType == CallIdleAlertView.CallIdleAlertDismissType.BACK) {
                    ThemeRecommendManager.logThemeRecommendCallAssistantClose();
                    SimpleContact sc = ContactManager.getInstance().getContact(phoneNumber);
                    if (sc == null) {
                        LauncherAnalytics.logEvent("ColorPhone_CallAssistant_Close", "type", "Stranger");
                    } else {
                        LauncherAnalytics.logEvent("ColorPhone_CallAssistant_Close", "type", "Contact");
                    }

                    boolean isCouldShowThemeRecommend = ThemeRecommendManager.getInstance().isShowRecommendTheme(phoneNumber);
                    if (isCouldShowThemeRecommend) {
                        ThemeRecommendManager.logThemeRecommendShouldShow();
                    }

                    String themeIdName = ThemeRecommendManager.getInstance().getRecommendThemeIdAndRecord(phoneNumber, false);
                    if (!TextUtils.isEmpty(themeIdName)) {
                        HSLog.d("ThemeRecommendManager", "phoneNumber = " + phoneNumber + ", isCouldShowThemeRecommend = " + isCouldShowThemeRecommend);
                        if (isCouldShowThemeRecommend) {
                            ThemeRecommendManager.getInstance().recordThemeRecommendShow(phoneNumber);
                            ThemeRecommendActivity.start(HSApplication.getContext(), phoneNumber, themeIdName);
                            ThemeRecommendManager.getInstance().getRecommendThemeIdAndRecord(phoneNumber, true);
                        }
                    }
                }
            }
        };
    }

    @Override
    public ThemeViewConfig getViewConfig() {
        return new ThemeViewConfig() {
            @Override
            public CallIdleAlertView getCallIdleAlertView(CallIdleAlertActivity callIdleAlertActivity, CallIdleAlertActivity.Data data) {
                if (CashUtils.showEntranceAtCallAlert()) {
                    return new CustomCallIdleAlert(callIdleAlertActivity, data);
                } else {
                    return super.getCallIdleAlertView(callIdleAlertActivity, data);
                }
            }
        };
    }

    @Override
    public IncomingCallReceiver.Event getCallStateEvent() {

        return new IncomingCallReceiver.Event() {
            @Override
            public void onRinging(String s) {
                startFlashIfProper();
                ResultPageManager.getInstance().preloadThemeRecommendAds();
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
                ResultPageManager.getInstance().preloadThemeRecommendAds();
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
                @Override public void onReceive(String s, HSBundle hsBundle) {
                    stopFlashIfProper();
                }
            };
            HSGlobalNotificationCenter.addObserver(ScreenStateMgr.ACTION_SCREEN_OFF, screenOffObserver);

            screenOffReceiver = new BroadcastReceiver() {
                @Override public void onReceive(Context context, Intent intent) {
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
            return AdPlacements.AD_CALL_OFF;
        }

        @Override
        public boolean showMarkAsSpam() {
            return true;
        }

        public int getAppNameDrawable() {
            return R.drawable.color_phone_logo;
        }

        @Override
        public String getFullScreenAdPlacement() {
            return AdPlacements.AD_CALL_ASSISTANT_FULL_SCREEN;
        }

        @Override
        public boolean enableFullScreenAd() {
            return CallFinishUtils.isCallFinishFullScreenAdEnabled();
        }

        @Override public int getFullScreenAdShowTimesEachDay() {
            return ADAutoPilotUtils.getCallFinishWireShowMaxTime();
        }

        @Override public long getFullScreenAdShowIntervalTime() {
            return ADAutoPilotUtils.getCallFinishWireTimeInterval();
        }

        @Override public int getAdRefreshInterval() {
            return super.getAdRefreshInterval();
        }

        @Override public int getInsteadLayoutID() {
            return ThemeGuide.getInsteadLayoutID();
        }
    }

}
