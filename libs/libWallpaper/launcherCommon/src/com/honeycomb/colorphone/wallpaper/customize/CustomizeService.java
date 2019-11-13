package com.honeycomb.colorphone.wallpaper.customize;

import android.os.RemoteException;


import java.util.List;
import java.util.Map;

/**
 * Service provided by main process for operations related to wallpaper & themes.
 * This service is used by ":customize" remote process and independent theme packages.
 */
public class CustomizeService {


        private CustomizeServiceImpl mImpl = new CustomizeServiceImpl();


        public String getDefaultSharedPreferenceString(String key, String defaultValue) {
            return mImpl.getDefaultSharedPreferenceString(key, defaultValue);
        }

        public void preChangeWallpaperFromLauncher() throws RemoteException {
            mImpl.preChangeWallpaperFromLauncher();
        }

        public void putDefaultSharedPreferenceString(String key, String value) {
            mImpl.putDefaultSharedPreferenceString(key, value);
        }

        public void notifyWallpaperFeatureUsed() throws RemoteException {
            mImpl.notifyWallpaperFeatureUsed();
        }

        public void notifyWallpaperSetEvent() throws RemoteException {
            mImpl.notifyWallpaperSetEvent();
        }

        @Deprecated
        public List getOnlineWallpaperConfig() {
            return mImpl.getOnlineWallpaperConfig();
        }

        @Deprecated
        public Map getOnlineThemeConfig() throws RemoteException {
            return mImpl.getOnlineThemeConfig();
        }

        @Deprecated
        public void logWallpaperEvent(String action, String label) {
            mImpl.logWallpaperEvent(action, label);
        }

        @Deprecated
        public void killWallpaperProcess() {
            mImpl.killWallpaperProcess();
        }

        public void notifyWallpaperPackageClicked() throws RemoteException {
            mImpl.notifyWallpaperPackageClicked();
        }

}
