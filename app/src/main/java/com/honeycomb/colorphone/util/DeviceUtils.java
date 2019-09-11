package com.honeycomb.colorphone.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import com.honeycomb.colorphone.PermanentService;
import com.honeycomb.colorphone.boost.ScanResultFilter;
import com.honeycomb.colorphone.notification.NotificationCondition;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.device.clean.memory.HSAppMemory;
import com.ihs.device.clean.memory.HSAppMemoryManager;

import java.util.ArrayList;
import java.util.List;

public class DeviceUtils {

    public interface RunningAppsListener {
        void onScanFinished(List<String> list, long l);
    }

    public static void getRunningPackageListFromMemory(final boolean isContainSystemApp, final RunningAppsListener runningAppsListener) {
        final List<String> packageNameList = new ArrayList<>();
        final long[] memSize = {0};
        HSAppMemoryManager.getInstance().startScanWithCompletedProgress(new HSAppMemoryManager.MemoryTaskListener() {

            @Override
            public void onSucceeded(List<HSAppMemory> list, long l) {
                if (null != runningAppsListener) {
                    HSLog.d(NotificationCondition.TAG, "getRunningPackageListFromMemory onSucceeded packageNameList = " + packageNameList);
                    runningAppsListener.onScanFinished(packageNameList, memSize[0]);
                }
            }

            @Override
            public void onFailed(int i, String s) {
                if (null != runningAppsListener) {
                    HSLog.d(NotificationCondition.TAG, "getRunningPackageListFromMemory onFailed packageNameList = " + packageNameList);
                    runningAppsListener.onScanFinished(packageNameList, memSize[0]);
                }
            }

            @Override
            public void onStarted() {

            }

            @Override
            public void onProgressUpdated(int processedCount, int total, HSAppMemory hsAppMemory) {
                if (null != hsAppMemory) {
                    String packageName = hsAppMemory.getPackageName();
                    HSLog.d(NotificationCondition.TAG,
                            "getRunningPackageListFromMemory processedCount = " + processedCount
                                    + " total = " + processedCount
                                    + "  size = " + (hsAppMemory.getSize() >>> 24)
                                    + " packageName = " + packageName);

                    String launcherPackageName = HSApplication.getContext().getPackageName();
                    boolean isSelf = false;
                    if (!TextUtils.isEmpty(launcherPackageName)) {
                        isSelf = launcherPackageName.equals(packageName);
                    }
                    ScanResultFilter filter = new ScanResultFilter();
                    Context context = HSApplication.getContext();
                    if (!TextUtils.isEmpty(packageName) && !isSelf) {
                        if (filter.filter(context, hsAppMemory)) {
                            if (!packageNameList.contains(packageName)) {
                                packageNameList.add(packageName);
                                memSize[0] += hsAppMemory.getSize();
                                HSLog.d(NotificationCondition.TAG,
                                        "add processedCount = " + processedCount
                                                + " total = " + processedCount
                                                + "  size = " + (hsAppMemory.getSize() >>> 24)
                                                + " packageName = " + packageName);
                            }
                        }
                    }
                }
            }
        });
    }

    public static void triggerRebirth(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Intent intent = new Intent(context, PermanentService.class);
            context.startService(intent);
        }
        
        if (context instanceof Activity) {
            ((Activity) context).finish();
        }

        Runtime.getRuntime().exit(0);
    }
}
