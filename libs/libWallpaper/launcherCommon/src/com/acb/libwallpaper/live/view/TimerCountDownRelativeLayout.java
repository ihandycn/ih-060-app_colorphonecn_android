package com.acb.libwallpaper.live.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;

import com.acb.libwallpaper.R;
import com.superapps.util.Dimensions;
import com.superapps.view.TypefacedTextView;

public class TimerCountDownRelativeLayout extends RelativeLayout {

    private Paint paint;
    private int strokeWidth;
    private final int START_ANGLE = -90;
    private int angle = 0;
    private @ColorInt int bottomColor;
    private @ColorInt int topColor;
    private TypefacedTextView timeText;
    private TimeUpListener timeUpListener;
    private int currentSecond;
    private boolean isDrawTop;

    public TimerCountDownRelativeLayout(Context context) {
        this(context, null);
    }

    public TimerCountDownRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimerCountDownRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TimerCountDownRelativeLayout, 0, 0);
            strokeWidth = a.getDimensionPixelOffset(R.styleable.TimerCountDownRelativeLayout_stroke_width, Dimensions.pxFromDp(4));
            bottomColor = a.getColor(R.styleable.TimerCountDownRelativeLayout_bottom_color, context.getResources().getColor(R.color.white_alpha_40));
            topColor = a.getColor(R.styleable.TimerCountDownRelativeLayout_top_color, context.getResources().getColor(R.color.white));
            isDrawTop = a.getBoolean(R.styleable.TimerCountDownRelativeLayout_draw_top, false);
            a.recycle();
        } else {
            strokeWidth = Dimensions.pxFromDp(4);
            bottomColor = context.getResources().getColor(R.color.white_alpha_40);
            topColor = context.getResources().getColor(R.color.white);
            isDrawTop = false;
        }
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        if (isDrawTop) {
            angle = 360;
        } else {
            angle = 0;
        }
    }

    public void setColors(@ColorRes int bottomColor, @ColorRes int topColor) {
        this.bottomColor = getContext().getResources().getColor(bottomColor);
        this.topColor = getContext().getResources().getColor(topColor);
    }

    public void setTimeText(TypefacedTextView textView) {
        timeText = textView;
    }

    public void setTimeUpListener(TimeUpListener timeUpListener) {
        this.timeUpListener = timeUpListener;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int radius = Math.min(width, height) / 2 - strokeWidth / 2;
        int cx = width / 2;
        int cy = height / 2;
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        drawFirstRing(canvas, cx, cy, radius);
        drawSecondRing(canvas, cx, cy, radius);
        super.dispatchDraw(canvas);
    }

    private void drawFirstRing(Canvas canvas, int cx, int cy, int r) {
        paint.setColor(bottomColor);
        canvas.drawCircle(cx, cy, r, paint);
    }

    private void drawSecondRing(Canvas canvas, int cx, int cy, int r) {
        paint.setColor(topColor);
        RectF rectF = new RectF(cx - r, cy - r, cx + r, cy + r);
        canvas.drawArc(rectF, START_ANGLE, angle, false, paint);
    }

    public void startCountDown(int num) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(num, 0);
        valueAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            angle = (int) ((1 - (num - value) / (float) num) * 360);
            int second = (int) Math.ceil(value);
            if (second < 4) {
                startTextAnim(second);
            } else {
                timeText.setText(String.valueOf(second));
            }
            invalidate();
        });
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (timeUpListener != null) {
                    timeUpListener.timeUp();
                }
            }
        });
        valueAnimator.setDuration(num * 1000);
        valueAnimator.start();
    }

    public void startCountDown(float proportion, Runnable startAction, long duration) {
        int angleValue = (int) (proportion * 360);
        ValueAnimator valueAnimator = ValueAnimator.ofInt(0, angleValue);
        valueAnimator.addUpdateListener(animation -> {
            angle = (int) animation.getAnimatedValue();
            invalidate();
        });
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (startAction != null) {
                    startAction.run();
                }
            }
        });
        valueAnimator.setDuration(duration);
        valueAnimator.start();
    }

    private void startTextAnim(int second) {
        if (second == currentSecond) {
            return;
        }
        currentSecond = second;
        PropertyValuesHolder xScaleHolder = PropertyValuesHolder.ofFloat("scaleX", 1f, 1.6f, 1f);
        PropertyValuesHolder yScaleHolder = PropertyValuesHolder.ofFloat("scaleY", 1f, 1.6f, 1f);
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(timeText, xScaleHolder, yScaleHolder);
        animator.setDuration(400);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                timeText.setText(String.valueOf(second));
            }
        });
        animator.start();
    }

    public interface TimeUpListener {
        void timeUp();
    }
}
