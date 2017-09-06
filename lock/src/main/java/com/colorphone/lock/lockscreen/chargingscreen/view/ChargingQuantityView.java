package com.colorphone.lock.lockscreen.chargingscreen.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.util.AttributeSet;

import com.colorphone.lock.TypefacedTextView;


/**
 * Created by zhouzhenliang on 17/3/23.
 */

public class ChargingQuantityView extends TypefacedTextView {

    private static final String UNIT = "%";

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private PorterDuffXfermode porterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);

    private ValueAnimator valueAnimator;
    private float percent;
    private int textValue;

    private int upColor = 0xffffffff;
    private int bottomColor = 0xffffffff;

    public ChargingQuantityView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public void setMaskOpColor(int upColor, int bottomColor) {
        this.upColor = upColor;
        this.bottomColor = bottomColor;

        invalidate();
    }

    public void startFrontColorAnimator() {
        startFrontColorAnimator(textValue);
    }

    public void startFrontColorAnimator(final int value) {

        if (valueAnimator == null) {
            valueAnimator = ValueAnimator.ofFloat(0f, 1f);
            valueAnimator.setDuration(800);
            valueAnimator.setInterpolator(new FastOutSlowInInterpolator());

            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {

                    percent = (float) animation.getAnimatedValue() * (float) value / 99f;
                    invalidate();
                }
            });
        }

        if (valueAnimator.isRunning()) {
            return;
        }

        percent = 0;
        textValue = value;
        updatePercentColorTextView();

        valueAnimator.start();
    }

    public void setTextValue(final int value) {

        if (valueAnimator != null && valueAnimator.isRunning()) {
            valueAnimator.removeAllListeners();
            valueAnimator.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);

                    percent = (float) value / 99f;
                    textValue = value;
                    updatePercentColorTextView();

                    valueAnimator.removeAllListeners();
                }
            });

            return;
        }

        percent = (float) value / 99f;
        textValue = value;
        updatePercentColorTextView();
    }

    private void updatePercentColorTextView() {
        String valueString = String.valueOf(textValue);

        SpannableString spanString = new SpannableString(valueString + UNIT);
        spanString.setSpan(new AbsoluteSizeSpan(48, true), valueString.length(),
            valueString.length() + UNIT.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        setText(spanString);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isInEditMode()) {
            super.onDraw(canvas);

            return;
        }

        float top = getBaseline() + getPaint().getFontMetrics().ascent + getPaint().getFontMetrics().descent;
        float bottom = getBaseline();
        if (textValue <= 0 || textValue >= 100) {
            top = 0;
            bottom = getHeight();
        }

        final int upLayerId = canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), null, Canvas.ALL_SAVE_FLAG);
        super.onDraw(canvas);

        paint.setXfermode(porterDuffXfermode);
        paint.setColor(upColor);
        canvas.drawRect(0, 0, getWidth(), (bottom - top) * (1 - percent) + top, paint);
        paint.setColor(bottomColor);
        canvas.drawRect(0, (bottom - top) * (1 - percent) + top, getWidth(), getHeight(), paint);
        paint.setXfermode(null);

        canvas.restoreToCount(upLayerId);
    }

    private void init() {
        paint.setColor(0xffffffff);
        paint.setStyle(Paint.Style.FILL);
    }
}
