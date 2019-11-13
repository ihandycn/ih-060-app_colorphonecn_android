package com.themelab.launcher.icon;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.text.TextUtils;

import com.honeycomb.colorphone.wallpaper.theme.ThemeResources;


/**
 * An {@link IconProcessor} that crops the original icon with given mask and draw a frame over the result.
 */
public abstract class CropFrameIconProcessor implements IconProcessor {

    private static final int MINIMUM_ICON_SIZE_PX = 24;

    private Paint mBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private Paint mCropPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    private Bitmap mIconBg;
    private Bitmap mIconBgDecor;

    public CropFrameIconProcessor(Resources res, @DrawableRes int iconBgId, @DrawableRes int iconBgDecorId) {
        loadResources(res, iconBgId, iconBgDecorId);
    }

    public CropFrameIconProcessor(ThemeResources res, String iconBgId, String iconBgDecorId) {
        loadResources(res, iconBgId, iconBgDecorId);
    }

    @Override
    public Bitmap processIcon(Bitmap original) {
        if (original == null

                // Skip processing images that are too small
                || original.getWidth() < MINIMUM_ICON_SIZE_PX
                || original.getHeight() < MINIMUM_ICON_SIZE_PX

                // If both mIconBg & mIconBgDecor are null, the output would be totally transparent,
                // which can never be what we want and hence should be treat as an error.
                || (mIconBg == null && mIconBgDecor == null)) {

            return original;
        }
        Bitmap result;
        try {
            result = Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
            throw new RuntimeException("Out of memory while creating result bitmap");
        }
        Canvas canvas = new Canvas(result);
        Rect dstRect = new Rect();
        dstRect.set(0, 0, result.getWidth(), result.getHeight());
        if (mIconBg != null) {
            canvas.drawBitmap(mIconBg, null, dstRect, mBitmapPaint);
        }

        Bitmap croppedIcon;
        try {
            croppedIcon = Bitmap.createBitmap(result.getWidth(), result.getHeight(), Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
            throw new RuntimeException("Out of memory while creating bitmap for processing icon");
        }
        Canvas iconCanvas = new Canvas(croppedIcon);
        if (mIconBg != null) {
            iconCanvas.drawBitmap(mIconBg, null, dstRect, mBitmapPaint);
        }

        Rect srcRect = new Rect();
        calculateSizeTransformation(original, srcRect, dstRect);
        iconCanvas.drawBitmap(original, srcRect, dstRect, mCropPaint);
        dstRect.set(0, 0, result.getWidth(), result.getHeight());

        canvas.drawBitmap(croppedIcon, null, dstRect, mBitmapPaint);
        if (mIconBgDecor != null) {
            canvas.drawBitmap(mIconBgDecor, null, dstRect, mBitmapPaint);
        }
        return result;
    }

    /**
     * Implement this to define the transfer metrics of original bitmap to result bitmap.
     */
    protected abstract void calculateSizeTransformation(Bitmap icon, Rect outSrcRect, Rect outDstRect);

    private void loadResources(ThemeResources resources, String iconBgId, String iconBgDecorId) {
        if (resources != null) {
            if (!TextUtils.isEmpty(iconBgId)) {
                Drawable themedFolderBg = null;
                // themeRes == null when system theme is applied
                try {
                    themedFolderBg = resources.getDrawable(iconBgId);
                    mIconBg = ((BitmapDrawable) themedFolderBg).getBitmap();
                } catch (Resources.NotFoundException ignored) {
                }
            }
            if (!TextUtils.isEmpty(iconBgDecorId)) {
                Drawable themedFolderBg = null;
                // themeRes == null when system theme is applied
                try {
                    themedFolderBg = resources.getDrawable(iconBgDecorId);
                    mIconBgDecor = ((BitmapDrawable) themedFolderBg).getBitmap();
                } catch (Resources.NotFoundException ignored) {
                }
            }
        }
        mCropPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
    }

    private void loadResources(Resources res, @DrawableRes int iconBgId, @DrawableRes int iconBgDecorId) {
        if (iconBgId > 0) {
            mIconBg = BitmapFactory.decodeResource(res, iconBgId);
        }
        if (iconBgDecorId > 0) {
            mIconBgDecor = BitmapFactory.decodeResource(res, iconBgDecorId);
        }

        mCropPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
    }
}
