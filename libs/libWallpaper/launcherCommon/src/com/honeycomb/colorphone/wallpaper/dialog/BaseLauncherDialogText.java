package com.honeycomb.colorphone.wallpaper.dialog;

public class BaseLauncherDialogText {

    private CharSequence mText;
    private int mColor;
    private float mAlpha;

    public BaseLauncherDialogText(CharSequence text, int color, float alpha) {
        mText = text;
        mColor = color;
        mAlpha = alpha;
    }

    public CharSequence getText() {
        return mText;
    }

    public float getAlpha() {
        return mAlpha;
    }

    public int getColor() {
        return mColor;
    }
}
