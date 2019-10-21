package com.honeycomb.colorphone.wallpaper.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.honeycomb.colorphone.LauncherAnalytics;
import com.honeycomb.colorphone.wallpaper.model.LauncherFiles;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Preferences;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by caoyixiong on 17/10/12.
 */

public class OurAppInstalledReceiver extends BroadcastReceiver {

    private static final String REFERRER_LOGGED = "REFERRER_LOGGED";

    @Override
    public void onReceive(Context context, Intent intent) {
        Preferences.get(LauncherFiles.COMMON_PREFS).doOnce(() -> {
            HSLog.d("OurAppInstalledReceiver", "onReceiver referrer = " + intent.getStringExtra("referrer"));

            String referrerString = intent.getStringExtra("referrer");

            LauncherAnalytics.logEvent("utm_source", "source", referrerString);
            logWallpaperShareReferrer(referrerString);

            logInternal(referrerString);
        }, REFERRER_LOGGED);
    }

    private void logInternal(String referrerString) {
        if (TextUtils.isEmpty(referrerString)) {
            return;
        }

        try {
            String decodeContent = Uri.decode(referrerString);
            if (!decodeContent.contains("internal")) {
                return;
            }

            Map<String, String> referrer = new HashMap<>();

            String[] strings = decodeContent.split("&");

            for (String string : strings) {
                int index = string.indexOf("=");
                if (index < 0) {
                    continue;
                }
                if (string.contains("internal")) {
                    continue;
                }
                referrer.put(string.substring(0, index), string.substring(index + 1, string.length()));
            }

            LauncherAnalytics.logEvent("Source_Channel_Internal", true, referrer);
        } catch (Exception e) {
            HSLog.e("referrer error");
        }
    }

    private void logWallpaperShareReferrer(String referrer) {
        if (!TextUtils.isEmpty(referrer)) {
            String logMsg = "";
            if (referrer.endsWith("PicturePage")) {
                logMsg = "PicturePage";
            } else if (referrer.endsWith("Live")) {
                logMsg = "Live";
            } else if (referrer.endsWith("3D")) {
                logMsg = "3D";
            }
            if (!TextUtils.isEmpty(logMsg)) {
                LauncherAnalytics.logEvent("Launcher_Installed_FromShareAlert", "type", logMsg);
            }
        }
    }
}