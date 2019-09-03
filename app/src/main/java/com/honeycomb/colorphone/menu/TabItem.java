package com.honeycomb.colorphone.menu;

import android.support.annotation.NonNull;
import android.view.View;

/**
 * @author sundxing
 */
public class TabItem {
    public static final String TAB_MAIN = "main";
    public static final String TAB_NEWS = "news";
    public static final String TAB_CASH = "cash";
    public static final String TAB_RINGTONE = "ringtone";
    public static final String TAB_SETTINGS = "settings";

    private String id;
    private int tabDrawable;
    private String tabName;
    private View frameView;
    private boolean enableToolBarTitle;
    private boolean enabled;

    public TabItem(@NonNull String id, int tabDrawable, @NonNull String tabName, boolean enableToolBarTitle) {
        this.id = id;
        this.tabDrawable = tabDrawable;
        this.tabName = tabName;
        this.enableToolBarTitle = enableToolBarTitle;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getTabDrawable() {
        return tabDrawable;
    }

    public void setTabDrawable(int tabDrawable) {
        this.tabDrawable = tabDrawable;
    }

    public String getTabName() {
        return tabName;
    }

    public void setTabName(String tabName) {
        this.tabName = tabName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public View getFrameView() {
        return frameView;
    }

    public void setFrameView(View frameView) {
        this.frameView = frameView;
    }

    public boolean isEnableToolBarTitle() {
        return enableToolBarTitle;
    }

    public void setEnableToolBarTitle(boolean enableToolBarTitle) {
        this.enableToolBarTitle = enableToolBarTitle;
    }
}
