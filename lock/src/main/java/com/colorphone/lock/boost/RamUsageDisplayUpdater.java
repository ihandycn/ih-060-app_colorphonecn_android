package com.colorphone.lock.boost;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

import com.ihs.commons.utils.HSLog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A helper class for calculating displayed (sometimes fake) RAM usage percentage.
 */
public class RamUsageDisplayUpdater {
    private static final String TAG = RamUsageDisplayUpdater.class.getSimpleName();

    private static final int RAM_MINIMUM_VALUE = 18;
    public static final int RAM_OPTIMIZED_VALUE = 35;
    private static final int RAM_LAST_MAX_VALUE = 48;

    private static final int RAM_UPDATE_MESSAGE = 1;
    private static final int RAM_UPDATE_INTERVAL = 10000;
    private static final int FAKE_INTERVAL = 600000; // 10 minutes to move from fake value to real value
    private static final int BOOST_LIB_CALLBACK_CHECK_TIME = 20000;
    private static final int BOOST_LIB_CALLBACK_CHECK_EVENT = 2;

    public static RamUsageDisplayUpdater sInstance = new RamUsageDisplayUpdater();

    public static RamUsageDisplayUpdater getInstance() {
        return sInstance;
    }

    public interface RamUsageChangeListener {
        void onDisplayedRamUsageChange(int displayedRamUsage, boolean isImmediatelyUpdate);

        void onBoostComplete(int afterBoostRamUsage);
    }

    private final List<WeakReference<RamUsageChangeListener>> mListenerRefs = new ArrayList<>(3);

    @SuppressWarnings("WeakerAccess")
    int mRealRamUsage;
    private int mFakeRamUsage;
    private int mLastBoostRamUsage = -1;
    @SuppressWarnings("WeakerAccess")
    int mDisplayedRamUsage;
    private long mLastBoostTime = -FAKE_INTERVAL - 1;

    private boolean mBoosting;
    private boolean mUpdatingRamUsage;
    private boolean mIsImmediatelyUpdateRamUsage;

    @SuppressWarnings({"HandlerLeak", "FieldCanBeLocal"})
    private Handler mRamUpdateHandler = new Handler(Looper.getMainLooper()) {
        private static final long DEBOUNCING_INTERVAL = 1000;

        private long mLastUpdateTime;

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            HSLog.d(TAG, "update ram usage display mBoosting = " + mBoosting + " mIsImmediatelyUpdateRamUsage = " + mIsImmediatelyUpdateRamUsage);
            if (mBoosting) {
                // Do not update while boosting but immediately update
                if (mIsImmediatelyUpdateRamUsage) {
                    performUpdate();
                }
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
            if (!mIsImmediatelyUpdateRamUsage && now - mLastUpdateTime < DEBOUNCING_INTERVAL) {
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

            HSLog.v(TAG, "Displayed: " + mDisplayedRamUsage + ", previous: " + lastDisplayedRamUsage + ", isImmediatelyUpdate:" + mIsImmediatelyUpdateRamUsage);
            if (mIsImmediatelyUpdateRamUsage || lastDisplayedRamUsage != mDisplayedRamUsage) {
                synchronized (mListenerRefs) {
                    for (WeakReference<RamUsageChangeListener> listenerRef : mListenerRefs) {
                        RamUsageChangeListener listener = listenerRef.get();
                        if (listener != null) {
                            listener.onDisplayedRamUsageChange(mDisplayedRamUsage, mIsImmediatelyUpdateRamUsage);
                        }
                    }
                    mIsImmediatelyUpdateRamUsage = false;
                }
            }
        }

        private void scheduleNext() {
            if (mUpdatingRamUsage) {
                sendEmptyMessageDelayed(RAM_UPDATE_MESSAGE, RAM_UPDATE_INTERVAL);
            }
        }
    };

    @SuppressWarnings({"HandlerLeak", "FieldCanBeLocal"})
    private Handler mBoostCallbackHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            mBoosting = false;
        }
    };

    private RamUsageDisplayUpdater() {
        mDisplayedRamUsage = mRealRamUsage = DeviceManager.getInstance().getRamUsage();
        startUpdatingRamUsage();
    }

    public int getDisplayedRamUsage() {
        return mDisplayedRamUsage;
    }

    public void startUpdatingRamUsage() {
        HSLog.i(TAG, "Start updating RAM usage");
        mUpdatingRamUsage = true;
        mRamUpdateHandler.removeCallbacksAndMessages(null);
        mRamUpdateHandler.sendEmptyMessage(RAM_UPDATE_MESSAGE);
    }

    public void stopUpdatingRamUsage() {
        HSLog.i(TAG, "Stop updating RAM usage");
        mUpdatingRamUsage = false;
        mRamUpdateHandler.removeCallbacksAndMessages(null);
    }

    public int startBoost(boolean forceUpdateIcon) {
        HSLog.d(TAG, "startBoost");
        if (mDisplayedRamUsage <= RAM_OPTIMIZED_VALUE) {
            return -1;
        }
        setFakeRamUsage();

        synchronized (mListenerRefs) {
            for (WeakReference<RamUsageChangeListener> listenerRef : mListenerRefs) {
                RamUsageChangeListener listener = listenerRef.get();
                if (listener != null) {
                    listener.onBoostComplete(mDisplayedRamUsage);
                }
            }
        }
        if (forceUpdateIcon) {
            for (WeakReference<RamUsageChangeListener> listenerRef : mListenerRefs) {
                RamUsageChangeListener listener = listenerRef.get();
                if (listener != null) {
                    listener.onDisplayedRamUsageChange(mDisplayedRamUsage, false);
                }
            }
        }

        mBoosting = true;
        startUpdatingRamUsage();
        mLastBoostTime = SystemClock.uptimeMillis();
        mBoostCallbackHandler.removeCallbacksAndMessages(null);
        mBoostCallbackHandler.sendEmptyMessageDelayed(BOOST_LIB_CALLBACK_CHECK_EVENT, BOOST_LIB_CALLBACK_CHECK_TIME);

        return mFakeRamUsage;
    }

    public void addRamUsageChangeListener(RamUsageChangeListener listener) {
        synchronized (mListenerRefs) {
            mListenerRefs.add(new WeakReference<>(listener));
        }
    }

    public void adjustRamUsageFromBoostPlus(long cleanedSize) {
        postBoostCleanedSize(cleanedSize);
        mLastBoostTime = SystemClock.uptimeMillis();
        setFakeRamUsage();
    }

    private void setFakeRamUsage() {
        if (mLastBoostRamUsage > 0
                && mDisplayedRamUsage > mLastBoostRamUsage
                && mLastBoostRamUsage < RAM_LAST_MAX_VALUE) {
            mDisplayedRamUsage = mFakeRamUsage = mLastBoostRamUsage;
        } else {
            Random random = new Random();
            mDisplayedRamUsage = mFakeRamUsage = random.nextInt(7) + 29;
        }
    }

    @SuppressWarnings("WeakerAccess")
    void postBoostCleanedSize(long cleanedSize) {
        int cleanedPercentage = Math.round(100f * cleanedSize / DeviceManager.getInstance().getTotalRam());
        mLastBoostRamUsage = Math.max(RAM_MINIMUM_VALUE, mRealRamUsage - cleanedPercentage);
        mBoosting = false;
    }
}
