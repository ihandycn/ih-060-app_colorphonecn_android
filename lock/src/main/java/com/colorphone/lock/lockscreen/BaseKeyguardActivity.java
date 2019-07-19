package com.colorphone.lock.lockscreen;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager.LayoutParams;

import com.colorphone.lock.LockerCustomConfig;
import com.colorphone.lock.lockscreen.chargingscreen.ChargingScreenUtils;
import com.ihs.app.alerts.HSAlertMgr;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.utils.HSLog;
import com.ihs.device.common.utils.Utils;
import com.superapps.util.Threads;

import static android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
import static android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN;

public abstract class BaseKeyguardActivity extends HSAppCompatActivity {

    public static boolean exist;

    private KeyguardManager keyguardManager;
    private boolean isKeyguardSecure;

    /**
     * We should ignore event of UserPresent that trigger by ourself.
     * but, if user trigger it , we should finish out ourself.
     * (In case face detect success, user back to home screen in time)
     */
    private boolean ingoreUserPresentEvent;

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            HSLog.d("Locker", "UserPresent, ingore = " + ingoreUserPresentEvent);
            if (ingoreUserPresentEvent) {
                ingoreUserPresentEvent = false;
            } else {
                finish();
            }
        }
    };

    private Runnable mUserPresentTimeoutChecker = new Runnable() {
        @Override
        public void run() {
            // Set flag to false.
            ingoreUserPresentEvent = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        exist = true;

        HSAlertMgr.delayRateAlert();

        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        if (fullScreen()) {
            if (!ChargingScreenUtils.isNativeLollipop()) {
                window.addFlags(LayoutParams.FLAG_FULLSCREEN);
            }
        } else {
            // 透明状态栏
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.addFlags(LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.setStatusBarColor(Color.TRANSPARENT);
            }
        }

        window.addFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.setSoftInputMode(SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // Check keyguard
        isKeyguardSecure = false;
        keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (keyguardManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            isKeyguardSecure = keyguardManager.isKeyguardSecure();
            HSLog.d("isKeyguardSecure: " + isKeyguardSecure
                    + " isKeyguardLocked: " + keyguardManager.isKeyguardLocked());
        }

        if (!isKeyguardSecure) {
            tryDismissKeyguard();
        }

        boolean isScreenOn = Utils.isScreenOn();
        if (!isScreenOn) {
            // Log only screen off,
            // When Screen On user may unlock screen by FaceDetect or FingerPrint
            LockerCustomConfig.getLogger().logEvent("LockScreen_Keyguard_User", "Type", isKeyguardSecure ? "Secure" : "None");
        }

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_USER_PRESENT);
        registerReceiver(mBroadcastReceiver, intentFilter);

        onInitView();
    }

    private void tryDismissKeyguard() {
        if (keyguardManager != null &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            keyguardManager.requestDismissKeyguard(this, null);
        } else {
            getWindow().addFlags(FLAG_DISMISS_KEYGUARD);
        }

        // Trigger by ourself.
        ingoreUserPresentEvent = true;
        Threads.removeOnMainThread(mUserPresentTimeoutChecker);
        Threads.postOnMainThread(mUserPresentTimeoutChecker);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (!isKeyguardSecure) {
           tryDismissKeyguard();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
        Threads.removeOnMainThread(mUserPresentTimeoutChecker);
    }

    protected abstract void onInitView();

    protected boolean fullScreen(){
        return true;
    }

}
