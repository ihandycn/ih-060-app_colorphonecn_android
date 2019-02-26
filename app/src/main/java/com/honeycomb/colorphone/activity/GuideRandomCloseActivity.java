package com.honeycomb.colorphone.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.util.StatusBarUtils;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;

/**
 *
 * @author sundxing
 */

public class GuideRandomCloseActivity extends HSAppCompatActivity {

    public static final String EVENT_TURNOFF = "event_random_theme_turnoff";
    public static final String EVENT_KEEP = "event_random_theme_keep";


    public static void start(Context context, boolean fullScreen) {
        Intent starter = new Intent(context, GuideRandomCloseActivity.class);
        starter.putExtra("fullscreen", fullScreen);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean isFullScreen = getIntent().getBooleanExtra("fullscreen", true);
        setContentView(R.layout.alert_random_close);
        if (isFullScreen) {
            StatusBarUtils.hideStatusBar(this);
        }

        TextView enableBtn = (TextView) findViewById(R.id.button_keep);
        enableBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                HSGlobalNotificationCenter.sendNotification(EVENT_KEEP);
            }
        });

        TextView disableBtn = (TextView) findViewById(R.id.button_turn_off);
        disableBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                HSGlobalNotificationCenter.sendNotification(EVENT_TURNOFF);
            }
        });

    }

}
