package com.themelab.launcher.icon;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.support.annotation.DrawableRes;
import android.util.TypedValue;

import com.acb.libwallpaper.live.theme.ThemeConstants;
import com.acb.libwallpaper.live.theme.ThemeResources;
import com.ihs.app.framework.HSApplication;

/**
 * A default implementation of {@link IconProcessor}.
 */
public class ShrinkIconProcessor extends CropFrameIconProcessor {

    private static final float DEFAULT_SHRINK_RATIO = 0.62f;

    public ShrinkIconProcessor(Resources res, @DrawableRes int iconBgId, @DrawableRes int iconBgDecorId) {
        super(res, iconBgId, iconBgDecorId);
    }

    public ShrinkIconProcessor(ThemeResources res, String iconBgId, String iconBgDecorId) {
        super(res, iconBgId, iconBgDecorId);
    }

    @Override
    protected void calculateSizeTransformation(Bitmap icon, Rect outSrcRect, Rect outDstRect) {
        final int halfWidth = icon.getWidth() / 2;
        final int halfHeight = icon.getHeight() / 2;

        // Shrink
        outSrcRect.set(0, 0, icon.getWidth(), icon.getHeight());
        float ratio = getShrinkRatio();
        int left = Math.round((1 - ratio) * halfWidth);
        int top = Math.round((1 - ratio) * halfHeight);
        int right = Math.round((1 + ratio) * halfWidth);
        int bottom = Math.round((1 + ratio) * halfHeight);
        outDstRect.set(left, top, right, bottom);
    }

    private float getShrinkRatio() {
        Context context = HSApplication.getContext();
        Resources res = context.getResources();
        int ratioOverrideResId = res.getIdentifier(
                ThemeConstants.RES_NAME_SHRINK_ICON_PROCESSOR_RATIO_OVERRIDE,
                "dimen", context.getPackageName());
        if (ratioOverrideResId > 0) {
            TypedValue value = new TypedValue();
            res.getValue(ratioOverrideResId, value, true);
            return value.getFloat();
        }
        return DEFAULT_SHRINK_RATIO;
    }
}
