package com.honeycomb.colorphone.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.acb.call.service.InCallWindow;
import com.honeycomb.colorphone.R;

/**
 * Created by sundxing on 17/11/22.
 */

public class TestActivity extends AppCompatActivity {

    Handler mHandler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
    }

    public void startCallRingingWindow(View view) {
        InCallWindow.show(this, "8888888");
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                InCallWindow.dismiss(TestActivity.this.getApplicationContext());
            }
        }, 4000);

    }

}
