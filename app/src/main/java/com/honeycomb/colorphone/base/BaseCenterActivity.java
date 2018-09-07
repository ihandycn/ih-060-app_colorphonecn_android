package com.honeycomb.colorphone.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.honeycomb.colorphone.resultpage.ResultPageActivity;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.commons.utils.HSLog;

/**
 *  Center activity before Result Page.
 *
 *  Handle logic when back from result page.
 */
public class BaseCenterActivity extends BaseAppCompatActivity {

    public static final String INTENT_NOTIFICATION_ACTIVITY_FINISH_ACTION = "android.intent.action.NOTIFICATION_ACTIVITY_FINISH";

    class NotificationActivityBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (null == intent) {
                return;
            }
            String action = intent.getAction();
            HSLog.d(ResultPageActivity.TAG, "BaseCenterActivity *** onReceive *** action = " + action);
            if (INTENT_NOTIFICATION_ACTIVITY_FINISH_ACTION.equals(action)) {
                finish();
            }
        }
    }

    NotificationActivityBroadcastReceiver mNotificationActivityBroadcastReceiver;

    public boolean isEnableNotificationActivityFinish() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isEnableNotificationActivityFinish()) {
            HSLog.d(ResultPageActivity.TAG, "BaseCenterActivity register broadcast notification activity finish");
            if (mNotificationActivityBroadcastReceiver == null) {
                mNotificationActivityBroadcastReceiver = new NotificationActivityBroadcastReceiver();
                registerReceiver(mNotificationActivityBroadcastReceiver, new IntentFilter(INTENT_NOTIFICATION_ACTIVITY_FINISH_ACTION));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isEnableNotificationActivityFinish() && null != mNotificationActivityBroadcastReceiver) {
            HSLog.d(ResultPageActivity.TAG, "BaseCenterActivity onDestroy unregisterReceiver notification activity finish remove");
            Utils.unregisterReceiver(this, mNotificationActivityBroadcastReceiver);
            mNotificationActivityBroadcastReceiver = null;
        }
    }

}
