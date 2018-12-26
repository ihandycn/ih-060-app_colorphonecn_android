package com.honeycomb.colorphone.factoryimpl;

import android.content.Context;
import android.content.Intent;

import com.honeycomb.colorphone.AdPlacements;
import com.honeycomb.colorphone.Ap;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.notification.NotificationAutoPilotUtils;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.honeycomb.colorphone.util.ModuleUtils;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.messagecenter.customize.MessageCenterSettings;
import com.messagecenter.notification.NotificationMessageAlertActivity;
import com.messagecenter.sms.SmsMessageAlertActivity;
import com.superapps.util.Navigations;

/**
 * Created by jelly on 2018/3/17.
 */

public class CpMessageCenterFactoryImpl extends com.messagecenter.customize.MessageCenterFactoryImpl {

    @Override
    public SmsMessageAlertActivity.Config getSMSConfig() {
        return new SmsMessageAlertActivity.Config() {
            @Override
            public String getAdPlacement() {
                return AdPlacements.AD_MSG_NEW;
            }

            @Override
            public boolean configEnabled() {
                return  ModuleUtils.isModuleConfigEnabled(ModuleUtils.AUTO_SMS_KEY_ASSISTANT)
                        && MessageCenterSettings.isSMSAssistantModuleEnabled();
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
    public boolean isSMSAssistantOpenDefault() {
        return false;
    }

    @Override
    public NotificationMessageAlertActivity.Config getNotificationMessageConfig() {
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
                        && MessageCenterSettings.isSMSAssistantModuleEnabled();
            }

            @Override
            public boolean isShowFloatingBall() {
                return HSApplication.getFirstLaunchInfo().appVersionCode > 5 && Ap.MsgBall.enable();
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
            public boolean showGmailMessage() {
                return NotificationAutoPilotUtils.isGmailEnabled();
            }

            @Override
            public boolean showGmailWhenScreenOff() {
                return NotificationAutoPilotUtils.isGmailEnabledWhenScreenOff();
            }

            @Override
            public boolean showGmailWhenScreenOn() {
                return NotificationAutoPilotUtils.isGmailEnabledWhenScreenOn();
            }

            @Override
            public boolean showSmsMessage() {
                return NotificationAutoPilotUtils.isSmsEnabled();
            }

            @Override
            public boolean showSmsWhenScreenOff() {
                return NotificationAutoPilotUtils.isSmsEnabledWhenScreenOff();
            }

            @Override
            public boolean showSmsWhenScreenOn() {
                return NotificationAutoPilotUtils.isSmsEnabledWhenScreenOn();
            }

            public int getAppNameDrawable() {
                return R.drawable.color_phone_logo;
            }

        };
    }

    @Override
    public NotificationMessageAlertActivity.Permission getNotificationMessagePermission() {
        return new NotificationMessageAlertActivity.Permission() {
            @Override
            public void requestNotificationPermission() {
                Context context = HSApplication.getContext();
                Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                Navigations.startActivitySafely(context, intent);
            }
        };
    }

    @Override
    public NotificationMessageAlertActivity.Event getNotificationMessageEvent() {
        return new NotificationMessageAlertActivity.Event() {

            @Override
            public void onAdShow() {
                NotificationAutoPilotUtils.logMessageAssistantAdShow();
                LauncherAnalytics.logEvent("Message_View_AD_Shown");
                if (getNotificationMessageConfig().isShowFloatingBall()) {
                    Ap.MsgBall.onAdShow();
                }
            }

            @Override
            public void onAdClick() {
                LauncherAnalytics.logEvent("Message_View_AD_Clicked");
            }

            @Override
            public void onAdFlurryRecord(boolean isShown) {

            }

            @Override
            public void messageViewShowed(int msgSrcCount, String flurryMessageType, String flurryAlertShowWhere) {
                NotificationAutoPilotUtils.logMessageAssistantShow();
                LauncherAnalytics.logEvent("Message_View_Shown", "msgSrcCount", String.valueOf(msgSrcCount),
                        "messageType", flurryMessageType, "AlertShowWhere", flurryAlertShowWhere);

                if (flurryAlertShowWhere.equals("OnLockScreen")) {
                    NotificationAutoPilotUtils.logMessageAssistantShowOnLockScreen();
                    HSAnalytics.logEvent("Message_View_Shown_OnLockScreen", "messageType", flurryMessageType);
                }
            }

            @Override
            public void floatingBallShow(int count, String from) {
                LauncherAnalytics.logEvent("ColorPhone_Message_FloatingBall_View_Show", "MsgCount", String.valueOf(count));
                Ap.MsgBall.onShow();

            }

            @Override
            public void floatingBallClicked(int count) {
                LauncherAnalytics.logEvent("ColorPhone_Message_FloatingBall_View_Click", "MsgCount", String.valueOf(count));
                Ap.MsgBall.onClick();
            }

            @Override
            public void floatingBallCanceled(int count) {
                LauncherAnalytics.logEvent("ColorPhone_Message_FloatingBall_Cancel", "MsgCount", String.valueOf(count));
                Ap.MsgBall.onCancel();
            }
        };
    }


}
