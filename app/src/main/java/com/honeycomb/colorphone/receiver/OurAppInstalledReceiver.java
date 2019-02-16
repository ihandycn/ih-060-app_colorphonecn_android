package com.honeycomb.colorphone.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.honeycomb.colorphone.util.Analytics;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;

import java.util.HashMap;
import java.util.Map;

public class OurAppInstalledReceiver extends BroadcastReceiver {

    public static final String PREF_FILE_OUR_APP_INSTALLED_RECEIVER = "optimizer_our_app_installed_receiver";
    public static final String REFERRER = "REFERRER";
    private static final String REFERRER_LOGGED = "REFERRER_LOGGED";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (HSPreferenceHelper.getDefault().contains(REFERRER_LOGGED) == false) {
            HSLog.d("OurAppInstalledReceiver", "onReceiver referrer = " + intent.getStringExtra("referrer"));

            HSPreferenceHelper.getDefault().putBoolean(REFERRER_LOGGED, true);
            String referrerString = intent.getStringExtra("referrer");

            Analytics.logEvent("utm_source", "source", referrerString);
            HSPreferenceHelper.create(context, PREF_FILE_OUR_APP_INSTALLED_RECEIVER).putStringInterProcess(REFERRER, referrerString);
            if (referrerString != null && referrerString.contains("internal")) {
                try {
                    String decodeContent = Uri.decode(referrerString);

                    Map<String, String> referrer = new HashMap<>();
                    if (TextUtils.isEmpty(referrerString)) {
                        return;
                    }

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

                    Analytics.logEvent("Source_Channel_Internal", referrer);
                } catch (Exception e) {
                    HSLog.e("referrer error");
                }
            }
        }
    }
}
