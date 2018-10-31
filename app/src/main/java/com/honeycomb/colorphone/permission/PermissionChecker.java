package com.honeycomb.colorphone.permission;

import android.Manifest;
import android.app.Activity;
import android.os.Build;
import android.support.annotation.Nullable;

import com.acb.call.activity.RequestPermissionsActivity;
import com.acb.call.customize.ScreenFlashManager;
import com.acb.call.customize.ScreenFlashSettings;
import com.acb.call.utils.PermissionHelper;
import com.honeycomb.colorphone.Constants;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.superapps.util.Preferences;
import com.superapps.util.RuntimePermissions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PermissionChecker {

    private static final PermissionChecker INSTANCE = new PermissionChecker();

    public static PermissionChecker getInstance() {
        return INSTANCE;
    }

    /**
     * Special
     */
    public static final String ScreenFlash = "ScreenFlash";
    public static final String CallAssistant = "CallAssistant";
    public static final String MessageAssistant = "MessageAssistant";
    public static final String CallReminder = "CallReminder";
    public static final String SmsReminder = "SmsReminder";

    public static final String PERM_NOTIFICATION = "perms_notification";
    public static final String PERM_OVERLAY = "perms_overlay";

    static List<Module> sModules = new ArrayList<>();

    public static String[] sDefaultRequestPermissions = new String[] {
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CONTACTS,
            RequestPermissionsActivity.PERMISSION_NOTIFICATION
    };

    public static String[] customPhonePermission = new String[] {
            Manifest.permission.CALL_PHONE, // reject call
            Manifest.permission.ANSWER_PHONE_CALLS, // answer call
    };

    public static void onPhonePermissionGranted(Activity activity) {
        if (activity != null) {
            String[] perms = isAtleastO() ? customPhonePermission : new String[]{Manifest.permission.CALL_PHONE};
            RuntimePermissions.requestPermissions(activity, perms, 1000);
        }
        com.call.assistant.receiver.IncomingCallReceiver.IncomingCallListener.init();
        com.acb.call.receiver.IncomingCallReceiver.IncomingCallListener.init();
    }

    public void check(Activity activity, String source) {
        if (Build.VERSION.SDK_INT >= 16 && hasNoGrantedPermissions(ScreenFlash)) {
            final ArrayList<String> permissions = new ArrayList<>(Arrays.asList(sDefaultRequestPermissions));
            RequestPermissionsActivity.start(activity, source, permissions);
        }
    }

    private static boolean isAtleastO() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    public abstract static class Module {
        String name;
        String[] needPermissions;
        public Module(String name, String[] needPermissions) {
            this.name = name;
            this.needPermissions = needPermissions;
        }

        public abstract boolean enabled();
    }

    static {
        sModules.add(new Module(ScreenFlash, new String[]{Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CONTACTS,
                PERM_NOTIFICATION}) {
            @Override
            public boolean enabled() {
                return ScreenFlashManager.getInstance().getAcbCallFactory().isConfigEnabled()
                        && ScreenFlashSettings.isScreenFlashModuleEnabled();
            }
        });
        sModules.add(new Module(CallReminder, new String[]{Manifest.permission.READ_PHONE_STATE}) {
            @Override
            public boolean enabled() {
                return  Preferences.get(Constants.DESKTOP_PREFS).getBoolean(Constants.PREFS_LED_FLASH_ENABLE,
                        HSConfig.optBoolean(false, "Application", "LEDReminder", "DefaultSwitch"));
            }
        });
    }

    public @Nullable Module getModuleByName(String name) {
        for (Module module : sModules) {
            if (module.name.equals(name)) {
                return module;
            }
        }
        return null;
    }

    public boolean hasNoGrantedPermissions(String moduleName) {
        List<String> list = getNoGrantedPermissionsForModule(moduleName);
        return !list.isEmpty();
    }

    public List<String> getAllNoGrantedPermissions() {
        List<String> noGrantedPermissions =  new ArrayList<>();
        List<String> neededPermissions = getAllNeededPermissions();
        for (String perm : neededPermissions) {
            if (!hasPermission(perm)) {
                noGrantedPermissions.add(perm);
            }
        }
        return noGrantedPermissions;
    }

    public List<String> getNoGrantedPermissionsForModule(String name) {
        Module module = getModuleByName(name);
        return getNoGrantedPermissions(module.needPermissions);
    }

    public List<String> getNoGrantedPermissions(String... list) {
        List<String> noGrantedPermissions = new ArrayList<>();
        for (String perm : list) {
            if (!hasPermission(perm)) {
                noGrantedPermissions.add(perm);
            }
        }
        return noGrantedPermissions;
    }
    public List<String> getAllNeededPermissions() {
        List<String> result = new ArrayList<>();
        for (Module module : sModules) {
            if (module.enabled()) {
                result.addAll(Arrays.asList(module.needPermissions));
            }
        }
        return result;
    }

    public static boolean hasPermission(String params) {
        if (PERM_NOTIFICATION.equals(params)) {
            return PermissionHelper.isNotificationAccessGranted(HSApplication.getContext());
        } else if (PERM_OVERLAY.equals(params)) {
            return PermissionHelper.isDrawOverlayGranted();
        }
        return RuntimePermissions.checkSelfPermission(HSApplication.getContext(), params)
                == RuntimePermissions.PERMISSION_GRANTED;
    }

}
