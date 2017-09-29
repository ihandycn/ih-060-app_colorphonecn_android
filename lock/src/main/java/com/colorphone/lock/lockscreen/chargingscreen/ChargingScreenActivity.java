package com.colorphone.lock.lockscreen.chargingscreen;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;

import com.colorphone.lock.R;
import com.colorphone.lock.lockscreen.DismissKeyguradActivity;
import com.ihs.app.alerts.HSAlertMgr;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.utils.HSLog;

import static android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN;

public class ChargingScreenActivity extends HSAppCompatActivity {

    private static final String TAG = "CHARGING_SCREEN_ACTIVITY";
    private ChargingScreen mScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HSLog.d(TAG, "onCreate()");

        HSAlertMgr.delayRateAlert();

        boolean keyguardFlag = false;

        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);

        if (keyguardManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            keyguardFlag = keyguardManager.isKeyguardSecure();
            HSLog.d("isKeyguardSecure: " + keyguardManager.isKeyguardSecure()
                    + " isKeyguardLocked: " + keyguardManager.isKeyguardLocked());
        }

        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        if (!ChargingScreenUtils.isNativeLollipop()) {
            window.addFlags(LayoutParams.FLAG_FULLSCREEN);
        }
        window.addFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.setSoftInputMode(SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (!keyguardFlag) {
            window.addFlags(LayoutParams.FLAG_DISMISS_KEYGUARD);
        }

        setContentView(R.layout.activity_charging_screen);

        mScreen = new ChargingScreen();
        mScreen.setup(((ViewGroup)findViewById(R.id.charging_screen_activity)), getIntent().getExtras());
        HSAnalytics.logEvent("Charging_Screen__Shown_Init");
    }

    @Override
    protected void onStart() {
        super.onStart();
        HSLog.d(TAG, "onStart()");
        mScreen.onStart();
        HSAnalytics.logEvent("Charging_Screen__Shown_Resume");
    }

    @Override
    protected void onStop() {
        super.onStop();
        mScreen.onStop();
        HSLog.d(TAG, "onStop()");
    }

    @Override
    protected void onDestroy() {
        mScreen.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {


        mScreen.onBackPressed();

        DismissKeyguradActivity.startSelfIfKeyguardSecure(ChargingScreenActivity.this);
    }

}
