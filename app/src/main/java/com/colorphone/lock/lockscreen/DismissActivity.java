package com.colorphone.lock.lockscreen;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.ihs.app.framework.activity.HSActivity;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;

public class DismissActivity extends HSActivity {

    private static final String TAG = "DismissActivity";
    private static final String FINISH_SELF = "com.example.locker.lockscreen.LockScreenActivity.FINISH_SELF";

    // TODO whether can just use HSNotificationCenter
    INotificationObserver selfFinishObserver = new INotificationObserver() {

        @Override
        public void onReceive(String eventName, HSBundle bundle) {
            HSLog.i("DismissActivity123", "onReceive()");
            finish();
        }
    };

    @SuppressLint("InlinedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HSLog.i(TAG, "onCreate()");

        //                getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.flags |= 0xc000000;
        if (Build.VERSION.SDK_INT < 20) {
            layoutParams.flags |= 0x480000;
        }
        if (Build.VERSION.SDK_INT <= 18) {
            layoutParams.flags ^= 0xc000000;
        }

        int viewFlag = getWindow().getDecorView().getSystemUiVisibility();
        HSLog.e("show flag == " + viewFlag);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            viewFlag |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//            viewFlag |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            viewFlag |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        }

        viewFlag |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        layoutParams.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(layoutParams);

        getWindow().getDecorView().setSystemUiVisibility(viewFlag);

        HSGlobalNotificationCenter.addObserver(FINISH_SELF, selfFinishObserver);

        if (!FloatWindowController.getInstance().isLockScreenShown()) {
            finish();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        HSLog.i(TAG, "onNewIntent()");

        setIntent(intent);

        if (null == FloatWindowController.getInstance() || !FloatWindowController.getInstance().isLockScreenShown()) {
            finish();
        }
    }

    @Override
    protected void onStart() {
        HSLog.i(TAG, "onStart()");
        super.onStart();
    }

    @Override
    protected void onStop() {
        HSLog.i(TAG, "onStop()");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HSLog.i(TAG, "onDestroy()");
        HSGlobalNotificationCenter.removeObserver(selfFinishObserver);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        HSLog.i(TAG, "onTouchEvent()");
        finish();
        return true;
    }

    public static void hide() {
        HSLog.i(TAG, "hide()");
        HSGlobalNotificationCenter.sendNotification(FINISH_SELF);
    }

}
