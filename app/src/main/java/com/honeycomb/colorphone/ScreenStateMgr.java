package com.honeycomb.colorphone;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSLog;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Checking the state of the screen, and save the state whether it is on or off.
 */
public class ScreenStateMgr {
    public final static String ACTION_SCREEN_ON = "ACTION_SCREEN_ON";
    public final static String ACTION_SCREEN_OFF = "ACTION_SCREEN_OFF";
    private final static int MSG_SCREEN_ON = 1;
    private final static int MSG_SCREEN_OFF = 2;

    private boolean isScreenOn;

    private boolean allowCrash = false;

    private ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> mRunningTask;
    private long lastActivateTime;

    private static class ScreenStateHolder {
        private static final ScreenStateMgr instance = new ScreenStateMgr();
    }

    private ScreenStateMgr() {
    }

    public static ScreenStateMgr getInstance() {
        return ScreenStateHolder.instance;
    }

    /**
     * Send ACTION_SCREEN_ON when screen state changes from off to on.
     * Send ACTION_SCREEN_OFF when screen state changes from on to off.
     */
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SCREEN_ON:
                    HSGlobalNotificationCenter.sendNotification(ACTION_SCREEN_ON);
                    break;
                case MSG_SCREEN_OFF:
                    HSGlobalNotificationCenter.sendNotification(ACTION_SCREEN_OFF);
                    break;
            }
            super.handleMessage(msg);
        }

    };

    public boolean isScreenOn() {
        return isScreenOn;
    }

    public void setAllowCrash(boolean allowCrash) {
        this.allowCrash = allowCrash;
    }

    public void reset() {
        lastActivateTime = 0;
        scheduledThreadPool.shutdownNow();
        scheduledThreadPool = Executors.newScheduledThreadPool(1);
    }

    public void startTimerTask() {
        if (mRunningTask != null && !mRunningTask.isDone()) {
            long timeInterval = System.currentTimeMillis() - lastActivateTime;
            if (timeInterval > 2000) {
                reset();
                String logMsg = "screen state task running, but interval too long! " + timeInterval;
                if (allowCrash) {
                    throw new IllegalStateException(logMsg);
                }
            }
            HSLog.i("screen state task running");
            return;
        }

        HSLog.i("screen state new");

        PowerManager pm = (PowerManager) HSApplication.getContext().getSystemService(Context.POWER_SERVICE);
        isScreenOn = pm.isScreenOn();
        mRunningTask = scheduledThreadPool.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                lastActivateTime = System.currentTimeMillis();
                PowerManager pm = (PowerManager) HSApplication.getContext().getSystemService(Context.POWER_SERVICE);
                if (pm.isScreenOn() && !isScreenOn) {
                    HSLog.i("screen state from off to on");
                    isScreenOn = true;
                    Message message = new Message();
                    message.what = MSG_SCREEN_ON;
                    handler.sendMessage(message);
                }
                if (!pm.isScreenOn() && isScreenOn) {
                    HSLog.i("screen state from on to off");
                    isScreenOn = false;
                    Message message = new Message();
                    message.what = MSG_SCREEN_OFF;
                    handler.sendMessage(message);
                }
            }
        }, 10, 200, TimeUnit.MILLISECONDS);
    }

    public void stopTimerTask() {
        if (mRunningTask != null) {
            mRunningTask.cancel(false);
        }
    }
}
