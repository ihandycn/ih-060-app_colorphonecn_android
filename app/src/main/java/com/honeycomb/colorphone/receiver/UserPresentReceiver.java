package com.honeycomb.colorphone.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;

/**
 * Created by zhewang on 09/01/2018.
 */

public class UserPresentReceiver extends BroadcastReceiver {
    public static final String USER_PRESENT = "user_present";

    @Override public void onReceive(Context context, Intent intent) {
        if (TextUtils.equals(intent.getAction(), Intent.ACTION_USER_PRESENT)) {
            HSGlobalNotificationCenter.sendNotification(USER_PRESENT);
        }
    }
}
