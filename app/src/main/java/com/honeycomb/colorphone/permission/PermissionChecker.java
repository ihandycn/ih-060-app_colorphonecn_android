package com.honeycomb.colorphone.permission;

import android.Manifest;
import android.app.Activity;
import android.os.Build;
import android.support.annotation.Nullable;

import com.acb.call.activity.RequestPermissionsActivity;
import com.acb.call.customize.ScreenFlashManager;
import com.acb.call.customize.ScreenFlashSettings;
import com.acb.colorphone.permissions.Module;
import com.acb.colorphone.permissions.NormalChecker;
import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.contact.ContactManager;
import com.ihs.commons.config.HSConfig;
import com.superapps.util.Preferences;
import com.superapps.util.RuntimePermissions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.acb.colorphone.permissions.NormalChecker.PERM_NOTIFICATION;

public class PermissionChecker {

    private static PermissionChecker INSTANCE;


    /**
     * Special
     */
    public static final String ScreenFlash = "ScreenFlash";
    public static final String CallAssistant = "CallAssistant";
    public static final String MessageAssistant = "MessageAssistant";
    public static final String CallReminder = "CallReminder";
    public static final String SmsReminder = "SmsReminder";

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

    public static void onContactPermissionGranted(Activity activity) {
        ContactManager.getInstance().update();
    }

    public static PermissionChecker getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PermissionChecker();
        }
        return INSTANCE;
    }

    private NormalChecker mNormalChecker;
    private PermissionChecker() {
        mNormalChecker = new NormalChecker(sModules);
    }

    public void checkForcely(Activity activity, String source) {
        if (Build.VERSION.SDK_INT >= 16 && hasNoGrantedPermissions(ScreenFlash)) {
            final ArrayList<String> permissions = new ArrayList<>(Arrays.asList(sDefaultRequestPermissions));
            RequestPermissionsActivity.start(activity, source, permissions);
        }
    }

    public void check(Activity activity, String source) {
        int limitTime = HSConfig.optInteger(3, "Application", "PermissionGuideTime");
        Preferences.get(Constants.DESKTOP_PREFS).doLimitedTimes(new Runnable() {
            @Override
            public void run() {
               checkForcely(activity, source);
            }
        }, "request_colorflash_permission", limitTime);

    }

    private static boolean isAtleastO() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
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
        return mNormalChecker.getModuleByName(name);
    }

    public boolean hasNoGrantedPermissions(String moduleName) {
        List<String> list = mNormalChecker.getNoGrantedPermissionsForModule(moduleName);
        return !list.isEmpty();
    }
}
