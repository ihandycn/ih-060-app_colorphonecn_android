package com.honeycomb.colorphone.wallpaper.livewallpaper.confetti.fade;

import android.support.annotation.Keep;


@SuppressWarnings("unused")
@Keep
public class CubicEaseInDimmed extends CubicEaseIn {

    @Override
    protected float getMaxAlpha() {
        return 0.7f;
    }
}
