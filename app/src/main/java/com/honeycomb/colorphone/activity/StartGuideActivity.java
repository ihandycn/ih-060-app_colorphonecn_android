package com.honeycomb.colorphone.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.acb.call.customize.ScreenFlashManager;
import com.acb.call.customize.ScreenFlashSettings;
import com.airbnb.lottie.LottieAnimationView;
import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.autopermission.AutoLogger;
import com.honeycomb.colorphone.autopermission.AutoPermissionChecker;
import com.honeycomb.colorphone.autopermission.AutoRequestManager;
import com.honeycomb.colorphone.startguide.StartGuidePermissionFactory;
import com.honeycomb.colorphone.startguide.StartGuideViewListHolder;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.ModuleUtils;
import com.honeycomb.colorphone.util.StatusBarUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.permission.HSPermissionRequestMgr;
import com.ihs.permission.Utils;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;
import com.superapps.util.Preferences;
import com.superapps.util.RuntimePermissions;
import com.superapps.util.Threads;
import com.superapps.util.rom.RomUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sundxing on 17/9/13.
 */

public class StartGuideActivity extends HSAppCompatActivity implements INotificationObserver {
    private static final String TAG = "AutoPermission";
    private static final int FIRST_LAUNCH_PERMISSION_REQUEST = 1000;
    public static final int CONFIRM_PAGE_PERMISSION_REQUEST = 2000;
    private static final int AUTO_PERMISSION_REQUEST = 3000;
    public static final String ACC_KEY_SHOW_COUNT = "key_acc_permission_count";
    public static final String NOTIFICATION_PERMISSION_GRANT = "notification_permission_grant";
    public static final String PREF_KEY_GUIDE_SHOW_WHEN_WELCOME = "pref_key_guide_show_when_welcome";
    private static final String INTENT_KEY_FROM = "intent_key_from";
    public static final String INTENT_KEY_PERMISSION_TYPE = "intent_key_permission_type";
    public static final String FROM_KEY_GUIDE = "Guide";
    public static final String FROM_KEY_START = "Start";
    public static final String FROM_KEY_APPLY = "Apply";
    public static final String FROM_KEY_BANNER = "Banner";

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private StartGuideViewListHolder holder;
    private AlertDialog dialog;
    private int permissionShowCount;
    private String from;
    private boolean directPermission;
    private boolean oneKeyFixPressed = false;
    private int confirmDialogPermission = 0;
    private boolean isAgreePrivacy;

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
        directPermission = HSConfig.optBoolean(true, "Application", "GrantAccess", "RequestOnStartGuide");

        from = getIntent().getStringExtra(INTENT_KEY_FROM);
        if (TextUtils.isEmpty(from)) {
            from = FROM_KEY_START;
        }

        TextView enableBtn = findViewById(R.id.start_guide_function_enable_btn);
        CheckBox agree = findViewById(R.id.start_guide_check);
        isAgreePrivacy = agree.isChecked();

        agree.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked != isAgreePrivacy) {
                isAgreePrivacy = isChecked;
                Analytics.logEvent(isChecked ? "StartGuide_Privacy_Agree_Click" : "StartGuide_Privacy_Refuse_Click");
            }
        });

        if (Utils.isAccessibilityGranted() || isRetryEnd()) {
            HSLog.i(TAG, "onPermissionChanged onCreate");
            onPermissionChanged();
        } else {
            HSLog.i(TAG, " onCreate");
            if (ModuleUtils.isAllModuleEnabled()) {
                showAccessibilityPermissionPage();
            } else {
                enableBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(0xff852bf5, Dimensions.pxFromDp(24), true));
                enableBtn.setOnClickListener(v -> {
                    if (agree.isChecked()) {
                        if (directPermission) {
                            showPermissionDialog();
                        }

                        if (!directPermission){
                            ModuleUtils.setAllModuleUserEnable();
                            showAccessibilityPermissionPage();
                        }
                    } else {
                        showToast();
                    }

                    Analytics.logEvent("ColorPhone_StartGuide_OK_Clicked");
                });
                Analytics.logEvent("ColorPhone_StartGuide_Show");
            }
        }
        HSGlobalNotificationCenter.addObserver(AutoRequestManager.NOTIFY_PERMISSION_CHECK_FINISH_AND_CLOSE_WINDOW, this);

        setUpPrivacyTextView();
    }

    boolean isOnNewIntent = false;
    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        HSLog.i(TAG, "onNewIntent");
        isOnNewIntent = true;
    }

    private void setUpPrivacyTextView() {
        final String privacyPolicyStr = Constants.getUrlPrivacy();
        if (!TextUtils.isEmpty(privacyPolicyStr)) {
            TextView privacyPolicy = findViewById(R.id.start_guide_policy);
            if (privacyPolicy != null) {
                privacyPolicy.setOnClickListener(v -> Navigations.startActivitySafely(StartGuideActivity.this, new Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyStr))));
            }
        }

        final String termServiceStr = Constants.getUrlTermServices();
        if (!TextUtils.isEmpty(termServiceStr)) {
            TextView termsOfService = findViewById(R.id.start_guide_service);
            if (termsOfService != null) {
                termsOfService.setOnClickListener(v ->  Navigations.startActivitySafely(StartGuideActivity.this, new Intent(Intent.ACTION_VIEW, Uri.parse(termServiceStr))));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HSGlobalNotificationCenter.removeObserver(AutoRequestManager.NOTIFY_PERMISSION_CHECK_FINISH_AND_CLOSE_WINDOW, this);
    }

    private boolean isRetryEnd() {
        return permissionShowCount >= HSConfig.optInteger(1, "Application", TAG, "AccessibilityShowCount");
    }

    private boolean canShowSkip() {
        return (permissionShowCount >= HSConfig.optInteger(3, "Application", TAG, "SkipShowCount")
                    && !AutoRequestManager.getInstance().isGrantAllPermission())
                || (AutoRequestManager.getInstance().isGrantAllWithoutNAPermission()
                    && HSConfig.optBoolean(false, "Application", "AutoPermission", "AutoSkipWhenNAGranted"));
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
            HSLog.i(TAG, "onPermissionChanged congratulation_page");

            View oldView = findViewById(R.id.start_guide_function_page);
            if (oldView.isShown()) {
                oldView.animate().alpha(0).setDuration(200).start();
            }
            oldView = findViewById(R.id.start_guide_confirm_page);
            if (oldView.isShown()) {
                oldView.animate().alpha(0).setDuration(200).start();
            }
            if (holder != null) {
                if (holder.isManualFix() && !oneKeyFixPressed) {
                    AutoLogger.logEventWithBrandAndOS("FixAlert_All_Granted");
                }
            }

            View newView = findViewById(R.id.start_guide_congratulation_page);

            if (newView.getVisibility() != View.VISIBLE) {
                Analytics.logEvent("Congratulation_Page_Shown_From_" + from);

                Analytics.logEvent("Congratulation_Page_Shown",
                        "Brand", AutoLogger.getBrand(),
                        "Os", AutoLogger.getOSVersion(),
                        "Permission", AutoLogger.getGrantRuntimePermissions());
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

            Threads.postOnMainThreadDelayed(() -> {
                finish();
                if (isOnNewIntent) {
                    Navigations.startActivitySafely(StartGuideActivity.this, ColorPhoneActivity.class);
                }
            }, 2000);
        } else {
            HSLog.i(TAG, "onPermissionChanged holder == " + holder);
            if (holder == null) {
                View view = findViewById(R.id.start_guide_function_page);
                view.setVisibility(View.GONE);

                view = findViewById(R.id.start_guide_confirm_page);
                view.setVisibility(View.VISIBLE);
                holder = new StartGuideViewListHolder(view, true);

                View oneKeyFix = view.findViewById(R.id.start_guide_confirm_fix);
                oneKeyFix.setBackground(BackgroundDrawables.createBackgroundDrawable(0xff852bf5, Dimensions.pxFromDp(24), true));

                oneKeyFix.setOnClickListener(v -> {
                    if (TextUtils.equals(from, FROM_KEY_GUIDE)) {
                        from = FROM_KEY_START;
                    }
                    if (AutoPermissionChecker.isPhonePermissionGranted()) {
                        permissionShowCount = Preferences.get(Constants.DESKTOP_PREFS).incrementAndGetInt(StartGuideActivity.ACC_KEY_SHOW_COUNT);
                        AutoRequestManager.getInstance().startAutoCheck(AutoRequestManager.AUTO_PERMISSION_FROM_FIX, from);
                    } else {
                        if (AutoPermissionChecker.isPermissionPermanentlyDenied(Manifest.permission.READ_PHONE_STATE)
                                || AutoPermissionChecker.isPermissionPermanentlyDenied(Manifest.permission.CALL_PHONE)) {
                            Analytics.logEvent("FixAlert_Automatic_Phone_Settings_Request");
                            AutoRequestManager.getInstance().openPermission(HSPermissionRequestMgr.TYPE_PHONE);
                        } else {
                            if (!AutoPermissionChecker.isRuntimePermissionGrant(Manifest.permission.READ_PHONE_STATE)) {
                                Analytics.logEvent("FixAlert_Automatic_ReadPhoneState_Request");
                            }

                            if (!AutoPermissionChecker.isRuntimePermissionGrant(Manifest.permission.CALL_PHONE)) {
                                Analytics.logEvent("FixAlert_Automatic_CallPhone_Request");
                            }
                            requiresPermission(AutoRequestManager.getConfirmRuntimePermission(), AUTO_PERMISSION_REQUEST);
                        }
                    }
                    oneKeyFixPressed = true;

                    Analytics.logEvent("FixAlert_Ok_Click", "From", from);
                });

                Analytics.logEvent("FixAlert_Show",
                        "From", from,
                        "Brand", AutoLogger.getBrand(),
                        "Os", AutoLogger.getOSVersion(),
                        "AccessType", AutoLogger.getPermissionString(RomUtils.checkIsHuaweiRom()));
            } else {
                int confirmPermission = holder.refreshConfirmPage();
                if (confirmPermission == 0 && confirmDialogPermission != 0) {
                    confirmPermission = confirmDialogPermission;
                }
                confirmDialogPermission = 0;
                boolean isGrant = StartGuidePermissionFactory.getItemGrant(confirmPermission);
                HSLog.i("Permission", "Permission: " + confirmPermission + "  grant: " + isGrant);
                if (isGrant) {
                    holder.requestNextPermission();
                    if (confirmPermission == StartGuidePermissionFactory.TYPE_PERMISSION_TYPE_PHONE) {
                        if (oneKeyFixPressed) {
                            Analytics.logEvent("FixAlert_Automatic_Phone_Settings_Granted");
                        } else {
                            Analytics.logEvent("FixAlert_Phone_Settings_Granted");
                        }
                    }
                    if (confirmPermission == StartGuidePermissionFactory.TYPE_PERMISSION_TYPE_NOTIFICATION) {
                        Analytics.logEvent("FixAlert_NA_Granted");
                    }
                } else {
                    if (confirmPermission == StartGuidePermissionFactory.TYPE_PERMISSION_TYPE_NOTIFICATION
                            && HSConfig.optBoolean(false, "Application", "AutoPermission", "AutoSkipWhenNAGranted")
                            && AutoRequestManager.getInstance().isGrantAllWithoutNAPermission()) {
                        finish();
                        if (isOnNewIntent) {
                            Navigations.startActivitySafely(StartGuideActivity.this, ColorPhoneActivity.class);
                        }
                        Analytics.logEvent("FixAlert_Auto_Skipped");
                        Preferences.getDefault().putBoolean(PREF_KEY_GUIDE_SHOW_WHEN_WELCOME, true);
                    }
                    showConfirmDialog(confirmPermission);
                }
            }

            if (canShowSkip()) {
                if (TextUtils.equals(from, FROM_KEY_GUIDE) || TextUtils.equals(from, FROM_KEY_START)) {
                    findViewById(R.id.start_guide_confirm_close).setVisibility(View.GONE);
                    View skip = findViewById(R.id.start_guide_confirm_skip);
                    skip.setVisibility(View.VISIBLE);
                    skip.setBackground(BackgroundDrawables.createBackgroundDrawable(0x0, Dimensions.pxFromDp(24), true));

                    skip.setOnClickListener(v -> {
                        finish();
                        if (isOnNewIntent) {
                            Navigations.startActivitySafely(StartGuideActivity.this, ColorPhoneActivity.class);
                        }
                        Analytics.logEvent("FixAlert_Cancel_Click", "From", from);
                        Preferences.getDefault().putBoolean(PREF_KEY_GUIDE_SHOW_WHEN_WELCOME, true);
                    });
                } else {
                    findViewById(R.id.start_guide_confirm_skip).setVisibility(View.GONE);
                    View close = findViewById(R.id.start_guide_confirm_close);
                    close.setVisibility(View.VISIBLE);
                    close.setBackground(BackgroundDrawables.createBackgroundDrawable(0x0, Dimensions.pxFromDp(24), true));

                    close.setOnClickListener(v -> {
                        showSkipDialog();
                        Analytics.logEvent("FixAlert_Cancel_Click", "From", from);
                    });
                }
            } else {
                findViewById(R.id.start_guide_confirm_skip).setVisibility(View.GONE);
                findViewById(R.id.start_guide_confirm_close).setVisibility(View.GONE);
            }
        }
    }

    private void showSkipDialog() {
        if (dialog != null) {
            dialog.dismiss();
        }

        View view = getLayoutInflater().inflate(R.layout.layout_skip_confirm_dialog, null);

        View bgView = view.findViewById(R.id.content_layout);
        bgView.setBackground(BackgroundDrawables.createBackgroundDrawable(0xffffffff, Dimensions.pxFromDp(16), false));
        View btn = view.findViewById(R.id.tv_first);
        btn.setBackground(BackgroundDrawables.createBackgroundDrawable(0xff6c63ff, Dimensions.pxFromDp(26), true));
        btn.setOnClickListener(v -> {
            dismissDialog();
            permissionShowCount = Preferences.get(Constants.DESKTOP_PREFS).incrementAndGetInt(StartGuideActivity.ACC_KEY_SHOW_COUNT);
            AutoRequestManager.getInstance().startAutoCheck(AutoRequestManager.AUTO_PERMISSION_FROM_FIX, from);

            Analytics.logEvent("FixAlert_Retain_Ok_Click", "From", from);
        });

        btn = view.findViewById(R.id.tv_second);
        btn.setOnClickListener(v -> {
            dismissDialog();
            finish();
            if (isOnNewIntent) {
                Navigations.startActivitySafely(StartGuideActivity.this, ColorPhoneActivity.class);
            }
            Preferences.getDefault().putBoolean(PREF_KEY_GUIDE_SHOW_WHEN_WELCOME, true);
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog);
        builder.setCancelable(false);
        builder.setView(view);
        dialog = builder.create();

        showDialog(dialog);

        Analytics.logEvent("FixAlert_Retain_Show", "From", from);
    }

    private boolean showPermissionDialog() {
        List<String> reqPermission = AutoRequestManager.getNOTGrantRuntimePermissions(AutoRequestManager.getAllRuntimePermission());
        if (reqPermission.size() == 0) {
            directPermission = false;
            return false;
        }

        View permissionDialog = findViewById(R.id.start_guide_confirm_permission);
        permissionDialog.setVisibility(View.VISIBLE);
        permissionDialog.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        View layout = permissionDialog.findViewById(R.id.start_guide_confirm_permission_layout);
        layout.setBackgroundDrawable(BackgroundDrawables.createBackgroundDrawable(Color.WHITE, Dimensions.pxFromDp(16), false));

        View action = permissionDialog.findViewById(R.id.start_guide_confirm_permission_action);
        action.setBackgroundDrawable(BackgroundDrawables.createBackgroundDrawable(0xff6c63ff, Dimensions.pxFromDp(21), true));
        action.setOnClickListener(v -> {
            requiresPermission(reqPermission, FIRST_LAUNCH_PERMISSION_REQUEST);

            permissionDialog.setVisibility(View.GONE);
            Analytics.logEvent("StartGuide_PermissionGuide_OK_Clicked");
        });

//        View cancel = permissionDialog.findViewById(R.id.start_guide_confirm_permission_close);
//        cancel.setOnClickListener(v -> {
//            layout.animate().scaleX(0.7f).scaleY(0.7f).alpha(0f).setDuration(300)
//                    .setListener(new AnimatorListenerAdapter() {
//                        @Override public void onAnimationEnd(Animator animation) {
//                            super.onAnimationEnd(animation);
//                            permissionDialog.setVisibility(View.GONE);
//                        }
//                    })
//                    .start();
//            directPermission = true;
//            Analytics.logEvent("StartGuide_PermissionGuide_Cancel_Clicked");
//        });

        layout.setScaleX(0.7f);
        layout.setScaleY(0.7f);
        layout.setAlpha(0.3f);
        layout.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(300).setListener(null).start();
        Analytics.logEvent("StartGuide_PermissionGuide_Show");

        return true;
    }

    private boolean showConfirmDialog(int confirmPermission) {
        if (dialog != null) {
            dialog.dismiss();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this, com.acb.call.R.style.Theme_AppCompat_Light_Dialog);
        builder.setCancelable(false);
        if (confirmPermission == StartGuidePermissionFactory.TYPE_PERMISSION_TYPE_SCREEN_FLASH) {
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
                onPermissionChanged();
            });

            builder.setNegativeButton(com.acb.call.R.string.no, (dialog, which) -> Analytics.logEvent("AutoStartAlert_No_Click"));
            Analytics.logEvent("AutoStartAlert_Show");

        } else if (confirmPermission == StartGuidePermissionFactory.TYPE_PERMISSION_TYPE_ON_LOCK) {
            builder.setTitle(com.acb.call.R.string.acb_request_permission_show_on_lockscreen_title);
            builder.setMessage(com.acb.call.R.string.acb_request_permission_show_on_lockscreen_content);
            builder.setPositiveButton(com.acb.call.R.string.yes, (dialog, which) -> {
                AutoPermissionChecker.onShowOnLockScreenChange(true);
                Preferences.getDefault().putBoolean("pref_key_permission_show_on_lock_screen_grant", true);
                Analytics.logEvent("LockScreenAlert_Yes_Click");

                Analytics.logEvent("FixALert_Lock_Granted");
                onPermissionChanged();
            });

            builder.setNegativeButton(com.acb.call.R.string.no, (dialog, which) -> Analytics.logEvent("LockScreenAlert_No_Click"));
            Analytics.logEvent("LockScreenAlert_Show");

        } else if (confirmPermission == StartGuidePermissionFactory.TYPE_PERMISSION_TYPE_BG_POP) {
            builder.setTitle(R.string.acb_request_permission_bg_pop_title);
            builder.setMessage(R.string.acb_request_permission_bg_pop_content);
            builder.setPositiveButton(com.acb.call.R.string.yes, (dialog, which) -> {
                AutoPermissionChecker.onBgPopupChange(true);
                Analytics.logEvent("BackgroundPopupAlert_Yes_Click");

                Analytics.logEvent("FixALert_BackgroundPopup_Granted");

                holder.refreshHolder(StartGuidePermissionFactory.TYPE_PERMISSION_TYPE_BG_POP);
                onPermissionChanged();
            });

            builder.setNegativeButton(com.acb.call.R.string.no, (dialog, which) -> Analytics.logEvent("BackgroundPopupAlert_No_Click"));
            Analytics.logEvent("BackgroundPopupAlert_Show");

        } else {
            if (confirmPermission == StartGuidePermissionFactory.TYPE_PERMISSION_TYPE_NOTIFICATION) {
                if (AutoPermissionChecker.isNotificationListeningGranted()) {
                    Analytics.logEvent("FixAlert_NA_Granted");
                }
            }
            return false;
        }

        confirmDialogPermission = confirmPermission;
        dialog = builder.create();
        dialog.show();

        return true;
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
            HSLog.i(TAG, "onPermissionChanged onStart");
            if (getIntent() != null) {
                confirmDialogPermission = getIntent().getIntExtra(INTENT_KEY_PERMISSION_TYPE, 0);
            }
            onPermissionChanged();
        }
    }

    @Override protected void onStop() {
        super.onStop();
    }

    @Override public void onReceive(String s, HSBundle hsBundle) {
        if (TextUtils.equals(AutoRequestManager.NOTIFY_PERMISSION_CHECK_FINISH_AND_CLOSE_WINDOW, s)) {
            HSLog.i(TAG, "onPermissionChanged onReceive");
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
            AutoRequestManager.getInstance().startAutoCheck(AutoRequestManager.AUTO_PERMISSION_FROM_AUTO, from);

            permissionShowCount = Preferences.get(Constants.DESKTOP_PREFS).incrementAndGetInt(ACC_KEY_SHOW_COUNT);
            Analytics.logEvent("Accessbility_Guide_Btn_Click",
                    "Brand", AutoLogger.getBrand(),
                    "Os", AutoLogger.getOSVersion(),
                    "Time", String.valueOf(permissionShowCount));
        });

        handler.postDelayed(() -> {
            title.setText(R.string.start_guide_request_accessibility_title);
            title.animate().alpha(0.8f).setDuration(750).start();
            button.animate().alpha(1f).setDuration(750).start();
        }, 2400);
        Analytics.logEvent("Accessbility_Guide_Show",
                "Brand", AutoLogger.getBrand(),
                "Os", AutoLogger.getOSVersion(),
                "Permission", AutoLogger.getGrantRuntimePermissions());
    }

    private Toast toast;
    public void showToast() {
        if (toast != null) {
            toast.cancel();
        }

        toast = new Toast(HSApplication.getContext().getApplicationContext());
        final View contentView = LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.toast_start_guide_check, null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            contentView.setElevation(Dimensions.pxFromDp(8));
        }
        TextView textView = contentView.findViewById(R.id.start_guide_check);
        textView.setBackground(BackgroundDrawables.createBackgroundDrawable(getResources().getColor(R.color.white_87_transparent), Dimensions.pxFromDp(8), false));
        toast.setGravity(Gravity.CENTER, 0 , 0);
        toast.setView(contentView);
        toast.show();

        Analytics.logEvent("StartGuide_Privacy_Toast_Show");
    }

    /**
     * Only request first launch. (if Enabled and not has permission)
     */
    private void requiresPermission(List<String> permissions, int reqCode) {
        boolean isEnabled = ScreenFlashManager.getInstance().getAcbCallFactory().isConfigEnabled()
                && ScreenFlashSettings.isScreenFlashModuleEnabled();
        HSLog.i("Permissions ScreenFlash state change : " + isEnabled);
        if (!isEnabled) {
            return;
        }

        boolean grantPermission = true;
        boolean grant;
        for (String p : permissions) {
            grant = RuntimePermissions.checkSelfPermission(this, p)
                    == RuntimePermissions.PERMISSION_GRANTED;
            grantPermission &= grant;
        }

        if (!grantPermission) {
            RuntimePermissions.requestPermissions(this, permissions.toArray(new String[0]), reqCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        RuntimePermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);

        directPermission = false;

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

        if (requestCode == FIRST_LAUNCH_PERMISSION_REQUEST) {
            ModuleUtils.setAllModuleUserEnable();
            showAccessibilityPermissionPage();
        }
    }

    public void onPermissionsGranted(int requestCode, List<String> list) {
        HSLog.i("Permission", "onPermissionsGranted: " + list);
        if (requestCode == CONFIRM_PAGE_PERMISSION_REQUEST) {
            for (String p : list) {
                switch (p) {
                    case Manifest.permission.CALL_PHONE:
                    case Manifest.permission.READ_PHONE_STATE:
                    case Manifest.permission.ANSWER_PHONE_CALLS:
                        holder.refreshHolder(StartGuidePermissionFactory.TYPE_PERMISSION_TYPE_PHONE);
                        if (TextUtils.equals(p, Manifest.permission.READ_PHONE_STATE)) {
                            Analytics.logEvent("FixAlert_ReadPhoneState_Granted");
                        }

                        if (TextUtils.equals(p, Manifest.permission.CALL_PHONE)) {
                            Analytics.logEvent("FixAlert_CallPhone_Granted");
                        }

                        if (AutoPermissionChecker.isPhonePermissionGranted()) {
                            if (!AutoRequestManager.getInstance().isGrantAllPermission()
                                && oneKeyFixPressed) {
                                AutoRequestManager.getInstance().startAutoCheck(AutoRequestManager.AUTO_PERMISSION_FROM_FIX, FROM_KEY_START);
                            } else {
                                confirmDialogPermission = StartGuidePermissionFactory.TYPE_PERMISSION_TYPE_PHONE;
                                onPermissionChanged();
                            }

                            if (AutoRequestManager.getInstance().isGrantAllPermission()) {
                                AutoLogger.logEventWithBrandAndOS("All_Granted_From_FixAlert");
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        } else if (requestCode == AUTO_PERMISSION_REQUEST) {
            for (String p : list) {
                switch (p) {
                    case Manifest.permission.CALL_PHONE:
                    case Manifest.permission.READ_PHONE_STATE:
                    case Manifest.permission.ANSWER_PHONE_CALLS:
                        holder.refreshHolder(StartGuidePermissionFactory.TYPE_PERMISSION_TYPE_PHONE);
                        if (TextUtils.equals(p, Manifest.permission.READ_PHONE_STATE)) {
                            Analytics.logEvent("FixAlert_Automatic_ReadPhoneState_Granted");
                        }

                        if (TextUtils.equals(p, Manifest.permission.CALL_PHONE)) {
                            Analytics.logEvent("FixAlert_Automatic_CallPhone_Granted");
                        }

                        if (AutoPermissionChecker.isPhonePermissionGranted()) {
                            if (!AutoRequestManager.getInstance().isGrantAllPermission()
                                    && oneKeyFixPressed) {
                                AutoRequestManager.getInstance().startAutoCheck(AutoRequestManager.AUTO_PERMISSION_FROM_FIX, FROM_KEY_START);
                            } else {
                                confirmDialogPermission = StartGuidePermissionFactory.TYPE_PERMISSION_TYPE_PHONE;
                                onPermissionChanged();
                            }
                        }

                        if (AutoRequestManager.getInstance().isGrantAllPermission()
                                && oneKeyFixPressed) {
                            AutoLogger.logEventWithBrandAndOS("All_Granted_From_FixAlert");

                            Analytics.logEvent("All_Granted_From_Automatic",
                                    "Brand", AutoLogger.getBrand(),
                                    "Os", AutoLogger.getOSVersion(),
                                    "Time", String.valueOf(AutoPermissionChecker.getAutoRequestCount()));
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Some permissions have been denied
        // ...

        HSLog.i("Permission", "onPermissionsDenied: " + list);
        if (requestCode == CONFIRM_PAGE_PERMISSION_REQUEST
                || requestCode == AUTO_PERMISSION_REQUEST) {
            if (!AutoPermissionChecker.isPhonePermissionGranted()) {
                Analytics.logEvent("FixAlert_Phone_Settings_Request");
                AutoRequestManager.getInstance().openPermission(HSPermissionRequestMgr.TYPE_PHONE);
            }
        }
    }

}
