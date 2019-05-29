package com.honeycomb.colorphone.autopermission;

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.support.annotation.StringDef;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.acb.colorphone.permissions.AccessibilityHuaweiGuideActivity;
import com.acb.colorphone.permissions.AccessibilityMIUIGuideActivity;
import com.acb.colorphone.permissions.AutoStartHuaweiGuideActivity;
import com.acb.colorphone.permissions.AutoStartMIUIGuideActivity;
import com.acb.colorphone.permissions.BackgroundPopupMIUIGuideActivity;
import com.acb.colorphone.permissions.NotificationGuideActivity;
import com.acb.colorphone.permissions.NotificationMIUIGuideActivity;
import com.acb.colorphone.permissions.ShowOnLockScreenGuideActivity;
import com.acb.colorphone.permissions.ShowOnLockScreenMIUIGuideActivity;
import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.activity.StartGuideActivity;
import com.honeycomb.colorphone.activity.WelcomeActivity;
import com.honeycomb.colorphone.boost.FloatWindowManager;
import com.honeycomb.colorphone.startguide.RequestPermissionDialog;
import com.honeycomb.colorphone.util.Analytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.device.accessibility.service.HSAccessibilityManager;
import com.ihs.permission.HSPermissionRequestCallback;
import com.ihs.permission.HSPermissionRequestMgr;
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

public class AutoRequestManager {
    public static final String NOTIFY_PERMISSION_CHECK_FINISH = "notification_permission_all_finish";

    public static final String NOTIFY_PERMISSION_CHECK_FINISH_AND_CLOSE_WINDOW = "notification_permission_all_finish_window_closed";

    public static final String NOTIFICATION_PERMISSION_RESULT = "notification_permission_result";
    public static final String BUNDLE_PERMISSION_TYPE = "permission_type";
    public static final String BUNDLE_PERMISSION_RESULT = "permission_result";
    public static final String AUTO_PERMISSION_FROM_AUTO = "auto";
    public static final String AUTO_PERMISSION_FROM_FIX = "fix";

    public static final String TYPE_CUSTOM_CONTACT_READ = "ReadContact";
    public static final String TYPE_CUSTOM_CONTACT_WRITE = "WriteContact";

    /**
     * 后台弹出界面
     */
    public static final String TYPE_CUSTOM_BACKGROUND_POPUP = "BackgroundPopup";

    private static final boolean DEBUG_TEST = false && BuildConfig.DEBUG;

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

    private PermissionTester mPermissionTester = new PermissionTester();

    private int mRetryCount = 0;
    private String from;
    private WindowManager windowMgr;
    private boolean isCoverWindow = false;
    private boolean isRequestPermission = false;

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
                            "Brand", AutoLogger.getBrand(),
                            "Os", AutoLogger.getOSVersion(),
                            "Time", String.valueOf(
                                    Preferences.get(Constants.DESKTOP_PREFS).getInt(StartGuideActivity.ACC_KEY_SHOW_COUNT, 0)));

                    onAccessibilityReady();

                }
            }, filter);
            listened = true;
        }
    }

    public void onAccessibilityReady() {
        isRequestPermission = true;
        if (Compats.IS_XIAOMI_DEVICE) {
            HSPermissionRequestMgr.getInstance().performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK, new HSPermissionRequestMgr.GloableActionResult() {
                @Override
                public void onSuccess() {
                    HSLog.d(TAG, "performGlobalAction success");
                    performPermissionCheck();
                }

                @Override
                public void onFailed() {
                    HSLog.d(TAG, "performGlobalAction fail");
                }
            });

        } else {
            performPermissionCheck();
        }

    }

    private void performPermissionCheck() {
        if (AutoPermissionChecker.hasFloatWindowPermission()) {
            onFloatWindowPermissionReady();
        } else {
            HSLog.d(TAG, "start request draw overlay!");
            ArrayList<String> permission = new ArrayList<String>();
            permission.add(HSPermissionRequestMgr.TYPE_DRAW_OVERLAY);
            HSPermissionRequestMgr.getInstance().startRequest(permission, new HSPermissionRequestCallback.Stub() {
                @Override
                public void onFinished(int succeedCount, int totalCount) {
                    if (succeedCount == 1) {
                        onFloatWindowPermissionReady();
                        AutoPermissionChecker.onFloatPermissionChange(true);
                        AutoLogger.logEventWithBrandAndOS("Accessbility_Float_Grant_Success");
                    } else {
                        notifyAutoTaskOver(false);
                        AutoLogger.logEventWithBrandAndOS("Accessbility_Float_Grant_Failed");
                    }
                }

                @Override
                public void onSinglePermissionFinished(int index, boolean isSucceed, String msg) {
                    if (!isSucceed) {
                        AutoLogger.logAutomaticPermissionFailed(HSPermissionRequestMgr.TYPE_DRAW_OVERLAY, msg);
                    }
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
        ArrayList<String> permission = new ArrayList<String>();
        if (!AutoPermissionChecker.hasAutoStartPermission()) {
            permission.add(HSPermissionRequestMgr.TYPE_AUTO_START);
        }

        if (Compats.IS_XIAOMI_DEVICE && !AutoPermissionChecker.hasShowOnLockScreenPermission()) {
            permission.add(HSPermissionRequestMgr.TYPE_SHOW_ON_LOCK);
        }
        if (Compats.IS_XIAOMI_DEVICE && !AutoPermissionChecker.hasBgPopupPermission()) {
            permission.add(TYPE_CUSTOM_BACKGROUND_POPUP);
        }
        if (Compats.IS_XIAOMI_DEVICE) {
            permission.add(TYPE_CUSTOM_CONTACT_WRITE);
            permission.add(TYPE_CUSTOM_CONTACT_READ);
        }
        if (!Permissions.isNotificationAccessGranted()) {
            permission.add(HSPermissionRequestMgr.TYPE_NOTIFICATION_LISTENING);
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
                if (BuildConfig.DEBUG) {
                    String result = permission.get(index)
                            + (isSucceed ? " success !" : ("  failed reason : " + msg));
                    HSLog.d(TAG, "[AutoPermission-Result] : index " + index + " finished, " + result);
                    Toasts.showToast(result, Toast.LENGTH_LONG);
                }
                String type = permission.get(index);
                switch (type) {
                    case HSPermissionRequestMgr.TYPE_AUTO_START:
                        AutoPermissionChecker.onAutoStartChange(isSucceed);
                        break;
                    case HSPermissionRequestMgr.TYPE_NOTIFICATION_LISTENING:

                        break;
                    case HSPermissionRequestMgr.TYPE_SHOW_ON_LOCK:
                        AutoPermissionChecker.onShowOnLockScreenChange(isSucceed);
                        break;
                    case TYPE_CUSTOM_BACKGROUND_POPUP:
                        AutoPermissionChecker.onBgPopupChange(isSucceed);
                        break;
                    default:
                        break;
                }
                if (!isSucceed) {
                    AutoLogger.logAutomaticPermissionFailed(type, msg);
                }
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

    public boolean isGrantAllPermission() {
        return AutoPermissionChecker.hasAutoStartPermission()
                && AutoPermissionChecker.hasShowOnLockScreenPermission()
                && AutoPermissionChecker.isNotificationListeningGranted();
    }

    public void startWindowPermissionTest() {
        if (windowMgr == null) {
            windowMgr = (WindowManager) HSApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
        }

        mPermissionTester.startTest(HSApplication.getContext());
    }

    public void startAutoCheck(@AUTO_PERMISSION_FROM String from) {
        this.from = from;

        if (Utils.isAccessibilityGranted()) {
            AutoRequestManager.getInstance().onAccessibilityReady();
        } else {
            Utils.goToAccessibilitySettingsPage();
            Analytics.logEvent("Accessbility_Show",
                    "Brand", AutoLogger.getBrand(),
                    "Os", AutoLogger.getOSVersion(),
                    "Time", String.valueOf(Preferences.get(Constants.DESKTOP_PREFS).incrementAndGetInt("Accessbility_Show")));
            Threads.postOnMainThreadDelayed(() -> {
                if (RomUtils.checkIsHuaweiRom()) {
                    Navigations.startActivitySafely(HSApplication.getContext(), AccessibilityHuaweiGuideActivity.class);
                } else if (RomUtils.checkIsMiuiRom()) {
                    Navigations.startActivitySafely(HSApplication.getContext(), AccessibilityMIUIGuideActivity.class);
                }
            }, 900);
            AutoRequestManager.getInstance().listenAccessibility();
        }
    }

    public boolean openPermission(String type) {
        if (HSPermissionRequestMgr.TYPE_AUTO_START.equals(type)) {
            if (AutoPermissionChecker.hasAutoStartPermission()) {
                return true;
            } else {
                Threads.postOnMainThreadDelayed(() -> {
                    if (RomUtils.checkIsHuaweiRom()) {
                        Navigations.startActivitySafely(HSApplication.getContext(), AutoStartHuaweiGuideActivity.class);
                    } else if (RomUtils.checkIsMiuiRom()){
                        Navigations.startActivitySafely(HSApplication.getContext(), AutoStartMIUIGuideActivity.class);
                    }
                }, 900);
            }
        } else if (HSPermissionRequestMgr.TYPE_NOTIFICATION_LISTENING.equals(type)) {
            if (Permissions.isNotificationAccessGranted()) {
                return true;
            } else {
                Threads.postOnMainThreadDelayed(() -> {
                    if (RomUtils.checkIsMiuiRom()){
                        Navigations.startActivitySafely(HSApplication.getContext(), NotificationMIUIGuideActivity.class);
                    } else {
                        Navigations.startActivitySafely(HSApplication.getContext(), NotificationGuideActivity.class);
                    }
                }, 900);
            }
        } else if (HSPermissionRequestMgr.TYPE_SHOW_ON_LOCK.equals(type)) {
            if (RomUtils.checkIsMiuiRom() && AutoPermissionChecker.hasShowOnLockScreenPermission()) {
                return true;
            } else if (RomUtils.checkIsMiuiRom() && !AutoPermissionChecker.hasShowOnLockScreenPermission()) {
                Threads.postOnMainThreadDelayed(() -> {
                    if (RomUtils.checkIsMiuiRom()){
                        Navigations.startActivitySafely(HSApplication.getContext(), ShowOnLockScreenMIUIGuideActivity.class);
                    } else {
                        Navigations.startActivitySafely(HSApplication.getContext(), ShowOnLockScreenGuideActivity.class);
                    }
                }, 900);
            }
        } else if (TYPE_CUSTOM_BACKGROUND_POPUP.equals(type)) {
            if (RomUtils.checkIsMiuiRom() && AutoPermissionChecker.hasBgPopupPermission()) {
                return true;
            } else if (RomUtils.checkIsMiuiRom() && !AutoPermissionChecker.hasBgPopupPermission()) {
                Threads.postOnMainThreadDelayed(() -> {
                    if (RomUtils.checkIsMiuiRom()){
                        Navigations.startActivitySafely(HSApplication.getContext(), BackgroundPopupMIUIGuideActivity.class);
                    }
                }, 900);
            }
        }

        HSPermissionRequestMgr.getInstance().switchRequestPage(type, new HSPermissionRequestCallback.Stub() {
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
                    if (HSPermissionRequestMgr.TYPE_AUTO_START.equals(type)) {
                        AutoPermissionChecker.onAutoStartChange(true);
                    } else if (HSPermissionRequestMgr.TYPE_SHOW_ON_LOCK.equals(type)) {
                        AutoPermissionChecker.onShowOnLockScreenChange(true);
                    }
                    notifyPermissionGranted(type, true);
                }
                if (BuildConfig.DEBUG) {
                    String result = isSucceed ?  " success !" : ("  failed reason : " + msg);
                    Toasts.showToast(type + result, Toast.LENGTH_LONG);
                }
            }
        });
        return false;
    }

    private class PermissionTester {
        View testView;
        boolean hasFloatWindowPermission;

        private void startTest(Context context) {
            if (testView == null) {
                testView = new TextView(context);
                if (BuildConfig.DEBUG) {
                    ((TextView) testView).setText("TESTEST");
                    ((TextView) testView).setTextSize(40);
                    ((TextView) testView).setTextColor(Color.YELLOW);
                }
                try {
                    windowMgr.addView(testView, getEmptyParams());
                    testView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                        @Override
                        public void onViewAttachedToWindow(View v) {
                            HSLog.d(TAG, "onViewAttachedToWindow : window show success");
                            hasFloatWindowPermission = true;
                            AutoPermissionChecker.onFloatPermissionChange(true);
                            removeTestView();
                        }

                        @Override
                        public void onViewDetachedFromWindow(View v) {
                            HSLog.d(TAG, "onViewDetachedFromWindow : window show success");
                        }
                    });
                    testView.requestFocus();
                    Threads.postOnMainThreadDelayed(new Runnable() {
                        @Override
                        public void run() {
                            removeTestView();
                        }
                    }, 2000);
                } catch (Exception e) {
                    HSLog.d(TAG, "window show fail");
                    e.printStackTrace();
                }
            }
        }

        private void removeTestView() {
            if (testView != null) {
                windowMgr.removeView(testView);
                testView = null;
            }
        }

        private  WindowManager.LayoutParams getEmptyParams() {
            WindowManager.LayoutParams emptyParams = new WindowManager.LayoutParams();
            emptyParams.flags |= WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN;
            if (!BuildConfig.DEBUG) {
                emptyParams.height = 1;
                emptyParams.width = 1;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                emptyParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else  {
                emptyParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            }
            emptyParams.format = PixelFormat.TRANSPARENT;

            return emptyParams;
        }

    }

}
