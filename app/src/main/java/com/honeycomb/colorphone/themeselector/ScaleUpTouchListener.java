package com.honeycomb.colorphone.themeselector;

import android.animation.Animator;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;

public class ScaleUpTouchListener implements View.OnTouchListener, Animator.AnimatorListener {

    private Interpolator mPathInterpolator1 = PathInterpolatorCompat.create(0f, 0f, 0.58f, 1.00f);
    private Interpolator mPathInterpolator2 = PathInterpolatorCompat.create( 0.42f, 0.00f, 1.00f, 1.00f);
    private boolean isScaleUp;
    private boolean isScaleDown;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!isScaleDown) {
                    isScaleDown = true;
                    isScaleUp = false;
                    v.animate().scaleX(0.94f).scaleY(0.94f).setDuration(200).setInterpolator(mPathInterpolator1).setListener(this).start();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (!isScaleUp) {
                    isScaleUp = true;
                    isScaleDown = false;
                    v.animate().scaleX(1f).scaleY(1f).setStartDelay(200).setInterpolator(mPathInterpolator2).start();
                }
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public void onAnimationStart(Animator animation) {

    }

    @Override
    public void onAnimationEnd(Animator animation) {
        isScaleUp = false;
        isScaleDown = false;
    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }
}
