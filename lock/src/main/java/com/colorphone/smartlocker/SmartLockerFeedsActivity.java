package com.colorphone.smartlocker;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.colorphone.lock.R;
import com.colorphone.smartlocker.utils.StatusBarUtils;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.utils.HSLog;

public class SmartLockerFeedsActivity extends HSAppCompatActivity {

    private static final String TAG = "SmartLockerFeedsActivity";
    private SmartLockerScreen smartLockerScreen;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HSLog.d(TAG, "SmartLockerFeedsActivity onCreate");
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        StatusBarUtils.setTransparent(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            StatusBarUtils.addTranslucentView(this, StatusBarUtils.DEFAULT_STATUS_BAR_ALPHA);
        }

        setContentView(R.layout.activity_smart_locker_feeds);

        smartLockerScreen = new SmartLockerScreen();
        smartLockerScreen.setActivityMode(true);
        smartLockerScreen.setup(((ViewGroup) findViewById(R.id.activity_smart_locker_feeds)), getIntent().getExtras());

        SmartLockerManager.getInstance().setExist(true);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        HSLog.d(TAG, "SmartLockerFeedsActivity onNewIntent");
        smartLockerScreen.onNewIntent();
    }

    @Override
    protected void onStart() {
        super.onStart();
        smartLockerScreen.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        smartLockerScreen.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        smartLockerScreen.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        smartLockerScreen.onStop();
    }

    @Override
    public void onBackPressed() {
        smartLockerScreen.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        smartLockerScreen.onDestroy();
        SmartLockerManager.getInstance().setExist(false);
    }
}
