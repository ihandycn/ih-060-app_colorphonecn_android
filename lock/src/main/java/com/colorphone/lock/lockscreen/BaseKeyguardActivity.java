package com.colorphone.lock.lockscreen;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager.LayoutParams;

import com.colorphone.lock.lockscreen.chargingscreen.ChargingScreenUtils;
import com.ihs.app.alerts.HSAlertMgr;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.utils.HSLog;

import static android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN;

public abstract class BaseKeyguardActivity extends HSAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        HSAlertMgr.delayRateAlert();

        Window window = getWindow();
        window.addFlags(LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        if (fullScreen()) {
            if (!ChargingScreenUtils.isNativeLollipop()) {
                window.addFlags(LayoutParams.FLAG_FULLSCREEN);
            }
        } else {
            // 透明状态栏
            window.addFlags(LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        window.addFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.setSoftInputMode(SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        onInitView();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        HSLog.i("LockManager", "BaseKeyguardActivity onDestroy");
        super.onDestroy();
    }

    @Override public void finish() {
        HSLog.i("LockManager", "BaseKeyguardActivity finish");
        super.finish();
    }

    protected abstract void onInitView();

    protected boolean fullScreen(){
        return true;
    }
}
