package com.colorphone.lock.lockscreen.locker;

import android.os.Build;
import android.view.ViewGroup;

import com.colorphone.lock.R;
import com.colorphone.lock.lockscreen.BaseKeyguardActivity;
import com.colorphone.lock.lockscreen.chargingscreen.TimeDurationLogger;

public class LockerActivity extends BaseKeyguardActivity {

    private static final String TAG = "LOCKER_ACTIVITY";
    public static final String EVENT_FINISH_SELF = "locker_event_finish_self";
    public static final String EXTRA_SHOULD_DISMISS_KEYGUARD = "extra_should_dismiss_keyguard";
    public static final String PREF_KEY_CURRENT_WALLPAPER_HD_URL = "current_hd_wallpaper_url";
    private Locker mLocker;

    private boolean noNavigationPadding = false;

    public static boolean exit = false;

    @Override
    protected void onInitView() {
        try {
            setContentView(R.layout.activity_locker);

//            NotchTools.getFullScreenTools().showNavigation(true).fullScreenUseStatus(this, new OnNotchCallBack() {
//                @Override
//                public void onNotchPropertyCallback(NotchProperty notchProperty) {
//
//                }
//            });

            mLocker = new Locker();
            mLocker.setActivityMode(true);
            mLocker.setup(((ViewGroup)findViewById(R.id.activity_locker)), null);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && !noNavigationPadding) {
                ViewGroup container = (ViewGroup) findViewById(R.id.locker_pager);
                //container.setPadding(0, 0, 0, Dimensions.getNavigationBarHeight(this));
            }
        } catch (Exception e) {
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
        super.onDestroy();
        mLocker.onDestroy();
        exit = false;
    }

    @Override
    public void onBackPressed() {
        mLocker.onBackPressed();
    }


}
