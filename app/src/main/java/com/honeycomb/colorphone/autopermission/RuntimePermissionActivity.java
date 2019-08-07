package com.honeycomb.colorphone.autopermission;

import android.Manifest;
import android.animation.Animator;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.colorphone.lock.AnimatorListenerAdapter;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.util.Analytics;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.utils.HSLog;
import com.ihs.permission.HSPermissionRequestMgr;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.RuntimePermissions;
import com.superapps.util.Threads;
import com.superapps.util.rom.RomUtils;

import java.util.ArrayList;
import java.util.List;

public class RuntimePermissionActivity extends HSAppCompatActivity {
    private static final int RUNTIME_PERMISSION_REQUEST_CODE = 0x333;

    private RuntimePermissionViewListHolder holder;
    private LottieAnimationView success;
    private View toast;
    private TextView action;

    private List<String> allPermissions;
    private List<String> deniedPermissions;
    private List<String> runtimePermissions;

    private boolean needRefresh = false;
    private boolean requested = false;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.runtime_permission_activity);
        View root = findViewById(R.id.permission_list);

        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.READ_CONTACTS);
        permissions.add(Manifest.permission.WRITE_CONTACTS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            permissions.add(Manifest.permission.READ_CALL_LOG);
        }
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        allPermissions = new ArrayList<>();
        runtimePermissions = new ArrayList<>();
        deniedPermissions = new ArrayList<>();

        for (String p : permissions) {
            if (!AutoPermissionChecker.isRuntimePermissionGrant(p)) {
                if (AutoPermissionChecker.isPermissionPermanentlyDenied(p)) {
                    deniedPermissions.add(p);
                } else {
                    runtimePermissions.add(p);
                }
                allPermissions.add(p);
            }
        }

        if (allPermissions.size() > 0) {
            HSLog.i("RuntimePermission", "need request: " + allPermissions.size() + "  denied: " + deniedPermissions.size());
            holder = new RuntimePermissionViewListHolder(root, allPermissions);
        } else {
            HSLog.i("RuntimePermission", "All grant");
            finish();
            return;
        }
        View layout = findViewById(R.id.layout);
        layout.setBackgroundDrawable(BackgroundDrawables.createBackgroundDrawable(0xffffffff, Dimensions.pxFromDp(16), false));

        toast = findViewById(R.id.close_toast);
        toast.setBackgroundDrawable(BackgroundDrawables.createBackgroundDrawable(0x99d43d3d, Dimensions.pxFromDp(16), false));

        action = findViewById(R.id.action_btn);
        action.setBackgroundDrawable(BackgroundDrawables.createBackgroundDrawable(0xff6c63ff, Dimensions.pxFromDp(21), true));
        action.setOnClickListener(v -> {
            if (runtimePermissions.size() > 0) {
                for (String p : runtimePermissions) {
                    switch (p) {
                        case Manifest.permission.READ_CONTACTS:
                            Analytics.logEvent("Permission_ReadContact_Alert_Request");
                            break;
                        case Manifest.permission.WRITE_CONTACTS:
                            Analytics.logEvent("Permission_WriteContact_Alert_Request");
                            break;
                        case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                            Analytics.logEvent("Permission_Storage_Alert_Request");
                            break;
                        case Manifest.permission.READ_CALL_LOG:
                            Analytics.logEvent("Permission_CallLog_Alert_Request");
                            break;
                    }
                }

                RuntimePermissions.requestPermissions(RuntimePermissionActivity.this, runtimePermissions.toArray(new String[0]), RUNTIME_PERMISSION_REQUEST_CODE);
            } else if (deniedPermissions.size() > 0) {
                openSettingsForDeniedPermission();
            } else {
                HSLog.i("RuntimePermission", "All grant");
                finish();
                return;
            }
            requested = true;
            action.setText(R.string.runtime_permission_continue);

            Analytics.logEvent("Permission_Guide_OK_Click");
        });

        View cancel = findViewById(R.id.close_btn);
        cancel.setOnClickListener(v -> {
            if (toast.isShown()) {
                finish();
            } else {
                toast.setVisibility(View.VISIBLE);
            }
        });

        success = findViewById(R.id.success);

        Analytics.logEvent("Permission_Guide_Show");
    }

    private void openSettingsForDeniedPermission() {
        if (deniedPermissions.size() == 0) {
            return;
        }

        String permission = "";
        for (String p : deniedPermissions) {
            if (!AutoPermissionChecker.isRuntimePermissionGrant(p)) {
                permission = p;
                break;
            }
        }

        switch (permission) {
            case Manifest.permission.READ_CONTACTS:
                permission = HSPermissionRequestMgr.TYPE_CONTACT_READ;
                break;
            default:
            case Manifest.permission.WRITE_CONTACTS:
                permission = HSPermissionRequestMgr.TYPE_CONTACT_WRITE;
                break;
            case Manifest.permission.READ_CALL_LOG:
                permission = HSPermissionRequestMgr.TYPE_CALL_LOG;
                break;
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                permission = HSPermissionRequestMgr.TYPE_STORAGE;
                break;
        }
        AutoRequestManager.getInstance().openPermission(permission);

        String eventID = "Permission_Settings_Request_" + (RomUtils.checkIsHuaweiRom() ? "Huawei" : "Xiaomi");
        Analytics.logEvent(eventID, "Permission", getDeniedPermissionString());
    }

    @Override public void onBackPressed() {
        if (toast.isShown()) {
            super.onBackPressed();
        } else {
            toast.setVisibility(View.VISIBLE);
        }
    }

    @Override protected void onStart() {
        super.onStart();
        if (needRefresh) {
            needRefresh = false;
            for (String p : allPermissions) {
                holder.refreshHolder(p);
            }

            if (holder.isAllGrant()) {
                action.setVisibility(View.INVISIBLE);
                success.setVisibility(View.VISIBLE);
                success.playAnimation();
                success.addAnimatorListener(new AnimatorListenerAdapter() {
                    @Override public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        Threads.postOnMainThreadDelayed(() -> {
                            finish();
                        }, 200);
                    }
                });
            }

            if (requested && deniedPermissions.size() > 0) {
                String eventID = "Permission_Settings_Granted_" + (RomUtils.checkIsHuaweiRom() ? "Huawei" : "Xiaomi");
                Analytics.logEvent(eventID, "Permission", getGrantDeniedPermissionString());
            }
            requested = false;
        }
    }

    @Override protected void onStop() {
        super.onStop();
        needRefresh = true;
    }

    @Override protected void onDestroy() {
        super.onDestroy();

        if (requested && deniedPermissions.size() > 0) {
            String eventID = "Permission_Settings_Granted_" + (RomUtils.checkIsHuaweiRom() ? "Huawei" : "Xiaomi");
            Analytics.logEvent(eventID, "Permission", getGrantDeniedPermissionString());
        }
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        RuntimePermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);

        List<String> granted = new ArrayList<>();
        List<String> denied = new ArrayList<>();

        for (int i = 0; i < permissions.length; ++i) {
            String perm = permissions[i];
            HSLog.i("Permission", "onRequestPermissionsResult: " + perm + "  ret: " + grantResults[i]);
            if (grantResults[i] == 0) {
                granted.add(perm);
                runtimePermissions.remove(perm);
            } else {
                denied.add(perm);
            }
        }

        onPermissionsGranted(requestCode, granted);
        onPermissionsDenied(requestCode, denied);

        if (deniedPermissions.size() > 0) {
            openSettingsForDeniedPermission();
        }
    }

    public void onPermissionsGranted(int requestCode, List<String> list) {
        HSLog.i("Permission", "onPermissionsGranted: " + list);
        if (requestCode == RUNTIME_PERMISSION_REQUEST_CODE) {
            for (String p : list) {
                holder.refreshHolder(p);

                switch (p) {
                    case Manifest.permission.READ_CONTACTS:
                        Analytics.logEvent("Permission_ReadContact_Granted");
                        break;
                    case Manifest.permission.WRITE_CONTACTS:
                        Analytics.logEvent("Permission_WriteContact_Granted");
                        break;
                    case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                        Analytics.logEvent("Permission_Storage_Granted");
                        break;
                    case Manifest.permission.READ_CALL_LOG:
                        Analytics.logEvent("Permission_CallLog_Granted");
                        break;
                }
            }

            if (holder.isAllGrant()) {
                action.setVisibility(View.INVISIBLE);
                success.setVisibility(View.VISIBLE);
                success.playAnimation();
                success.addAnimatorListener(new AnimatorListenerAdapter() {
                    @Override public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        Threads.postOnMainThreadDelayed(() -> {
                            finish();
                        }, 200);
                    }
                });
            }
        }
    }

    public void onPermissionsDenied(int requestCode, List<String> list) {
        HSLog.i("Permission", "onPermissionsDenied: " + list);
        if (requestCode == RUNTIME_PERMISSION_REQUEST_CODE) {
            for (String p : list) {
                holder.refreshHolder(p);

                if (AutoPermissionChecker.isPermissionPermanentlyDenied(p)) {
                    deniedPermissions.add(p);
                    runtimePermissions.remove(p);
                }
            }
        }
    }

    private String getDeniedPermissionString() {
        StringBuilder sb = new StringBuilder();

        if (deniedPermissions.contains(Manifest.permission.READ_CONTACTS)
                || deniedPermissions.contains(Manifest.permission.WRITE_CONTACTS)) {
            sb.append("Contact_");
        }

        for (String p : deniedPermissions) {
            switch (p) {
                default:
                case Manifest.permission.READ_CONTACTS:
                case Manifest.permission.WRITE_CONTACTS:
                    break;
                case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                    sb.append("Storage_");
                    break;
                case Manifest.permission.READ_CALL_LOG:
                    sb.append("Phone_");
                    break;

            }
        }

        if (sb.length() > 0) {
            sb.delete(sb.length() - 1, sb.length());
        } else {
            sb.append("null");
        }

        return sb.toString();
    }

    private String getGrantDeniedPermissionString() {
        StringBuilder sb = new StringBuilder();

        if (deniedPermissions.contains(Manifest.permission.READ_CONTACTS)
                || deniedPermissions.contains(Manifest.permission.WRITE_CONTACTS)) {
            if (AutoPermissionChecker.isRuntimePermissionGrant(Manifest.permission.READ_CONTACTS)
                    || AutoPermissionChecker.isRuntimePermissionGrant(Manifest.permission.WRITE_CONTACTS)) {
                sb.append("Contact_");
            }
        }

        for (String p : deniedPermissions) {
            if (AutoPermissionChecker.isRuntimePermissionGrant(p)) {
                switch (p) {
                    default:
                    case Manifest.permission.READ_CONTACTS:
                    case Manifest.permission.WRITE_CONTACTS:
                        break;
                    case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                        sb.append("Storage_");
                        break;
                    case Manifest.permission.READ_CALL_LOG:
                        sb.append("Phone_");
                        break;

                }
            }
        }

        if (sb.length() > 0) {
            sb.delete(sb.length() - 1, sb.length());
        } else {
            sb.append("null");
        }

        return sb.toString();
    }
}
