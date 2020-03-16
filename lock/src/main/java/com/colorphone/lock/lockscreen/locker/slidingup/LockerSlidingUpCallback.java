package com.colorphone.lock.lockscreen.locker.slidingup;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;

import com.colorphone.lock.LockerCustomConfig;
import com.colorphone.lock.R;
import com.colorphone.lock.lockscreen.locker.Locker;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSBundle;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;

import static com.ihs.app.framework.HSApplication.getContext;

public class LockerSlidingUpCallback implements SlidingUpCallback {

    private static final int DURATION_START_ANIMATION = 300;
    private static final int DURATION_END_ANIMATION = 500;

    Animator mAnimator;
    private boolean mIsTurnBack = false;

    private ViewGroup mTransitionContainer;
    ViewGroup mBottomLayer;
    private ImageView mBottomIcon;
    private int mNavigationBarHeight;

    public LockerSlidingUpCallback(Locker activity) {
        ViewGroup rootView = activity.getRootView();
        mTransitionContainer = (ViewGroup) rootView.findViewById(R.id.transition_container);
        mBottomLayer = (ViewGroup) rootView.findViewById(R.id.bottom_layer);
        mBottomIcon = (ImageView) rootView.findViewById(R.id.bottom_icon);
        mNavigationBarHeight = Dimensions.getNavigationBarHeight(getContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mBottomIcon.setPadding(0, 0, 0, mNavigationBarHeight);
        }
    }

    @Override
    public void onActionDown(int type) {
        mTransitionContainer.requestDisallowInterceptTouchEvent(true);
        mBottomIcon.setImageResource(type == SlidingUpTouchListener.TYPE_LEFT ?
                R.drawable.main_wallpaper_up : R.drawable.main_camera);
    }

    @Override
    public void translateY(int translationY) {
        if (translationY < -(Dimensions.pxFromDp(50) + mNavigationBarHeight)) {
            if (mAnimator != null && mAnimator.isRunning()) {
                mAnimator.cancel();
            }
            mTransitionContainer.setTranslationY(translationY);
        } else {
            if (null == mAnimator || !mAnimator.isRunning()) {
                mTransitionContainer.setTranslationY(-Dimensions.pxFromDp(50) - mNavigationBarHeight);
            }
        }
    }

    @Override
    public void onActionUp() {
        mTransitionContainer.requestDisallowInterceptTouchEvent(false);
    }

    @Override
    public void doStartAnimator(float translationY) {
        if (null != mAnimator) {
            mAnimator.cancel();
        }
        mAnimator = ObjectAnimator.ofFloat(mTransitionContainer, View.TRANSLATION_Y, translationY);
        mAnimator.setInterpolator(new DecelerateInterpolator());
        mAnimator.setDuration(DURATION_START_ANIMATION);
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mIsTurnBack) {
                    mIsTurnBack = false;
                    runEndAnimator();
                }
            }
        });
        mAnimator.start();
    }

    @Override
    public void doEndAnimator() {
        if (mAnimator != null && mAnimator.isRunning()) {
            mIsTurnBack = true;
        } else {
            runEndAnimator();
        }
    }

    private void runEndAnimator() {
        if (mAnimator != null) {
            mAnimator.cancel();
        }
        mAnimator = ObjectAnimator.ofFloat(mTransitionContainer, View.TRANSLATION_Y, 0);
        mAnimator.setInterpolator(new BounceInterpolator());
        mAnimator.setDuration(DURATION_END_ANIMATION);
        mAnimator.start();
    }

    @Override
    public void doAcceleratingEndAnimator(int duration) {
        if (mAnimator != null) {
            mAnimator.cancel();
        }
        mAnimator = ObjectAnimator.ofFloat(mTransitionContainer, View.TRANSLATION_Y, 0);
        mAnimator.setInterpolator(new AccelerateWithStartSpeedInterpolator());
        mAnimator.setDuration(duration);
        mAnimator.start();
    }

    @Override
    public void doSuccessAnimator(int duration, final int type) {
        if (null != mAnimator) {
            mAnimator.cancel();
        }

        // do not know why duration is negative, ignore.
        if (duration < 0) {
            return;
        }

        mAnimator = ObjectAnimator.ofFloat(mTransitionContainer, View.TRANSLATION_Y, -mTransitionContainer.getHeight());
        mAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mAnimator.setDuration(duration);
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (type == SlidingUpTouchListener.TYPE_LEFT) {
                    HSBundle bundle = new HSBundle();
                    bundle.putString(Locker.EXTRA_DISMISS_REASON, "Slide");
                    HSGlobalNotificationCenter.sendNotification(Locker.EVENT_FINISH_SELF, bundle);
                } else {
                    if (Navigations.startCamera(HSApplication.getContext())) {
                        HSBundle bundle = new HSBundle();
                        bundle.putString(Locker.EXTRA_DISMISS_REASON, "CameraClick");
                        HSGlobalNotificationCenter.sendNotification(Locker.EVENT_FINISH_SELF, bundle);
                    }
                }
            }
        });
        mAnimator.start();
    }

    static class AccelerateWithStartSpeedInterpolator implements Interpolator {
        @Override
        public float getInterpolation(float input) {
            return 0.5f * input * input + 0.5f * input;
        }
    }
}
