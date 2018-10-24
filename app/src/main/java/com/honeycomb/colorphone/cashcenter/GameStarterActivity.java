package com.honeycomb.colorphone.cashcenter;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;

import net.appcloudbox.h5game.AcbH5GameInfo;

/**
 * Created by sundxing on 2018/6/15.
 */

public class GameStarterActivity extends Activity {

    Handler mHandler = new Handler();
    private AcbH5GameInfo mGame;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CashUtils.startWheelActivity(this, CashUtils.Source.Shortcut);
        CashUtils.Event.logEvent("colorphone_earncash_desktop_icon_click");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        },2000);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
