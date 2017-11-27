package com.honeycomb.colorphone.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("NotificationUtils", "receiveAlarmIntent");
        NotificationUtils.sendNotificationIfProper();
    }
}
