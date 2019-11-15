package com.honeycomb.colorphone.wallpaper.livewallpaper.confetti.fade;

import android.support.annotation.Keep;
import android.view.animation.Interpolator;

@SuppressWarnings({"unused", "WeakerAccess"})
@Keep
public class CubicEaseIn implements Interpolator {

    protected float getMaxAlpha() {
        return 1f;
    }

    protected float getEaseInDurationFraction() {
        return 0.2f;
    }

    @Override
    public float getInterpolation(float input) {
        float maxAlpha = getMaxAlpha();
        float durationFraction = getEaseInDurationFraction();
        if (input < durationFraction) {
            float val = (durationFraction - input) / durationFraction;
            return maxAlpha * (1f - val * val * val);
        }
        return maxAlpha;
    }
}
