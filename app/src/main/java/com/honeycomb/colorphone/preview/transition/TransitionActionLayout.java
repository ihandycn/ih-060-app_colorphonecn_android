package com.honeycomb.colorphone.preview.transition;

import android.view.View;

import com.honeycomb.colorphone.preview.ThemePreviewView;
import com.superapps.util.Dimensions;

public class TransitionActionLayout extends SimpleTransitionView {
    public TransitionActionLayout(View v) {
        super(v);
    }

    @Override
    public void hide(boolean anim) {
        View targetView = getTargetView();
        if (anim) {
            targetView.animate().alpha(0)
                    .setDuration(ThemePreviewView.ANIMATION_DURATION)
                    .setInterpolator(ThemePreviewView.mInter)
                    .setStartDelay(0)
                    .start();
        } else {
            targetView.setVisibility(View.GONE);
        }
    }

    @Override
    public void show(boolean anim) {
        View targetView = getTargetView();
        int mActionLayoutHeight = Dimensions.pxFromDp(60);
        targetView.setVisibility(View.VISIBLE);
        // Reset if view fade out before.
        targetView.setAlpha(1);
        if (anim) {
            targetView.setTranslationY(mActionLayoutHeight);
            targetView.animate().translationY(0)
                    .setDuration(ThemePreviewView.ANIMATION_DURATION)
                    .setInterpolator(ThemePreviewView.mInter)
                    .setStartDelay(1000).start();
        } else {
            targetView.setTranslationY(0);
        }
    }
}
