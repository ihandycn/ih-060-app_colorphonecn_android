package com.themelab.launcher.icon;

import android.graphics.Bitmap;

/**
 * A trivial {@link IconProcessor} that returns the input bitmap directly without any processing.
 */
public class IdentityIconProcessor implements IconProcessor {

    @Override
    public Bitmap processIcon(Bitmap original) {
        return original;
    }
}
