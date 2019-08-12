package com.colorphone.lock.lockscreen.locker;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.colorphone.lock.ReflectionHelper;
import com.honeycomb.colorphone.R;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.Bitmaps;
import com.superapps.util.Dimensions;
import com.superapps.util.Threads;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class LockerUtils {

    public static boolean isIntentExist(Context context, Intent intent) {
        if (intent == null) {
            return false;
        }
        if (context.getPackageManager().resolveActivity(intent, 0) == null) {
            return false;
        }
        return true;
    }

    public static boolean isTouchInView(View view, MotionEvent event) {
        if (null == view) {
            return false;
        }

        Rect rect = new Rect();
        view.getDrawingRect(rect);

        int[] location = new int[2];
        view.getLocationOnScreen(location);

        RectF viewRectF = new RectF(rect);
        viewRectF.offset(location[0], location[1]);
        return viewRectF.contains(event.getRawX(), event.getRawY());
    }

    public static boolean hasPermission(String permission) {
        boolean granted = false;
        if (!TextUtils.isEmpty(permission)) {
            try {
                granted = ContextCompat.checkSelfPermission(HSApplication.getContext(), permission)
                        == PackageManager.PERMISSION_GRANTED;
            } catch (RuntimeException e) {}
        }
        return granted;
    }

    @SuppressWarnings("TryWithIdenticalCatches")
    public static boolean isKeyguardSecure(Context context, boolean defaultValue) {
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        try {
            Method declaredMethod = ReflectionHelper.getDeclaredMethod(KeyguardManager.class, "isKeyguardSecure");
            declaredMethod.setAccessible(true);
            defaultValue = (Boolean) declaredMethod.invoke(keyguardManager);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e2) {
            e2.printStackTrace();
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
        }
        return defaultValue;
    }

    public static final int WALLPAPER_BLUR_RADIUS = 8;

    public static void blurBitmapAsync(Context context, Bitmap bitmap, ImageView imageView) {
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }
        Threads.postOnThreadPoolExecutor(new Runnable() {
            @Override
            public void run() {
                final Bitmap bluredBitmap = LockerUtils.blurBitmap(context, bitmap, WALLPAPER_BLUR_RADIUS);
                Threads.postOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageBitmap(bluredBitmap);
                    }
                });
            }
        });
    }
    /**
     * Calculate the range of the toggle background, and return the blurred range.
     *
     * @return Blurred bitmap. Original bitmap for inputs with illegal dimensions.
     */
    public static Bitmap blurBitmap(Context context, Bitmap bitmap, int radius) {
        // resize the bitmap to fit sliding drawer
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width <= 0 || height <= 0) {
            return bitmap;
        }
        int startX;
        int startY;
        int rangeHeight;

        int rangeWidth = height * Dimensions.getPhoneWidth(context) / Dimensions.getPhoneHeight(context);
        if (rangeWidth <= width) {
            rangeHeight = height * context.getResources().getDimensionPixelOffset(R.dimen.locker_toggle_height)
                    / Dimensions.getPhoneHeight(context);
            startX = (width - rangeWidth) / 2;
            startY = height - rangeHeight;
        } else {
            rangeWidth = width;
            startX = 0;
            startY = (height + Dimensions.getPhoneHeight(context)) / 2
                    - context.getResources().getDimensionPixelOffset(R.dimen.locker_toggle_height);
            rangeHeight = context.getResources().getDimensionPixelOffset(R.dimen.locker_toggle_height);
        }
        if (Dimensions.hasNavBar(context)) {
            startY -= Dimensions.getNavigationBarHeight(context);
        }

        // Clamp the left & top of blurred area first
        if (startX < 0) startX = 0;
        if (startY < 0) startY = 0;

        // If left / top of blurred area is calculated to be out-of-bounds,
        // the input bitmap must be of illegal dimensions that we cannot blur.
        if (startX >= width || startY >= height) {
            return bitmap;
        }

        // Clamp the right & bottom of blurred area
        if (startX + rangeWidth > width) rangeWidth = width - startX;
        if (startY + rangeHeight > height) rangeHeight = height - startY;

        // Guard against negative size of blurred output
        if (rangeWidth <= 0) rangeWidth = 1;
        if (rangeHeight <= 0) rangeHeight = 1;

        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, startX, startY, rangeWidth, rangeHeight);

        // blur resized bitmap
        Bitmap blurSrcBitmap = Bitmap.createBitmap(Math.max(1, rangeWidth / 8), Math.max(1, rangeHeight / 8), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(blurSrcBitmap);
        canvas.scale(1 / 8.0f, 1 / 8.0f);
        Paint paint = new Paint();
        paint.setFlags(2);
        canvas.drawBitmap(resizedBitmap, 0.0f, 0.0f, paint);
        try {
            return Bitmaps.fastBlur(blurSrcBitmap, 1, radius);
        } catch (Exception e) {
            return resizedBitmap;
        }
    }
}
