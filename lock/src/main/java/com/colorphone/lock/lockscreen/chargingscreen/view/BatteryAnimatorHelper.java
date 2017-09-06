package com.colorphone.lock.lockscreen.chargingscreen.view;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Handler;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.colorphone.lock.R;
import com.ihs.app.framework.HSApplication;

/**
 * Created by zhouzhenliang on 17/2/8.
 */

public class BatteryAnimatorHelper {

    private static final float WAVE_AMPLITUDE_RATIO = 0.03f;
    private static final float WAVE_WATER_LEVEL_RATIO = 0.8f;

    private static final long INTERVAL_DISPLAY_BLISTER_ANIMATOR = 2000L;

    private WaterWaveView waterWaveView;

    private View chargingAlertBlister1;
    private View chargingAlertBlister2;
    private View chargingAlertBlister3;

    private ValueAnimator waveValueAnimator;

    private Handler handler;

    private boolean isStarted;

    private Runnable startBlisterAnimatorRunnable = new Runnable() {
        @Override
        public void run() {
            final float startTranslationY = waterWaveView.getHeight() * (1f - WAVE_WATER_LEVEL_RATIO);

            ObjectAnimator transYBlister1Animator = ObjectAnimator.ofFloat(chargingAlertBlister1, "translationY", startTranslationY,
                startTranslationY - HSApplication.getContext().getResources().getDimensionPixelSize(R.dimen.smart_charging_spread_blister1_trans_y));
            ObjectAnimator alphaBlister1Animator = ObjectAnimator.ofFloat(chargingAlertBlister1, "alpha", 1, 0);
            AnimatorSet animatorSet1 = new AnimatorSet();
            animatorSet1.setDuration(1640);
            animatorSet1.setInterpolator(new LinearOutSlowInInterpolator());
            animatorSet1.playTogether(transYBlister1Animator, alphaBlister1Animator);
            animatorSet1.start();

            ObjectAnimator transYBlister2Animator = ObjectAnimator.ofFloat(chargingAlertBlister2, "translationY", startTranslationY,
                startTranslationY - HSApplication.getContext().getResources().getDimensionPixelSize(R.dimen.smart_charging_spread_blister2_trans_y));
            ObjectAnimator alphaBlister2Animator = ObjectAnimator.ofFloat(chargingAlertBlister2, "alpha", 1, 0);
            AnimatorSet animatorSet2 = new AnimatorSet();
            animatorSet2.setDuration(1640 - 440);
            animatorSet2.setInterpolator(new LinearOutSlowInInterpolator());
            animatorSet2.playTogether(transYBlister2Animator, alphaBlister2Animator);
            animatorSet2.setStartDelay(440);
            animatorSet2.start();

            ObjectAnimator transYBlister3Animator = ObjectAnimator.ofFloat(chargingAlertBlister3, "translationY", startTranslationY,
                startTranslationY - HSApplication.getContext().getResources().getDimensionPixelSize(R.dimen.smart_charging_spread_blister3_trans_y));
            ObjectAnimator alphaBlister3Animator = ObjectAnimator.ofFloat(chargingAlertBlister3, "alpha", 1, 0);
            AnimatorSet animatorSet3 = new AnimatorSet();
            animatorSet3.setDuration(1640 - 160);
            animatorSet3.setInterpolator(new LinearOutSlowInInterpolator());
            animatorSet3.playTogether(transYBlister3Animator, alphaBlister3Animator);
            animatorSet3.setStartDelay(160);
            animatorSet3.start();

            handler.postDelayed(this, INTERVAL_DISPLAY_BLISTER_ANIMATOR);
        }
    };

    public BatteryAnimatorHelper(View chargingAlertContent) {
        this.handler = new Handler();

        waterWaveView = (WaterWaveView) chargingAlertContent.findViewById(R.id.charging_alert_wave_view);
        chargingAlertBlister1 = chargingAlertContent.findViewById(R.id.charging_alert_blister1);
        chargingAlertBlister2 = chargingAlertContent.findViewById(R.id.charging_alert_blister2);
        chargingAlertBlister3 = chargingAlertContent.findViewById(R.id.charging_alert_blister3);

        waterWaveView.setWaveColor(0xFFADC8F8, 0xFF376FEC);
        waterWaveView.setAmplitudeRatio(WAVE_AMPLITUDE_RATIO);
        waterWaveView.setWaterLevelRatio(WAVE_WATER_LEVEL_RATIO);
        waterWaveView.setShowWave(true);

        waveValueAnimator = ValueAnimator.ofFloat(1f, 0f);
        waveValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        waveValueAnimator.setDuration(1600);
        waveValueAnimator.setInterpolator(new LinearInterpolator());

        waveValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                waterWaveView.setWaveShiftRatio((float) animation.getAnimatedValue());
            }
        });
    }

    public void displayAnimator() {
        if (isStarted) {
            return;
        }
        isStarted = true;

        waveValueAnimator.start();
        handler.post(startBlisterAnimatorRunnable);
    }

    public void release() {
        if (!isStarted) {
            return;
        }
        isStarted = false;

        waveValueAnimator.cancel();
        handler.removeCallbacks(startBlisterAnimatorRunnable);
    }
}
