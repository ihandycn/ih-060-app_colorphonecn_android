package com.acb.libwallpaper.live.dialog;

public class BaseLauncherDialogButton {
    private int mColor;
    private int mButtonTextColor;
    private String mButtonText;
    private int mButtonRadius;

    public BaseLauncherDialogButton(int color, int textColor, String buttonText, int buttonRadius) {
        mColor = color;
        mButtonTextColor = textColor;
        mButtonText = buttonText;
        mButtonRadius = buttonRadius;
    }

    public int getColor() {
        return mColor;
    }

    public int getButtonTextColor() {
        return mButtonTextColor;
    }

    public String getButtonText() {
        return mButtonText;
    }

    public int getRippleColor() {
        return BaseLauncherDialog.adjustAlpha(mButtonTextColor,mColor);
    }

    public int getButtonRadius() {
        return mButtonRadius;
    }
}
