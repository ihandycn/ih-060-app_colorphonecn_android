package com.honeycomb.colorphone.util;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.widget.TextView;

import com.honeycomb.colorphone.R;
import com.superapps.util.Dimensions;
import com.superapps.util.Fonts;

import static com.ihs.app.framework.HSApplication.getContext;

/**
 * Set Style for some common views, like ActionBar, FloatButton, etc.
 */
public class ViewStyleUtils {

    public static void setToolBarTitle(TextView titleTextView) {
        setToolBarTitle(titleTextView, false);
    }

    @SuppressWarnings("deprecation")
    public static void setToolBarTitle(TextView titleTextView, boolean largeMargin) {
        setToolbarTitleWithoutLayoutParams(titleTextView);

        Toolbar.LayoutParams toolbarTitleParams = new Toolbar.LayoutParams(
                Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT, Gravity.START);
        boolean isRtl = Utils.isRtl();
        int margin = largeMargin ? Dimensions.pxFromDp(20) : Dimensions.pxFromDp(16);
        //noinspection ResourceType
        toolbarTitleParams.setMargins(isRtl ? 0 : margin, 0, isRtl ? margin : 0, 0);
        titleTextView.setLayoutParams(toolbarTitleParams);
    }

    public static void setToolbarTitleWithoutLayoutParams(TextView titleTextView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            titleTextView.setTextAppearance(R.style.ToolbarTextAppearance);
        } else {
            titleTextView.setTextAppearance(getContext(), R.style.ToolbarTextAppearance);
        }
        titleTextView.setTextColor(Color.WHITE);
        titleTextView.setTextSize(20);
        final Typeface typeface = Fonts.getTypeface(Fonts.Font.CUSTOM_FONT_SEMIBOLD);
        titleTextView.setTypeface(typeface);
    }
}
