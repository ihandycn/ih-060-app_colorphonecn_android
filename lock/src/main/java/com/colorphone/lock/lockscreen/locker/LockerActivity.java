package com.colorphone.lock.lockscreen.locker;

import android.os.Build;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;

import com.colorphone.lock.R;
import com.colorphone.lock.lockscreen.chargingscreen.ChargingScreenUtils;
import com.ihs.app.framework.activity.HSAppCompatActivity;

import static android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
import static android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
import static android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
import static android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN;

public class LockerActivity extends HSAppCompatActivity {

    private static final String TAG = "LOCKER_ACTIVITY";

    public static final String EVENT_FINISH_SELF = "locker_event_finish_self";
    public static final String EXTRA_SHOULD_DISMISS_KEYGUARD = "extra_should_dismiss_keyguard";
    public static final String PREF_KEY_CURRENT_WALLPAPER_HD_URL = "current_hd_wallpaper_url";
    private Locker mLocker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        // set translucent status bar & navigation bar
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(FLAG_TRANSLUCENT_STATUS);
            window.addFlags(FLAG_TRANSLUCENT_NAVIGATION);
        }
        if (!ChargingScreenUtils.isNativeLollipop()) {
            window.addFlags(FLAG_FULLSCREEN);
        }
        window.addFlags(FLAG_SHOW_WHEN_LOCKED);
        window.setSoftInputMode(SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (!LockerUtils.isKeyguardSecure(LockerActivity.this, false)) {
            window.addFlags(FLAG_DISMISS_KEYGUARD);
        }

        try {
            setContentView(R.layout.activity_locker);
        } catch (Exception e) {
            finish();
        }


        mLocker = new Locker();
        mLocker.setup(((ViewGroup)findViewById(R.id.activity_locker)), null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLocker.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLocker.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocker.onDestroy();
    }



    @Override
    public void onBackPressed() {
        mLocker.onBackPressed();
    }


}
