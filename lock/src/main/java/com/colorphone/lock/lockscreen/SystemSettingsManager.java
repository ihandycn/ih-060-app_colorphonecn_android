package com.colorphone.lock.lockscreen;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.WindowManager;

import com.colorphone.lock.ReflectionHelper;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Compats;
import com.superapps.util.Navigations;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SystemSettingsManager {

    private static final String TAG = SystemSettingsManager.class.getName();

    private static final String ACTION_MOBILE_DATA_STATUS_CHANGED = "android.intent.action.ANY_DATA_STATE";
    private static final String BLUETOOTH_ACTION = "android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED";

    /**
     * Whether the phone vibrates when it is ringing due to an incoming call. This will
     * be used by Phone and Setting apps; it shouldn't affect other apps.
     * The value is boolean (1 or 0).
     *
     * Note: this is not same as "vibrate on ring", which had been available until ICS.
     * It was about AudioManager's setting and thus affected all the applications which
     * relied on the setting, while this is purely about the vibration setting for incoming
     * calls.
     *
     * See android.provider.Settings.System.VIBRATE_WHEN_RINGING.
     */
    private static final String VIBRATE_WHEN_RINGING = "vibrate_when_ringing";

    /**
     * Whether Airplane Mode is on.
     *
     * See android.provider.Settings.Global.AIRPLANE_MODE_ON.
     */
    private static final String AIRPLANE_MODE_ON = "airplane_mode_on";

    public enum SettingsItem {
        AIRPLANE_MODE,
        BLUETOOTH,
        BRIGHTNESS,
        MOBILE_DATA,
        AUTO_ROTATE,
        SOUND,
        VIBRATE,
        AUTO_SYNC,
        WIFI,
        SCREEN_TIMEOUT,
        HAPTIC_FEEDBACK,
        NETWORK_TYPE,
        BATTERY,
        RINGMODE
    }

    public interface ISystemSettingsListener {
        void onSystemSettingsStateChanged(SettingsItem item, int state);
    }

    private Context mContext;

    private ISystemSettingsListener listener;

    private ContentObserver mSettingsObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            // Brightness
            if (uri != null
                    && (uri.equals(Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS))
                    || uri.equals(Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE)))) {
                notifyListener(SettingsItem.BRIGHTNESS);
            }

            // Rotation
            if (uri != null && uri.equals(Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION))) {
                notifyListener(SettingsItem.AUTO_ROTATE);
            }

            // Screen timeout
            if (uri != null && uri.equals(Settings.System.getUriFor(Settings.System.SCREEN_OFF_TIMEOUT))) {
                notifyListener(SettingsItem.SCREEN_TIMEOUT);
            }
        }
    };

    private class StateChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                notifyListener(SettingsItem.WIFI, transferWifiState(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1)));
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action) || BLUETOOTH_ACTION.equals(action)) {
                notifyListener(SettingsItem.BLUETOOTH, transferBluetoothState(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)));
            } else if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {
                notifyListener(SettingsItem.AIRPLANE_MODE, intent.getBooleanExtra("state", false) ? 1 : 0);
            } else if (AudioManager.RINGER_MODE_CHANGED_ACTION.equals(action)) {
                notifyListener(SettingsItem.SOUND);
                notifyListener(SettingsItem.RINGMODE);
            } else if (ACTION_MOBILE_DATA_STATUS_CHANGED.equals(action)) {
                notifyListener(SettingsItem.MOBILE_DATA);
            }
        }
    }

    private StateChangeReceiver mStateChangeReceiver;

    public SystemSettingsManager(Context context) {
        mContext = context;
    }

    public void register(ISystemSettingsListener listener) {
        this.listener = listener;

        mContext.getContentResolver().registerContentObserver(Settings.System.CONTENT_URI, true, mSettingsObserver);

        mStateChangeReceiver = new StateChangeReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(ACTION_MOBILE_DATA_STATUS_CHANGED);
        intentFilter.addAction(BLUETOOTH_ACTION);
        intentFilter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
        mContext.registerReceiver(mStateChangeReceiver, intentFilter);
    }

    public void unRegister() {
        mContext.getContentResolver().unregisterContentObserver(mSettingsObserver);
        this.listener = null;

        try {
            mContext.unregisterReceiver(mStateChangeReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void notifyListener(SettingsItem item) {
        notifyListener(item, getSystemSettingsItemState(item));
    }

    private void notifyListener(SettingsItem item, int state) {
        if (null != listener) {
            HSLog.i(TAG, "getSysState " + item + " == " + state);
            listener.onSystemSettingsStateChanged(item, state);
        }
    }

    //region Getter
    public int getSystemSettingsItemState(SettingsItem item) {
        int state = 0;

        switch (item) {
            case AIRPLANE_MODE:
                state = isAirplaneModeOn() ? 1 : 0;
                break;
            case BLUETOOTH:
//                state = getBluetoothState();
                break;
            case BRIGHTNESS:
                state = getBrightness();
                break;
            case MOBILE_DATA:
                state = getMobileDataStatus(mContext) ? 1 : 0;
                break;
            case AUTO_ROTATE:
                state = isAutoRotateOn() ? 1 : 0;
                break;
            case SOUND:
                state = isSoundOn() ? 1 : 0;
                break;
            case VIBRATE:
                state = isVibrateOn() ? 1 : 0;
                break;
            case AUTO_SYNC:
//                state = isAutoSyncOn() ? 1 : 0;
                break;
            case WIFI:
                state = getWifiState();
                break;
            case SCREEN_TIMEOUT:
                state = getScreenTimeOut();
                break;
            case HAPTIC_FEEDBACK:
                break;
            case NETWORK_TYPE:
                state = getHapticFeedback();
                break;
            case BATTERY:
                break;
            case RINGMODE:
                state = getRingMode();
        }
        return state;
    }

    private boolean isAirplaneModeOn() {
        return 1 == Settings.System.getInt(mContext.getContentResolver(), AIRPLANE_MODE_ON, 0);
    }

//    private boolean isAutoSyncOn() {
//        return ContentResolver.getMasterSyncAutomatically();
//    }

    private boolean isAutoRotateOn() {
        return 1 == Settings.System.getInt(mContext.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
    }

    private int getScreenTimeOut() {
        return Settings.System.getInt(mContext.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 0) / 1000;
    }

    private int getHapticFeedback() {
        return Settings.System.getInt(mContext.getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED, 0);
    }

    private int getBrightness() {
        boolean auto = false;
        try {
            auto = Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC ==
                    Settings.System.getInt(mContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        int light;
        if (auto) {
            light = -1;
        } else {
            light = android.provider.Settings.System.getInt(mContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);
        }
        return light;
    }

    public static boolean getMobileDataStatus(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        String methodName = "getMobileDataEnabled";
        Class cmClass = connectivityManager.getClass();
        boolean isOpen;

        try {
            @SuppressWarnings("unchecked")
            Method method = ReflectionHelper.getMethod(cmClass, methodName);
            isOpen = (Boolean) method.invoke(connectivityManager);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }
        return isOpen;
    }

//
//    private int getBluetoothState() {
//        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        if (null == bluetoothAdapter) {
//            return 3;
//        }
//        return transferBluetoothState(bluetoothAdapter.getState());
//    }

    private int getRingMode() {
        AudioManager audioMgr = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        switch (audioMgr.getRingerMode()) {
            case AudioManager.RINGER_MODE_SILENT:
                return 0;
            case AudioManager.RINGER_MODE_VIBRATE:
                return 1;
            case AudioManager.RINGER_MODE_NORMAL:
                return 2;
            default:
                return 0;
        }
    }

    public void toggleRingMode() {
        AudioManager audioMgr = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        switch (audioMgr.getRingerMode()) {
            case AudioManager.RINGER_MODE_SILENT:
                audioMgr.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                audioMgr.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                break;
            case AudioManager.RINGER_MODE_NORMAL:
                audioMgr.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                break;
            default:
                break;
        }
    }

    private int getWifiState() {
        WifiManager wifiMgr = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return transferWifiState(wifiMgr.getWifiState());
    }

    private boolean isSoundOn() {
        AudioManager audioMgr = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        int ringMode = audioMgr != null ? audioMgr.getRingerMode() : -1;
        switch (ringMode) {
            case AudioManager.RINGER_MODE_SILENT:
            case AudioManager.RINGER_MODE_VIBRATE:
                return false;
            case AudioManager.RINGER_MODE_NORMAL:
                return true;
            default:
                return false;
        }
    }

    @SuppressWarnings("SimplifiableIfStatement")
    private boolean isVibrateOn() {
        AudioManager audioMgr = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        int ringerStatus = audioMgr.getRingerMode();
        if (ringerStatus == AudioManager.RINGER_MODE_VIBRATE) {
            return true;
        } else if (ringerStatus == AudioManager.RINGER_MODE_SILENT) {
            return false;
        } else {
            // RINGER_MODE_NORMAL
            return Settings.System.getInt(HSApplication.getContext().getContentResolver(),
                    VIBRATE_WHEN_RINGING, 0) == 1;
        }
    }

    private int transferWifiState(int systemState) {
        int state;
        switch (systemState) {
            case WifiManager.WIFI_STATE_ENABLED:
                state = 1;
                break;
            case WifiManager.WIFI_STATE_ENABLING:
                state = 2;
                break;
            case WifiManager.WIFI_STATE_DISABLING:
                state = 3;
                break;
            case WifiManager.WIFI_STATE_DISABLED:
            case WifiManager.WIFI_STATE_UNKNOWN:
            default:
                state = 0;
                break;
        }
        return state;
    }

    private int transferBluetoothState(int systemState) {
        int state;
        switch (systemState) {
            case BluetoothAdapter.STATE_ON:
                state = 1;
                break;
            case BluetoothAdapter.STATE_TURNING_ON:
                state = 2;
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                state = 3;
                break;
            case BluetoothAdapter.STATE_OFF:
            default:
                state = 0;
                break;
        }
        return state;
    }
    //endregion

    //region Setter
    public void setSystemSettingsItemState(SettingsItem item, int state) {
        switch (item) {
            case AIRPLANE_MODE:
                break;
            case BLUETOOTH:
                if (getSystemSettingsItemState(item) != state) {
                    toggleBluetooth();
                }
                break;
            case BRIGHTNESS:
                setBrightness(state);
                break;
            case MOBILE_DATA:
                if (getSystemSettingsItemState(item) != state) {
                    setMobileDataStatus(mContext, state == 1);
                }
                break;
            case AUTO_ROTATE:
                break;
            case SOUND:
                if (getSystemSettingsItemState(item) != state) {
                    toggleSound();
                }
                break;
            case VIBRATE:
                if (getSystemSettingsItemState(item) != state) {
                    toggleVibrate();
                }
                break;
            case AUTO_SYNC:
                if (getSystemSettingsItemState(item) != state) {
                    toggleAutoSync();
                }
                break;
            case WIFI:
                if (getSystemSettingsItemState(item) != state) {
                    toggleWifi();
                }
                break;
            case SCREEN_TIMEOUT:
                setScreenTimeout(state);
                break;
            case HAPTIC_FEEDBACK:
                setHapticFeedback(state);
                break;
            case NETWORK_TYPE:
                break;
            case BATTERY:
                break;
        }
    }

    private void setScreenTimeout(int timeout) {
        putIntSafely(Settings.System.SCREEN_OFF_TIMEOUT, timeout * 1000);
    }

    private void setHapticFeedback(int on) {
        putIntSafely(Settings.System.HAPTIC_FEEDBACK_ENABLED, on);
    }

    private void setBrightness(int brightness) {
        if (brightness == -1) {
            putIntSafely(Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
        } else {
            putIntSafely(Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            putIntSafely(Settings.System.SCREEN_BRIGHTNESS, brightness);
        }

        // Enforce a refresh for current window
        if (mContext instanceof Activity) {
            WindowManager.LayoutParams lp = ((Activity) mContext).getWindow().getAttributes();
            if (brightness == -1) {
                lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
            } else {
                lp.screenBrightness = getBrightness() / 100f;
            }
            ((Activity) mContext).getWindow().setAttributes(lp);
        }
    }

    public static boolean isWifiEnabled() {
        WifiManager wifiManager = (WifiManager) HSApplication.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }

    public static boolean setMobileDataStatus(Context context, boolean enabled) {
        if (Compats.IS_HUAWEI_DEVICE && isWifiEnabled()) {
            return false;
        }
        ConnectivityManager connectivityManager;
        Class connectivityManagerClz;
        try {
            connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            connectivityManagerClz = connectivityManager.getClass();
            Method method = ReflectionHelper.getMethod(connectivityManagerClz, "setMobileDataEnabled", boolean.class);
            // Asynchronous invocation
            method.invoke(connectivityManager, enabled);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public static void startSystemDataUsageSetting(Context context) {
        startSystemDataUsageSetting(context, false);
    }

    public static void startSystemDataUsageSetting(Context context, boolean attachNewTaskFlag) {
        Intent dataUsageIntent = new Intent();
        dataUsageIntent.setComponent(new ComponentName(
                "com.android.settings",
                "com.android.settings.Settings$DataUsageSummaryActivity"));
        Intent intent;
        if (dataUsageIntent.resolveActivity(context.getPackageManager()) != null) {
            intent = dataUsageIntent;
        } else {
            intent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
        }
        if (attachNewTaskFlag) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        Navigations.startActivitySafely(context, intent);
    }

    public void toggleSound() {
        AudioManager audioMgr = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        if (!isSoundOn()) {
            boolean isVibrate = isVibrateOn();
            audioMgr.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            if (isVibrate) {
                if (hasWriteSettingsPermission()) {
                    Settings.System.putInt(HSApplication.getContext().getContentResolver(),
                            VIBRATE_WHEN_RINGING, 1);
                }
                audioMgr.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,
                        AudioManager.VIBRATE_SETTING_ON);
            } else {
                if (hasWriteSettingsPermission()) {
                    try {
                        Settings.System.putInt(HSApplication.getContext().getContentResolver(),
                                "vibrate_when_ringing", 0);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }
                audioMgr.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,
                        AudioManager.VIBRATE_SETTING_OFF);
            }
        } else {
            if (isVibrateOn()) {
                audioMgr.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            } else {
                audioMgr.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            }
        }
    }

    public void toggleVibrate() {
        AudioManager audioMgr = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        boolean vibrate = isVibrateOn();
        if (vibrate) {
            try {
                if (isSoundOn()) {
                    if (hasWriteSettingsPermission()) {
                        try {
                            Settings.System.putInt(HSApplication.getContext().getContentResolver(),
                                    "vibrate_when_ringing", 0);
                        } catch (SecurityException e) {
                            e.printStackTrace();
                        }
                    }
                    audioMgr.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,
                            AudioManager.VIBRATE_SETTING_OFF);
                } else {
                    audioMgr.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        } else {
            Vibrator v = (Vibrator) HSApplication.getContext().getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null) {
                v.vibrate(60);
            }
            try {
                if (isSoundOn()) {
                    if (hasWriteSettingsPermission()) {
                        Settings.System.putInt(HSApplication.getContext().getContentResolver(),
                                VIBRATE_WHEN_RINGING, 1);
                    }
                    audioMgr.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,
                            AudioManager.VIBRATE_SETTING_ON);
                } else {
                    audioMgr.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    private void toggleAutoSync() {
//        if (isAutoSyncOn()) {
//            ContentResolver.setMasterSyncAutomatically(false);
//        } else {
//            ContentResolver.setMasterSyncAutomatically(true);
//        }
    }

    public void toggleWifi() {
        WifiManager wifiMgr = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (null != wifiMgr) {
            wifiMgr.setWifiEnabled(!wifiMgr.isWifiEnabled());
        }
    }

    public void toggleBluetooth() {
//        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        if (null == bluetoothAdapter) {
//            return;
//        }
//        switch (bluetoothAdapter.getState()) {
//            case BluetoothAdapter.STATE_ON:
//                try {
//                    bluetoothAdapter.disable();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
//            case BluetoothAdapter.STATE_TURNING_ON:
//                try {
//                    bluetoothAdapter.disable();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
//            case BluetoothAdapter.STATE_OFF:
//                bluetoothAdapter.enable();
//                break;
//            case BluetoothAdapter.STATE_TURNING_OFF:
//                bluetoothAdapter.enable();
//                break;
//            default:
//                break;
//        }
    }
    //endregion

    //region Util

    /**
     * Check if android.permission.WRITE_SETTINGS is granted, pop a system window if not.
     *
     * @return True when permission is already granted.
     */
    @TargetApi(Build.VERSION_CODES.M)
    private boolean hasWriteSettingsPermission() {
        //noinspection SimplifiableIfStatement
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        return Settings.System.canWrite(HSApplication.getContext());
    }

    private void putIntSafely(String name, int value) {
        ContentResolver cr = mContext.getContentResolver();
        try {
            Settings.System.putInt(cr, name, value);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
    //endregion
}
