package com.honeycomb.colorphone.factoryimpl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;

import com.acb.call.service.InCallWindow;
import com.call.assistant.receiver.IncomingCallReceiver;
import com.call.assistant.ui.CallIdleAlert;
import com.honeycomb.colorphone.AdPlacements;
import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.FlashManager;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.activity.NotificationAccessGuideAlertActivity;
import com.honeycomb.colorphone.activity.RateAlertActivity;
import com.honeycomb.colorphone.dialog.FiveStarRateTip;
import com.honeycomb.colorphone.notification.NotificationConfig;
import com.honeycomb.colorphone.notification.permission.PermissionUtils;
import com.honeycomb.colorphone.util.CallFinishUtils;
import com.honeycomb.colorphone.util.ModuleUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.flashlight.FlashlightManager;
import com.ihs.libcharging.ScreenStateMgr;
import com.superapps.util.Preferences;

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

    private  boolean isShowNotificationAccessOutAppGuide(Context context) {
        boolean isAcceptCallFailed = HSPreferenceHelper.getDefault().getBoolean(PREFS_ACCEPT_FAIL, false);
        boolean isEnabled = NotificationConfig.isOutsideAppAccessAlertOpen();
        boolean isAtValidTime =
                System.currentTimeMillis() - HSPreferenceHelper.getDefault().getLong(ACB_PHONE_NOTIFICATION_ACCESS_GUIDE_OUT_APP_LAST_SHOW_TIME, 0)
                        > NotificationConfig.getOutsideAppAccessAlertInterval();
        boolean beyondMaxCount = HSPreferenceHelper.getDefault().getInt(ACB_PHONE_NOTIFICATION_ACCESS_GUIDE_OUT_APP_SHOW_COUNT, 0)
                >= NotificationConfig.getOutsideAppAccessAlertShowMaxTime();

        return isAcceptCallFailed && isEnabled && isAtValidTime && !PermissionUtils.isNotificationAccessGranted(context) && !beyondMaxCount;
    }

    @Override
    public CallIdleAlert.Event getCallIdleEvent() {
        return new CallIdleAlert.FlurryEvent() {
            @Override
            public void onCallFinished() {
                CallFinishUtils.logCallFinish();
            }

            @Override
            public void onCallFinishedCallAssistantShow() {
                CallFinishUtils.logCallFinishCallAssistantShow();
            }

            @Override
            public void onFullScreenAdShouldShow() {
                CallFinishUtils.logCallFinishWiredShouldShow();
            }

            @Override
            public void onFullScreenAdShow() {
                CallFinishUtils.logCallFinishWiredShow();
            }
        };
    }

    @Override
    public IncomingCallReceiver.Event getCallStateEvent() {

        return new IncomingCallReceiver.Event() {
            @Override
            public void onRinging(String s) {
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

            @Override
            public void inCallWindowShow(String themeName) {

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
    }

}
