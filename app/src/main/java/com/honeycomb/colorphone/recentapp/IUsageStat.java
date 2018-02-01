package com.honeycomb.colorphone.recentapp;

/**
 * Created by sundxing on 2018/2/1.
 */

public interface IUsageStat {
    int getLaunchCountByDays(int days);
    long getLastTimeUsed();
    String getPackageName();
}
