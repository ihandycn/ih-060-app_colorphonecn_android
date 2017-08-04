package com.honeycomb.colorphone;

import android.content.Context;
import android.support.multidex.MultiDex;

import com.acb.call.AcbCallManager;
import com.acb.call.CPSettings;
import com.acb.nativeads.AcbNativeAdManager;
import com.crashlytics.android.Crashlytics;
import com.honeycomb.colorphone.util.HSPermanentUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSNotificationConstant;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.liulishuo.filedownloader.FileDownloader;

import hugo.weaving.DebugLog;
import io.fabric.sdk.android.Fabric;

public class ColorPhoneApplication extends HSApplication {

    private static ConfigLog mConfigLog;

    private INotificationObserver sessionEventObserver = new INotificationObserver() {

        @Override
        public void onReceive(String notificationName, HSBundle bundle) {
            if (HSNotificationConstant.HS_SESSION_START.equals(notificationName)) {
                HSLog.d("Session Start.");
            } else if (HSNotificationConstant.HS_SESSION_END.equals(notificationName)) {
                HSLog.d("Session End.");
            } else if (HSNotificationConstant.HS_CONFIG_CHANGED.equals(notificationName)) {
                checkCallAssistantAdPlacement();
            } else if (CPSettings.NOTIFY_CHANGE_SCREEN_FLASH.equals(notificationName) || CPSettings.NOTIFY_CHANGE_CALL_ASSISTANT.equals(notificationName)) {
                checkCallAssistantAdPlacement();
            }
        }
    };

    @DebugLog
    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        mConfigLog = new ConfigLogDefault();
        AcbCallManager.init(this, new CallConfigFactory());
        HSPermanentUtils.keepAlive();
        FileDownloader.setup(this);
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_SESSION_START, sessionEventObserver);
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_SESSION_END, sessionEventObserver);
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_CONFIG_CHANGED, sessionEventObserver);
        HSGlobalNotificationCenter.addObserver(CPSettings.NOTIFY_CHANGE_CALL_ASSISTANT, sessionEventObserver);
        HSGlobalNotificationCenter.addObserver(CPSettings.NOTIFY_CHANGE_SCREEN_FLASH, sessionEventObserver);

        checkCallAssistantAdPlacement();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        HSGlobalNotificationCenter.removeObserver(sessionEventObserver);
    }


    @Override
    protected String getConfigFileName() {
        if (HSLog.isDebugging()) {
            return "config-d.ya";
        } else {
            return "config-r.ya";
        }
    }

    public static void checkCallAssistantAdPlacement() {
        final String adName = AcbCallManager.getInstance().getAcbCallFactory().getCallIdleConfig().getAdPlaceName();
        boolean enable = CPSettings.isScreenFlashModuleEnabled() && CPSettings.isCallAssistantModuleEnabled();
        if (enable) {
            AcbNativeAdManager.sharedInstance().activePlacementInProcess(adName);
        } else {
            AcbNativeAdManager.sharedInstance().deactivePlacementInProcess(adName);
        }
    }
    public static ConfigLog getConfigLog() {
        return mConfigLog;
    }
}
