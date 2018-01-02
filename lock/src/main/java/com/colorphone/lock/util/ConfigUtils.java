package com.colorphone.lock.util;

import android.content.pm.PackageManager;

import com.colorphone.lock.BuildConfig;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.inner.SessionMgr;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSMapUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ConfigUtils {
    public static final String APP_FIRST_INSTALL_TIME = "app_first_install_time";

    static SimpleDateFormat sSimpleDateFormat = new SimpleDateFormat("yyyy.MM.dd");

    public static boolean isEnabled(String... path) {
        boolean result = false;
        try {
            Map<String, ?> map = HSConfig.getMap(path);
            result = isEnabled(map);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                throw e;
            }
        }
        return result;
    }

    public static boolean isEnabled(Map<String, ?> map) {
        if (map != null && !map.isEmpty()) {
            String time = HSMapUtils.getString(map, "Time");
            boolean beforeEnable = HSMapUtils.getBoolean(map, "Before");
            boolean afterEnable = HSMapUtils.getBoolean(map, "After");

            long firstInstallTime = getAppFirstInstallTime();
            String firstInstallDate = sSimpleDateFormat.format(
                    firstInstallTime > 0 ? new Date(firstInstallTime) : new Date());
            boolean currentAfter = firstInstallDate.compareTo(time) >= 0;
            HSLog.d("Enable check install data : " + firstInstallDate + ", enableDateLine = " + time);
            return currentAfter ? afterEnable : beforeEnable;
        }
        return false;
    }

    public static long getAppFirstInstallTime() {
        long firstSessionTime = SessionMgr.getInstance().getFirstSessionStartTime();
        if (firstSessionTime <= 0) {
            return System.currentTimeMillis();
        }
        return firstSessionTime;
    }

    public static boolean isAnyLockerAppInstalled(String... path) {
        List<?> lockers = HSConfig.getList(path);
        for (Object item : lockers) {
            try {
                HSApplication.getContext().getPackageManager().getPackageInfo((String)item, 0);
                return true;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return false;

    }

}
