package com.honeycomb.colorphone.autopermission;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.StringDef;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.WindowManager;
import android.widget.Toast;

import com.acb.colorphone.permissions.AccessibilityHuaweiGuideActivity;
import com.acb.colorphone.permissions.AccessibilityMIUIGuideActivity;
import com.acb.colorphone.permissions.AutoStartHuaweiGuideActivity;
import com.acb.colorphone.permissions.AutoStartMIUIGuideActivity;
import com.acb.colorphone.permissions.BackgroundPopupMIUIGuideActivity;
import com.acb.colorphone.permissions.ContactHuawei8GuideActivity;
import com.acb.colorphone.permissions.ContactHuawei9GuideActivity;
import com.acb.colorphone.permissions.ContactMIUIGuideActivity;
import com.acb.colorphone.permissions.NotificationGuideActivity;
import com.acb.colorphone.permissions.NotificationMIUIGuideActivity;
import com.acb.colorphone.permissions.PhoneHuawei8GuideActivity;
import com.acb.colorphone.permissions.PhoneMiuiGuideActivity;
import com.acb.colorphone.permissions.ShowOnLockScreenGuideActivity;
import com.acb.colorphone.permissions.ShowOnLockScreenMIUIGuideActivity;
import com.acb.colorphone.permissions.WriteSettingsPopupGuideActivity;
import com.acb.colorphone.permissions.StableToast;
import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.activity.StartGuideActivity;
import com.honeycomb.colorphone.activity.WelcomeActivity;
import com.honeycomb.colorphone.boost.FloatWindowManager;
import com.honeycomb.colorphone.startguide.RequestPermissionDialog;
import com.honeycomb.colorphone.startguide.StartGuidePermissionFactory;
import com.honeycomb.colorphone.util.Analytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.device.accessibility.service.HSAccessibilityManager;
import com.ihs.permission.HSPermissionRequestCallback;
import com.ihs.permission.HSPermissionRequestMgr;
import com.ihs.permission.HSRuntimePermissions;
import com.ihs.permission.Utils;
import com.superapps.BuildConfig;
import com.superapps.util.Compats;
import com.superapps.util.HomeKeyWatcher;
import com.superapps.util.Navigations;
import com.superapps.util.Permissions;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;
import com.superapps.util.Toasts;
import com.superapps.util.rom.RomUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

public class AutoRequestManager {
    public static final String NOTIFY_PERMISSION_CHECK_FINISH = "notification_permission_all_finish";

    public static final String NOTIFY_PERMISSION_CHECK_FINISH_AND_CLOSE_WINDOW = "notification_permission_all_finish_window_closed";

    public static final String NOTIFICATION_PERMISSION_RESULT = "notification_permission_result";
    public static final String BUNDLE_PERMISSION_TYPE = "permission_type";
    public static final String BUNDLE_PERMISSION_RESULT = "permission_result";
    public static final String AUTO_PERMISSION_FROM_AUTO = "auto";
    public static final String AUTO_PERMISSION_FROM_FIX = "fix";

    public static final String TYPE_CUSTOM_BACKGROUND_POPUP = HSPermissionRequestMgr.TYPE_BACKGROUND_POPUP;
    public static final String TYPE_CUSTOM_NOTIFICATION = "TYPE_CUSTOM_NOTIFICATION";

    private static final boolean DEBUG_TEST = false && BuildConfig.DEBUG;
    private static final long GUIDE_DELAY = 900;

    private HomeKeyWatcher homeKeyWatcher;
    private boolean needRestartApplication;

    @StringDef({ AUTO_PERMISSION_FROM_AUTO,
            AUTO_PERMISSION_FROM_FIX })
    @Retention(RetentionPolicy.SOURCE)
    private @interface AUTO_PERMISSION_FROM {}

    private static final String TAG = "AutoRequestManager";
    private static final int MAX_RETRY_COUNT = 2;
    private static AutoRequestManager sManager = new AutoRequestManager();
    private boolean listened = false;

    private int mRetryCount = 0;
    private String from;
    private String point;
    private WindowManager windowMgr;
    private boolean isCoverWindow = false;
    private boolean isRequestPermission = false;
    private boolean isRequestFloatPermission = false;
    private boolean backPressExecuted = false;
    private boolean needDoubleCheckFloatPermission = false;

    private int executeBackPressTryCount;

    private static final int CHECK_PHONE_PERMISSION = 0x800;
    private static final int CHECK_NOTIFICATION_PERMISSION = 0x801;
    private static final int CHECK_WRITE_SETTINGS_PERMISSION = 0x802;
    private static final int CHECK_NOTIFICATION_PERMISSION_RP = 0x803;
    private static final int CHECK_RUNTIME_PERMISSION = 0x804;
    private static final int CHECK_PERMISSION_TIMEOUT = 0x810;
//    public static final String FIX_ALERT_PERMISSION_PHONE = "permission_phone_for_fix_alert";

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CHECK_PHONE_PERMISSION:
                    if (AutoPermissionChecker.isPhonePermissionGranted()) {
                        onGrantPermission(StartGuidePermissionFactory.PERMISSION_TYPE_PHONE);
                    } else {
                        HSLog.i(TAG, "handleMessage CHECK_PHONE_PERMISSION");
                        sendEmptyMessageDelayed(CHECK_PHONE_PERMISSION, 500);
                    }
                    break;
                case CHECK_NOTIFICATION_PERMISSION:
                    if (AutoPermissionChecker.isNotificationListeningGranted()) {
                        onGrantPermission(StartGuidePermissionFactory.PERMISSION_TYPE_NOTIFICATION);
                    } else {
                        HSLog.i(TAG, "handleMessage CHECK_NOTIFICATION_PERMISSION");
                        sendEmptyMessageDelayed(CHECK_NOTIFICATION_PERMISSION, 500);
                    }
                    break;
                case CHECK_NOTIFICATION_PERMISSION_RP:
                    if (AutoPermissionChecker.isNotificationListeningGranted()) {
                        startRuntimePermissionActivity();
                    } else {
                        HSLog.i(TAG, "handleMessage CHECK_NOTIFICATION_PERMISSION_RP");
                        sendEmptyMessageDelayed(CHECK_NOTIFICATION_PERMISSION_RP, 500);
                    }
                    break;
                case CHECK_RUNTIME_PERMISSION:
                    if (isGrantAllRuntimePermission()) {
                        if (!AutoPermissionChecker.isNotificationListeningGranted()) {
                            openPermission(TYPE_CUSTOM_NOTIFICATION);
                        } else {
                            startRuntimePermissionActivity();
                        }
                    } else {
                        HSLog.i(TAG, "handleMessage CHECK_RUNTIME_PERMISSION");
                        sendEmptyMessageDelayed(CHECK_RUNTIME_PERMISSION, 500);
                    }
                    break;
                case CHECK_WRITE_SETTINGS_PERMISSION:
                    if (AutoPermissionChecker.isWriteSettingsPermissionGranted()) {
                        onGrantPermission(StartGuidePermissionFactory.PERMISSION_TYPE_WRITE_SETTINGS);
                    } else {
                        HSLog.i(TAG, "handleMessage CHECK_WRITE_SETTINGS_PERMISSION");
                        sendEmptyMessageDelayed(CHECK_WRITE_SETTINGS_PERMISSION, 500);
                    }
                    break;
                case CHECK_PERMISSION_TIMEOUT:
                    clearMessage();
                    break;
            }
        }
    };

    private void clearMessage() {
        mHandler.removeMessages(CHECK_PHONE_PERMISSION);
        mHandler.removeMessages(CHECK_RUNTIME_PERMISSION);
        mHandler.removeMessages(CHECK_NOTIFICATION_PERMISSION);
        mHandler.removeMessages(CHECK_NOTIFICATION_PERMISSION_RP);
        mHandler.removeMessages(CHECK_WRITE_SETTINGS_PERMISSION);

        mHandler.removeMessages(CHECK_PERMISSION_TIMEOUT);
    }

    private void onGrantPermission(int permissionType) {
        clearMessage();

        if (AutoPermissionChecker.isAccessibilityGranted()) {
            backForPhoneTask.run();
        } else {
            startStartGuideActivity(permissionType);
        }
    }

    private void startStartGuideActivity(int permissionType) {
        clearMessage();

        HSLog.i(TAG, "handleMessage startStartGuideActivity");
        Intent intent = StartGuideActivity.getIntent(HSApplication.getContext(), StartGuideActivity.FROM_KEY_GUIDE);
        intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        intent.putExtra(StartGuideActivity.INTENT_KEY_PERMISSION_TYPE, permissionType);
        Navigations.startActivitySafely(HSApplication.getContext(), intent);
    }

    private void startRuntimePermissionActivity() {
        if (AutoPermissionChecker.isAccessibilityGranted()) {
            backForPhoneTask.run();
        } else {
            HSLog.i(TAG, "handleMessage startRuntimePermissionActivity");
            Intent intent = new Intent(HSApplication.getContext(), RuntimePermissionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            Navigations.startActivitySafely(HSApplication.getContext(), intent);
        }
    }

    private AutoRequestManager() {}

    public static AutoRequestManager getInstance() {
        return sManager;
    }

    public void listenAccessibility() {
        if (!listened) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(HSAccessibilityManager.BROADCAST_ACTION_ACCESSIBILITY_SERVICE_AVAILABLE);
            HSApplication.getContext().registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Analytics.logEvent("Accessbility_Granted",
                            "From", point,
                            "Brand", AutoLogger.getBrand(),
                            "Os", AutoLogger.getOSVersion(),
                            "Time", String.valueOf(
                                    Preferences.get(Constants.DESKTOP_PREFS).getInt(StartGuideActivity.ACC_KEY_SHOW_COUNT, 0)));

                    isRequestPermission = true;
                    if (Compats.IS_XIAOMI_DEVICE) {
                        AutoRepairingToast.showRepairingToast();
                        backTask.run();
                    } else {
                        StableToast.cancelToast();
                        AutoRepairingToast.showRepairingToast();
                        onAccessibilityReady();
                    }
                    HSApplication.getContext().unregisterReceiver(this);
                    listened = false;
                }
            }, filter);
            listened = true;
        }
    }

    private Runnable backTask = new Runnable() {
        @Override
        public void run() {
            HSPermissionRequestMgr.getInstance().performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK, new HSPermissionRequestMgr.GlobalActionResult() {
                @Override
                public void onSuccess() {
                    HSLog.d(TAG, "performGlobalAction success");
                    backPressExecuted = true;
                    performPermissionCheck();
                }

                @Override
                public void onFailed() {
                    HSLog.d(TAG, "performGlobalAction fail");
                    if (executeBackPressTryCount < MAX_RETRY_COUNT) {
                        executeBackPressTryCount++;
                        HSLog.d(TAG, "performGlobalAction try , time = " + executeBackPressTryCount);
                        Threads.postOnMainThreadDelayed(backTask, 200 * executeBackPressTryCount);
                    }
                }
            });
        }
    };

    private Runnable backForPhoneTask = new Runnable() {
        @Override
        public void run() {
            HSPermissionRequestMgr.getInstance().performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK, new HSPermissionRequestMgr.GlobalActionResult() {
                @Override
                public void onSuccess() {
                    HSLog.d(TAG, "performGlobalAction success");
                    backPressExecuted = true;
                    if (!isGrantAllPermission()) {
                        startAutoCheck(AUTO_PERMISSION_FROM_FIX, StartGuideActivity.FROM_KEY_START);
                    }
                }

                @Override
                public void onFailed() {
                    HSLog.d(TAG, "performGlobalAction fail");
                    if (executeBackPressTryCount < MAX_RETRY_COUNT) {
                        executeBackPressTryCount++;
                        HSLog.d(TAG, "performGlobalAction try , time = " + executeBackPressTryCount);
                        Threads.postOnMainThreadDelayed(backTask, 200 * executeBackPressTryCount);
                    }
                }
            });
        }
    };

    public void onAccessibilityReady() {
        performPermissionCheck();
    }

    private void performPermissionCheck() {
        if (AutoPermissionChecker.hasFloatWindowPermission()) {
            onFloatWindowPermissionReady();
        } else {
            if (isRequestFloatPermission) {
                HSPermissionRequestMgr.getInstance().cancelRequest();
                HSLog.d(TAG, "User start cancel request draw overlay!");
            }
            HSLog.d(TAG, "start request draw overlay!");

            ArrayList<String> permission = new ArrayList<String>();
            if (RomUtils.checkIsMiuiRom()
                    && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
                if (!needDoubleCheckFloatPermission) {
                    permission.add(HSPermissionRequestMgr.TYPE_DRAW_OVERLAY_SYSTEM);
                    needDoubleCheckFloatPermission = true;
                } else {
                    // Double check
                    permission.add(HSPermissionRequestMgr.TYPE_DRAW_OVERLAY);
                    needDoubleCheckFloatPermission = false;
                }
            } else {
                permission.add(HSPermissionRequestMgr.TYPE_DRAW_OVERLAY);
            }
            HSPermissionRequestMgr.getInstance().startRequest(permission, new HSPermissionRequestCallback.Stub() {
                @Override
                public void onFinished(int succeedCount, int totalCount) {
                    HSLog.d(TAG, "Overlay : onFinished " + succeedCount);
                    if (succeedCount == 1) {
                        onFloatWindowPermissionReady();
                        AutoPermissionChecker.onFloatPermissionChange(true);
                        AutoLogger.logEventWithBrandAndOS("Accessbility_Float_Grant_Success");
                    } else {
                        if (needDoubleCheckFloatPermission) {
                            // Lets check it again.
                            performPermissionCheck();
                        } else {
                            notifyAutoTaskOver(false);
                            AutoLogger.logEventWithBrandAndOS("Accessbility_Float_Grant_Failed");
                        }
                    }
                }

                @Override
                public void onSinglePermissionFinished(int index, boolean isSucceed, String msg) {
                    HSLog.d(TAG, "Overlay : onSinglePermissionFinished , success = " + isSucceed );
                    AutoLogger.logAutomaticPermissionResult(HSPermissionRequestMgr.TYPE_DRAW_OVERLAY, isSucceed, msg);
                    isRequestFloatPermission = false;
                }

                @Override
                public void onSinglePermissionStarted(int index) {
                    super.onSinglePermissionStarted(index);
                    HSLog.d(TAG, "Overlay : onSinglePermissionStarted" );
                    isRequestFloatPermission = true;
                }

                @Override
                public void onSinglePermissionExecuted(int index, boolean isSucceed, String msg) {
                    super.onSinglePermissionExecuted(index, isSucceed, msg);
                    AutoRepairingToast.cancelRepairingToast();
                }

                @Override
                public void onCancelled() {
                    super.onCancelled();
                    HSLog.d(TAG, "Overlay : onCancelled");
                    isRequestFloatPermission = false;
                }
            });
        }
    }

    private void onFloatWindowPermissionReady() {
        HSLog.d(TAG, "onFloatWindowPermissionReady");
        AutoPermissionChecker.incrementAutoRequestCount();

        Threads.postOnMainThreadDelayed(() -> {
            if (!DEBUG_TEST) {
                showCoverWindow();
            }

            executeAutoTask();
        }, 1000);
    }

    private void executeAutoTask() {
        ArrayList<String> permission = new ArrayList<>();

        if (Compats.IS_XIAOMI_DEVICE && !AutoPermissionChecker.hasBgPopupPermission()) {
            permission.add(TYPE_CUSTOM_BACKGROUND_POPUP);
        }
        if (!AutoPermissionChecker.hasAutoStartPermission()) {
            permission.add(HSPermissionRequestMgr.TYPE_AUTO_START);
        }
        if (Compats.IS_XIAOMI_DEVICE && !AutoPermissionChecker.hasShowOnLockScreenPermission()) {
            permission.add(HSPermissionRequestMgr.TYPE_SHOW_ON_LOCK);
        }

        if (!AutoPermissionChecker.hasIgnoreBatteryPermission()) {
            boolean configEnable = HSConfig.optBoolean(false,
                    "Application", "AutoPermission", "IngoreBattery");
            if (configEnable) {
                permission.add(HSPermissionRequestMgr.TYPE_IGNORE_BATTERY_OPTIMIZATION);
            }
        }
        if (!Permissions.isNotificationAccessGranted()) {
            permission.add(HSPermissionRequestMgr.TYPE_ACCESS_NOTIFICATIONS);
        }

        if (!AutoPermissionChecker.isWriteSettingsPermissionGranted()) {
            permission.add(HSPermissionRequestMgr.TYPE_WRITE_SETTINGS);
        }

        if (Compats.IS_OPPO_DEVICE) {
            if (!AutoPermissionChecker.isPostNotificationPermissionGrant()) {
                permission.add(HSPermissionRequestMgr.TYPE_POST_NOTIFICATION);
            }

//            if (!AutoPermissionChecker.isAddShortcutPermissionGrant()) {
//                permission.add(HSPermissionRequestMgr.TYPE_ADD_SHORTCUT);
//            }

            List<String> runtimePermissions = getNOTGrantRuntimePermissions(getAllRuntimePermission());
            permission.addAll(runtimePermissions);
        }

        if (permission.isEmpty()) {
            notifyAutoTaskOver(true);
            return;
        }
        startWatchHomeKey();
        HSPermissionRequestMgr.getInstance().startRequest(permission, new HSPermissionRequestCallback.Stub() {
            @Override
            public void onFinished(int succeedCount, int totalCount) {
                if (totalCount == 0) {
                    if (BuildConfig.DEBUG) {
                        Toasts.showToast("Not match andy permissions!", Toast.LENGTH_LONG);
                    }
                }

                HSLog.d(TAG, "[AutoPermission-Result] : retry = " + mRetryCount
                        + ", successCount = " + succeedCount
                        + ", totalCount = " + totalCount);

                if (succeedCount < totalCount && mRetryCount < MAX_RETRY_COUNT) {
                    // Try to get
                    mRetryCount++;
                    executeAutoTask();
                } else {
                    boolean allGranted = succeedCount == totalCount;
                    notifyAutoTaskOver(allGranted);
                }
            }

            @Override
            public void onSinglePermissionFinished(int index, boolean isSucceed, String msg) {
                if (permission.size() == 0 || index < 0 || index >= permission.size()) {
                    String result = "IndexOutOfBoundsException " + permission;
                    HSLog.d(TAG, "[AutoPermission-Result] : index " + index + " finished, " + result);
                    Toasts.showToast(result, Toast.LENGTH_LONG);
                    return;
                }

                String type = permission.get(index);
                if (BuildConfig.DEBUG) {
                    String result = type + (isSucceed ? " success !" : ("  failed reason : " + msg));
                    HSLog.d(TAG, "[AutoPermission-Result] : index " + index + " finished, " + result);
                    Toasts.showToast(result, Toast.LENGTH_LONG);
                }

                switch (type) {
                    case HSPermissionRequestMgr.TYPE_AUTO_START:
                        AutoPermissionChecker.onAutoStartChange(isSucceed);
                        break;
                    case HSPermissionRequestMgr.TYPE_ACCESS_NOTIFICATIONS:

                        break;
                    case HSPermissionRequestMgr.TYPE_SHOW_ON_LOCK:
                        AutoPermissionChecker.onShowOnLockScreenChange(isSucceed);
                        break;
                    case TYPE_CUSTOM_BACKGROUND_POPUP:
                        AutoPermissionChecker.onBgPopupChange(isSucceed);
                        break;
                    case HSPermissionRequestMgr.TYPE_ADD_SHORTCUT:
                        AutoPermissionChecker.onAddShortcutPermissionChange(isSucceed);
                        break;
                    case HSPermissionRequestMgr.TYPE_POST_NOTIFICATION:
                        AutoPermissionChecker.onPostNotificationPermissionChange(isSucceed);
                        break;
                    default:
                        break;
                }
                AutoLogger.logAutomaticPermissionResult(type, isSucceed, msg);
                notifyPermissionGranted(type, isSucceed);
            }
        });
    }

    private void startWatchHomeKey() {
        if (homeKeyWatcher == null) {
            homeKeyWatcher = new HomeKeyWatcher(HSApplication.getContext());
            homeKeyWatcher.setOnHomePressedListener(new HomeKeyWatcher.OnHomePressedListener() {
                @Override
                public void onHomePressed() {
                    needRestartApplication = true;
                    Analytics.logEvent("Automatic_Permission_HomePress");
                }

                @Override
                public void onRecentsPressed() {

                }
            });
        }
        homeKeyWatcher.startWatch();
    }

    private void notifyAutoTaskOver(boolean allGranted) {
        isRequestPermission = false;
        HSGlobalNotificationCenter.sendNotification(AutoRequestManager.NOTIFY_PERMISSION_CHECK_FINISH);
        if (homeKeyWatcher != null) {
            homeKeyWatcher.stopWatch();
        }
        if (needRestartApplication) {
            Navigations.startActivitySafely(HSApplication.getContext(), WelcomeActivity.class);
        }
    }

    private void notifyPermissionGranted(String type, boolean isSucceed) {
        HSBundle hsBundle = new HSBundle();
        hsBundle.putString(BUNDLE_PERMISSION_TYPE, type);
        hsBundle.putBoolean(BUNDLE_PERMISSION_RESULT, isSucceed);
        HSGlobalNotificationCenter.sendNotification(NOTIFICATION_PERMISSION_RESULT, hsBundle);
    }

    public void showCoverWindow() {
        isCoverWindow = true;
        HSLog.w("WindowManager", "showCoverWindow: " + AutoPermissionChecker.hasFloatWindowPermission());

        FloatWindowManager.getInstance().showDialog(new RequestPermissionDialog(HSApplication.getContext()));

        if (TextUtils.equals(from, AUTO_PERMISSION_FROM_AUTO)) {
            AutoLogger.logEventWithBrandAndOS("Automatic_Begin_FromAccessbility");
        } else {
            AutoLogger.logEventWithBrandAndOS("Automatic_Begin_From_FixAlert");
        }
    }

    public void dismissCoverWindow() {
        isCoverWindow = false;
        FloatWindowManager.getInstance().removeDialog(FloatWindowManager.getInstance().getDialog(RequestPermissionDialog.class));

        if (TextUtils.equals(from, AUTO_PERMISSION_FROM_AUTO)) {
            AutoLogger.logEventWithBrandAndOS("Automatic_Finished_FromAccessbility");
        } else {
            AutoLogger.logEventWithBrandAndOS("Automatic_Finished_From_FixAlert");
        }

        if (isGrantAllPermission()) {
            if (TextUtils.equals(from, AUTO_PERMISSION_FROM_AUTO)) {
                AutoLogger.logEventWithBrandAndOS("All_Granted_From_Accessbility");
            } else {
                AutoLogger.logEventWithBrandAndOS("All_Granted_From_FixAlert");
            }

            Analytics.logEvent("All_Granted_From_Automatic",
                    "Brand", AutoLogger.getBrand(),
                    "Os", AutoLogger.getOSVersion(),
                    "Time", String.valueOf(AutoPermissionChecker.getAutoRequestCount()));
        }

        if (RomUtils.checkIsMiuiRom()) {
            Analytics.logEvent("Automatic_Permission_Granted_Xiaomi", "AccessType", AutoLogger.getPermissionString(false));
        } else if (RomUtils.checkIsHuaweiRom()) {
            Analytics.logEvent("Automatic_Permission_Granted_Huawei", "AccessType", AutoLogger.getPermissionString(true));
        }
    }

    public boolean isCoverWindowShow() {
        return isCoverWindow;
    }

    public boolean isRequestPermission() {
        return isRequestPermission;
    }

    public boolean isRequestFloatPermission() {
        return isRequestFloatPermission;
    }

    public boolean isBackPressExecuted() {
        return backPressExecuted;
    }

    public boolean isGrantAllPermission() {
        return AutoPermissionChecker.hasAutoStartPermission()
                && AutoPermissionChecker.hasBgPopupPermission()
                && AutoPermissionChecker.hasShowOnLockScreenPermission()
                && AutoPermissionChecker.isNotificationListeningGranted()
                && AutoPermissionChecker.isPhonePermissionGranted();
    }

    public boolean isGrantAllWithoutNAPermission() {
        return AutoPermissionChecker.hasAutoStartPermission()
                && AutoPermissionChecker.hasBgPopupPermission()
                && AutoPermissionChecker.hasShowOnLockScreenPermission()
                && AutoPermissionChecker.isPhonePermissionGranted();
    }

    public boolean isGrantAllRuntimePermission() {
        List<String> permissions = new ArrayList<>();
        permissions.add(HSRuntimePermissions.TYPE_RUNTIME_CONTACT_READ);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            permissions.add(HSRuntimePermissions.TYPE_RUNTIME_CALL_LOG);
        }
        permissions.add(HSRuntimePermissions.TYPE_RUNTIME_STORAGE);

        for (String p : permissions) {
            if (!AutoPermissionChecker.isRuntimePermissionGrant(p)) {
                return false;
            }
        }
        return true;
    }

    public void startAutoCheck(@AUTO_PERMISSION_FROM String from, String point) {
        this.from = from;
        this.point = point;

        if (Utils.isAccessibilityGranted()) {
            AutoRequestManager.getInstance().onAccessibilityReady();
        } else {
            Intent intent = Utils.getAccessibilitySettingsIntent();

            Analytics.logEvent("Accessbility_Show",
                    "Brand", AutoLogger.getBrand(),
                    "Os", AutoLogger.getOSVersion(),
                    "Time", String.valueOf(Preferences.get(Constants.DESKTOP_PREFS).incrementAndGetInt("Accessbility_Show")));
            Intent guideIntent = null;
            if (RomUtils.checkIsHuaweiRom()) {
                guideIntent = new Intent(HSApplication.getContext(), AccessibilityHuaweiGuideActivity.class);
                Intent finalGuideIntent1 = guideIntent;
                Threads.postOnMainThreadDelayed(() -> {
                    Navigations.startActivitySafely(HSApplication.getContext(), finalGuideIntent1);
                }, GUIDE_DELAY);

                Navigations.startActivitySafely(HSApplication.getContext(), intent);
            } else if (RomUtils.checkIsMiuiRom()) {
                guideIntent = new Intent(HSApplication.getContext(), AccessibilityMIUIGuideActivity.class);
                Navigations.startActivitiesSafely(HSApplication.getContext(), new Intent[] { intent, guideIntent});
            } else {
                Navigations.startActivitySafely(HSApplication.getContext(), intent);
            }
            AutoRequestManager.getInstance().listenAccessibility();
        }
    }

    public boolean openPermission(String type) {
        clearMessage();

        switch (type) {
            case HSPermissionRequestMgr.TYPE_AUTO_START:
                if (AutoPermissionChecker.hasAutoStartPermission()) {
                    return true;
                } else {
                    Threads.postOnMainThreadDelayed(() -> {
                        if (RomUtils.checkIsHuaweiRom()) {
                            Navigations.startActivitySafely(HSApplication.getContext(), AutoStartHuaweiGuideActivity.class);
                        } else if (RomUtils.checkIsMiuiRom()){
                            Navigations.startActivitySafely(HSApplication.getContext(), AutoStartMIUIGuideActivity.class);
                        }
                    }, GUIDE_DELAY);
                }
                break;
            case HSPermissionRequestMgr.TYPE_ACCESS_NOTIFICATIONS:
            case TYPE_CUSTOM_NOTIFICATION:
                if (AutoPermissionChecker.isNotificationListeningGranted()) {
                    return true;
                } else {
                    if (TextUtils.equals(type, TYPE_CUSTOM_NOTIFICATION)) {
                        mHandler.sendEmptyMessageDelayed(CHECK_NOTIFICATION_PERMISSION_RP, 2 * DateUtils.SECOND_IN_MILLIS);
                    } else {
                        mHandler.sendEmptyMessageDelayed(CHECK_NOTIFICATION_PERMISSION, 2 * DateUtils.SECOND_IN_MILLIS);
                    }

                    mHandler.sendEmptyMessageDelayed(CHECK_PERMISSION_TIMEOUT, 60 * DateUtils.SECOND_IN_MILLIS);
                    type = HSPermissionRequestMgr.TYPE_ACCESS_NOTIFICATIONS;

                    Threads.postOnMainThreadDelayed(() -> {
                        if (RomUtils.checkIsMiuiRom()){
                            Navigations.startActivitySafely(HSApplication.getContext(), NotificationMIUIGuideActivity.class);
                        } else {
                            Navigations.startActivitySafely(HSApplication.getContext(), NotificationGuideActivity.class);
                        }
                    }, GUIDE_DELAY);
                }
                break;
            case HSPermissionRequestMgr.TYPE_SHOW_ON_LOCK:
                if (RomUtils.checkIsMiuiRom() && AutoPermissionChecker.hasShowOnLockScreenPermission()) {
                    return true;
                } else if (RomUtils.checkIsMiuiRom() && !AutoPermissionChecker.hasShowOnLockScreenPermission()) {
                    Threads.postOnMainThreadDelayed(() -> {
                        if (RomUtils.checkIsMiuiRom()){
                            Navigations.startActivitySafely(HSApplication.getContext(), ShowOnLockScreenMIUIGuideActivity.class);
                        } else {
                            Navigations.startActivitySafely(HSApplication.getContext(), ShowOnLockScreenGuideActivity.class);
                        }
                    }, GUIDE_DELAY);
                }
                break;

            case TYPE_CUSTOM_BACKGROUND_POPUP:
                if (RomUtils.checkIsMiuiRom() && AutoPermissionChecker.hasBgPopupPermission()) {
                    return true;
                } else if (RomUtils.checkIsMiuiRom() && !AutoPermissionChecker.hasBgPopupPermission()) {
                    Threads.postOnMainThreadDelayed(() -> {
                        if (RomUtils.checkIsMiuiRom()){
                            Navigations.startActivitySafely(HSApplication.getContext(), BackgroundPopupMIUIGuideActivity.class);
                        }
                    }, GUIDE_DELAY);
                }
                break;
            case HSPermissionRequestMgr.TYPE_PHONE:
                if (AutoPermissionChecker.isPhonePermissionGranted()) {
                    return true;
                } else {
                    mHandler.sendEmptyMessageDelayed(CHECK_PHONE_PERMISSION, 2 * DateUtils.SECOND_IN_MILLIS);
                    mHandler.sendEmptyMessageDelayed(CHECK_PERMISSION_TIMEOUT, 60 * DateUtils.SECOND_IN_MILLIS);

                    Threads.postOnMainThreadDelayed(() -> {
                        if (RomUtils.checkIsMiuiRom()){
                            Navigations.startActivitySafely(HSApplication.getContext(), PhoneMiuiGuideActivity.class);
                        } else if (RomUtils.checkIsHuaweiRom()) {
                            Navigations.startActivitySafely(HSApplication.getContext(), PhoneHuawei8GuideActivity.class);
                        }
                    }, GUIDE_DELAY);
                }
                break;
            case HSPermissionRequestMgr.TYPE_WRITE_SETTINGS:
                if (AutoPermissionChecker.isWriteSettingsPermissionGranted()) {
                    return true;
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Threads.postOnMainThreadDelayed(() -> {
                            Navigations.startActivitySafely(HSApplication.getContext(), WriteSettingsPopupGuideActivity.class);
                        }, GUIDE_DELAY);
                    }
                }
                break;
            case HSPermissionRequestMgr.TYPE_CALL_LOG:
            case HSPermissionRequestMgr.TYPE_CONTACT_READ:
            case HSPermissionRequestMgr.TYPE_CONTACT_WRITE:
            case HSPermissionRequestMgr.TYPE_STORAGE:
                mHandler.sendEmptyMessageDelayed(CHECK_RUNTIME_PERMISSION, 2 * DateUtils.SECOND_IN_MILLIS);
                mHandler.sendEmptyMessageDelayed(CHECK_PERMISSION_TIMEOUT, 60 * DateUtils.SECOND_IN_MILLIS);

                Threads.postOnMainThreadDelayed(() -> {
                    if (RomUtils.checkIsMiuiRom()) {
                        Navigations.startActivitySafely(HSApplication.getContext(), ContactMIUIGuideActivity.class);
                    } else if (RomUtils.checkIsHuaweiRom()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            Navigations.startActivitySafely(HSApplication.getContext(), ContactHuawei9GuideActivity.class);
                        } else {
                            Navigations.startActivitySafely(HSApplication.getContext(), ContactHuawei8GuideActivity.class);
                        }
                    }
                }, GUIDE_DELAY);
                break;
        }

        final String permission = type;

        HSPermissionRequestMgr.getInstance().switchRequestPage(permission, new HSPermissionRequestCallback.Stub() {
            @Override
            public void onFinished(int succeedCount, int totalCount) {
                if (totalCount == 0) {
                    if (BuildConfig.DEBUG) {
                        Toasts.showToast("Not match andy permissions!", Toast.LENGTH_LONG);
                    }
                }
            }

            @Override
            public void onSinglePermissionFinished(int index, boolean isSucceed, String msg) {
                HSLog.d(TAG, "permission open index " + index + " finished, result " + isSucceed + "，msg = " + msg);
                if (isSucceed) {
                    // already has permission.
                    if (HSPermissionRequestMgr.TYPE_AUTO_START.equals(permission)) {
                        AutoPermissionChecker.onAutoStartChange(true);
                    } else if (HSPermissionRequestMgr.TYPE_SHOW_ON_LOCK.equals(permission)) {
                        AutoPermissionChecker.onShowOnLockScreenChange(true);
                    }
                    notifyPermissionGranted(permission, true);
                }
                if (BuildConfig.DEBUG) {
                    String result = isSucceed ?  " success !" : ("  failed reason : " + msg);
                    Toasts.showToast(permission + result, Toast.LENGTH_LONG);
                }
            }
        });
        return false;
    }

    public static List<String> getAllRuntimePermission() {
        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.READ_PHONE_STATE);
        permissions.add(Manifest.permission.CALL_PHONE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            permissions.add(Manifest.permission.ANSWER_PHONE_CALLS);
        }
        permissions.add(Manifest.permission.READ_CONTACTS);
        permissions.add(Manifest.permission.WRITE_CONTACTS);
        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            permissions.add(Manifest.permission.READ_CALL_LOG);
        }

        return permissions;
    }

    public static List<String> getConfirmRuntimePermission() {
        List<String> reqPermission = new ArrayList<>();
        reqPermission.add(Manifest.permission.READ_PHONE_STATE);
        reqPermission.add(Manifest.permission.CALL_PHONE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            reqPermission.add(Manifest.permission.ANSWER_PHONE_CALLS);
        }
        return reqPermission;
    }

    public static List<String> getGrantRuntimePermissions(List<String> permissions) {
        List<String> grantPermissions = new ArrayList<>();
        if (permissions != null && permissions.size() > 0) {
            for (String p : permissions) {
                if (AutoPermissionChecker.isRuntimePermissionGrant(p)) {
                    grantPermissions.add(p);
                }
            }
        }
        return grantPermissions;
    }

    public static List<String> getNOTGrantRuntimePermissions(List<String> permissions) {
        List<String> grantPermissions = new ArrayList<>();
        if (permissions != null && permissions.size() > 0) {
            for (String p : permissions) {
                if (!AutoPermissionChecker.isRuntimePermissionGrant(p)) {
                    grantPermissions.add(p);
                }
            }
        }
        return grantPermissions;
    }

    public static String getMainOpenGrantPermissionString() {
        StringBuilder permission = new StringBuilder();

        if (AutoPermissionChecker.hasAutoStartPermission()) {
            permission.append("AutoStar");
        }

        if (AutoPermissionChecker.hasShowOnLockScreenPermission()) {
            permission.append("Lock");
        }

        if (AutoPermissionChecker.hasBgPopupPermission()) {
            permission.append("Background");
        }

        if (AutoPermissionChecker.isNotificationListeningGranted()) {
            permission.append("NA");
        }

        return permission.toString();
    }
}
