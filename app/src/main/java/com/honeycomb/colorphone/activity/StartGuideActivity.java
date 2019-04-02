package com.honeycomb.colorphone.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import com.acb.call.customize.ScreenFlashManager;
import com.acb.call.customize.ScreenFlashSettings;
import com.airbnb.lottie.LottieAnimationView;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.autopermission.AutoRequestManager;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.ModuleUtils;
import com.honeycomb.colorphone.util.StatusBarUtils;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.permission.Utils;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.RuntimePermissions;

/**
 * Created by sundxing on 17/9/13.
 */

public class StartGuideActivity extends HSAppCompatActivity {
    private static final String TAG = "AutoPermission";
    private static final int FIRST_LAUNCH_PERMISSION_REQUEST = 1000;

    private String[] perms = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS};
    private int permsCount = 0;

    Handler mHandler = new Handler(Looper.getMainLooper());

    public static void start(Context context) {
        Intent starter = new Intent(context, StartGuideActivity.class);
        context.startActivity(starter);
    }

    public static boolean isStarted() {
       return HSPreferenceHelper.getDefault().getBoolean("guide_locker_stated", false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HSPreferenceHelper.getDefault().putBoolean("guide_locker_stated", true);
        setContentView(R.layout.start_guide_all_features);
        StatusBarUtils.hideStatusBar(this);

        Analytics.logEvent("ColorPhone_StartGuide_Show");

        TextView enableBtn = findViewById(R.id.start_guide_function_enable_btn);
        enableBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(0xff852bf5, Dimensions.pxFromDp(24), true));
        enableBtn.setOnClickListener(v -> {
            Analytics.logEvent("ColorPhone_StartGuide_OK_Clicked");
            ModuleUtils.setAllModuleUserEnable();

            View view = findViewById(R.id.start_guide_function_page);
            view.setVisibility(View.GONE);

            view = findViewById(R.id.start_guide_permission_page);
            view.setVisibility(View.VISIBLE);

            loadingForPermission();
        });
    }

    @Override
    public void onBackPressed() {
        //Ignore back press.
        super.onBackPressed();
    }

    @Override protected void onStart() {
        super.onStart();
    }

    private void loadingForPermission() {
        LottieAnimationView animationView = findViewById(R.id.start_guide_permission_anim);
        animationView.useHardwareAcceleration();
        animationView.playAnimation();

        View view = findViewById(R.id.start_guide_permission_fetch_btn);
        view.setBackground(BackgroundDrawables.createBackgroundDrawable(0xff852bf5, Dimensions.pxFromDp(24), true));
        view.setOnClickListener(v -> {
            gotoAcc();
        });
    }

    private void gotoAcc() {
        HSLog.i("AutoPermission", "isAccessibilityGranted == " + Utils.isAccessibilityGranted());
        AutoRequestManager.getInstance().startAutoCheck();
    }

//    private void setUpPrivacyTextView() {
//        TextView privacyPolicy = (TextView) findViewById(R.id.welcome_guide_privacy_policy);
//        privacyPolicy.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
//        privacyPolicy.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Navigations.startActivitySafely(StartGuideActivity.this, new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.getUrlPrivacy())));
//            }
//        });
//
//        TextView termsOfService = (TextView) findViewById(R.id.welcome_guide_terms_of_service);
//        termsOfService.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
//        termsOfService.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Navigations.startActivitySafely(StartGuideActivity.this, new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.getUrlTermServices())));
//            }
//        });
//    }

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
            Analytics.logEvent("Permission_Phone_SystemStyle_Show_1st");
        }
        if (!contactPerm) {
            Analytics.logEvent("Permission_Contact_SystemStyle_Show_1st");
        }
        if (!phonePerm || !contactPerm){
            // Do not have permissions, request them now
            RuntimePermissions.requestPermissions(this, perms, FIRST_LAUNCH_PERMISSION_REQUEST);
            return true;
        }
        return false;
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        RuntimePermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
//
//        List<String> granted = new ArrayList<>(permissions.length);
//        List<String> denied = new ArrayList<>(permissions.length);
//
//        for(int i = 0; i < permissions.length; ++i) {
//            String perm = permissions[i];
//            if (grantResults[i] == 0) {
//                granted.add(perm);
//            } else {
//                denied.add(perm);
//            }
//        }
//
//        onPermissionsGranted(requestCode, granted);
//        onPermissionsDenied(requestCode, denied);
//
//        if (!ingoreNotificationPermission) {
//            if (RomUtils.checkIsMiuiRom() || RomUtils.checkIsVivoRom()) {
//                PermissionHelper.requestAutoStartIfNeeded(StartGuideActivity.this);
//            } else {
//                PermissionHelper.requestNotificationAccessIfNeeded(EventSource.FirstScreen, StartGuideActivity.this);
//            }
////            PermissionHelper.requestNotificationAccessIfNeeded(EventSource.FirstScreen, GuideAllFeaturesActivity.this);
//        }
////        }
//        finish();
//
//    }
//
//    public void onPermissionsGranted(int requestCode, List<String> list) {
//        if (requestCode == FIRST_LAUNCH_PERMISSION_REQUEST) {
//            if (list.contains(Manifest.permission.READ_PHONE_STATE)) {
//                Analytics.logEvent("Permission_Phone_SystemStyle_Allow_1st");
//                PermissionChecker.onPhonePermissionGranted();
//            }
//            if (list.contains(Manifest.permission.READ_CONTACTS)) {
//                Analytics.logEvent("Permission_Contact_SystemStyle_Allow_1st");
//                PermissionChecker.onContactPermissionGranted();
//            }
//        }
//    }
//
//    public void onPermissionsDenied(int requestCode, List<String> list) {
//        // Some permissions have been denied
//        // ...
//    }

}
