package com.honeycomb.colorphone.util;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.Theme;
import com.ihs.app.framework.HSApplication;

public class TransitionUtil {

    public static String TAG_PREVIEW_IMAGE = HSApplication.getContext().getResources().getString(R.string.transition_view_theme_image);
    public static final String TAG_PREIVIEW_TXT = "Txt";
    public static final String TAG_PREIVIEW_RINTONE = "Ringtone";
    public static final String TAG_PREIVIEW_Btn = "Btn";

    public static String getViewTransitionName(String tag, Theme theme) {
        return tag + theme.getIdName();
    }
}
