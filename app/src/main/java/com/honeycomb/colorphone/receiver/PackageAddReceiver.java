package com.honeycomb.colorphone.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.honeycomb.colorphone.activity.PromoteLockerActivity;
import com.honeycomb.colorphone.boost.SystemAppsManager;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.AvatarAutoPilotUtils;
import com.honeycomb.colorphone.util.PromoteLockerAutoPilotUtils;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;


/**
 * Created by jelly on 2017/12/18.
 */

public class PackageAddReceiver extends BroadcastReceiver {

    private static final int INSTALL_NOT_BY_PROMOTE = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())
                || Intent.ACTION_PACKAGE_INSTALL.equals(intent.getAction())) {
            final String pkgAdd = intent.getData().getSchemeSpecificPart();
            HSLog.d("PackageAddReceiver", "Pkg add :" + pkgAdd);
            String pkgName = PromoteLockerAutoPilotUtils.getPromoteLockerApp();
            Threads.postOnMainThread(new Runnable() {
                @Override
                public void run() {
                    SystemAppsManager.getInstance().addPackage(pkgAdd);
                }
            });
            HSLog.d("PackageAddReceiver", "Pkg local read :" + pkgName);

            if (TextUtils.equals(pkgName, pkgAdd)) {
                int promoteLockerAlertType = Preferences.get(PromoteLockerActivity.PREFS_FILE).getInt(PromoteLockerActivity.ALERT_TYPE, INSTALL_NOT_BY_PROMOTE);
                if (promoteLockerAlertType == PromoteLockerActivity.WHEN_APP_LAUNCH) {
                    Analytics.logEvent("StartApp_Promote_App_Downloaded");
                } else if (promoteLockerAlertType == PromoteLockerActivity.AFTER_APPLY_FINISH){
                    Analytics.logEvent("ApplyFinished_Promote_App_Downloaded");
                }
                if (promoteLockerAlertType != INSTALL_NOT_BY_PROMOTE) {
                    PromoteLockerAutoPilotUtils.logPromoteLockerDownloaded();
                }
            } else if (TextUtils.equals(AvatarAutoPilotUtils.CAMERA_PKG_NAME, pkgAdd)) {
                Analytics.logEvent("Colorphone_AvatarApp_Download", "AvatarType", AvatarAutoPilotUtils.CAMERA_NAME);
            } else if (TextUtils.equals(AvatarAutoPilotUtils.HEAD_PKG_NAME, pkgAdd)) {
                Analytics.logEvent("Colorphone_AvatarApp_Download", "AvatarType", AvatarAutoPilotUtils.HEAD_NAME);
            } else if (TextUtils.equals(AvatarAutoPilotUtils.ZMOJI_PKG_NAME, pkgAdd)) {
                Analytics.logEvent("Colorphone_AvatarApp_Download", "AvatarType", AvatarAutoPilotUtils.ZMOJI_NAME);
            }
        }
    }
}
