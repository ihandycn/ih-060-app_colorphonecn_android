package com.honeycomb.colorphone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.flashlight.FlashlightManager;

public class SmsFlashListener {

    private static SmsFlashListener INSTANCE = new SmsFlashListener();

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            HSLog.d("SmsListener", "got new Msg");
            if (!FlashlightManager.getInstance().isOn()
                    && !FlashManager.getInstance().isFlash()) {
                FlashManager.getInstance().startFlash(1);
            }
        }
    };

    SharedPreferences.OnSharedPreferenceChangeListener mSharedPreferenceChangeListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    if (Constants.PREFS_LED_SMS_ENABLE.equals(key)) {
                        checkState(sharedPreferences.getBoolean(Constants.PREFS_LED_SMS_ENABLE, false));
                    }
                }
            };

    public static SmsFlashListener getInstance() {
        return INSTANCE;
    }

    public void start() {
        SharedPreferences sharedPreferences = HSApplication.getContext().getSharedPreferences(
                Constants.DESKTOP_PREFS, Context.MODE_PRIVATE);
        sharedPreferences.registerOnSharedPreferenceChangeListener(mSharedPreferenceChangeListener);
        checkState(sharedPreferences.getBoolean(Constants.PREFS_LED_SMS_ENABLE, false));
    }

    private void checkState(boolean enable) {
        HSLog.d("SmsListener", "checkState enable : " + enable);
        if (enable) {
            bind();
        } else {
            unbind();
        }
    }


    private void bind() {
        Context context = HSApplication.getContext();
        try {
            context.registerReceiver(mReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
        } catch (SecurityException ignore) {}
    }

    private void unbind() {
        Context context = HSApplication.getContext();
        try {
            context.unregisterReceiver(mReceiver);
        } catch (Exception ignore) {
        }
    }
}
