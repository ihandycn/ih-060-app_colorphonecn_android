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
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

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

    private Picture picture = new Picture();

    private Paint mPaint;
    private Paint mBitmapPaint;
    private Paint mAnimPaint;

    private Bitmap mSourceBitmap;
    private Bitmap mDotResultBitmap;
    private Bitmap mDotCropBitmap;

    private Canvas mBitmapCanvas;

    private ValueAnimator mAnimator;
    private float progress;

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

        // Animation
        mAnimator = ValueAnimator.ofFloat(0, 1).setDuration(1000);
        mAnimator.setInterpolator(new AccelerateInterpolator(3f));
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                progress = (float) animation.getAnimatedValue();
                setAlpha((int) (1 - progress) * 255);
                invalidate();
            }
        });
    }

    public void setSourceBitmap(Bitmap sourceBitmap) {
        HSLog.d("DigP", "setSourceBitmap");
        mSourceBitmap = sourceBitmap.copy(Bitmap.Config.ARGB_8888, true);
        mDotResultBitmap = Bitmap.createBitmap(mSourceBitmap.getWidth(), mSourceBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        mDotCropBitmap = Bitmap.createBitmap(mSourceBitmap.getWidth(), mSourceBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        // New picture
        mBitmapCanvas = new Canvas(mDotResultBitmap);
        doDrawDots(mBitmapCanvas);

        // Clear and reset
        mSourceBitmap.recycle();
        mBitmapCanvas = new Canvas(mDotCropBitmap);

        HSLog.d("DigP", "setSourceBitmap --end");
        mAnimator.start();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (mBitmapCanvas == null) {
            return;
        }

        long startMills = System.currentTimeMillis();
        mBitmapCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        HSLog.d("DigP", "draw end1 , duration " + (System.currentTimeMillis() - startMills));

        mAnimPaint.setStrokeWidth(Math.min(0.5f, progress) * maxStokeWidth);
        mBitmapCanvas.drawCircle(mDotResultBitmap.getWidth() * 0.5f,
                mDotResultBitmap.getHeight() * 0.5f,
                mDotResultBitmap.getHeight() * progress * 0.5f, mAnimPaint);
        HSLog.d("DigP", "draw end2 , duration " + (System.currentTimeMillis() - startMills));

        mBitmapCanvas.drawBitmap(mDotResultBitmap, 0, 0, mBitmapPaint);
        HSLog.d("DigP", "draw end3 , duration " + (System.currentTimeMillis() - startMills));

        canvas.drawBitmap(mDotCropBitmap, 0, 0, null);
        HSLog.d("DigP", "draw end , duration " + (System.currentTimeMillis() - startMills));
    }

    private void doDrawDots(Canvas canvas) {
        long startMills = System.currentTimeMillis();
        Log.d("DigP", "start draw : ");
        float tY = ballRadius;
        float tX = ballRadius;
        int w = canvas.getWidth();
        int h = canvas.getHeight();

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
        canvas.drawBitmap(mSourceBitmap, 0, 0, mBitmapPaint);
        HSLog.d("DigP", "drawBitmap end , duration " + (System.currentTimeMillis() - startMills));
    }
}
