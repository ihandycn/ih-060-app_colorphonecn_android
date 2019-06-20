package com.honeycomb.colorphone.notification;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Process;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.acb.call.CallIntentManager;
import com.ihs.commons.utils.HSLog;
import com.messagecenter.customize.MessageCenterManager;

import java.util.List;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NotificationServiceV18 extends NotificationListenerService {

    public static final String TAG = NotificationServiceV18.class.getSimpleName();
    public static boolean inServiceRunning = false;

    public NotificationServiceV18() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        HSLog.i(TAG, "NotificationListenerService created");
    }

    @Override
    public void onListenerConnected() {
        inServiceRunning = true;
        HSLog.i(TAG, "NotificationListenerService onListenerConnected");
    }

    @SuppressLint("NewApi")
    @Override
    public void onNotificationPosted(StatusBarNotification statusBarNotification) {
        inServiceRunning = true;
        CallIntentManager.getInstance().recordAnswerCallIntent(statusBarNotification);
        MessageCenterManager.getInstance().showMessageAssistantIfProper(statusBarNotification);
        HSLog.e(TAG, "New notification: " + statusBarNotification);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        MessageCenterManager.getInstance().removeMessage(sbn);
        HSLog.d(TAG, "Removed notification: " + sbn);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        inServiceRunning = false;
        HSLog.i(TAG, "NotificationListenerService destroyed");
    }

    @Override
    public StatusBarNotification[] getActiveNotifications() {
        StatusBarNotification[] notifications = null;
        try {
            notifications = super.getActiveNotifications();
        } catch (SecurityException e) {
            // The user has revoked our permission. Turn off the feature.
//            BadgeSettings.setBadgeEnabled(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (notifications == null) {
            notifications = new StatusBarNotification[0];
        }
        return notifications;
    }

    /**
     * Try fix service not active.
     *
     *  https://gist.github.com/xinghui/b2ddd8cffe55c4b62f5d8846d5545bf9
     *  https://www.zhihu.com/question/33540416
     */
    public static void ensureCollectorRunning(Context context) {
        ComponentName collectorComponent = new ComponentName(context, /*NotificationListenerService Inheritance*/ NotificationServiceV18.class);
        HSLog.v(TAG, "ensureCollectorRunning collectorComponent: " + collectorComponent);
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        boolean collectorRunning = false;
        List<ActivityManager.RunningServiceInfo> runningServices = manager.getRunningServices(Integer.MAX_VALUE);
        if (runningServices == null ) {
            HSLog.w(TAG, "ensureCollectorRunning() runningServices is NULL");
            return;
        }
        for (ActivityManager.RunningServiceInfo service : runningServices) {
            if (service.service.equals(collectorComponent)) {
                HSLog.w(TAG, "ensureCollectorRunning service - pid: " + service.pid + ", currentPID: " + Process.myPid() + ", clientPackage: " + service.clientPackage + ", clientCount: " + service.clientCount
                        + ", clientLabel: " + ((service.clientLabel == 0) ? "0" : "(" + context.getResources().getString(service.clientLabel) + ")"));
                if (service.pid == Process.myPid() /*&& service.clientCount > 0 && !TextUtils.isEmpty(service.clientPackage)*/) {
                    collectorRunning = true;
                }
            }
        }
        if (!inServiceRunning) {
            inServiceRunning = collectorRunning;
        }
        if (collectorRunning) {
            HSLog.d(TAG, "ensureCollectorRunning: collector is running");
            return;
        }
        HSLog.d(TAG, "ensureCollectorRunning: collector not running, reviving...");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NotificationServiceV18.requestRebind(collectorComponent);
        } else {
            toggleNotificationListenerService(context);
        }
    }

    private static void toggleNotificationListenerService(Context context) {
        HSLog.d(TAG, "toggleNotificationListenerService() called");
        ComponentName thisComponent = new ComponentName(context, /*getClass()*/ NotificationServiceV18.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

    }
}
