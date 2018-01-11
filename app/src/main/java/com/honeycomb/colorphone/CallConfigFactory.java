package com.honeycomb.colorphone;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;

import com.acb.call.CPSettings;
import com.acb.call.customize.AcbCallFactoryImpl;
import com.acb.call.customize.AcbCallManager;
import com.acb.call.customize.ThemeViewConfig;
import com.acb.call.receiver.IncomingCallReceiver;
import com.acb.call.service.InCallWindow;
import com.acb.call.themes.Type;
import com.acb.call.views.CallIdleAlert;
import com.acb.notification.FloatWindowController;
import com.acb.notification.NotificationAccessGuideAlertActivity;
import com.acb.notification.NotificationMessageAlertActivity;
import com.acb.utils.MessageCenterUtils;
import com.acb.utils.NavUtils;
import com.colorphone.lock.util.CommonUtils;
import com.honeycomb.colorphone.contact.ContactManager;
import com.honeycomb.colorphone.notification.NotificationAutoPilotUtils;
import com.honeycomb.colorphone.notification.NotificationConfig;
import com.honeycomb.colorphone.notification.NotificationServiceV18;
import com.honeycomb.colorphone.util.FontUtils;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.honeycomb.colorphone.util.ModuleUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSPreferenceHelper;

import net.appcloudbox.ads.nativeads.AcbNativeAdAnalytics;

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

            }

            public void onAdShow() {
                NotificationAutoPilotUtils.logMessageAssistantAdShow();
                LauncherAnalytics.logEvent("Message_View_AD_Shown", "AlertShowWhere", "NotOnLockScreen", "MessageType", "SMS");
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
//                LauncherAnalytics.logEvent("Message_View_Shown");
            }

            @Override
            public void onAdShow() {
                NotificationAutoPilotUtils.logMessageAssistantAdShow();
                LauncherAnalytics.logEvent("Message_View_AD_Shown");
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
}
