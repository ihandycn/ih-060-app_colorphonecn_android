package com.colorphone.lock.boost;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.format.DateUtils;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.device.clean.memory.HSAppMemory;
import com.ihs.device.clean.memory.HSAppMemoryManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A helper class for calculating displayed (sometimes fake) RAM usage percentage.
 */
@SuppressWarnings("WeakerAccess")
public class RamUsageDisplayUpdater {
    private static final String TAG = RamUsageDisplayUpdater.class.getSimpleName();

    private static final int RAM_OPTIMIZED_VALUE = 35;

    private static final int RAM_UPDATE_MESSAGE = 1;
    private static final long RAM_UPDATE_INTERVAL = 3 * DateUtils.MINUTE_IN_MILLIS;
    private static final long FAKE_INTERVAL = 600000; // 10 minutes to move from fake value to real value

    public static RamUsageDisplayUpdater sInstance = new RamUsageDisplayUpdater();
    private long mCleandSize;

    public static RamUsageDisplayUpdater getInstance() {
        return sInstance;
    }

    public interface RamUsageChangeListener {
        void onDisplayedRamUsageChange(int displayedRamUsage);

        void onBoostComplete(int afterBoostRamUsage);
    }

    final List<WeakReference<RamUsageChangeListener>> mListenerRefs = new ArrayList<>(3);

    int mRealRamUsage;
    private int mFakeRamUsage;
    private int mLastBoostRamUsage = -1;
    int mDisplayedRamUsage;
    private long mLastBoostTime = -FAKE_INTERVAL - 1;

    private boolean mBoosting;
    private boolean mUpdatingRamUsage;

    @SuppressWarnings({"HandlerLeak", "FieldCanBeLocal"})
    private Handler mRamUpdateHandler = new Handler() {
        private static final long DEBOUNCING_INTERVAL = 1000;

        private long mLastUpdateTime;

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            HSLog.d("MemoryBoost", "update ram usage display");
            if (mBoosting) {
                // Do not update while boosting
                scheduleNext();
                return;
            }
            switch (msg.what) {
                case RAM_UPDATE_MESSAGE:
                    performUpdate();
                    scheduleNext();
                    break;
            }
        }

        private void performUpdate() {
            long now = SystemClock.uptimeMillis();
            if (now - mLastUpdateTime < DEBOUNCING_INTERVAL) {
                HSLog.v(TAG, "Skip frequent RAM update");
                return;
            }
            mLastUpdateTime = now;
            performUpdateDebounced();
        }

        private void performUpdateDebounced() {
            mRealRamUsage = DeviceManager.getInstance().getRamUsage();
            long currentTime = SystemClock.uptimeMillis();
            int lastDisplayedRamUsage = mDisplayedRamUsage;
            if (currentTime - mLastBoostTime > FAKE_INTERVAL) {
                mDisplayedRamUsage = mRealRamUsage;
            } else {
                float ratio = (float) (currentTime - mLastBoostTime) / FAKE_INTERVAL;
                mDisplayedRamUsage = Math.round(ratio * mRealRamUsage + (1 - ratio) * mFakeRamUsage);
            }
            if (lastDisplayedRamUsage != mDisplayedRamUsage) {
                synchronized (mListenerRefs) {
                    for (WeakReference<RamUsageChangeListener> listenerRef : mListenerRefs) {
                        RamUsageChangeListener listener = listenerRef.get();
                        if (listener != null) {
                            listener.onDisplayedRamUsageChange(mDisplayedRamUsage);
                        }
                    }
                }
            }
            HSLog.v(TAG, "Displayed: " + mDisplayedRamUsage +
                    ", previous: " + lastDisplayedRamUsage);
        }

        private void scheduleNext() {
            if (mUpdatingRamUsage) {
                sendEmptyMessageDelayed(RAM_UPDATE_MESSAGE, RAM_UPDATE_INTERVAL);
            }
        }
    };

    private RamUsageDisplayUpdater() {
        mDisplayedRamUsage = mRealRamUsage = DeviceManager.getInstance().getRamUsage();

        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                    HSLog.i(TAG, "Screen off, stop updating RAM usage");
                    stopUpdatingRamUsage();
                } else if (Intent.ACTION_USER_PRESENT.equals(action)
                        || Intent.ACTION_SCREEN_ON.equals(action)) {
                    HSLog.i(TAG, "Screen on, start updating RAM usage");
                    startUpdatingRamUsage();
                }
            }
        };
        HSApplication.getContext().registerReceiver(receiver, filter);
        startUpdatingRamUsage();
    }

    public long getCleandSize() {
        return mCleandSize;
    }

    public int getDisplayedRamUsage() {
        return mDisplayedRamUsage;
    }

    public void startUpdatingRamUsage() {
        mUpdatingRamUsage = true;
        mRamUpdateHandler.removeCallbacksAndMessages(null);
        mRamUpdateHandler.sendEmptyMessage(RAM_UPDATE_MESSAGE);
    }

    private void stopUpdatingRamUsage() {
        mUpdatingRamUsage = false;
        mRamUpdateHandler.removeCallbacksAndMessages(null);
    }

    public int startBoost() {
        if (mDisplayedRamUsage <= RAM_OPTIMIZED_VALUE) {
            return -1;
        }
        if (mLastBoostRamUsage > 0 && mDisplayedRamUsage > mLastBoostRamUsage) {
            mDisplayedRamUsage = mFakeRamUsage = mLastBoostRamUsage;
        } else {
            Random random = new Random();
            mDisplayedRamUsage = mFakeRamUsage = random.nextInt(7) + 29;
        }

        mBoosting = true;
        HSAppMemoryManager.getInstance().startFullClean(new HSAppMemoryManager.MemoryTaskListener() {
            @Override
            public void onStarted() {
            }

            @Override
            public void onProgressUpdated(int i, int i1, HSAppMemory hsAppMemory) {
            }

            @Override
            public void onSucceeded(List<HSAppMemory> list, long l) {
                postBoostCleanedSize(l);
            }

            @Override
            public void onFailed(int i, String s) {
            }
        });

        return mFakeRamUsage;
    }

    public void addRamUsageChangeListener(RamUsageChangeListener listener) {
        synchronized (mListenerRefs) {
            mListenerRefs.add(new WeakReference<>(listener));
        }
    }

    public void removeRamUsageChangeListener(RamUsageChangeListener listener) {
        synchronized (mListenerRefs) {
            List<WeakReference<RamUsageChangeListener>> listenerToRemove = new ArrayList<>(1);
            for (WeakReference<RamUsageChangeListener> listenerRef : mListenerRefs) {
                RamUsageChangeListener listenerFromRef = listenerRef.get();
                if (listenerFromRef != null && listenerFromRef.equals(listener)) {
                    listenerToRemove.add(listenerRef);
                }
            }
            mListenerRefs.removeAll(listenerToRemove);
        }
    }

    public void postBoostCleanedSize(long cleanedSize) {
        mCleandSize = cleanedSize;
        int totalRam = DeviceManager.getInstance().getRamUsage();
        int cleanedPercentage = 0;
        if (totalRam > 0) {
            cleanedPercentage = Math.round(100f * cleanedSize / DeviceManager.getInstance().getTotalRam());
        }
        int afterBoostRamUsage = mRealRamUsage - cleanedPercentage;
        mLastBoostRamUsage = afterBoostRamUsage;

        mBoosting = false;
        mLastBoostTime = SystemClock.uptimeMillis();
        synchronized (mListenerRefs) {
            for (WeakReference<RamUsageChangeListener> listenerRef : mListenerRefs) {
                RamUsageChangeListener listener = listenerRef.get();
                if (listener != null) {
                    listener.onBoostComplete(afterBoostRamUsage);
                }
            }
        }
    }
}
