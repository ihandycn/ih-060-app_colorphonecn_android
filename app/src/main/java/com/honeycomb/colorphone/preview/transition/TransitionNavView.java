package com.honeycomb.colorphone.preview.transition;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;

import com.honeycomb.colorphone.preview.ThemePreviewView;
import com.superapps.util.Dimensions;

public class TransitionNavView extends SimpleTransitionView {

    public TransitionNavView(View v) {
        super(v);
    }

    @Override
    public void hide(boolean anim) {
        switchVisible(false, anim);
    }

    @Override
    public void show(boolean anim) {
        switchVisible(true, anim);
    }

    private void switchVisible(boolean show, boolean animation) {
        float offsetX = Dimensions.isRtl() ? -Dimensions.pxFromDp(60) : Dimensions.pxFromDp(60);
        float targetX = show ? 0 : -offsetX;
        View targetView = getTargetView();
        // State already right.
        if (Math.abs(targetView.getTranslationX() - targetX) <= 1) {
            return;
        }
        if (animation) {
            targetView.animate().translationX(targetX)
                    .setDuration(ThemePreviewView.ANIMATION_DURATION)
                    .setInterpolator(ThemePreviewView.mInter)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            if (show) {
                                targetView.setVisibility(View.VISIBLE);
                            }
                        }
                    })
                    .start();
        } else {
            targetView.setTranslationX(targetX);
        }
    }
}
