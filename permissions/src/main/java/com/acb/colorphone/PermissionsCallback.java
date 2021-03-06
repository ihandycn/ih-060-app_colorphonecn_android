package com.acb.colorphone;

import android.support.annotation.DrawableRes;

public interface PermissionsCallback {

    boolean isShowActivityGuide();

    @DrawableRes
    int getAppIcon();

    void logEvent(String eventID, boolean onlyUMENG, String... vars);
}
