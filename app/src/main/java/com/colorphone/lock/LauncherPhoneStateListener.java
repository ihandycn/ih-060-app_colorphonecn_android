package com.colorphone.lock;

import android.app.Service;
import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;

public class LauncherPhoneStateListener extends PhoneStateListener {

    public static final String NOTIFICATION_CALL_RINGING = "notification_call_ringing";

    private boolean mPhoneRinging = false;

    public boolean isPhoneRinging() {
        return mPhoneRinging;
    }

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        if (state == TelephonyManager.CALL_STATE_RINGING) {
            mPhoneRinging = true;
            HSGlobalNotificationCenter.sendNotification(NOTIFICATION_CALL_RINGING);
            handleCallRinging();
        } else if (state == TelephonyManager.CALL_STATE_IDLE
                || state == TelephonyManager.CALL_STATE_OFFHOOK) {
            mPhoneRinging = false;
        }
    }

    private void handleCallRinging() {

    }

    public void register(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
        tm.listen(this, PhoneStateListener.LISTEN_CALL_STATE);
    }

    public void unregister(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
        tm.listen(this, PhoneStateListener.LISTEN_NONE);
    }
}
