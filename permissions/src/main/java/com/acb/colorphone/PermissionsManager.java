package com.acb.colorphone;

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
}
