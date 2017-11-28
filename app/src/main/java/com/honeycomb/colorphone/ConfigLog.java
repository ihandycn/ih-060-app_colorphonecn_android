package com.honeycomb.colorphone;

public interface ConfigLog {

    String FROM_LIST = "list";
    String FROM_DETAIL = "detail_page";
    Event getEvent();

    interface Event {
        void onMainViewOpen();
        void onThemePreviewOpen(String name);
        void onChooseTheme(String name, String from);
        void onThemeDownloadStart(String name, String from);
        void onThemeDownloadFinish(String name);
        void onFeedBackClick();
        void onColorPhoneEnableFromSetting(boolean enabled);
        void onCallAssistantEnableFromSetting(boolean enabled);
    }

}
