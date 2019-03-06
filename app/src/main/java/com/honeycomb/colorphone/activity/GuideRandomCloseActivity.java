package com.honeycomb.colorphone.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.view.View;
import android.widget.TextView;

import com.honeycomb.colorphone.Ap;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.honeycomb.colorphone.util.StatusBarUtils;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *
 * @author sundxing
 */

public class GuideRandomCloseActivity extends HSAppCompatActivity {

    public static final String EVENT_TURNOFF = "event_random_theme_turnoff";
    public static final String EVENT_KEEP = "event_random_theme_keep";


    @IntDef({DETAIL, SETTINGS})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Source {}

    public static final int DETAIL = 1;
    public static final int SETTINGS = 2;

    private int mFrom;

    public static void start(Context context, @Source int from, boolean fullScreen) {
        Intent starter = new Intent(context, GuideRandomCloseActivity.class);
        starter.putExtra("fullscreen", fullScreen);
        starter.putExtra("from", from);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean isFullScreen = getIntent().getBooleanExtra("fullscreen", true);
        mFrom = getIntent().getIntExtra("from",0);

        setContentView(R.layout.alert_random_close);
        if (isFullScreen) {
            StatusBarUtils.hideStatusBar(this);
        }

        Ap.RandomTheme.logEvent(mFrom == DETAIL ? "detail_page_retain_alert_show" : "settings_retain_alert_show");
        LauncherAnalytics.logEvent(mFrom == DETAIL ? "detail_page_retain_alert_show_round2" : "settings_retain_alert_show_round2");

        TextView textViewDesc = findViewById(R.id.alert_random_desc);
        textViewDesc.setText(mFrom == DETAIL ? R.string.alert_turn_off_random_desc_beset : R.string.alert_turn_off_random_desc);

        TextView enableBtn = (TextView) findViewById(R.id.button_keep);
        enableBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                HSGlobalNotificationCenter.sendNotification(EVENT_KEEP);
                Ap.RandomTheme.logEvent(mFrom == DETAIL ? "detail_page_retain_alert_cancel_click" : "settings_retain_alert_cancel_click");
                LauncherAnalytics.logEvent(mFrom == DETAIL ? "detail_page_retain_alert_cancel_click_round2" : "settings_retain_alert_cancel_click_round2");
            }
        });

        TextView disableBtn = (TextView) findViewById(R.id.button_turn_off);
        disableBtn.setText(mFrom == DETAIL ? R.string.alert_button_continue : R.string.alert_button_turn_off);
        disableBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                HSGlobalNotificationCenter.sendNotification(EVENT_TURNOFF);
                Ap.RandomTheme.logEvent(mFrom == DETAIL ? "detail_page_retain_alert_set_click" : "settings_retain_alert_close_click");
                LauncherAnalytics.logEvent(mFrom == DETAIL ? "detail_page_retain_alert_set_click_round2" : "settings_retain_alert_close_click_round2");
            }
        });

    }

}
