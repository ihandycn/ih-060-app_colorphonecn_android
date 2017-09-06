package com.colorphone.lock.lockscreen.locker.slidingup;

/**
 * Created by lz on 2/23/17.
 */

/**
 * Lifecycle callback of a series of status.
 */
public interface SlidingUpCallback {

    /**
     * Callback when ACTION_DOWN triggered.
     * @param type
     */
    void onActionDown(int type);

    /**
     * Callback when translationY should be changed.
     * @param translationY
     */
    void translateY(int translationY);

    /**
     * Callback when ACTION_CANCEL or ACTION_UP triggered.
     */
    void onActionUp();

    /**
     * Should animate to translationY from current status.
     * @param translationY
     */
    void doStartAnimator(float translationY);

    /**
     * Should do end animator.
     */
    void doEndAnimator();

    /**
     * Should do accelerating end animator.
     * @param duration
     */
    void doAcceleratingEndAnimator(int duration);

    /**
     * Should do success animator. Dismiss the corresponding view.
     * @param duration
     * @param type
     */
    void doSuccessAnimator(int duration, int type);
}
