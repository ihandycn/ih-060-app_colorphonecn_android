package com.honeycomb.colorphone.wallpaper.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.animation.DecelerateInterpolator;

import com.honeycomb.colorphone.wallpaper.LauncherAnalytics;
 import com.honeycomb.colorphone.R;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;

/**
 * Defaults to the following status when IPC to WifiManager failed for any reason:
 * <p>
 * - Wi-Fi is not enabled and (certainly) not connected.
 * - {@link #getWifiState()} returns {@link WifiManager#WIFI_STATE_UNKNOWN}.
 */
public class WifiHelper implements INotificationObserver {

    public static final String NOTIFICATION_NETWORK_CHANGED = "wifi.helper.notification.network.changed";

    private static final int MAX_WIFI_ANIM_REPEAT_COUNT = 15;

    private static final String SSID_UNKNOWN = "<unknown ssid>";

    private WifiManager mWifiManager;
    private ConnectivityManager mConnectivityManager;

    private ValueAnimator mWifiAnimator;
    private int mWifiAnimationRepeatCount;
    private AnimatedWifiTarget mAnimatedTarget;

    private Handler mHandler;

    @SuppressWarnings("WeakerAccess")
    public enum WifiState {
        CUSTOM_WIFI_ENABLED, // Wifi enabled and connected
        CUSTOM_WIFI_DISABLED, // Wifi disabled
        CUSTOM_WIFI_AVAILABLE, // Wifi enabled and disconnected
    }

    public WifiHelper(Context context) {
        HSGlobalNotificationCenter.addObserver(NOTIFICATION_NETWORK_CHANGED, this);
        mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        initWifiAnimator();
        mHandler = new Handler(Looper.getMainLooper());
    }

    public int getWifiState() {
        try {
            return mWifiManager.getWifiState();
        } catch (Exception e) {
            return WifiManager.WIFI_STATE_UNKNOWN;
        }
    }

    public boolean isWifiEnabled() {
        try {
            return mWifiManager.isWifiEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public @Nullable WifiInfo getConnectionInfo() {
        try {
            return mWifiManager.getConnectionInfo();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @return SSID of currently connected Wi-Fi, or empty string or "unknown ssid" if no such network exists.
     */
    public String getSSID() {
        WifiInfo connectionInfo = null;
        try {
            connectionInfo = mWifiManager.getConnectionInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String ssid = "";
        if (connectionInfo != null) {
            ssid = connectionInfo.getSSID();
            if (!TextUtils.isEmpty(ssid) && ssid.startsWith("\"") && ssid.endsWith("\"")) {
                ssid = ssid.substring(1, ssid.lastIndexOf("\""));
            }
        }
        return ssid;
    }

    public void setWifiEnabled(boolean enabled) {
        try {
            mWifiManager.setWifiEnabled(enabled);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initWifiAnimator() {
        mWifiAnimator = ValueAnimator.ofFloat(0, 1);
        mWifiAnimator.setDuration(800);
        mWifiAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mWifiAnimator.setRepeatMode(ValueAnimator.RESTART);
        mWifiAnimator.setInterpolator(new DecelerateInterpolator());
        mWifiAnimator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationRepeat(Animator animation) {
                HSLog.d("WifiHelper.test", "onAnimationRepeat Repeat count: " + mWifiAnimationRepeatCount);
                if (mWifiAnimationRepeatCount >= MAX_WIFI_ANIM_REPEAT_COUNT - 1) {
                    mWifiAnimator.cancel();
                    HSLog.d("WifiHelper.test", "Max count");
                    if (mAnimatedTarget != null) {
                        mAnimatedTarget.onWifiNoAvailable();
                    }
                    return;
                }
                mWifiAnimationRepeatCount++;
            }

            @Override
            public void onAnimationStart(Animator animation) {
                mWifiAnimationRepeatCount = 0;
                HSLog.d("WifiHelper.test", "onAnimationStart");
            }
        });
        mWifiAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mHandler.removeCallbacksAndMessages(null);
                if (mWifiAnimationRepeatCount >= MAX_WIFI_ANIM_REPEAT_COUNT - 1) {
                    HSLog.d("WifiHelper.test", "onAnimationUpdate Max count");
                    return;
                } else if (mWifiAnimationRepeatCount == 0 && isWifiConnected()) {
                    if (mAnimatedTarget != null) {
                        mAnimatedTarget.onWifiConnected(getSSID());
                    }
                    mWifiAnimator.cancel();
                    HSLog.d("WifiHelper.test", "onAnimationUpdate filter this update");
                    return;
                }

                float animatedFraction = animation.getAnimatedFraction();
                int level = (int) (animatedFraction / 0.25);
                HSLog.d("WifiHelper.test", "Update level " + level);

                if (!isWifiEnabled()) {
                    HSLog.d("WifiHelper.test", "Update wifi disable");
                    if (mAnimatedTarget != null) {
                        mAnimatedTarget.onWifiDisconnected();
                    }
                    mWifiAnimator.cancel();
                } else if (isWifiConnected() && level == 3) {
                    HSLog.d("WifiHelper.test", "Update connect & level = 3");
                    if (mAnimatedTarget != null) {
                        mAnimatedTarget.onWifiConnected(getSSID());
                    }
                    mWifiAnimator.cancel();
                } else {
                    if (mAnimatedTarget != null) {
                        mAnimatedTarget.onWifiConnecting(level);
                    }
                }
            }
        });
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        if (NOTIFICATION_NETWORK_CHANGED.equals(s)) {
            if (mAnimatedTarget == null) {
                // No need to start the animation if there's nothing to animate
                return;
            }
            if (isWifiEnabled()) {
                HSLog.d("WifiHelper.test", "onReceive wifi enable");
                if (!mWifiAnimator.isRunning()) {
                    HSLog.d("WifiHelper.test", "onReceive start anim");
                    mWifiAnimator.start();
                }
                // For Nexus 5, Animator#isRunning() return true but not running.
                // Don't know why
                mHandler.postDelayed(() -> {
                    if (isWifiEnabled()) {
                        mWifiAnimator.cancel();
                        mWifiAnimator.start();
                    }
                }, 600);
                LauncherAnalytics.logEvent("Launcher_Action_WiFi", "On");
            } else {
                HSLog.d("WifiHelper.test", "onReceive wifi disable");
                if (mWifiAnimator.isRunning()) {
                    HSLog.d("WifiHelper.test", "onReceive cancel anim");
                    mWifiAnimator.cancel();
                }
                if (mAnimatedTarget != null) {
                    mAnimatedTarget.onWifiDisconnected();
                }

                LauncherAnalytics.logEvent("Launcher_Action_WiFi", "Off");
            }
        }
    }

    public void setAnimatedWifiTarget(AnimatedWifiTarget target) {
        this.mAnimatedTarget = target;
        if (isWifiConnected()) {
            HSLog.d("WifiHelper.test", "getView wifi connected");
            target.onWifiConnected(getSSID());
        } else if (!isWifiEnabled()) {
            HSLog.d("WifiHelper.test", "getView wifi disconnected");
            target.onWifiDisconnected();
        } else {
            if (!mWifiAnimator.isRunning()) {
                mWifiAnimator.start();
            }
        }
    }

    public boolean isWifiConnected() {
        String ssid = getSSID();
        NetworkInfo activeNetworkInfo = null;
        try {
            activeNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return isWifiEnabled()
                    && activeNetworkInfo != null
                    && activeNetworkInfo.getState() == NetworkInfo.State.CONNECTED
                    && activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI;
        }
        return isWifiEnabled()
                && !(TextUtils.equals(ssid, SSID_UNKNOWN) || TextUtils.isEmpty(ssid))
                && activeNetworkInfo != null
                && activeNetworkInfo.getState() == NetworkInfo.State.CONNECTED
                && activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }

    public WifiState getCustomWifiState() {
        HSLog.d("Wifi.Alert", "getWifiState " + getWifiState());
        boolean isWifiEnabled = isWifiEnabled();
        boolean isWifiConnected = isWifiConnected();
        HSLog.d("Wifi.Alert", "isWifiEnabled " + isWifiEnabled);
        HSLog.d("Wifi.Alert", "isWifiConnected " + isWifiConnected);
        if (isWifiEnabled && isWifiConnected) {
            return WifiState.CUSTOM_WIFI_ENABLED;
        } else if (isWifiEnabled /* && !isWifiConnected */) {
            return WifiState.CUSTOM_WIFI_AVAILABLE;
        } else {
            return WifiState.CUSTOM_WIFI_DISABLED;
        }
    }

    public void release() {
        HSGlobalNotificationCenter.removeObserver(this);
        mAnimatedTarget = null;
    }

    public interface AnimatedWifiTarget {

        void onWifiNoAvailable();

        void onWifiConnected(String ssid);

        void onWifiDisconnected();

        void onWifiConnecting(int stage);
    }

    public String getConnectingText() {
        return HSApplication.getContext().getResources().getString(R.string.notification_toolbar_wifi_connecting);
    }
}
