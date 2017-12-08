package com.honeycomb.colorphone.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.acb.call.service.InCallWindow;
import com.honeycomb.colorphone.R;

/**
 * Created by sundxing on 17/11/22.
 */

public class TestActivity extends AppCompatActivity {

    Handler mHandler = new Handler();
    EditText editText;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        editText = findViewById(R.id.test_number);
    }

    public void startCallRingingWindow(View view) {
        String number = editText.getText().toString().trim();
        InCallWindow.show(this, TextUtils.isEmpty(number) ? "8888888" : number);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                InCallWindow.dismiss(TestActivity.this.getApplicationContext());
            }
        }, 4000);

    }

}
