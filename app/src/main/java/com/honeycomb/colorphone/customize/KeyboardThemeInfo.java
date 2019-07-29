package com.honeycomb.colorphone.customize;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.crashlytics.android.core.CrashlyticsCore;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Packages;

import java.util.Map;

public class KeyboardThemeInfo {

    public String packageName;
    public String displayName;

    public String bannerImgUrl;
    public String mediumPreviewUrl;
    public String largePreviewUrl;

    public boolean installed;

    public static @Nullable
    KeyboardThemeInfo ofConfig(Map<String, ?> config) {
        KeyboardThemeInfo info = new KeyboardThemeInfo();

        try {
            String packageName = (String) config.get("themePkName");
            if (TextUtils.isEmpty(packageName)) {
                return null;
            }
            info.packageName = packageName;

            String displayName = (String) config.get("showName");
            if (TextUtils.isEmpty(displayName)) {
                return null;
            }
            info.displayName = displayName;

            String mediumPreviewUrl = (String) config.get("mediumPreviewUrl");
            if (TextUtils.isEmpty(mediumPreviewUrl)) {
                return null;
            }
            info.mediumPreviewUrl = mediumPreviewUrl;

            info.bannerImgUrl = (String) config.get("bannerImgUrl");
            info.largePreviewUrl = (String) config.get("largePreviewUrl");

            info.installed = Packages.isPackageInstalled(packageName);
        } catch (Exception e) {
            HSLog.w("Theme.Keyboard", "Error loading keyboard theme config, please check config format");
            CrashlyticsCore.getInstance().logException(e);
            return null;
        }

        return info;
    }

    @Override
    public String toString() {
        return "Keyboard theme: " + displayName + ", package: " + packageName;
    }
}
