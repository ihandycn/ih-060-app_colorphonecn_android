package com.honeycomb.colorphone.recentapp;

import android.provider.BaseColumns;
import android.text.format.DateUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by sundxing on 2018/1/31.
 */

public final class AppUsage implements IUsageStat {
    private String mPackageName;
    private List<Long> mLaunchTimeRecords = new ArrayList<>();
    private long mRecentLaunchTime;

    public AppUsage() {
    }

    public AppUsage(String packageName, long recentLaunchTime) {
        mPackageName = packageName;
        setRecentLaunchTime(recentLaunchTime);
    }

    public int getTotalLaunchCount() {
        return mLaunchTimeRecords.size();
    }

    @Override
    public int getLaunchCountByDays(int days) {
        final long timeline = System.currentTimeMillis() - days * DateUtils.DAY_IN_MILLIS;
        int count = 0;
        for (Long timeRecord : mLaunchTimeRecords) {
            if (timeRecord > timeline) {
                count++;
            }
        }
        return count;
    }

    public void setRecentLaunchTime(long recentLaunchTime) {
        boolean changed = recentLaunchTime != mRecentLaunchTime;
        if (changed) {
            mRecentLaunchTime = recentLaunchTime;
            addLaunchTimeRecord(recentLaunchTime);
        }
    }

    private void addLaunchTimeRecord(long launchTime) {
        mLaunchTimeRecords.add(launchTime);
    }

    @Override
    public long getLastTimeUsed() {
        return mRecentLaunchTime;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public void setPackageName(String packageName) {
        mPackageName = packageName;
    }

    /* Inner class that defines the table contents */
    public static class AppUsageEntry implements BaseColumns {
        public static final String TABLE_NAME = "entry";
        public static final String COLUMN_NAME_LAUNCHTIME_LAST = "last_launch_time";
        public static final String COLUMN_NAME_PACKAGE_NAME = "package_name";
    }

}
