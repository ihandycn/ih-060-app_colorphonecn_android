package com.honeycomb.colorphone.cashcenter;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Keep;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.honeycomb.colorphone.R;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.superappscommon.util.Dimensions;

public class HandAnimHelper {

    private static final float TRANS_XY_DP = 16;
    private AnimatorSet driveSpinAnimatorSet;
    private View hand;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private final Interpolator easeOut = PathInterpolatorCompat.create(
            0f, 0f, 0.7f, 1f);
    private final Interpolator easeIn = PathInterpolatorCompat.create(
            0.3f, 0f, 1f, 1f);

    public void init(View hand) {
        this.hand = hand;
        driveSpinAnimatorSet = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), R.animator.cash_hand_click);
        driveSpinAnimatorSet.setTarget(this);
        driveSpinAnimatorSet.addListener(new AnimatorListenerAdapter() {
            int count = 0;

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
//                if (count < 1) {
//                    driveSpinAnimatorSet.start();
//                    count++;
//                } else {
//                    hand.setVisibility(View.INVISIBLE);
//                }
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                count = 0;
            }
        });
        driveSpinAnimatorSet.setInterpolator(new LinearInterpolator());
    }

    public void start() {
        if (driveSpinAnimatorSet.isStarted()) {
            return;
        }
        hand.setVisibility(View.VISIBLE);
        driveSpinAnimatorSet.start();
        mHandler.removeCallbacksAndMessages(null);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                start();
            }
        }, 1800);
    }

    public void stop() {
        if (driveSpinAnimatorSet != null) {
            driveSpinAnimatorSet.removeAllListeners();
            driveSpinAnimatorSet.cancel();
        }
        mHandler.removeCallbacksAndMessages(null);

    }

    private Context getContext() {
        return HSApplication.getContext();
    }

    @Keep
    public void setHandScale(float process) {
        if (hand.getVisibility() != View.VISIBLE) {
            return;
        }
        if (process <= 120) {
            float start = 1.0f;
            float end = 0.85f;

            float proportion = easeOut.getInterpolation((process - 0) / 120);
            float scale = start + (end - start) * proportion;

            hand.setScaleX(scale);
            hand.setScaleY(scale);
        } else if (process <= 240) {
            float start = 0.85f;
            float end = 1.0f;

            float proportion = easeIn.getInterpolation((process - 120) / 120);
            float scale = start + (end - start) * proportion;

            hand.setScaleX(scale);
            hand.setScaleY(scale);
        } else if (process <= 340) {
            float start = 1.0f;
            float end = 1.0f;
            float scale = start + (end - start) * (process - 240) / 100; // always == 1.0f
            hand.setScaleX(scale);
            hand.setScaleY(scale);
        } else if (process <= 460) {
            float start = 1.0f;
            float end = 0.85f;

            float proportion = easeOut.getInterpolation((process - 340) / 120);
            float scale = start + (end - start) * proportion;

            hand.setScaleX(scale);
            hand.setScaleY(scale);
        } else {
            float start = 0.85f;
            float end = 1.0f;

            float proportion = easeIn.getInterpolation((process - 460) / 120);
            float scale = start + (end - start) * proportion;

            hand.setScaleX(scale);
            hand.setScaleY(scale);
        }
    }

    @Keep
    public void setHandTranslation(float process) {
        if (hand.getVisibility() != View.VISIBLE) {
            return;
        }
        float translation = 0;
        if (process <= 120) {
            float start = 0;
            float end = -Dimensions.pxFromDp(TRANS_XY_DP);

            float proportion = easeOut.getInterpolation((process - 0) / 120);
            translation = start + (end - start) * proportion;
        } else if (process <= 560) {
            float start = -Dimensions.pxFromDp(TRANS_XY_DP);
            float end = -Dimensions.pxFromDp(TRANS_XY_DP);

            translation = start + (end - start) * 0;
        } else {
            float start = -Dimensions.pxFromDp(TRANS_XY_DP);
            float end = 0;

            float proportion = easeIn.getInterpolation((process - 560) / 280);
            translation = start + (end - start) * proportion;
        }
        HSLog.d("HandAnimation", "TransX = " + translation);
        hand.setTranslationX(translation);
        hand.setTranslationY(translation);
    }
}
