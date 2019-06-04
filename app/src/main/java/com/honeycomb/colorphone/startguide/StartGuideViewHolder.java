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
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.activity.StartGuideActivity;
import com.honeycomb.colorphone.autopermission.AutoPermissionChecker;
import com.honeycomb.colorphone.autopermission.AutoRequestManager;
import com.honeycomb.colorphone.util.Analytics;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.permission.HSPermissionRequestMgr;
import com.ihs.permission.Utils;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class StartGuideViewHolder implements INotificationObserver {

    public static final int TYPE_PERMISSION_TYPE_SCREEN_FLASH = 1;
    public static final int TYPE_PERMISSION_TYPE_ON_LOCK = 2;
    public static final int TYPE_PERMISSION_TYPE_CALL = 3;
    public static final int TYPE_PERMISSION_TYPE_BG_POP = 4;

    public static final int PERMISSION_STATUS_HIDE = -1;
    public static final int PERMISSION_STATUS_NOT_START = 0;
    public static final int PERMISSION_STATUS_LOADING = 1;
    public static final int PERMISSION_STATUS_FAILED = 2;
    public static final int PERMISSION_STATUS_FIX = 3;
    public static final int PERMISSION_STATUS_OK = 4;

    @IntDef({TYPE_PERMISSION_TYPE_SCREEN_FLASH,
            TYPE_PERMISSION_TYPE_ON_LOCK,
            TYPE_PERMISSION_TYPE_CALL, 
            TYPE_PERMISSION_TYPE_BG_POP})
    @Retention(RetentionPolicy.SOURCE)
    private @interface PERMISSION_TYPES {
    }

    @IntDef({PERMISSION_STATUS_HIDE,
            PERMISSION_STATUS_NOT_START,
            PERMISSION_STATUS_LOADING,
            PERMISSION_STATUS_OK,
            PERMISSION_STATUS_FAILED,
            PERMISSION_STATUS_FIX
    })
    @Retention(RetentionPolicy.SOURCE)
    private @interface PERMISSION_STATUS {
    }

    private static final String TAG = "AutoPermission";
    private static final int CIRCLE_SPEED = 360;
    private static final int UPGRADE_MIN_INTERVAL = 25;
    private static final int UPGRADE_MAX_INTERVAL = UPGRADE_MIN_INTERVAL * 20;
    private static final int PROGRESS_MAX_VALUE = 100;
    private static final int EVENT_UPGRADE = 5000;

    /**
     *
     */
    private boolean isConfirmPage = true;
    private View container;
    private View circleAnimView;
    private ValueAnimator circleAnimator;

    private ImageView screenFlashOK;
    private LottieAnimationView screenFlashLoading;
    private View screenFlashText;
    private boolean gotoFetchScreenFlash = false;

    private ImageView onLockerOK;
    private LottieAnimationView onLockerLoading;
    private View onLockerText;
    private boolean gotoFetchOnLock = false;

    private ImageView callOK;
    private LottieAnimationView callLoading;
    private View callText;
    public boolean gotoFetchCall = false;

    private ImageView bgPopOK;
    private LottieAnimationView bgPopLoading;
    private View bgPopText;
    public boolean gotoFetchBgPop = false;

    private View oneKeyFix;
    private View skip;
    private boolean showSkipDialog = true;

    private TextView progress;
    private int progressNum;
    private int goalNum = 0;
    private int progressInterval = UPGRADE_MIN_INTERVAL * 2;
    private int finalStatus;

    private SparseIntArray permissionList = new SparseIntArray();

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
                                finish(1000);
                                interval = 0;
                            } else {
                                progressNum = PROGRESS_MAX_VALUE - 1;
                            }
                        }

                        if (interval != 0) {
                            handler.sendEmptyMessageDelayed(EVENT_UPGRADE, interval);
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

        // Init status
        permissionList.append(TYPE_PERMISSION_TYPE_SCREEN_FLASH, isConfirmPage ? PERMISSION_STATUS_FIX : PERMISSION_STATUS_LOADING);
        permissionList.append(TYPE_PERMISSION_TYPE_ON_LOCK, isConfirmPage ? PERMISSION_STATUS_FIX : PERMISSION_STATUS_LOADING);
        permissionList.append(TYPE_PERMISSION_TYPE_CALL, isConfirmPage ? PERMISSION_STATUS_FIX : PERMISSION_STATUS_LOADING);
        permissionList.append(TYPE_PERMISSION_TYPE_BG_POP, isConfirmPage ? PERMISSION_STATUS_FIX : PERMISSION_STATUS_LOADING);

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
        
        bgPopText = container.findViewById(R.id.start_guide_permission_bg_pop);
        bgPopOK = container.findViewById(R.id.start_guide_permission_bg_pop_ok);
        bgPopOK.setTag(PERMISSION_STATUS_NOT_START);
        bgPopLoading = container.findViewById(R.id.start_guide_permission_bg_pop_loading);

        if (isConfirmPage) {

            refresh();
        } else {
            progress = container.findViewById(R.id.start_guide_request_progress);

            setPermissionStatus(TYPE_PERMISSION_TYPE_SCREEN_FLASH,
                    AutoPermissionChecker.hasAutoStartPermission() ? PERMISSION_STATUS_OK : PERMISSION_STATUS_LOADING);
            setPermissionStatus(TYPE_PERMISSION_TYPE_ON_LOCK,
                    AutoPermissionChecker.hasShowOnLockScreenPermission() ? PERMISSION_STATUS_OK : PERMISSION_STATUS_LOADING);
            setPermissionStatus(TYPE_PERMISSION_TYPE_CALL,
                    AutoPermissionChecker.isAccessibilityGranted() ? PERMISSION_STATUS_OK : PERMISSION_STATUS_LOADING);
            setPermissionStatus(TYPE_PERMISSION_TYPE_BG_POP,
                    AutoPermissionChecker.hasBgPopupPermission() ? PERMISSION_STATUS_OK : PERMISSION_STATUS_LOADING);
        }

        HSGlobalNotificationCenter.addObserver(AutoRequestManager.NOTIFICATION_PERMISSION_RESULT, this);
        HSGlobalNotificationCenter.addObserver(AutoRequestManager.NOTIFY_PERMISSION_CHECK_FINISH, this);

    }

    public int refresh() {
        int confirmPermission = 0;
        if (!isConfirmPage) {
            return confirmPermission;
        }

        boolean screenFlashGrant = AutoPermissionChecker.hasAutoStartPermission();
        boolean onLockGrant = AutoPermissionChecker.hasShowOnLockScreenPermission();
        boolean callGrant = Utils.isNotificationListeningGranted();
        boolean bgPopGrant = AutoPermissionChecker.hasBgPopupPermission();
        setPermissionStatus(TYPE_PERMISSION_TYPE_SCREEN_FLASH, screenFlashGrant ? PERMISSION_STATUS_OK : PERMISSION_STATUS_FIX);
        setPermissionStatus(TYPE_PERMISSION_TYPE_ON_LOCK, onLockGrant ? PERMISSION_STATUS_OK : PERMISSION_STATUS_FIX);
        setPermissionStatus(TYPE_PERMISSION_TYPE_CALL, callGrant ? PERMISSION_STATUS_OK : PERMISSION_STATUS_FIX);
        setPermissionStatus(TYPE_PERMISSION_TYPE_BG_POP, bgPopGrant ? PERMISSION_STATUS_OK : PERMISSION_STATUS_FIX);

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

        if (!bgPopGrant) {
            notGrant++;
        }

        if (notGrant == 0) {
            finish(1000);
        }

        if (gotoFetchCall) {
            gotoFetchCall = false;
            confirmPermission = TYPE_PERMISSION_TYPE_CALL;
        }

        if (gotoFetchScreenFlash) {
            gotoFetchScreenFlash = false;
            confirmPermission = TYPE_PERMISSION_TYPE_SCREEN_FLASH;
        }

        if (gotoFetchOnLock) {
            gotoFetchOnLock = false;
            confirmPermission = TYPE_PERMISSION_TYPE_ON_LOCK;
        }

        if (gotoFetchBgPop) {
            gotoFetchBgPop = false;
            confirmPermission = TYPE_PERMISSION_TYPE_BG_POP;
        }

        TextView ball = container.findViewById(R.id.start_guide_confirm_number);
        TextView title = container.findViewById(R.id.start_guide_permission_title);
        ball.setText(String.valueOf(notGrant));
        title.setText(String.format(container.getContext().getString(R.string.start_guide_permission_title), String.valueOf(notGrant)));
        return confirmPermission;
    }

    public boolean isManualFix() {
        return gotoFetchOnLock || gotoFetchScreenFlash || gotoFetchCall || gotoFetchBgPop;
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
                    circleAnimator.setInterpolator(new LinearInterpolator());
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
            String pType = hsBundle.getString(AutoRequestManager.BUNDLE_PERMISSION_TYPE);
            boolean result = hsBundle.getBoolean(AutoRequestManager.BUNDLE_PERMISSION_RESULT);
            int status = result ? PERMISSION_STATUS_OK : isConfirmPage ? PERMISSION_STATUS_FIX : PERMISSION_STATUS_FAILED;
            switch (pType) {
                case HSPermissionRequestMgr.TYPE_AUTO_START:
                    updateProgress(TYPE_PERMISSION_TYPE_SCREEN_FLASH, status);
                    HSLog.w(TAG, "cast time 11 " + (System.currentTimeMillis() - startAutoRequestAnimation) + "  num == " + progressNum);
                    break;
                case HSPermissionRequestMgr.TYPE_SHOW_ON_LOCK:
                    updateProgress(TYPE_PERMISSION_TYPE_ON_LOCK, status);
                    HSLog.w(TAG, "cast time 22 " + (System.currentTimeMillis() - startAutoRequestAnimation) + "  num == " + progressNum);
                    break;
                case HSPermissionRequestMgr.TYPE_NOTIFICATION_LISTENING:
                    updateProgress(TYPE_PERMISSION_TYPE_CALL, status);
                    HSLog.w(TAG, "cast time 33 " + (System.currentTimeMillis() - startAutoRequestAnimation) + "  num == " + progressNum);
                    break;
                case AutoRequestManager.TYPE_CUSTOM_BACKGROUND_POPUP:
                    updateProgress(TYPE_PERMISSION_TYPE_BG_POP, status);
                    HSLog.w(TAG, "cast time 44 " + (System.currentTimeMillis() - startAutoRequestAnimation) + "  num == " + progressNum);
                    break;

                default:
                    break;
            }

            refresh();
        } else if (TextUtils.equals(s, AutoRequestManager.NOTIFY_PERMISSION_CHECK_FINISH)) {
            if (!isConfirmPage) {
                goalNum = PROGRESS_MAX_VALUE;
            }
        }
    }

    private void updateProgress(int pType, int status) {
        goalNum += 25;
        progressInterval = (1500 / (PROGRESS_MAX_VALUE - progressNum));
        setPermissionStatus(pType, status);
    }

    private void finish(long delay) {
        HSLog.w(TAG, "finish num == " + progressNum);

        if (handler != null) {
            handler.removeMessages(EVENT_UPGRADE);
            handler = null;
        }

        if (!isConfirmPage) {
            Threads.postOnMainThreadDelayed(() -> {
                HSLog.w(TAG, "dismiss float window num == " + progressNum);
                HSGlobalNotificationCenter.sendNotification(AutoRequestManager.NOTIFY_PERMISSION_CHECK_FINISH_AND_CLOSE_WINDOW);
                AutoRequestManager.getInstance().dismissCoverWindow();
            }, delay);
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
        } else if (progressNum - goalNum > 20) {
            progressInterval *= 1.3;
        }
        return Math.min(Math.max(progressInterval, UPGRADE_MIN_INTERVAL), UPGRADE_MAX_INTERVAL);
    }

    private void setPermissionStatus(@PERMISSION_TYPES int pType, @PERMISSION_STATUS int pStatus) {
        ImageView ok;
        LottieAnimationView loading;
        View text;
        switch (pType) {
            case TYPE_PERMISSION_TYPE_CALL:
                ok = callOK;
                loading = callLoading;
                text = callText;
                break;
            case TYPE_PERMISSION_TYPE_ON_LOCK:
                ok = onLockerOK;
                loading = onLockerLoading;
                text = onLockerText;
                break;
            case TYPE_PERMISSION_TYPE_SCREEN_FLASH:
                ok = screenFlashOK;
                loading = screenFlashLoading;
                text = screenFlashText;
                break;
            case TYPE_PERMISSION_TYPE_BG_POP:
                ok = bgPopOK;
                loading = bgPopLoading;
                text = bgPopText;
                break;
            default:

                return;
        }

        // Check end status.
        permissionList.put(pType, pStatus);
        boolean shouldFinish = true;
        if (isConfirmPage) {
            for (int i = 0; i < permissionList.size() ; i++) {
               int grantedResult = permissionList.valueAt(i);
               if (grantedResult != PERMISSION_STATUS_OK) {
                   shouldFinish = false;
               }
            }
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
                ok.setVisibility(View.VISIBLE);
                ok.setImageResource(R.drawable.start_guide_confirm_alert_image);
                break;
            case PERMISSION_STATUS_LOADING:
                if (lastStatus != PERMISSION_STATUS_OK) {
                    if (loading != null) {
                        ok.setVisibility(View.GONE);
                        loading.setVisibility(View.VISIBLE);
                        loading.useHardwareAcceleration();
                        loading.playAnimation();
                    }
                }
                break;
            case PERMISSION_STATUS_NOT_START:
                ok.setVisibility(View.VISIBLE);
                ok.setImageResource(R.drawable.start_guide_confirm_alert_image);
                if (loading != null) {
                    loading.setVisibility(View.GONE);
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
                        loading.cancelAnimation();
                        loading.setAnimation("lottie/start_guide/permission_done.json");
                        loading.setRepeatCount(0);
                        loading.playAnimation();
                        HSLog.i(TAG, "only done animation " + pType);

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
                    } else {
                        ok.setVisibility(View.VISIBLE);
                        ok.setImageResource(R.drawable.start_guide_confirm_ok_image);

                        ok.animate().alpha(0.3f).setDuration(100).start();
                        text.animate().alpha(0.3f).setDuration(100).start();
                    }
                }

                break;
        }
        ok.setTag(pStatus);
    }
}
