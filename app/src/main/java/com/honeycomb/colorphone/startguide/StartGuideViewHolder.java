package com.honeycomb.colorphone.startguide;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.annotation.IntDef;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.autopermission.AutoRequestManager;
import com.honeycomb.colorphone.autopermission.PermissionChecker;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.permission.HSPermissionType;
import com.ihs.permission.Utils;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class StartGuideViewHolder implements INotificationObserver {

    public static final int TYPE_PERMISSION_TYPE_SCREEN_FLASH = 1;
    public static final int TYPE_PERMISSION_TYPE_ON_LOCK = 2;
    public static final int TYPE_PERMISSION_TYPE_CALL = 3;

    public static final int PERMISSION_STATUS_NOT_START = 0;
    public static final int PERMISSION_STATUS_LOADING = 1;
    public static final int PERMISSION_STATUS_OK = 2;
    public static final int PERMISSION_STATUS_FAILED = 3;
    public static final int PERMISSION_STATUS_FIX = 4;

    @IntDef({TYPE_PERMISSION_TYPE_SCREEN_FLASH,
            TYPE_PERMISSION_TYPE_ON_LOCK,
            TYPE_PERMISSION_TYPE_CALL})
    @Retention(RetentionPolicy.SOURCE)
    private @interface PERMISSION_TYPES {}

    @IntDef({PERMISSION_STATUS_NOT_START, 
            PERMISSION_STATUS_LOADING, 
            PERMISSION_STATUS_OK,
            PERMISSION_STATUS_FAILED,
            PERMISSION_STATUS_FIX
            })
    @Retention(RetentionPolicy.SOURCE)
    private @interface PERMISSION_STATUS {}

    private static final String TAG = "AutoPermission";
    private static final int CIRCLE_SPEED = 360;
    private static final int UPGRADE_MIN_INTERVAL = 30;
    private static final int UPGRADE_MAX_INTERVAL = 660;
    private static final int PROGRESS_MAX_VALUE = 100;
    private static final int EVENT_UPGRADE = 5000;

    private boolean isConfirmPage = true;
    private View container;
    private View circleAnimView;
    private ValueAnimator circleAnimator;

    private ImageView screenFlashOK;
    private LottieAnimationView screenFlashLoading;
    private View screenFlashFix;
    private View screenFlashText;

    private ImageView onLockerOK;
    private LottieAnimationView onLockerLoading;
    private View onLockerFix;
    private View onLockerText;

    private ImageView callOK;
    private LottieAnimationView callLoading;
    private View callFix;
    private View callText;

    private View oneKeyFix;

    private TextView progress;
    private int progressNum;
    private int goalNum;
    private int progressInterval = UPGRADE_MIN_INTERVAL * 4;
    private int finalStatus;

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case EVENT_UPGRADE:
                    if (!isConfirmPage) {
                        progressNum++;
                        long interval = getNextUpgradeDelay();

                        if (goalNum == PROGRESS_MAX_VALUE || progressNum >= PROGRESS_MAX_VALUE) {
                            interval = UPGRADE_MIN_INTERVAL;
                        }

                        if (progressNum >= PROGRESS_MAX_VALUE) {
                            if (goalNum == PROGRESS_MAX_VALUE) {
                                progressNum = PROGRESS_MAX_VALUE;
                                setPermissionStatus(TYPE_PERMISSION_TYPE_CALL, finalStatus);
                                interval = 0;
                            } else {
                                progressNum = PROGRESS_MAX_VALUE - 1;
                            }
                            HSLog.i(TAG, "progress Animation end " + (System.currentTimeMillis() - startAutoRequestAnimation));
                        }

                        if (interval != 0) {
                            handler.sendEmptyMessageDelayed(EVENT_UPGRADE, interval);
                        } else {
                            finish();
                        }
                        progress.setText(String.valueOf(progressNum + "%"));
                    }
                    break;
            }
        }
    };

    public StartGuideViewHolder(View root, boolean isConfirmPage) {
        container = root;
        this.isConfirmPage = isConfirmPage;

        screenFlashText = container.findViewById(R.id.start_guide_permission_auto_start);
        screenFlashOK = container.findViewById(R.id.start_guide_permission_auto_start_ok);
        screenFlashOK.setTag(PERMISSION_STATUS_NOT_START);
        screenFlashLoading = container.findViewById(R.id.start_guide_permission_auto_start_loading);

        onLockerText = container.findViewById(R.id.start_guide_permission_onlocker);
        onLockerOK = container.findViewById(R.id.start_guide_permission_onlocker_ok);
        onLockerOK.setTag(PERMISSION_STATUS_NOT_START);
        onLockerLoading = container.findViewById(R.id.start_guide_permission_onlocker_loading);

        callText = container.findViewById(R.id.start_guide_permission_call);
        callOK = container.findViewById(R.id.start_guide_permission_call_ok);
        callOK.setTag(PERMISSION_STATUS_NOT_START);
        callLoading = container.findViewById(R.id.start_guide_permission_call_loading);

        if (isConfirmPage) {
            screenFlashFix = container.findViewById(R.id.start_guide_permission_auto_start_fix);
            screenFlashFix.setBackground(BackgroundDrawables.createBackgroundDrawable(0xff852bf5, Dimensions.pxFromDp(24), true));
            screenFlashFix.setOnClickListener(v -> AutoRequestManager.getInstance().openPermission(HSPermissionType.TYPE_AUTO_START));

            onLockerFix = container.findViewById(R.id.start_guide_permission_onlocker_fix);
            onLockerFix.setBackground(BackgroundDrawables.createBackgroundDrawable(0xff852bf5, Dimensions.pxFromDp(24), true));
            onLockerFix.setOnClickListener(v -> AutoRequestManager.getInstance().openPermission(HSPermissionType.TYPE_SHOW_ON_LOCK));

            callFix = container.findViewById(R.id.start_guide_permission_call_fix);
            callFix.setBackground(BackgroundDrawables.createBackgroundDrawable(0xff852bf5, Dimensions.pxFromDp(24), true));
            callFix.setOnClickListener(v -> AutoRequestManager.getInstance().openPermission(HSPermissionType.TYPE_NOTIFICATION_LISTENING));

            oneKeyFix = container.findViewById(R.id.start_guide_confirm_fix);
            oneKeyFix.setBackground(BackgroundDrawables.createBackgroundDrawable(0xff852bf5, Dimensions.pxFromDp(24), true));

            oneKeyFix.setOnClickListener(v -> {
                AutoRequestManager.getInstance().startAutoCheck();
            });

            boolean screenFlashGrant = PermissionChecker.hasAutoStartPermission();
            boolean onLockGrant = PermissionChecker.hasShowOnLockScreenPermission();
            boolean callGrant = Utils.isNotificationListeningGranted();
            setPermissionStatus(TYPE_PERMISSION_TYPE_SCREEN_FLASH, screenFlashGrant ? PERMISSION_STATUS_OK : PERMISSION_STATUS_FIX);
            setPermissionStatus(TYPE_PERMISSION_TYPE_ON_LOCK, onLockGrant ? PERMISSION_STATUS_OK : PERMISSION_STATUS_FIX);
            setPermissionStatus(TYPE_PERMISSION_TYPE_CALL, callGrant ? PERMISSION_STATUS_OK : PERMISSION_STATUS_FIX);

            int notGrant = 0;
            if (!screenFlashGrant) {
                notGrant++;
            }
            if (!onLockGrant) {
                notGrant++;
            }
            if (!callGrant) {
                notGrant++;
            }

            TextView ball = container.findViewById(R.id.start_guide_confirm_number);
            TextView title = container.findViewById(R.id.start_guide_permission_title);
            ball.setText(String.valueOf(notGrant));
            title.setText(String.format(container.getContext().getString(R.string.start_guide_permission_title), String.valueOf(notGrant)));

        } else {
            progress = container.findViewById(R.id.start_guide_request_progress);

            setPermissionStatus(TYPE_PERMISSION_TYPE_SCREEN_FLASH, PERMISSION_STATUS_NOT_START);
            setPermissionStatus(TYPE_PERMISSION_TYPE_ON_LOCK, PERMISSION_STATUS_NOT_START);
            setPermissionStatus(TYPE_PERMISSION_TYPE_CALL, PERMISSION_STATUS_NOT_START);
        }
    }

    public void setCircleAnimView(@IdRes int viewID) {
        circleAnimView = container.findViewById(viewID);
    }

    public void startCircleAnimation() {
        if (circleAnimView != null) {
            ViewGroup.LayoutParams params = circleAnimView.getLayoutParams();
            if (params instanceof ConstraintLayout.LayoutParams) {
                ConstraintLayout.LayoutParams clParams = ((ConstraintLayout.LayoutParams) params);
                final float oriAngle = clParams.circleAngle;
                cancelCircleAnimation();

                if (isConfirmPage) {
                    circleAnimator = ValueAnimator.ofFloat(-0.125f, 0f);
                } else {
                    circleAnimator = ValueAnimator.ofFloat(0, 1);
                    circleAnimator.setRepeatCount(ValueAnimator.INFINITE);
                    circleAnimator.setRepeatMode(ValueAnimator.RESTART);
                }

                circleAnimator.setDuration(360 * 1000 / CIRCLE_SPEED);
                circleAnimator.addUpdateListener(animation -> {
                    clParams.circleAngle = (oriAngle + animation.getAnimatedFraction() * 360) % 360;
                    circleAnimView.setLayoutParams(clParams);
                    circleAnimView.requestLayout();
                });
                circleAnimator.start();
                return;
            }
            HSLog.i(TAG, "not ConstraintLayout.LayoutParams ");
        }
        HSLog.i(TAG, "circleAnimView is null ");
    }

    public void cancelCircleAnimation() {
        if (circleAnimator != null) {
            circleAnimator.cancel();
            circleAnimator = null;
        }
    }

    @Override public void onReceive(String s, HSBundle hsBundle) {
        if (TextUtils.equals(s, AutoRequestManager.NOTIFICATION_PERMISSION_RESULT)) {
            HSPermissionType pType = (HSPermissionType) hsBundle.getObject(AutoRequestManager.BUNDLE_PERMISSION_TYPE);
            boolean result = hsBundle.getBoolean(AutoRequestManager.BUNDLE_PERMISSION_RESULT);
            int status = result ? PERMISSION_STATUS_OK : PERMISSION_STATUS_FAILED;
            switch (pType) {
                case TYPE_NOTIFICATION_LISTENING:
                    setPermissionStatus(TYPE_PERMISSION_TYPE_CALL, status);
                    break;
                case TYPE_AUTO_START:
                    setPermissionStatus(TYPE_PERMISSION_TYPE_SCREEN_FLASH, status);
                    break;
                case TYPE_SHOW_ON_LOCK:
                    setPermissionStatus(TYPE_PERMISSION_TYPE_ON_LOCK, status);
                    break;
                default:
                    break;
            }
        }
    }

    private void finish() {
        AutoRequestManager.getInstance().dismissCoverWindow();
    }

    private long startAutoRequestAnimation;
    public void startAutoRequestAnimation() {
        if (isConfirmPage) {
            return;
        }

        HSLog.i(TAG, "startAutoRequestAnimation ");
        startAutoRequestAnimation = System.currentTimeMillis();

        handler.sendEmptyMessage(EVENT_UPGRADE);
        setPermissionStatus(TYPE_PERMISSION_TYPE_SCREEN_FLASH, PERMISSION_STATUS_LOADING);

        HSGlobalNotificationCenter.addObserver(AutoRequestManager.NOTIFICATION_PERMISSION_RESULT, this);

    }

    private long getNextUpgradeDelay() {
        if (goalNum == PROGRESS_MAX_VALUE) {
            progressInterval = UPGRADE_MIN_INTERVAL;
        } else if (goalNum - progressNum > 15) {
            progressInterval *= 0.8;
        } else if (progressNum - goalNum > 5) {
            progressInterval *= 1.3;
        }
        return Math.min(Math.max(progressInterval, UPGRADE_MIN_INTERVAL), UPGRADE_MAX_INTERVAL);
    }

    public void setPermissionStatus(@PERMISSION_TYPES int pType, @PERMISSION_STATUS int pStatus) {
        ImageView ok;
        LottieAnimationView loading;
        View fix;
        View text;
        switch (pType) {
            case TYPE_PERMISSION_TYPE_CALL:
                ok = callOK;
                loading = callLoading;
                fix = callFix;
                text = callText;
                break;
            case TYPE_PERMISSION_TYPE_ON_LOCK:
                ok = onLockerOK;
                loading = onLockerLoading;
                fix = onLockerFix;
                text = onLockerText;
                break;
            case TYPE_PERMISSION_TYPE_SCREEN_FLASH:
                ok = screenFlashOK;
                loading = screenFlashLoading;
                fix = screenFlashFix;
                text = screenFlashText;
                break;
            default:
                return;
        }

        int lastStatus = Integer.valueOf(ok.getTag().toString());
        switch (pStatus) {
            case PERMISSION_STATUS_FAILED:
                if (lastStatus == PERMISSION_STATUS_LOADING) {
                    loading.addAnimatorListener(new AnimatorListenerAdapter() {
                        @Override public void onAnimationRepeat(Animator animation) {
                            super.onAnimationRepeat(animation);
                            loading.cancelAnimation();
                            loading.removeAllAnimatorListeners();
                            loading.setVisibility(View.GONE);

                            ok.setVisibility(View.VISIBLE);
                            ok.setImageResource(R.drawable.start_guide_confirm_alert_image);
                            setPermissionStatus(pType + 1, PERMISSION_STATUS_LOADING);
                        }
                    });
                } else {
                    loading.setVisibility(View.GONE);
                    ok.setVisibility(View.VISIBLE);
                    ok.setImageResource(R.drawable.start_guide_confirm_alert_image);
                }
                break;
            case PERMISSION_STATUS_FIX:
                if (fix != null) {
                    ok.setVisibility(View.GONE);
                    fix.setVisibility(View.VISIBLE);
                }
                break;
            case PERMISSION_STATUS_LOADING:
                if (loading != null) {
                    ok.setVisibility(View.GONE);
                    loading.setVisibility(View.VISIBLE);
                    loading.useHardwareAcceleration();
                    loading.playAnimation();
                }
                break;
            case PERMISSION_STATUS_NOT_START:
                ok.setVisibility(View.GONE);
                if (loading != null) {
                    loading.setVisibility(View.GONE);
                }
                if (fix != null) {
                    fix.setVisibility(View.GONE);
                }
                break;
            case PERMISSION_STATUS_OK:
                if (lastStatus == PERMISSION_STATUS_LOADING) {
                    ok.setVisibility(View.GONE);
                    loading.addAnimatorListener(new AnimatorListenerAdapter() {
                        @Override public void onAnimationRepeat(Animator animation) {
                            super.onAnimationRepeat(animation);
                            loading.cancelAnimation();
                            loading.setAnimation("lottie/start_guide/permission_done.json");
                            loading.setRepeatCount(0);
                            loading.playAnimation();

                            loading.removeAllAnimatorListeners();
                            loading.addAnimatorListener(new AnimatorListenerAdapter() {
                                @Override public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    ok.setVisibility(View.VISIBLE);
                                    ok.setImageResource(R.drawable.start_guide_confirm_ok_image);
                                    loading.removeAllAnimatorListeners();
                                    loading.setVisibility(View.GONE);
                                    setPermissionStatus(pType + 1, PERMISSION_STATUS_LOADING);

                                    ok.animate().alpha(0.5f).setDuration(100).start();
                                    text.animate().alpha(0.5f).setDuration(100).start();
                                }
                            });
                        }
                    });
                } else {
                    loading.setVisibility(View.GONE);
                    ok.setVisibility(View.VISIBLE);
                    ok.setImageResource(R.drawable.start_guide_confirm_ok_image);
                }

                if (fix != null) {
                    fix.setVisibility(View.GONE);
                }
                break;
        }
        ok.setTag(pStatus);
    }
}
