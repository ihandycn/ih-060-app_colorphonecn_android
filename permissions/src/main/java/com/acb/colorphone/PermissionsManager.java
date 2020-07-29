package com.acb.colorphone;

import android.support.annotation.DrawableRes;

public class PermissionsManager {

    private PermissionsManager() {
    }

    private static class ClassHolder {
        private static final PermissionsManager INSTANCE = new PermissionsManager();
    }

    public static PermissionsManager getInstance() {
        return ClassHolder.INSTANCE;
    }

    private PermissionsCallback permissionsCallback;

    public void init(PermissionsCallback permissionsCallback) {
        this.permissionsCallback = permissionsCallback;
    }

    public boolean isShowActivityGuide() {
        if (permissionsCallback != null) {
            return permissionsCallback.isShowActivityGuide();
        }
        return false;
    }

    @DrawableRes
    public int getAppIcon() {
        if (permissionsCallback != null) {
            return permissionsCallback.getAppIcon();
        }
        return 0;
    }

    public void logEvent(String eventID, boolean onlyUMENG, String... vars) {
        permissionsCallback.logEvent(eventID, onlyUMENG, vars);
    }
}
