package com.acb.libwallpaper.live.debug;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import com.acb.libwallpaper.live.util.CommonUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Profile utility. For API 17 or above.
 * <p>
 * Note that for convenience of test this tool does NOT shortcut its operation on release builds,
 * and uses {@link Log} instead of {@link com.ihs.commons.utils.HSLog} as logging facility.
 * Be sure to shortcut, if not remove, any usage of this class upon shipping the application.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class Profiler {

    private static final String TAG = "Launcher.Profiler";

    private static final String DEFAULT_PROFILE_NAME = "default_profile";

    private static Map<String, ProfileInfo> sProfileMap = new HashMap<>(1);

    public static void start() {
        start(DEFAULT_PROFILE_NAME);
    }

    public static void start(String profileName) {
        if (CommonUtils.ATLEAST_JB_MR1) {
            String threadName = Thread.currentThread().getName();
            String combinedName = threadName + ": " + profileName;
            sProfileMap.put(combinedName, new ProfileInfo(combinedName));
        }
    }

    public static void end() {
        end(DEFAULT_PROFILE_NAME);
    }

    public static void end(String profileName) {
        if (CommonUtils.ATLEAST_JB_MR1) {
            String threadName = Thread.currentThread().getName();
            String combinedName = threadName + ": " + profileName;
            ProfileInfo profile = sProfileMap.get(combinedName);
            if (profile != null) {
                profile.endProfile();
                profile.dump();
                sProfileMap.remove(combinedName);
            } else {
                Log.w(TAG, "Profile " + combinedName + " is not started");
            }
        }
    }

    private static class ProfileInfo {
        String name;

        long startTimeNanos;
        long endTimeNanos;
        long startThreadTimeMillis;
        long endThreadTimeMillis;

        private static Map<String, Long> mTotalTime = new HashMap<>();

        ProfileInfo(String profileName) {
            name = profileName;
            startTimeNanos = SystemClock.elapsedRealtimeNanos();
            startThreadTimeMillis = SystemClock.currentThreadTimeMillis();
        }

        void endProfile() {
            endTimeNanos = SystemClock.elapsedRealtimeNanos();
            endThreadTimeMillis = SystemClock.currentThreadTimeMillis();
        }

        void dump() {
            Long totalTime = mTotalTime.get(name);
            if (totalTime == null) {
                totalTime = endTimeNanos - startTimeNanos;
            } else {
                totalTime += (endTimeNanos - startTimeNanos);
            }
            mTotalTime.put(name, totalTime);
            Log.i(TAG, name + " costs " + (endTimeNanos - startTimeNanos) / 1000 + " us in real time, " +
                    (endThreadTimeMillis - startThreadTimeMillis) * 1000 + " us in current thread" +
                    " total: " + totalTime / 1000);
        }
    }
}
