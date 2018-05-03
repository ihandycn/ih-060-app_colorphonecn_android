package com.honeycomb.colorphone.notification;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.honeycomb.colorphone.activity.ColorPhoneActivity;
import com.honeycomb.colorphone.activity.ThemePreviewActivity;
import com.honeycomb.colorphone.boost.BoostAutoPilotUtils;
import com.honeycomb.colorphone.boost.BoostPlusCleanDialog;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;


/**
 * Created by jelly on 2017/11/23.
 */

public class NotificationActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        HSLog.d("NotificationUtils", "receive notification action: " + action);
//        int id = intent.getIntExtra(NotificationConstants.THEME_NOTIFICATION_KEY, -1);
//        if (id != NotificationConstants.THEME_NOTIFICATION_ID) {
//            return;
//        }
        if (action == null) {
            return;
        }

        boolean isNewTheme = intent.getBooleanExtra(NotificationConstants.THEME_NOTIFICATION_IS_NEW_THEME, false);
        boolean isMp4Downloaded = intent.getBooleanExtra(NotificationConstants.THEME_NOTIFICATION_MP4_DOWNLOADED, false);
        int index = intent.getIntExtra(NotificationConstants.THEME_NOTIFICATION_THEME_INDEX, 0);
        String themeName = intent.getStringExtra(NotificationConstants.THEME_NOTIFICATION_THEME_NAME);

        switch (action) {
            case NotificationConstants.THEME_NOTIFICATION_CLICK_ACTION:
                HSLog.d("NotificationUtils", "receive click action");

                Intent parentActivityIntent = new Intent(context, ColorPhoneActivity.class);
                parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                Intent themePreviewIntent = new Intent(context, ThemePreviewActivity.class);
                themePreviewIntent.putExtra("position", index);
                themePreviewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                Intent[] intents = {parentActivityIntent, themePreviewIntent};
                context.startActivities(intents);

                if (isNewTheme) {
                    LauncherAnalytics.logEvent("Colorphone_LocalPush_NewTheme_Clicked",
                            "ThemeName", themeName, "isDownloaded", String.valueOf(isMp4Downloaded));
                    NotificationAutoPilotUtils.logNewThemeNotificationClicked();
                } else {
                    LauncherAnalytics.logEvent("Colorphone_LocalPush_OldTheme_Clicked",
                            "ThemeName", themeName, "isDownloaded", String.valueOf(isMp4Downloaded));
                    NotificationAutoPilotUtils.logOldThemeNotificationClicked();
                }

                android.app.NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(NotificationConstants.THEME_NOTIFICATION_ID);
                break;
            case NotificationConstants.THEME_NOTIFICATION_DELETE_ACTION:
                HSLog.d("NotificationUtils", "receive delete action");

                if (isNewTheme) {
                    LauncherAnalytics.logEvent("Colorphone_LocalPush_NewTheme_Deleted",
                            "ThemeName", themeName, "isDownloaded", String.valueOf(isMp4Downloaded));
                } else {
                    LauncherAnalytics.logEvent("Colorphone_LocalPush_OldTheme_Deleted",
                            "ThemeName", themeName, "isDownloaded", String.valueOf(isMp4Downloaded));
                }
                break;
            case NotificationConstants.ACTION_BOOST_PLUS:
                BoostAutoPilotUtils.logBoostPushClicked();
                LauncherAnalytics.logEvent("Colorphone_Push_Boost_Clicked");
                BoostPlusCleanDialog.showBoostPlusCleanDialog(context, BoostPlusCleanDialog.CLEAN_TYPE_CLEAN_CENTER);
//                BoostActivity.start(context, false);
                break;
            default:
                break;
        }
    }
}
