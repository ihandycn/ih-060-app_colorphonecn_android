package com.acb.colorphone;

import android.support.annotation.DrawableRes;

import com.acb.colorphone.permissions.R;

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
}
