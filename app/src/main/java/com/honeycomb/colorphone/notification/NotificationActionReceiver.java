package com.honeycomb.colorphone.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by jelly on 2017/11/23.
 */

public class NotificationActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {


        Log.d("NotificationUtils", "receive notification action");
        String action = intent.getAction();
        int id = intent.getIntExtra(NotificationConstants.THEME_NOTIFICATION_KEY, -1);
        if (id != NotificationConstants.THEME_NOTIFICATION_ID) {
           return;
        }
        if (action == null) {
            return;
        }

        if (action.equals(NotificationConstants.THEME_NOTIFICATION_CLICK_ACTION)) {
            Log.d("NotificationUtils", "receive click action");
        }

        if (action.equals(NotificationConstants.THEME_NOTIFICATION_DELETE_ACTION)) {
            Log.d("NotificationUtils", "receive delete action");
        }
    }
}
