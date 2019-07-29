package com.honeycomb.colorphone.customize.livewallpaper.guide;

import android.view.View;


public class RotationView implements RotationMaker.Callback {
    private View above;
    private View back;
    private float thick;

    private RotationMaker mRotationMaker;

    private final Runnable animationWork = new Runnable() {
        @Override
        public void run() {
            do3DAnimation();
        }
    };

    public RotationView(View above, View back, RotationMaker rotationMaker) {
        this.above = above;
        this.back = back;
        this.mRotationMaker = rotationMaker;
    }

    public void startRotate() {
        ensureRotationMaker();
        back.postDelayed(animationWork, 100);
    }

    private void ensureRotationMaker() {
        if (mRotationMaker == null) {
            mRotationMaker = new RotationMaker();
        }
    }

    public void endRotate() {
        back.removeCallbacks(animationWork);
        if (mRotationMaker != null ) {
            if (mRotationMaker.isRunning()) {
                mRotationMaker.cancel();
                mRotationMaker.removeCallback(this);
                mRotationMaker = null;
            }
        }
    }

    private void do3DAnimation() {
        above.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        back.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        thick = 0.04f * above.getHeight();
        mRotationMaker.addCallback(this);
        mRotationMaker.start();
    }

    private float getTrans(float thick, float angle) {
        return (float) (thick * Math.sin(angle * Math.PI / 180f));
    }

    @Override
    public void onRotateX(float angle) {
        above.setRotationX(angle);
        back.setRotationX(angle);
        back.setTranslationY(getTrans(thick, angle));
    }

    @Override
    public void onRotateY(float angle) {
        above.setRotationY(angle);
        back.setRotationY(angle);
        back.setTranslationX(-getTrans(thick, angle));
    }

    @Override
    public void onRotateEnd() {
        above.setLayerType(View.LAYER_TYPE_NONE, null);
        back.setLayerType(View.LAYER_TYPE_NONE, null);
    }
}