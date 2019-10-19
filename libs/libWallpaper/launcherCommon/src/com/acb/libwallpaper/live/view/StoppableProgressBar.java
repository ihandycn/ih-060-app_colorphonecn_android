package com.acb.libwallpaper.live.view;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;

 import com.honeycomb.colorphone.R;
import com.ihs.commons.utils.HSLog;

/**
 * A indeterminate progress bar that can be stopped and resumed.
 */
public class StoppableProgressBar extends AppCompatImageView {

    AnimationSet mRotatingAnim;
    private boolean mStopRequested;

    public StoppableProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mRotatingAnim = (AnimationSet) AnimationUtils.loadAnimation(getContext(), R.anim.rotate_fast);
        for (Animation anim : mRotatingAnim.getAnimations()) {
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    HSLog.i("Weather.Animation", "Progress bar repeat");
                    if (mStopRequested) {
                        setAnimation(null);
                        mRotatingAnim.reset();
                    }
                }
            });
        }
        requestStop();
    }

    public void start() {
        mStopRequested = false;
        startAnimation(mRotatingAnim);
    }

    public void requestStop() {
        mStopRequested = true;
    }
}
