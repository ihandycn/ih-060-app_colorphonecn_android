package com.honeycomb.colorphone.battery;

import android.graphics.drawable.Drawable;

import com.honeycomb.colorphone.util.Utils;

import java.io.Serializable;

public class BatteryAppInfo implements Serializable {
    private Drawable appIcon = null;
    private String packageName;
    private CharSequence appName;
    private double percent;
    private boolean isSystemApp;

    BatteryAppInfo(String packageName) {
        this.packageName = packageName;
        appIcon = packageName == null ? null : Utils.getAppIcon(packageName);
    }

    public void setAppName(CharSequence appName) {
        this.appName = appName;
    }

    public CharSequence getAppName() {
        return appName;
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }

    public double getPercent() {
        return percent;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }

    public Drawable getAppDrawable() {
        return appIcon;
    }

    public void setIsSystemApp(boolean isSystemApp) {
        this.isSystemApp = isSystemApp;
    }

    public boolean getIsSystemApp() {
        return isSystemApp;
    }

}
