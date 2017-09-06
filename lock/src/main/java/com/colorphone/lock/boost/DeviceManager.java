package com.colorphone.lock.boost;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.support.annotation.Nullable;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * Class for RAM usage.
 */
public class DeviceManager {

    private static final String TAG = DeviceManager.class.getSimpleName();
    private static volatile DeviceManager sManager = null;

    private Intent mBatteryData;
    private IntentFilter mBatteryFilter;

    public static DeviceManager getInstance() {
        if (sManager == null) {
            synchronized (DeviceManager.class) {
                if (sManager == null) {
                    sManager = new DeviceManager();
                }
            }
        }
        return sManager;
    }

    private DeviceManager() {
        // RAM
        mActivityManager = (ActivityManager) HSApplication.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        mMemoryInfo = new ActivityManager.MemoryInfo();
        mActivityManager.getMemoryInfo(mMemoryInfo);

        // Battery
        mBatteryFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

    }

    private ActivityManager mActivityManager;
    private ActivityManager.MemoryInfo mMemoryInfo;

    /**
     * @return RAM usage percentile. Or 60 if no valid value could be fetched.
     */
    public int getRamUsage() {
        try {
            mActivityManager.getMemoryInfo(mMemoryInfo);
        } catch (SecurityException e) {
            return 60;
        }

        // Percentage can be calculated for API 16+
        int usage = 100 - Math.round(100f * mMemoryInfo.availMem / getTotalRam());

        if (usage <= 5 || usage > 100) {
            // It's inferred that sometimes {@link ActivityManager#getMemoryInfo()} could give absurd result.
            // Default to 60 in that case.
            return 60;
        }
        return usage;
    }

    /**
     * @return Total RAM size in byte.
     */
    public long getTotalRam() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return mMemoryInfo.totalMem;
        }
        try {
            File file = new File("/proc/meminfo");
            FileInputStream fis = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line = br.readLine();
            StringBuilder sb = new StringBuilder();
            for (char c : line.toCharArray()) {
                if (c >= '0' && c <= '9') {
                    sb.append(c);
                }
            }
            return Long.parseLong(sb.toString()) * 1024;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    /**
     * Perform a refresh and get battery data.
     *
     * @return {@link Intent} containing battery data. May be obsolete or {@code null} on failure.
     */
    private @Nullable
    Intent refreshAndGetBatteryData() {
        try {
            mBatteryData = HSApplication.getContext().registerReceiver(null, mBatteryFilter);
            HSLog.v(TAG, "Battery data obtained from sticky broadcast: " + mBatteryData);
        } catch (Exception ignored) {
            HSLog.w(TAG, "Failed to get battery data");
        }
        return mBatteryData;
    }

    public boolean isCharging() {
        Intent battery = refreshAndGetBatteryData();
        if (battery != null) {
            int plugged = battery.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            return plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;
        }
        return false;
    }

}
