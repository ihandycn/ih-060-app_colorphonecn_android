package com.honeycomb.colorphone.customize;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.honeycomb.colorphone.BuildConfig;


/**
 * When apk download completes, this receiver handles it.
 */
public class ThemeDownloadReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

        if (downloadId > 0 && context.getPackageName().equals(BuildConfig.APPLICATION_ID)) {

        }
    }
}
