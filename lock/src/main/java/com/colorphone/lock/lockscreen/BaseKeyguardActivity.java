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
import com.superapps.util.Compats;
import com.superapps.util.Threads;

import static android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN;
import static com.colorphone.lock.lockscreen.locker.NotificationWindowHolder.BUNDLE_KEY_PACKAGE_NAME;
import static com.colorphone.lock.lockscreen.locker.NotificationWindowHolder.NOTIFY_KEY_REMOVE_MESSAGE;

public abstract class BaseKeyguardActivity extends HSAppCompatActivity {

    public static boolean exist;

    private KeyguardManager keyguardManager;
    private boolean isKeyguardSecure;
    private KeyguardManager.KeyguardDismissCallback notificaitoHandleCallback;

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
    private boolean keyguardCleaned;

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

        boolean isScreenOn = Utils.isScreenOn();
        if (!isScreenOn) {
            // Log only screen off,
            // When Screen On user may unlock screen by FaceDetect or FingerPrint
            LockerCustomConfig.getLogger().logEvent("LockScreen_Keyguard_User", "Type", isKeyguardSecure ? "Secure" : "None");
        }

        onInitView();
    }

    public void tryDismissKeyguard(boolean finishActivity) {
        if (keyguardCleaned) {
            return;
        }

        // keyguard dismiss may black screen.
        boolean finishActivityAfterKeyguardDismiss = false;
        final AppNotificationInfo appNotificationInfo = LockNotificationManager.getInstance().getClickedNotification();

        if (keyguardManager != null &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (appNotificationInfo != null) {
                removeNotification(appNotificationInfo);
                finishActivityAfterKeyguardDismiss = !isKeyguardSecure;

            }
            if (finishActivityAfterKeyguardDismiss) {
                // Handle notification intent after keyguard dismissed.
                keyguardManager.requestDismissKeyguard(this, getNotificaitonHandleCallback(appNotificationInfo));
            } else {
                startNotificationIntent(appNotificationInfo);

                // Call ths for Huawei device to remove notificaiton on keyguard screen.
                if (isKeyguardSecure && Compats.IS_HUAWEI_DEVICE) {
                    DismissKeyguradActivity.startSelfIfKeyguardSecure(this);
                } else {
                    keyguardManager.requestDismissKeyguard(this, null);
                }
            }


        } else {
            DismissKeyguradActivity.startSelfIfKeyguardSecure(this);
            removeNotification(appNotificationInfo);
            startNotificationIntent(appNotificationInfo);
        }

        if (finishActivity && !finishActivityAfterKeyguardDismiss) {
            finish();
            overridePendingTransition(0, 0);
        }
        keyguardCleaned = true;

        // Trigger by ourself.
        ingoreUserPresentEvent = true;
        Threads.removeOnMainThread(mUserPresentTimeoutChecker);
        Threads.postOnMainThreadDelayed(mUserPresentTimeoutChecker, 8000);
    }

    private static void startNotificationIntent(AppNotificationInfo appNotificationInfo) {
        boolean userNotClicked = appNotificationInfo != null;
        if (userNotClicked) {
            PendingIntent pendingIntent = appNotificationInfo.notification.contentIntent;
            if (pendingIntent != null) {
                try {
                    pendingIntent.send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }
            LockNotificationManager.getInstance().setClickedNotification(null);
        }
    }

    protected void removeNotification(AppNotificationInfo appNotificationInfo) {
        NotificationManager noMan = (NotificationManager)
                HSApplication.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        noMan.cancel(appNotificationInfo.tag, appNotificationInfo.notificationId);
        HSBundle bundle = new HSBundle();
        bundle.putString(BUNDLE_KEY_PACKAGE_NAME, appNotificationInfo.packageName);
        HSGlobalNotificationCenter.sendNotification(NOTIFY_KEY_REMOVE_MESSAGE, bundle);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        keyguardCleaned = false;
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
    }

    protected abstract void onInitView();

    protected boolean fullScreen(){
        return true;
    }

    @TargetApi(Build.VERSION_CODES.O)
    private KeyguardManager.KeyguardDismissCallback getNotificaitonHandleCallback(final AppNotificationInfo appNotificationInfo) {
        if (notificaitoHandleCallback == null) {
            notificaitoHandleCallback = new KeyguardManager.KeyguardDismissCallback() {
                @Override
                public void onDismissError() {
                    super.onDismissError();
                    onDismissKeyguardEndAboveOreo(appNotificationInfo);
                }

                @Override
                public void onDismissSucceeded() {
                    super.onDismissSucceeded();
                    onDismissKeyguardEndAboveOreo(appNotificationInfo);
                }

                @Override
                public void onDismissCancelled() {
                    super.onDismissCancelled();
                    onDismissKeyguardEndAboveOreo(appNotificationInfo);
                }
            };
        }
        return notificaitoHandleCallback;
    }

    private void onDismissKeyguardEndAboveOreo(AppNotificationInfo appNotificationInfo) {
        startNotificationIntent(appNotificationInfo);
    }

}
