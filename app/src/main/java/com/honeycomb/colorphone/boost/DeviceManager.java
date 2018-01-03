package com.honeycomb.colorphone.boost;

import android.app.ActivityManager;
import android.content.Context;

import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.framework.HSApplication;

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

    private static final long MEMORY_GOOD_LEVEL = 55;
    private static final long MEMORY_EMERGENCY_LEVEL = 80;

    public enum RAMStatus {
        EMERGENCY,
        NORMAL,
        GOOD,
    }

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
        if (Utils.ATLEAST_JELLY_BEAN) {
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


    public RAMStatus getRamStatus() {
        int ram = getRamUsage();

        if (ram >= MEMORY_EMERGENCY_LEVEL) {
            return RAMStatus.EMERGENCY;
        } else if (ram > MEMORY_GOOD_LEVEL) {
            return RAMStatus.NORMAL;
        } else {
            return RAMStatus.GOOD;
        }
    }

}
