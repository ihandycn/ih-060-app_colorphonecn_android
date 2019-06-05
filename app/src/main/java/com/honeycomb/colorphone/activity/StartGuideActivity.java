package com.honeycomb.colorphone.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.autopermission.AutoLogger;
import com.honeycomb.colorphone.autopermission.AutoPermissionChecker;
import com.honeycomb.colorphone.autopermission.AutoRequestManager;
import com.honeycomb.colorphone.startguide.StartGuideViewHolder;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.ModuleUtils;
import com.honeycomb.colorphone.util.StatusBarUtils;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.config.HSConfig;
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
    public static final String ACC_KEY_SHOW_COUNT = "key_acc_permission_count";
    public static final String NOTIFICATION_PERMISSION_GRANT = "notification_permission_grant";
    public static final String PREF_KEY_GUIDE_SHOW_WHEN_WELCOME = "pref_key_guide_show_when_welcome";
    private static final String INTENT_KEY_FROM = "intent_key_from";
    public static final String FROM_KEY_GUIDE = "Guide";
    public static final String FROM_KEY_START = "Start";
    public static final String FROM_KEY_APPLY = "Apply";
    public static final String FROM_KEY_BANNER = "Banner";

    private String[] perms = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS};
    private int permsCount = 0;

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private StartGuideViewHolder holder;
    private AlertDialog dialog;
    private int permissionShowCount;
    private String from;

    public static @Nullable Intent getIntent(Context context, String from) {
        if (RomUtils.checkIsMiuiRom()
                || RomUtils.checkIsHuaweiRom()) {
            Intent starter = new Intent(context, StartGuideActivity.class);
            starter.putExtra(INTENT_KEY_FROM, from);
            return starter;
        } else {
            return null;
        }
    }

    public static void start(Context context, String from) {
        Intent intent = getIntent(context, from);
        if (intent != null) {
            Navigations.startActivitySafely(context, intent);
        }
    }

    public static boolean isStarted() {
        return Preferences.getDefault().contains(PREF_KEY_GUIDE_SHOW_WHEN_WELCOME);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_guide_all_features);
        StatusBarUtils.hideStatusBar(this);
        permissionShowCount = Preferences.get(Constants.DESKTOP_PREFS).getInt(ACC_KEY_SHOW_COUNT, 0);

        from = getIntent().getStringExtra(INTENT_KEY_FROM);
        if (TextUtils.isEmpty(from)) {
            from = FROM_KEY_START;
        }

        TextView enableBtn = findViewById(R.id.start_guide_function_enable_btn);
        if (Utils.isAccessibilityGranted() || isRetryEnd()) {
            HSLog.i("AutoPermission", "onPermissionChanged onCreate");
            onPermissionChanged();
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
                Analytics.logEvent("ColorPhone_StartGuide_Show");
            }
        }
        HSGlobalNotificationCenter.addObserver(AutoRequestManager.NOTIFY_PERMISSION_CHECK_FINISH_AND_CLOSE_WINDOW, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HSGlobalNotificationCenter.removeObserver(AutoRequestManager.NOTIFY_PERMISSION_CHECK_FINISH_AND_CLOSE_WINDOW, this);
    }

    private boolean isRetryEnd() {
        return permissionShowCount >= HSConfig.optInteger(1, "Application", "AutoPermission", "AccessibilityShowCount");
    }

    private void showAccessibilityPermissionPage() {
        View view = findViewById(R.id.start_guide_function_page);
        view.setVisibility(View.GONE);

        view = findViewById(R.id.start_guide_permission_page);
        view.setVisibility(View.VISIBLE);

        loadingForPermission();
    }

    private void onPermissionChanged() {
        if (AutoRequestManager.getInstance().isGrantAllPermission()) {
            HSLog.i("AutoPermission", "onPermissionChanged congratulation_page");

            View oldView = findViewById(R.id.start_guide_function_page);
            if (oldView.isShown()) {
                oldView.animate().alpha(0).setDuration(200).start();
            }
            oldView = findViewById(R.id.start_guide_confirm_page);
            if (oldView.isShown()) {
                oldView.animate().alpha(0).setDuration(200).start();
            }
            if (holder != null) {
                if (holder.isManualFix()) {
                    AutoLogger.logEventWithBrandAndOS("FixAlert_All_Granted");
                }
            }

            View newView = findViewById(R.id.start_guide_congratulation_page);

            if (newView.getVisibility() != View.VISIBLE) {
                Analytics.logEvent("Congratulation_Page_Shown_From_" + from);
                AutoLogger.logEventWithBrandAndOS("Congratulation_Page_Shown");
            }

            newView.setVisibility(View.VISIBLE);
            newView.setAlpha(0);
            newView.animate().alpha(1).setDuration(200).start();

            View view = newView.findViewById(R.id.start_guide_congratulation_circle_image);
            view.setPivotX(Dimensions.pxFromDp(70));
            view.setPivotY(Dimensions.pxFromDp(70));
            view.setScaleX(0);
            view.setScaleY(0);
            view.animate().scaleX(1).scaleY(1).setDuration(500).setInterpolator(new OvershootInterpolator(3)).start();

            view = newView.findViewById(R.id.start_guide_congratulation_center_image);
            view.setPivotX(Dimensions.pxFromDp(19));
            view.setPivotY(Dimensions.pxFromDp(8));
            view.setScaleX(0);
            view.setScaleY(0);
            view.animate().scaleX(1).scaleY(1).setDuration(500).setInterpolator(new OvershootInterpolator(3)).start();

            Threads.postOnMainThreadDelayed(this::finish, 2000);
        } else {
            HSLog.i("AutoPermission", "onPermissionChanged holder == " + holder);
            if (holder == null) {
                View view = findViewById(R.id.start_guide_function_page);
                view.setVisibility(View.GONE);

                view = findViewById(R.id.start_guide_confirm_page);
                view.setVisibility(View.VISIBLE);
                holder = new StartGuideViewHolder(view, true);
                holder.setCircleAnimView(R.id.start_guide_confirm_number);
                holder.startCircleAnimation();

                if (TextUtils.equals(from, FROM_KEY_GUIDE)) {
                    view.findViewById(R.id.start_guide_confirm_close).setVisibility(View.GONE);
                    View skip = view.findViewById(R.id.start_guide_confirm_skip);
                    skip.setVisibility(View.VISIBLE);
                    skip.setBackground(BackgroundDrawables.createBackgroundDrawable(0x0, Dimensions.pxFromDp(24), true));

                    skip.setOnClickListener(v -> {
                        finish();
                        Analytics.logEvent("FixAlert_Cancel_Click", "From", from);
                    });
                } else {
                    view.findViewById(R.id.start_guide_confirm_skip).setVisibility(View.GONE);
                    View close = view.findViewById(R.id.start_guide_confirm_close);
                    close.setVisibility(View.VISIBLE);
                    close.setBackground(BackgroundDrawables.createBackgroundDrawable(0x0, Dimensions.pxFromDp(24), true));

                    close.setOnClickListener(v -> {
                        showSkipDialog();
                        Analytics.logEvent("FixAlert_Cancel_Click", "From", from);
                    });
                }

                View oneKeyFix = view.findViewById(R.id.start_guide_confirm_fix);
                oneKeyFix.setBackground(BackgroundDrawables.createBackgroundDrawable(0xff852bf5, Dimensions.pxFromDp(24), true));

                oneKeyFix.setOnClickListener(v -> {
                    if (TextUtils.equals(from, FROM_KEY_GUIDE)) {
                        from = FROM_KEY_START;
                    }
                    Preferences.get(Constants.DESKTOP_PREFS).incrementAndGetInt(StartGuideActivity.ACC_KEY_SHOW_COUNT);
                    AutoRequestManager.getInstance().startAutoCheck(AutoRequestManager.AUTO_PERMISSION_FROM_FIX, from);

                    Analytics.logEvent("FixAlert_Ok_Click", "From", from);
                });


                Analytics.logEvent("FixAlert_Show",
                        "From", from,
                        "Brand", AutoLogger.getBrand(),
                        "Os", AutoLogger.getOSVersion(),
                        "AccessType", AutoLogger.getPermissionString(RomUtils.checkIsHuaweiRom()));
            } else {
                int confirmPermission = holder.refresh();
                showConfirmDialog(confirmPermission);
            }
        }
    }

    private void showSkipDialog() {
        if (dialog != null) {
            dialog.dismiss();
        }

        View view = getLayoutInflater().inflate(R.layout.layout_skip_confirm_dialog, null);

        View bgView = view.findViewById(R.id.skip_layout);
        bgView.setBackground(BackgroundDrawables.createBackgroundDrawable(0xffffffff, Dimensions.pxFromDp(16), false));
        View btn = view.findViewById(R.id.tv_ok);
        btn.setBackground(BackgroundDrawables.createBackgroundDrawable(0xff6c63ff, Dimensions.pxFromDp(26), true));
        btn.setOnClickListener(v -> {
            dismissDialog();
            Preferences.get(Constants.DESKTOP_PREFS).incrementAndGetInt(StartGuideActivity.ACC_KEY_SHOW_COUNT);
            AutoRequestManager.getInstance().startAutoCheck(AutoRequestManager.AUTO_PERMISSION_FROM_FIX, from);

            Analytics.logEvent("FixAlert_Retain_Ok_Click", "From", from);
        });

        btn = view.findViewById(R.id.tv_turn_off);
        btn.setOnClickListener(v -> {
            dismissDialog();
            finish();
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog);
        builder.setCancelable(false);
        builder.setView(view);
        dialog = builder.create();

        showDialog(dialog);

        Analytics.logEvent("FixAlert_Retain_Show", "From", from);
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

                Analytics.logEvent("FixAlert_AutoStart_Granted");
                if (AutoRequestManager.getInstance().isGrantAllPermission()) {
                    AutoLogger.logEventWithBrandAndOS("FixAlert_All_Granted");
                }
                onPermissionChanged();
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

                Analytics.logEvent("FixALert_Lock_Granted");
                if (AutoRequestManager.getInstance().isGrantAllPermission()) {
                    AutoLogger.logEventWithBrandAndOS("FixAlert_All_Granted");
                }
                onPermissionChanged();
            });

            builder.setNegativeButton(com.acb.call.R.string.no, (dialog, which) -> Analytics.logEvent("LockScreenAlert_No_Click"));
            Analytics.logEvent("LockScreenAlert_Show");

        } else if (confirmPermission == StartGuideViewHolder.TYPE_PERMISSION_TYPE_BG_POP) {
            builder.setTitle(R.string.acb_request_permission_bg_pop_title);
            builder.setMessage(R.string.acb_request_permission_bg_pop_content);
            builder.setPositiveButton(com.acb.call.R.string.yes, (dialog, which) -> {
                AutoPermissionChecker.onBgPopupChange(true);
                Analytics.logEvent("BackgroundPopupAlert_Yes_Click");

                Analytics.logEvent("FixALert_BackgroundPopup_Granted");
                if (AutoRequestManager.getInstance().isGrantAllPermission()) {
                    AutoLogger.logEventWithBrandAndOS("FixAlert_All_Granted");
                }
                onPermissionChanged();
            });

            builder.setNegativeButton(com.acb.call.R.string.no, (dialog, which) -> Analytics.logEvent("BackgroundPopupAlert_No_Click"));
            Analytics.logEvent("BackgroundPopupAlert_Show");

        } else {
            if (confirmPermission == StartGuideViewHolder.TYPE_PERMISSION_TYPE_CALL) {
                if (AutoPermissionChecker.isNotificationListeningGranted()) {
                    AutoLogger.logEventWithBrandAndOS("FixALert_NA_Granted");
                }
            }
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
        boolean needRefreshView = (Utils.isAccessibilityGranted() || isRetryEnd())
                && !AutoRequestManager.getInstance().isRequestPermission();
        if (needRefreshView) {
            HSLog.i("AutoPermission", "onPermissionChanged onStart");
            onPermissionChanged();
        }
    }

    @Override protected void onStop() {
        super.onStop();
        HSGlobalNotificationCenter.removeObserver(this::onReceive);
    }

    @Override public void onReceive(String s, HSBundle hsBundle) {
        if (TextUtils.equals(AutoRequestManager.NOTIFY_PERMISSION_CHECK_FINISH_AND_CLOSE_WINDOW, s)) {
            HSLog.i("AutoPermission", "onPermissionChanged onReceive");
            onPermissionChanged();
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
            permissionShowCount = Preferences.get(Constants.DESKTOP_PREFS).incrementAndGetInt(ACC_KEY_SHOW_COUNT);
            Analytics.logEvent("Accessbility_Guide_Btn_Click",
                    "Brand", AutoLogger.getBrand(),
                    "Os", AutoLogger.getOSVersion(),
                    "Time", String.valueOf(permissionShowCount));

            Preferences.getDefault().putBoolean(PREF_KEY_GUIDE_SHOW_WHEN_WELCOME, true);
        });

        handler.postDelayed(() -> {
            title.setText(R.string.start_guide_request_accessibility_title);
            title.animate().alpha(0.8f).setDuration(750).start();
            button.animate().alpha(1f).setDuration(750).start();
        }, 2400);
        AutoLogger.logEventWithBrandAndOS("Accessbility_Guide_Show");
    }

    private void gotoAcc() {
        HSLog.i("AutoPermission", "isAccessibilityGranted == " + Utils.isAccessibilityGranted());
        AutoRequestManager.getInstance().startAutoCheck(AutoRequestManager.AUTO_PERMISSION_FROM_AUTO, from);
    }
}
