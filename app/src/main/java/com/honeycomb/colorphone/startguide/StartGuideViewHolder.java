package com.honeycomb.colorphone.startguide;

import android.animation.ValueAnimator;
import android.support.annotation.IdRes;
import android.support.annotation.IntDef;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.airbnb.lottie.LottieAnimationView;
import com.honeycomb.colorphone.R;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class StartGuideViewHolder {

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

    private boolean isConfirmPage = true;
    private View container;
    private View circleAnimView;
    private ValueAnimator circleAnimator;

    private ImageView screenFlashOK;
    private LottieAnimationView screenFlashLoading;
    private View screenFlashFix;

    private ImageView onLockerOK;
    private LottieAnimationView onLockerLoading;
    private View onLockerFix;

    private ImageView callOK;
    private LottieAnimationView callLoading;
    private View callFix;

    private View oneKeyFix;

    public StartGuideViewHolder(View root, boolean isConfirmPage) {
        container = root;
        this.isConfirmPage = isConfirmPage;

        screenFlashOK = container.findViewById(R.id.start_guide_permission_auto_start_ok);
        screenFlashLoading = container.findViewById(R.id.start_guide_permission_auto_start_loading);
        onLockerOK = container.findViewById(R.id.start_guide_permission_onlocker_ok);
        onLockerLoading = container.findViewById(R.id.start_guide_permission_onlocker_loading);
        callOK = container.findViewById(R.id.start_guide_permission_call_ok);
        callLoading = container.findViewById(R.id.start_guide_permission_call_loading);

        if (isConfirmPage) {
            screenFlashFix = container.findViewById(R.id.start_guide_permission_auto_start_fix);
            screenFlashFix.setBackground(BackgroundDrawables.createBackgroundDrawable(0xff852bf5, Dimensions.pxFromDp(24), true));
            onLockerFix = container.findViewById(R.id.start_guide_permission_onlocker_fix);
            onLockerFix.setBackground(BackgroundDrawables.createBackgroundDrawable(0xff852bf5, Dimensions.pxFromDp(24), true));
            callFix = container.findViewById(R.id.start_guide_permission_call_fix);
            callFix.setBackground(BackgroundDrawables.createBackgroundDrawable(0xff852bf5, Dimensions.pxFromDp(24), true));

            oneKeyFix = container.findViewById(R.id.start_guide_confirm_fix);
            oneKeyFix.setBackground(BackgroundDrawables.createBackgroundDrawable(0xff852bf5, Dimensions.pxFromDp(24), true));

            oneKeyFix.setOnClickListener(v -> {
//                HSLog.i(TAG, "oneKeyFix  OnClick");
//                if (circleAnimView == null) {
//                    circleAnimView = container.findViewById(R.id.start_guide_confirm_number);
//                }
//                HSLog.i(TAG, "oneKeyFix  OnClick anim == " +circleAnimView);
//                startCircleAnimation();
//                AutoRequestManager.getInstance().showCoverWindow();
//
//                Threads.postOnMainThreadDelayed(() -> {
//                    AutoRequestManager.getInstance().dismissCoverWindow();
//                }, 20000);
//                setPermissionStatus(TYPE_PERMISSION_TYPE_SCREEN_FLASH, PERMISSION_STATUS_LOADING);
//                setPermissionStatus(TYPE_PERMISSION_TYPE_ON_LOCK, PERMISSION_STATUS_OK);
//                setPermissionStatus(TYPE_PERMISSION_TYPE_CALL, PERMISSION_STATUS_FIX);

            });
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

                circleAnimator = ValueAnimator.ofInt(0, 1);
                circleAnimator.setDuration(360 * 1000 / CIRCLE_SPEED);
                circleAnimator.setRepeatCount(ValueAnimator.INFINITE);
                circleAnimator.setRepeatMode(ValueAnimator.RESTART);
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

    public void setPermissionStatus(@PERMISSION_TYPES int pType, @PERMISSION_STATUS int pStatus) {
        ImageView ok;
        LottieAnimationView loading;
        View fix;
        switch (pType) {
            case TYPE_PERMISSION_TYPE_CALL:
                ok = callOK;
                loading = callLoading;
                fix = callFix;
                break;
            case TYPE_PERMISSION_TYPE_ON_LOCK:
                ok = onLockerOK;
                loading = onLockerLoading;
                fix = onLockerFix;
                break;
            default:
            case TYPE_PERMISSION_TYPE_SCREEN_FLASH:
                ok = screenFlashOK;
                loading = screenFlashLoading;
                fix = screenFlashFix;
                break;
        }

        switch (pStatus) {
            case PERMISSION_STATUS_FAILED:
                ok.setVisibility(View.VISIBLE);
                ok.setImageResource(R.drawable.start_guide_confirm_alert_image);
                break;
            case PERMISSION_STATUS_FIX:
                if (fix != null) {
                    ok.setVisibility(View.GONE);
                    fix.setVisibility(View.VISIBLE);
                }
                break;
            case PERMISSION_STATUS_LOADING:
                if (loading != null) {
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
                ok.setVisibility(View.VISIBLE);
                ok.setImageResource(R.drawable.start_guide_confirm_ok_image);
                if (loading != null) {
                    loading.setVisibility(View.GONE);
                }
                if (fix != null) {
                    fix.setVisibility(View.GONE);
                }
                break;
        }
    }
}
