package com.honeycomb.colorphone;

import com.acb.call.customize.ScreenFlashManager;
import com.acb.call.themes.Type;
import com.honeycomb.colorphone.factoryimpl.CpScreenFlashFactoryImpl;
import com.honeycomb.colorphone.notification.NotificationServiceV18;
import com.honeycomb.colorphone.theme.RandomTheme;
import com.honeycomb.colorphone.theme.ThemeList;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSMapUtils;
import com.superapps.util.Permissions;
import com.superapps.util.Preferences;

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
                type.setUploaderName(HSMapUtils.optString(map, "", Theme.CONFIG_UPLOADER));
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

                RandomTheme.getInstance().onFlashShow(themeId);
            }

            private void logOnceFlashShowNewUser(String themeId) {
                Theme targetTheme = null;
                for (Theme theme : ThemeList.themes()) {
                    if (String.valueOf(theme.getId()).equals(themeId)) {
                        targetTheme = theme;
                        break;
                    }
                }

                String name = "Null";
                if (targetTheme != null) {
                    name = targetTheme.getIdName();
                }
                Analytics.logEvent("ColorPhone_ScreenFlash_Set_NewUser", "themename", name);
            }

            @Override
            public void onScreenFlashSetSucceed(String idName) {
                Analytics.logEvent("ColorPhone_Set_Success", "type", idName);
            }
        });
        ScreenFlashManager.getInstance().logTest = true;
        if (Permissions.isNotificationAccessGranted()) {
            NotificationServiceV18.ensureCollectorRunning(application.getApplicationContext());
        }

    }
}
