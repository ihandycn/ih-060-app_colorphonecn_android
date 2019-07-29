package com.honeycomb.colorphone.customize.theme;

import android.support.v4.view.ViewPager;
import android.view.View;

public class RotateTransformer implements ViewPager.PageTransformer {

    private static final float MAX_ROTATE = 1f;

    @Override
    public void transformPage(View view, float position) {
        if (position < -1) {
            view.setRotation(MAX_ROTATE);
            view.setPivotX(view.getWidth());
            view.setPivotY(0);
        } else if (position <= 1) {
            if (position < 0) {
                view.setPivotX(view.getWidth() * (0.5f + 0.5f * (-position)));
                view.setPivotY(0);
                view.setRotation(-MAX_ROTATE * position);
            } else {
                view.setPivotX(view.getWidth() * 0.5f * (1 - position));
                view.setPivotY(0);
                view.setRotation(-MAX_ROTATE * position);
            }
        } else {
            view.setRotation(-MAX_ROTATE);
            view.setPivotY(0);
            view.setPivotX(0);
        }
    }
}
