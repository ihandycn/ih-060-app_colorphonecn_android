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
import com.airbnb.lottie.LottieAnimationView;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.notification.CleanGuideCondition;
import com.honeycomb.colorphone.customize.activity.CustomizeActivity;
import com.honeycomb.colorphone.toolbar.NotificationManager;
import com.superapps.util.Navigations;

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
        mLottieAnimationView.setOnClickListener(v -> mLottieAnimationView.playAnimation());
        final LottieAnimationView lottieAnimationView2 = findViewById(R.id.lottie_anim_2);
        lottieAnimationView2.setOnClickListener(v -> lottieAnimationView2.playAnimation());
        mInCallWindow = new InCallWindow(this);

    }

    public void startCallRingingWindow(View view) {
        String number = editText.getText().toString().trim();
        mInCallWindow.show(TextUtils.isEmpty(number) ? "8888888" : number);
        mHandler.postDelayed(() -> mInCallWindow.endFlashCall(), 8000);

    }

    @Override
    protected void onStop() {
        mHandler.removeCallbacksAndMessages(null);
        super.onStop();
    }

    public void startRate(View view) {
//        RateAlertActivity.showRateFrom(this, FiveStarRateTip.From.SET_THEME);
        NotificationManager.cancelSafely(NotificationManager.NOTIFICATION_ID_CLEAN_GUIDE);
    }

    public void startRecentApp(View view) {
//        CleanGuideActivity.start(CleanGuideCondition.CLEAN_GUIDE_TYPE_BOOST_APPS);
        CleanGuideCondition.getInstance().clearData();
    }

    public void checkFloatWindow(View view) {
//        CleanGuideCondition.getInstance().showCleanGuideIfNeeded();
        CleanGuideCondition.getInstance().showCleanGuideIfNeeded();
    }

    public void startWallpaper(View view) {
        Intent launchIntent = CustomizeActivity.getLaunchIntent(this,
                "From App drawer", 0);

        Navigations.startActivitySafely(this, launchIntent);

    }
}
