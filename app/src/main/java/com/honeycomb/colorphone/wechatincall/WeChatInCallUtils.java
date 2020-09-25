package com.honeycomb.colorphone.wechatincall;

import com.acb.call.constant.ScreenFlashConst;
import com.acb.call.customize.ScreenFlashSettings;
import com.acb.call.wechat.WeChatInCallManager;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.download.TasksManager;
import com.honeycomb.colorphone.download.TasksManagerModel;
import com.superapps.util.Preferences;

public class WeChatInCallUtils {

    public static final String PREFS_SCREEN_FLASH_WE_CHAT_THEME_ID = "prefs_screen_flash_we_chat_theme_id";

    public static void applyWeChatInCallTheme(Theme theme) {
        TasksManagerModel tasksManagerModel = TasksManager.getImpl().requestRingtoneTask(theme);
        if (tasksManagerModel != null) {
            String path = tasksManagerModel.getPath();
            Preferences.getDefault().putString(WeChatInCallManager.PREFS_SCREEN_FLASH_WE_CHAT_RING_TONE_PATH, path);
        }
        Preferences.getDefault().putString(PREFS_SCREEN_FLASH_WE_CHAT_THEME_ID, theme.getFileName());
        ScreenFlashSettings.putInt(ScreenFlashConst.PREFS_SCREEN_FLASH_WE_CHAT_THEME_ID, theme.getId());
    }

    public static String getWeChatInCallThemeName() {
        return Preferences.getDefault().getString(PREFS_SCREEN_FLASH_WE_CHAT_THEME_ID, "");
    }
}
