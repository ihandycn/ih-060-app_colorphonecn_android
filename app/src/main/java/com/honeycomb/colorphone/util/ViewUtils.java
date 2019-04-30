package com.honeycomb.colorphone.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;

import com.superapps.util.Dimensions;

public class ViewUtils {

    @SuppressWarnings("unchecked")
    public static <T extends View> T findViewById(Activity context, int id) {
        return (T) context.findViewById(id);
    }

    @SuppressWarnings("unchecked")
    public static <T extends View> T findViewById(View parentView, int id) {
        return (T) parentView.findViewById(id);
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

    public static Bitmap convertDrawable2BitmapByCanvas(Drawable drawable, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height,
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888: Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
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
}
