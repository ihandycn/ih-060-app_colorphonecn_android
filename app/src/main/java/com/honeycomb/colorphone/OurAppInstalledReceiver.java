package com.honeycomb.colorphone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;

import java.util.HashMap;
import java.util.Map;

public class OurAppInstalledReceiver extends BroadcastReceiver {

    public static final String PREF_FILE_OUR_APP_INSTALLED_RECEIVER = "optimizer_our_app_installed_receiver";
    public static final String REFERRER = "REFERRER";

    @Override
    public void onReceive(Context context, Intent intent) {

        HSLog.d("OurAppInstalledReceiver", "onReceiver referrer = " + intent.getStringExtra("referrer"));

        String referrerString = intent.getStringExtra("referrer");

        HSAnalytics.logEvent("utm_source", "source", referrerString);
        HSPreferenceHelper.create(context, PREF_FILE_OUR_APP_INSTALLED_RECEIVER).putStringInterProcess(REFERRER, referrerString);

        try {
            String decodeContent = Uri.decode(referrerString);
            if (!decodeContent.contains("internal=panel")){
                return;
            }

            Map<String, String> referrer = new HashMap<>();
            if (referrerString.equals("")) {
                return;
            }

            String[] strings = decodeContent.split("&");

            for (String string : strings) {
                int index = string.indexOf("=");
                if (index < 0) {
                    continue;
                }
                if (string.contains("internal")){
                    continue;
                }
                referrer.put(string.substring(0, index), string.substring(index + 1, string.length()));
            }

            HSAnalytics.logEvent("Source_Channel_Internal", referrer);
        } catch (Exception e) {
            HSLog.e("referrer error");
        }
    }
}
