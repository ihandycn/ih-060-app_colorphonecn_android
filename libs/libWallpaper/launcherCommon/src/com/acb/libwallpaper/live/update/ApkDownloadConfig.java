package com.acb.libwallpaper.live.update;

import com.acb.libwallpaper.BuildConfig;
import com.acb.libwallpaper.R;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;

public class ApkDownloadConfig {

    private boolean mShowNotification;
    private boolean mShowInDownloadUI;
    private boolean mDownloadOnlyWifi;

    private String mDownloadUrl;

    private String mLocalFileName;
    private CharSequence mTitle;
    private CharSequence mDescription;
    private String md5Key;

    // If true we check version code.
    private boolean mUpdateMode;

    private ApkDownloadConfig(){
      mShowNotification = true;
      mShowInDownloadUI = true;
      mDownloadOnlyWifi = false;
    }

    /**
     *  Url config changed after app update success, so don't config md5 key!
     *
     * @return
     */
    public static ApkDownloadConfig getUpdateConfig() {
        ApkDownloadConfig config = new ApkDownloadConfig();
        config.mDownloadUrl = HSConfig.optString("", "Application", "Update", "DownloadUrl");
        config.mLocalFileName = BuildConfig.APPLICATION_ID + "_update.apk";
        config.mTitle = HSApplication.getContext().getString(R.string.app_name);
        config.mUpdateMode = true;
        return config;
    }

    public boolean isShowNotification() {
        return mShowNotification;
    }

    public boolean isShowInDownloadUI() {
        return mShowInDownloadUI;
    }

    public boolean isDownloadOnlyWifi() {
        return mDownloadOnlyWifi;
    }

    public boolean isUpdateMode() {
        return mUpdateMode;
    }

    public void setDownloadOnlyWifi(boolean downloadOnlyWifi) {
        mDownloadOnlyWifi = downloadOnlyWifi;
    }

    public String getDownloadUrl() {
        return mDownloadUrl;
    }

    public void setDownloadUrl(String mDownloadUrl) {
        this.mDownloadUrl = mDownloadUrl;
    }

    public String getLocalFileName() {
        return mLocalFileName;
    }

    public CharSequence getTitle() {
        return mTitle;
    }

    public CharSequence getDescription() {
        return mDescription;
    }

    @Override
    public String toString() {
        return "downloadUrl:\n" + mDownloadUrl;
    }


    public String getMD5Key() {
        return md5Key;
    }
}
