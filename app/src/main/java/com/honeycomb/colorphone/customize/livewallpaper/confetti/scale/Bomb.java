package com.honeycomb.colorphone.customize.livewallpaper.confetti.scale;

import android.support.annotation.Keep;
import android.view.animation.Interpolator;


@SuppressWarnings("unused")
@Keep
public class Bomb implements Interpolator {

    @Override
    public float getInterpolation(float input) {
        return (float) Math.sin(Math.PI / 2 * Math.sqrt(input));
    }
}
