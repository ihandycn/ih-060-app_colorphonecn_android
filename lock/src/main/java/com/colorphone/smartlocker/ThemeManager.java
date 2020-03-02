package com.colorphone.smartlocker;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.util.TypedValue;

import com.colorphone.lock.R;
import com.ihs.app.framework.HSApplication;

/**
 * @author JackSparrow
 * @date 10/03/2017.
 */

public class ThemeManager {
    public static final int THEME_DEFAULT = 0;

    private static final String MODULE_NAME = "optimizer_theme_manager";

    private static final String PREF_APPLIED_THEME = "PREF_APPLIED_THEME";

    private volatile static ThemeManager instance;

    private int theme;

    public static ThemeManager getInstance() {
        if (instance == null) {
            synchronized (ThemeManager.class) {
                if (instance == null) {
                    instance = new ThemeManager();
                }
            }
        }
        return instance;
    }

    private ThemeManager() {
        theme = THEME_DEFAULT;
    }

    public int getTheme() {
        return theme;
    }

    @ColorInt
    public static int getPrimaryColor() {
        return getPrimaryColor(HSApplication.getContext());
    }

    @ColorInt
    public static int getPrimaryColor(Context context) {
        return getAttrTypeValueData(context, R.attr.colorPrimary);
    }

    public static int getAttrTypeValueData(Context context, int resId) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(resId, typedValue, true);
        return typedValue.data;
    }

    public static int getAttrTypeValueResourceId(Context context, int resId) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(resId, typedValue, true);
        return typedValue.resourceId;
    }
}
