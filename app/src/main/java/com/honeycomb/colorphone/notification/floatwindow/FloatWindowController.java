package com.honeycomb.colorphone.notification.floatwindow;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;

import com.honeycomb.colorphone.notification.NotificationAutoPilotUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;


public class FloatWindowController {

    private volatile static FloatWindowController instance;

    public static FloatWindowController getInstance() {
        if (instance == null) {
            synchronized (FloatWindowController.class) {
                if (instance == null) {
                    instance = new FloatWindowController();
                }
            }
        }
        return instance;
    }

    private WindowManager windowManager;
    private Handler handler;
    private UsageAccessTip usageAccessTip;
    private boolean isShown = false;

    private FloatWindowController() {
        windowManager = (WindowManager) HSApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
        handler = new Handler();
    }

    public void showTip(View tip, boolean bottom) {
        try {
            windowManager.addView(tip, generateWindowLayoutParams(bottom));
        } catch (Exception e) {
            HSLog.e(e.getMessage());
        }
    }

    public void removeTip(View tip) {
        try {
            windowManager.removeViewImmediate(tip);
        } catch (Exception e) {
            HSLog.e(e.getMessage());
        }
    }

    public void createUsageAccessTip(Context context) {
        createUsageAccessTip(context, null);
    }

    public void createUsageAccessTip(Context context, String desc) {
        try {
            usageAccessTip = new UsageAccessTip(context);
            usageAccessTip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (usageAccessTip != null && isShown) {
                        removeUsageAccessTip();
                    }
                }
            });
            if (!TextUtils.isEmpty(desc)) {
                usageAccessTip.setDescText(desc);
            }

            WindowManager.LayoutParams usageAccessTipWindowParams = generateWindowLayoutParams(
                    NotificationAutoPilotUtils.isNotificationAccessTipAtBottom());

            windowManager.addView(usageAccessTip, usageAccessTipWindowParams);
            isShown = true;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    public static WindowManager.LayoutParams generateWindowLayoutParams(boolean bottom) {
        WindowManager.LayoutParams usageAccessTipWindowParams = new WindowManager.LayoutParams();
        usageAccessTipWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        usageAccessTipWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        usageAccessTipWindowParams.format = PixelFormat.TRANSLUCENT;
        // In HuaWei System Settings - Notification Center - Dropzones, Default block app float window but TYPE_TOAST
        // TYPE_TOAST float window will dissmiss above api 25
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            usageAccessTipWindowParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            usageAccessTipWindowParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        } else {
            usageAccessTipWindowParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }

        if(bottom) {
            usageAccessTipWindowParams.gravity = Gravity.BOTTOM;
        } else {
            usageAccessTipWindowParams.gravity = Gravity.CENTER;
        }

        usageAccessTipWindowParams.flags |= WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        return usageAccessTipWindowParams;
    }

    public void removeUsageAccessTipWithAnimation() {
        if (usageAccessTip != null) {
            try {
                ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
                animator.setDuration(400);
                animator.setInterpolator(new DecelerateInterpolator());
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        handler.removeCallbacksAndMessages(null);
                        if (usageAccessTip != null && isShown) {
                            windowManager.removeViewImmediate(usageAccessTip);
                            usageAccessTip = null;
                            isShown = false;
                        }
                    }
                });
                animator.start();
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }
    }

    public void removeUsageAccessTip() {
        if (usageAccessTip != null && isShown) {
            try {
                handler.removeCallbacksAndMessages(null);
                windowManager.removeViewImmediate(usageAccessTip);
                usageAccessTip = null;
                isShown = false;
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }
    }
}
