package com.colorphone.lock.lockscreen.chargingscreen.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.LinearInterpolator;

import com.honeycomb.colorphone.R;
import com.superapps.util.Bitmaps;


/**
 * Author JackSparrow
 * Create Date 23/12/2016.
 */

public class FlashButton extends android.support.v7.widget.AppCompatButton {
    private Bitmap flashBitmap;
    private Bitmap flashBound;
    private Paint paint;

    private PorterDuffXfermode porterDuffXfermode;

    private float flashTranslation;
    private float flashLeft;

    private boolean enableFlash;
    private boolean blockFlash;

    private int repeatCount = 5;

    public FlashButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        flashBitmap = Bitmaps.drawable2Bitmap(context.getResources().getDrawable(R.drawable.button_flash));
        flashLeft = -flashBitmap.getWidth();

        porterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
    }

    public void startFlash() {
        if (enableFlash) {
            return;
        }

        enableFlash = true;

        final ValueAnimator flashAnim = ValueAnimator.ofFloat(0, 1);
        flashAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (animation.getAnimatedFraction() < (450 / (float) 1450)) {
                    flashLeft = -flashBitmap.getWidth() + flashTranslation * animation.getAnimatedFraction() * (1450 / (float) 450);
                    invalidate();
                }
            }
        });

        flashAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
                if (!enableFlash) {
                    animation.cancel();
                    return;
                }

                porterDuffXfermode = new PorterDuffXfermode(blockFlash ? PorterDuff.Mode.CLEAR : PorterDuff.Mode.SRC_IN);
            }
        });

        flashAnim.setRepeatCount(repeatCount);
        flashAnim.setRepeatMode(ValueAnimator.RESTART);
        flashAnim.setDuration(1450).setInterpolator(new LinearInterpolator());
        flashAnim.start();
    }

    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    public void stopFlash() {
        enableFlash = false;
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        if (width <= 0 || height <= 0) {
            return;
        }

        float scale = height / (float) flashBitmap.getHeight();

        //放大倍数过大会导致crash
        if (scale > 10) {
            return;
        }

        flashBitmap = getBitmap(flashBitmap, (int) (flashBitmap.getWidth() * scale), height);
        flashTranslation = width + 2 * flashBitmap.getWidth();

        flashBound = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas rectCanvas = new Canvas(flashBound);
        rectCanvas.drawRect(new RectF(0, 0, width, height), paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                blockFlash = true;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                blockFlash = false;
                break;
        }

        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (flashBound == null) {
            return;
        }

        int layerId = canvas.saveLayer(0, 0, getWidth(), getHeight(), paint, Canvas.ALL_SAVE_FLAG);

        canvas.drawBitmap(flashBitmap, flashLeft, 0, paint);

        paint.setXfermode(porterDuffXfermode);

        canvas.drawBitmap(flashBound, 0, 0, paint);

        paint.setXfermode(null);

        canvas.restoreToCount(layerId);
    }

    public static Bitmap getBitmap(Bitmap bitmap, int width, int height) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleW = (float) width / w;
        float scaleH = (float) height / h;
        matrix.postScale(scaleW, scaleH);
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
    }
}