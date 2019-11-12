package com.honeycomb.colorphone.wallpaper.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import com.honeycomb.colorphone.wallpaper.animation.LauncherAnimUtils;


public class HoleLayerView extends View {

    private Bitmap mBackBitmap;
    private Bitmap mFrontBitmap;
    private Paint mPaint;
    private Paint mDigPaint;

    private int mWidth;
    private int mHeight;

    private Canvas mDigCanvas;
    private ValueAnimator mDigAnimator;
    private RectF mDigRectF;
    private float mAnimatedFraction;

    public HoleLayerView(Context context) {
        this(context, null);
    }

    public HoleLayerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HoleLayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        mDigPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        mDigPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        mDigRectF = new RectF();
        mDigAnimator = LauncherAnimUtils.ofFloat(this, 0.0f, 1.5f);
        mDigAnimator.setInterpolator(new AccelerateInterpolator());
        mDigAnimator.setDuration(580);
        mDigAnimator.setInterpolator(new AccelerateInterpolator());
        mDigAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAnimatedFraction = (float) animation.getAnimatedValue();
                digHoleOnBitmap();
                invalidate();
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBackBitmap != null && mFrontBitmap != null) {
            canvas.drawBitmap(mBackBitmap, 0, 0, mPaint);
            canvas.drawBitmap(mFrontBitmap, 0, 0, mPaint);
        }
    }

    public void startDiggingHole() {
        if (mDigAnimator.isRunning()) {
            mDigAnimator.cancel();
        }
        mDigAnimator.start();
    }

    public void setupView(View back, View front) {
        mWidth = back.getWidth() > front.getWidth() ? back.getWidth() : front.getWidth();
        mHeight = back.getHeight() > front.getHeight() ? back.getHeight() : front.getHeight();
        mBackBitmap = getBitmapByView(back);
        mFrontBitmap = getBitmapByView(front);
        mDigCanvas = new Canvas(mFrontBitmap);
        requestLayout();
    }

    private void digHoleOnBitmap() {
        float left = mWidth * (1 - mAnimatedFraction) / 2;
        float right = mWidth * (1 + mAnimatedFraction) / 2;
        float top = mHeight * (1 - mAnimatedFraction) / 2;
        float bottom = mHeight * (1 + mAnimatedFraction) / 2;
        mDigRectF.set(left, top, right, bottom);
        if (mDigCanvas != null) {
            mDigCanvas.drawOval(mDigRectF, mDigPaint);
        }
    }

    private Bitmap getBitmapByView(View targetView) {
        Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        if (targetView.getWidth() != mWidth) {
            canvas.translate((mWidth - targetView.getWidth()) / 2, 0);
        }
        targetView.draw(canvas);
        return bitmap;
    }
}
