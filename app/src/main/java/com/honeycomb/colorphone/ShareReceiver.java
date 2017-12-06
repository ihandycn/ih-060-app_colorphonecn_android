package com.honeycomb.colorphone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ComponentInfo;

import com.honeycomb.colorphone.activity.ShareAlertActivity;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.commons.utils.HSLog;


public class ShareReceiver extends BroadcastReceiver {
    public static final String THEME_NAME = "theme_name";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getExtras() != null) {
           ComponentInfo componentInfo =  intent.getExtras().getParcelable(Intent.EXTRA_CHOSEN_COMPONENT);
           if (componentInfo != null) {

               boolean isInsideApp = intent.getBooleanExtra(ShareAlertActivity.IS_INSIDE_APP, false);
               String themeName = intent.getStringExtra(THEME_NAME);
               HSLog.d("ShareReceiver" + componentInfo.packageName);

               if (isInsideApp) {
                   HSAnalytics.logEvent("Colorphone_Inapp_ShareAlert_ChooseAppToShare", "packageName", componentInfo.packageName, "V22", "true", "themeName", themeName);
               } else {
                   HSAnalytics.logEvent("Colorphone_Outapp_ShareAlert_ChooseAppToShare", "packageName", componentInfo.packageName, "V22", "true", "themeName", themeName);
               }

           }
        }
    }
}
