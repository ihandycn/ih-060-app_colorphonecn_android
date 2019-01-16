package com.honeycomb.colorphone.permission;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.acb.call.activity.RequestPermissionsActivity;
import com.acb.call.customize.ScreenFlashManager;
import com.acb.call.customize.ScreenFlashSettings;
import com.acb.call.utils.PermissionHelper;
import com.acb.utils.FontUtils;
import com.call.assistant.util.CommonUtils;
import com.honeycomb.colorphone.R;
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
        TextView enableBtn = findViewById(R.id.request_permission_action);
        enableBtn.setTypeface(FontUtils.getTypeface(FontUtils.Font.PROXIMA_NOVA_SEMIBOLD));
        enableBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(0xff3487ff,
                Dimensions.pxFromDp(6f), true));
        enableBtn.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && requiresPermission()) {
            } else {
                PermissionHelper.requestNotificationAccessIfNeeded(RequestPermissionsActivity.class);
                finish();
            }
        });

        HSGlobalNotificationCenter.addObserver(EVENT_DISMISS, this);
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

        boolean phonePerm = RuntimePermissions.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                == RuntimePermissions.PERMISSION_GRANTED;
        boolean contactPerm = RuntimePermissions.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                == RuntimePermissions.PERMISSION_GRANTED;
        if (!phonePerm) {
//            LauncherAnalytics.logEvent("ColorPhone_Permission__Phone_SystemStyle_Show_FirstScreen");
        }
        if (!contactPerm) {
//            LauncherAnalytics.logEvent("ColorPhone_Permission__Contact_SystemStyle_Show_FirstScreen");
        }
        if (!phonePerm || !contactPerm){
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

        if (!CommonUtils.ATLEAST_MARSHMALLOW) {
            PermissionHelper.requestNotificationAccessIfNeeded(RequestPermissionsActivity.class);
        }
        finish();

    }

    public void onPermissionsGranted(int requestCode, List<String> list) {
        if (requestCode == FIRST_LAUNCH_PERMISSION_REQUEST) {
            if (list.contains(Manifest.permission.READ_PHONE_STATE)) {
//                LauncherAnalytics.logEvent("ColorPhone_Permission_Phone_SystemStyle_Allow_Click_FirstScreen");
            }
            if (list.contains(Manifest.permission.READ_CONTACTS)) {
//                LauncherAnalytics.logEvent("ColorPhone_Permission_Contact_SystemStyle_Allow_Click_FirstScreen");
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
