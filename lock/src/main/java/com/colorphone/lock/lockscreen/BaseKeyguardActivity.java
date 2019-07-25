package com.colorphone.lock.lockscreen;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager.LayoutParams;

import com.colorphone.lock.LockerCustomConfig;
import com.colorphone.lock.lockscreen.chargingscreen.ChargingScreenUtils;
import com.ihs.app.alerts.HSAlertMgr;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.device.common.utils.Utils;
import com.superapps.util.Threads;

import static android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
import static android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN;
import static com.colorphone.lock.lockscreen.locker.NotificationWindowHolder.BUNDLE_KEY_PACKAGE_NAME;
import static com.colorphone.lock.lockscreen.locker.NotificationWindowHolder.NOTIFY_KEY_REMOVE_MESSAGE;

public abstract class BaseKeyguardActivity extends HSAppCompatActivity {

    public static boolean exist;

    private KeyguardManager keyguardManager;
    private boolean isKeyguardSecure;
    private KeyguardManager.KeyguardDismissCallback callback;

    /**
     * We should ignore event of UserPresent that trigger by ourself.
     * but, if user trigger it , we should finish out ourself.
     * (In case face detect success, user back to home screen in time)
     */
    private boolean ingoreUserPresentEvent;

//    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            HSLog.d("Locker", "UserPresent, ingore = " + ingoreUserPresentEvent);
//            if (ingoreUserPresentEvent) {
//                ingoreUserPresentEvent = false;
////            } else {
////                finish();
//            }
//        }
//    };

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

//        if (!isKeyguardSecure) {
//            tryDismissKeyguard();
//        }

        boolean isScreenOn = Utils.isScreenOn();
        if (!isScreenOn) {
            // Log only screen off,
            // When Screen On user may unlock screen by FaceDetect or FingerPrint
            LockerCustomConfig.getLogger().logEvent("LockScreen_Keyguard_User", "Type", isKeyguardSecure ? "Secure" : "None");
        }

//        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_USER_PRESENT);
//        registerReceiver(mBroadcastReceiver, intentFilter);

        onInitView();
    }

    private void tryDismissKeyguard() {
        if (keyguardManager != null &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            initCallback();
            keyguardManager.requestDismissKeyguard(this, callback);
        } else {
            getWindow().addFlags(FLAG_DISMISS_KEYGUARD);
        }

        // Trigger by ourself.
        ingoreUserPresentEvent = true;
        Threads.removeOnMainThread(mUserPresentTimeoutChecker);
        Threads.postOnMainThreadDelayed(mUserPresentTimeoutChecker, 8000);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onDestroy() {
        HSLog.i("LockManager", "BaseKeyguardActivity onDestroy");
        exist = false;
        super.onDestroy();
//        unregisterReceiver(mBroadcastReceiver);
        Threads.removeOnMainThread(mUserPresentTimeoutChecker);
    }

    @Override public void finish() {
        HSLog.i("LockManager", "BaseKeyguardActivity finish");
        exist = false;
        super.finish();

        if (!isKeyguardSecure) {
            tryDismissKeyguard();
        }
    }

    protected abstract void onInitView();

    protected boolean fullScreen(){
        return true;
    }


    @TargetApi(Build.VERSION_CODES.O)
    private void initCallback() {

        callback = new KeyguardManager.KeyguardDismissCallback() {
            @Override
            public void onDismissError() {
                super.onDismissError();
            }

            @Override
            public void onDismissSucceeded() {
                super.onDismissSucceeded();
                if (LockNotificationManager.getInstance().callbackInfo != null) {
                    PendingIntent pendingIntent = LockNotificationManager.getInstance().callbackInfo.notification.contentIntent;
                    if (pendingIntent != null) {
                        try {
                            pendingIntent.send();
                            NotificationManager noMan = (NotificationManager)
                                    HSApplication.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                            noMan.cancel(LockNotificationManager.getInstance().callbackInfo.tag, LockNotificationManager.getInstance().callbackInfo.notificationId);

                            HSBundle bundle = new HSBundle();
                            bundle.putString(BUNDLE_KEY_PACKAGE_NAME, LockNotificationManager.getInstance().callbackInfo.packageName);
                            HSGlobalNotificationCenter.sendNotification(NOTIFY_KEY_REMOVE_MESSAGE, bundle);
                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onDismissCancelled() {
                super.onDismissCancelled();
            }
        };
    }

}
