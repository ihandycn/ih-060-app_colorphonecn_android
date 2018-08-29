package com.honeycomb.colorphone.cpucooler.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.cpucooler.CpuCoolDownActivity;
import com.honeycomb.colorphone.cpucooler.CpuCoolerManager;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CpuCoolerUtils {

    public static List<String> getLauncherAppList() {
        PackageManager packageManager = HSApplication.getContext().getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

        List<String> launcherAppList = new ArrayList<>();
        for (ResolveInfo resolveInfo : resolveInfoList) {
            launcherAppList.add(resolveInfo.activityInfo.packageName);
        }

        return launcherAppList;
    }

    public static final String MODULE_NAME = "optimizer_cpu_cooler";

    private static final int CPU_TEMPERATURE_LEVEL_COOL = 30;
    private static final int CPU_TEMPERATURE_LEVEL_OVERHEATED = 40;

    private static final int[] CPU_STATE_COLORS = {0xFF4285F4, 0xFFFFBE00, 0xFFF44336};

    public static HSPreferenceHelper preferenceHelper() {
        return HSPreferenceHelper.create(HSApplication.getContext(), MODULE_NAME);
    }


    public static int getCpuTemperatureColor() {
        float cpuTemperature = CpuCoolerManager.getInstance().getCachedCpuTemperature();

        if (cpuTemperature < CPU_TEMPERATURE_LEVEL_COOL) {
            return CPU_STATE_COLORS[0];
        } else if (cpuTemperature < CPU_TEMPERATURE_LEVEL_OVERHEATED) {
            return CPU_STATE_COLORS[1];
        } else {
            return CPU_STATE_COLORS[2];
        }
    }

    public static String getCpuTemperatureStateText(Context context) {
        float cpuTemperature = CpuCoolerManager.getInstance().getCachedCpuTemperature();

        if (cpuTemperature < CPU_TEMPERATURE_LEVEL_COOL) {
            return context.getString(R.string.cpu_cooler_state_hint, context.getString(R.string.cpu_cooler_state_cool));
        } else if (cpuTemperature < CPU_TEMPERATURE_LEVEL_OVERHEATED) {
            return context.getString(R.string.cpu_cooler_state_hint, context.getString(R.string.cpu_cooler_state_overheated));
        } else {
            return context.getString(R.string.cpu_cooler_state_hint, context.getString(R.string.cpu_cooler_state_extremely_heated));
        }
    }

    public static boolean isCpuCoolerScanFrozen() {
        long lastCpuCoolerFinishTime = CpuPreferenceHelper.getLastCpuCoolerScanFinishTime();
        if (0 == lastCpuCoolerFinishTime) {
            return false;
        }

        boolean isScanCanceled = CpuPreferenceHelper.isScanCanceled();
        long secondTimeFromLastOpen = (System.currentTimeMillis() - lastCpuCoolerFinishTime) / 1000;
        HSLog.d(CpuCoolDownActivity.TAG, "isCpuCoolerFrozen isScanCanceled = " + isScanCanceled + " lastCpuCoolerFinishTime = " + lastCpuCoolerFinishTime + " secondTimeFromLastOpen = " + secondTimeFromLastOpen);
        return !isScanCanceled && secondTimeFromLastOpen <= CpuCoolerConstant.FROZEN_CPU_COOLER_SECOND_TIME;
    }

    public static boolean isCpuCoolerCleanFrozen() {

        return false;
//        long lastCpuCoolerCleanStartTime = CpuPreferenceHelper.getLastCpuCoolerCleanStartTime();
//        if (0 == lastCpuCoolerCleanStartTime) {
//            return false;
//        }
//
//        long secondTimeFromLastOpen = (System.currentTimeMillis() - lastCpuCoolerCleanStartTime) / 1000;
//        HSLog.d(CpuCoolDownActivity.TAG, "isCpuCoolerFrozen lastCpuCoolerCleanStartTime = " + lastCpuCoolerCleanStartTime + " secondTimeFromLastOpen = " + secondTimeFromLastOpen);
//        return secondTimeFromLastOpen <= CpuCoolerConstant.FROZEN_CPU_COOLER_SECOND_TIME;
    }

    public static String getTemperatureColorText(int cpuTemperature) {
        String colorText;
        if (cpuTemperature >= CpuCoolerConstant.TEMPERATURE_RED_LIMIT) {
            colorText = CpuCoolerConstant.TEMPERATURE_RED;
        } else if (cpuTemperature >= CpuCoolerConstant.TEMPERATURE_YELLOW_LIMIT) {
            colorText = CpuCoolerConstant.TEMPERATURE_YELLOW;
        } else {
            colorText = CpuCoolerConstant.TEMPERATURE_BLUE;
        }
        return colorText;
    }


    public static class FlurryLogger {

        public static void logScanAnimationStart() {
            LauncherAnalytics.logEvent("CPU_ScanAnimation_Start");
        }

        public static void logScanAnimationEnd() {
            LauncherAnalytics.logEvent("CPU_ScanAnimation_End");
        }

        public static void logHomePageShow() {
            LauncherAnalytics.logEvent("CPU_Homepage_Show");
        }

        public static void logHomePageButtonClicked() {
            LauncherAnalytics.logEvent("CPU_Homepage_BtnClicked");
        }

        public static void logOptimalShow() {
            LauncherAnalytics.logEvent("CPU_Optimal_Show");
        }

    }

    public static boolean shouldDisplayFahrenheit() {
        String currentCountry = Locale.getDefault().getCountry().toUpperCase();
        @SuppressWarnings("unchecked") List<String> fahrenheitCountries =
                (List<String>) HSConfig.getList("Application", "Units", "FahrenheitDisplayCountries");
        boolean fahrenheit = false;
        for (String fahrenheitCountry : fahrenheitCountries) {
            if (TextUtils.equals(currentCountry, fahrenheitCountry)) {
                fahrenheit = true;
                break;
            }
        }
        return fahrenheit;
    }

}
