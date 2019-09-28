package com.colorphone.lock.lockscreen;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import com.colorphone.lock.LockerCustomConfig;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.device.common.utils.Utils;
import com.superapps.util.Compats;

import static com.colorphone.lock.lockscreen.locker.NotificationWindowHolder.BUNDLE_KEY_PACKAGE_NAME;
import static com.colorphone.lock.lockscreen.locker.NotificationWindowHolder.NOTIFY_KEY_REMOVE_MESSAGE;

public class KeyguardHandler {

    public static final String EVENT_KEYGUARD_UNLOCKED = "keyguard_unlock";
    public static final String EVENT_KEYGUARD_LOCKED = "keyguard_lock";

    private KeyguardManager keyguardManager;
    private boolean isKeyguardSecure;
    private KeyguardManager.KeyguardDismissCallback notificaitoHandleCallback;

    /**
     * Get USER_PRESENT event when lock exist.
     * user trigger this by Finger-print or Face-detect.
     */
    protected boolean mUserPresentWithoutSlide;

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
                mUserPresentWithoutSlide = true;
                HSGlobalNotificationCenter.sendNotification(EVENT_KEYGUARD_UNLOCKED);
            } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                // After screen off , keyguard is back.
                keyguardCleaned = false;
            }
        }
    };

    private boolean keyguardCleaned;
    private Context mContext;

    public KeyguardHandler(Context context) {
        mContext = context;
    }

    public void onInit() {
        // Check keyguard
        isKeyguardSecure = false;
        keyguardManager = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
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
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_USER_PRESENT);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);

        mContext.registerReceiver(mBroadcastReceiver, intentFilter);
    }

    public void tryDismissKeyguard(boolean finishActivity, Activity activity) {
        if (keyguardCleaned) {
            return;
        }

        // keyguard dismiss may black screen.
        boolean finishActivityAfterKeyguardDismiss = false;
        final AppNotificationInfo appNotificationInfo = LockNotificationManager.getInstance().getClickedNotification();

        boolean hasNotification = appNotificationInfo != null;
        if (keyguardManager != null &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (hasNotification) {
                removeNotification(appNotificationInfo);
                finishActivityAfterKeyguardDismiss = !isKeyguardSecure;

            }
            if (finishActivityAfterKeyguardDismiss) {
                // Handle notification intent after keyguard dismissed.
                keyguardManager.requestDismissKeyguard(activity, getNotificaitonHandleCallback(appNotificationInfo));
            } else {
                startNotificationIntent(appNotificationInfo);

                // Call ths for Huawei device to remove notificaiton on keyguard screen.
                if (isKeyguardSecure && Compats.IS_HUAWEI_DEVICE) {
                    DismissKeyguradActivity.startSelfIfKeyguardSecure(mContext);
                } else {
                    keyguardManager.requestDismissKeyguard(activity, null);
                }
            }


        } else {
            DismissKeyguradActivity.startSelfIfKeyguardSecure(mContext);
            if (hasNotification) {
                removeNotification(appNotificationInfo);
                startNotificationIntent(appNotificationInfo);
            }
        }

        if (finishActivity && !finishActivityAfterKeyguardDismiss) {
            activity.finish();
            activity.overridePendingTransition(0, 0);
        }
        keyguardCleaned = true;

    }

    /**
     * Use for float window
     */
    public void tryDismissKeyguard() {
        if (keyguardCleaned) {
            return;
        }

        final AppNotificationInfo appNotificationInfo = LockNotificationManager.getInstance().getClickedNotification();

        boolean hasNotification = appNotificationInfo != null;
        if (keyguardManager != null &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (hasNotification) {
                removeNotification(appNotificationInfo);
            }
            startNotificationIntent(appNotificationInfo);
            // Call ths for Huawei device to remove notificaiton on keyguard screen.
            if (isKeyguardSecure && Compats.IS_HUAWEI_DEVICE) {
                DismissKeyguradActivity.startSelfIfKeyguardSecure(mContext);
            } else {
                DismissKeyguradActivity.startSelfIfKeyguardSecureCompatO(mContext);
            }

        } else {
            DismissKeyguradActivity.startSelfIfKeyguardSecure(mContext);
            if (hasNotification) {
                removeNotification(appNotificationInfo);
                startNotificationIntent(appNotificationInfo);
            }
        }

        keyguardCleaned = true;
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

    public void onLockScreenForeground() {
        if (keyguardManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            isKeyguardSecure = keyguardManager.isKeyguardSecure();
            boolean keyguardLocked = keyguardManager.isKeyguardLocked();
            if (mUserPresentWithoutSlide && keyguardLocked) {
                HSGlobalNotificationCenter.sendNotification(EVENT_KEYGUARD_LOCKED);
            }
            HSLog.d("LockManager", "isKeyguardSecure: " + isKeyguardSecure
                    + " isKeyguardLocked: " + keyguardLocked);
        }
    }

    public void onViewDestroy() {
        mContext.unregisterReceiver(mBroadcastReceiver);
    }
}
