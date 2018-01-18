package com.honeycomb.colorphone.view;

import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.StateSet;

/**
 * If setColorFilter is applied to a drawable inside a StateListDrawable, it can't make sense.
 * So a class extends StateListDrawable is needed to setColorFilter to StateListDrawable itself.
 */
public class SelectorDrawable extends StateListDrawable {

    public SelectorDrawable(Drawable drawable) {
        super();

        addState(StateSet.WILD_CARD, drawable);
    }

    @Override
    protected boolean onStateChange(int[] stateSet) {
        boolean isClicked = false;
        for (int state : stateSet) {
            if (state == android.R.attr.state_pressed) {
                isClicked = true;
            }
        }
        if (isClicked) {
            setColorFilter(new LightingColorFilter(Color.LTGRAY, Color.TRANSPARENT));
        } else {
            clearColorFilter();
        }

        return super.onStateChange(stateSet);
    }
}