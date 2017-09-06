package com.colorphone.lock.lockscreen.chargingscreen.tipview;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ToolTip {

    @IntDef({ANIMATOR_TYPE_NONE})
    @Retention(RetentionPolicy.SOURCE)
    private @interface AnimatorType {
    }

    public static final int ANIMATOR_TYPE_NONE = 2;

    private CharSequence charSequence;
    private int bgColor;
    private int textColor;

    @AnimatorType
    private int animationType;

    public ToolTip() {
        charSequence = null;
        bgColor = 0;
        animationType = ANIMATOR_TYPE_NONE;
    }

    public ToolTip withText(final CharSequence text) {
        charSequence = text;
        return this;
    }

    public ToolTip withColor(final int color) {
        bgColor = color;
        return this;
    }

    public ToolTip withTextColor(final int color) {
        textColor = color;
        return this;
    }

    public ToolTip withAnimationType(@AnimatorType int animationType) {
        this.animationType = animationType;
        return this;
    }

    public CharSequence getText() {
        return charSequence;
    }

    public int getColor() {
        return bgColor;
    }

    public int getTextColor() {
        return textColor;
    }

    @AnimatorType
    public int getAnimationType() {
        return animationType;
    }
}
