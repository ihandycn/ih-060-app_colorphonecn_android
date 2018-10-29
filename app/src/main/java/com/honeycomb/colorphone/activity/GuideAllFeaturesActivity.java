package com.honeycomb.colorphone.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;

import com.acb.call.customize.ScreenFlashManager;
import com.acb.call.customize.ScreenFlashSettings;
import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.dialer.AP;
import com.honeycomb.colorphone.dialer.guide.GuideSetDefaultActivity;
import com.honeycomb.colorphone.gdpr.GdprUtils;
import com.honeycomb.colorphone.notification.floatwindow.FloatWindowController;
import com.honeycomb.colorphone.notification.permission.EventSource;
import com.honeycomb.colorphone.notification.permission.PermissionHelper;
import com.honeycomb.colorphone.recentapp.SmartAssistantUtils;
import com.honeycomb.colorphone.util.FontUtils;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.honeycomb.colorphone.util.ModuleUtils;
import com.honeycomb.colorphone.util.StatusBarUtils;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.device.monitor.topapp.HSUsageAccessMgr;
import com.superapps.util.RuntimePermissions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sundxing on 17/9/13.
 */

public class GuideAllFeaturesActivity extends HSAppCompatActivity {
    private static final int FIRST_LAUNCH_PERMISSION_REQUEST = 1000;

    private String[] perms = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS};
    private int permsCount = 0;

    Handler mHandler = new Handler(Looper.getMainLooper());

    public static void start(Context context) {
        Intent starter = new Intent(context, GuideAllFeaturesActivity.class);
        context.startActivity(starter);
    }

    public static boolean isStarted() {
       return HSPreferenceHelper.getDefault().getBoolean("guide_locker_stated", false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HSPreferenceHelper.getDefault().putBoolean("guide_locker_stated", true);
        AP.guideShow();
        setContentView(R.layout.guide_all_features);
        StatusBarUtils.hideStatusBar(this);

        setUpPrivacyTextView();
        LauncherAnalytics.logEvent("ColorPhone_StartGuide_Show");
        findViewById(R.id.guide_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LauncherAnalytics.logEvent("ColorPhone_StartGuide_Cancel_Clicked");

                finish();
            }
        });
        TextView enableBtn = (TextView) findViewById(R.id.welcome_guide_function_enable_btn);
        enableBtn.setTypeface(FontUtils.getTypeface(FontUtils.Font.PROXIMA_NOVA_SEMIBOLD));
        enableBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AP.guideConfirmed();
                LauncherAnalytics.logEvent("ColorPhone_StartGuide_OK_Clicked");
                ModuleUtils.setAllModuleUserEnable();
                if (!GuideSetDefaultActivity.start(GuideAllFeaturesActivity.this)) {
                    requiresPermission();
                }

//                finish();
            }
        });

        TextView tvTitle = (TextView) findViewById(R.id.tv_title);
        tvTitle.setText(titleNew() ? R.string.guide_first_page_title : R.string.guide_first_page_title_old);

        GdprUtils.showGdprAlertIfNeeded(this);
    }


    private boolean titleNew() {
        String titleType = HSConfig.optString("new", "Application", "NotificationAccess", "FirstScreenTitle");
        if ("new".equalsIgnoreCase(titleType)) {
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {

        //Ignore back press.
    }

    private void setUpPrivacyTextView() {
        TextView privacyPolicy = (TextView) findViewById(R.id.welcome_guide_privacy_policy);
        privacyPolicy.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        privacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.startActivitySafely(GuideAllFeaturesActivity.this, new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.URL_PRIVACY)));
            }
        });

        TextView termsOfService = (TextView) findViewById(R.id.welcome_guide_terms_of_service);
        termsOfService.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        termsOfService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.startActivitySafely(GuideAllFeaturesActivity.this, new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.URL_TERM_SERVICES)));
            }
        });
    }

    public boolean requestForUsageAccess() {
        boolean needUsageAccess =
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                        && SmartAssistantUtils.isConfigEnabled()
                        && SmartAssistantUtils.gainUsageAccessOnFirstLaunch();

        if (needUsageAccess) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Utils.startActivitySafely(HSApplication.getContext(), intent);

            boolean needShowTip = SmartAssistantUtils.showUsageAccessTip();
            if (needShowTip) {
                String hintTxt = SmartAssistantUtils.getUsageAccessTipText();
                FloatWindowController.getInstance().createUsageAccessTip(HSApplication.getContext(), hintTxt);
            }

            LauncherAnalytics.logEvent("ColorPhone_SystemUsageAccessView_Show");
            HSUsageAccessMgr.getInstance().checkPermission(new HSUsageAccessMgr.PermissionListener() {
                @Override
                public void onPermissionChanged(boolean granted) {
                    if (granted) {
                        PermissionHelper.bringActivityToFront(ColorPhoneActivity.class, 0);
                        LauncherAnalytics.logEvent("ColorPhone_Usage_Access_Enabled");
                    }
                }
            });
        }

        return needUsageAccess;
    }

    /**
     * Only request first launch. (if Enabled and not has permission)
     */
    private void requiresPermission() {
        boolean isEnabled = ScreenFlashManager.getInstance().getAcbCallFactory().isConfigEnabled()
                && ScreenFlashSettings.isScreenFlashModuleEnabled();
        HSLog.d("ScreenFlash state change : " + isEnabled);
        if (!isEnabled) {
            HSLog.w("Permissions ScreenFlash state change : " + isEnabled);
            return;
        }

        boolean phonePerm = RuntimePermissions.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                == RuntimePermissions.PERMISSION_GRANTED;
        boolean contactPerm = RuntimePermissions.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                == RuntimePermissions.PERMISSION_GRANTED;
        if (!phonePerm) {
            LauncherAnalytics.logEvent("ColorPhone_Permission__Phone_SystemStyle_Show_FirstScreen");
        }
        if (!contactPerm) {
            LauncherAnalytics.logEvent("ColorPhone_Permission__Contact_SystemStyle_Show_FirstScreen");
        }
        if (!phonePerm || !contactPerm){
            // Do not have permissions, request them now
            RuntimePermissions.requestPermissions(this, perms, FIRST_LAUNCH_PERMISSION_REQUEST);
        }
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

//        if (PermissionHelper.requestDrawOverlayIfNeeded(EventSource.FirstScreen)) {
//            PermissionHelper.waitOverlayGranted(EventSource.FirstScreen, true);
//        } else {
        PermissionHelper.requestNotificationAccessIfNeeded(EventSource.FirstScreen, GuideAllFeaturesActivity.this);
//        }
        finish();

    }

    public void onPermissionsGranted(int requestCode, List<String> list) {
        if (requestCode == FIRST_LAUNCH_PERMISSION_REQUEST) {
            if (list.contains(Manifest.permission.READ_PHONE_STATE)) {
                LauncherAnalytics.logEvent("ColorPhone_Permission_Phone_SystemStyle_Allow_Click_FirstScreen");
            }
            if (list.contains(Manifest.permission.READ_CONTACTS)) {
                LauncherAnalytics.logEvent("ColorPhone_Permission_Contact_SystemStyle_Allow_Click_FirstScreen");

            }
        }
    }

    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Some permissions have been denied
        // ...
    }

}
