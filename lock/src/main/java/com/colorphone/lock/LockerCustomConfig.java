package com.colorphone.lock;

/**
 * Created by sundxing on 17/9/5.
 */

public class LockerCustomConfig {

    private static LockerCustomConfig INSTANCE = new LockerCustomConfig();
    private String mChargingExpressAdName;
    private String mSPFileName;
    private String mLockerAdName;
    private int mLauncherIcon;

    public static LockerCustomConfig get() {
        return INSTANCE;
    }

    public String getChargingExpressAdName() {
        return mChargingExpressAdName;
    }

    public void setChargingExpressAdName(String chargingExpressAdName) {
        //TODO
        mChargingExpressAdName = chargingExpressAdName;
    }

    public String getSPFileName() {
        return mSPFileName;
    }

    public void setSPFileName(String SPFileName) {
        mSPFileName = SPFileName;
    }

    public String getLockerAdName() {
        return mLockerAdName;
    }

    public void setLockerAdName(String lockerAdName) {
        mLockerAdName = lockerAdName;
    }

    public int getLauncherIcon() {
        return mLauncherIcon;
    }

    public void setLauncherIcon(int launcherIcon) {
        mLauncherIcon = launcherIcon;
    }
}
