package com.colorphone.lock.lockscreen.locker.statusbar;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Looper;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.colorphone.lock.R;
import com.colorphone.lock.ScreenStatusReceiver;
import com.colorphone.lock.lockscreen.SystemSettingsManager;
import com.colorphone.lock.lockscreen.locker.Locker;
import com.colorphone.lock.lockscreen.locker.LockerUtils;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.libcharging.HSChargingManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class StatusBar extends RelativeLayout implements SystemSettingsManager.ISystemSettingsListener
        , INotificationObserver {

    boolean shouldShowWifi = false;
    private boolean isListeningPhoneState = false;

    private TextView tvTime;
    private TextView tvBattery;
    private ImageView ivWifi;
    private TextView tvMobileData;
    private ImageView ivMobileStrength;
    private ImageView ivAirPlane;
    private ImageView ivSound;
    private ImageView ivBatteryCharging;
    private StatusBarBatteryIndicator batteryIndicator;
    private int[] resWifiImages = new int[]{
            R.drawable.status_wifi_strength_1,
            R.drawable.status_wifi_strength_2,
            R.drawable.status_wifi_strength_3
    };
    private int[] resMobileStrengthImages = new int[]{
            R.drawable.status_signal_disable,
            R.drawable.status_signal_strength_1,
            R.drawable.status_signal_strength_2,
            R.drawable.status_signal_strength_3,
            R.drawable.status_signal_strength_4
    };

    private SystemSettingsManager mSystemSettingsManager;

    private HSChargingManager.IChargingListener chargingListener = new HSChargingManager.IChargingListener() {

        @Override public void onBatteryLevelChanged(int i, int i1) {
            updateBattery();
        }

        @Override
        public void onChargingStateChanged(HSChargingManager.HSChargingState preChargingState, HSChargingManager.HSChargingState curChargingState) {
            updateBattery();
        }

        @Override public void onChargingRemainingTimeChanged(int i) {

        }

        @Override public void onBatteryTemperatureChanged(float v, float v1) {

        }
    };

    protected Handler handler = new Handler(Looper.getMainLooper());
    private boolean isReceiverRegistered = false;

    private BroadcastReceiver datetimeUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateTime();
        }
    };

    private ContentObserver dateFormatObserver = new ContentObserver(handler) {
        @Override
        public void onChange(boolean selfChange) {
            updateTime();
        }
    };

    private BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            if (!LockerUtils.hasPermission(Manifest.permission.ACCESS_WIFI_STATE)
                    || !LockerUtils.hasPermission(Manifest.permission.CHANGE_WIFI_STATE)) {
                return;
            }
            final WifiManager wManager = (WifiManager) c.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            List<ScanResult> wifiList = wManager.getScanResults();
            if (wifiList == null) {
                return;
            }
            boolean hasConnected = false;
            for (int i = 0; i < wifiList.size(); i++) {
                ScanResult wifi = wifiList.get(i);
                if (wManager.getConnectionInfo() != null && wifi != null && wifi.BSSID != null) {
                    if (wifi.BSSID.equals(wManager.getConnectionInfo().getBSSID())) {
                        hasConnected = true;
                        int signalLevel = WifiManager.calculateSignalLevel(wifi.level, 3);
                        if (signalLevel < 0) {
                            signalLevel = 0;
                        } else if (signalLevel > 2) {
                            signalLevel = 2;
                        }
                        if (shouldShowWifi) {
                            ivWifi.setVisibility(View.VISIBLE);
                            ivWifi.setImageResource(resWifiImages[signalLevel]);
                        }
                    }
                }
            }
            if (!hasConnected) {
                ivWifi.setVisibility(View.GONE);
            }
            if (ScreenStatusReceiver.isScreenOn()) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        wManager.startScan();
                    }
                }, 5 * 1000);
            }
        }
    };

    private class StatusBarPhoneStateListener extends PhoneStateListener {

        StatusBarPhoneStateListener(StatusBar statusBar) {
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            if (state == TelephonyManager.CALL_STATE_RINGING) {
                HSBundle bundle = new HSBundle();
                bundle.putBoolean(Locker.EXTRA_SHOULD_DISMISS_KEYGUARD, false);
                bundle.putString(Locker.EXTRA_DISMISS_REASON, "Ringing");
                HSGlobalNotificationCenter.sendNotification(Locker.EVENT_FINISH_SELF, bundle);
            }
        }

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            StatusBar statusBar = StatusBar.this;
            if (statusBar.isValid()) {
                ImageView mobileStrengthView = statusBar.ivMobileStrength;
                int asu = signalStrength.getGsmSignalStrength();
                if (asu <= 2 || asu == 99)
                    mobileStrengthView.setImageResource(statusBar.resMobileStrengthImages[0]);
                else if (asu >= 12) mobileStrengthView.setImageResource(statusBar.resMobileStrengthImages[4]);
                else if (asu >= 8) mobileStrengthView.setImageResource(statusBar.resMobileStrengthImages[3]);
                else if (asu >= 5) mobileStrengthView.setImageResource(statusBar.resMobileStrengthImages[2]);
                else mobileStrengthView.setImageResource(statusBar.resMobileStrengthImages[1]);
            }
        }

        @Override
        public void onDataConnectionStateChanged(int state, int networkType) {
            StatusBar statusBar = StatusBar.this;;
            if (statusBar.isValid()) {
                TextView mobileDataView = statusBar.tvMobileData;
                switch (networkType) {
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        mobileDataView.setText(R.string.locker_2g);
                        break;
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                        mobileDataView.setText(R.string.locker_3g);
                        break;
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        mobileDataView.setText(R.string.locker_4g);
                        break;
                    default:
                        mobileDataView.setText(R.string.locker_unkown);
                        break;
                }
            }
        }
    }

    private boolean isValid() {
        return false;
    }


    private StatusBarPhoneStateListener phoneStateListener = new StatusBarPhoneStateListener(this);

    public StatusBar(Context context, AttributeSet set) {
        super(context, set);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        tvTime = (TextView) findViewById(R.id.tv_time);
        tvBattery = (TextView) findViewById(R.id.tv_battery);
        ivWifi = (ImageView) findViewById(R.id.iv_wifi);
        tvMobileData = (TextView) findViewById(R.id.tv_mobile_data);
        ivMobileStrength = (ImageView) findViewById(R.id.iv_mobile_strength);
        ivAirPlane = (ImageView) findViewById(R.id.iv_airplane);
        ivSound = (ImageView) findViewById(R.id.iv_sound);
        ivBatteryCharging = (ImageView) findViewById(R.id.iv_battery_charging);
        batteryIndicator = (StatusBarBatteryIndicator) findViewById(R.id.battery_indicator);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        startListenPhoneState();
        HSGlobalNotificationCenter.addObserver(ScreenStatusReceiver.NOTIFICATION_SCREEN_ON, this);
        HSGlobalNotificationCenter.addObserver(ScreenStatusReceiver.NOTIFICATION_SCREEN_OFF, this);

        mSystemSettingsManager = new SystemSettingsManager(getContext());
        mSystemSettingsManager.register(this);

        updateTime();
        updatePlaneOn();
        updateSound();
        updateWifi();
        updateBattery();
        HSChargingManager.getInstance().addChargingListener(chargingListener);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        stopListenPhoneState();
        HSGlobalNotificationCenter.removeObserver(this);
        if (mSystemSettingsManager != null) {
            mSystemSettingsManager.unRegister();
            mSystemSettingsManager = null;
        }
        HSChargingManager.getInstance().removeChargingListener(chargingListener);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    private void updateTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        if (DateFormat.is24HourFormat(getContext())) {
            dateFormat.applyPattern("HH:mm");
        } else {
            dateFormat.applyPattern("hh:mm");
        }
        tvTime.setText(dateFormat.format(calendar.getTime()));
    }

    private void updatePlaneOn() {
        if (mSystemSettingsManager.getSystemSettingsItemState(SystemSettingsManager.SettingsItem.AIRPLANE_MODE) == 1) {
            ivAirPlane.setVisibility(View.VISIBLE);
            ivMobileStrength.setVisibility(View.GONE);
            tvMobileData.setVisibility(View.GONE);
            updateWifi();
        } else {
            ivAirPlane.setVisibility(View.GONE);
            ivMobileStrength.setVisibility(View.VISIBLE);
            updateWifi();
        }
    }

    private void updateSound() {
        if (mSystemSettingsManager.getSystemSettingsItemState(SystemSettingsManager.SettingsItem.RINGMODE) == 0) {
            ivSound.setVisibility(View.VISIBLE);
            ivSound.setImageResource(R.drawable.status_silence);
        } else if (mSystemSettingsManager.getSystemSettingsItemState(SystemSettingsManager.SettingsItem.RINGMODE) == 1) {
            ivSound.setVisibility(View.VISIBLE);
            ivSound.setImageResource(R.drawable.status_vibrate);
        } else {
            ivSound.setVisibility(View.GONE);
        }
    }

    private void updateWifi() {
        if (mSystemSettingsManager.getSystemSettingsItemState(SystemSettingsManager.SettingsItem.AIRPLANE_MODE) == 1) {
            shouldShowWifi = false;
            ivWifi.setVisibility(View.GONE);
        } else {
            int state = mSystemSettingsManager.getSystemSettingsItemState(SystemSettingsManager.SettingsItem.WIFI);
            if (state > 0) {
                shouldShowWifi = true;
                ivWifi.setVisibility(View.VISIBLE);
            } else {
                shouldShowWifi = false;
                ivWifi.setVisibility(View.GONE);
            }
        }
        updateMobileData();
    }

    private void updateBattery() {
        int batteryPercentage = getBatteryPercentage();
        batteryIndicator.setPercentage(batteryPercentage);
        String batteryText;
        if (batteryPercentage == 100) {
            batteryText = "100";
        } else if (batteryPercentage < 10) {
            batteryText = "  " + batteryPercentage + "% ";
        } else {
            batteryText = batteryPercentage + "% ";
        }
        tvBattery.setText(batteryText);
        if (HSChargingManager.getInstance().isCharging()) {
            ivBatteryCharging.setVisibility(View.VISIBLE);
        } else {
            ivBatteryCharging.setVisibility(View.GONE);
        }
    }

    private int getBatteryPercentage() {
        int batteryPercentage = HSChargingManager.getInstance().getBatteryRemainingPercent();
        if (batteryPercentage <= 0) {
            Intent intent = getContext().registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
            if (intent != null) {
                int currentBatteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                int batteryScale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                batteryPercentage = currentBatteryLevel * 100 / batteryScale;
            }
        }
        return batteryPercentage;
    }

    private void updateMobileData() {
        if (mSystemSettingsManager.getSystemSettingsItemState(SystemSettingsManager.SettingsItem.AIRPLANE_MODE) == 0
                && mSystemSettingsManager.getSystemSettingsItemState(SystemSettingsManager.SettingsItem.MOBILE_DATA) == 1
                && mSystemSettingsManager.getSystemSettingsItemState(SystemSettingsManager.SettingsItem.WIFI) == 0) {
            tvMobileData.setVisibility(View.VISIBLE);
        } else {
            tvMobileData.setVisibility(View.GONE);
        }
    }

    private void startScanWifi() {
        WifiManager wManager = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wManager.startScan();
        getContext().registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    private void stopScanWifi() {
        unregisterReceiver(getContext(), wifiReceiver);
    }

    public static boolean unregisterReceiver(Context context, BroadcastReceiver receiver) {
        if (receiver == null) {
            return true;
        }
        try {
            context.unregisterReceiver(receiver);
            return true;
        } catch (Exception e) {
            HSLog.e("Reciever", "Error unregistering broadcast receiver: " + receiver + " at ");
            e.printStackTrace();
            return false;
        }
    }

    private void startListenPhoneState() {
        if (!isListeningPhoneState) {
            TelephonyManager telephonyManager = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS | PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
            isListeningPhoneState = true;
        }
    }

    private void stopListenPhoneState() {
        if (isListeningPhoneState) {
            TelephonyManager telephonyManager = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
            isListeningPhoneState = false;
        }
    }

    @Override
    public void onSystemSettingsStateChanged(SystemSettingsManager.SettingsItem item, int state) {
        switch (item) {
            case RINGMODE:
                updateSound();
                break;
            case WIFI:
                updateWifi();
                break;
            case AIRPLANE_MODE:
                updatePlaneOn();
                break;
            case MOBILE_DATA:
                updateMobileData();
                break;
            default:
                break;
        }
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        switch (s) {
            case ScreenStatusReceiver.NOTIFICATION_SCREEN_ON:
                startListenPhoneState();
                break;
            case ScreenStatusReceiver.NOTIFICATION_SCREEN_OFF:
                stopListenPhoneState();
                break;
            default:
                break;
        }
    }
}
