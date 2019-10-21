package com.honeycomb.colorphone.wallpaper.livewallpaper.confetti.fade;

import android.support.annotation.Keep;


@SuppressWarnings({"unused", "WeakerAccess"})
@Keep
public class CubicEaseInSlow extends CubicEaseIn {

    @Override
    protected float getEaseInDurationFraction() {
        return 0.8f;
    }
}