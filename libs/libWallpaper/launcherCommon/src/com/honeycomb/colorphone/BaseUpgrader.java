package com.honeycomb.colorphone;

import android.content.Context;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;

import java.io.File;

/**
 * Handle launcher app upgrade on main process.
 */
abstract class BaseUpgrader {

    protected static final String TAG = "Upgrader";

    protected Context mContext;

    BaseUpgrader(Context context) {
        mContext = context;
    }

    public void upgrade() {
        HSApplication.HSLaunchInfo lastLaunch = HSApplication.getLastLaunchInfo();
        int newVersion = HSApplication.getCurrentLaunchInfo().appVersionCode;
        int oldVersion = lastLaunch == null ? newVersion : lastLaunch.appVersionCode;

        HSLog.i(TAG, "Old version: " + oldVersion + ", new version: " + newVersion);
        if (oldVersion != newVersion) {
            onAppUpgrade(oldVersion, newVersion);
        }
    }

    protected abstract void onAppUpgrade(final int oldVersion, final int newVersion);

    boolean renameFile(File src, File des) {
        boolean moveSuccess = false;
        if (src.exists()) {
            moveSuccess = src.renameTo(des);
        }
        return moveSuccess;
    }

    void migrateBoolean(HSPreferenceHelper fromPrefs, HSPreferenceHelper toPrefs, String key) {
        if (fromPrefs.contains(key)) {
            toPrefs.putBoolean(key, fromPrefs.getBoolean(key, true));
            fromPrefs.remove(key);
        }
    }

    void migrateInt(HSPreferenceHelper fromPrefs, HSPreferenceHelper toPrefs, String key) {
        if (fromPrefs.contains(key)) {
            toPrefs.putInt(key, fromPrefs.getInt(key, 0));
            fromPrefs.remove(key);
        }
    }

    void migrateLong(HSPreferenceHelper fromPrefs, HSPreferenceHelper toPrefs, String key) {
        if (fromPrefs.contains(key)) {
            toPrefs.putLong(key, fromPrefs.getLong(key, 0));
            fromPrefs.remove(key);
        }
    }

    void migrateFloat(HSPreferenceHelper fromPrefs, HSPreferenceHelper toPrefs, String key) {
        if (fromPrefs.contains(key)) {
            toPrefs.putFloat(key, fromPrefs.getFloat(key, 0f));
            fromPrefs.remove(key);
        }
    }

    void migrateString(HSPreferenceHelper fromPrefs, HSPreferenceHelper toPrefs, String key) {
        if (fromPrefs.contains(key)) {
            toPrefs.putString(key, fromPrefs.getString(key, ""));
            fromPrefs.remove(key);
        }
    }
}
