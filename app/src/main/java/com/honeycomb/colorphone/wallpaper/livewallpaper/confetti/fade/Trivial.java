package com.honeycomb.colorphone.wallpaper.livewallpaper.confetti.fade;

import android.support.annotation.Keep;
import android.view.animation.Interpolator;

@Keep
public class Trivial implements Interpolator {

    @Override
    public float getInterpolation(float input) {
        return 1f;
    }
}
