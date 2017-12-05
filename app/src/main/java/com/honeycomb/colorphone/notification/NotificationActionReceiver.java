package com.honeycomb.colorphone.notification;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.ihs.commons.utils.HSLog;

import com.acb.call.themes.Type;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.activity.ThemePreviewActivity;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.inner.SessionMgr;
import com.ihs.commons.utils.HSPreferenceHelper;

import java.util.ArrayList;


/**
 * Created by jelly on 2017/11/23.
 */

public class NotificationActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        HSLog.d("NotificationUtils", "receive notification action");
        String action = intent.getAction();
        int id = intent.getIntExtra(NotificationConstants.THEME_NOTIFICATION_KEY, -1);
        if (id != NotificationConstants.THEME_NOTIFICATION_ID) {
           return;
        }
        if (action == null) {
            return;
        }

        boolean isNewTheme = intent.getBooleanExtra(NotificationConstants.THEME_NOTIFICATION_IS_NEW_THEME, false);
        boolean isMp4Downloaded = intent.getBooleanExtra(NotificationConstants.THEME_NOTIFICATION_MP4_DOWNLOADED, false);
        int index = intent.getIntExtra(NotificationConstants.THEME_NOTIFICATION_THEME_INDEX, 0);
        String themeName = intent.getStringExtra(NotificationConstants.THEME_NOTIFICATION_THEME_NAME);

        if (action.equals(NotificationConstants.THEME_NOTIFICATION_CLICK_ACTION)) {
            HSLog.d("NotificationUtils", "receive click action");

            ThemePreviewActivity.start(HSApplication.getContext(), index);
            if (isNewTheme) {
                HSAnalytics.logEvent("Colorphone_LocalPush_NewTheme_Clicked",
                        "ThemeName", themeName, "isDownloaded", String.valueOf(isMp4Downloaded));
                NotificationAutoPilotUtils.logNewThemeNotificationClicked();
            } else {
                HSAnalytics.logEvent("Colorphone_LocalPush_OldTheme_Clicked",
                        "ThemeName", themeName, "isDownloaded", String.valueOf(isMp4Downloaded));
                NotificationAutoPilotUtils.logOldThemeNotificationClicked();
            }

            android.app.NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(NotificationConstants.THEME_NOTIFICATION_ID);
        }

        if (action.equals(NotificationConstants.THEME_NOTIFICATION_DELETE_ACTION)) {
            HSLog.d("NotificationUtils", "receive delete action");

            if (isNewTheme) {
                HSAnalytics.logEvent("Colorphone_LocalPush_NewTheme_Deleted",
                        "ThemeName", themeName, "isDownloaded", String.valueOf(isMp4Downloaded));
            } else {
                HSAnalytics.logEvent("Colorphone_LocalPush_OldTheme_Deleted",
                        "ThemeName", themeName, "isDownloaded", String.valueOf(isMp4Downloaded));
            }
        }
    }
}
