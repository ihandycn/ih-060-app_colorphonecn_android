package com.honeycomb.colorphone;

import android.content.Context;
import android.support.multidex.MultiDex;

import com.appsflyer.AFLogger;
import com.appsflyer.AppsFlyerLib;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.ChannelInfoUtil;
import com.ihs.app.framework.HSApplication;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.beta.interfaces.BetaPatchListener;

import hugo.weaving.DebugLog;

/**
 * @author sundxing
 */
public class ColorPhoneApplication extends HSApplication {

    public static boolean isAppForeground() {
        return ColorPhoneApplicationImpl.isAppForeground();
    }

    public static ConfigLog getConfigLog() {
        return ColorPhoneApplicationImpl.getConfigLog();
    }

    public static boolean isFabricInited() {
        return ColorPhoneApplicationImpl.isFabricInited();
    }

    ColorPhoneApplicationImpl mColorPhoneApplicationProxy;

    @DebugLog
    @Override
    public void onCreate() {
        initAppFlyer();
        super.onCreate();
        Bugly.init(this, getString(R.string.bugly_app_id), BuildConfig.DEBUG);

        mColorPhoneApplicationProxy = new ColorPhoneApplicationImpl(this);
        mColorPhoneApplicationProxy.onCreate();
    }

    private void initAppFlyer() {
        AppsFlyerLib.getInstance().setLogLevel(BuildConfig.DEBUG ? AFLogger.LogLevel.DEBUG : AFLogger.LogLevel.ERROR);
        AppsFlyerLib.getInstance().setDebugLog(BuildConfig.DEBUG);
        AppsFlyerLib.getInstance().setCollectIMEI(true);
        AppsFlyerLib.getInstance().setCollectAndroidID(true);
        AppsFlyerLib.getInstance().setOutOfStore(ChannelInfoUtil.getStore(this));
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
        // 安装tinker
        Beta.betaPatchListener = new BetaPatchListener() {
            @Override
            public void onPatchReceived(String patchFile) {
                Analytics.logEvent("Patch_Received");
            }

            @Override
            public void onDownloadReceived(long savedLength, long totalLength) {
            }

            @Override
            public void onDownloadSuccess(String msg) {
                Analytics.logEvent("Patch_Download_Success");
            }

            @Override
            public void onDownloadFailure(String msg) {
                Analytics.logEvent("Patch_Download_Failed", "Reason", msg);
            }

            @Override
            public void onApplySuccess(String msg) {
                Analytics.logEvent("Patch_Apply_Success");
            }

            @Override
            public void onApplyFailure(String msg) {
                Analytics.logEvent("Patch_Apply_Failed", "Reason", msg);
            }

            @Override
            public void onPatchRollback() {
            }
        };
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
