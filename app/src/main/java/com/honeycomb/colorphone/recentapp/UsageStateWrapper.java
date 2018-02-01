package com.honeycomb.colorphone.recentapp;

import android.app.usage.UsageStats;
import android.os.Build;
import android.support.annotation.RequiresApi;

/**
 * Created by sundxing on 2018/2/1.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class UsageStateWrapper implements IUsageStat{

    private final UsageStats mUsageStats;
    private int mLaunchCount = -1;

    public UsageStateWrapper(UsageStats usageStats) {
        mUsageStats = usageStats;
    }

    @Override
    public int getLaunchCountByDays(int days) {
        if (mLaunchCount < 0) {
            mLaunchCount = UsageStatsUtil.getLaunchCountWithL(mUsageStats);
        }
        return mLaunchCount;
    }

    @Override
    public long getLastTimeUsed() {
       return mUsageStats.getLastTimeUsed();
    }

    @Override
    public String getPackageName() {
        return mUsageStats.getPackageName();
    }
}
