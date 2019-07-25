package com.honeycomb.colorphone.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.Dimensions;

public class DotsPictureResManager {
    private static final int MAX_BITMAP_COUNT = 2;
    private static final int BYTES_PER_ARGB_8888_PIXEL = 4;

    public static int ballRadius = Dimensions.pxFromDp(2);
    private static DotsPictureResManager INSTANCE = new DotsPictureResManager();

    private Path mPath;
    private Bitmap mDotResultBitmap;
    private BitmapPool mBitmapPool;

    public static DotsPictureResManager get() {
        return INSTANCE;
    }

    public Path getDotsPath(int w, int h) {
        if (mPath == null) {
            mPath = new Path();
            mPath.setFillType(Path.FillType.EVEN_ODD);
            float tY = ballRadius;
            float tX = ballRadius;
            // Draw a line
            Path pathLine = new Path();
            pathLine.setFillType(Path.FillType.EVEN_ODD);

            while (tX < w) {
                pathLine.addCircle(tX, tY, ballRadius, Path.Direction.CW);
                tX += 4 * ballRadius;
            }

            // Continue draw lines
            while (tY < h) {
                tY += 4 * ballRadius;
                mPath.addPath(pathLine, 0, tY);
            }
        }
        return mPath;
    }

    public Bitmap getDotsBitmap() {
        if (mDotResultBitmap == null) {
            int[] thumbSize = Utils.getThumbnailImageSize();
            int w = Math.min(1080, thumbSize[0] * 2);
            int h = Math.min(1920, thumbSize[1] * 2);
            mDotResultBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(mDotResultBitmap);
            Paint paint = new Paint(Paint.DITHER_FLAG | Paint.ANTI_ALIAS_FLAG);
            paint.setColor(0x1affffff);
            c.drawPath(getDotsPath(mDotResultBitmap.getWidth(), mDotResultBitmap.getHeight()), paint);
        }
        return mDotResultBitmap;
    }

    public void releaseDotsBitmap() {
        if (mDotResultBitmap != null) {
            mDotResultBitmap.recycle();
            mDotResultBitmap = null;
        }
        if (mBitmapPool != null) {
            mBitmapPool.clearMemory();
            mBitmapPool = null;
        }
    }

    public BitmapPool getBitmapPool() {
        if (mBitmapPool == null) {
            mBitmapPool = new LruBitmapPool(getPoolSize());
        }
        return mBitmapPool;
    }

    private int getPoolSize() {
        Context c = HSApplication.getContext();
        int bitmapCount = MAX_BITMAP_COUNT;
        return Dimensions.getPhoneHeight(c) * Dimensions.getPhoneWidth(c) * BYTES_PER_ARGB_8888_PIXEL * bitmapCount;
    }
}
