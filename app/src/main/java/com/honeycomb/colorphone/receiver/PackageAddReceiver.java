package com.honeycomb.colorphone.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.honeycomb.colorphone.boost.SystemAppsManager;
import com.honeycomb.colorphone.util.AvatarAutoPilotUtils;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Threads;


/**
 * Created by jelly on 2017/12/18.
 */

@Deprecated
public class PackageAddReceiver extends BroadcastReceiver {

    private static final int INSTALL_NOT_BY_PROMOTE = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())
                || Intent.ACTION_PACKAGE_INSTALL.equals(intent.getAction())) {
            final String pkgAdd = intent.getData().getSchemeSpecificPart();
            HSLog.d("PackageAddReceiver", "Pkg add :" + pkgAdd);
            Threads.postOnMainThread(new Runnable() {
                @Override
                public void run() {
                    SystemAppsManager.getInstance().addPackage(pkgAdd);
                }
            });

           if (TextUtils.equals(AvatarAutoPilotUtils.CAMERA_PKG_NAME, pkgAdd)) {
                LauncherAnalytics.logEvent("Colorphone_AvatarApp_Download", "AvatarType", AvatarAutoPilotUtils.CAMERA_NAME);
            } else if (TextUtils.equals(AvatarAutoPilotUtils.HEAD_PKG_NAME, pkgAdd)) {
                LauncherAnalytics.logEvent("Colorphone_AvatarApp_Download", "AvatarType", AvatarAutoPilotUtils.HEAD_NAME);
            } else if (TextUtils.equals(AvatarAutoPilotUtils.ZMOJI_PKG_NAME, pkgAdd)) {
                LauncherAnalytics.logEvent("Colorphone_AvatarApp_Download", "AvatarType", AvatarAutoPilotUtils.ZMOJI_NAME);
            }
        }
    }
}
