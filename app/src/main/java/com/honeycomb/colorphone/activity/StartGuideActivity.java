package com.honeycomb.colorphone.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.autopermission.AutoLogger;
import com.honeycomb.colorphone.autopermission.AutoPermissionChecker;
import com.honeycomb.colorphone.autopermission.AutoRequestManager;
import com.honeycomb.colorphone.startguide.StartGuideViewHolder;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.ModuleUtils;
import com.honeycomb.colorphone.util.StatusBarUtils;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.permission.Utils;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;
import com.superapps.util.rom.RomUtils;

/**
 * Created by sundxing on 17/9/13.
 */

public class StartGuideActivity extends HSAppCompatActivity implements INotificationObserver {
    private static final String TAG = "AutoPermission";
    private static final int FIRST_LAUNCH_PERMISSION_REQUEST = 1000;

    private String[] perms = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS};
    private int permsCount = 0;

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private StartGuideViewHolder holder;
    private AlertDialog dialog;

    public static void start(Context context) {
        Intent starter = new Intent(context, StartGuideActivity.class);
        Navigations.startActivitySafely(context, starter);
    }

    public static boolean isStarted() {
        return false;
//       return HSPreferenceHelper.getDefault().getBoolean("guide_locker_stated", false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        HSPreferenceHelper.getDefault().putBoolean("guide_locker_stated", true);
        setContentView(R.layout.start_guide_all_features);
        StatusBarUtils.hideStatusBar(this);

        Analytics.logEvent("ColorPhone_StartGuide_Show");

        TextView enableBtn = findViewById(R.id.start_guide_function_enable_btn);
        if (Utils.isAccessibilityGranted()) {
            HSLog.i("AutoPermission", "onPermissionGranted onCreate");
            onPermissionGranted();
        } else {
            if (ModuleUtils.isAllModuleEnabled()) {
                showAccessibilityPermissionPage();
            } else {
                enableBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(0xff852bf5, Dimensions.pxFromDp(24), true));
                enableBtn.setOnClickListener(v -> {
                    Analytics.logEvent("ColorPhone_StartGuide_OK_Clicked");
                    ModuleUtils.setAllModuleUserEnable();
                    showAccessibilityPermissionPage();
                });
            }
        }
    }

    private void showAccessibilityPermissionPage() {
        View view = findViewById(R.id.start_guide_function_page);
        view.setVisibility(View.GONE);

        view = findViewById(R.id.start_guide_permission_page);
        view.setVisibility(View.VISIBLE);

        loadingForPermission();
    }

    private void onPermissionGranted() {
        if (AutoRequestManager.getInstance().isGrantAllPermission()) {
            HSLog.i("AutoPermission", "onPermissionGranted congratulation_page");

            View oldView = findViewById(R.id.start_guide_function_page);
            if (oldView.isShown()) {
                oldView.animate().alpha(0).setDuration(200).start();
            }

            View newView = findViewById(R.id.start_guide_congratulation_page);
            newView.setVisibility(View.VISIBLE);
            newView.setAlpha(0);
            newView.animate().alpha(1).setDuration(200).start();

            View view = newView.findViewById(R.id.start_guide_congratulation_circle_image);
            view.setPivotX(Dimensions.pxFromDp(70));
            view.setPivotY(Dimensions.pxFromDp(70));
            view.setScaleX(0);
            view.setScaleY(0);
            view.animate().scaleX(1).scaleY(1).setDuration(500).setInterpolator(new OvershootInterpolator()).start();

            view = newView.findViewById(R.id.start_guide_congratulation_center_image);
            view.setPivotX(Dimensions.pxFromDp(19));
            view.setPivotY(Dimensions.pxFromDp(8));
            view.setScaleX(0);
            view.setScaleY(0);
            view.animate().scaleX(1).scaleY(1).setDuration(500).setInterpolator(new OvershootInterpolator()).start();

            Threads.postOnMainThreadDelayed(this::finish, 2500);
        } else {
            HSLog.i("AutoPermission", "onPermissionGranted holder == " + holder);
            if (holder == null) {
                View view = findViewById(R.id.start_guide_function_page);
                view.setVisibility(View.GONE);

                view = findViewById(R.id.start_guide_confirm_page);
                view.setVisibility(View.VISIBLE);
                holder = new StartGuideViewHolder(view, true);
                holder.setCircleAnimView(R.id.start_guide_confirm_number);
                holder.startCircleAnimation();

                Analytics.logEvent("FixAlert_Show", "AccessType", AutoLogger.getPermissionString(RomUtils.checkIsHuaweiRom()));
            } else {
                int confirmPermission = holder.refresh();
                showConfirmDialog(confirmPermission);
            }
        }
    }

    private void showConfirmDialog(int confirmPermission) {
        if (dialog != null) {
            dialog.dismiss();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this, com.acb.call.R.style.Theme_AppCompat_Light_Dialog);
        builder.setCancelable(false);
        if (confirmPermission == StartGuideViewHolder.TYPE_PERMISSION_TYPE_SCREEN_FLASH) {
            if (RomUtils.checkIsVivoRom()) {
                builder.setTitle(com.acb.call.R.string.acb_request_permission_white_title_vivo);
                builder.setMessage(com.acb.call.R.string.acb_request_permission_white_content_vivo);
            } else {
                builder.setTitle(com.acb.call.R.string.acb_request_permission_auto_start_title);
                builder.setMessage(com.acb.call.R.string.acb_request_permission_auto_start_content);
            }
            builder.setPositiveButton(com.acb.call.R.string.yes, (dialog, which) -> {
                AutoPermissionChecker.onAutoStartChange(true);
                Preferences.getDefault().putBoolean("pref_key_permission_auto_start_grant", true);
                Analytics.logEvent("AutoStartAlert_Yes_Click");

                holder.refresh();
            });

            builder.setNegativeButton(com.acb.call.R.string.no, (dialog, which) -> Analytics.logEvent("AutoStartAlert_No_Click"));
            Analytics.logEvent("AutoStartAlert_Show");

        } else if (confirmPermission == StartGuideViewHolder.TYPE_PERMISSION_TYPE_ON_LOCK) {
            builder.setTitle(com.acb.call.R.string.acb_request_permission_show_on_lockscreen_title);
            builder.setMessage(com.acb.call.R.string.acb_request_permission_show_on_lockscreen_content);
            builder.setPositiveButton(com.acb.call.R.string.yes, (dialog, which) -> {
                AutoPermissionChecker.onShowOnLockScreenChange(true);
                Preferences.getDefault().putBoolean("pref_key_permission_show_on_lock_screen_grant", true);
                Analytics.logEvent("LockScreenAlert_Yes_Click");

                holder.refresh();
            });

            builder.setNegativeButton(com.acb.call.R.string.no, (dialog, which) -> Analytics.logEvent("LockScreenAlert_No_Click"));
            Analytics.logEvent("LockScreenAlert_Show");

        } else {
            return;
        }

        dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        //Ignore back press.
    }

    @Override protected void onStart() {
        super.onStart();
        if (Utils.isAccessibilityGranted() && !AutoRequestManager.getInstance().isRequestPermission()) {
            HSLog.i("AutoPermission", "onPermissionGranted onStart");
            onPermissionGranted();
        }

        HSGlobalNotificationCenter.addObserver(StartGuideViewHolder.ALL_PERMISSION_GRANT, this);
    }

    @Override protected void onStop() {
        super.onStop();
        HSGlobalNotificationCenter.removeObserver(this::onReceive);
    }

    @Override public void onReceive(String s, HSBundle hsBundle) {
        if (TextUtils.equals(StartGuideViewHolder.ALL_PERMISSION_GRANT, s)) {
            HSLog.i("AutoPermission", "onPermissionGranted onReceive");
            onPermissionGranted();
        }
    }

    private void loadingForPermission() {
        LottieAnimationView animationView = findViewById(R.id.start_guide_permission_anim);
        animationView.useHardwareAcceleration();
        animationView.playAnimation();

        TextView title = findViewById(R.id.start_guide_permission_title);
        title.setAlpha(0);
        title.animate().alpha(0.5f).setDuration(33).start();
        Handler handler = new Handler();
        handler.postDelayed(() -> title.animate().alpha(0f).setDuration(250).start(), 2150);

        View button = findViewById(R.id.start_guide_permission_fetch_btn);
        button.setAlpha(0);
        button.setBackground(BackgroundDrawables.createBackgroundDrawable(0xff852bf5, Dimensions.pxFromDp(24), true));
        button.setOnClickListener(v -> {
            gotoAcc();
            AutoLogger.logEventWithBrandAndOS("Accessbility_Guide_Btn_Click");
        });

        handler.postDelayed(() -> {
            title.setText(R.string.start_guide_request_accessibility_title);
            title.animate().alpha(0.8f).setDuration(750).start();
            button.animate().alpha(1f).setDuration(750).start();
        }, 2400);
        Analytics.logEvent("Accessbility_Guide_Show");
    }

    private void gotoAcc() {
        HSLog.i("AutoPermission", "isAccessibilityGranted == " + Utils.isAccessibilityGranted());
        AutoRequestManager.getInstance().startAutoCheck(AutoRequestManager.AUTO_PERMISSION_FROM_AUTO);
    }
}
