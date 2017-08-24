package com.honeycomb.colorphone;

import java.util.List;

public interface ConfigLog {

    List<String> getHotThemeList();

    Event getEvent();

    interface Event {
        void onMainViewOpen();
        void onThemePreviewOpen(String name);
        void onChooseTheme(String name);
        void onThemeDownloadStart(String name);
        void onThemeDownloadFinish(String name);
        void onFeedBackClick();
        void onColorPhoneEnableFromSetting(boolean enabled);
        void onCallAssistantEnableFromSetting(boolean enabled);
    }

}
