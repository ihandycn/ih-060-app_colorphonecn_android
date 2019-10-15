package com.acb.libwallpaper.live.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.ihs.commons.utils.HSLog;

/**
 * Created by zhewang on 22/03/2018.
 */

public class BitmapUtils {
    private static final String TAG = BitmapUtils.class.getSimpleName();
    /**
     * 把两个位图覆盖合成为一个位图，以底层位图的长宽为基准
     * @param backBitmap 在底部的位图
     * @param frontBitmap 盖在上面的位图
     * @return
     */
    public static Bitmap mergeBitmap(Bitmap backBitmap, Bitmap frontBitmap) {

        if (backBitmap == null || backBitmap.isRecycled()
                || frontBitmap == null || frontBitmap.isRecycled()) {
            HSLog.e(TAG, "backBitmap=" + backBitmap + ";frontBitmap=" + frontBitmap);
            return null;
        }
        Bitmap bitmap = backBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmap);
//        Rect baseRect  = new Rect(0, 0, backBitmap.getWidth(), backBitmap.getHeight());
//        Rect frontRect = new Rect(0, 0, frontBitmap.getWidth(), frontBitmap.getHeight());
        Rect minRect = new Rect(0, 0, Math.min(backBitmap.getWidth(), frontBitmap.getWidth()), Math.min(backBitmap.getHeight(), frontBitmap.getHeight()));
        canvas.drawBitmap(frontBitmap, minRect, minRect, null);
        return bitmap;
    }

}
