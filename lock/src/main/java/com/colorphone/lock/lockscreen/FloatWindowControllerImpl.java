package com.colorphone.lock.lockscreen;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.colorphone.lock.R;
import com.colorphone.lock.lockscreen.chargingscreen.ChargingScreen;
import com.colorphone.lock.lockscreen.locker.Locker;
import com.colorphone.smartlocker.SmartLockerManager;
import com.colorphone.smartlocker.SmartLockerScreen;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Navigations;
import com.superapps.util.Permissions;

import java.util.ArrayList;
import java.util.List;


public class FloatWindowControllerImpl {

    private List<String> startAlarmAction;
    private List<String> terminalAlarmAction;
    private Context context;
    private boolean isAutoUnlocked = false;
    private boolean isCalling = false;

    private ViewGroup container;
    private LockScreen lockScreenWindow;
    private WindowManager windowMgr;
    private boolean addedToWindowMgr;
    private boolean isShowLockScreen;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        private final static String SYSTEM_DIALOG_REASON_KEY = "reason";
        private final static String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
        private final static String SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS = "globalactions";

        private boolean alarmAlert;

        @Override
        public void onReceive(Context context, Intent intent) {
            HSLog.d("onReceive(), screen broadcast receiver, intent action = " + intent.getAction());

            if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(intent.getAction())) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                HSLog.i("action on reason == " + reason + "  isAuto == " + isAutoLockState());
                if (null == reason) {
                    return;
                }
                switch (reason) {
                    case SYSTEM_DIALOG_REASON_HOME_KEY:
                        if (isAutoLockState()) {
                            showLockScreen();
                        }
                        break;
                    case SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS:
                        hideLockScreen(true);
                        break;
                    default:
                        break;
                }
            } else if (startAlarmAction.contains(intent.getAction())) {
                if (addedToWindowMgr) {
                    hideLockScreen(true);
                    alarmAlert = true;
                }
            } else if (terminalAlarmAction.contains(intent.getAction())) {
                if (alarmAlert) {
                    showLockScreen();
                    alarmAlert = false;
                }
            }
        }
    };

    @SuppressLint("InflateParams")
    public FloatWindowControllerImpl(final Context context) {
        this.context = context;

        windowMgr = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        initAlertAction();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
        intentFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        for (String action : startAlarmAction) {
            intentFilter.addAction(action);
        }

        for (String action : terminalAlarmAction) {
            intentFilter.addAction(action);
        }
        context.registerReceiver(broadcastReceiver, intentFilter);

        TelephonyManager telephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyMgr.listen(new PhoneStateListener() {

            private boolean incomingcall;

            public void onCallStateChanged(int state, String number) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_RINGING:
                        isCalling = true;
                        if (addedToWindowMgr) {
                            hideLockScreen(true);
                            incomingcall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        isCalling = true;
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        isCalling = false;
                        if (incomingcall) {
                            showLockScreen();
                            incomingcall = false;
                        }
                        break;
                    default:
                        break;
                }
            }
        }, PhoneStateListener.LISTEN_CALL_STATE);
    }

    private void initAlertAction() {
        startAlarmAction = new ArrayList<>();
        terminalAlarmAction = new ArrayList<>();

        startAlarmAction.add("com.android.deskclock.ALARM_ALERT");
        startAlarmAction.add("com.samsung.sec.android.clockpackage.alarm.ALARM_ALERT");
        startAlarmAction.add("com.htc.worldclock.ALARM_ALERT");
        startAlarmAction.add("com.sonyericsson.alarm.ALARM_ALERT");
        startAlarmAction.add("zte.com.cn.alarmclock.ALARM_ALERT");
        startAlarmAction.add("com.motorola.blur.alarmclock.ALARM_ALERT");
        startAlarmAction.add("com.lge.alarm.alarmclocknew");
        startAlarmAction.add("com.lge.clock.alarmclock");
        startAlarmAction.add("com.htc.android.ALARM_ALERT");
        startAlarmAction.add("android.intent.action.ALARM_CHANGED");

        terminalAlarmAction.add("com.android.deskclock.ALARM_DISMISS");
        terminalAlarmAction.add("com.android.deskclock.ALARM_DONE");
        terminalAlarmAction.add("com.android.deskclock.ALARM_SNOOZE");
    }

    public void showChargingScreen(Bundle bundle) {
        if (!hasPermission(Manifest.permission.SYSTEM_ALERT_WINDOW) && !Permissions.isFloatWindowAllowed(context)) {
            return;
        }
        if (addedToWindowMgr && isShowLockScreen) {
            doHideLockScreen(false);
        }

        Navigations.startActivity(context, DismissActivity.class);

        HSLog.i("LockManager", "showChargingScreen");
        if (!addedToWindowMgr) {
            addedToWindowMgr = true;
            isAutoUnlocked = false;

            if (SmartLockerManager.isShowH5NewsLocker()) {
                container = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.activity_smart_locker_feeds, null);
                lockScreenWindow = new SmartLockerScreen();
                lockScreenWindow.setActivityMode(false);
                Bundle smartBundle = new Bundle();
                smartBundle.putInt(SmartLockerManager.EXTRA_START_TYPE, SmartLockerManager.EXTRA_VALUE_START_BY_CHARGING_SCREEN_OFF);
                lockScreenWindow.setup(container, smartBundle);
            } else {
                container = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.activity_charging_screen, null);
                lockScreenWindow = new ChargingScreen();
                lockScreenWindow.setActivityMode(false);
                lockScreenWindow.setup(container, bundle);
            }

            try {
                windowMgr.addView(container, FloatWindowCompat.getLockScreenParams());
                HSLog.d("manual_flurry", "Locker: start flurry .......");
                HSAnalytics.startFlurry();
            } catch (SecurityException ignored) {
            }
        }
    }

    public void showLockScreen() {
        if (!hasPermission(Manifest.permission.SYSTEM_ALERT_WINDOW) && !Permissions.isFloatWindowAllowed(context)) {
            return;
        }
        Navigations.startActivity(context, DismissActivity.class);
        HSLog.i("LockManager", "showLockScreen ");
        if (!addedToWindowMgr) {
            addedToWindowMgr = true;
            isShowLockScreen = true;
            isAutoUnlocked = false;

            if (SmartLockerManager.isShowH5NewsLocker()) {
                container = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.activity_smart_locker_feeds, null);
                lockScreenWindow = new SmartLockerScreen();
                Bundle smartBundle = new Bundle();
                smartBundle.putInt(SmartLockerManager.EXTRA_START_TYPE, SmartLockerManager.EXTRA_VALUE_START_BY_LOCKER);
                lockScreenWindow.setActivityMode(false);
                lockScreenWindow.setup(container, smartBundle);
            } else {
                container = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.activity_locker, null);
                lockScreenWindow = new Locker();
                lockScreenWindow.setActivityMode(false);
                lockScreenWindow.setup(container, null);
            }

            try {
                windowMgr.addView(container, FloatWindowCompat.getLockScreenParams());
                HSLog.d("manual_flurry", "Locker: start flurry .......");
                HSAnalytics.startFlurry();
            } catch (SecurityException ignored) {

            }
        }
    }

    public void hideLockScreen(boolean dismissKeyguard) {
        HSLog.i("hideLockScreen(), auto lock = " + dismissKeyguard);
        doHideLockScreen(dismissKeyguard);
    }

    public void hideLockScreen(int hideType) {
        HSLog.i("hideLockScreen(), hideType = " + hideType);
        doHideLockScreen((hideType & FloatWindowController.HIDE_LOCK_WINDOW_NO_ANIMATION) == 0);
    }

    private void doHideLockScreen(boolean dismissKeyguard) {
        DismissActivity.hide();
        if (addedToWindowMgr) {
            try {
                windowMgr.removeView(container);
                HSLog.d("manual_flurry", "Locker: stop flurry .......");
                HSAnalytics.stopFlurry();
                container = null;
            } catch (IllegalStateException exception) {
                exception.printStackTrace();
            }
            addedToWindowMgr = false;
            isShowLockScreen = false;

            try {
                if (hasPermission(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
                    View emptyView = new View(context);
                    windowMgr.addView(emptyView, getEmptyParams());
                    windowMgr.removeView(emptyView);
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            if (dismissKeyguard) {
                DismissKeyguradActivity.startSelfIfKeyguardSecure(context);
            }
        }
    }

    public boolean isAutoLockState() {
        return !addedToWindowMgr && isAutoUnlocked;
    }

    public boolean isLockScreenShown() {
        return addedToWindowMgr;
    }

    public boolean isCalling() {
        return isCalling;
    }

    public void release() {
        context.unregisterReceiver(broadcastReceiver);
    }

    private WindowManager.LayoutParams getEmptyParams() {
        WindowManager.LayoutParams emptyParams = new WindowManager.LayoutParams();
        emptyParams.flags |= WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN;
        emptyParams.height = 1;
        emptyParams.width = 1;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            emptyParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            emptyParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        emptyParams.format = PixelFormat.TRANSPARENT;

        return emptyParams;
    }

    public static boolean hasPermission(String permission) {
        boolean granted = false;
        if (!TextUtils.isEmpty(permission)) {
            try {
                granted = ContextCompat.checkSelfPermission(HSApplication.getContext(), permission)
                        == PackageManager.PERMISSION_GRANTED;
            } catch (RuntimeException ignored) {
            }
        }
        return granted;
    }

}
