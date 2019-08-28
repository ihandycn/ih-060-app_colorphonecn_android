package com.honeycomb.colorphone.factoryimpl;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;

import com.colorphone.lock.lockscreen.BaseKeyguardActivity;
import com.colorphone.lock.lockscreen.FloatWindowController;
import com.colorphone.lock.lockscreen.locker.NotificationWindowHolder;
import com.honeycomb.colorphone.Placements;
import com.honeycomb.colorphone.activity.ExitNewsActivity;
import com.honeycomb.colorphone.preview.PreviewAdManager;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.ModuleUtils;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.messagecenter.customize.MessageCenterSettings;
import com.messagecenter.notification.NotificationMessageAlertActivity;
import com.superapps.util.Navigations;

/**
 * Created by jelly on 2018/3/17.
 */

public class CpMessageCenterFactoryImpl extends com.messagecenter.customize.MessageCenterFactoryImpl {


    @Override
    public boolean isSMSAssistantOpenDefault() {
        return false;
    }

    @Override
    public NotificationMessageAlertActivity.Config getNotificationMessageConfig() {
        return new NotificationMessageAlertActivity.Config() {
            @Override
            public String getAdPlacement() {
                return Placements.AD_MSG;
            }

            @Override
            public boolean showAd() {
                return HSConfig.optBoolean(true, "Application", "ScreenFlash", "SmsAssistant", "ShowAds");
            }

            @Override
            public boolean enable() {
                return ModuleUtils.isModuleConfigEnabled(ModuleUtils.AUTO_SMS_KEY_ASSISTANT)
                        && MessageCenterSettings.isSMSAssistantModuleEnabled();
            }

            @Override public boolean waitForLocker() {
                return (FloatWindowController.getInstance().isLockScreenShown() || BaseKeyguardActivity.exist)
                        && !Utils.isKeyguardLocked(HSApplication.getContext(), false);
            }

            @Override public String getLockerDismissEvent() {
                return FloatWindowController.NOTIFY_KEY_LOCKER_DISMISS;
            }

            @Override public String getRemoveMessageEvent() {
                return NotificationWindowHolder.NOTIFY_KEY_REMOVE_MESSAGE;
            }

            @Override public String getRemoveMessageParam() {
                return NotificationWindowHolder.BUNDLE_KEY_PACKAGE_NAME;
            }

            @Override
            public boolean isShowFloatingBall() {
                return false;
            }

            @Override
            public boolean showMessengerMessage() {
                return false;
            }

            @Override
            public boolean showWhatsAppMessage() {
                return false;
            }

            @Override
            public boolean showWhatsappWhenScreenOff() {
                return false;
            }

            @Override
            public boolean showWhatsappWhenScreenOn() {
                return false;
            }

            @Override
            public boolean showWeChatMessage() {
                return HSConfig.optBoolean(true, "Application", "ScreenFlash", "SmsAssistant", "SourceSwitch", "WeChat");
            }

            @Override
            public boolean showWeChatWhenScreenOff() {
                return showWeChatMessage();
            }

            @Override
            public boolean showWeChatWhenScreenOn() {
                return showWeChatMessage()
                        && !HSConfig.optBoolean(true, "Application", "ScreenFlash", "SmsAssistant", "NotShowOnScreen");
            }

            @Override
            public boolean showFacebookMessengerWhenScreenOn() {
                return false;
            }

            @Override
            public boolean showFacebookMessengerWhenScreenOff() {
                return false;
            }

            @Override
            public boolean showGmailMessage() {
                return false;
            }

            @Override
            public boolean showGmailWhenScreenOff() {
                return false;
            }

            @Override
            public boolean showGmailWhenScreenOn() {
                return false;
            }

            @Override
            public boolean showSmsMessage() {
                return HSConfig.optBoolean(true, "Application", "ScreenFlash", "SmsAssistant", "SourceSwitch", "SMS");
            }

            @Override
            public boolean showSmsWhenScreenOff() {
                return showSmsMessage();
            }

            @Override
            public boolean showSmsWhenScreenOn() {
                return showSmsMessage()
                        && !HSConfig.optBoolean(true, "Application", "ScreenFlash", "SmsAssistant", "NotShowOnScreen");
            }

            @Override
            public long getMessageAssistantIntervalInMilli() {
                return DateUtils.MINUTE_IN_MILLIS *
                        HSConfig.optInteger( 0, "Application", "ScreenFlash", "MessageAssistant", "IntervalInMinute");
            }

            @Override
            public boolean isTextureWireEnable() {
                boolean ret = showAd() && HSConfig.optBoolean(true, "Application", "ScreenFlash", "SmsAssistant", "TextureWireEnable");
                if (ret) {
                    HSLog.i("NotificationMessageAlertActivity", "isTextureWireEnable enable, preload ");
                    PreviewAdManager.getInstance().preload(null);
                } else {
                    HSLog.i("NotificationMessageAlertActivity", "isTextureWireEnable NOT enable ");
                }
                return ret;
            }

            @Override
            public String getTextureWirePlacement() {
                return Placements.AD_EXIT_TEXTURE_WIRE;
            }

            @Override
            public long getTextureWireInterval() {
                return DateUtils.MINUTE_IN_MILLIS * HSConfig.optInteger(0, "Application", "ScreenFlash", "SmsAssistant", "TextureWireIntervalMinute");
            }

            @Override public boolean showExitInfo() {
                if (PreviewAdManager.getInstance().getNativeAd() != null) {
                    HSLog.i("NotificationMessageAlertActivity", "showExitInfo show ExitNewsActivity ");
                    Navigations.startActivitySafely(HSApplication.getContext(), ExitNewsActivity.class);
                    return true;
                } else {
                    HSLog.w("NotificationMessageAlertActivity", "showExitInfo NOT show, no AD ");
                }
                return false;
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
                Analytics.logEvent("Message_View_AD_Shown");

            }

            @Override
            public void onAdClick() {
//                Analytics.logEvent("Message_View_AD_Clicked");
            }

            @Override
            public void onAdFlurryRecord(boolean isShown) {
                Analytics.logEvent("AcbAdNative_Viewed_In_App", "MsgAssistant", String.valueOf(isShown));
            }

            @Override
            public void messageViewShowed(int msgSrcCount, String flurryMessageType, String flurryAlertShowWhere) {
                Analytics.logEvent("Message_View_Shown", "msgSrcCount", String.valueOf(msgSrcCount),
                        "messageType", flurryMessageType, "AlertShowWhere", flurryAlertShowWhere);

                if (flurryAlertShowWhere.equals("OnLockScreen")) {
                    Analytics.logEvent("Message_View_Shown_OnLockScreen", "messageType", flurryMessageType);
                }
            }

            @Override
            public void messageViewSource(String source) {
                Analytics.logEvent("Message_View_In", "Type", source);
            }


            @Override
            public void onDismiss(NotificationMessageAlertActivity.DismissType type) {
                if (type == NotificationMessageAlertActivity.DismissType.MENU_CLOSE) {
                    Analytics.logEvent("MessageAssistant_Disable", "From", "Popup");
                }
                if (type != NotificationMessageAlertActivity.DismissType.ACTIVITY_DESTROY) {
                    Analytics.logEvent("Message_View_PopUp_Closed", "DismissType", getFormatName(type));
                }
            }

            private String getFormatName(NotificationMessageAlertActivity.DismissType type) {
                if (type == NotificationMessageAlertActivity.DismissType.CLOSE) {
                    return "Close";
                } else if (type == NotificationMessageAlertActivity.DismissType.BACK) {
                    return "Back";
                } else if (type == NotificationMessageAlertActivity.DismissType.CLICK_CONTENT) {
                    return "Content";
                } else if (type == NotificationMessageAlertActivity.DismissType.REPLY) {
                    return "Reply";
                } else {
                    return "Other";
                }
            }

            @Override
            public void onNextClicked(String msgType) {
                Analytics.logEvent("Message_View_NextBtn_Clicked");
            }

            @Override public void onContentClick(String msgType) {
                Analytics.logEvent("Message_View_Alert_Content_Clicked", "MessageType", msgType);
            }

            @Override public void onReplyClicked(String msgType) {
                Analytics.logEvent("Message_View_Alert_Btn_Reply_Clicked","MessageType", msgType);
            }

            @Override public void logEvent(String eventID, String... vars) {
                Analytics.logEvent(eventID, vars);
            }
        };
    }


}
