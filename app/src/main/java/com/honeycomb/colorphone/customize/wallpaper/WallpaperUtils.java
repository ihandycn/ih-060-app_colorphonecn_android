package com.honeycomb.colorphone.customize.wallpaper;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.call.assistant.util.CommonUtils;
import com.colorphone.lock.lockscreen.locker.Locker;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.customize.WallpaperInfo;
import com.honeycomb.colorphone.customize.util.CustomizeUtils;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Dimensions;
import com.superapps.util.Threads;

import java.io.File;
import java.io.FileOutputStream;

public class WallpaperUtils {

    public static final int WALLPAPER_TYPE_SCROLLABLE_STANDARD = 1;
    public static final int WALLPAPER_TYPE_STATIC_STANDARD = 2;

    private static final String TAG = WallpaperUtils.class.getSimpleName();

    private static final int COLOR_SAMPLE_COUNT = 6;

    public static Matrix centerCrop(int dWidth, int dHeight, ImageView imageView) {

        Matrix newMatrix = new Matrix();

        int vWidth = Dimensions.getPhoneWidth(HSApplication.getContext());
        int vHeight = Dimensions.getPhoneHeight(HSApplication.getContext());
        HSLog.i("centerCrop  screen  vWidth " + vWidth + " vHeight " + vHeight);
        float scale;
        float dx = 0f, dy = 0f;

        if (dWidth * vHeight > vWidth * dHeight) {
            scale = (float) vHeight / (float) dHeight;
            dx = (vWidth - dWidth * scale) * 0.5f;
        } else {
            scale = (float) vWidth / (float) dWidth;
            dy = (vHeight - dHeight * scale) * 0.5f;
        }

        HSLog.i("centerCrop  dWidth " + dWidth + " dHeight " + dHeight + " vWidth " + vWidth + " vHeight " + vHeight
                + " scale " + scale + " dx " + Math.round(dx) + " dy " + Math.round(dy));

        newMatrix.setScale(scale, scale);
        newMatrix.postTranslate(Math.round(dx), Math.round(dy));
        return newMatrix;
    }

    public static Matrix centerInside(int dWidth, int dHeight, int top, int bottom) {
        RectF bitmapRect = new RectF();
        int vWidth = Dimensions.getPhoneWidth(HSApplication.getContext());
        int vHeight = HSApplication.getContext().getResources().getDisplayMetrics().heightPixels;

        bitmapRect.set(0, 0, dWidth, dHeight);
        RectF imgRect = new RectF(0, top, vWidth, bottom);
        HSLog.i("centerInside  dWidth " + dWidth + " dHeight " + dHeight + " vWidth " + vWidth + " vHeight "
                + vHeight + " top " + top + " bottom " + bottom);

        Matrix matrix = new Matrix();
        matrix.setRectToRect(bitmapRect, imgRect, Matrix.ScaleToFit.CENTER);
        return matrix;
    }

    public static Bitmap centerInside(Bitmap src) {
        Rect bitmapRect = new Rect();
        Point point = getWindowSize(HSApplication.getContext());
        int vWidth = point.x;
        int vHeight = point.y;

        bitmapRect.set(0, 0, src.getWidth(), src.getHeight());
        Rect windowRect = new Rect(0, 0, vWidth, vHeight);
        HSLog.i("centerInside  dWidth " + src.getWidth() + " dHeight " + src.getHeight() + " vWidth " + vWidth + " vHeight " + vHeight);
        RectF windowRectF = new RectF(0, 0, vWidth, vHeight);
        RectF bitmapRectF = new RectF(bitmapRect);

        Matrix matrix = new Matrix();
        matrix.setRectToRect(windowRectF, bitmapRectF, Matrix.ScaleToFit.CENTER);
        matrix.mapRect(windowRectF);

        Bitmap bg = Bitmap.createBitmap(point.x, point.y, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bg);
        Paint paint = new Paint();

        bitmapRect.set((int) windowRectF.left, (int) windowRectF.top, (int) windowRectF.right, (int) windowRectF.bottom);

        canvas.drawBitmap(src, bitmapRect, windowRect, paint);
        return bg;
    }

    static Bitmap translateToFixedWallpaper(Bitmap src, Context context) {
        Point point = getWindowSize(context);
        if (src.getWidth() == point.x && src.getHeight() == point.y) {
            return src;
        } else {
            src = getSameRatioBitmap(src, point.x, point.y);
        }

        Bitmap bg = Bitmap.createBitmap(point.x, point.y, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bg);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        Rect dst = new Rect(0, 0, point.x, point.y);
        canvas.drawBitmap(src, null, dst, paint);
        return bg;
    }

    static Bitmap getSameRatioBitmap(Bitmap src, int screenWidth, int screenHeight) {
        Bitmap result;
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();

        float srcRatio = srcHeight / (float) srcWidth;
        float screenRatio = screenHeight / (float) screenWidth;
        if (srcRatio == screenRatio) {
            if (srcWidth > screenWidth) {
                float scale = screenWidth / (float) srcWidth;
                result = getScaledBitmap(src, scale);
            } else {
                result = src;
            }
        } else if (srcRatio < screenRatio) {
            float scale = screenWidth / (float) srcWidth;
            src = getScaledBitmap(src, scale);
            int h = src.getHeight();
            int w = (int) (h * (screenWidth / (float) screenHeight));
            int left = (src.getWidth() - w) / 2;
            Rect rect = new Rect(left, 0, left + w, h);
            result = getAppointedRectBitmap(src, rect, w, h);
        } else {
            float scale = screenHeight / (float) srcHeight;
            src = getScaledBitmap(src, scale);
            int w = src.getWidth();
            int h = (int) (w * (screenHeight / (float) screenWidth));
            int top = (src.getHeight() - h) / 2;
            Rect rect = new Rect(0, top, w, top + h);
            result = getAppointedRectBitmap(src, rect, w, h);
        }
        return result;
    }

    static Bitmap getScaledBitmap(Bitmap bitmap, float scale) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
    }

    static Bitmap getAppointedRectBitmap(Bitmap bitmap, Rect src, int width, int height) {
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        Rect dst = new Rect(0, 0, width, height);
        canvas.drawBitmap(bitmap, src, dst, paint);
        return result;
    }

    static Bitmap translateToScrollWallpaper(Bitmap src, Context context) {
        Point point = getWindowSize(context);
        if (src.getWidth() == 2 * point.x && src.getHeight() == point.y) {
            return src;
        }
        // avoid crash
        if (point.x <= 0 || point.y <= 0) {
            point.x = Dimensions.DEFAULT_DEVICE_SCREEN_WIDTH;
            point.y = Dimensions.DEFAULT_DEVICE_SCREEN_HEIGHT;
        }
        Bitmap bg = Bitmap.createBitmap(2 * point.x, point.y, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bg);
        PaintFlagsDrawFilter pfd = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        canvas.setDrawFilter(pfd);
        Rect dst = new Rect(0, 0, 2 * point.x, point.y);
        canvas.drawBitmap(src, null, dst, paint);
        return bg;
    }

    public static boolean canScroll(Context context, Bitmap wallpaper) {
        if (wallpaper == null || wallpaper.isRecycled()) {
            return false;
        }
        int width = wallpaper.getWidth();
        int height = wallpaper.getHeight();
        float wallpaperRatio = (float) width / (float) height;
        Point size = WallpaperUtils.getWindowSize(context);
        float windowRatio = (float) size.x / (float) size.y;
        float detla = wallpaperRatio / windowRatio;

        if (DebugMode.HELPER_DEBUG) {
            HSLog.i("wallpaper width " + width + " height " + height + " windowsSize width " + size.x + " height " + size.y);
            HSLog.i("wallpaper rate " + wallpaperRatio + " windowRatio " + windowRatio + " detla " + detla);
        }

        /**
         * 1.125 = 1440/1280 Our wallpaper's size is 1440*1280
         */
        if (Math.abs(detla - 2) <= 0.05 || Math.abs(wallpaperRatio - 1.125) <= 0.05) {
            return true;
        } else {
            return false;
        }
    }

    // TODO: 13/12/2016 remove this method

    /**
     * use Activity context
     */
    private static Point sCachePoint;

    public static Point getWindowSize(Context context) {
        Point point = new Point();
        if (sCachePoint != null) {
            point.set(sCachePoint.x, sCachePoint.y);
            return point;
        }
        int screenTotalHeight = 0;
        int screenTotalWidth = 0;
        if (context instanceof Activity) {
            View rootView = ((Activity) context).getWindow().getDecorView();
            screenTotalHeight = rootView.getHeight();
            screenTotalWidth = rootView.getWidth();
        }
        if (screenTotalWidth != 0 && screenTotalHeight != 0) {
            point.x = screenTotalWidth;
            point.y = screenTotalHeight;
            sCachePoint = new Point();
            sCachePoint.x = screenTotalWidth;
            sCachePoint.y = screenTotalHeight;
        } else {
            point.x = Dimensions.getPhoneWidth(context);
            point.y = Dimensions.getPhoneHeight(context);
        }
        return point;
    }

    public static final float WALLPAPER_SCREENS_SPAN = 2f;

    public static void suggestWallpaperDimension(Resources res,
                                                 WindowManager windowManager,
                                                 final WallpaperManagerProxy wallpaperManager, boolean fallBackToDefaults) {
        final Point defaultWallpaperSize = WallpaperUtils.getDefaultWallpaperSize(res, windowManager);

        int savedWidth;
        int savedHeight;

        if (!fallBackToDefaults) {
            return;
        } else {
            savedWidth = defaultWallpaperSize.x;
            savedHeight = defaultWallpaperSize.y;
        }

        if (savedWidth != wallpaperManager.getDesiredMinimumWidth() ||
                savedHeight != wallpaperManager.getDesiredMinimumHeight()) {
            wallpaperManager.suggestDesiredDimensions(savedWidth, savedHeight);
        }
    }

    /**
     * As a ratio of screen height, the total distance we want the parallax effect to span
     * horizontally
     */
    public static float wallpaperTravelToScreenWidthRatio(int width, int height) {
        float aspectRatio = width / (float) height;

        // At an aspect ratio of 16/10, the wallpaper parallax effect should span 1.5 * screen width
        // At an aspect ratio of 10/16, the wallpaper parallax effect should span 1.2 * screen width
        // We will use these two data points to extrapolate how much the wallpaper parallax effect
        // to span (ie travel) at any aspect ratio:

        final float ASPECT_RATIO_LANDSCAPE = 16 / 10f;
        final float ASPECT_RATIO_PORTRAIT = 10 / 16f;
        final float WALLPAPER_WIDTH_TO_SCREEN_RATIO_LANDSCAPE = 1.5f;
        final float WALLPAPER_WIDTH_TO_SCREEN_RATIO_PORTRAIT = 1.2f;

        // To find out the desired width at different aspect ratios, we use the following two
        // formulas, where the coefficient on x is the aspect ratio (width/height):
        //   (16/10)x + y = 1.5
        //   (10/16)x + y = 1.2
        // We solve for x and y and end up with a final formula:
        final float x =
                (WALLPAPER_WIDTH_TO_SCREEN_RATIO_LANDSCAPE - WALLPAPER_WIDTH_TO_SCREEN_RATIO_PORTRAIT) /
                        (ASPECT_RATIO_LANDSCAPE - ASPECT_RATIO_PORTRAIT);
        final float y = WALLPAPER_WIDTH_TO_SCREEN_RATIO_PORTRAIT - x * ASPECT_RATIO_PORTRAIT;
        return x * aspectRatio + y;
    }

    private static Point sDefaultWallpaperSize;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static Point getDefaultWallpaperSize(Resources res, WindowManager windowManager) {
        if (sDefaultWallpaperSize == null) {
            Point minDims = new Point();
            Point maxDims = new Point();
            windowManager.getDefaultDisplay().getCurrentSizeRange(minDims, maxDims);

            int maxDim = Math.max(maxDims.x, maxDims.y);
            int minDim = Math.max(minDims.x, minDims.y);

            if (CommonUtils.ATLEAST_JB_MR1) {
                Point realSize = new Point();
                windowManager.getDefaultDisplay().getRealSize(realSize);
                maxDim = Math.max(realSize.x, realSize.y);
                minDim = Math.min(realSize.x, realSize.y);
            }

            // We need to ensure that there is enough extra space in the wallpaper
            // for the intended parallax effects
            final int defaultWidth, defaultHeight;
            if (res.getConfiguration().smallestScreenWidthDp >= 720) {
                defaultWidth = (int) (maxDim * wallpaperTravelToScreenWidthRatio(maxDim, minDim));
                defaultHeight = maxDim;
            } else {
                defaultWidth = Math.max((int) (minDim * WALLPAPER_SCREENS_SPAN), maxDim);
                defaultHeight = maxDim;
            }
            sDefaultWallpaperSize = new Point(defaultWidth, defaultHeight);
        }
        return sDefaultWallpaperSize;
    }

    public static boolean textColorLightForWallPaper(Bitmap bitmap) {
        try {
            Palette palette = Palette.from(bitmap).clearFilters().maximumColorCount(COLOR_SAMPLE_COUNT).generate();
            return textColorLightForWallPaper(palette);
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    private static boolean textColorLightForWallPaper(Palette palette) {
        int size = palette.getSwatches().size();
        if (size == 0) {
            return true;
        }

        float max = 0;
        float min = 1;
        float lightTemp;

        float totalLightness = 0;
        long totalPop = 0;
        for (Palette.Swatch swatch : palette.getSwatches()) {
            HSLog.d("Palette", "WallPaper swatch: " + swatch);
            lightTemp = swatch.getHsl()[2];
            if (max < lightTemp) {
                max = lightTemp;
            }
            if (min > lightTemp) {
                min = lightTemp;
            }
            totalLightness += lightTemp * swatch.getPopulation();
            totalPop += swatch.getPopulation();
        }
        float lightness = totalLightness / (float) totalPop;
        HSLog.d("Palette", "WallPaper lightness average : " + lightness + ", max = " + max + ", min = " + min);

        // When the brightest swatch is bright enough, we loosen requirement for average lightness
        if (max > 0.89f && lightness > 0.57f) {
            return false;
        }
        if (max > 0.85f && lightness > 0.64f) {
            return false;
        }
        if (max > 0.80f && lightness > 0.71f) {
            return false;
        }

        // Basic threshold for average lightness
        return lightness < 0.74f;
    }

    public static boolean onlyEnableLockerWallpaper() {
        return true;
    }

    private static File getLockerWallpaperFile(String url) {
        File myDir = new File(HSApplication.getContext().getFilesDir() + "/locker_image");
        myDir.mkdirs();
        File file = new File(myDir, Utils.md5(url) + ".jpg");
        return file;
    }

    public static void saveAsLockerWallpaper(Bitmap bitmap, WallpaperInfo wallpaperInfo, String typeName) {
        final String url = wallpaperInfo.getSource();
        Threads.postOnThreadPoolExecutor(new Runnable() {
            @Override
            public void run() {
                if (!bitmap.isRecycled()) {
                    File file = getLockerWallpaperFile(url);
                    if (file.exists()) {
                        file.delete();
                    }
                    try {
                        FileOutputStream out = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        out.flush();
                        out.close();
                        // delete last
                        String oldPath = CustomizeUtils.getLockerWallpaperPath();
                        if (!TextUtils.isEmpty(oldPath)) {
                            File oldSettings = new File(oldPath);
                            oldSettings.delete();
                        }
                        CustomizeUtils.setVideoAudioStatus(CustomizeUtils.VIDEO_NO_AUDIO);
                        CustomizeUtils.setLockerWallpaperPath(file.getAbsolutePath());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Threads.postOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(HSApplication.getContext(), R.string.apply_success, Toast.LENGTH_LONG).show();
                            HSGlobalNotificationCenter.sendNotification(Locker.EVENT_WALLPAPER_CHANGE);
                            Analytics.logEvent(Analytics.upperFirstCh("wallpaper_detail_set_success"),
                                    "Type", typeName);
                        }
                    });
                }
            }
        });
    }
}
