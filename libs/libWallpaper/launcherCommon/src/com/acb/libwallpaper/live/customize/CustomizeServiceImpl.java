package com.acb.libwallpaper.live.customize;

import android.os.RemoteException;

import com.acb.libwallpaper.live.customize.util.CustomizeUtils;
import com.ihs.commons.utils.HSPreferenceHelper;

import java.util.List;
import java.util.Map;

public class CustomizeServiceImpl {

    private static final String TAG = CustomizeService.class.getSimpleName();

    public String getCurrentTheme() throws RemoteException {
        return CustomizeUtils.getCurrentThemePackageWithDefault();
    }

    public String getDefaultSharedPreferenceString(String key, String defaultValue) {
        return HSPreferenceHelper.getDefault().getString(key, defaultValue);
    }

    public void preChangeWallpaperFromLauncher() throws RemoteException {
    }

    public void putDefaultSharedPreferenceString(String key, String value) {
        HSPreferenceHelper.getDefault().putString(key, value);
    }

    public void notifyWallpaperFeatureUsed() throws RemoteException {
    }

    public void notifyWallpaperSetEvent() throws RemoteException {
    }

    public List getOnlineWallpaperConfig() {
        return CustomizeConfig.getList("Wallpapers");
    }

    public Map getOnlineThemeConfig() throws RemoteException {
        // This interface shall be never invoked except by old v1.0.0 Pure / Android M / iOS 9 themes
        return CustomizeConfig.getMap("Themes");
    }

    public void logWallpaperEvent(String action, String label) {
        throw new UnsupportedOperationException("logWallpaperEvent() is deprecated. Use HSAnalytics#logEvent() instead.");
    }

    public void killWallpaperProcess() {
        throw new UnsupportedOperationException("killWallpaperProcess() is deprecated. Kill yourself with System.exit().");
    }

    public void notifyWallpaperPackageClicked() throws RemoteException {
    }
}
