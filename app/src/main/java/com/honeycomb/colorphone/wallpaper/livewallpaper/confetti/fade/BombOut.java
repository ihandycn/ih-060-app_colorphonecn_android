package com.honeycomb.colorphone.wallpaper.livewallpaper.confetti.fade;

import android.support.annotation.Keep;
import android.view.animation.Interpolator;


@SuppressWarnings("unused")
@Keep
public class BombOut implements Interpolator {

    @Override
    public float getInterpolation(float input) {
        return 1f - (float) Math.sin(Math.PI / 2 * Math.sqrt(input));
    }
}
