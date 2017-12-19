package com.honeycomb.colorphone.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.colorphone.lock.util.PreferenceHelper;
import com.honeycomb.colorphone.activity.PromoteLockerActivity;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.honeycomb.colorphone.util.PromoteLockerAutoPilotUtils;
import com.ihs.commons.utils.HSLog;

/**
 * Created by jelly on 2017/12/18.
 */

public class PackageAddReceiver extends BroadcastReceiver {

    private static final int INSTALL_NOT_BY_PROMOTE = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())
                || Intent.ACTION_PACKAGE_INSTALL.equals(intent.getAction())) {
            String pkgAdd = intent.getData().getSchemeSpecificPart();
            HSLog.d("PackageAddReceiver", "Pkg add :" + pkgAdd);
            String pkgName = PromoteLockerAutoPilotUtils.getPromoteLockerApp();
            HSLog.d("PackageAddReceiver", "Pkg local read :" + pkgName);

            if (TextUtils.equals(pkgName, pkgAdd)) {
                int promoteLockerAlertType = PreferenceHelper.get(PromoteLockerActivity.PREFS_FILE).getInt(PromoteLockerActivity.ALERT_TYPE, INSTALL_NOT_BY_PROMOTE);
                if (promoteLockerAlertType == PromoteLockerActivity.WHEN_APP_LAUNCH) {
                    LauncherAnalytics.logEvent("StartApp_Promote_App_Downloaded");
                } else if (promoteLockerAlertType == PromoteLockerActivity.AFTER_APPLY_FINISH){
                    LauncherAnalytics.logEvent("ApplyFinished_Promote_App_Downloaded");
                }
                if (promoteLockerAlertType != INSTALL_NOT_BY_PROMOTE) {
                    PromoteLockerAutoPilotUtils.logPromoteLockerDownloaded();
                }
            }
        }
    }
}
