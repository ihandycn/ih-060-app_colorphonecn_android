package com.honeycomb.colorphone.preview.transition;

import android.view.View;

public class SimpleTransitionView implements TransitionView {
    private View mTargetView;

    public SimpleTransitionView(View v) {
        mTargetView = v;
    }

    public View getTargetView() {
        return mTargetView;
    }

    @Override
    public void show(boolean anim) {
        mTargetView.setVisibility(View.VISIBLE);
    }

    @Override
    public void hide(boolean anim) {
        mTargetView.setVisibility(View.GONE);
    }
}