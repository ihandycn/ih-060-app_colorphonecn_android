package com.honeycomb.colorphone.wallpaper.livewallpaper.confetti.scale;

import android.support.annotation.Keep;
import android.view.animation.Interpolator;

@SuppressWarnings("unused")
@Keep
public class LargeThenSmall implements Interpolator {

    @Override
    public float getInterpolation(float input) {
        if (input < 0.2f) {
            float val = (0.2f - input) / 0.2f;
            return 1f - val * val * val;
        }
        if (input < 0.9f) {
            return 1f;
        }
        if (input < 1.0f) {
            return 1f - (input - 0.9f) / (1f - 0.9f);
        }
        return 0f;
    }
}
