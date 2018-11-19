package com.acb.colorphone.permissions;

import android.support.annotation.Nullable;

import com.ihs.app.framework.HSApplication;
import com.superapps.util.Permissions;
import com.superapps.util.RuntimePermissions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NormalChecker {

    public static final String PERM_NOTIFICATION = "perms_notification";
    public static final String PERM_OVERLAY = "perms_overlay";

    private List<Module> sModules = new ArrayList<>();

    public NormalChecker(List<Module> sModules) {
        this.sModules = sModules;
    }

    public @Nullable
    Module getModuleByName(String name) {
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
            return Permissions.isNotificationAccessGranted();
        } else if (PERM_OVERLAY.equals(params)) {
            return Permissions.isFloatWindowAllowed(HSApplication.getContext());
        }
        return RuntimePermissions.checkSelfPermission(HSApplication.getContext(), params)
                == RuntimePermissions.PERMISSION_GRANTED;
    }
}
