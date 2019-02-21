package cn.jpush.android.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.honeycomb.colorphone.util.Analytics;

public class DaemonService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        Analytics.logEvent("Jpush_Daemon_Service_Create");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
