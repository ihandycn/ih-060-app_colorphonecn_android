package com.honeycomb.colorphone.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Picture;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.commons.utils.HSLog;
import com.superapps.BuildConfig;
import com.superapps.util.Dimensions;

import hugo.weaving.DebugLog;

/**
 * @author sundxing
 */
public class DotsPictureView extends View {

    private static boolean DEBUG_LOG = false && BuildConfig.DEBUG;
    private static float SHRINK_RATIO = 0.2f;

    private int ballRadius = DotsPictureResManager.ballRadius;
    private int maxStokeWidth;
    private int minStokeWidth;

    private Picture picture = new Picture();

    private Paint mPaint;
    private Paint mBitmapPaint;
    private Paint mAnimPaint;
    private Paint mAlphaPaint;

    private Bitmap mSourceBitmap;
    private Bitmap mDotResultBitmap;
    private Bitmap mDotCropBitmap;

    private Canvas mBitmapCanvas;

    private ValueAnimator mAnimator;
    private Interpolator pathInterpolator;
    private float progress;
    private float fraction;
    private boolean needLight;
    private Matrix mDrawMatrix;
    private int canvasSize;
    private BitmapPool mBitmapPool;

    private Path mDotsPath = new Path();
    /**
     * Bitmap from BitmapPool, back to pool
     */
    private boolean mDotsBitmapNeedReused;
    /**
     * Bitmap from outside, do nothing.
     */
    private boolean mDotsBitmapOutside;

    public DotsPictureView(Context context) {
        super(context);
        init();
    }

    public DotsPictureView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DotsPictureView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        maxStokeWidth = Dimensions.pxFromDp(80) * 2;

        mPaint = new Paint(Paint.DITHER_FLAG | Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.RED);

        mBitmapPaint = new Paint(Paint.DITHER_FLAG | Paint.ANTI_ALIAS_FLAG);
        mBitmapPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        mAnimPaint = new Paint(mPaint);
        mAnimPaint.setStyle(Paint.Style.STROKE);

        mAlphaPaint = new Paint(Paint.DITHER_FLAG | Paint.ANTI_ALIAS_FLAG);
        pathInterpolator = PathInterpolatorCompat.create(0.48f, 0.04f, 0.52f, 0.96f);

        // Animation
        mAnimator = ValueAnimator.ofFloat(0, 1).setDuration(1000);
        mAnimator.setInterpolator(pathInterpolator);
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                fraction = animation.getAnimatedFraction();
                progress = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
    }

    /**
     * @param bm
     * @return brightness value , larger than 128 is bright, otherwise is darker.
     */
    public int getBright(Bitmap bm) {
        if (bm == null) {
            return -1;
        }

        final int bitmapWidth = bm.getWidth();
        final int bitmapHeight = bm.getHeight();
        final int[] pixels = new int[bitmapWidth * bitmapHeight];
        bm.getPixels(pixels, 0, bitmapWidth, 0, 0, bitmapWidth, bitmapHeight);

        int r, g, b;
        int count = pixels.length;
        int bright = 0;
        for (int localTemp : pixels) {
            r = (localTemp | 0xff00ffff) >> 16 & 0x00ff;
            g = (localTemp | 0xffff00ff) >> 8 & 0x0000ff;
            b = (localTemp | 0xffffff00) & 0x0000ff;
            bright = (int) (bright + 0.299 * r + 0.587 * g + 0.114 * b);
        }
        return bright / count;
    }

    public int darkPercent(Bitmap bm, int brightnessValue) {
        if (bm == null) {
            return -1;
        }

        final int bitmapWidth = bm.getWidth();
        final int bitmapHeight = bm.getHeight();
        final int[] pixels = new int[bitmapWidth * bitmapHeight];
        bm.getPixels(pixels, 0, bitmapWidth, 0, 0, bitmapWidth, bitmapHeight);

        int r, g, b;
        int count = 0;
        int bright = 0;
        for (int localTemp : pixels) {
            r = (localTemp | 0xff00ffff) >> 16 & 0x00ff;
            g = (localTemp | 0xffff00ff) >> 8 & 0x0000ff;
            b = (localTemp | 0xffffff00) & 0x0000ff;
            bright = (int) (0.299 * r + 0.587 * g + 0.114 * b);
            if (bright < brightnessValue) {
                count++;
            }
        }
        return count * 100 / pixels.length;
    }

    public void setBitmapPool(BitmapPool bitmapPool) {
        mBitmapPool = bitmapPool;
    }

    public void setDotResultBitmap(Bitmap dotsResult) {
        mDotResultBitmap = dotsResult;
        mDotsBitmapOutside = true;
    }

    @DebugLog
    public void setSourceBitmap(@NonNull Bitmap sourceBitmap) {
        HSLog.d("DigP", "setSourceBitmap");

    }

    private boolean ensureBitmapCanvas() {
        HSLog.d("DigP", "ensureBitmapCanvas");

        if (mBitmapCanvas != null) {
            return true;
        }
        int oH;
        int oW;
        if (mDotResultBitmap == null) {
            // Create dots
            if (mSourceBitmap != null) {
                oH = mSourceBitmap.getHeight();
                oW = mSourceBitmap.getWidth();
                HSLog.d("DigP", "createScaledBitmap");
                Bitmap bitmap = Bitmap.createScaledBitmap(mSourceBitmap,
                        (int) Math.ceil(oW * SHRINK_RATIO),
                        (int) Math.ceil(oH * SHRINK_RATIO),
                        false);
                int darkPercent = darkPercent(bitmap, 30);
                mSourceBitmap = bitmap;
                HSLog.d("DigP", "createScaledBitmap--end , darkPercent = " + darkPercent);
                needLight = darkPercent > 40;
            } else {
                needLight = true;
                int[] thumbSize = Utils.getThumbnailImageSize();
                oW = thumbSize[0];
                oH = thumbSize[1];
            }
            mDotResultBitmap = makeBitmap(oW, oH, Bitmap.Config.ARGB_8888);
            mDotsBitmapNeedReused = true;
            HSLog.d("DigP", "mBitmapPool get = " + mDotResultBitmap);
            // New picture
            doDrawDots(new Canvas(mDotResultBitmap));
        } else {
            oH = mDotResultBitmap.getHeight();
            oW = mDotResultBitmap.getWidth();
        }

        mDotCropBitmap = makeBitmap(oW, oH, Bitmap.Config.ARGB_8888);
        HSLog.d("DigP", "mBitmapPool get2 = " + mDotCropBitmap);
        mBitmapCanvas = new Canvas(mDotCropBitmap);

        maxStokeWidth = (int) (oH * 0.1f);
        minStokeWidth = maxStokeWidth / 2;
        return true;
    }

    Bitmap makeBitmap(int width, int height, Bitmap.Config config) {
        if (mBitmapPool != null) {
            return mBitmapPool.get(width, height, config);
        }
        return Bitmap.createBitmap(width, height, config);
    }

    /**
     * get matrix for center-crop effect
     *
     * @param canvas view canvas
     */
    private void ensureDrawMatrix(Canvas canvas, int sWidth, int sHeight) {
        if (mDrawMatrix == null) {
            mDrawMatrix = new Matrix();
            mDrawMatrix.reset();
        }

        if (mDrawMatrix.isIdentity()) {

            int vwidth = canvas.getWidth();
            int vheight = canvas.getHeight();
            int dwidth = sWidth;
            int dheight = sHeight;

            float scale;
            float dx = 0, dy = 0;

            if (dwidth * vheight > vwidth * dheight) {
                scale = (float) vheight / (float) dheight;
                dx = (vwidth - dwidth * scale) * 0.5f;
            } else {
                scale = (float) vwidth / (float) dwidth;
                dy = (vheight - dheight * scale) * 0.5f;
            }

            mDrawMatrix.setScale(scale, scale);
            mDrawMatrix.postTranslate(Math.round(dx), Math.round(dy));
        }
    }

    public boolean startAnimation() {
        HSLog.d("DigP", "start animation");
        if (!mAnimator.isStarted()) {
            ensureBitmapCanvas();
            mAnimator.start();
            return true;
        }
        return false;
    }

    public void pauseAnimation() {
        HSLog.d("DigP", "pause animation");
        if (mAnimator.isRunning()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mAnimator.pause();
            } else {
                mAnimator.cancel();
            }
        }
    }

    public void resumeAnimation() {
        HSLog.d("DigP", "resume animation");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (mAnimator.isPaused()) {
                ensureBitmapCanvas();
                mAnimator.resume();
            }
        } else {
            startAnimation();
        }
    }

    public void stopAnimation() {
        mAnimator.cancel();
    }

    @Override
    protected void onDetachedFromWindow() {
        mAnimator.cancel();
        releaseBitmaps();
        super.onDetachedFromWindow();
    }

    public void releaseBitmaps() {
        mBitmapCanvas = null;
        if (mDotResultBitmap != null && !mDotResultBitmap.isRecycled()) {
            if (mDotsBitmapNeedReused && mBitmapPool != null) {
                // Put it back
                mBitmapPool.put(mDotResultBitmap);
                HSLog.d("DigP", "mBitmapPool put1 = " + mDotResultBitmap);

            } else if (mDotsBitmapOutside) {
                // Do nothing
            } else {
                mDotResultBitmap.recycle();
            }
            mDotResultBitmap = null;
        }

        if (mDotCropBitmap != null && !mDotCropBitmap.isRecycled()) {
            if (mBitmapPool != null) {
                mBitmapPool.put(mDotCropBitmap);
            } else {
                mDotCropBitmap.recycle();
            }
            HSLog.d("DigP", "mBitmapPool put2 = " + mDotCropBitmap);
            mDotCropBitmap = null;
        }
    }

    float strokeWidth = 0;
    int alpha = 0;

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (mBitmapCanvas == null || mDotResultBitmap == null || mDotResultBitmap.isRecycled()) {
            return;
        }
        if (fraction < 0.4f) {
            alpha = 255;
            strokeWidth = minStokeWidth +
                    (maxStokeWidth - minStokeWidth) * pathInterpolator.getInterpolation(fraction / 0.4f);
        } else {
            strokeWidth = maxStokeWidth -
                    maxStokeWidth * pathInterpolator.getInterpolation((fraction - 0.4f) / 0.6f);
            alpha = (int) (255 * Math.pow(1f - (fraction - 0.4f) / 0.6f, 3f));
        }

        if (DEBUG_LOG) {
            HSLog.d("DigP", "draw , fraction = " + fraction + ", progress = " + progress + ",strokeWidth = " + strokeWidth + ",alpha = " + alpha);
        }
        long startMills = System.currentTimeMillis();
        mBitmapCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        mAnimPaint.setStrokeWidth(strokeWidth);
        mBitmapCanvas.drawCircle(mDotResultBitmap.getWidth() * 0.5f,
                mDotResultBitmap.getHeight() * 0.5f,
                mDotResultBitmap.getHeight() * 1.3f * progress * 0.5f, mAnimPaint);

        mBitmapCanvas.drawBitmap(mDotResultBitmap, 0, 0, mBitmapPaint);

        mAlphaPaint.setAlpha(alpha);

        // Draw dots image into view canvas
        ensureDrawMatrix(canvas, mDotResultBitmap.getWidth(), mDotResultBitmap.getHeight());

        if (mDrawMatrix != null) {
            final int saveCount = canvas.getSaveCount();
            canvas.save();
            canvas.concat(mDrawMatrix);
            canvas.drawBitmap(mDotCropBitmap, 0, 0, mAlphaPaint);
            canvas.restoreToCount(saveCount);
        } else {
            canvas.drawBitmap(mDotCropBitmap, 0, 0, mAlphaPaint);
        }

        if (DEBUG_LOG) {
            HSLog.d("DigP", "draw end , duration " + (System.currentTimeMillis() - startMills));
        }
    }

    private void doDrawDots(Canvas canvas) {
        long startMills = System.currentTimeMillis();
        Log.d("DigP", "start draw : ");

        int w = canvas.getWidth();
        int h = canvas.getHeight();
        if (mSourceBitmap == null) {
            // Copy fail
            HSLog.d("DigP", "mSourceBitmap null, force light color");
            needLight = true;
        }

        if (needLight) {
            mPaint.setColor(0x1affffff);
        }
        mDotsPath = DotsPictureResManager.get().getDotsPath(w, h);
        canvas.drawPath(mDotsPath, mPaint);

        HSLog.d("DigP", "start end , duration " + (System.currentTimeMillis() - startMills));

        // Get dots of picture
        if (!needLight) {
            canvas.drawBitmap(mSourceBitmap, 0, 0, mBitmapPaint);
            mSourceBitmap.recycle();
        }

        HSLog.d("DigP", "drawBitmap end , duration " + (System.currentTimeMillis() - startMills));
    }

}
