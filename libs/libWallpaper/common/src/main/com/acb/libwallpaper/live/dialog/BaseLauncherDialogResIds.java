package com.acb.libwallpaper.live.dialog;

public class BaseLauncherDialogResIds {
    private int mBackgroundColor;
    private int mLottieSet;
    private int mLottiePath;
    private int mLottieImgPath;
    private int mTopImage;
    private int mButtonColor;

    public BaseLauncherDialogResIds(int backgroundColor, int lottieSet, int lottiePath,
                                    int lottieImgPath, int topImage, int buttonColor) {
        this.mBackgroundColor = backgroundColor;
        this.mLottieSet = lottieSet;
        this.mLottiePath = lottiePath;
        this.mLottieImgPath = lottieImgPath;
        this.mTopImage = topImage;
        this.mButtonColor = buttonColor;
    }

    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    public int getLottieSet() {
        return mLottieSet;
    }

    public int getLottiePath() {
        return mLottiePath;
    }

    public int getLottieImgPath() {
        return mLottieImgPath;
    }

    public int getTopImage() {
        return mTopImage;
    }

    public int getButtonColor() {
        return mButtonColor;
    }
}
