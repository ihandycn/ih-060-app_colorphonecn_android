package com.honeycomb.colorphone.dialer;

import android.graphics.Typeface;

public class ConfigProvider {

    private static ConfigProvider sConfigProvider = new ConfigProvider();

    public static ConfigProvider get() {
        return sConfigProvider;
    }

    public static ConfigProvider set(ConfigProvider provider) {
        return sConfigProvider = provider;
    }

    public boolean getBoolean(String name, boolean defaultValue) {
        return defaultValue;
    }

    public Typeface getCustomTypeface() {
        return null;
    }

    public Typeface getCustomMediumTypeface() {
        return null;
    }

    public Typeface getCustomBoldTypeface() {
        return null;
    }
}
