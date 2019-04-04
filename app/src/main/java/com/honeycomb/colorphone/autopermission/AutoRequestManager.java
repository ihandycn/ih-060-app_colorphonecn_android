package com.honeycomb.colorphone.autopermission;

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
import com.ihs.permission.HSPermissionType;
import com.ihs.permission.Utils;
import com.superapps.BuildConfig;
import com.superapps.util.Compats;
import com.superapps.util.HomeKeyWatcher;
import com.superapps.util.Navigations;
import com.superapps.util.Permissions;
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
                    AutoLogger.logEventWithBrandAndOS("Accessbility_Granted");
                    onAccessibilityReady();

                }
            }, filter);
            listened = true;
        }
    }

    public void onAccessibilityReady() {
        if (AutoPermissionChecker.hasFloatWindowPermission()) {
            onFloatWindowPermissionReady();
            AutoPermissionChecker.incrementAutoRequestCount();
        } else {
            HSLog.d(TAG, "start request draw overlay!");
            ArrayList<HSPermissionType> permission = new ArrayList<HSPermissionType>();
            permission.add(HSPermissionType.TYPE_DRAW_OVERLAY);
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
            });
        }
    }

    private void onFloatWindowPermissionReady() {
        HSLog.d(TAG, "onFloatWindowPermissionReady");
        Threads.postOnMainThreadDelayed(() -> {
            showCoverWindow();

            executeAutoTask();
        }, 1000);
    }

    private void executeAutoTask() {
        ArrayList<HSPermissionType> permission = new ArrayList<HSPermissionType>();
        if (!AutoPermissionChecker.hasAutoStartPermission()) {
            permission.add(HSPermissionType.TYPE_AUTO_START);
        }

        if (Compats.IS_XIAOMI_DEVICE && !AutoPermissionChecker.hasShowOnLockScreenPermission()) {
            permission.add(HSPermissionType.TYPE_SHOW_ON_LOCK);
        }

        if (!Permissions.isNotificationAccessGranted()) {
            permission.add(HSPermissionType.TYPE_NOTIFICATION_LISTENING);
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
                HSPermissionType type = permission.get(index);
                switch (type) {
                    case TYPE_AUTO_START:
                        AutoPermissionChecker.onAutoStartChange(isSucceed);
                        break;
                    case TYPE_NOTIFICATION_LISTENING:

                        break;
                    case TYPE_SHOW_ON_LOCK:
                        AutoPermissionChecker.onShowOnLockScreenChange(isSucceed);
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

    private void notifyPermissionGranted(HSPermissionType type, boolean isSucceed) {
        HSBundle hsBundle = new HSBundle();
        hsBundle.putObject(BUNDLE_PERMISSION_TYPE, type);
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

            Analytics.logEvent("All_Granted_From_Automatic", "Time", String.valueOf(AutoPermissionChecker.getAutoRequestCount()));
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
        isRequestPermission = true;

        if (Utils.isAccessibilityGranted()) {
            AutoRequestManager.getInstance().onAccessibilityReady();
        } else {
            Utils.goToAccessibilitySettingsPage();
            Threads.postOnMainThreadDelayed(() -> {
                if (RomUtils.checkIsHuaweiRom()) {
                    Navigations.startActivitySafely(HSApplication.getContext(), AccessibilityHuaweiGuideActivity.class);
                }
//                else if (RomUtils.checkIsMiuiRom()) {
//                    Navigations.startActivitySafely(HSApplication.getContext(), AccessibilityMIUIGuideActivity.class);
//                }
            }, 900);
            AutoRequestManager.getInstance().listenAccessibility();
        }
    }

    public boolean openPermission(HSPermissionType type) {
        if (type == HSPermissionType.TYPE_AUTO_START && AutoPermissionChecker.hasAutoStartPermission()) {
            return true;
        } else if (type == HSPermissionType.TYPE_NOTIFICATION_LISTENING && Permissions.isNotificationAccessGranted()) {
            return true;
        } else if (type == HSPermissionType.TYPE_SHOW_ON_LOCK && Compats.IS_XIAOMI_DEVICE && AutoPermissionChecker.hasShowOnLockScreenPermission()) {
            return true;
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
                    if (type == HSPermissionType.TYPE_AUTO_START) {
                        AutoPermissionChecker.onAutoStartChange(true);
                    } else if (type == HSPermissionType.TYPE_SHOW_ON_LOCK) {
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
