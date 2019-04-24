package com.honeycomb.colorphone.startguide;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.boost.FloatWindowManager;
import com.honeycomb.colorphone.boost.FullScreenDialog;
import com.honeycomb.colorphone.boost.SafeWindowManager;
import com.honeycomb.colorphone.dialer.ConfigEvent;
import com.honeycomb.colorphone.dialer.util.DefaultPhoneUtils;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.FontUtils;
import com.ihs.permission.HSPermissionRequestCallback;
import com.ihs.permission.HSPermissionRequestMgr;
import com.ihs.permission.HSPermissionType;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Threads;

import java.util.ArrayList;

@RequiresApi(api = Build.VERSION_CODES.M)
public class SetAsDialerDialog extends FullScreenDialog {
    private boolean blockDismiss = false;
    LottieAnimationView progressLottie;
    LottieAnimationView successLottie;
    private Button actionBtn;
    private TextView titleView;

    public SetAsDialerDialog(Context context) {
        this(context, null);
    }

    public SetAsDialerDialog(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SetAsDialerDialog(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPage();
    }

    private void initPage() {

        ConfigEvent.guideShow();

        titleView = findViewById(R.id.typefacedTextView);
        actionBtn = findViewById(R.id.guide_action);
        actionBtn.setTypeface(FontUtils.getTypeface(FontUtils.Font.PROXIMA_NOVA_BOLD));
        actionBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(Color.parseColor("#dcdcdc"),
                Color.parseColor("#55000000"),
                Dimensions.pxFromDp(22),
                false,
                true));
        actionBtn.setOnClickListener(v ->
        {
            ConfigEvent.guideConfirmed();
            startAutoTask();
        });

        findViewById(R.id.guide_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConfigEvent.guideClose();
                dismiss();
            }
        });
        setAlpha(0.1f);
        animate().alpha(1f).setDuration(200).start();

        progressLottie = findViewById(R.id.guide_setting_progress);
        successLottie = findViewById(R.id.guide_setting_success);
    }

    private void startAutoTask() {
        blockDismiss = true;
        // Disable touch
        FloatWindowManager.getInstance().updateDialog(this, super.getLayoutParams());

        showProgress();

        ArrayList<HSPermissionType> permission = new ArrayList<HSPermissionType>();
        permission.add(HSPermissionType.TYPE_DEFAULT_DAILER);
        HSPermissionRequestMgr.getInstance().startRequest(permission, new HSPermissionRequestCallback.Stub() {
            @Override
            public void onFinished(int succeedCount, int totalCount) {
                blockDismiss = false;
                if (succeedCount > 0) {
                    Threads.postOnMainThreadDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onAutoRequestSuccess();

                        }
                    }, 300);
                } else {
                    onAutoRequestFail();
                    SetAsDialerDialog.this.dismiss();
                }
            }
        });
    }

    private void showProgress() {
        actionBtn.animate().alpha(0).setDuration(200).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                progressLottie.setVisibility(VISIBLE);
                progressLottie.playAnimation();
            }
        }).start();

    }

    @Override
    public WindowManager.LayoutParams getLayoutParams() {
        WindowManager.LayoutParams wl =  super.getLayoutParams();
        wl.flags &= ~(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        return wl;
    }

    private void onAutoRequestSuccess() {
        // Cancel progress
        actionBtn.animate().cancel();
        progressLottie.setVisibility(INVISIBLE);
        progressLottie.clearAnimation();
        successLottie.setVisibility(VISIBLE);
        successLottie.playAnimation();
        successLottie.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                Threads.postOnMainThreadDelayed(new Runnable() {
                    @Override
                    public void run() {
                        SetAsDialerDialog.this.dismiss();
                    }
                }, 300);
            }
        });

        Threads.postOnMainThreadDelayed(new Runnable() {
            @Override
            public void run() {
                titleView.setText(R.string.guide_default_phone_title_2);
            }
        }, 200);

        Analytics.logEvent("Dialer_Set_Default_From_Automatic");
    }

    private void onAutoRequestFail() {
        DefaultPhoneUtils.checkDefaultPhoneSettings();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.guide_set_default_phone;
    }


    @Override
    public boolean shouldDismissOnLauncherStop() {
        return false;
    }

    @Override
    public void onAddedToWindow(SafeWindowManager windowManager) {

    }

    @Override
    protected boolean IsInitStatusBarPadding() {
        return false;
    }

    @Override
    public void dismiss() {
        if (blockDismiss) {
            // Block
        } else {
            blockDismiss = true;
            SetAsDialerDialog.super.dismiss();
        }
    }

    @Override
    public boolean onBackPressed() {
        dismiss();
        return true;
    }

}
