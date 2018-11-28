package com.honeycomb.colorphone;

import com.acb.call.customize.ScreenFlashManager;
import com.acb.call.themes.Type;
import com.honeycomb.colorphone.factoryimpl.CpScreenFlashFactoryImpl;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSMapUtils;

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

            }

            @Override
            public void onScreenFlashSetSucceed(String idName) {
                LauncherAnalytics.logEvent("ColorPhone_Set_Success", "type", idName);
            }
        });
        ScreenFlashManager.getInstance().logTest = true;

    }
}
