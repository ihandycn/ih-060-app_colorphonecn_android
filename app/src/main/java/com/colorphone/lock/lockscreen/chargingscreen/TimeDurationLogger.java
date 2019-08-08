package com.colorphone.lock.lockscreen.chargingscreen;

import com.colorphone.lock.LockerCustomConfig;
import com.superapps.util.rom.RomUtils;

public class TimeDurationLogger {

    private static long startTimeMills;
    private static String sNAME;
    public static void start(String name) {
        sNAME = name;
        startTimeMills = System.currentTimeMillis();
    }

    public static void stop() {
        long intervalMills = System.currentTimeMillis() - startTimeMills;
        if (intervalMills < 500) {
            return;
        }
        String romName = "Others";
        if (RomUtils.checkIsMiuiRom()) {
            romName = "Xiaomi";
        } else if (RomUtils.checkIsHuaweiRom()) {
            romName = "Huawei";
        }
        LockerCustomConfig.getLogger().logEvent(sNAME + "_StayTime" ,
                "RomName" , romName,
        "Time", getDurationFormatName(intervalMills));
    }

    private static String getDurationFormatName(long intervalMills) {
        long seconds = intervalMills / 1000;
        long num = seconds / 12;
        if (seconds < 1) {
            return "0-1s";
        } else if (seconds < 3) {
            return "1-3s";
        } else if (seconds < 6) {
            return "3-6s";
        } else if (seconds <= 12) {
            return "6-12s";
        } else if (num < 3) {
            return String.valueOf(num);
        } else if (num <= 5) {
            return "3-5";
        } else if (num <= 10) {
            return "5-10";
        } else if (num <= 20) {
            return "10-20";
        }  else {
            return "20+";
        }
    }
}
