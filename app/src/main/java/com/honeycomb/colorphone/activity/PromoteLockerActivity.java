package com.honeycomb.colorphone.activity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.colorphone.lock.util.PreferenceHelper;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.honeycomb.colorphone.util.PromoteLockerAutoPilotUtils;

/**
 * Created by jelly on 2017/12/14.
 */

public class PromoteLockerActivity extends AppCompatActivity {

    public static final String ALERT_TYPE = "alert_type";
    public static final int AFTER_APPLY_FINISH = 8;
    public static final int WHEN_APP_LAUNCH = 9;

    public static final String PREFS_FILE = "promote_locker_prefs_file_name";
    public static final String PREFS_PROMOTE_LOCKER_ALERT_SHOW_COUNT = "promote_locker_alert_show_count";
    public static final String PREFS_PROMOTE_LOCKER_ALERT_APP_SHOW_TIME = "promote_locker_alert_show_time";

    private int alertType;

    public static void startPromoteLockerActivity(Activity activity, int alertType) {
        Intent intent = new Intent(activity, PromoteLockerActivity.class);
        intent.putExtra(ALERT_TYPE, alertType);
        activity.overridePendingTransition(0, 0);
        activity.startActivity(intent);
        PreferenceHelper helper = PreferenceHelper.get(PREFS_FILE);
        helper.putLong(PREFS_PROMOTE_LOCKER_ALERT_APP_SHOW_TIME, System.currentTimeMillis());
        helper.incrementAndGetInt(PREFS_PROMOTE_LOCKER_ALERT_SHOW_COUNT);
        helper.putInt(ALERT_TYPE, alertType);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        alertType = getIntent().getIntExtra(ALERT_TYPE, AFTER_APPLY_FINISH);
        setContentView(alertType == WHEN_APP_LAUNCH ? R.layout.promote_locker_when_app_launch : R.layout.promote_locker_when_apply_theme);

        initAlertDetails();
        initAddLockerButton();
        ImageButton close = findViewById(R.id.close);
        if (close != null) {
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        TextView noThanks = findViewById(R.id.no_thanks);
        if (noThanks != null) {
            noThanks.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        LauncherAnalytics.logEvent(alertType == WHEN_APP_LAUNCH ? "StartApp_Promote_Alert_Viewed" : "ApplyFinished_Promote_Alert_Viewed");
        PromoteLockerAutoPilotUtils.logPromoteAlertViewed();
    }

    private void initAlertDetails() {
        TextView title = findViewById(R.id.title);
        title.setText(PromoteLockerAutoPilotUtils.getPromoteAlertTitle());
        TextView description = findViewById(R.id.description);
        description.setText(PromoteLockerAutoPilotUtils.getPromoteAlertDetailText());
    }

    private void initAddLockerButton() {
        TextView addLockerButton = findViewById(R.id.add_locker_button);
        addLockerButton.setText(PromoteLockerAutoPilotUtils.getPromoteAlertBtnText());
        addLockerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String packageName = PromoteLockerAutoPilotUtils.getPromoteLockerApp();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=" + packageName)); //跳转到应用市场，非Google Play市场一般情况也实现了这个接口

                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));
                    startActivity(intent);
                }
                finish();
                LauncherAnalytics.logEvent(alertType == WHEN_APP_LAUNCH ? "StartApp_Promote_Alert_Btn_Clicked" : "ApplyFinished_Promote_Alert_Btn_Clicked");
                PromoteLockerAutoPilotUtils.logPromoteAlertBtnClicked();
            }
        });
    }

}
