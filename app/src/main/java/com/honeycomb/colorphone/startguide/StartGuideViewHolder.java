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
import com.honeycomb.colorphone.autopermission.AutoLogger;
import com.honeycomb.colorphone.autopermission.AutoPermissionChecker;
import com.honeycomb.colorphone.autopermission.AutoRequestManager;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.permission.HSPermissionType;
import com.ihs.permission.Utils;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Threads;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class StartGuideViewHolder implements INotificationObserver {

    public static final String ALL_PERMISSION_GRANT = "all_permission_grant";
    public static final int TYPE_PERMISSION_TYPE_SCREEN_FLASH = 1;
    public static final int TYPE_PERMISSION_TYPE_ON_LOCK = 2;
    public static final int TYPE_PERMISSION_TYPE_CALL = 3;

    public static final int PERMISSION_STATUS_NOT_START = 0;
    public static final int PERMISSION_STATUS_LOADING = 1;
    public static final int PERMISSION_STATUS_FAILED = 2;
    public static final int PERMISSION_STATUS_FIX = 3;
    public static final int PERMISSION_STATUS_OK = 4;

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
    private boolean gotoFetchScreenFlash = false;

    private ImageView onLockerOK;
    private LottieAnimationView onLockerLoading;
    private View onLockerFix;
    private View onLockerText;
    private boolean gotoFetchOnLock = false;

    private ImageView callOK;
    private LottieAnimationView callLoading;
    private View callFix;
    private View callText;
    private boolean gotoFetchCall = false;

    private View oneKeyFix;

    private TextView progress;
    private int progressNum;
    private int goalNum = 1;
    private int progressInterval = UPGRADE_MIN_INTERVAL * 2;
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
            screenFlashFix.setOnClickListener(v -> {
                AutoRequestManager.getInstance().openPermission(HSPermissionType.TYPE_AUTO_START);
                AutoLogger.logEventWithBrandAndOS("FixALert_AutoStart_Click");
                gotoFetchScreenFlash = true;
            });

            onLockerFix = container.findViewById(R.id.start_guide_permission_onlocker_fix);
            onLockerFix.setBackground(BackgroundDrawables.createBackgroundDrawable(0xff852bf5, Dimensions.pxFromDp(24), true));
            onLockerFix.setOnClickListener(v -> {
                AutoRequestManager.getInstance().openPermission(HSPermissionType.TYPE_SHOW_ON_LOCK);
                AutoLogger.logEventWithBrandAndOS("FixALert_Lock_Click");
                gotoFetchOnLock = true;
            });

            callFix = container.findViewById(R.id.start_guide_permission_call_fix);
            callFix.setBackground(BackgroundDrawables.createBackgroundDrawable(0xff852bf5, Dimensions.pxFromDp(24), true));
            callFix.setOnClickListener(v -> {
                AutoRequestManager.getInstance().openPermission(HSPermissionType.TYPE_NOTIFICATION_LISTENING);
                AutoLogger.logEventWithBrandAndOS("FixALert_NA_Click");
                gotoFetchCall = true;
            });

            oneKeyFix = container.findViewById(R.id.start_guide_confirm_fix);
            oneKeyFix.setBackground(BackgroundDrawables.createBackgroundDrawable(0xff852bf5, Dimensions.pxFromDp(24), true));

            oneKeyFix.setOnClickListener(v -> {
                AutoRequestManager.getInstance().startAutoCheck(AutoRequestManager.AUTO_PERMISSION_FROM_FIX);
                AutoLogger.logEventWithBrandAndOS("Automatic_Begin_FromAccessbility");
            });

            refresh();
        } else {
            progress = container.findViewById(R.id.start_guide_request_progress);

            setPermissionStatus(TYPE_PERMISSION_TYPE_SCREEN_FLASH, PERMISSION_STATUS_NOT_START);
            setPermissionStatus(TYPE_PERMISSION_TYPE_ON_LOCK, PERMISSION_STATUS_NOT_START);
            setPermissionStatus(TYPE_PERMISSION_TYPE_CALL, PERMISSION_STATUS_NOT_START);
        }

        HSGlobalNotificationCenter.addObserver(AutoRequestManager.NOTIFICATION_PERMISSION_RESULT, this);
    }

    public void refresh() {
        if (!isConfirmPage) {
            return;
        }
        boolean screenFlashGrant = AutoPermissionChecker.hasAutoStartPermission();
        boolean onLockGrant = AutoPermissionChecker.hasShowOnLockScreenPermission();
        boolean callGrant = Utils.isNotificationListeningGranted();
        setPermissionStatus(TYPE_PERMISSION_TYPE_SCREEN_FLASH, screenFlashGrant ? PERMISSION_STATUS_OK : PERMISSION_STATUS_FIX);
        setPermissionStatus(TYPE_PERMISSION_TYPE_ON_LOCK, onLockGrant ? PERMISSION_STATUS_OK : PERMISSION_STATUS_FIX);
        setPermissionStatus(TYPE_PERMISSION_TYPE_CALL, callGrant ? PERMISSION_STATUS_OK : PERMISSION_STATUS_FIX);

        int notGrant = 0;
        if (!screenFlashGrant) {
            notGrant++;
        } else if (gotoFetchScreenFlash) {
            gotoFetchScreenFlash = false;
            AutoLogger.logEventWithBrandAndOS("FixAlert_AutoStart_Granted");
        }
        if (!onLockGrant) {
            notGrant++;
        } else if (gotoFetchOnLock) {
            gotoFetchOnLock = false;
            AutoLogger.logEventWithBrandAndOS("FixALert_Lock_Granted");
        }
        if (!callGrant) {
            notGrant++;
        } else if (gotoFetchCall) {
            gotoFetchCall = false;
            AutoLogger.logEventWithBrandAndOS("FixALert_NA_Granted");

        }

        if (notGrant == 0) {
            AutoLogger.logEventWithBrandAndOS("FixAlert_All_Granted");
            finish();
        }

        TextView ball = container.findViewById(R.id.start_guide_confirm_number);
        TextView title = container.findViewById(R.id.start_guide_permission_title);
        ball.setText(String.valueOf(notGrant));
        title.setText(String.format(container.getContext().getString(R.string.start_guide_permission_title), String.valueOf(notGrant)));
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
            int status = result ? PERMISSION_STATUS_OK : isConfirmPage ? PERMISSION_STATUS_FIX : PERMISSION_STATUS_FAILED;
            switch (pType) {
                case TYPE_AUTO_START:
                    goalNum += 33;
                    setPermissionStatus(TYPE_PERMISSION_TYPE_SCREEN_FLASH, status);
                    HSLog.w(TAG, "cast time 11 " + (System.currentTimeMillis() - startAutoRequestAnimation) + "  num == " + progressNum);
                    break;
                case TYPE_SHOW_ON_LOCK:
                    goalNum += 33;
                    setPermissionStatus(TYPE_PERMISSION_TYPE_ON_LOCK, status);
                    HSLog.w(TAG, "cast time 22 " + (System.currentTimeMillis() - startAutoRequestAnimation) + "  num == " + progressNum);
                    break;
                case TYPE_NOTIFICATION_LISTENING:
                    goalNum += 33;
                    finalStatus = status;
                    setPermissionStatus(TYPE_PERMISSION_TYPE_CALL, status);
                    HSLog.w(TAG, "cast time 33 " + (System.currentTimeMillis() - startAutoRequestAnimation) + "  num == " + progressNum);
                    break;

                default:
                    break;
            }

            refresh();
        }
    }

    private void finish() {
        HSLog.w(TAG, "finish num == " + progressNum);
        handler.removeMessages(EVENT_UPGRADE);
        handler = null;

        if (isConfirmPage) {
            HSGlobalNotificationCenter.sendNotification(ALL_PERMISSION_GRANT);
        } else {
            Threads.postOnMainThreadDelayed(() -> {
                HSLog.w(TAG, "dismiss float window num == " + progressNum);
                AutoRequestManager.getInstance().dismissCoverWindow();
            }, 1200);
        }
        HSGlobalNotificationCenter.removeObserver(this);
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
    }

    private long getNextUpgradeDelay() {
        if (goalNum == PROGRESS_MAX_VALUE) {
            progressInterval = UPGRADE_MIN_INTERVAL;
        } else if (goalNum - progressNum > 15) {
            progressInterval *= 0.8;
        } else if (progressNum - goalNum > 15) {
            progressInterval *= 1.3;
        }
        return Math.min(Math.max(progressInterval, UPGRADE_MIN_INTERVAL), UPGRADE_MAX_INTERVAL);
    }

    private void setPermissionStatus(@PERMISSION_TYPES int pType, @PERMISSION_STATUS int pStatus) {
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
        if (pStatus < lastStatus) {
            HSLog.i(TAG, "setPermissionStatus C == " + isConfirmPage + "  pt == " + pType + "  ps == " + pStatus + "  last == " + lastStatus);
            return;
        }

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
                            HSLog.i(TAG, "onAnimationRepeat " + pType);

                            loading.removeAllAnimatorListeners();
                            loading.addAnimatorListener(new AnimatorListenerAdapter() {
                                @Override public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    ok.setVisibility(View.VISIBLE);
                                    ok.setImageResource(R.drawable.start_guide_confirm_ok_image);
                                    loading.removeAllAnimatorListeners();
                                    loading.setVisibility(View.GONE);
                                    setPermissionStatus(pType + 1, PERMISSION_STATUS_LOADING);
                                    HSLog.i(TAG, "onAnimationEnd " + pType);

                                    ok.animate().alpha(0.3f).setDuration(100).start();
                                    text.animate().alpha(0.3f).setDuration(100).start();
                                }
                            });
                        }
                    });
                } else {
                    if (loading != null) {
                        loading.setVisibility(View.GONE);
                    }
                    ok.setVisibility(View.VISIBLE);
                    ok.setImageResource(R.drawable.start_guide_confirm_ok_image);
                    ok.animate().alpha(0.3f).setDuration(100).start();
                    text.animate().alpha(0.3f).setDuration(100).start();
                    setPermissionStatus(pType + 1, PERMISSION_STATUS_LOADING);
                }

                if (fix != null) {
                    fix.setVisibility(View.GONE);
                }
                break;
        }
        ok.setTag(pStatus);
    }
}
