package com.honeycomb.colorphone.toolbar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.alerts.HSAlertMgr;

/**
 * This {@link Activity} is not intended to be visible. Launched from notification with
 * {@link android.app.PendingIntent#getActivity(Context, int, Intent, int)} pending intent.
 */
public class NotificationActivity extends Activity {

    private static final String TAG = NotificationActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HSAlertMgr.delayRateAlert();

        Utils.showWhenLocked(this);

        NotificationManager.getInstance().handleEvent(this, getIntent());
//        switch (getIntent().getAction()) {
//            case NotificationManager.ACTION_BOOST_TOOLBAR:
//                Analytics.logEvent("Notification_boost_clicked");
//                BoostActivity.start(this, true);
//                break;
//            case NotificationManager.ACTION_MOBILE_DATA:
//                NavUtils.startSystemDataUsageSetting(this, true);
//                break;
//            case NotificationManager.ACTION_SETTINGS_CLICK:
//                NavUtils.startActivitySafely(this, new Intent(AlarmClock.ACTION_SET_ALARM));
//                break;
//            case NotificationManager.ACTION_CPU_COOLER_TOOLBAR:
//                Analytics.logEvent("Notification_Toolbar_CPU_Clicked", "Type", CpuCoolerUtils.getTemperatureColorText(mCpuTemperature));
//                Analytics.logEvent("CPUCooler_Open", "Type", "Toolbar");
//                Intent cpuCoolerIntent = new Intent(this, CpuCoolDownActivity.class);
//                cpuCoolerIntent.putExtra(CpuCoolDownActivity.EXTRA_KEY_NEED_SCAN, true);
//                cpuCoolerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                NavUtils.startActivitySafely(this, cpuCoolerIntent);
//                break;
//            default:
//                HSLog.w(TAG, "Unsupported action");
//                break;
//        }

        finish();
    }
}
