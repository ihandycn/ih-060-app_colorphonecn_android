package com.honeycomb.colorphone.module;

import android.content.Context;
import android.content.Intent;

import com.honeycomb.colorphone.Ap;
import com.honeycomb.colorphone.battery.BatteryCleanActivity;
import com.honeycomb.colorphone.battery.BatteryUtils;
import com.honeycomb.colorphone.resultpage.ResultPageActivity;
import com.honeycomb.colorphone.resultpage.ResultPageManager;
import com.honeycomb.colorphone.triviatip.TriviaTip;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.honeycomb.colorphone.util.ModuleUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.chargingimprover.ChargingImproverCallBack;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.device.clean.memory.HSAppMemory;
import com.superapps.util.Navigations;

import net.appcloudbox.autopilot.AutopilotConfig;

import java.util.ArrayList;
import java.util.List;

public class ChargingImproverCallbackImpl implements ChargingImproverCallBack {
    @Override
    public void onClickWhenBatteryScanning() {
        ResultPageManager.getInstance().setInBatteryImprover(true);
        ResultPageManager.getInstance().setFromImproverOK(false);

        Intent intent = new Intent(getApplicationContext(), BatteryCleanActivity.class);
        Navigations.startActivitySafely(getApplicationContext(), intent);
        Ap.Improver.logEvent("batteryboost_alert_improve_btnclicked");
    }

    private Context getApplicationContext() {
        return HSApplication.getContext();
    }

    @Override
    public void onClickWhenBatteryScanFinished(boolean hasAppsToClean, List<HSAppMemory> appList) {
        if (hasAppsToClean && !appList.isEmpty()) {
            Ap.Improver.logEvent("batteryboost_alert_improve_btnclicked");
            ResultPageManager.getInstance().setInBatteryImprover(true);
            ResultPageManager.getInstance().setFromImproverOK(false);
            Intent intent = new Intent(getApplicationContext(), BatteryCleanActivity.class);
            ArrayList<String> packageList = new ArrayList<>();
            for (HSAppMemory appMemory : appList) {
                packageList.add(appMemory.getPackageName());
            }
            intent.putStringArrayListExtra(BatteryCleanActivity.EXTRA_KEY_SCANNED_LIST, packageList);
            intent.putExtra(BatteryCleanActivity.EXTRA_KEY_COME_FROM_MAIN_PAGE, true);
            Navigations.startActivitySafely(getApplicationContext(), intent);
        } else {
            Ap.Improver.logEvent("batteryboost_alert_ok_btnclicked");
            boolean startResultPage = HSConfig.optBoolean(false, "Application", "ChargingImprover", "ChargingImproverXAdEnabled");
            if (startResultPage) {
                ResultPageManager.getInstance().setInBatteryImprover(true);
                ResultPageManager.getInstance().setFromImproverOK(true);
                ResultPageActivity.startForBattery(HSApplication.getContext(), true, 0, 0);
            }
        }
    }

    @Override
    public boolean isCleanTimeFrozen() {
        return BatteryUtils.isCleanTimeFrozen();
    }

    @Override public boolean isImproverNewUser() {
        return ModuleUtils.isChargingImproverNewUser();
    }

    @Override public boolean isImproverEnabled() {
        return HSConfig.optBoolean(false, "Application", "ChargingImprover", "Enabled")
                && Ap.Improver.enable();
    }

    @Override public void onImproverShow() {
        Ap.Improver.logEvent("batteryboost_alert_show");
        ResultPageManager.getInstance().setInBatteryImprover(true);
        ResultPageManager.getInstance().preloadResultPageAds();
        TriviaTip.getInstance().setImproverShow(true);
    }

    @Override public void onImproveButtonClick() {

    }

    @Override public void onLaterButtonClick() {
    }

    @Override public boolean allowAlertBack() {
        return HSConfig.optBoolean(true, "Application", "ChargingImprover", "AlertAllowBack");
    }

    @Override public int intervalInMinutes() {
        return Math.max(HSConfig.optInteger(15, "Application", "ChargingImprover", "IntervalTimeMinutes"),
                (int) AutopilotConfig.getDoubleToTestNow(Ap.Improver.TOPIC_ID, "charging_improver_show_interval", 15));
    }

    @Override public int afterInstallInHour() {
        return HSConfig.optInteger(2, "Application", "ChargingImprover", "FirstAlertShowTime");
    }

    @Override public int minAlertShowRate() {
        return HSConfig.optInteger(15, "Application", "ChargingImprover", "MinAlertShowRate");
    }

    @Override
    public boolean showOnlyOnceBeforeUnplug() {
        boolean config = HSConfig.optBoolean(false, "Application", "ChargingImprover", "ShowAlertOnceWhenOneCharging");
        boolean autopilot = AutopilotConfig.getBooleanToTestNow(Ap.Improver.TOPIC_ID, "show_alert_once_when_one_charging", false);
        HSLog.d("ChargingImprove", "only once config : " + config + "; autopilot : " + autopilot);
        return config && autopilot;
    }

    @Override public void logEvent(String eventID, String... vars) {
        LauncherAnalytics.logEvent(eventID, vars);
    }

    @Override public void onChargingImproverDismiss() {
        TriviaTip.getInstance().setImproverShow(false);
    }
}
