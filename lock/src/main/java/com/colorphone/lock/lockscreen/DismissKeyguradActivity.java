package com.colorphone.lock.lockscreen;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.ViewTreeObserver;

import com.ihs.commons.utils.HSLog;
import com.superapps.util.Commons;

import static android.view.Window.FEATURE_NO_TITLE;
import static android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;

/**
 * We want show unlock pattern view automatically, so we create this activity with
 * FLAG_DISMISS_KEYGUARD.
 */
public class DismissKeyguradActivity extends Activity {

    private Handler handler= new Handler();
    private Runnable runnable = new Runnable() {
        public void run() {
            HSLog.i("LockManager", "DismissKeyguradActivity finish ");
            finish();
        }
    };
    private ViewTreeObserver.OnPreDrawListener onPreDrawListener;

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        requestWindowFeature(FEATURE_NO_TITLE);

        getWindow().addFlags(FLAG_DISMISS_KEYGUARD | FLAG_FULLSCREEN);

        HSLog.i("LockManager", "DismissKeyguradActivity onCreate ");

        onPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                getWindow().getDecorView().getViewTreeObserver().removeOnPreDrawListener(onPreDrawListener);
                HSLog.i("LockManager", "DismissKeyguradActivity post  finish ");
                handler.postDelayed(runnable, Commons.isKeyguardLocked(DismissKeyguradActivity.this, false) ? 1000 : 0);
                return true;
            }
        };
        getWindow().getDecorView().getViewTreeObserver().addOnPreDrawListener(onPreDrawListener);
    }

    public static void startSelfIfKeyguardSecure(Context context) {
        if (Commons.isKeyguardLocked(context, false)) {
            HSLog.i("LockManager", "startSelfIfKeyguardSecure ");
            Intent intent = new Intent(context, DismissKeyguradActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}
