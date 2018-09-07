package com.honeycomb.colorphone.cpucooler;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.boost.DeviceManager;
import com.honeycomb.colorphone.cpucooler.util.CpuCoolerConstant;
import com.honeycomb.colorphone.cpucooler.util.CpuCoolerUtils;
import com.ihs.app.framework.HSApplication;

import java.util.ArrayList;
import java.util.List;

public class CpuCoolerManager {

    private volatile static CpuCoolerManager instance;

    private float cpuTemperature;

    private List<String> cpuDetailApps = new ArrayList<>();

    private CpuCoolerManager() {
    }

    public static CpuCoolerManager getInstance() {
        if (instance == null) {
            synchronized (CpuCoolerManager.class) {
                if (instance == null) {
                    instance = new CpuCoolerManager();
                }
            }
        }
        return instance;
    }

    public int fetchCpuTemperature() {
        return Math.round(cpuTemperature = DeviceManager.getInstance().getCpuTemperatureCelsius());
    }

    public float getCachedCpuTemperature() {
        return cpuTemperature;
    }

    public void setScannedApp(List<String> apps) {
        cpuDetailApps.clear();
        cpuDetailApps.addAll(apps);
    }

    public List<String> getScannedApp() {
        return cpuDetailApps;
    }

    public int getRandomCoolDownTemperature() {
        return (int) (Math.random() * 5 + 2);
    }

    public int getColor(int temperature) {
        Context context = HSApplication.getContext();
        int color;
        if (temperature >= CpuCoolerConstant.TEMPERATURE_RED_LIMIT) {
            color = ContextCompat.getColor(context, R.color.cpu_cooler_notification_red);
        } else if (temperature >= CpuCoolerConstant.TEMPERATURE_YELLOW_LIMIT) {
            color = ContextCompat.getColor(context, R.color.cpu_cooler_notification_yellow);
        } else {
            color = ContextCompat.getColor(context, R.color.cpu_cooler_notification_blue);
        }
        return color;
    }

    public int getDrawableId(int temperature) {
        if (CpuCoolerUtils.isCpuCoolerCleanFrozen()) {
            return R.drawable.cpu_cooler_notification_green_svg;
        }

        int drawableId;
        if (temperature >= CpuCoolerConstant.TEMPERATURE_RED_LIMIT) {
            drawableId = R.drawable.cpu_cooler_notification_red_svg;
        } else if (temperature >= CpuCoolerConstant.TEMPERATURE_YELLOW_LIMIT) {
            drawableId = R.drawable.cpu_cooler_notification_yellow_svg;
        } else {
            drawableId = R.drawable.cpu_cooler_notification_green_svg;
        }
        return drawableId;
    }

}
