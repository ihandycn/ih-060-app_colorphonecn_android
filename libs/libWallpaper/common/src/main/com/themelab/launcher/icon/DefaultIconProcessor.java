package com.themelab.launcher.icon;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.support.annotation.DrawableRes;

import com.acb.libwallpaper.live.theme.ThemeResources;

/**
 * A default implementation of {@link IconProcessor}.
 */
public class DefaultIconProcessor extends CropFrameIconProcessor {

    private static final int ALPHA_THRESHOLD = 23;
    private static final float MIN_RATIO_THRESHOLD = 0.78f;
    private static final float SHRINK_RATIO = 0.7f;

    public DefaultIconProcessor(Resources res, @DrawableRes int iconBgId, @DrawableRes int iconBgDecorId) {
        super(res, iconBgId, iconBgDecorId);
    }

    public DefaultIconProcessor(ThemeResources res, String iconBgId, String iconBgDecorId) {
        super(res, iconBgId, iconBgDecorId);
    }

    @Override
    protected void calculateSizeTransformation(Bitmap icon, Rect outSrcRect, Rect outDstRect) {
        final int halfWidth = icon.getWidth() / 2;
        final int halfHeight = icon.getHeight() / 2;
        int sampleX;

        // Left
        int sampleY = halfHeight;
        for (sampleX = 0; sampleX < halfWidth; sampleX++) {
            if (!isTransparent(icon, sampleX, sampleY)) {
                break;
            }
        }
        float leftRatio = ((float) (halfWidth - sampleX)) / halfWidth;

        // Top
        sampleX = halfWidth;
        for (sampleY = 0; sampleY < halfHeight; sampleY++) {
            if (!isTransparent(icon, sampleX, sampleY)) {
                break;
            }
        }
        float topRatio = ((float) (halfHeight - sampleY)) / halfHeight;

        // Right
        sampleY = halfHeight;
        for (sampleX = icon.getWidth() - 1; sampleX > halfWidth; sampleX--) {
            if (!isTransparent(icon, sampleX, sampleY)) {
                break;
            }
        }
        float rightRatio = ((float) (sampleX - halfWidth)) / halfWidth;

        // Bottom
        sampleX = halfWidth;
        for (sampleY = icon.getHeight() - 1; sampleY > halfHeight; sampleY--) {
            if (!isTransparent(icon, sampleX, sampleY)) {
                break;
            }
        }
        float bottomRatio = ((float) (sampleY - halfHeight)) / halfHeight;

        float minimumRatio = Math.min(leftRatio, Math.min(topRatio, Math.min(rightRatio, bottomRatio)));
        if (minimumRatio < MIN_RATIO_THRESHOLD) {
            // Shrink
            outSrcRect.set(0, 0, icon.getWidth(), icon.getHeight());
            int left = Math.round((1 - SHRINK_RATIO) * halfWidth);
            int top = Math.round((1 - SHRINK_RATIO) * halfHeight);
            int right = Math.round((1 + SHRINK_RATIO) * halfWidth);
            int bottom = Math.round((1 + SHRINK_RATIO) * halfHeight);
            outDstRect.set(left, top, right, bottom);
        } else {
            // Enlarge
            int left = Math.round((1 - minimumRatio) * halfWidth);
            int top = Math.round((1 - minimumRatio) * halfHeight);
            int right = Math.round((1 + minimumRatio) * halfWidth);
            int bottom = Math.round((1 + minimumRatio) * halfHeight);
            outSrcRect.set(left, top, right, bottom);
        }
    }

    private boolean isTransparent(Bitmap icon, int pixelX, int pixelY) {
        int pixel;
        try {
            pixel = icon.getPixel(pixelX, pixelY);
        } catch (Exception e) {
            throw new RuntimeException("Error getting bitmap pixel at (" + pixelX + ", " + pixelY + "), bitmap size (" +
                    icon.getWidth() + ", " + icon.getHeight() + ")");
        }
        int alpha = (pixel & 0xff000000) >> 24;
        if (alpha >= ALPHA_THRESHOLD || alpha < 0) {
            return false;
        }
        return true;
    }
}
