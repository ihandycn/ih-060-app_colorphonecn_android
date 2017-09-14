package com.honeycomb.colorphone.module;

/**
 * Created by sundxing on 17/9/14.
 */

public class Module {

    public static final int AD_NATIVE = 1;
    public static final int AD_EXPRESS = 2;

    private String mAdName;
    private String mNotifyKey;
    private int mAdType;
    private Checker mChecker;

    public String getAdName() {
        return mAdName;
    }

    public void setAdName(String adName) {
        mAdName = adName;
    }

    public int getAdType() {
        return mAdType;
    }

    public void setAdType(int adType) {
        mAdType = adType;
    }

    public Checker getChecker() {
        return mChecker;
    }

    public void setChecker(Checker checker) {
        mChecker = checker;
    }

    public String getNotifyKey() {
        return mNotifyKey;
    }

    public void setNotifyKey(String notifyKey) {
        mNotifyKey = notifyKey;
    }

    public interface Checker {
        boolean isEnable();
    }

}
