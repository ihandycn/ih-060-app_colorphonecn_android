package com.acb.libwallpaper.live.receiver;

import android.content.Intent;
import android.content.IntentFilter;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.superapps.broadcast.BroadcastCenter;

public class ScreenStatusRouter {

    private static final String TAG = ScreenStatusRouter.class.getSimpleName();

    public static void turnOn() {
        IntentFilter screenFilter = new IntentFilter();
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF);
        screenFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);

        BroadcastCenter.register(HSApplication.getContext(), (context, intent) -> {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                HSApplication.getContext().sendBroadcast(new Intent(BroadcastCenter.ACTION_UNORDERED_SCREEN_OFF));
                HSLog.d(TAG, "Route broadcast SCREEN_ON");
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                HSApplication.getContext().sendBroadcast(new Intent(BroadcastCenter.ACTION_UNORDERED_SCREEN_ON));
                HSLog.d(TAG, "Route broadcast SCREEN_OFF");
            }
        }, screenFilter);
    }
}
