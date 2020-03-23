package com.colorphone.smartlocker.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.colorphone.lock.R;

public class RefreshViewHeader extends LinearLayout {
    private static long DURATION_REFRESH = 600L;

    private static final int MSG_REPEAT = 0;

    private ImageView rocketImageView;
    private ImageView rocketShadowImageView;

    private FlyLineView flyLineView;

    private boolean isStateRefreshing = false;
    private double headerMovePercentOnStartRefresh;

    private boolean isNeedRepeatRocketShakeAnim;

    private ObjectAnimator rocketShakeAnim;
    private ObjectAnimator rocketShadowShakeAnim;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REPEAT:
                    if (isNeedRepeatRocketShakeAnim) {
                        rocketShakeAnim.start();
                        rocketShadowShakeAnim.start();
                        handler.sendEmptyMessageDelayed(MSG_REPEAT, DURATION_REFRESH);
                    } else {
                        rocketMoveOutAnim();
                    }
                    break;
            }

        }
    };

    public RefreshViewHeader(Context context) {
        super(context);
        initView(context);
    }

    public RefreshViewHeader(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public RefreshViewHeader(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.refreshview_header, this);
        rocketImageView = findViewById(R.id.header_rocket);
        rocketShadowImageView = findViewById(R.id.header_rocket_shadow);
        flyLineView = findViewById(R.id.fly_line);
    }

    public void show() {
        setVisibility(View.VISIBLE);
    }

    public void onStateNormal() {
        rocketImageView.setVisibility(View.VISIBLE);
    }

    public void onStateReady() {
        rocketImageView.setVisibility(View.VISIBLE);
    }

    public void onStateRefreshing() {
        rocketShadowImageView.setVisibility(VISIBLE);
        rocketShadowImageView.setAlpha(0f);

        isStateRefreshing = true;
        isNeedRepeatRocketShakeAnim = true;
    }

    public void onStateFinish() {
        flyLineView.stopAnim();
        isNeedRepeatRocketShakeAnim = false;
    }

    public void onHeaderMove(double headerMovePercent) {
        rocketImageView.setScaleX((float) (headerMovePercent));
        rocketImageView.setScaleY((float) (headerMovePercent));

        if (isStateRefreshing) {
            if (headerMovePercentOnStartRefresh == 0) {
                headerMovePercentOnStartRefresh = headerMovePercent;
            }

            rocketShadowImageView.setAlpha((float) (1 - (headerMovePercent - 1) / (headerMovePercentOnStartRefresh - 1)));

            rocketShadowImageView.setScaleX((float) (headerMovePercent));
            rocketShadowImageView.setScaleY((float) (headerMovePercent));

            if (headerMovePercent == 1) {
                startRefreshAnim();
            }
        }
    }

    private void startRefreshAnim() {
        rocketShakeAnim = ObjectAnimator.ofFloat(rocketImageView, "translationY", 0f, 30f, 0f);
        rocketShadowShakeAnim = ObjectAnimator.ofFloat(rocketShadowImageView, "translationY", 0f, 30f, 0f);

        rocketShakeAnim.setInterpolator(new LinearInterpolator());
        rocketShakeAnim.setDuration(DURATION_REFRESH);
        rocketShadowShakeAnim.setInterpolator(new LinearInterpolator());
        rocketShadowShakeAnim.setDuration(DURATION_REFRESH);

        flyLineView.startAnim();

        handler.sendEmptyMessage(MSG_REPEAT);
    }

    private void rocketMoveOutAnim() {
        final ObjectAnimator rocketMoveOutAnim = ObjectAnimator.ofFloat(rocketImageView, "translationY", 0f, -300f);
        final ObjectAnimator rocketShadowMoveOutAnim = ObjectAnimator.ofFloat(rocketShadowImageView, "translationY", 0f, -300f);
        final AnimatorSet moveOutAnimatorSet = new AnimatorSet();
        moveOutAnimatorSet.playTogether(rocketMoveOutAnim, rocketShadowMoveOutAnim);
        moveOutAnimatorSet.setInterpolator(new AccelerateInterpolator());
        moveOutAnimatorSet.setDuration(600L);
        moveOutAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                rocketImageView.setTranslationY(0f);
                rocketImageView.setVisibility(GONE);
                rocketShadowImageView.setTranslationY(0f);
                rocketShadowImageView.setVisibility(GONE);

                isStateRefreshing = false;
                headerMovePercentOnStartRefresh = 0;

            }
        });
        moveOutAnimatorSet.start();
    }

    public int getHeaderHeight() {
        return getMeasuredHeight();
    }
}
