package com.honeycomb.colorphone;

import com.honeycomb.colorphone.util.LauncherAnalytics;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sundxing on 2017/12/13.
 */

public class PackageList {
    final static Map<String, String> mMap = new HashMap();
    static {
        mMap.put("com.surpax.ledflashlight.panel", "Panel");
        mMap.put("com.honeycomb.launcher", "Launcher");
        mMap.put("com.smart.color.phone.emoji", "Launcher2");
        mMap.put("com.oneapp.max", "Clean");
        mMap.put("com.smartkeyboard.emoji", "Keyboard");
        mMap.put("com.keyboard.colorkeyboard", "Keyboard2");
    }

    public static void checkAndLogPackage(String pkgName) {
        String name = mMap.get(pkgName);
        if (name != null) {
            LauncherAnalytics.logEvent("App_Conflict_Test", "App", name);
        }
    }

}
