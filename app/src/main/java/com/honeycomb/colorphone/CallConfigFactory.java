package com.honeycomb.colorphone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.Handler;

import com.acb.call.CPSettings;
import com.acb.call.customize.AcbCallFactoryImpl;
import com.acb.call.customize.AcbCallManager;
import com.acb.call.customize.ThemeViewConfig;
import com.acb.call.receiver.IncomingCallReceiver;
import com.acb.call.service.InCallWindow;
import com.acb.call.themes.Type;
import com.acb.call.utils.CallUtils;
import com.acb.call.views.CallIdleAlert;
import com.acb.notification.FloatWindowController;
import com.acb.notification.NotificationAccessGuideAlertActivity;
import com.acb.notification.NotificationMessageAlertActivity;
import com.acb.utils.MessageCenterUtils;
import com.acb.utils.NavUtils;
import com.colorphone.lock.util.CommonUtils;
import com.honeycomb.colorphone.activity.ColorPhoneActivity;
import com.honeycomb.colorphone.activity.RateAlertActivity;
import com.honeycomb.colorphone.contact.ContactManager;
import com.honeycomb.colorphone.dialog.FiveStarRateTip;
import com.honeycomb.colorphone.notification.NotificationAutoPilotUtils;
import com.honeycomb.colorphone.notification.NotificationConfig;
import com.honeycomb.colorphone.notification.NotificationServiceV18;
import com.honeycomb.colorphone.util.FontUtils;
import com.honeycomb.colorphone.util.LauncherAnalytics;
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

import net.appcloudbox.ads.nativead.AcbNativeAdAnalytics;

import java.util.Iterator;
import java.util.List;
import java.util.Random;


public class CallConfigFactory extends AcbCallFactoryImpl {

    @Override
    public boolean isModuleEnable() {
        return true;
    }

    @Override
    public boolean isScreenFlashModuleOpenedDefault() {
        return true;
    }

    @Override
    public boolean isSMSAssistantOpenDefault() {
        return false;
    }

    @Override
    public boolean isCallAssistantOpenDefault() {
        return HSConfig.optBoolean(false, "Application", "ScreenFlash", "CallAssistant", "DefaultEnabled");
    }

    @Override
    public CallIdleAlert.Config getCallIdleConfig() {
        return new CPCallIdleConfig();
    }

    @Override
    public ThemeViewConfig getViewConfig() {
        return new CPViewConfig();
    }

    @Override
    public MessageCenterUtils.Config getSMSConfig() {
        return new MessageCenterUtils.Config() {
            @Override
            public String getAdPlacement() {
                return AdPlacements.AD_MSG_NEW;
            }

            @Override
            public boolean enable() {
                return ModuleUtils.isModuleConfigEnabled(ModuleUtils.AUTO_SMS_KEY_ASSISTANT)
                        && CPSettings.isSMSAssistantModuleEnabled();
            }

            @Override
            public boolean hideNotificationGuide() {
                return false;
            }

            @Override
            public int getAppNameDrawable() {
                return R.drawable.color_phone_logo;
            }
        };
    }

    @Override
    public MessageCenterUtils.Event getSMSEvent() {
        return new MessageCenterUtils.Event() {
            public void onShow() {
                NotificationAutoPilotUtils.logMessageAssistantShow();
                NotificationAutoPilotUtils.logMessageAssistantSmsShowWhenScreenOn();
                LauncherAnalytics.logEvent("Message_View_Shown", "AlertShowWhere", "NotOnLockScreen", "MessageType", "SMS");
                LauncherAnalytics.logEvent("Message_View_SMS_Shown_NotOnLockScreen");

                CallUtils.recordAlertDailyCount(com.acb.Constants.PREFS_ALERT_DAILY_SHOW_COUNT_FILE, com.acb.Constants.PREFS_ALERT_MESSAGE_VIEW_TIME_STAMP,
                        com.acb.Constants.PREFS_ALERT_MESSAGE_VIEW_DAILY_SHOW_COUNT, "Message_View_Daily_Shown");
            }

            public void onAdShow() {
                NotificationAutoPilotUtils.logMessageAssistantAdShow();
                LauncherAnalytics.logEvent("Message_View_AD_Shown", "AlertShowWhere", "NotOnLockScreen", "MessageType", "SMS");

                CallUtils.recordAlertDailyCount(com.acb.Constants.PREFS_ALERT_DAILY_SHOW_COUNT_FILE, com.acb.Constants.PREFS_ALERT_MESSAGE_VIEW_AD_SHOW_TIME_STAMP,
                        com.acb.Constants.PREFS_ALERT_MESSAGE_VIEW_AD_DAILY_SHOW_COUNT, "Message_View_AD_Daily_Shown");
            }

            public void onAdClick() {
                LauncherAnalytics.logEvent("Message_View_AD_Clicked");
            }

            public void onAdFlurryRecord(boolean isShown) {
                AcbNativeAdAnalytics.logAppViewEvent(AcbCallManager.getInstance().getAcbCallFactory().getSMSConfig().getAdPlacement(), isShown);
            }
        };
    }


    private static class CPCallIdleConfig extends CallIdleAlert.PlistConfig {
        @Override
        public String getAdPlaceName() {
            return AdPlacements.AD_CALL_OFF;
        }

        @Override
        public int getAppNameDrawable() {
            return R.drawable.color_phone_logo;
        }
    }

    public static class CPViewConfig extends ThemeViewConfig {
        int[] faces = new int[]{
                R.drawable.face_1,
                R.drawable.face_2,
                R.drawable.face_3,
                R.drawable.face_4,
                R.drawable.face_5,
                R.drawable.face_6,
                R.drawable.face_7,
                R.drawable.face_8

        };

        @Override
        public int getCallerDefaultPhoto() {
            final int index = new Random().nextInt(900);
            return faces[index % faces.length];
        }

        @Override
        public List<?> getConfigThemes() {
            return HSConfig.getList(new String[]{"Application", "Theme", "List"});

        }

        @Override
        public Typeface getBondFont() {
            return FontUtils.getTypeface(FontUtils.Font.PROXIMA_NOVA_SEMIBOLD);
        }

        @Override
        public Typeface getNormalFont() {
            return FontUtils.getTypeface(FontUtils.Font.PROXIMA_NOVA_REGULAR);
        }

        @Override
        public void onConfigTypes(List<Type> types) {
            Iterator<Type> iter = types.iterator();
            while (iter.hasNext()) {
                Type t = iter.next();
                if (t instanceof Theme) {
                    ((Theme) t).configAvatar();
                }
                if (t.getValue() == Type.NONE) {
                    iter.remove();
                }
            }
        }
    }

    //notification
    @Override
    public NotificationAccessGuideAlertActivity.Config getNotificationAccessConfig() {
        return new NotificationAccessGuideAlertActivity.Config() {
            @Override
            public boolean isAtBottom() {
                return NotificationAutoPilotUtils.isNotificationAccessTipAtBottom();
            }

            @Override
            public boolean animated() {
                return NotificationAutoPilotUtils.isNotificationAccessTipAnimated();
            }

            @Override
            public int getAppIconId() {
                return R.drawable.drawer_icon;
            }

            @Override
            public int getAppNameId() {
                return R.string.app_name;
            }

            @Override
            public long getOutAppNotificationGuideInterval() {
                return NotificationConfig.getOutsideAppAccessAlertInterval();
            }

            @Override
            public boolean isNotificationOutsideAppGuideEnabled() {
                return NotificationConfig.isOutsideAppAccessAlertOpen();
            }

            @Override
            public int getNotificationAccessOutAppShowMaxTime() {
                return NotificationConfig.getOutsideAppAccessAlertShowMaxTime();
            }

            @Override
            public Class getParentActivity() {
                return ColorPhoneActivity.class;
            }
        };
    }

    public NotificationAccessGuideAlertActivity.Event getNotificationAccessEvent() {
        return new NotificationAccessGuideAlertActivity.Event() {
            @Override
            public void onOpenPermissionSettings(boolean insideApp, boolean isFirstSession) {
                super.onOpenPermissionSettings(insideApp, isFirstSession);
                NotificationAutoPilotUtils.logSettingsAlertShow();
            }

            @Override
            public void onNotificationAccessGranted(String fromType) {
                super.onNotificationAccessGranted(fromType);
                NotificationAutoPilotUtils.logSettingsAccessEnabled();
            }
        };
    }


    @Override
    public NotificationMessageAlertActivity.Config getNotificationConfig() {
        return new NotificationMessageAlertActivity.Config() {
            @Override
            public String getAdPlacement() {
                return AdPlacements.AD_MSG_NEW;
            }

            @Override
            public boolean showAd() {
                return true;
            }

            @Override
            public boolean enable() {
                return ModuleUtils.isModuleConfigEnabled(ModuleUtils.AUTO_SMS_KEY_ASSISTANT)
                        && NotificationAutoPilotUtils.isMessageCenterEnabled()
                        && CPSettings.isSMSAssistantModuleEnabled();
            }

            @Override
            public boolean showMessengerMessage() {
                return NotificationAutoPilotUtils.isFacebookMessengerEnabled();
            }

            @Override
            public boolean showWhatsAppMessage() {
                return NotificationAutoPilotUtils.isWhatsAppEnabled();
            }

            @Override
            public boolean showWhatsappWhenScreenOff() {
                return NotificationAutoPilotUtils.isWhatsappShowOnLock();
            }

            @Override
            public boolean showWhatsappWhenScreenOn() {
                return NotificationAutoPilotUtils.isWhatsAppShowOnUnlock();
            }

            @Override
            public boolean showFacebookMessengerWhenScreenOn() {
                return NotificationAutoPilotUtils.isFacebookMessengerShowOnUnlock();
            }

            @Override
            public boolean showFacebookMessengerWhenScreenOff() {
                return NotificationAutoPilotUtils.isFacebookMessengerShowOnLock();
            }

            @Override
            public int getAppNameDrawable() {
                return R.drawable.color_phone_logo;
            }
        };
    }

    @Override
    public NotificationMessageAlertActivity.Permission getNotificationPermission() {
        return new NotificationMessageAlertActivity.Permission() {
            @Override
            public void requestNotificationPermission() {
                Context context = HSApplication.getContext();
                Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                NavUtils.startActivitySafely(context, intent);
                FloatWindowController.getInstance().createUsageAccessTip(context);
            }
        };
    }

    @Override
    public NotificationMessageAlertActivity.Event getNotificationEvent() {
        return new NotificationMessageAlertActivity.Event() {
            @Override
            public void onShow(int[] count) {
                CallUtils.recordAlertDailyCount(com.acb.Constants.PREFS_ALERT_DAILY_SHOW_COUNT_FILE, com.acb.Constants.PREFS_ALERT_MESSAGE_VIEW_TIME_STAMP,
                        com.acb.Constants.PREFS_ALERT_MESSAGE_VIEW_DAILY_SHOW_COUNT, "Message_View_Daily_Shown");
            }

            @Override
            public void onAdShow() {
                NotificationAutoPilotUtils.logMessageAssistantAdShow();
                LauncherAnalytics.logEvent("Message_View_AD_Shown");

                CallUtils.recordAlertDailyCount(com.acb.Constants.PREFS_ALERT_DAILY_SHOW_COUNT_FILE, com.acb.Constants.PREFS_ALERT_MESSAGE_VIEW_AD_SHOW_TIME_STAMP,
                        com.acb.Constants.PREFS_ALERT_MESSAGE_VIEW_AD_DAILY_SHOW_COUNT, "Message_View_AD_Daily_Shown");
            }

            @Override
            public void onAdClick() {
                LauncherAnalytics.logEvent("Message_View_AD_Clicked");

            }

            @Override
            public void onBtnClick(String string) {
                LauncherAnalytics.logEvent("Message_View_" + string + "_Clicked");
            }

            @Override
            public void onAdFlurryRecord(boolean isShown) {
                AcbNativeAdAnalytics.logAppViewEvent(
                        AcbCallManager.getInstance().getAcbCallFactory().getSMSConfig().getAdPlacement(), isShown);
            }

            @Override
            public void onContentClick() {

            }


            @Override
            public void onShow(int[] smsCount, boolean showOnLock, boolean isOverlayed) {
                boolean hasSms = smsCount[0] > 0;
                boolean hasWhatsApp = smsCount[1] > 0;
                boolean hasMessenger = smsCount[6] > 0;
                String lockFlurry = showOnLock ? "OnLockScreen" : "NotOnLockScreen";
                String overlayed = String.valueOf(isOverlayed);
                if (hasSms && !hasWhatsApp && !hasMessenger) {
                    LauncherAnalytics.logEvent("Message_View_Shown", "AlertShowWhere", lockFlurry, "MessageType", "SMS", "isOverlayed", overlayed);
                    if(!showOnLock) {
                        LauncherAnalytics.logEvent("Message_View_SMS_Shown_NotOnLockScreen");
                        NotificationAutoPilotUtils.logMessageAssistantSmsShowWhenScreenOn();
                    }
                } else if (hasSms) {
                    LauncherAnalytics.logEvent("Message_View_Shown", "AlertShowWhere", lockFlurry, "MessageType", "Multi","isOverlayed", overlayed);
                } else if (hasWhatsApp && !hasMessenger) {
                    LauncherAnalytics.logEvent("Message_View_Shown", "AlertShowWhere", lockFlurry, "MessageType", "WhatsApp", "isOverlayed", overlayed);
                    if(!showOnLock) {
                        LauncherAnalytics.logEvent("Message_View_WhatsApp_Shown_NotOnLockScreen");
                        NotificationAutoPilotUtils.logMessageAssistantWhatsappShowWhenScreenOn();
                    }
                } else {
                    LauncherAnalytics.logEvent("Message_View_Shown", "AlertShowWhere", lockFlurry, "MessageType", "Messenger", "isOverlayed", overlayed);
                    if(!showOnLock) {
                        LauncherAnalytics.logEvent("Message_View_Messenger_Shown_NotOnLockScreen");
                        NotificationAutoPilotUtils.logMessageAssistantMessengerShowWhenScreenOn();
                    }
                }

                String messageType = "";
                if (hasSms) {
                    messageType += "SMS";
                }
                if (hasWhatsApp) {
                    messageType += "+WhatsApp";
                }
                if(hasMessenger) {
                    messageType += "+Messenger";
                }

                if (showOnLock) {
                    LauncherAnalytics.logEvent("Message_View_Shown_OnLockScreen", "Type", messageType);
                    NotificationAutoPilotUtils.logMessageAssistantShowOnLockScreen();
                }

                NotificationAutoPilotUtils.logMessageAssistantShow();
            }
        };
    }

    @Override
    public Class getNotificationServiceClass() {
        try {
            if (CommonUtils.ATLEAST_JB_MR2) {
                return NotificationServiceV18.class;
            } else {
                return null;
            }
        } catch (Exception ignore) {
            return null;
        }
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
                if (CPSettings.isShowNotificationAccessOutAppGuide(CallConfigFactory.this, context)) {
                    NotificationAccessGuideAlertActivity.startOutAppGuide(context);
                    return true;
                }
                if (callType == IncomingCallReceiver.CALL_IN_SUCCESS && FiveStarRateTip.canShowWhenEndCall()) {
                    RateAlertActivity.showRateFrom(context, FiveStarRateTip.From.END_CALL);
                    return true;
                }
                return false;
            }

            @Override
            public int getThemeIdByPhoneNumber(String number) {
                int themeId = ContactManager.getInstance().getThemeIdByNumber(number);
                if (themeId > 0) {
                    return themeId;
                } else {
                    return super.getThemeIdByPhoneNumber(number);
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
            }

            @Override
            public void onIdle(int i, String s) {
                stopFlashIfProper();
            }

            @Override
            public void onHookOff(String s) {

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

}
