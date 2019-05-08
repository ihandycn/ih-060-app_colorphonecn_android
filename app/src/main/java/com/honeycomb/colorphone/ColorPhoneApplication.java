package com.honeycomb.colorphone;

import android.content.Context;
import android.support.multidex.MultiDex;


import com.ihs.app.framework.HSApplication;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;

import hugo.weaving.DebugLog;

/**
 * @author sundxing
 */
public class ColorPhoneApplication extends HSApplication {

    public static boolean isAppForeground() {
        return ColorPhoneApplicationImpl.isAppForeground();
    }

    ColorPhoneApplicationImpl mColorPhoneApplicationProxy;

    public static ConfigLog getConfigLog() {
        return ColorPhoneApplicationImpl.getConfigLog();
    }

    public static boolean isFabricInitted() {
        return ColorPhoneApplicationImpl.isFabricInitted();
    }

    @DebugLog
    @Override
    public void onCreate() {
        super.onCreate();
        Bugly.init(this, getString(R.string.bugly_app_id), BuildConfig.DEBUG);
        mColorPhoneApplicationProxy = new ColorPhoneApplicationImpl(this);
        mColorPhoneApplicationProxy.onCreate();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
        // 安装tinker
        Beta.installTinker();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        mColorPhoneApplicationProxy.onTerminate();
    }

    @Override
    protected String getConfigFileName() {
        if (BuildConfig.DEBUG) {
            return "config-d.ya";
        } else {
            return "config-r.ya";
        }
    }

    public void onGdprGranted() {
        mColorPhoneApplicationProxy.initFabric();
    }
}
