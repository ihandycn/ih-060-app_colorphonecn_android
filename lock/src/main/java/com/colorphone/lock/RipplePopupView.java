package com.colorphone.lock;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.colorphone.lock.util.CommonUtils;

import java.util.ArrayList;
import java.util.List;

public class RipplePopupView extends PopupView {

    private int mContentX;
    private int mContentY;
    private int mRevealCenterX;
    private int mRevealCenterY;

    private Interpolator mInterpolator;
    private Animator mAppearAnimator;
    private ValueAnimator mAppearcaleAlphaAnimator;
    private Animator mDisappearAnimator;
    private ValueAnimator mDissappearscaleAlphaAnimator;
    private Runnable mCompleteRunnable;

    private boolean mShouldDispatchTouchEvent = true;

    private boolean mIsShowing;

    public RipplePopupView(Activity activity) {
        super(activity);
        init();
    }

    public RipplePopupView(Context context, ViewGroup rootView) {
        super(context, rootView);
        init();
    }

    private void init() {
        mInterpolator = PathInterpolatorCompat.create(.38f, .11f, .23f, 1.01f);
    }

    @Override
    public void setContentView(View contentView) {
        super.setContentView(contentView);

        mAppearcaleAlphaAnimator = ValueAnimator.ofFloat(0, 1);
        mAppearcaleAlphaAnimator.setDuration(280);
        mAppearcaleAlphaAnimator.setInterpolator(mInterpolator);
        mAppearcaleAlphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float scale = 0.2f + 0.8f * animation.getAnimatedFraction();
                float alpha = animation.getAnimatedFraction();
                mContentView.setScaleX(scale);
                mContentView.setScaleY(scale);
                mContentView.setAlpha(alpha);
            }
        });

        mDissappearscaleAlphaAnimator = ValueAnimator.ofFloat(0, 1);
        mDissappearscaleAlphaAnimator.setDuration(180);
        mDissappearscaleAlphaAnimator.setInterpolator(new DecelerateInterpolator(2f));
        mDissappearscaleAlphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float scale = 1 - 0.8f * animation.getAnimatedFraction();
                float alpha = 1 - animation.getAnimatedFraction();
                mContentView.setScaleX(scale);
                mContentView.setScaleY(scale);
                mContentView.setAlpha(alpha);
            }
        });
    }

    @Override
    public void setDropDownAnchor(View anchor) {
        super.setDropDownAnchor(anchor);
    }

    @Override
    public void showAsDropDown(View anchor) {
        mAnchorView = anchor;
        super.showAsDropDown(anchor);
    }

    @Override
    public void showAsDropDown(View anchor, int xOffset, int yOffset) {
        mAnchorView = anchor;
        super.showAsDropDown(anchor, xOffset, yOffset);
    }

    @Override
    protected void showAtPosition(int x, int y) {
        mContentX = x;
        mContentY = y;
        super.showAtPosition(x, y);
    }

    @Override
    protected void show() {
        if(mIsShowing) {
            return;
        }
        mIsShowing = true;
        super.show();
        mAppearAnimator = getAppearAnimation();
        mAppearAnimator.start();
    }

    public void dismiss(Runnable completeRunnable) {
        if (!mIsShowing) {
            return;
        }
        mIsShowing = false;
        if (mContentViewParent.getParent() == null) {
            super.dismiss();
            return;
        }
        mCompleteRunnable = completeRunnable;
        mDisappearAnimator = getDisappearAnimation();
        mDisappearAnimator.start();
    }

    @Override
    public void dismiss() {
        dismiss(null);
    }

    private Animator getAppearAnimation() {
        List<Animator> animatorList = new ArrayList<>();
        int[] anchorCoord = new int[2];
        mAnchorView.getLocationInWindow(anchorCoord);

        int centerX = anchorCoord[0] + mAnchorView.getWidth() / 2;
        int centerY = anchorCoord[1] + mAnchorView.getHeight() / 2;

        mRevealCenterX = centerX - mContentX;
        mRevealCenterY = centerY - mContentY;

        if (CommonUtils.ATLEAST_LOLLIPOP) {
            float finalX = Math.max(Math.abs(mRevealCenterX), Math.abs(mRevealCenterX - mContentView.getWidth()));
            float finalY = Math.max(Math.abs(mRevealCenterY), Math.abs(mRevealCenterY - mContentView.getHeight()));
            float finalRadius = (float) Math.sqrt(finalX * finalX + finalY * finalY);

            Animator circularAnimator = ViewAnimationUtils.createCircularReveal(mContentView, mRevealCenterX, mRevealCenterY, 0, finalRadius);
            circularAnimator.setDuration(280);
            circularAnimator.setInterpolator(mInterpolator);
            animatorList.add(circularAnimator);
        }

        mContentView.setPivotX(mRevealCenterX);
        mContentView.setPivotY(mRevealCenterY);
        animatorList.add(mAppearcaleAlphaAnimator);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mShouldDispatchTouchEvent = true;
            }

            @Override
            public void onAnimationStart(Animator animation) {
                mShouldDispatchTouchEvent = false;
            }
        });
        animatorSet.playTogether(animatorList);
        return animatorSet;
    }

    private Animator getDisappearAnimation() {
        List<Animator> animatorList = new ArrayList<>();
        int[] anchorCoord = new int[2];
        mAnchorView.getLocationInWindow(anchorCoord);
        int[] contentCoord = new int[2];
        mContentView.getLocationInWindow(contentCoord);

        int centerX = anchorCoord[0] + mAnchorView.getWidth() / 2;
        int centerY = anchorCoord[1] + mAnchorView.getHeight() / 2;

        mRevealCenterX = centerX - contentCoord[0];
        mRevealCenterY = centerY - contentCoord[1];
        if (CommonUtils.ATLEAST_LOLLIPOP) {
            float finalX = Math.max(Math.abs(mRevealCenterX), Math.abs(mRevealCenterX - mContentView.getWidth()));
            float finalY = Math.max(Math.abs(mRevealCenterY), Math.abs(mRevealCenterY - mContentView.getHeight()));
            float finalRadius = (float) Math.sqrt(finalX * finalX + finalY * finalY);

            Animator circularAnimator = ViewAnimationUtils.createCircularReveal(mContentView, mRevealCenterX, mRevealCenterY, finalRadius, 0);
            circularAnimator.setDuration(180);
            circularAnimator.setInterpolator(new DecelerateInterpolator(2f));
            animatorList.add(circularAnimator);
        }
        mContentView.setPivotX(mRevealCenterX);
        mContentView.setPivotY(mRevealCenterY);

        animatorList.add(mDissappearscaleAlphaAnimator);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                mShouldDispatchTouchEvent = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mShouldDispatchTouchEvent = true;
                mContentView.setScaleX(1);
                mContentView.setScaleY(1);
                mContentView.setAlpha(1);
                mAnchorView = null;
                RipplePopupView.super.dismiss();
                if (mCompleteRunnable != null) {
                    mCompleteRunnable.run();
                    mCompleteRunnable = null;
                }
            }
        });
        animatorSet.playTogether(animatorList);
        return animatorSet;
    }

    @Override
    protected boolean shouldDispatchTouchEvent() {
        return mShouldDispatchTouchEvent;
    }
}
