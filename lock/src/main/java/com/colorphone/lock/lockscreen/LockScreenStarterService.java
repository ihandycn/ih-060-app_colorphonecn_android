package com.colorphone.lock.lockscreen;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.ihs.commons.utils.HSLog;

/**
 * Receives start command issued by {@link LockScreenStarter} that works in ":work" process.
 * This service works in main process.
 *
 * Historically, a broadcast receiver had been expected to work too, before it turned out that
 * broadcasts cannot bring up their receivers on some devices (eg. Samsung API 18, #177).
 * So we switched to this service. Don't roll back without a reason.
 */
public class LockScreenStarterService extends Service {

    private static final String TAG = LockScreenStarterService.class.getSimpleName();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        HSLog.d(TAG, "onStartCommand" + intent);
        LockScreenStarter.handleStart(intent);
        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    @Nullable public IBinder onBind(Intent intent) {
        return null;
    }
}
