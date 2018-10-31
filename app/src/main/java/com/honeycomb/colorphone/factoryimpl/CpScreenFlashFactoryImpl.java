package com.honeycomb.colorphone.factoryimpl;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.text.TextUtils;

import com.acb.call.activity.RequestPermissionsActivity;
import com.acb.call.customize.ThemeViewConfig;
import com.acb.call.receiver.IncomingCallReceiver;
import com.acb.call.themes.Type;
import com.acb.call.utils.PermissionHelper;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.contact.ContactManager;
import com.honeycomb.colorphone.notification.NotificationServiceV18;
import com.honeycomb.colorphone.permission.NotificationGuideActivity;
import com.honeycomb.colorphone.permission.OverlayGuideActivity;
import com.honeycomb.colorphone.permission.PermissionChecker;
import com.honeycomb.colorphone.permission.PermissionUI;
import com.honeycomb.colorphone.util.FontUtils;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.superapps.util.Navigations;
import com.superapps.util.RuntimePermissions;
import com.superapps.util.Threads;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Created by jelly on 2018/3/17.
 */

public class CpScreenFlashFactoryImpl extends com.acb.call.customize.ScreenFlashFactoryImpl {


    @Override
    public boolean isScreenFlashModuleOpenedDefault() {
        return true;
    }

    @Override
    public IncomingCallReceiver.Config getIncomingReceiverConfig() {
        return new IncomingCallReceiver.Config() {
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
    public Class getNotificationServiceClass() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                return NotificationServiceV18.class;
            } else {
                return null;
            }
        } catch (Exception ignore) {
            return null;
        }
    }

    @Override
    public ThemeViewConfig getViewConfig() {
        return new CPViewConfig();
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

    @Override public RequestPermissionsActivity.RequestPermissions requestPermissions() {
        return new RequestPermissionsActivity.RequestPermissions() {
            @Override
            public void onPhonePermissionGranted() {
                com.call.assistant.receiver.IncomingCallReceiver.IncomingCallListener.init();
            }

            @Override
            public void showRequestFloatWindowPermissionGuideDialog(Activity activity) {
                Threads.postOnMainThreadDelayed(() -> {
                    Intent intent = new Intent(activity, OverlayGuideActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    activity.startActivity(intent);
                }, 1000);
            }

            @Override
            public void requestPhoneContactsPermissionInSettings(Activity activity) {
                PermissionUI.tryRequestPermissionFromSystemSettings(activity, true);
                boolean hasPhonePerm = RuntimePermissions.checkSelfPermission(HSApplication.getContext(), Manifest.permission.READ_PHONE_STATE)
                        == RuntimePermissions.PERMISSION_GRANTED;
                boolean hasContactPerm = RuntimePermissions.checkSelfPermission(HSApplication.getContext(), Manifest.permission.READ_CONTACTS)
                        == RuntimePermissions.PERMISSION_GRANTED;
                if (!hasPhonePerm) {
                    LauncherAnalytics.logEvent("Flashlight_Permission_Settings_Phone_View_Showed");
                }
                if (!hasContactPerm) {
                    LauncherAnalytics.logEvent("Flashlight_Permission_Settings_Contact_View_Showed");
                }

            }

            @Override
            public void showRequestNotificationAccessGuideDialog(boolean isOpenSettingsSuccess) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && isOpenSettingsSuccess) {
                    Threads.postOnMainThreadDelayed(() -> {
                        Navigations.startActivity(HSApplication.getContext(), NotificationGuideActivity.class);
                    }, 1000);
                }
            }

            @Override public void showRequestPermissionFailedToast() {
                PermissionUI.showPermissionRequestToast(false);
            }

            @Override public void showRequestPermissionSuccessToast() {
//                PermissionUI.showPermissionRequestToast(true);
            }

        };
    }

    public String from;
    @Override public RequestPermissionsActivity.Event requestPermissionsEvents() {
        return new RequestPermissionsActivity.Event() {
            private String source;
            WeakReference<Activity> mActivityWeakReference;

            @Override
            public void logScreenFlashPhoneContactsAllowClicked() {
                // No use.
                // See #logScreenFlashPhoneAccessRequested
                //     #logScreenFlashContactsAccessRequested
            }

            @Override
            public void logScreenFlashAllOpenDefault(Activity activity) {
                mActivityWeakReference = new WeakReference<>(activity);
            }

            @Override
            public void logScreenFlashAccessPageShowed(String source, String type) {
                this.source = source;
                logPermissionGuideShowEvent(source);
            }

            private void logPermissionGuideShowEvent(String source) {
                Context context = HSApplication.getContext();
                boolean phoneAccessGranted = RuntimePermissions.checkSelfPermission(
                        context, Manifest.permission.READ_PHONE_STATE) >= 0;
                boolean contactsAccessGranted = RuntimePermissions.checkSelfPermission(
                        context, Manifest.permission.READ_CONTACTS) >= 0;

                boolean notificationAccessGranted = PermissionHelper.isNotificationAccessGranted(context);

                logPermissionStatusEvent("ColorPhone_Permission_Guide_Show_From_" + source,
                        phoneAccessGranted, contactsAccessGranted,
                        notificationAccessGranted);
            }

            private void logPermissionStatusEvent(String eventName,
                                                  boolean phoneAccessGranted,
                                                  boolean contactsAccessGranted,
                                                  boolean notificationAccessGranted) {
                StringBuilder permission = new StringBuilder();

                if (phoneAccessGranted) {
                    permission.append("Phone");
                }

                if (contactsAccessGranted) {
                    permission.append("Contact");
                }

                if (notificationAccessGranted) {
                    permission.append("NA");
                }

                if (TextUtils.isEmpty(permission.toString())) {
                    permission.append("None");
                }

                LauncherAnalytics.logEvent(eventName,
                        "type", permission.toString()
                );
            }

            @Override
            public void logScreenFlashPhoneAccessRequested() {
                LauncherAnalytics.logEvent("ColorPhone_Permission_Guide_Phone_Allow_Click", "type", source);
            }

            @Override
            public void logScreenFlashPhoneAccessSucceed(RequestPermissionsActivity.PermissionSource permissionSource) {
                LauncherAnalytics.logEvent("ColorPhone_Permission_Guide_Phone_Allow_Success", "type", source);
                PermissionChecker.onPhonePermissionGranted(mActivityWeakReference != null ? mActivityWeakReference.get() : null);
            }

            @Override
            public void logScreenFlashContactsAccessRequested() {
                LauncherAnalytics.logEvent("ColorPhone_Permission_Guide_Contact_Allow_Click", "type", source);
            }

            @Override
            public void logScreenFlashContactsAccessSucceed(RequestPermissionsActivity.PermissionSource permissionSource) {
                LauncherAnalytics.logEvent("ColorPhone_Permission_Guide_Contact_Allow_Success", "type", source);
            }

            @Override
            public void logScreenFlashDrawOverAppsRequested() {
            }

            @Override
            public void logScreenFlashDrawOverAppsSucceeded() {
            }

            @Override
            public void logScreenFlashNotificationAccessRequested() {
                LauncherAnalytics.logEvent("ColorPhone_Permission_Guide_NotificationAccess_Allow_Click", "type", source);
            }

            @Override
            public void logScreenFlashNotificationAccessSucceed() {
                LauncherAnalytics.logEvent("ColorPhone_Permission_Guide_NotificationAccess_Allow_Success", "type", source);
            }

            @Override
            public void logScreenFlashAccessAllOpenGuide() {

            }
        };
    }

    @Override
    public void logEvent(String eventID, String... vars) {
        LauncherAnalytics.logEvent(eventID, vars);
    }
}
