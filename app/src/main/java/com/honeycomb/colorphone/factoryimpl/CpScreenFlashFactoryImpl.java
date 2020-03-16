package com.honeycomb.colorphone.factoryimpl;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import com.acb.call.RequestCallerAddressListener;
import com.acb.call.activity.RequestPermissionsActivity;
import com.acb.call.customize.ScreenFlashManager;
import com.acb.call.customize.ThemeViewConfig;
import com.acb.call.themes.Type;
import com.acb.call.utils.PermissionHelper;
import com.acb.colorphone.permissions.AutoStartGuideActivity;
import com.acb.colorphone.permissions.AutoStartHuaweiGuideActivity;
import com.acb.colorphone.permissions.AutoStartMIUIGuideActivity;
import com.acb.colorphone.permissions.NotificationGuideActivity;
import com.acb.colorphone.permissions.NotificationMIUIGuideActivity;
import com.acb.colorphone.permissions.OverlayGuideActivity;
import com.acb.colorphone.permissions.PermissionUI;
import com.acb.colorphone.permissions.ShowOnLockScreenGuideActivity;
import com.acb.colorphone.permissions.ShowOnLockScreenMIUIGuideActivity;
import com.colorphone.lock.lockscreen.locker.Locker;
import com.honeycomb.colorphone.Ap;
import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.contact.ContactManager;
import com.honeycomb.colorphone.http.HttpManager;
import com.honeycomb.colorphone.http.lib.call.Callback;
import com.honeycomb.colorphone.notification.NotificationServiceV18;
import com.honeycomb.colorphone.permission.PermissionChecker;
import com.honeycomb.colorphone.theme.RandomTheme;
import com.honeycomb.colorphone.theme.ThemeApplyManager;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.PermissionTestUtils;
import com.honeycomb.colorphone.util.StringUtils;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Navigations;
import com.superapps.util.Preferences;
import com.superapps.util.RuntimePermissions;
import com.superapps.util.Threads;
import com.superapps.util.rom.RomUtils;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import okhttp3.ResponseBody;

/**
 * Created by jelly on 2018/3/17.
 */

public class CpScreenFlashFactoryImpl extends com.acb.call.customize.ScreenFlashFactoryImpl {

    private boolean isScreenFlashShow = true;

    @Override
    public boolean isScreenFlashModuleOpenedDefault() {
        return true;
    }

    @Override
    public ScreenFlashManager.Config getIncomingReceiverConfig() {
        return new ScreenFlashManager.Config() {
            @Override
            public int getThemeIdByPhoneNumber(String number) {
                int themeId = ContactManager.getInstance().getThemeIdByNumber(number);
                if (themeId > 0) {
                    return themeId;
                } else {
                    themeId = super.getThemeIdByPhoneNumber(number);
                    if (themeId == Theme.RANDOM_THEME) {
                        Theme theme = RandomTheme.getInstance().getRealTheme();
                        if (theme != null) {
                            return theme.getId();
                        }
                        HSLog.e("RandomTheme no theme ready");
//                        Analytics.logEvent("RandomThemeNone");
                    }
                    return themeId;
                }
            }
        };
    }

    @Override
    public void onCallFinish(String number) {
        super.onCallFinish(number);
    }

    @Override
    public int getDefaultThemeId() {
        return Utils.getDefaultThemeId();
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
        public void onConfigTypes(List<Type> types) {
            Iterator<Type> iter = types.iterator();
            while (iter.hasNext()) {
                Type t = iter.next();
                if (t instanceof Theme) {
                    ((Theme) t).configAvatar();
                }
            }
        }

        @Override
        public float getTitleSize() {
            return 24;
        }

        @Override
        public float getSecondTitleSize() {
            return 16;
        }
    }

    @Override
    public RequestPermissionsActivity.RequestPermissions requestPermissions() {
        return new RequestPermissionsActivity.RequestPermissions() {
            @Override
            public void onPhonePermissionGranted() {
                PermissionChecker.onPhonePermissionGranted();
            }

            @Override
            public void onContactsPermissionGranted() {

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
                    Analytics.logEvent("Permission_Settings_Phone_View_Showed");
                }
                if (!hasContactPerm) {
                    Analytics.logEvent("Permission_Settings_Contact_View_Showed");
                }

            }

            @Override
            public void showRequestNotificationAccessGuideDialog(boolean isOpenSettingsSuccess) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && isOpenSettingsSuccess) {
                    Threads.postOnMainThreadDelayed(() -> {
                        if (RomUtils.checkIsMiuiRom() || RomUtils.checkIsHuaweiRom()) {
                            Navigations.startActivitySafely(HSApplication.getContext(), NotificationMIUIGuideActivity.class);
                        } else {
                            Navigations.startActivitySafely(HSApplication.getContext(), NotificationGuideActivity.class);
                        }
                    }, 1000);
                }
            }

            @Override
            public void showRequestAutoStartAccessGuideDialog(boolean isOpenSettingsSuccess) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && isOpenSettingsSuccess) {
                    Threads.postOnMainThreadDelayed(() -> {
                        if (RomUtils.checkIsMiuiRom()) {
                            Navigations.startActivitySafely(HSApplication.getContext(), AutoStartMIUIGuideActivity.class);
                        } else if (RomUtils.checkIsHuaweiRom()) {
                            Navigations.startActivitySafely(HSApplication.getContext(), AutoStartHuaweiGuideActivity.class);
                        } else {
                            Navigations.startActivitySafely(HSApplication.getContext(), AutoStartGuideActivity.class);
                        }
                    }, 1000);
                }
            }

            @Override
            public void showRequestNotificationOnLockScreenGuideDialog(boolean isOpenSettingsSuccess) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && isOpenSettingsSuccess) {
                    Threads.postOnMainThreadDelayed(() -> {
                        if (RomUtils.checkIsMiuiRom()) {
                            Navigations.startActivitySafely(HSApplication.getContext(), ShowOnLockScreenMIUIGuideActivity.class);
                        } else {
                            Navigations.startActivitySafely(HSApplication.getContext(), ShowOnLockScreenGuideActivity.class);
                        }
                    }, 1000);
                }
            }

            @Override
            public void showRequestPermissionFailedToast() {
                PermissionUI.showPermissionRequestToast(false);
            }

            @Override
            public void showRequestPermissionSuccessToast() {
//                PermissionUI.showPermissionRequestToast(true);
            }
        };
    }

    public String from;

    @Override
    public RequestPermissionsActivity.Event requestPermissionsEvents() {
        return new RequestPermissionsActivity.Event() {
            private int launchTime;
            private int confirmShowTime;
            private String source;
            WeakReference<Activity> mActivityWeakReference;

            @Override
            public void logScreenFlashPhoneContactsAllowClicked() {
                // No use.
                // See #logScreenFlashPhoneAccessRequested
                //     #logScreenFlashContactsAccessRequested
//                if (PermissionTestUtils.getAlertStyle()) {
//                    Analytics.logEvent("ColorPhone_PermissionGuide_View_Click_New");
//                }
            }

            @Override
            public void logScreenFlashAllOpenDefault(Activity activity) {
                mActivityWeakReference = new WeakReference<>(activity);
            }

            @Override
            public void logScreenFlashAccessPageShowed(String source, String type) {
                this.source = source;
                launchTime = Preferences.get(Constants.DESKTOP_PREFS).incrementAndGetInt("PermissionGuideShow");

                logPermissionGuideShowEvent("Permission_Guide_Show_From_" + source);

//                if (PermissionTestUtils.getAlertStyle()) {
//                    Analytics.logEvent("ColorPhone_PermissionGuide_View_show_New");
//                }

//                switch (launchTime) {
//                    case 1:
//                        PermissionTestUtils.logPermissionEvent("ColorPhone_PermissionGuide_show_Firsttime", true);
//                        break;
//                    case 2:
//                        PermissionTestUtils.logPermissionEvent("ColorPhone_PermissionGuide_show_Secondtime", true);
//                        break;
//                    case 3:
//                        PermissionTestUtils.logPermissionEvent("ColorPhone_PermissionGuide_show_Thirdtime", true);
//                        break;
//                }
            }

            private void logPermissionGuideShowEvent(String eventName) {
                Context context = HSApplication.getContext();
                boolean phoneAccessGranted = RuntimePermissions.checkSelfPermission(
                        context, Manifest.permission.READ_PHONE_STATE) >= 0;
                boolean contactsAccessGranted = RuntimePermissions.checkSelfPermission(
                        context, Manifest.permission.READ_CONTACTS) >= 0;

                boolean notificationAccessGranted = PermissionHelper.isNotificationAccessGranted(context);

                logPermissionStatusEvent(eventName,
                        phoneAccessGranted, contactsAccessGranted,
                        notificationAccessGranted);
            }

            private void logPermissionStatusEvent(String eventName,
                                                  boolean phoneAccessGranted,
                                                  boolean contactsAccessGranted,
                                                  boolean notificationAccessGranted) {
                StringBuilder permission = new StringBuilder();

                if (!phoneAccessGranted) {
                    permission.append("Phone");
                }

                if (!contactsAccessGranted) {
                    permission.append("Contact");
                }

                if (!notificationAccessGranted) {
                    permission.append("NA");
                }

                if (TextUtils.isEmpty(permission.toString())) {
                    permission.append("None");
                }

                Analytics.logEvent(eventName,
                        "type", permission.toString()
                );
            }

            @Override
            public void logScreenFlashPhoneAccessRequested() {
                if (PermissionTestUtils.functionVersion()) {
                    Analytics.logEvent("Permission_Guide_Phone_Allow_Click_new",
                            "type", source, "from", String.valueOf(launchTime));
                }
//                PermissionTestUtils.logPermissionEvent("colorphone_permissionguide_phone_view_show");
            }

            @Override
            public void logScreenFlashPhoneAccessSucceed(RequestPermissionsActivity.PermissionSource permissionSource) {
//                PermissionTestUtils.logPermissionEvent("colorphone_permissionguide_phone_allow_success");
            }

            @Override
            public void logScreenFlashContactsAccessRequested() {
                Analytics.logEvent("Permission_Guide_Contact_Allow_Click",
                        "type", source, "from", String.valueOf(launchTime));
                if (PermissionTestUtils.functionVersion()) {
                    Analytics.logEvent("Permission_Guide_Contact_Allow_Click_new",
                            "type", source, "from", String.valueOf(launchTime));
                }
//                PermissionTestUtils.logPermissionEvent("colorphone_permissionguide_contact_view_show");
            }

            @Override
            public void logScreenFlashContactsAccessSucceed(RequestPermissionsActivity.PermissionSource permissionSource) {
                Analytics.logEvent("Permission_Guide_Contact_Allow_Success",
                        "type", source, "from", String.valueOf(launchTime));
                if (PermissionTestUtils.functionVersion()) {
                    Analytics.logEvent("Permission_Guide_Contact_Allow_Success_new",
                            "type", source, "from", String.valueOf(launchTime));
                }
                PermissionChecker.onContactPermissionGranted();
//                PermissionTestUtils.logPermissionEvent("colorphone_permissionguide_contact_allow_success");
            }

            @Override
            public void logScreenFlashDrawOverAppsRequested() {
            }

            @Override
            public void logScreenFlashDrawOverAppsSucceeded() {
            }

            @Override
            public void logScreenFlashNotificationAccessRequested() {
                Analytics.logEvent("Permission_Guide_NA_Allow_Click",
                        "type", source, "from", String.valueOf(launchTime));
//                PermissionTestUtils.logPermissionEvent("colorphone_permissionguide_notificationaccess_view_show");
            }

            @Override
            public void logScreenFlashNotificationAccessSucceed() {
//                PermissionTestUtils.logPermissionEvent("colorphone_permissionguide_notificationaccess_allow_success");
            }

            @Override
            public void logScreenFlashAccessAllOpenGuide() {

            }

            @Override
            public void logConfirmAlertEvent(String eventID) {
                if ("AutoStartAlert_Show".equalsIgnoreCase(eventID)) {
                    confirmShowTime = Preferences.get(Constants.DESKTOP_PREFS).incrementAndGetInt("PermissionConfirmAutoStartShow");
                } else if ("LockScreenAlert_Show".equalsIgnoreCase(eventID)) {
                    confirmShowTime = Preferences.get(Constants.DESKTOP_PREFS).incrementAndGetInt("PermissionConfirmShowOnLockScreenShow");
                } else if ("LockScreenAlert_Show_Outside_App".equalsIgnoreCase(eventID)) {
                    confirmShowTime = Preferences.get(Constants.DESKTOP_PREFS).incrementAndGetInt("PermissionConfirmShowOnLockScreenOutSideShow");
                }

                logAlertShow(eventID);
            }

            private void logAlertShow(String eventID) {
                String param;
                switch (confirmShowTime) {
                    case 1:
                        param = "FirstTime";
                        break;
                    case 2:
                        param = "SecondTime";
                        break;
                    case 3:
                        param = "ThirdTime";
                        break;
                    default:
                        return;
                }

                Analytics.logEvent(eventID,
                        "from", param,
                        "Brand", Build.BRAND.toLowerCase(),
                        "DeviceVersion", Locker.getDeviceInfo());
            }

            @Override
            public void onClose() {

            }
        };
    }

    @Override
    public void logEvent(boolean onlyUMENG, String eventID, String... vars) {
        // Umeng-event format.
        eventID = eventID.replace("Acb_ScreenFlash_AcceptFail_TimeOut_2s", "SF_AcceptFail_TimeOut_2s");
        eventID = eventID.replace("Acb_ScreenFlash_RejectCallTimeout_2s", "SF_RejectCallTimeout_2s");
        if ("Acb_ScreenFlash_Accept_Notification".equalsIgnoreCase(eventID)) {
            String[] expandVars = new String[vars.length + 2];
            System.arraycopy(vars, 0, expandVars, 0, vars.length);
            expandVars[vars.length] = "NaService";
            expandVars[vars.length + 1] = NotificationServiceV18.inServiceRunning + "";
            Analytics.logEvent(eventID, onlyUMENG, expandVars);
        } else {
            Analytics.logEvent(eventID, onlyUMENG, vars);
        }

        if ("Acb_Screenflash_Shouldshow".equalsIgnoreCase(eventID)) {
            isScreenFlashShow = false;
        } else if ("Acb_Screenflash_Show".equalsIgnoreCase(eventID)) {
            isScreenFlashShow = true;
        } else if ("Acb_Screenflash_DisplayFail".equalsIgnoreCase(eventID)) {
            isScreenFlashShow = false;
        }
    }

    public boolean isScreenFlashNotShown() {
        boolean ret = !isScreenFlashShow;
        isScreenFlashShow = true;
        return ret;
    }

    @Override
    public Type getType(int themeId) {
        return ThemeApplyManager.getInstance().getAppliedThemeByThemeId(themeId);
    }

    @Override
    public void addAppliedType(Type type) {
        if (type instanceof Theme) {
            ThemeApplyManager.getInstance().addAppliedTheme(((Theme) type).toPrefString());
        } else {
            ThemeApplyManager.getInstance().addAppliedTheme(type.toPrefTypeString());
        }
    }

    @Override
    public void getCallAddress(String number, RequestCallerAddressListener callerAddressListener) {
        HttpManager.getInstance().getCallerAddressInfo(number, new Callback<ResponseBody>() {
            @Override
            public void onFailure(String errorMsg) {
                callerAddressListener.fail();
                Analytics.logEvent("Acb_Screenflash_Show_Location_Details", "withlocation", "false");
            }

            @Override
            public void onSuccess(ResponseBody responseBody) {
                String string = "";
                String address = "";
                String province;
                String city;
                String operator;
                try {
                    string = responseBody.string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (!TextUtils.isEmpty(string)) {
                    province = StringUtils.getProvince(string);
                    city = StringUtils.getCity(string);
                    operator = StringUtils.getOperator(string);

                    if (!TextUtils.isEmpty(province)) {
                        if (province.equals(city)) {
                            province = "";
                        }

                        address = province + " " + city + " " + operator;
                    }

                }
                callerAddressListener.success(address);
                if (TextUtils.isEmpty(address)) {
                    Analytics.logEvent("Acb_Screenflash_Show_Location_Details", "withlocation", "false");
                } else {
                    Analytics.logEvent("Acb_Screenflash_Show_Location_Details", "withlocation", "true");
                }
            }
        });

    }
}
