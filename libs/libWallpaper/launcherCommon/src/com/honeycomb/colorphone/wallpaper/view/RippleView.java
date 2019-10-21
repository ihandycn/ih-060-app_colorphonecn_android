package com.honeycomb.colorphone.wallpaper.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.ColorRes;
import android.util.AttributeSet;
import android.view.View;

 import com.honeycomb.colorphone.R;
import com.superapps.util.Dimensions;

public class RippleView extends View {

    private final int MIN_WIDTH = Dimensions.pxFromDp(30);
    private final int EXTRA_DISTANCE = 700;

    private int[] initColors;
    private int[] endColors;
    private Paint paint;
    private int radius;
    private int[] colorArray;
    private boolean isStartUpAnim = false;
    private boolean isAlreadyActionUp = true;
    private int alpha = 255;
    private boolean couldRunUpAnim = true;

    private float startX = -1;
    private float endX = -1;
    private ValueAnimator upAnimator;
    private ValueAnimator downAnimator;

    public RippleView(Context context) {
        this(context, null);
    }

    public RippleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RippleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        int middleColor = context.getResources().getColor(R.color.desktop_content_game_choice_wrong_middle_color);
        int edgeColor = context.getResources().getColor(R.color.desktop_content_game_choice_wrong_edge_color);
        initColors = new int[]{edgeColor, middleColor, edgeColor};
        endColors = new int[]{middleColor, middleColor};
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        radius = Dimensions.pxFromDp(7);
        colorArray = initColors;
    }

    public void setCouldRunUpAnim(boolean couldRunUpAnim) {
        this.couldRunUpAnim = couldRunUpAnim;
    }

    public void startUpAnim() {
        isAlreadyActionUp = true;
        if (downAnimator != null && downAnimator.isRunning()) {
            return;
        } else {
            upAnim();
        }
    }

    public void resetStatus() {
        isStartUpAnim = true;
        alpha = 0;
        invalidate();
    }

    public void setColors(@ColorRes int middleColorRes, @ColorRes int edgeColorRes) {
        Resources resources = getContext().getResources();
        int middleColor = resources.getColor(middleColorRes);
        int edgeColor = resources.getColor(edgeColorRes);
        initColors = new int[]{edgeColor, middleColor, edgeColor};
        endColors = new int[]{middleColor, middleColor};
    }

    public void cancelAnim() {
        if (downAnimator != null && downAnimator.isRunning()) {
            downAnimator.cancel();
        }
        if (upAnimator != null && upAnimator.isRunning()) {
            upAnimator.cancel();
        }
    }

    @Override
    @SuppressLint("DrawAllocation")
    protected void onDraw(Canvas canvas) {
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        float x1 = startX;
        float x2 = endX;
        if (x1 < 0) {
            x1 = 0;
        }
        if (x2 > width) {
            x2 = width;
        }
        if (isStartUpAnim) {
            paint.setAlpha(alpha);
            LinearGradient linearGradient = new LinearGradient(startX, 0, endX, 0, colorArray, null, Shader.TileMode.CLAMP);
            paint.setShader(linearGradient);
            RectF rf = new RectF(x1, 0, x2, height);
            canvas.drawRoundRect(rf, radius, radius, paint);
        } else if (startX != -1 && endX != -1) {
            paint.setAlpha(255);
            LinearGradient linearGradient = new LinearGradient(startX, 0, endX, 0, colorArray, null, Shader.TileMode.CLAMP);
            paint.setShader(linearGradient);
            RectF rectF = new RectF(x1, 0, x2, height);
            int r = 0;
            if (x1 < radius / 2 || x2 > (width - radius / 2)) {
                r = radius;
            }
            canvas.drawRoundRect(rectF, r, r, paint);
        }
        super.onDraw(canvas);
    }


    public void startDownAnim(float downX, int width) {
        isStartUpAnim = false;
        isAlreadyActionUp = false;
        if (downAnimator != null && downAnimator.isRunning()) {
            downAnimator.cancel();
        }
        if (upAnimator != null && upAnimator.isRunning()) {
            upAnimator.cancel();
        }

        downAnimator = ValueAnimator.ofFloat(downX, 0);
        downAnimator.addUpdateListener(animation -> {
            float x = (float) animation.getAnimatedValue();
            drawBg(downX, x, width);
        });

        downAnimator.addListener(new AnimatorListenerAdapter() {
            private boolean isCancel = false;

            @Override
            public void onAnimationStart(Animator animation) {
                colorArray = initColors;
                drawBg(downX, downX, width);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                isCancel = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isCancel) {
                    colorArray = endColors;
                    invalidate();
                    if (isAlreadyActionUp) {
                        upAnim();
                    }
                }
            }
        });

        downAnimator.setDuration(200);
        downAnimator.start();
    }

    private void drawBg(float downX, float x, int width) {
        float ratio = (downX - x) / downX;
        startX = downX - MIN_WIDTH / 2f - ratio * (downX + EXTRA_DISTANCE);
        endX = downX + MIN_WIDTH / 2f + ratio * (width - downX + EXTRA_DISTANCE);
        invalidate();
    }

    private void upAnim() {
        if (!couldRunUpAnim) {
            return;
        }
        isStartUpAnim = true;
        if (upAnimator != null && upAnimator.isRunning()) {
            upAnimator.cancel();
        }
        upAnimator = ValueAnimator.ofInt(255, 0);
        upAnimator.addUpdateListener(animation -> {
            alpha = (int) animation.getAnimatedValue();
            invalidate();
        });

        upAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                alpha = 0;
                invalidate();
            }
        });

        upAnimator.setDuration(200);
        upAnimator.start();
    }
}
