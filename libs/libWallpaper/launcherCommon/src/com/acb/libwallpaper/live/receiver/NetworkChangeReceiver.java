package com.acb.libwallpaper.live.receiver;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import com.acb.libwallpaper.live.util.WifiHelper;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.superapps.broadcast.BroadcastCenter;
import com.superapps.broadcast.BroadcastListener;
import com.superapps.util.Networks;

public class NetworkChangeReceiver implements BroadcastListener, StrictReceiver {

    private static final String TAG = NetworkChangeReceiver.class.getSimpleName();

    public static final String NOTIFICATION_CONNECTIVITY_CHANGED = "connectivity_changed";
    public static final String BUNDLE_KEY_IS_NETWORK_AVAILABLE = "is_network_available";

    private boolean mNetworkAvailable;

    @Override
    public void onReceive(Context context, Intent intent) {
        HSLog.i(TAG, "NetworkChangeReceiver invoked");
        updateNotificationToolbar(context);
        HSGlobalNotificationCenter.sendNotification(WifiHelper.NOTIFICATION_NETWORK_CHANGED);

        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            boolean isNetworkAvailable = Networks.isNetworkAvailable(-1);
            boolean changed = (isNetworkAvailable != mNetworkAvailable);
            mNetworkAvailable = isNetworkAvailable;
            if (changed) {
                HSBundle data = new HSBundle();
                data.putBoolean(BUNDLE_KEY_IS_NETWORK_AVAILABLE, isNetworkAvailable);
                HSGlobalNotificationCenter.sendNotification(NOTIFICATION_CONNECTIVITY_CHANGED, data);
            }

        }
    }

    private void updateNotificationToolbar(Context context) {
        HSLog.d("ToolBar.Wifi", "Connect change");
        NetworkInfo activeNetworkInfo;
        try {
            activeNetworkInfo = ((ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        } catch (Exception e) {
            HSLog.w("ToolBar.Wifi", "Failed to get active network info");
            return;
        }
        WifiManager wifiManager;
        try {
            wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        } catch (Exception e) {
            HSLog.w("ToolBar.Wifi", "Failed to obtain wifi service");
            return;
        }
        int wifiState;
        try {
            wifiState = wifiManager.getWifiState();
        } catch (Exception e) {
            HSLog.w("ToolBar.Wifi", "Failed to get wifi state");
            return;
        }
    }

    @Override
    public void register() {
        IntentFilter wifiFilter = new IntentFilter();
        wifiFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        wifiFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        BroadcastCenter.register(HSApplication.getContext(), this, wifiFilter);
    }

    @Override
    public void unRegister() {
        BroadcastCenter.unregister(HSApplication.getContext(), this);
    }

}
