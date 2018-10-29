package com.honeycomb.colorphone.factoryimpl;

import android.Manifest;
import android.app.Activity;
import android.graphics.Typeface;
import android.os.Build;

import com.acb.call.activity.RequestPermissionsActivity;
import com.acb.call.customize.ThemeViewConfig;
import com.acb.call.receiver.IncomingCallReceiver;
import com.acb.call.themes.Type;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.contact.ContactManager;
import com.honeycomb.colorphone.notification.NotificationServiceV18;
import com.honeycomb.colorphone.util.FontUtils;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.superapps.util.RuntimePermissions;

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

    public String from;
    @Override public RequestPermissionsActivity.Event requestPermissionsEvents() {
        return new RequestPermissionsActivity.Event() {

            @Override
            public void logScreenFlashPhoneContactsAllowClicked() {
                // No use.
                // See #logScreenFlashPhoneAccessRequested
                //     #logScreenFlashContactsAccessRequested
            }

            @Override
            public void logScreenFlashAllOpenDefault(Activity activity) {

            }

            @Override
            public void logScreenFlashAccessPageShowed(String source, String type) {
                if (type.contains(RequestPermissionsActivity.ITEM_DRAW_OVERLAY)) {
                    LauncherAnalytics.logEvent("ColorPhone_Permission_Guide_Overlay_Allow_Click", "type", source);
                }
                if (type.contains(RequestPermissionsActivity.ITEM_NOTIFICATION)) {
                    LauncherAnalytics.logEvent("ColorPhone_Permission_Guide_NotificationAccess_Allow_Click", "type", source);
                }

                if (type.contains(RequestPermissionsActivity.ITEM_PHONE_CONTACT)) {
                    boolean hasPhonePerm = RuntimePermissions.checkSelfPermission(HSApplication.getContext(), Manifest.permission.READ_PHONE_STATE)
                            == RuntimePermissions.PERMISSION_GRANTED;
                    boolean hasContactPerm = RuntimePermissions.checkSelfPermission(HSApplication.getContext(), Manifest.permission.READ_CONTACTS)
                            == RuntimePermissions.PERMISSION_GRANTED;
                    if (!hasContactPerm) {
                        LauncherAnalytics.logEvent("ColorPhone_Permission_Guide_Contact_Allow_Click", "type", source);
                    }
                    if (!hasPhonePerm) {
                        LauncherAnalytics.logEvent("ColorPhone_Permission_Guide_Phone_Allow_Click", "type", source);
                    }

                }
            }

            @Override
            public void logScreenFlashPhoneAccessRequested() {
            }

            @Override
            public void logScreenFlashPhoneAccessSucceed(RequestPermissionsActivity.PermissionSource permissionSource) {
            }

            @Override
            public void logScreenFlashContactsAccessRequested() {
            }

            @Override
            public void logScreenFlashContactsAccessSucceed(RequestPermissionsActivity.PermissionSource permissionSource) {
            }

            @Override
            public void logScreenFlashDrawOverAppsRequested() {
            }

            @Override
            public void logScreenFlashDrawOverAppsSucceeded() {
            }

            @Override
            public void logScreenFlashNotificationAccessRequested() {
                LauncherAnalytics.logEvent("ColorPhone_Permission_Guide_NotificationAccess_Allow_Click");
            }

            @Override
            public void logScreenFlashNotificationAccessSucceed() {
                LauncherAnalytics.logEvent("ColorPhone_Permission_Guide_NotificationAccess_Allow_Success");
            }

            @Override
            public void logScreenFlashAccessAllOpenGuide() {

            }
        };
    }
}
