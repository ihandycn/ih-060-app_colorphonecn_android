package com.colorphone.lock.util;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Debug;
import android.text.TextUtils;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;


public class DeviceUtils {
    private static final String TAG = "DeviceUtils";
    public static final int BYTE_PER_MB = 1024 * 1024;
    private static final String HUA_WEI_LOWER_CASE = "huawei";


    public static double getMemoryUsedPercent(Context context) {
        long memoryUnused = getMemoryUnused(context);
        long memoryTotal = getMemoryTotal();
        return (1 - ((double) memoryUnused / (double) memoryTotal));
    }

    public static int getMemoryUsedNumberPercent(Context context) {
        return (int) (DeviceUtils.getMemoryUsedPercent(context) * 100 + 0.5);
    }

    public static long getMemoryTotalMB(Context context) {
        long memoryTotal = getMemoryTotal() / 1024;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(memoryInfo);
        if (Build.VERSION.SDK_INT >= 16) {
            memoryTotal = memoryInfo.totalMem / BYTE_PER_MB;
        }
        return memoryTotal;
    }

    public static long getMemoryUnused(Context context) {
        long memoryUnused;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        memoryUnused = mi.availMem / 1024;
        return memoryUnused;
    }

    public static long getMemoryTotal() {
        long mTotal;
        String path = "/proc/meminfo";
        String content = null;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(path), 8);
            String line;
            if ((line = br.readLine()) != null) {
                content = line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        int begin = content.indexOf(':');
        int end = content.indexOf('k');
        content = content.substring(begin + 1, end).trim();
        mTotal = Integer.parseInt(content);
        return mTotal;
    }

    public static long getMemoryTotalSize() {
        String str1 = "/proc/meminfo";// 系统内存信息文件
        String str2;
        String[] arrayOfString;
        long initial_memory = 0;
        try {
            FileReader localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
            str2 = localBufferedReader.readLine();
            arrayOfString = str2.split("\\s+");
            initial_memory = Long.valueOf(arrayOfString[1]) * 1024;
            localBufferedReader.close();
        } catch (IOException ignored) {
        }
        return initial_memory;
    }

    public static long getMemoryAvailableSize() {
        ActivityManager am = (ActivityManager) HSApplication.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        return mi.availMem + getSelfMemoryUsed();
    }

    private static long getSelfMemoryUsed() {
        long memSize = 0;
        ActivityManager am = (ActivityManager) HSApplication.getContext().getSystemService(HSApplication.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo runningAPP : runningApps) {
            if (HSApplication.getContext().getPackageName().equals(runningAPP.processName)) {
                int[] pids = new int[]{runningAPP.pid};
                Debug.MemoryInfo[] memoryInfo = am.getProcessMemoryInfo(pids);
                memSize = memoryInfo[0].getTotalPss() * 1024;
                break;
            }
        }
        return memSize;
    }

    public static long getMemoryAvailMB(Context context) {
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(info);
        return info.availMem / BYTE_PER_MB;
    }

    public static float getWidthBy1080(float width1080) {
        return width1080 * CommonUtils.getPhoneWidth(HSApplication.getContext()) / 1080f;
    }

    public static float getHeightBy1920(float height1920) {
        return height1920 * CommonUtils.getPhoneHeight(HSApplication.getContext()) / 1920f;
    }

    public interface RunningAppsListener {
        void onScanFinished(List<String> list, long l);
    }


    public static boolean isHuaWeiDevice() {
        String brand = android.os.Build.BRAND;
        String manufacturer = android.os.Build.MANUFACTURER;
        String model = android.os.Build.MODEL; // "PE-TL10"
        HSLog.d(TAG, "isHuaWeiDevice brand = " + brand + " manufacturer = " + manufacturer + " model = " + model);
        return (!TextUtils.isEmpty(brand) && HUA_WEI_LOWER_CASE.equals(brand.toLowerCase())
                && !TextUtils.isEmpty(manufacturer) && HUA_WEI_LOWER_CASE.equals(manufacturer.toLowerCase()));
    }

}
