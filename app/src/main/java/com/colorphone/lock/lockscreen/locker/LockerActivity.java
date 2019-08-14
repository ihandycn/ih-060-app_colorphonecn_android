package com.colorphone.lock.lockscreen.locker;

import android.view.ViewGroup;

import com.colorphone.lock.LockerCustomConfig;
import com.colorphone.lock.lockscreen.BaseKeyguardActivity;
import com.colorphone.lock.lockscreen.chargingscreen.TimeDurationLogger;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.util.Analytics;

public class LockerActivity extends BaseKeyguardActivity {

    private Locker mLocker;

    private boolean noNavigationPadding = false;
    public static boolean exist = false;

    @Override
    protected void onInitView() {
        try {
            setContentView(R.layout.activity_locker);
            mLocker = new Locker();
            mLocker.setActivityMode(true);
            mLocker.setup(((ViewGroup)findViewById(R.id.activity_locker)), null);
            exist = true;
        } catch (Exception e) {
            Analytics.logEvent("LockerStartErr", true, "Type", e.getMessage());
            finish();
        }
    }

    @Override
    protected boolean fullScreen() {
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLocker.onStart();
        TimeDurationLogger.start("LockScreen");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLocker.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocker.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLocker.onStop();
        TimeDurationLogger.stop();
    }

    @Override
    protected void onDestroy() {
        exist = false;
        super.onDestroy();
        mLocker.onDestroy();
    }

    @Override
    public void finish() {
        super.finish();
        LockerCustomConfig.getLogger().logEvent("ColorPhone_LockScreen_UnlockType",
                "Type", mUserPresentWithoutSlide ? "untouch" : "touch");
    }

    @Override
    public void onBackPressed() {
        mLocker.onBackPressed();
    }


}
