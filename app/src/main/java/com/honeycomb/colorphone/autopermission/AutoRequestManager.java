package com.honeycomb.colorphone.autopermission;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.honeycomb.colorphone.boost.FloatWindowManager;
import com.honeycomb.colorphone.startguide.RequestPermissionDialog;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.device.accessibility.service.HSAccessibilityManager;
import com.ihs.permission.HSPermissionRequestCallback;
import com.ihs.permission.HSPermissionRequestMgr;
import com.ihs.permission.HSPermissionType;
import com.ihs.permission.Utils;
import com.superapps.BuildConfig;
import com.superapps.util.Compats;
import com.superapps.util.Permissions;
import com.superapps.util.Threads;
import com.superapps.util.Toasts;

import java.util.ArrayList;

public class AutoRequestManager {
    private static final String TAG = "AutoRequestManager";
    private static final int MAX_RETRY_COUNT = 2;
    private static AutoRequestManager sManager = new AutoRequestManager();
    private boolean listened = false;
    private WindowManager windowMgr;
    private View coverView;

    private PermissionTester mPermissionTester = new PermissionTester();

    private int mRetryCount = 0;
    private WindowManager windowManager;

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
                    onAccessiblityReady();

                }
            }, filter);
            listened = true;
        }
    }

    public void onAccessiblityReady() {
        if (PermissionChecker.hasFloatWindowPermission()) {
            onFloatWindowPermissionReady();
        } else {
            HSLog.d(TAG, "start request draw overlay!");
            ArrayList<HSPermissionType> permission = new ArrayList<HSPermissionType>();
            permission.add(HSPermissionType.TYPE_DRAW_OVERLAY);
            HSPermissionRequestMgr.getInstance().startRequest(permission, new HSPermissionRequestCallback.Stub() {
                @Override
                public void onFinished(int succeedCount, int totalCount) {
                    if (succeedCount == 1) {
                        onFloatWindowPermissionReady();
                        PermissionChecker.onFloatPermissionChange(true);
                    }
                }
            });
        }
    }

    private void onFloatWindowPermissionReady() {
        HSLog.d(TAG, "onFloatWindowPermissionReady");
        showCoverWindow();

        executeAutoTask();
    }

    private void executeAutoTask() {
        ArrayList<HSPermissionType> permission = new ArrayList<HSPermissionType>();
        if (!PermissionChecker.hasAutoStartPermission()) {
            permission.add(HSPermissionType.TYPE_AUTO_START);
        }

        if (!Permissions.isNotificationAccessGranted()) {
            permission.add(HSPermissionType.TYPE_NOTIFICATION_LISTENING);
        }

        if (Compats.IS_XIAOMI_DEVICE && !PermissionChecker.hasShowOnLockScreenPermission()) {
            permission.add(HSPermissionType.TYPE_SHOW_ON_LOCK);
        }

        HSPermissionRequestMgr.getInstance().startRequest(permission, new HSPermissionRequestCallback.Stub() {
            @Override
            public void onFinished(int succeedCount, int totalCount) {
                if (totalCount == 0) {
                    if (BuildConfig.DEBUG) {
                        Toasts.showToast("Not match andy permissions!", Toast.LENGTH_LONG);
                    }
                }
                if (succeedCount == 0) {
                    HSLog.i(TAG, "No permission granted!");
                } else if (succeedCount == totalCount) {
                    HSLog.i(TAG, "All permissions granted!");
                }

                if (succeedCount < totalCount && mRetryCount < MAX_RETRY_COUNT) {
                    // Try to get
                    mRetryCount++;
                    executeAutoTask();
                } else {
                    dismissCoverWindow();
                }
            }

            @Override
            public void onSinglePermissionFinished(int index, boolean isSucceed, String msg) {
                HSLog.i(TAG, "permission request index " + index + " finished, result " + isSucceed + "，msg = " + msg);
                if (BuildConfig.DEBUG) {
                    String result = isSucceed ?  " success !" : ("  failed reason : " + msg);
                    Toasts.showToast(permission.get(index) + result, Toast.LENGTH_LONG);
                }
                HSPermissionType type = permission.get(index);
                switch (type) {
                    case TYPE_AUTO_START:
                        PermissionChecker.onAutoStartChange(isSucceed);
                        break;
                    case TYPE_NOTIFICATION_LISTENING:

                        break;
                    case TYPE_SHOW_ON_LOCK:
                        PermissionChecker.onShowOnLockScreenChange(isSucceed);
                        break;
                    default:
                        break;
                }
            }
        });
    }

//    private void showCoverWindow() {
//        if (windowMgr == null) {
//            windowMgr = (WindowManager) HSApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
//        }
//        // TODO 显示悬浮窗
//        if (coverView == null) {
//            coverView = new TextView(HSApplication.getContext());
//            ((TextView) coverView).setText("正在获取权限。。。");
//            ((TextView) coverView).setTextColor(Color.YELLOW);
//            ((TextView) coverView).setGravity(Gravity.CENTER);
//            coverView.setBackgroundColor(Color.WHITE);
//            coverView.setAlpha(0.5f);
//            windowMgr.addView(coverView, getCoverViewLayoutParams());
//        }
//    }
//
//    private ViewGroup.LayoutParams getCoverViewLayoutParams() {
//        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
//        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
//        lp.format = PixelFormat.TRANSLUCENT;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
//        } else {
//            lp.type = WindowManager.LayoutParams.TYPE_PHONE;
//        }
//
//        lp.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//
//        // In HuaWei System Settings - Notification Center - Dropzones, Default block app float window but TYPE_TOAST
//        // TYPE_TOAST float window will dismiss above api 25
//        lp.flags |=
//                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_FULLSCREEN
//                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
//        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {
//            lp.type = WindowManager.LayoutParams.TYPE_TOAST;
//        } else if (Compats.IS_HUAWEI_DEVICE) {
//            lp.type = WindowManager.LayoutParams.TYPE_TOAST;
//        }
//        return lp;
//    }
//
//    private void dismissCoverWindow() {
//        // TODO 关闭悬浮窗
//        if (coverView != null) {
//            windowMgr.removeView(coverView);
//            coverView = null;
//        }
//    }

    public void showCoverWindow() {
        FloatWindowManager.getInstance().showDialog(new RequestPermissionDialog(HSApplication.getContext()));
    }

    public void dismissCoverWindow() {
        FloatWindowManager.getInstance().removeDialog(FloatWindowManager.getInstance().getDialog(RequestPermissionDialog.class));
    }

    public void startWindowPermissionTest() {
        if (windowMgr == null) {
            windowMgr = (WindowManager) HSApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
        }

        mPermissionTester.startTest(HSApplication.getContext());
    }

    public void startAutoCheck() {
        if (Utils.isAccessibilityGranted()) {
            AutoRequestManager.getInstance().onAccessiblityReady();
        } else {
            Utils.goToAccessibilitySettingsPage();
            AutoRequestManager.getInstance().listenAccessibility();
        }
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
                            PermissionChecker.onFloatPermissionChange(true);
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
