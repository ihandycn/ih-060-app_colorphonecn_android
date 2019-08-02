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
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.RuntimePermissions;
import com.superapps.util.Threads;

import java.util.ArrayList;
import java.util.List;

public class RuntimePermissionActivity extends HSAppCompatActivity {
    private static final int RUNTIME_PERMISSION_REQUEST_CODE = 0x333;

    private RuntimePermissionViewListHolder holder;
    private LottieAnimationView success;
    private View toast;
    private TextView action;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.runtime_permission_activity);
        View root = findViewById(R.id.permission_list);

        List<String> permissions = new ArrayList<>();
        List<String> requestPermissions = new ArrayList<>();
        List<String> deniedPermissions = new ArrayList<>();

        permissions.add(Manifest.permission.READ_CONTACTS);
        permissions.add(Manifest.permission.WRITE_CONTACTS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            permissions.add(Manifest.permission.READ_CALL_LOG);
        }
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        for (String p : permissions) {
            if (!RuntimePermissionViewListHolder.getItemGrant(p)) {
                if (RuntimePermissions.checkSelfPermission(this, p) == RuntimePermissions.PERMISSION_PERMANENTLY_DENIED) {
                    deniedPermissions.add(p);
                }
                requestPermissions.add(p);
            }
        }

        if (requestPermissions.size() + deniedPermissions.size() > 0) {
            HSLog.i("RuntimePermission", "need request: " + requestPermissions.size() + "  denied: " + deniedPermissions.size());
            holder = new RuntimePermissionViewListHolder(root, requestPermissions);
        } else {
            HSLog.i("RuntimePermission", "All grant");
            finish();
        }
        View layout = findViewById(R.id.layout);
        layout.setBackgroundDrawable(BackgroundDrawables.createBackgroundDrawable(0xffffffff, Dimensions.pxFromDp(16), false));

        toast = findViewById(R.id.close_toast);
        toast.setBackgroundDrawable(BackgroundDrawables.createBackgroundDrawable(0x99d43d3d, Dimensions.pxFromDp(16), false));

        action = findViewById(R.id.action_btn);
        action.setBackgroundDrawable(BackgroundDrawables.createBackgroundDrawable(0xff6c63ff, Dimensions.pxFromDp(21), true));
        action.setOnClickListener(v -> {
            RuntimePermissions.requestPermissions(RuntimePermissionActivity.this, requestPermissions.toArray(new String[0]), RUNTIME_PERMISSION_REQUEST_CODE);
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
            } else {
                denied.add(perm);
            }
        }

        onPermissionsGranted(requestCode, granted);
        onPermissionsDenied(requestCode, denied);
    }

    public void onPermissionsGranted(int requestCode, List<String> list) {
        HSLog.i("Permission", "onPermissionsGranted: " + list);
        if (requestCode == RUNTIME_PERMISSION_REQUEST_CODE) {
            for (String p : list) {
                holder.refreshHolder(p);

                switch (p) {
                    case Manifest.permission.READ_CONTACTS:
                        Analytics.logEvent("Permission_Contact_Allow_Success");
                        break;
                    case Manifest.permission.WRITE_CONTACTS:
                        Analytics.logEvent("Permission_Write_Contact_Allow_Success");
                        break;
                    case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                        Analytics.logEvent("Permission_Write_Storage_Allow_Success");
                        break;
                    case Manifest.permission.READ_CALL_LOG:
                        Analytics.logEvent("Permission_Write_Settings_Allow_Success");
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
            } else {
                action.setText(R.string.runtime_permission_continue);
            }
        }
    }

    public void onPermissionsDenied(int requestCode, List<String> list) {
        HSLog.i("Permission", "onPermissionsDenied: " + list);
        if (requestCode == RUNTIME_PERMISSION_REQUEST_CODE) {
            for (String p : list) {
                holder.refreshHolder(p);
                switch (p) {
                    case Manifest.permission.READ_CONTACTS:
                        break;
                    case Manifest.permission.WRITE_CONTACTS:
                        break;
                    case Manifest.permission.READ_CALL_LOG:
                        break;
                    case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                        break;
                }
            }
        }
    }
}
