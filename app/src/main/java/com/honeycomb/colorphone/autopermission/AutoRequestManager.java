package com.honeycomb.colorphone.autopermission;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.device.accessibility.service.HSAccessibilityManager;
import com.ihs.permission.HSPermissionRequestCallback;
import com.ihs.permission.HSPermissionRequestMgr;
import com.ihs.permission.HSPermissionType;
import com.superapps.util.Compats;
import com.superapps.util.Permissions;
import com.superapps.util.Toasts;

import java.util.ArrayList;

public class AutoRequestManager {
    private static final String TAG = "AutoRequestManager";
    private static AutoRequestManager sManager = new AutoRequestManager();
    private boolean listened = false;

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
        if (Permissions.isFloatWindowAllowed(HSApplication.getContext())) {
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
                    }
                }
            });
        }
    }

    private void onFloatWindowPermissionReady() {
        showCoverWindow();
        HSLog.d(TAG, "onFloatWindowPermissionReady");
        ArrayList<HSPermissionType> permission = new ArrayList<HSPermissionType>();
        permission.add(HSPermissionType.TYPE_AUTO_START);
        permission.add(HSPermissionType.TYPE_NOTIFICATION_LISTENING);
        if (Compats.IS_XIAOMI_DEVICE) {
            permission.add(HSPermissionType.TYPE_SHOW_ON_LOCK);
        }
        HSPermissionRequestMgr.getInstance().startRequest(permission, new HSPermissionRequestCallback.Stub() {
            @Override
            public void onFinished(int succeedCount, int totalCount) {
                dismissCoverWindow();
                if (totalCount == 0) {
                    Toasts.showToast("Not match andy permissions!", Toast.LENGTH_LONG);
                }
                if (succeedCount == 0) {
                    HSLog.i(TAG, "No permission granted!");
                } else if (succeedCount == totalCount) {
                    HSLog.i(TAG, "All permissions granted!");
                }
            }

            @Override
            public void onSinglePermissionFinished(int index, boolean isSucceed, String msg) {
                HSLog.i(TAG, "permission request index " + index + " finished, result " + isSucceed + "，msg = " + msg);
                if (!isSucceed) {
                    Toasts.showToast(permission.get(index) + "  failed reason : " + msg, Toast.LENGTH_LONG);
                }
            }
        });
    }

    private void showCoverWindow() {
        // TODO 显示悬浮窗
    }

    private void dismissCoverWindow() {
        // TODO 关闭悬浮窗
    }
}
