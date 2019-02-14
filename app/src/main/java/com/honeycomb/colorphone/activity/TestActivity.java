package com.honeycomb.colorphone.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.acb.call.service.InCallWindow;
import com.acb.colorphone.permissions.FloatWindowManager;
import com.airbnb.lottie.LottieAnimationView;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.dialog.FiveStarRateTip;
import com.honeycomb.colorphone.recentapp.SmartAssistantActivity;
import com.honeycomb.colorphone.themerecommend.ThemeRecommendActivity;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.framework.HSApplication;

/**
 * Created by sundxing on 17/11/22.
 */

public class TestActivity extends AppCompatActivity {

    Handler mHandler = new Handler();
    EditText editText;
    LottieAnimationView mLottieAnimationView;
    InCallWindow mInCallWindow;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        editText = findViewById(R.id.test_number);
        mLottieAnimationView = findViewById(R.id.lottie_anim);
        mLottieAnimationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLottieAnimationView.playAnimation();
            }
        });
        final LottieAnimationView lottieAnimationView2 = findViewById(R.id.lottie_anim_2);
        lottieAnimationView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lottieAnimationView2.playAnimation();
            }
        });
        mInCallWindow = new InCallWindow(this);

    }

    public void startCallRingingWindow(View view) {
        String number = editText.getText().toString().trim();
        mInCallWindow.show(TextUtils.isEmpty(number) ? "8888888" : number);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mInCallWindow.endFlashCall();
            }
        }, 8000);

    }

    @Override
    protected void onStop() {
        mHandler.removeCallbacksAndMessages(null);
        super.onStop();
    }


    public void startRate(View view) {
        RateAlertActivity.showRateFrom(this, FiveStarRateTip.From.END_CALL);
    }

    public void startRecentApp(View view) {
        Intent recentApp = new Intent(HSApplication.getContext(), SmartAssistantActivity.class);
        Utils.startActivitySafely(HSApplication.getContext(), recentApp);
    }

    public void checkFloatWindow(View view) {
        FloatWindowManager.getInstance().applyOrShowFloatWindow(this);
    }

    public void themeRecommend(View view) {
        String number = editText.getText().toString().trim();
        ThemeRecommendActivity.start(TestActivity.this, TextUtils.isEmpty(number) ? "13800138000" : number);
    }
}
