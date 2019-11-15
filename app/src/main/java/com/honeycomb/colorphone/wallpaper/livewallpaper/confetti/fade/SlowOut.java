package com.honeycomb.colorphone.wallpaper.livewallpaper.confetti.fade;

import android.support.annotation.Keep;
import android.view.animation.Interpolator;

@SuppressWarnings({"unused", "WeakerAccess"})
@Keep
public class SlowOut implements Interpolator {
    @Override public float getInterpolation(float input) {
        if (input < 0.5) {
            return 1;
        }
        return 1f - (float) Math.sqrt(Math.sqrt(Math.sqrt(input)));
    }
}
