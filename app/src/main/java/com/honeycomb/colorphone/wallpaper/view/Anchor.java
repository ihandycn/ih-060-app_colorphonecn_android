package com.honeycomb.colorphone.wallpaper.view;

import android.content.Context;
import android.support.v4.widget.Space;
import android.util.AttributeSet;

/**
 * Anchor view used in XML layouts to reduce layout tree depth with {@link android.widget.RelativeLayout}.
 */
public class Anchor extends Space {

    public Anchor(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}
