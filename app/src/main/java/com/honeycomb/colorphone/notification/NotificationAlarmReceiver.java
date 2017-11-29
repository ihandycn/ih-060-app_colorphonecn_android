package com.honeycomb.colorphone.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ihs.commons.utils.HSLog;

public class NotificationAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        HSLog.d("NotificationUtils", "receiveAlarmIntent");
        NotificationUtils.sendNotificationIfProper();
    }
}
