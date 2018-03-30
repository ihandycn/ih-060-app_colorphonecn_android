package com.honeycomb.colorphone.activity;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.acb.utils.NavUtils;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.notification.NotificationAutoPilotUtils;
import com.honeycomb.colorphone.notification.permission.PermissionHelper;
import com.honeycomb.colorphone.notification.permission.PermissionUtils;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSPreferenceHelper;

public class NotificationAccessGuideAlertActivity extends Activity {

    public static final String ACB_PHONE_NOTIFICATION_GUIDE_INSIDE_APP = "ACB_PHONE_NOTIFICATION_GUIDE_INSIDE_APP";

    public static final String ACB_PHONE_NOTIFICATION_ACCESS_GUIDE_OUT_APP_LAST_SHOW_TIME = "ACB_PHONE_NOTIFICATION_ACCESS_GUIDE_OUT_APP_LAST_SHOW_TIME";
    public static final String ACB_PHONE_NOTIFICATION_ACCESS_GUIDE_OUT_APP_SHOW_COUNT = "ACB_PHONE_NOTIFICATION_ACCESS_GUIDE_OUT_APP_SHOW_COUNT";

    public static final String ACB_PHONE_NOTIFICATION_APP_IS_FIRST_SESSION = "ACB_PHONE_NOTIFICATION_APP_IS_FIRST_SESSION";
    public static final String ACB_PHONE_NOTIFICATION_INSIDE_GUIDE_SHOW_TIME = "ACB_PHONE_NOTIFICATION_INSIDE_GUIDE_SHOW_TIME";
    public static final String ACB_PHONE_NOTIFICATION_INSIDE_GUIDE_SHOW_COUNT = "ACB_PHONE_NOTIFICATION_INSIDE_GUIDE_SHOW_COUNT";

    public static final String ACB_PHONE_NOTIFICATION_ACCESS_GUIDE_PREFS_FILE = "ACB_PHONE_NOTIFICATION_ACCESS_GUIDE_PREFS_FILE";

    public static HSPreferenceHelper prefs = HSPreferenceHelper.create(HSApplication.getContext(), ACB_PHONE_NOTIFICATION_ACCESS_GUIDE_PREFS_FILE);

    private boolean insideApp;

    public static void startOutAppGuide(final Context context) {
        HSPreferenceHelper.getDefault().putLong(ACB_PHONE_NOTIFICATION_ACCESS_GUIDE_OUT_APP_LAST_SHOW_TIME, System.currentTimeMillis());
        int pre = prefs.getInt(ACB_PHONE_NOTIFICATION_ACCESS_GUIDE_OUT_APP_SHOW_COUNT, 0);
        prefs.putInt(ACB_PHONE_NOTIFICATION_ACCESS_GUIDE_OUT_APP_SHOW_COUNT, ++pre);

        Intent intent = new Intent(context, NotificationAccessGuideAlertActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(ACB_PHONE_NOTIFICATION_GUIDE_INSIDE_APP, false);
        context.startActivity(intent);
    }

    public static void startInAppGuide(final Context context) {
        HSPreferenceHelper.getDefault().putLong(ACB_PHONE_NOTIFICATION_INSIDE_GUIDE_SHOW_TIME, System.currentTimeMillis());
        int pre = prefs.getInt(ACB_PHONE_NOTIFICATION_INSIDE_GUIDE_SHOW_COUNT, 0);
        prefs.putInt(ACB_PHONE_NOTIFICATION_INSIDE_GUIDE_SHOW_COUNT, ++pre);

        Intent intent = new Intent(context, NotificationAccessGuideAlertActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(ACB_PHONE_NOTIFICATION_GUIDE_INSIDE_APP, true);
        NavUtils.startActivitySafely(context, intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        insideApp = getIntent().getBooleanExtra(ACB_PHONE_NOTIFICATION_GUIDE_INSIDE_APP, false);
        final boolean isFirstSession = getIntent().getBooleanExtra(ACB_PHONE_NOTIFICATION_APP_IS_FIRST_SESSION, false);
        if (insideApp) {
            setContentView(R.layout.acb_phone_notification_access_guide_alert_activity_without_icon);
        } else {
            setContentView(R.layout.acb_phone_notification_access_guide_alert_activity);
            initAppIconAndName();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.enable_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Class parentClass = ColorPhoneActivity.class;
                if (parentClass != null) {
                    PermissionHelper.requestNotificationPermission(parentClass, NotificationAccessGuideAlertActivity.this, true, new Handler(),
                            isFirstSession ? "firstLaunch" : (insideApp ? "insideApp" : "outsideApp"));
                } else {
                    PermissionUtils.requestNotificationPermission(NotificationAccessGuideAlertActivity.this, true, new Handler(),
                            isFirstSession ? "firstLaunch" : (insideApp ? "insideApp" : "outsideApp"));
                }
                onOpenPermissionSettings(insideApp, isFirstSession);
                finish();
                onEnableClick(insideApp, isFirstSession);
            }
        });
        onShow(insideApp, isFirstSession);
    }

    private void initAppIconAndName() {
        ImageView appIcon = findViewById(R.id.app_icon);
        TextView appName = findViewById(R.id.app_name);
        appIcon.setImageResource(R.mipmap.ic_launcher);
        appName.setText(R.string.app_name);
    }

    private void onShow(boolean insideApp, boolean isFirstSession) {
        if (isFirstSession) {
            HSAnalytics.logEvent("Colorphone_Notification_Alert_Show_First_Launch");
            return;
        }
        int order;
        if (insideApp) {
            order = prefs.getInt(ACB_PHONE_NOTIFICATION_INSIDE_GUIDE_SHOW_COUNT, 0);
            HSAnalytics.logEvent("Colorphone_Notification_Alert_Show_Inside_App", "time", "" + order);
        } else {
            order = prefs.getInt(ACB_PHONE_NOTIFICATION_ACCESS_GUIDE_OUT_APP_SHOW_COUNT, 0);
            HSAnalytics.logEvent("Colorphone_Notification_Alert_Show_Outside_App", "time", "" + order);
        }
    }


    private void onEnableClick(boolean insideApp, boolean isFirstSession) {
        if (isFirstSession) {
            HSAnalytics.logEvent("Colorphone_Notification_Alert_Ok_Clicked_First_Launch");
            return;
        }
        int order;
        if (insideApp) {
            order = prefs.getInt(ACB_PHONE_NOTIFICATION_INSIDE_GUIDE_SHOW_COUNT, 0);
            HSAnalytics.logEvent("Colorphone_Notification_Alert_Ok_Clicked_Inside_App", "time", "" + order);
        } else {
            order = prefs.getInt(ACB_PHONE_NOTIFICATION_ACCESS_GUIDE_OUT_APP_SHOW_COUNT, 0);
            HSAnalytics.logEvent("Colorphone_Notification_Alert_Ok_Clicked_Outside_App", "time", "" + order);
        }
    }

    private void onOpenPermissionSettings(boolean insideApp, boolean isFirstSession) {
        if (isFirstSession) {
            HSAnalytics.logEvent("Colorphone_SystemNotificationAccessView_Show", "from", "firstLaunch");
            return;
        }

        HSAnalytics.logEvent("Colorphone_SystemNotificationAccessView_Show", "from", insideApp ? "insideApp" : "outsideApp");
        NotificationAutoPilotUtils.logSettingsAlertShow();
    }

    private void onNotificationAccessGranted(String fromType) {
        HSAnalytics.logEvent("Colorphone_Notification_Access_Enabled", "from", fromType);

        NotificationAutoPilotUtils.logSettingsAccessEnabled();

    }
}

