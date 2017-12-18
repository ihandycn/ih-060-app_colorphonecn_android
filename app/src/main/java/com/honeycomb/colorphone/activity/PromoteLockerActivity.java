package com.honeycomb.colorphone.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.colorphone.lock.util.PreferenceHelper;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.util.PromoteLockerAutoPilotUtils;

/**
 * Created by jelly on 2017/12/14.
 */

public class PromoteLockerActivity extends AppCompatActivity {

    public static final String ALERT_TYPE = "alert_type";
    public static final int AFTER_APPLY_FINISH = 8;
    public static final int WHEN_APP_LAUNCH = 9;

    public static final String PREFS_FILE = "promote_locker_prefs_file_name";
    public static final String PROMOTE_LOCKER_ALERT_SHOW_COUNT = "promote_locker_alert_show_count";
    public static final String PROMOTE_LOCKER_ALERT_APP_SHOW_TIME = "promote_locker_alert_show_time";

    public static void startPromoteLockerActivity(Activity activity, int alertType) {
        Intent intent = new Intent(activity, PromoteLockerActivity.class);
        intent.putExtra(ALERT_TYPE, alertType);
        activity.overridePendingTransition(0, 0);
        activity.startActivity(intent);
        PreferenceHelper helper = PreferenceHelper.get(PREFS_FILE);
        helper.putLong(PROMOTE_LOCKER_ALERT_APP_SHOW_TIME, System.currentTimeMillis());
        helper.incrementAndGetInt(PROMOTE_LOCKER_ALERT_SHOW_COUNT);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int alertType = getIntent().getIntExtra(ALERT_TYPE, AFTER_APPLY_FINISH);
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
                PromoteLockerAutoPilotUtils.logPromoteAlertBtnClicked();
            }
        });
    }
}
