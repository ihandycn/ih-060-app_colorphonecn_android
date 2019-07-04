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
        long num = intervalMills / 12 / 1000;
        if (num <= 3) {
            return String.valueOf(num);
        } else if (num <= 5) {
            return "3-5";
        } else if (num <= 10) {
            return "5-10";
        } else if (num <= 20) {
            return "10-20";
        } else if (num <= 30) {
            return "30";
        } else {
            return "30+";
        }
    }
}
