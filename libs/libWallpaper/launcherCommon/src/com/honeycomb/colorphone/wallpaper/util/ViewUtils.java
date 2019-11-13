package com.honeycomb.colorphone.wallpaper.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.superapps.util.Dimensions;

public class ViewUtils {
    public static final int BASE_WIDTH = 0;
    public static final int BASE_HEIGHT = 1;
    public static final int DEFAULT_BASE = BASE_WIDTH;

    @SuppressWarnings("unchecked")
    public static <T extends View> T findViewById(Activity context, int id) {
        return (T) context.findViewById(id);
    }

    @SuppressWarnings("unchecked")
    public static <T extends View> T findViewById(View parentView, int id) {
        return (T) parentView.findViewById(id);
    }

    public static void rotateView(View view, float angle) {
        view.clearAnimation();
        view.setRotation(angle);
    }

    public static @NonNull Matrix centerCrop(@NonNull ImageView imageView) {
        Matrix newMatrix = new Matrix();
        if (imageView.getDrawable() == null) {
            return newMatrix;
        }

        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
        int dWidth = bitmapDrawable.getIntrinsicWidth();
        int dHeight = bitmapDrawable.getIntrinsicHeight();

        int vWidth = imageView.getWidth() - imageView.getPaddingLeft() - imageView.getPaddingRight();
        int vHeight = imageView.getHeight() - imageView.getPaddingTop() - imageView.getPaddingBottom();

        float scale;
        float dx = 0, dy = 0;

        if (dWidth * vHeight > vWidth * dHeight) {
            scale = (float) vHeight / (float) dHeight;
            dx = (vWidth - dWidth * scale) * 0.5f;
        } else {
            scale = (float) vWidth / (float) dWidth;
            dy = (vHeight - dHeight * scale) * 0.5f;
        }
        newMatrix.setScale(scale, scale);
        newMatrix.postTranslate(Math.round(dx), Math.round(dy));
        return newMatrix;
    }

    public static void setMargins(View v, int l, int t, int r, int b) {
        if (null == v) {
            return;
        }
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            boolean isRtl;
            if (v.isInEditMode()) {
                isRtl = false;
            } else {
                isRtl = Dimensions.isRtl();
            }
            p.setMargins(isRtl ? r : l, t, isRtl ? l : r, b);
            v.requestLayout();
        }
    }

    public static Rect getLocationRect(View view) {
        Rect location = new Rect();
        view.getGlobalVisibleRect(location);
        return location;
    }

    public static int getBackgroundColor(View view) {
        Drawable drawable = view.getBackground();
        if (drawable instanceof ColorDrawable) {
            ColorDrawable colorDrawable = (ColorDrawable) drawable;
            return colorDrawable.getColor();
        }
        return 0;
    }

    public static Bitmap getRotateAndScaleBitmap(Bitmap bitmap, float angle, float scaleX, float scaleY, float centerX, float centerY) {
        if (null == bitmap) {
            return null;
        }

        Matrix mat = new Matrix();
        mat.postRotate(angle, centerX, centerY);
        mat.postScale(scaleX, scaleY, centerX, centerY);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mat, true);
    }

    public static boolean ratio(int base, float ratio, int width, int height, MeasuredDimensionSetter setter) {
        if (ratio > 0f) {
            if (base == BASE_WIDTH) {
                height = (int) Math.ceil(width / ratio);
                setter.setMeasuredDimension(getMeasureSpec(width), getMeasureSpec(height));
            } else {
                width = (int) Math.ceil(height * ratio);
                setter.setMeasuredDimension(getMeasureSpec(width), getMeasureSpec(height));
            }
            return true;
        } else {
            return false;
        }
    }

    private static int getMeasureSpec(int size) {
        return View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY);
    }

    public interface MeasuredDimensionSetter {
        void setMeasuredDimension(int width, int height);
    }
}
