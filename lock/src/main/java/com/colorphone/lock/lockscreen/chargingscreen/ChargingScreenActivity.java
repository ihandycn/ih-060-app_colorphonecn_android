package com.colorphone.lock.lockscreen.chargingscreen;

import android.view.View;
import android.view.ViewGroup;

import com.colorphone.lock.R;
import com.colorphone.lock.fullscreen.NotchTools;
import com.colorphone.lock.fullscreen.core.NotchProperty;
import com.colorphone.lock.fullscreen.core.OnNotchCallBack;
import com.colorphone.lock.lockscreen.BaseKeyguardActivity;
import com.colorphone.lock.lockscreen.DismissKeyguradActivity;
import com.colorphone.lock.lockscreen.LockScreenStarter;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Threads;

public class ChargingScreenActivity extends BaseKeyguardActivity {

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
    protected void onInitView() {
        setContentView(R.layout.activity_charging_screen);

        mScreen = new ChargingScreen();
        mScreen.setActivityMode(true);
        mScreen.setup(((ViewGroup)findViewById(R.id.charging_screen_activity)), getIntent().getExtras());

        NotchTools.getFullScreenTools().showNavigation(true).fullScreenUseStatus(this, new OnNotchCallBack() {
            @Override
            public void onNotchPropertyCallback(NotchProperty notchProperty) {
                HSLog.d("Notch", "has notch : " + notchProperty.isNotch());
                View titleLayout = findViewById(R.id.charging_screen_title_container);
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) titleLayout.getLayoutParams();
                if (params != null) {
                    params.topMargin += notchProperty.getMarginTop();
                    titleLayout.setLayoutParams(params);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        HSLog.d(TAG, "onStart()");
        mScreen.onStart();
        TimeDurationLogger.start("ChargingScreen");
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
        TimeDurationLogger.stop();
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
