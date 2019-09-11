package com.honeycomb.colorphone.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.acb.call.customize.ScreenFlashManager;
import com.acb.call.customize.ScreenFlashSettings;
import com.acb.call.service.InCallWindow;
import com.acb.colorphone.permissions.AutoStartMIUIGuideActivity;
import com.acb.colorphone.permissions.PhoneHuawei8GuideActivity;
import com.acb.colorphone.permissions.WriteSettingsPopupGuideActivity;
import com.airbnb.lottie.LottieAnimationView;
import com.colorphone.ringtones.view.RingtoneEntranceActivity;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.dialog.FiveStarRateTip;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.ModuleUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Navigations;
import com.superapps.util.RuntimePermissions;
import com.superapps.util.Threads;

import java.util.ArrayList;
import java.util.List;
import com.superapps.util.Navigations;
import com.honeycomb.colorphone.boost.FloatWindowManager;
import com.honeycomb.colorphone.dialog.FiveStarRateTip;
import com.honeycomb.colorphone.feedback.HuaweiRateGuideDialog;

/**
 * Created by sundxing on 17/11/22.
 */

public class TestActivity extends AppCompatActivity {
    private static int FIRST_LAUNCH_PERMISSION_REQUEST = 1110;
    private static int PERMISSION_REQUEST = 1111;

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
        RateAlertActivity.showRateFrom(this, FiveStarRateTip.From.SET_THEME);
    }

    public void startRecentApp(View view) {
//        CleanGuideActivity.start(CleanGuideCondition.CLEAN_GUIDE_TYPE_BOOST_APPS);
        FloatWindowManager.getInstance().removeDialog(FloatWindowManager.getInstance().getDialog(HuaweiRateGuideDialog.class));

    }

    public void checkFloatWindow(View view) {
//        CleanGuideCondition.getInstance().showCleanGuideIfNeeded();
        FloatWindowManager.getInstance().showDialog(new HuaweiRateGuideDialog(this));
    }

    public void openRingtone(View view) {
        Navigations.startActivity(this, RingtoneEntranceActivity.class);
    }

}
