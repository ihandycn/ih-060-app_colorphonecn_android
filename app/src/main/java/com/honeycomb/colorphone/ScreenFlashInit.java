package com.honeycomb.colorphone;

import com.acb.call.customize.ScreenFlashManager;
import com.acb.call.themes.Type;
import com.honeycomb.colorphone.download.TasksManager;
import com.honeycomb.colorphone.download.TasksManagerModel;
import com.honeycomb.colorphone.factoryimpl.CpScreenFlashFactoryImpl;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSMapUtils;
import com.superapps.util.Preferences;

import net.appcloudbox.autopilot.AutopilotConfig;

import java.util.Map;

public class ScreenFlashInit extends AppMainInit {
    @Override
    public void onInit(HSApplication application) {
        ScreenFlashManager.init(new CpScreenFlashFactoryImpl());
        ScreenFlashManager.getInstance().setParser(new ScreenFlashManager.TypeParser() {
            @Override
            public Type parse(Map<String, ?> map) {
                Theme type = new Theme();
                Type.fillData(type, map);
                type.setNotificationLargeIconUrl(HSMapUtils.optString(map, "", "LocalPush", "LocalPushIcon"));
                type.setNotificationBigPictureUrl(HSMapUtils.optString(map, "", "LocalPush", "LocalPushPreviewImage"));
                type.setNotificationEnabled(HSMapUtils.optBoolean(map, false, "LocalPush", "Enable"));
                type.setDownload(HSMapUtils.getInteger(map, Theme.CONFIG_DOWNLOAD_NUM));
                type.setRingtoneUrl(HSMapUtils.optString(map, "", Theme.CONFIG_RINGTONE));
                type.setLocked(HSMapUtils.optBoolean(map,false, "Status", "Lock"));
                type.setCanDownload(!HSMapUtils.optBoolean(map,true, "Status", "StaticPreview"));
                type.setSpecialTopic(HSMapUtils.optBoolean(map, false, "SpecialTopic"));
                return type;
            }
        });
        ScreenFlashManager.getInstance().setImageLoader(new ThemeImageLoader());
        ScreenFlashManager.getInstance().setLogEventListener(new ScreenFlashManager.LogEventListener() {
            @Override
            public void onListPageItemClicked() {

            }

            @Override
            public void onListPageScrollUp() {

            }

            @Override
            public void onListItemDownloadIconClicked() {

            }

            @Override
            public void onDetailContactIconClicked() {

            }

            @Override
            public void onContactSetIconClicked() {

            }

            @Override
            public void onSelectedFromList() {

            }

            @Override
            public void onSelectedFromDetail() {

            }

            @Override
            public void onInCallFlashShown(String themeId) {
                if (Utils.isNewUser()) {

                    Preferences.get(Constants.DESKTOP_PREFS).doOnce(new Runnable() {
                        @Override
                        public void run() {
                            logOnceFlashShowNewUser(themeId);
                        }
                    }, "flash_show_log_once");
                }
            }

            private void logOnceFlashShowNewUser(String themeId) {
                Theme targetTheme = null;
                for (Theme theme : Theme.themes()) {
                    if (String.valueOf(theme.getId()).equals(themeId)) {
                        targetTheme = theme;
                        break;
                    }
                }

                boolean themeNotReady = false;
                if (targetTheme != null && targetTheme.isMedia()) {
                    TasksManager.getImpl().addTask(targetTheme);
                    TasksManagerModel model = TasksManager.getImpl().getByThemeId(targetTheme.getId());
                    if (model != null) {
                        themeNotReady = !TasksManager.getImpl().isDownloaded(model);
                    }
                }

                if (targetTheme != null) {
                    AutopilotConfig.setAudienceProperty("theme_id", targetTheme.getIdName());
                }

                String name = "Null";
                if (targetTheme != null) {
                    name = targetTheme.getIdName();
                    if (themeNotReady) {
                        name += "_NotReady";
                    }
                }
                LauncherAnalytics.logEvent("ColorPhone_ScreenFlash_Set_NewUser", "themename", name);
            }

            @Override
            public void onScreenFlashSetSucceed(String idName) {
                LauncherAnalytics.logEvent("ColorPhone_Set_Success", "type", idName);
            }
        });
        ScreenFlashManager.getInstance().logTest = true;

    }
}
