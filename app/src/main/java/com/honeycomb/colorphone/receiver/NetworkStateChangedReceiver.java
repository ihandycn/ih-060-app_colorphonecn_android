package com.honeycomb.colorphone.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.text.TextUtils;

import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.NetUtils;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Preferences;

public class NetworkStateChangedReceiver extends BroadcastReceiver {
    public static final String TAG = NetworkStateChangedReceiver.class.getSimpleName();
    public static final String PREF_KEY_NETWORK_STATE_NAME = "pref_key_network_state";
    public static final String PREF_KEY_LAST_NETWORK_CHANGED_TIME = "pref_key_last_network_changed_time";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            String lastNetworkName = Preferences.getDefault().getString(PREF_KEY_NETWORK_STATE_NAME, "Default");
            String networkName = NetUtils.getNetWorkStateName();
            if (!TextUtils.equals(lastNetworkName, networkName) && !TextUtils.equals(lastNetworkName, "Default")) {
                long currentTime = System.currentTimeMillis();
                long lastLogTime = Preferences.getDefault().getLong(PREF_KEY_LAST_NETWORK_CHANGED_TIME, 0);
                Analytics.logEvent("Network_State_Change", false, "State", lastNetworkName + "_" + networkName, "Interval", getEventValueFromLastTime(currentTime - lastLogTime));
                HSLog.d(TAG, "Network_State_Change : " + lastNetworkName + "_" + networkName);
                Preferences.getDefault().putString(PREF_KEY_NETWORK_STATE_NAME, networkName);
                HSLog.d(TAG, "refresh network state = " + networkName);
                Preferences.getDefault().putLong(PREF_KEY_LAST_NETWORK_CHANGED_TIME, currentTime);
            }
        }
    }

    private String getEventValueFromLastTime(long intervalMillis) {
        int intervalMin = (int) (intervalMillis / 1000 / 60);
        if (intervalMin <= 0) {
            return "0";
        }
        if (intervalMin >= 24 * 60) {
            return "24h+";
        }
        if (intervalMin < 60) {
            return (intervalMin / 5 + 1) * 5 + "min";
        } else {
            return (intervalMin / 60 + 1) + "h";
        }

    }
}
