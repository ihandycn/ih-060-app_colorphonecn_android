package com.acb.libwallpaper.live.animation;

import android.animation.TypeEvaluator;

public class SpringEvaluator implements TypeEvaluator {

    private float mFrequencyFactor;
    private float mDampingFactor = 15;
    private float mAmplitudeFactor = 1;
    private float mOffsetCycle;

    public SpringEvaluator(float frequencyFactor, float dampingFactor, float amplitudeFactor, float offsetCycle) {
        mFrequencyFactor = frequencyFactor;
        mDampingFactor = dampingFactor;
        mAmplitudeFactor = amplitudeFactor;
        mOffsetCycle = offsetCycle;
    }

    @Override
    public Object evaluate(float fraction, Object startValue, Object endValue) {
        return (float) (Math.pow(2, -1 * mDampingFactor * fraction) *
                Math.sin((fraction - mFrequencyFactor / 4) * (2 * Math.PI) / mFrequencyFactor + mOffsetCycle * 2 * Math.PI) * mAmplitudeFactor + 1);
    }
}
