package com.honeycomb.colorphone.notification;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;

import com.acb.notification.NotificationServiceListenerManager;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;

public class NotificationServiceV16 extends AccessibilityService {

    private static final String TAG = NotificationServiceV16.class.getSimpleName();

    public NotificationServiceV16() {
        super();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final int eventType = event.getEventType();
        HSLog.d(TAG, "Event type = " + eventType);
        NotificationServiceListenerManager.getInstance().onAccessibilityEvent(event);
    }

    @Override
    public void onInterrupt() {
        HSLog.d(TAG, "onInterrupt");
        NotificationServiceListenerManager.getInstance().onInterrupt();
    }

    @Override
    protected void onServiceConnected() {
        HSLog.d(TAG, "AccessibilityService connected");
        final AccessibilityServiceInfo info = new AccessibilityServiceInfo();

        // Set the type of events that this service wants to listen to.
        // Others won't be passed to this service.
        info.eventTypes =
                AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED | AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;

        // Set the type of feedback your service will provide.
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;

        // Default services are invoked only if no package-specific ones are present
        // for the type of AccessibilityEvent generated.  This service *is*
        // application-specific, so the flag isn't necessary.  If this was a
        // general-purpose service, it would be worth considering setting the
        // DEFAULT flag.
        // info.flags = AccessibilityServiceInfo.DEFAULT;

        info.notificationTimeout = 100;

        this.setServiceInfo(info);

        NotificationServiceListenerManager.getInstance().onServiceConnected();
    }

    private static Intent getNotificationPermissionIntent(boolean isNewTask) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        if (isNewTask) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        return intent;
    }

    public static boolean isAccessibilitySettingsOn() {
        Context context = HSApplication.getContext();
        int accessibilityEnabled = 0;
        final String service = context.getPackageName() + "/" + NotificationServiceV16.class.getName();

        boolean accessibilityFound = false;
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    context.getApplicationContext().getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
            HSLog.v(TAG, "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            HSLog.e(TAG, "Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(
                    context.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                TextUtils.SimpleStringSplitter splitter = mStringColonSplitter;
                splitter.setString(settingValue);
                while (splitter.hasNext()) {
                    String accessabilityService = splitter.next();

                    HSLog.v(TAG, "-------------- > accessabilityService :: " + accessabilityService);
                    if (accessabilityService.equalsIgnoreCase(service)) {
                        HSLog.v(TAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        }
        return accessibilityFound;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        HSLog.d(TAG, "onDestroy");
    }
}
