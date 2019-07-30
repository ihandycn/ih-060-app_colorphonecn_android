package com.honeycomb.colorphone.activity;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.acb.call.customize.ScreenFlashManager;
import com.acb.call.customize.ScreenFlashSettings;
import com.acb.call.service.InCallWindow;
import com.acb.colorphone.permissions.FloatWindowManager;
import com.acb.colorphone.permissions.WriteSettingsPopupGuideActivity;
import com.airbnb.lottie.LottieAnimationView;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.dialog.FiveStarRateTip;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.ModuleUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Navigations;
import com.superapps.util.RuntimePermissions;
import com.superapps.util.Threads;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sundxing on 17/11/22.
 */

public class TestActivity extends AppCompatActivity {
    private static int FIRST_LAUNCH_PERMISSION_REQUEST = 1110;
    private static int PERMISSION_REQUEST = 1111;

    Handler mHandler = new Handler();
    EditText editText;
    LottieAnimationView mLottieAnimationView;
    InCallWindow mInCallWindow;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        editText = findViewById(R.id.test_number);
        mLottieAnimationView = findViewById(R.id.lottie_anim);
        mLottieAnimationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLottieAnimationView.playAnimation();
            }
        });
        final LottieAnimationView lottieAnimationView2 = findViewById(R.id.lottie_anim_2);
        lottieAnimationView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lottieAnimationView2.playAnimation();
            }
        });
        mInCallWindow = new InCallWindow(this);

    }

    public void startCallRingingWindow(View view) {
        String number = editText.getText().toString().trim();
        mInCallWindow.show(TextUtils.isEmpty(number) ? "8888888" : number);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mInCallWindow.endFlashCall();
            }
        }, 8000);

    }

    @Override
    protected void onStop() {
        mHandler.removeCallbacksAndMessages(null);
        super.onStop();
    }

    public void startRate(View view) {
        RateAlertActivity.showRateFrom(this, FiveStarRateTip.From.END_CALL);
    }

    public void startRecentApp(View view) {
    }

    public void checkFloatWindow(View view) {
        FloatWindowManager.getInstance().applyOrShowFloatWindow(this);
        requiresPermission();

    }

    /**
     * Only request first launch. (if Enabled and not has permission)
     */
    private void requiresPermission() {
        boolean isEnabled = ScreenFlashManager.getInstance().getAcbCallFactory().isConfigEnabled()
                && ScreenFlashSettings.isScreenFlashModuleEnabled();
        HSLog.i("Permissions ScreenFlash state change : " + isEnabled);
        if (!isEnabled) {
            return;
        }

        List<String> permissions = new ArrayList<>();
//        permissions.add(Manifest.permission.READ_PHONE_STATE);
//        permissions.add(Manifest.permission.READ_CONTACTS);
//        permissions.add(Manifest.permission.WRITE_CONTACTS);
//        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
//        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.WRITE_SETTINGS);

        boolean grantPermission = true;
        boolean grant;
        for (String p : permissions) {
            grant = RuntimePermissions.checkSelfPermission(this, p)
                    == RuntimePermissions.PERMISSION_GRANTED;
            grantPermission &= grant;

            if (!grant) {
                switch (p) {
                    case Manifest.permission.READ_PHONE_STATE:
                        Analytics.logEvent("Permission_Phone_View_Showed");
                        break;
                    case Manifest.permission.READ_CONTACTS:
                        Analytics.logEvent("Permission_Contact_View_Showed");
                        break;
                    case Manifest.permission.WRITE_CONTACTS:
                        Analytics.logEvent("Permission_Write_Contact_View_Showed");
                        break;
                    case Manifest.permission.READ_EXTERNAL_STORAGE:
                        Analytics.logEvent("Permission_Read_Storage_View_Showed");
                        break;
                    case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                        Analytics.logEvent("Permission_Write_Storage_View_Showed");
                        break;
                    case Manifest.permission.WRITE_SETTINGS:
                        Analytics.logEvent("Permission_Write_Settings_View_Showed");
                        break;

                }
            }
        }

        if (!grantPermission) {
            // Do not have permissions, request them now
            RuntimePermissions.requestPermissions(this, permissions.toArray(new String[0]), FIRST_LAUNCH_PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        RuntimePermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);

        List<String> granted = new ArrayList<>();
        List<String> denied = new ArrayList<>();

        for (int i = 0; i < permissions.length; ++i) {
            String perm = permissions[i];
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
        HSLog.i("Permission", "Test onPermissionsGranted: " + list);
        if (requestCode == FIRST_LAUNCH_PERMISSION_REQUEST) {
            for (String p : list) {
                switch (p) {
                    case Manifest.permission.READ_PHONE_STATE:
                        Analytics.logEvent("Permission_Phone_Allow_Success");
                        break;
                    case Manifest.permission.READ_CONTACTS:
                        Analytics.logEvent("Permission_Contact_Allow_Success");
                        break;
                    case Manifest.permission.WRITE_CONTACTS:
                        Analytics.logEvent("Permission_Write_Contact_Allow_Success");
                        break;
                    case Manifest.permission.READ_EXTERNAL_STORAGE:
                        Analytics.logEvent("Permission_Read_Storage_Allow_Success");
                        break;
                    case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                        Analytics.logEvent("Permission_Write_Storage_Allow_Success");
                        break;
                    case Manifest.permission.WRITE_SETTINGS:
                        Analytics.logEvent("Permission_Write_Settings_Allow_Success");
                        break;

                }
            }

            ModuleUtils.setAllModuleUserEnable();
        } else if (requestCode == PERMISSION_REQUEST) {
            for (String p : list) {
                switch (p) {
                    case Manifest.permission.READ_PHONE_STATE:
                        break;
                    case Manifest.permission.WRITE_SETTINGS:
                        break;
                    case Manifest.permission.READ_CONTACTS:
                        break;
                    case Manifest.permission.WRITE_CONTACTS:
                        break;
                    case Manifest.permission.READ_EXTERNAL_STORAGE:
                        break;
                    case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                        break;
                }
            }
        }
    }

    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Some permissions have been denied
        // ...

        HSLog.i("Permission", "Test onPermissionsDenied: " + list);

        for (String p : list) {
            switch (p) {
                case Manifest.permission.READ_PHONE_STATE:
//                    AutoRequestManager.getInstance().openPermission(HSPermissionRequestMgr.TYPE_PHONE);
                    break;
                case Manifest.permission.WRITE_SETTINGS:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        Navigations.startActivitySafely(this, intent);

                        Threads.postOnMainThreadDelayed(() -> {
                            Navigations.startActivitySafely(HSApplication.getContext(), WriteSettingsPopupGuideActivity.class);
                        }, 900);
                    }
                    break;
                case Manifest.permission.READ_CONTACTS:
                    break;
                case Manifest.permission.WRITE_CONTACTS:
                    break;
                case Manifest.permission.READ_EXTERNAL_STORAGE:
                    break;
                case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                    break;
            }
        }
    }

}
