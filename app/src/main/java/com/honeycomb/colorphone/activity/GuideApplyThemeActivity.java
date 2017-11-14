package com.honeycomb.colorphone.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.content.res.AppCompatResources;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.acb.call.CPSettings;
import com.acb.notification.NotificationAccessGuideAlertActivity;
import com.colorphone.lock.lockscreen.chargingscreen.ChargingScreenSettings;
import com.colorphone.lock.lockscreen.locker.LockerSettings;
import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.notification.NotificationConfig;
import com.honeycomb.colorphone.util.FontUtils;
import com.honeycomb.colorphone.notification.NotificationUtils;
import com.honeycomb.colorphone.util.ModuleUtils;
import com.honeycomb.colorphone.util.StatusBarUtils;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.app.framework.inner.SessionMgr;
import com.ihs.commons.utils.HSPreferenceHelper;

/**
 * Created by sundxing on 17/9/13.
 */

public class GuideApplyThemeActivity extends HSAppCompatActivity {

    public static String KEY_SHOW_TIME = "apply_guide_show_times";
    public static String KEY_DISPLAY_COUNT = "apply_guide_show_count";
    public static String PREFS_GUIDE_APPLY_ALERT_SHOW_SESSION_ID = "PREFS_GUIDE_APPLY_ALERT_SHOW_SESSION_ID";

    public static boolean FULL_SCREEN = true;

    public static boolean start(final Context context, boolean fullScreen) {
        if (NotificationUtils.isShowNotificationGuideAlertWhenApplyTheme(context)) {
            return Utils.doLimitedTimes(new Runnable() {
                @Override
                public void run() {
                    startNotificationAccessAlertActivity(context);
                }
            }, NotificationUtils.PREFS_NOTIFICATION_INSIDE_GUIDE_SHOW_COUNT, NotificationConfig.getInsideAppAccessAlertShowMaxTime());
        } else if (ModuleUtils.isNeedGuideAfterApply()) {
            Intent starter = new Intent(context, GuideApplyThemeActivity.class);
            starter.putExtra("fullscreen", fullScreen);
            context.startActivity(starter);
            return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean isFullScreen = getIntent().getBooleanExtra("fullscreen", true);
        setContentView(R.layout.guide_apply_success);
        if (isFullScreen) {
            StatusBarUtils.hideStatusBar(this);
        }
        View cbContainer = findViewById(R.id.welcome_guide_enable_checkbox_container);
        final CheckBox cb = (CheckBox) findViewById(R.id.welcome_guide_enable_checkbox);
        cb.setButtonDrawable(AppCompatResources.getDrawable(this, R.drawable.welcome_guide_check_box_selector));
        setUpPrivacyTextView();
        HSAnalytics.logEvent("ColorPhone_ApplyFinishGuide_Show");
        findViewById(R.id.guide_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HSAnalytics.logEvent("ColorPhone_ApplyFinishGuide_Cancel_Clicked");
                finish();
            }
        });
        TextView enableBtn = (TextView) findViewById(R.id.welcome_guide_function_enable_btn);
        enableBtn.setTypeface(FontUtils.getTypeface(FontUtils.Font.PROXIMA_NOVA_SEMIBOLD));
        enableBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cb.isChecked()) {
                    HSAnalytics.logEvent("ColorPhone_ApplyFinishGuide_OK_Clicked");
                    CPSettings.setSMSAssistantModuleEnabled(true);
                    CPSettings.setCallAssistantModuleEnabled(true);
                    LockerSettings.setLockerEnabled(true);
                    ChargingScreenSettings.setChargingScreenEnabled(true);
                } else {
                    HSAnalytics.logEvent("ColorPhone_ApplyFinishGuide_OK_Clicked_WithUnselectFeature");
                }
                finish();
            }
        });

        cbContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cb.performClick();
            }
        });

        HSPreferenceHelper.getDefault().putInt(PREFS_GUIDE_APPLY_ALERT_SHOW_SESSION_ID, SessionMgr.getInstance().getCurrentSessionId());
    }

    @Override
    public void onBackPressed() {
        //Ignore back press.
    }

    private static void startNotificationAccessAlertActivity(Context context) {
        Intent intent = new Intent(context, NotificationAccessGuideAlertActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        HSPreferenceHelper.getDefault().putLong(NotificationUtils.PREFS_NOTIFICATION_GUIDE_ALERT_SHOW_TIME, System.currentTimeMillis());
    }

    private void setUpPrivacyTextView() {
        TextView privacyPolicy = (TextView) findViewById(R.id.welcome_guide_privacy_policy);
        privacyPolicy.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        privacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.startActivitySafely(GuideApplyThemeActivity.this, new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.URL_PRIVACY)));
            }
        });

        TextView termsOfService = (TextView) findViewById(R.id.welcome_guide_terms_of_service);
        termsOfService.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        termsOfService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.startActivitySafely(GuideApplyThemeActivity.this, new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.URL_TERM_SERVICES)));
            }
        });
    }
}
