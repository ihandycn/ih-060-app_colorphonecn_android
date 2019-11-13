package com.honeycomb.colorphone.wallpaper.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

 import com.honeycomb.colorphone.R;
import com.superapps.util.Bitmaps;
import com.superapps.util.Dimensions;

public class FlashIcon extends View {
    private Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint iconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Bitmap circleBitmap;
    private Bitmap iconCopy;
    private Bitmap icon;

    private Canvas circleCanvas;

    private PorterDuffXfermode modeAtop = new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP);

    private Matrix matrix = new Matrix();
    private RectF rectF = new RectF();

    private AnimatorSet infiniteAnimators;

    private int width = Dimensions.pxFromDp(40);
    private int height = Dimensions.pxFromDp(40);

    private int circleRadius;
    private int iconOffset;

    private float startAngle;

    public FlashIcon(Context context, AttributeSet attrs) {
        super(context, attrs);

        setLayerType(LAYER_TYPE_SOFTWARE, null);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.flash);
        int iconRes = typedArray.getResourceId(R.styleable.flash_src, -1);
        icon = BitmapFactory.decodeResource(getResources(), iconRes);
        typedArray.recycle();

        float ringRough = Dimensions.pxFromDp(1.2f);
        matrix.setTranslate((width - icon.getWidth()) / 2, (height - icon.getHeight()) / 2);
        rectF.set(ringRough, ringRough, width - ringRough, height - ringRough);

        iconCopy = Bitmaps.tintBitmap(icon.copy(icon.getConfig(), true), Color.WHITE);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(ringRough);
        paint.setPathEffect(new DashPathEffect(new float[]{1f, 10}, 0));
        paint.setColor(0xB36F6F6F);

        circleBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        circleCanvas = new Canvas(circleBitmap);

        startAnim();
    }

    public void setIconOffset(int offset) {
        iconOffset = offset;
        matrix.setTranslate((width - icon.getWidth()) / 2, (height - icon.getHeight()) / 2 + offset);
    }

    public void startAnim() {
        ValueAnimator breatheAnim = ValueAnimator.ofInt(0xFF, 0x80);
        breatheAnim.setDuration(400).setInterpolator(new FastOutSlowInInterpolator());
        breatheAnim.setRepeatCount(ValueAnimator.INFINITE);
        breatheAnim.setRepeatMode(ValueAnimator.REVERSE);
        breatheAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                iconPaint.setAlpha((int) animation.getAnimatedValue());
                invalidate();
            }
        });

        ValueAnimator sweepAnim = ValueAnimator.ofFloat(0, 360);
        sweepAnim.setDuration(6000).setInterpolator(new LinearInterpolator());
        sweepAnim.setRepeatCount(ValueAnimator.INFINITE);
        sweepAnim.setRepeatMode(ValueAnimator.RESTART);
        sweepAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                startAngle = (float) animation.getAnimatedValue();
            }
        });

        infiniteAnimators = new AnimatorSet();
        infiniteAnimators.playTogether(breatheAnim, sweepAnim);
        infiniteAnimators.start();
    }

    public void endAnim(int color) {
        circlePaint.setColor(color);

        ValueAnimator circleScaleAnim = ValueAnimator.ofInt(0, getWidth() / 2);
        circleScaleAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                circleRadius = (int) animation.getAnimatedValue();
            }
        });

        ValueAnimator iconScaleAnim = ValueAnimator.ofFloat(1, 1.1f, 1);
        iconScaleAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                matrix.setScale((float) animation.getAnimatedValue(), (float) animation.getAnimatedValue(), width / 2, height / 2);
                matrix.postTranslate((width - icon.getWidth()) / 2, (height - icon.getHeight()) / 2 + iconOffset);
            }
        });

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(375).setInterpolator(new FastOutSlowInInterpolator());
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                infiniteAnimators.cancel();
            }
        });
        animatorSet.playTogether(circleScaleAnim, iconScaleAnim);
        animatorSet.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(icon, matrix, iconPaint);

        canvas.drawArc(rectF, startAngle, 359, false, paint);

        int layerId = canvas.saveLayer(0, 0, width, height, circlePaint, Canvas.ALL_SAVE_FLAG);

        circleCanvas.drawCircle(width / 2, height / 2, circleRadius, circlePaint);

        canvas.drawBitmap(iconCopy, matrix, circlePaint);

        circlePaint.setXfermode(modeAtop);

        canvas.drawBitmap(circleBitmap, 0, 0, circlePaint);

        circlePaint.setXfermode(null);

        canvas.restoreToCount(layerId);
    }
}
