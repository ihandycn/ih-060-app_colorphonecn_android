package com.honeycomb.colorphone.feedback;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.boost.SafeWindowManager;
import com.superapps.util.Dimensions;
import com.superapps.util.Threads;

public abstract class FloatedRateGuideDialog extends BaseRateGuideDialog {

    private boolean isClosing = false;
    private View contentView;

    public FloatedRateGuideDialog(Context context) {
        this(context, null);
    }

    public FloatedRateGuideDialog(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatedRateGuideDialog(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected int getLayoutResId() {
        return R.layout.oppo_rate_guide_layout;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isClosing) {
            return true;
        }
        isClosing = true;
        if (contentView != null) {
            long delay = 100;
            contentView.setAlpha(1);
            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(contentView, "alpha", 1f, 0f);
            alphaAnimator.setDuration(delay);
            alphaAnimator.start();
            Threads.postOnMainThreadDelayed(this::dismiss, delay);
        } else {
            dismiss();
        }
        return true;
    }

    @Override
    public void onAddedToWindow(SafeWindowManager windowManager) {
        contentView = findViewById(getRateGuideContent());
        contentView.setAlpha(0f);
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(contentView,"alpha",0f,1f);
        alphaAnimator.setStartDelay(40);
        alphaAnimator.setDuration(260);
        alphaAnimator.start();

        ObjectAnimator translationAnimator = ObjectAnimator.ofFloat(contentView,"translationY", Dimensions.pxFromDp(-48),0f);
        translationAnimator.setDuration(300);
        translationAnimator.setInterpolator(PathInterpolatorCompat.create(0.32f,0.99f,0.6f,1f));
        translationAnimator.start();

        ObjectAnimator floatAnimator = ObjectAnimator.ofFloat(contentView,"translationY",0f,Dimensions.pxFromDp(-8),0f);
        floatAnimator.setDuration(1000);
        floatAnimator.setStartDelay(300);
        floatAnimator.setRepeatCount(ValueAnimator.INFINITE);
        floatAnimator.setInterpolator(new LinearInterpolator());
        floatAnimator.start();
    }

    protected abstract int getRateGuideContent();
}
