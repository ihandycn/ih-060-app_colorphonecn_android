package com.colorphone.lock.lockscreen;

public class LockScreensLifeCycleRegistry {

    private static boolean sIsChargingScreenActive;
    private static boolean sIsLockerActive;

    public static void setChargingScreenActive(boolean active) {
        sIsChargingScreenActive = active;
    }

    public static boolean isChargingScreenActive() {
        return sIsChargingScreenActive;
    }

    public static void setLockerActive(boolean active) {
        sIsLockerActive = active;
    }

    public static boolean isLockerActive() {
        return sIsLockerActive;
    }
}
