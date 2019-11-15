package com.honeycomb.colorphone.theme;

public interface ThemeUpdateListener {

    void onFailure(String errorMsg);

    void onSuccess(boolean isHasData);
}
