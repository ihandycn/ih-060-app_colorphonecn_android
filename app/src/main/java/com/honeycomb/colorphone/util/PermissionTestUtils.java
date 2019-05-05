package com.honeycomb.colorphone.util;

import com.ihs.app.framework.HSApplication;

public class PermissionTestUtils {

    public static boolean functionVersion() {
        return HSApplication.getFirstLaunchInfo().appVersionCode >= 43;
    }
}
