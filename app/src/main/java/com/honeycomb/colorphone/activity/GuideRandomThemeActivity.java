package com.honeycomb.colorphone.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.honeycomb.colorphone.Ap;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.theme.RandomTheme;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.honeycomb.colorphone.util.StatusBarUtils;
import com.ihs.app.framework.activity.HSAppCompatActivity;

/**
 * Created by sundxing on 17/9/13.
 */

public class GuideRandomThemeActivity extends HSAppCompatActivity {

    public static void start(Context context, boolean fullScreen) {
        Intent starter = new Intent(context, GuideRandomThemeActivity.class);
        starter.putExtra("fullscreen", fullScreen);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean isFullScreen = getIntent().getBooleanExtra("fullscreen", true);
        setContentView(R.layout.guide_random_feature);
        if (isFullScreen) {
            StatusBarUtils.hideStatusBar(this);
        }
        Ap.RandomTheme.logEvent("random_theme_guide_show");
        LauncherAnalytics.logEvent("random_theme_guide_show_round2");

        TextView textView = findViewById(R.id.theme_guide_desc);
        textView.setText(Ap.RandomTheme.randomThemeGuideDesc());

        TextView enableBtn = (TextView) findViewById(R.id.guide_random_ok);
        enableBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RandomTheme.getInstance().setUserSettingsEnable(true);
                Ap.RandomTheme.logEvent("random_theme_guide_ok_click");
                LauncherAnalytics.logEvent("random_theme_guide_ok_click_round2");
                finish();
            }
        });

    }

}
