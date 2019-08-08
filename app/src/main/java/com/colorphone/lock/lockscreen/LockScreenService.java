package com.colorphone.lock.lockscreen;

import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.ihs.app.framework.HSApplication;



@SuppressWarnings("deprecation")
public class LockScreenService extends Service {

    private static final String TAG = LockScreenService.class.getSimpleName();

    private static final String KEYGUARD_LOCK_NAME = "KeyguardLock";
    private static  String START_ACTION = HSApplication.getContext().getPackageName() + ".lockscreen.LockService";

    private boolean setForeground;
    private KeyguardLock keyguardLock;

    @Override
    public void onCreate() {
        super.onCreate();

        FloatWindowController.init(this);
        FloatWindowController.getInstance().start();

        KeyguardManager keyguardMgr = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        keyguardLock = keyguardMgr.newKeyguardLock(KEYGUARD_LOCK_NAME);
        try {
            keyguardLock.disableKeyguard();
        } catch (Exception e) {
            keyguardLock = null;
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        startService(getIntent());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!setForeground) {
            setForeground = true;
        }
        return START_STICKY;
    }

    public static Intent getIntent() {
        Intent intent = new Intent();
        intent.setAction(START_ACTION);
        intent.setPackage(HSApplication.getContext().getPackageName());
        return intent;
    }
}
