package com.honeycomb.colorphone.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.honeycomb.colorphone.R;
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

        TextView enableBtn = (TextView) findViewById(R.id.guide_random_ok);
        enableBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });

    }

}
