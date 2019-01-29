package com.honeycomb.colorphone.permission;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.acb.call.activity.RequestPermissionsActivity;
import com.acb.call.customize.ScreenFlashManager;
import com.acb.call.customize.ScreenFlashSettings;
import com.acb.call.utils.PermissionHelper;
import com.acb.colorphone.permissions.NotificationGuideActivity;
import com.acb.utils.FontUtils;
import com.call.assistant.util.CommonUtils;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.util.ModuleUtils;
import com.honeycomb.colorphone.util.PermissionTestUtils;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;
import com.superapps.util.RuntimePermissions;
import com.superapps.util.Threads;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sundxing on 17/9/13.
 */

public class OutsidePermissionGuideActivity extends HSAppCompatActivity implements INotificationObserver {
    private static final int FIRST_LAUNCH_PERMISSION_REQUEST = 1000;
    public static final String EVENT_DISMISS = OutsidePermissionGuideActivity.class.getSimpleName() + "_dismiss";

    private String[] perms = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS};

    public static void start(Context context) {
        Intent starter = new Intent(context, OutsidePermissionGuideActivity.class);
        Navigations.startActivitySafely(context, starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HSPreferenceHelper.getDefault().putBoolean("guide_locker_stated", true);
        setContentView(R.layout.acb_request_permission_outside);

        findViewById(R.id.close_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.request_permission_later).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView enableBtn = findViewById(R.id.request_permission_action);
        enableBtn.setTypeface(FontUtils.getTypeface(FontUtils.Font.PROXIMA_NOVA_SEMIBOLD));
        enableBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(0xff3487ff,
                Dimensions.pxFromDp(6f), true));
        enableBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionTestUtils.logPermissionEvent("ColorPhone_PermissionGuide_OutSide_Click");
                ModuleUtils.setAllModuleUserEnable();
                if (CommonUtils.ATLEAST_MARSHMALLOW && requiresPermission()) {
                } else {
                    PermissionHelper.requestNotificationAccessIfNeeded(RequestPermissionsActivity.class);
                    Threads.postOnMainThreadDelayed(() -> {
                        Intent intent = new Intent(OutsidePermissionGuideActivity.this, NotificationGuideActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        Navigations.startActivitySafely(OutsidePermissionGuideActivity.this, intent);
                    }, 1000);

                    finish();
                }
            }
        });

        HSGlobalNotificationCenter.addObserver(EVENT_DISMISS, this);
        PermissionTestUtils.logPermissionEvent("ColorPhone_PermissionGuide_OutSide_Show");
    }

    /**
     * Only request first launch. (if Enabled and not has permission)
     */
    private boolean requiresPermission() {
        boolean isEnabled = ScreenFlashManager.getInstance().getAcbCallFactory().isConfigEnabled()
                && ScreenFlashSettings.isScreenFlashModuleEnabled();
        HSLog.d("ScreenFlash state change : " + isEnabled);
        if (!isEnabled) {
            HSLog.w("Permissions ScreenFlash state change : " + isEnabled);
            return false;
        }
        boolean contactPerm = RuntimePermissions.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                == RuntimePermissions.PERMISSION_GRANTED;
        if (!contactPerm) {
            PermissionTestUtils.logPermissionEvent("ColorPhone_PermissionGuide_Contact_View_Show_OutSideApp");
        }
        if (!contactPerm){
            // Do not have permissions, request them now
            RuntimePermissions.requestPermissions(this, perms, FIRST_LAUNCH_PERMISSION_REQUEST);
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        RuntimePermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);

        List<String> granted = new ArrayList<>(permissions.length);
        List<String> denied = new ArrayList<>(permissions.length);

        for(int i = 0; i < permissions.length; ++i) {
            String perm = permissions[i];
            if (grantResults[i] == 0) {
                granted.add(perm);
            } else {
                denied.add(perm);
            }
        }

        onPermissionsGranted(requestCode, granted);
        onPermissionsDenied(requestCode, denied);

        if (!PermissionHelper.isNotificationAccessGranted(OutsidePermissionGuideActivity.this)) {
            PermissionHelper.requestNotificationPermission(RequestPermissionsActivity.class, () -> {
                PermissionTestUtils.logPermissionEvent("ColorPhone_PermissionGuide_NotificationAccess_Allow_Success_OutSideApp");
            });
            PermissionTestUtils.logPermissionEvent("ColorPhone_PermissionGuide_NotificationAccess_View_Show_OutSideApp");
        }
        finish();

    }

    public void onPermissionsGranted(int requestCode, List<String> list) {
        if (requestCode == FIRST_LAUNCH_PERMISSION_REQUEST) {
            if (list.contains(Manifest.permission.READ_CONTACTS)) {
                PermissionTestUtils.logPermissionEvent("ColorPhone_PermissionGuide_Contact_Allow_Success_OutSideApp");
            }
        }
    }

    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Some permissions have been denied
        // ...
    }

    @Override public void onReceive(String s, HSBundle hsBundle) {
        if (TextUtils.equals(s, EVENT_DISMISS)) {
            finish();
        }
    }
}
