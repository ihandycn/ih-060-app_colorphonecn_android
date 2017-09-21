package com.honeycomb.colorphone;

public interface ConfigLog {

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
