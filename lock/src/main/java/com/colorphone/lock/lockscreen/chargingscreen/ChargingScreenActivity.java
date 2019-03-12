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
import com.colorphone.lock.lockscreen.LockScreenStarter;
import com.ihs.app.alerts.HSAlertMgr;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Threads;

import static android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN;

public class ChargingScreenActivity extends HSAppCompatActivity {

    private static final String TAG = "CHARGING_SCREEN_ACTIVITY";
    private ChargingScreen mScreen;
    public static boolean exist;

    private Runnable displaySuccessChecker = new Runnable() {
        @Override
        public void run() {
            LockScreenStarter.getInstance().onScreenDisplayed();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HSLog.d(TAG, "onCreate()");
        exist = true;

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
        mScreen.setActivityMode(true);
        mScreen.setup(((ViewGroup)findViewById(R.id.charging_screen_activity)), getIntent().getExtras());
    }

    @Override
    protected void onStart() {
        super.onStart();
        HSLog.d(TAG, "onStart()");
        mScreen.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Threads.postOnMainThreadDelayed(displaySuccessChecker, 1000);
    }

    @Override
    protected void onPause() {
        Threads.removeOnMainThread(displaySuccessChecker);
        super.onPause();
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
        exist = false;
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        mScreen.onBackPressed();
        DismissKeyguradActivity.startSelfIfKeyguardSecure(ChargingScreenActivity.this);
    }

}
