package com.colorphone.lock.lockscreen.chargingscreen;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.honeycomb.colorphone.R;
import com.ihs.app.framework.activity.HSActivity;

/**
 * Created by sundxing on 17/9/8.
 */

public class ChargingGuideActivity extends HSActivity {

    public static void start(Context context) {
        Intent starter = new Intent(context, ChargingGuideActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChargingScreenGuideView chargingScreenGuideView = (ChargingScreenGuideView) getLayoutInflater().inflate(R.layout.charging_screen_guide_view, null);
        setContentView(chargingScreenGuideView);
        chargingScreenGuideView.setOnCloseListener(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        });
    }
}
