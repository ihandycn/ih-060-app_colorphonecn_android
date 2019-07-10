package com.honeycomb.colorphone.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;

import com.ihs.commons.utils.HSLog;
import com.superapps.BuildConfig;
import com.superapps.util.Dimensions;

/**
 * @author sundxing
 */
public class DotsPictureView extends View {

    private static boolean DEBUG_LOG = false && BuildConfig.DEBUG;

    private int ballRadius;
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
        ballRadius = Dimensions.pxFromDp(2);

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
     *
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

    public void setSourceBitmap(Bitmap sourceBitmap) {
        HSLog.d("DigP", "setSourceBitmap");
        mSourceBitmap = sourceBitmap.copy(Bitmap.Config.ARGB_8888, true);

        HSLog.d("DigP", "createScaledBitmap");
        Bitmap bitmap = Bitmap.createScaledBitmap(mSourceBitmap,
                (int) Math.ceil(mSourceBitmap.getWidth() * 0.1),
                (int) Math.ceil(mSourceBitmap.getHeight() * 0.1),
                false);

        int darkPercent = darkPercent(bitmap, 30);
        HSLog.d("DigP", "createScaledBitmap--end , darkPercent = " + darkPercent);

        needLight = darkPercent > 40;

        mDotResultBitmap = Bitmap.createBitmap(mSourceBitmap.getWidth(), mSourceBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        mDotCropBitmap = Bitmap.createBitmap(mSourceBitmap.getWidth(), mSourceBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        // New picture
        mBitmapCanvas = new Canvas(mDotResultBitmap);
        doDrawDots(mBitmapCanvas);

        // Clear and reset
        mSourceBitmap.recycle();
        mBitmapCanvas = new Canvas(mDotCropBitmap);

        maxStokeWidth = (int) (mSourceBitmap.getHeight() * 0.1f);
        minStokeWidth = maxStokeWidth / 2;
        HSLog.d("DigP", "setSourceBitmap --end");
    }

    public boolean startAnimation() {
        if (!mAnimator.isStarted()) {
            mAnimator.start();
            return true;
        }
        return false;
    }

    public void pauseAnimation() {
        if (mAnimator.isRunning()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mAnimator.pause();
            } else {
                mAnimator.cancel();
            }
        }
    }

    public void resumeAnimation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (mAnimator.isPaused()) {
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
        super.onDetachedFromWindow();
    }

    float strokeWidth = 0;
    int alpha = 0;
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (mBitmapCanvas == null) {
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
        canvas.drawBitmap(mDotCropBitmap, 0, 0, mAlphaPaint);
        if (DEBUG_LOG) {
            HSLog.d("DigP", "draw end , duration " + (System.currentTimeMillis() - startMills));
        }
    }

    private void doDrawDots(Canvas canvas) {
        long startMills = System.currentTimeMillis();
        Log.d("DigP", "start draw : ");
        float tY = ballRadius;
        float tX = ballRadius;
        int w = canvas.getWidth();
        int h = canvas.getHeight();

        if (needLight) {
            mPaint.setColor(0x1affffff);
        }
        // Draw a line
        Canvas recodingCanvas = picture.beginRecording(w, h);
        while (tX < w) {
            recodingCanvas.drawCircle(tX, tY, ballRadius, mPaint);
            tX += 4 * ballRadius;
        }
        picture.endRecording();

        // Continue draw lines
        canvas.save();
        while (tY < h) {
            canvas.drawPicture(picture);
            tY += 4 * ballRadius;
            canvas.translate(0, 4 * ballRadius);
        }
        canvas.restore();

        HSLog.d("DigP", "start end , duration " + (System.currentTimeMillis() - startMills));

        // Get dots of picture
        if (!needLight) {
//            mBitmapPaint.setColorFilter(new LightingColorFilter(0xffffff, 0x1a1a1a));
            canvas.drawBitmap(mSourceBitmap, 0, 0, mBitmapPaint);
//            mBitmapPaint.setColorFilter(null);
        }

        HSLog.d("DigP", "drawBitmap end , duration " + (System.currentTimeMillis() - startMills));
    }
}
