package com.honeycomb.colorphone.preview.transition;

import android.view.View;

import com.honeycomb.colorphone.preview.ThemePreviewView;

public class TransitionFadeView extends SimpleTransitionView {
    private long duration;

    public TransitionFadeView(View v, long duration) {
        super(v);
        this.duration = duration;
    }

    @Override
    public void hide(boolean anim) {
        View targetView = getTargetView();
        if (anim) {
            targetView.animate().alpha(0)
                    .setDuration(duration)
                    .setInterpolator(ThemePreviewView.mInter)
                    .start();
        } else {
            targetView.setVisibility(View.GONE);
        }
    }

    @Override
    public void show(boolean anim) {
        View targetView = getTargetView();
        targetView.setVisibility(View.VISIBLE);
        if (anim) {
            targetView.animate().alpha(1)
                    .setDuration(duration)
                    .setInterpolator(ThemePreviewView.mInter)
                    .start();
        } else {
            targetView.setAlpha(1);
        }
    }
}
