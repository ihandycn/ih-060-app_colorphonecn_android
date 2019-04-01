package com.honeycomb.colorphone.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.acb.call.customize.ScreenFlashManager;
import com.acb.call.customize.ScreenFlashSettings;
import com.airbnb.lottie.LottieAnimationView;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.ModuleUtils;
import com.honeycomb.colorphone.util.StatusBarUtils;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.permission.HSPermissionRequestCallback;
import com.ihs.permission.HSPermissionRequestMgr;
import com.ihs.permission.HSPermissionType;
import com.ihs.permission.Utils;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;
import com.superapps.util.RuntimePermissions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sundxing on 17/9/13.
 */

public class StartGuideActivity extends HSAppCompatActivity {
    private static final String TAG = "AutoPermission";
    private static final int FIRST_LAUNCH_PERMISSION_REQUEST = 1000;

    private String[] perms = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS};
    private int permsCount = 0;

    Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean ingoreNotificationPermission;
    private boolean requstAutoStart = false;

    private Runnable checkAccessibilityPermission = new Runnable() {
        int count = 0;

        @Override public void run() {
            count++;
            HSLog.i("AutoPermission", "AccessibilityGranted == " + Utils.isAccessibilityGranted() + "   count == " + count);
            if (Utils.isAccessibilityGranted()) {
                count = 100;
                mHandler.removeCallbacks(checkAccessibilityPermission);
                if (com.honeycomb.colorphone.util.Utils.isFloatWindowAllowed(StartGuideActivity.this)) {
                    HSLog.i("AutoPermission", "has TYPE_DRAW_OVERLAY ");
                    finish();
                } else {
                    HSLog.i("AutoPermission", "request TYPE_DRAW_OVERLAY ");
                    List<HSPermissionType> permissionTypes = new ArrayList<>();
                    permissionTypes.add(HSPermissionType.TYPE_DRAW_OVERLAY);
                    HSPermissionType mSelectedPermission = HSPermissionType.TYPE_DRAW_OVERLAY;

                    /**
                     * 调用startRequest接口。
                     */
                    HSPermissionRequestMgr.getInstance().startRequest(permissionTypes, new HSPermissionRequestCallback.Stub() {

                        @Override
                        public void onStarted() {
                            super.onStarted();
                            HSLog.i(TAG, "permission request started");
                        }

                        @Override
                        public void onFinished(int succeedCount, int totalCount) {
                            HSLog.i(TAG, "permission request finished, succeeded " + succeedCount + " , total " + totalCount);
//                mWm.removeView(guideView);
                            if (succeedCount > 0) {
                                Toast.makeText(getApplicationContext(), mSelectedPermission + "  succeeded", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getApplicationContext(), mSelectedPermission + "  failed", Toast.LENGTH_LONG).show();
                            }
                        }



                        @Override
                        public void onCancelled() {
                            super.onCancelled();
                            HSLog.i(TAG, "permission request cancelled");
//                mWm.removeView(guideView);
                        }

                        @Override
                        public void onSinglePermissionStarted(int index) {
                            HSLog.i(TAG, "permission request index " + index + " started");
                        }

                        @Override
                        public void onSinglePermissionFinished(int index, boolean isSucceed, String msg) {
                            HSLog.i(TAG, "permission request index " + index + " finished, result " + isSucceed + "  errmsg: " + msg);
                        }

                        /**
                         * 获取DeviceAdmin权限必须实现的回调方法。
                         * 把自己的管理员广播接收者MyAdmin放到Intent里，并取消FLAG_ACTIVITY_NEW_TASK。
                         *
                         * @param intent 库里回传的开启Device Admin权限的Intent。
                         */
                        @Override
                        public void onDeviceAdminAction(Intent intent) {
                            if (intent != null) {
//                                intent.setFlags(intent.getFlags() & ~Intent.FLAG_ACTIVITY_NEW_TASK);
//                                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, new ComponentName(MainActivity.this, MyAdmin.class));
//                                startActivity(intent);
                            }
                        }
                    });
                }
            } else {
                if (count >= 100) {
                    count = 0;
                }

                if (count < 30) {
                    mHandler.postDelayed(checkAccessibilityPermission, 1000);
                }
            }
        }
    };

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
        if (!Utils.isAccessibilityGranted()) {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

            Navigations.startActivitySafely(StartGuideActivity.this, intent);
            mHandler.postDelayed(checkAccessibilityPermission, 1000);
        }
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
