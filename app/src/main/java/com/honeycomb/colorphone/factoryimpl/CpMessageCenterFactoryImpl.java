package com.honeycomb.colorphone.factoryimpl;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.acb.call.utils.CallUtils;
import com.acb.utils.NavUtils;
import com.honeycomb.colorphone.AdPlacements;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.notification.NotificationAutoPilotUtils;
import com.honeycomb.colorphone.notification.NotificationServiceV18;
import com.honeycomb.colorphone.notification.floatwindow.FloatWindowController;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.honeycomb.colorphone.util.ModuleUtils;
import com.ihs.app.framework.HSApplication;
import com.messagecenter.customize.MessageCenterSettings;
import com.messagecenter.notification.NotificationMessageAlertActivity;
import com.messagecenter.sms.SmsMessageAlertActivity;

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
                return ModuleUtils.isModuleConfigEnabled(ModuleUtils.AUTO_SMS_KEY_ASSISTANT)
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
                return ModuleUtils. isModuleConfigEnabled(ModuleUtils.AUTO_SMS_KEY_ASSISTANT)
                        && NotificationAutoPilotUtils.isMessageCenterEnabled()
                        && MessageCenterSettings.isSMSAssistantModuleEnabled();
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
                return true;
            }

            @Override
            public boolean showGmailWhenScreenOff() {
                return true;
            }

            @Override
            public boolean showGmailWhenScreenOn() {
                return true;
            }

            @Override
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
                NavUtils.startActivitySafely(context, intent);
                FloatWindowController.getInstance().createUsageAccessTip(context);
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

                CallUtils.recordAlertDailyCount(com.acb.Constants.PREFS_ALERT_DAILY_SHOW_COUNT_FILE, com.acb.Constants.PREFS_ALERT_MESSAGE_VIEW_AD_SHOW_TIME_STAMP,
                        com.acb.Constants.PREFS_ALERT_MESSAGE_VIEW_AD_DAILY_SHOW_COUNT, "Message_View_AD_Daily_Shown");
            }

            @Override
            public void onAdClick() {
                LauncherAnalytics.logEvent("Message_View_AD_Clicked");

            }

            @Override
            public void onAdFlurryRecord(boolean isShown) {

            }

        };
    }


}
