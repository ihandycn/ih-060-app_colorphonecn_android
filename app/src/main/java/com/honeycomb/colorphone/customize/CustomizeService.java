package com.honeycomb.colorphone.customize;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.colorphone.customize.ICustomizeService;

import java.util.List;
import java.util.Map;

/**
 * Service provided by main process for operations related to wallpaper & themes.
 * This service is used by ":customize" remote process and independent theme packages.
 */
public class CustomizeService extends Service {

    private static final String TAG = CustomizeService.class.getSimpleName();
    private Handler mMainHandler= new Handler(Looper.getMainLooper());

    private final ICustomizeService.Stub mBinder = new ICustomizeService.Stub() {
        private CustomizeServiceImpl mImpl = new CustomizeServiceImpl();

        @Override
        public String getCurrentTheme() throws RemoteException {
            return mImpl.getCurrentTheme();
        }

        @Override
        public void setCurrentTheme(final String themePackage) throws RemoteException {
            mImpl.setCurrentTheme(CustomizeService.this, themePackage);
        }

        /**
         * Caution: this called not main thread!
         * @param packageName
         */
        @Override
        public long browseMarketApp(String packageName) {
            return mImpl.browseMarketApp(packageName);
        }

        @Override
        public String getDefaultSharedPreferenceString(String key, String defaultValue) {
            return mImpl.getDefaultSharedPreferenceString(key, defaultValue);
        }

        @Override
        public void preChangeWallpaperFromLauncher() throws RemoteException {
            mImpl.preChangeWallpaperFromLauncher();
        }

        @Override
        public void putDefaultSharedPreferenceString(String key, String value) {
            mImpl.putDefaultSharedPreferenceString(key, value);
        }

        @Override
        public void notifyWallpaperFeatureUsed() throws RemoteException {
            mImpl.notifyWallpaperFeatureUsed();
        }

        @Override
        public void notifyWallpaperSetEvent() throws RemoteException {
            mImpl.notifyWallpaperSetEvent();
        }

        @Override
        @Deprecated
        public List getOnlineWallpaperConfig() {
            return mImpl.getOnlineWallpaperConfig();
        }

        @Override
        @Deprecated
        public Map getOnlineThemeConfig() throws RemoteException {
            return mImpl.getOnlineThemeConfig();
        }

        @Override
        @Deprecated
        public void logWallpaperEvent(String action, String label) {
            mImpl.logWallpaperEvent(action, label);
        }

        @Override
        @Deprecated
        public void killWallpaperProcess() {
            mImpl.killWallpaperProcess();
        }

        @Override
        public void notifyWallpaperPackageClicked() throws RemoteException {
            mImpl.notifyWallpaperPackageClicked();
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        mMainHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
