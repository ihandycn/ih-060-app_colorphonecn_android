package com.themelab.launcher.icon;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.support.annotation.DrawableRes;

import com.honeycomb.colorphone.wallpaper.theme.ThemeResources;


/**
 * A default implementation of {@link IconProcessor}.
 */
public class EnlargeIconProcessor extends CropFrameIconProcessor {

    private static final float ENLARGE_RATIO = 0.80f; // (1f / 0.85f)x

    public EnlargeIconProcessor(Resources res, @DrawableRes int iconBgId, @DrawableRes int iconBgDecorId) {
        super(res, iconBgId, iconBgDecorId);
    }

    public EnlargeIconProcessor(ThemeResources res, String iconBgId, String iconBgDecorId) {
        super(res, iconBgId, iconBgDecorId);
    }

    @Override
    protected void calculateSizeTransformation(Bitmap icon, Rect outSrcRect, Rect outDstRect) {
        final int halfWidth = icon.getWidth() / 2;
        final int halfHeight = icon.getHeight() / 2;

        // Enlarge
        outSrcRect.set(0, 0, icon.getWidth(), icon.getHeight());
        int left = Math.round((1 - ENLARGE_RATIO) * halfWidth);
        int top = Math.round((1 - ENLARGE_RATIO) * halfHeight);
        int right = Math.round((1 + ENLARGE_RATIO) * halfWidth);
        int bottom = Math.round((1 + ENLARGE_RATIO) * halfHeight);
        outSrcRect.set(left, top, right, bottom);
    }
}
