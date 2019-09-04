package com.honeycomb.colorphone.boost;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;

import com.honeycomb.colorphone.util.DeviceUtils;
import com.honeycomb.colorphone.util.FormatSizeBuilder;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Class for RAM usage.
 */
public class DeviceManager {


    private static final String TAG = DeviceManager.class.getSimpleName();

    private static final int CPU_TEMPERATURE_PATH_NOT_INIT = -1;
    private static final int CPU_TEMPERATURE_PATH_NOT_FOUND = -2;

    private static int sCpuTemperaturePathIndex = CPU_TEMPERATURE_PATH_NOT_INIT;

    private static final int CPU_TEMPERATURE_OFFSET_FROM_BATTERY = 5;
    private static final double CPU_TEMPERATURE_FALLBACK_BASE = 40.0;

    private static final long MEMORY_GOOD_LEVEL = 55;
    private static final long MEMORY_EMERGENCY_LEVEL = 80;

    private static final String[] CPU_TEMPERATURE_FILES = {
            "/sys/class/thermal/thermal_zone1/temp",
            "/sys/devices/virtual/thermal/thermal_zone1/temp",
            "/sys/devices/system/cpu/cpu0/cpufreq/cpu_temp",
            "/sys/devices/system/cpu/cpu0/cpufreq/FakeShmoo_cpu_temp",
            "/sys/class/i2c-adapter/i2c-4/4-004c/temperature",
            "/sys/devices/platform/tegra-i2c.3/i2c-4/4-004c/temperature",
            "/sys/devices/platform/omap/omap_temp_sensor.0/temperature",
            "/sys/devices/platform/tegra_tmon/temp1_input",
            "/sys/kernel/debug/tegra_thermal/temp_tj",
            "/sys/devices/platform/s5p-tmu/temperature",
            "/sys/class/hwmon/hwmon0/device/temp1_input",
            "/sys/devices/platform/s5p-tmu/curr_temp", "/sys/htc/cpu_temp",
            "/sys/devices/platform/tegra-i2c.3/i2c-4/4-004c/ext_temperature",
            "/sys/devices/platform/tegra-tsensor/tsensor_temperature"
    };

    private static final long BATTERY_DATA_REFRESH_DEBOUNCE_INTERVAL = 30 * DateUtils.SECOND_IN_MILLIS;
    private static final long CPU_TEMPERATURE_REFRESH_DEBOUNCE_INTERVAL = 30 * DateUtils.SECOND_IN_MILLIS;
    private static final long RUNNING_APPS_REFRESH_DEBOUNCE_INTERVAL = 30 * DateUtils.MINUTE_IN_MILLIS;

    private static volatile DeviceManager sManager = null;

    private IntentFilter mBatteryFilter;
    private Intent mBatteryData;
    private long mBatteryDataRefreshTime;

    private float mCpuTemperature;
    private long mCpuTemperatureRefreshTime;

    private long mRunningAppsRefreshTime;
    private static int runningApps = -1;
    private List<String> runningAppsPNs = new ArrayList<>();

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
        // Battery
        mBatteryFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

        // RAM
        mActivityManager = (ActivityManager) HSApplication.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        mMemoryInfo = new ActivityManager.MemoryInfo();
        mActivityManager.getMemoryInfo(mMemoryInfo);
    }

    private static final int VALUE_UNKNOWN = -1;

    private int mBatteryLevel = 100;
    private float mBatteryTemperature = VALUE_UNKNOWN;

    /**
     * @return Battery percentile.
     */
    public int getBatteryLevel() {
        Intent battery = refreshAndGetBatteryData();
        if (battery != null) {
            int rawLevel = battery.getIntExtra(BatteryManager.EXTRA_LEVEL, VALUE_UNKNOWN);
            int scale = battery.getIntExtra(BatteryManager.EXTRA_SCALE, VALUE_UNKNOWN);
            if (rawLevel >= 0 && scale > 0) {
                mBatteryLevel = Math.round((rawLevel * 100) / (float) scale);
            }
            HSLog.v(TAG, "Battery rawLevel: " + rawLevel + ", scale: " + scale + ", calculated level: " + mBatteryLevel);
        }
        HSLog.v(TAG, "Battery level: " + mBatteryLevel);
        return mBatteryLevel;
    }

    public boolean isCharging() {
        Intent battery = refreshAndGetBatteryData();
        if (battery != null) {
            int plugged = battery.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            return plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;
        }
        return false;
    }

    private float getBatteryTemperature() {
        Intent battery = refreshAndGetBatteryData();
        if (battery != null) {
            HSLog.v(TAG, "Extract battery temperature from battery data");
            mBatteryTemperature = battery.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, VALUE_UNKNOWN) / 10f;
        }
        HSLog.v(TAG, "Battery temperature: " + mBatteryTemperature);
        return mBatteryTemperature;
    }

    /**
     * Perform a debounced refresh and get battery data.
     *
     * @return {@link Intent} containing battery data. May be obsolete or {@code null} on failure.
     */
    private @Nullable Intent refreshAndGetBatteryData() {
        long now = SystemClock.elapsedRealtime();
        long timeSinceLastRefresh = now - mBatteryDataRefreshTime;
        HSLog.d(TAG, "refreshAndGetBatteryData(): " + timeSinceLastRefresh + " ms since last refresh");
        if (timeSinceLastRefresh > BATTERY_DATA_REFRESH_DEBOUNCE_INTERVAL) {
            try {
                mBatteryData = HSApplication.getContext().registerReceiver(null, mBatteryFilter);
                mBatteryDataRefreshTime = now;
                HSLog.v(TAG, "Battery data obtained from sticky broadcast: " + mBatteryData);
            } catch (Exception ignored) {
                HSLog.w(TAG, "Failed to get battery data");
            }
        }
        return mBatteryData;
    }

    /**
     * @return CPU temperature in degrees, celsius.
     */
    public float getCpuTemperatureCelsius() {
        long now = SystemClock.elapsedRealtime();
        long sinceLastFetch = now - mCpuTemperatureRefreshTime;
        HSLog.d(TAG, "getCpuTemperatureCelsius(): " + sinceLastFetch + " ms since last CPU temperature fetch");
        if (sinceLastFetch > CPU_TEMPERATURE_REFRESH_DEBOUNCE_INTERVAL) {
            mCpuTemperatureRefreshTime = now;
            mCpuTemperature = getCpuTemperatureCelsiusDebounced();
        }
        return mCpuTemperature;
    }

    private float getCpuTemperatureCelsiusDebounced() {
        // 如果没有初始化文件 index 则先进行初始化
        if (sCpuTemperaturePathIndex == CPU_TEMPERATURE_PATH_NOT_INIT) {
            initTempIndex();
        }

        // 没有获取到 index，直接通过电池温度进行计算
        if (sCpuTemperaturePathIndex == CPU_TEMPERATURE_PATH_NOT_FOUND) {
            return estimateCpuTemperatureFromBattery();
        }

        // 通过读文件获取 CPU 温度
        int result = readFileInt(CPU_TEMPERATURE_FILES[sCpuTemperaturePathIndex]);
        if (result <= 0) {
            return estimateCpuTemperatureFromBattery();
        }

        float temperature = (float) result;
        while (temperature >= 100) {
            temperature *= 0.1f;
        }

        return temperature;
    }

    private void initTempIndex() {
        for (sCpuTemperaturePathIndex = 0;
             sCpuTemperaturePathIndex < CPU_TEMPERATURE_FILES.length;
             sCpuTemperaturePathIndex++) {
            if (readFileInt(CPU_TEMPERATURE_FILES[sCpuTemperaturePathIndex]) <= 0) {
                break;
            }
        }
        if (sCpuTemperaturePathIndex >= CPU_TEMPERATURE_FILES.length) {
            sCpuTemperaturePathIndex = CPU_TEMPERATURE_PATH_NOT_FOUND;
            HSLog.w(TAG, "Failed to get CPU temperature file index");
        }
    }

    private float estimateCpuTemperatureFromBattery() {
        Random generator = new Random();

        float batteryTemperature = getBatteryTemperature();
        if (batteryTemperature > 0) {
            return batteryTemperature + CPU_TEMPERATURE_OFFSET_FROM_BATTERY + generator.nextFloat() - generator.nextFloat() * 2;
        } else {
            return (float) CPU_TEMPERATURE_FALLBACK_BASE + generator.nextFloat() * 3 - generator.nextFloat() * 5;
        }
    }

    private int readFileInt(String path) {
        File file = new File(path);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            return Integer.parseInt(reader.readLine());
        } catch (IOException | NumberFormatException e) {
            HSLog.i(TAG, "Failed to read temperature from file " + path);
        } finally {
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }
        return -1;
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
        int usage = 100 - Math.round(100f * mMemoryInfo.availMem / mMemoryInfo.totalMem);

        if (usage <= 5 || usage > 100) {
            // It's inferred that sometimes {@link ActivityManager#getMemoryInfo()} could give absurd result.
            // Default to 60 in that case.
            return 60;
        }
        return usage;
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

    /**
     * @return Total RAM size in byte.
     */
    public long getTotalRam() {
        return mMemoryInfo.totalMem;
    }

    public long getAvailRam() {
        return mMemoryInfo.availMem;
    }

    public String getJunkSize() {
        float[] defaultValue = new float[] { 40.1f, 60.8f, 278, 564, 1174f };
        Random random = new Random();
        int seed = random.nextInt(defaultValue.length);
        float selectSize = defaultValue[seed] * (1 + (random.nextInt(11) - 5) / 100f);
        long junkSize = (long) (selectSize * (1 << 20));
        FormatSizeBuilder builder = new FormatSizeBuilder(junkSize);

        return builder.sizeUnit;
    }

    public int getJunkSizeMb() {
        float[] defaultValue = new float[] { 40.1f, 60.8f, 278, 564, 1174f };
        Random random = new Random();
        int seed = random.nextInt(defaultValue.length);
        float selectSize = defaultValue[seed] * (1 + (random.nextInt(11) - 5) / 100f);
        return (int) selectSize;
    }

    public interface OnMemoryScanListener {
        void onMemoryScanned();
    }

    public void checkRunningApps(OnMemoryScanListener listener) {
        long now = SystemClock.elapsedRealtime();
        long sinceLastFetch = now - mRunningAppsRefreshTime;
        HSLog.d(TAG, "checkRunningApps(): " + sinceLastFetch + " ms since last Running Apps fetch");
        if (sinceLastFetch > RUNNING_APPS_REFRESH_DEBOUNCE_INTERVAL || runningApps <= 0 || isRandomRunningApps) {
            runningApps = -1;
            HSLog.d(TAG, "getRunningPackageListFromMemory ");

            DeviceUtils.getRunningPackageListFromMemory(false, (list, l) -> {
                runningAppsPNs.clear();
                runningApps = list.size();
                isRandomRunningApps = false;
                HSLog.d(TAG, "onScanFinished appSize == " + runningApps);
                runningAppsPNs.addAll(list);

                if (runningApps == 0) {
                    setRunningAppsRandom();
                }

                mRunningAppsRefreshTime = SystemClock.elapsedRealtime();

                if (listener != null) {
                    listener.onMemoryScanned();
                }
            });
        } else {
            if (listener != null) {
                listener.onMemoryScanned();
            }
        }
    }

    public int getRunningApps() {
        return runningApps;
    }

    private boolean isRandomRunningApps = false;
    public void setRunningAppsRandom() {
        if (isRandomRunningApps || runningApps <= 0) {
            Random random = new Random();
            runningApps = random.nextInt(10) + 3;
            isRandomRunningApps = true;
        }
    }

    public List<String> getRunningAppsPackageNames() {
        return runningAppsPNs;
    }
}
