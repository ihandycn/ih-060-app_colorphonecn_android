package com.colorphone.lock.lockscreen.chargingscreen;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.colorphone.lock.LockerCustomConfig;
import com.honeycomb.colorphone.R;
import com.colorphone.lock.lockscreen.chargingscreen.view.BatteryAnimatorHelper;
import com.colorphone.lock.lockscreen.chargingscreen.view.FlashButton;
import com.colorphone.lock.lockscreen.locker.LockerSettings;
import com.colorphone.lock.util.ViewUtils;
import com.ihs.commons.config.HSConfig;
import com.superapps.util.Dimensions;
import com.superapps.util.Preferences;

/**
 * Created by lz on 4/6/17.
 */

public class ChargingScreenGuideView extends LinearLayout {

    private Animator mTransAnimator;
    private BatteryAnimatorHelper batteryAnimatorHelper;
    private FlashButton flashButton;

    public ChargingScreenGuideView(Context context) {
        this(context, null);
    }

    public ChargingScreenGuideView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChargingScreenGuideView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        ImageView appIcon = findViewById(R.id.app_custom_icon);
        appIcon.setImageResource(LockerCustomConfig.get().getCustomScreenIcon());
        
        setPadding(0, 0, 0, Dimensions.getNavigationBarHeight(getContext()));

        ViewUtils.findViewById(this, R.id.ic_close).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ChargingScreenGuideView.this.dismissSelf();
            }
        });

        View batteryLayout = findViewById(R.id.charging_alert_battery_layout);
        final int densityDpi = getResources().getDisplayMetrics().densityDpi;
        if (densityDpi <= DisplayMetrics.DENSITY_HIGH) {
            batteryLayout.setScaleX(0.9f);
            batteryLayout.setScaleY(0.9f);
        }

        final View chargingAlertContent = findViewById(R.id.charging_alert_content);
        batteryAnimatorHelper = new BatteryAnimatorHelper(chargingAlertContent);
        chargingAlertContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    chargingAlertContent.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    chargingAlertContent.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                batteryAnimatorHelper.displayAnimator();
            }
        });

        flashButton = (FlashButton) findViewById(R.id.charging_alert_button);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            flashButton.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        } else {
            flashButton.setTypeface(Typeface.SANS_SERIF);
        }
        flashButton.setRepeatCount(10);
        flashButton.startFlash();

        flashButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ChargingScreenSettings.setChargingScreenEnabled(true);
                if (HSConfig.optBoolean(false, "Application", "Locker", "AutoOpenWhenSwitchOn") && !LockerSettings.isLockerEverEnabled()) {
                    LockerSettings.setLockerEnabled(true);
                }
                Toast.makeText(ChargingScreenGuideView.this.getContext(), R.string.charging_screen_guide_turn_on, Toast.LENGTH_SHORT).show();
                LockerCustomConfig.getLogger().logEvent("Alert_ChargingScreen_TurnOn_Clicked", "type", "Turn on");
                ChargingScreenGuideView.this.dismissSelf();
            }
        });
    }

    private void dismissSelf() {

        onClose();
//        ObjectAnimator slideOut = ObjectAnimator.ofFloat(ChargingScreenGuideView.this, View.TRANSLATION_Y, getMeasuredHeight());
//        slideOut.setDuration(300);
//        slideOut.setInterpolator(new AccelerateInterpolator());
//        slideOut.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                transitionOut(false);
//            }
//        });
//        slideOut.start();
    }

    private Runnable mCloseCallback;
    public void setOnCloseListener(Runnable listener) {
        mCloseCallback = listener;
    }

    private void onClose() {
        if (mCloseCallback != null) {
            mCloseCallback.run();
        }
    }


    public void transitionIn(boolean animated) {
        setTranslationY(getMeasuredHeight());
        setVisibility(View.VISIBLE);
        mTransAnimator = ObjectAnimator.ofFloat(this, View.TRANSLATION_Y, 0);
        mTransAnimator.setDuration(300);
        mTransAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mTransAnimator.start();

        ChargingScreenSettings.increaseChargingScreenGuideShowCount();
        final String spName = LockerCustomConfig.get().getSPFileName();
        Preferences.get(spName).putInt(
                ChargingScreenSettings.PREF_KEY_CHARGING_SCREEN_GUIDE_LAST_SHOW_TIME,
                ChargingScreenSettings.getChargingCount());
    }


    public void transitionOut(boolean animated) {
        if (getParent() == null) {
            return;
        }
        flashButton.stopFlash();
        batteryAnimatorHelper.release();
        ((ViewGroup) getParent()).removeView(this);
    }


    public boolean onBackPressed() {
        dismissSelf();
        return true;
    }



}
