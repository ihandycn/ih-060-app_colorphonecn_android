package com.acb.libwallpaper.live.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;

public class UserPresentReceiver extends BroadcastReceiver {
    public static final String USER_PRESENT = "user_present";


    @Override
    public void onReceive(Context context, Intent intent) {
        HSGlobalNotificationCenter.sendNotification(USER_PRESENT);
    }

}
