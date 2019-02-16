package com.honeycomb.colorphone.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.honeycomb.colorphone.activity.ShareAlertActivity;
import com.ihs.commons.utils.HSLog;


public class ShareReceiver extends BroadcastReceiver {
    public static final String THEME_NAME = "theme_name";
    public static final String IS_CONTACT = "is_contact";
    public static final String IS_SET_FOR_SOMEONE = "is_set_for_someone";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getExtras() != null) {
           ComponentName componentName =  intent.getExtras().getParcelable(Intent.EXTRA_CHOSEN_COMPONENT);
           if (componentName != null) {

               boolean isInsideApp = intent.getBooleanExtra(ShareAlertActivity.IS_INSIDE_APP, false);
               boolean isContact = intent.getBooleanExtra(IS_CONTACT, false);
               boolean isSetForSomeone = intent.getBooleanExtra(IS_SET_FOR_SOMEONE, false);
               String themeName = intent.getStringExtra(THEME_NAME);

               HSLog.d("ShareReceiver" + componentName.getPackageName());

               if (isInsideApp) {
//                   Analytics.logEvent("Colorphone_Inapp_ShareAlert_ChooseAppToShare", "packageName",
//                           componentName.getPackageName(), "V22", "true", "themeName", themeName, "isContact", String.valueOf(isContact));
               } else {
//                   Analytics.logEvent("Colorphone_Outapp_ShareAlert_ChooseAppToShare", "packageName", componentName.getPackageName(),
//                           "V22", "true", "themeName", themeName, "isSetForSomeOne", String.valueOf(isSetForSomeone));
               }
           }
        }
    }
}
