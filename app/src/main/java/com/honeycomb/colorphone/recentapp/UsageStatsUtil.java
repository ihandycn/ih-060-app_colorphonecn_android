package com.honeycomb.colorphone.recentapp;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.format.DateUtils;

import com.ihs.commons.utils.HSLog;

import java.lang.reflect.Field;
import java.util.List;

public class UsageStatsUtil {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static List<UsageStats> getUsageStatsList(Context context, int days) {
        if (null == context) return null;

        UsageStatsManager usm = getUsageStatsManager(context);

        long endTime = System.currentTimeMillis();
        long startTime = endTime - days * DateUtils.DAY_IN_MILLIS;

        final List<UsageStats> usageStatsList = usm.queryUsageStats(UsageStatsManager.INTERVAL_BEST, startTime, endTime);

        if (usageStatsList.isEmpty()) {
            HSLog.d("usage stats list is null");
        } else {
            HSLog.d("usage stats list size = " + usageStatsList.size());
        }
        return usageStatsList;
    }


    private static UsageStatsManager getUsageStatsManager(Context context){
        UsageStatsManager usm = (UsageStatsManager) context.getSystemService("usagestats");
        return usm;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static int getLaunchCountWithL(UsageStats stat) {
        long count = 0;
        try {
            Field field = stat.getClass().getDeclaredField("mLaunchCount");
            if (field.getType().equals(int.class)) {
                count = field.getInt(stat);
            } else if (field.getType().equals(long.class)) {
                count = field.getLong(stat);
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return (int)count;
    }
}