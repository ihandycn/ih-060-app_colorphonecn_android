package com.honeycomb.colorphone.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.honeycomb.colorphone.R;
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
import com.superapps.util.Threads;

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

    public static void start(Context context) {
        Intent starter = new Intent(context, StartGuideActivity.class);
        starter.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
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
            onPermissionGranted();
        } else {
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
    }

    private void onPermissionGranted() {
        HSLog.i("AutoPermission", "onPermissionGranted ");
        if (AutoPermissionChecker.hasAutoStartPermission()
                && AutoPermissionChecker.hasShowOnLockScreenPermission()
                && Utils.isNotificationListeningGranted()) {

            View oldView = findViewById(R.id.start_guide_function_page);
            oldView.animate().alpha(0).setDuration(200).start();

            View newView = findViewById(R.id.start_guide_congratulation_page);
            newView.setVisibility(View.VISIBLE);
            newView.setAlpha(0);
            newView.animate().alpha(1).setDuration(200).start();

            Threads.postOnMainThreadDelayed(this::finish, 3200);
        } else {
            if (holder == null) {
                View view = findViewById(R.id.start_guide_function_page);
                view.setVisibility(View.GONE);

                view = findViewById(R.id.start_guide_confirm_page);
                view.setVisibility(View.VISIBLE);
                holder = new StartGuideViewHolder(view, true);
                holder.setCircleAnimView(R.id.start_guide_confirm_number);
                holder.startCircleAnimation();
            } else {
                holder.refresh();
            }
        }
    }

    @Override
    public void onBackPressed() {
        //Ignore back press.
        super.onBackPressed();
    }

    @Override protected void onStart() {
        super.onStart();
        if (Utils.isAccessibilityGranted()) {
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
            onPermissionGranted();
        }
    }

    private void loadingForPermission() {
        LottieAnimationView animationView = findViewById(R.id.start_guide_permission_anim);
        animationView.useHardwareAcceleration();
        animationView.playAnimation();

        TextView title = findViewById(R.id.start_guide_permission_title);
        title.setAlpha(0);
        title.animate().alpha(0.51f).setDuration(33).start();
        Handler handler = new Handler();
        handler.postDelayed(() -> title.animate().alpha(0f).setDuration(250).start(), 2150);

        View button = findViewById(R.id.start_guide_permission_fetch_btn);
        button.setAlpha(0);
        button.setBackground(BackgroundDrawables.createBackgroundDrawable(0xff852bf5, Dimensions.pxFromDp(24), true));
        button.setOnClickListener(v -> {
            gotoAcc();
        });

        handler.postDelayed(() -> {
            title.setText(R.string.start_guide_request_accessibility_title);
            title.animate().alpha(0.8f).setDuration(750).start();
            button.animate().alpha(1f).setDuration(750).start();
        }, 2400);
    }

    private void gotoAcc() {
        HSLog.i("AutoPermission", "isAccessibilityGranted == " + Utils.isAccessibilityGranted());
        AutoRequestManager.getInstance().startAutoCheck();
    }
}
