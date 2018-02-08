package com.honeycomb.colorphone.recentapp;

import android.content.Intent;

import com.honeycomb.colorphone.boost.AppInfo;

/**
 * Created by sundxing on 2018/2/5.
 */

public class RecentAppInfo {
    public static final int TYPE_NEW_INSTALL = 1;
    public static final int TYPE_RECENTLY_USED = 2;
    public static final int TYPE_MOSTLY_USED = 3;

    private AppInfo mAppInfo;

    private int mType;

    public RecentAppInfo(AppInfo info) {
        mAppInfo = info;
    }

    public RecentAppInfo(AppInfo appInfo, int type) {
        mAppInfo = appInfo;
        mType = type;
    }

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        mType = type;
    }

    public AppInfo getAppInfo() {
        return mAppInfo;
    }

    public void setAppInfo(AppInfo appInfo) {
        mAppInfo = appInfo;
    }

    public String getPackageName() {
        return mAppInfo != null ? mAppInfo.getPackageName() : "";
    }

    public String getName() {
        return mAppInfo != null ? mAppInfo.getName() : "";
    }

    public Intent getIntent() {
        return mAppInfo != null ? mAppInfo.getIntent() : null;
    }
}
