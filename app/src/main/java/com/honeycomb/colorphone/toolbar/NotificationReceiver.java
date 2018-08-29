package com.honeycomb.colorphone.toolbar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Launched from notification with {@link android.app.PendingIntent#getBroadcast(Context, int, Intent, int)} pending
 * intent.
 */
public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager.getInstance().handleEvent(context.getApplicationContext(), intent);
    }
}
