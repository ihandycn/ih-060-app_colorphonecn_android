package com.acb.libwallpaper.live.animation;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.view.View;

import com.ihs.commons.utils.HSLog;
import com.superapps.util.Dimensions;

public class GestureHintDrawer {
    // Repeat count + 1
    private static final int PLAY_COUNT = 3;

    /**
     * Minimum animation repeat count once started. If {@link #requestCancel(GestureHintDrawer.OnCancelCompleteListener)} is invoked
     * before completing this count, animation would continue to play until this count.
     */
    private static final int MIN_COUNT = 1;

    public enum GestureType {
        TOUCH_UP,
        TOUCH_DOWN,
        TOUCH_TWO_UP,
        TOUCH_DOWN_FOLDER,
        TOUCH_RIGHT,
    }

    private ValueAnimator mAnimator;
    int mRepeatCount;
    boolean mCancelRequested;
    GestureHintDrawer.OnCancelCompleteListener mCancelCompleteListener;

    private GestureHintDrawer.AnimationDelegateBase mAnimDelegate;
    GestureType mGestureType;

    public interface OnCancelCompleteListener {
        void onCancelComplete(GestureHintDrawer drawer);
    }

    public GestureHintDrawer(final View canvasView,
                             int pageWidth, int pageHeight, int pageCenterX, int pageCenterY, GestureType gestureType) {
        mGestureType = gestureType;

        mAnimator = LauncherAnimUtils.ofFloat(canvasView, 0f, 1f);
        mAnimator.setDuration(1650);
        mAnimator.setRepeatCount(PLAY_COUNT - 1);
        mAnimator.setStartDelay(500);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAnimDelegate.postAnimationProgress(animation.getAnimatedFraction());
                canvasView.invalidate(mAnimDelegate.getDirtyRect());
            }
        });
        mAnimator.addListener(new GestureHintDrawer.AnimatorListener(canvasView));

        switch (gestureType) {
            case TOUCH_UP:
            case TOUCH_DOWN:
            case TOUCH_DOWN_FOLDER:
                mAnimDelegate = new AnimationDelegateSingle(pageWidth, pageHeight, pageCenterX, pageCenterY);
                break;
            case TOUCH_TWO_UP:
                mAnimDelegate = new AnimationDelegateTwoFingers(pageWidth, pageHeight, pageCenterX, pageCenterY);
                break;
        }
    }

    public GestureHintDrawer(final View canvasView, GestureType gestureType, int pageWidth, int pageHeight, int pageCenterX, int pageCenterY) {
        mGestureType = gestureType;

        final int scrollX = canvasView.getScrollX();
        final int scrollY = canvasView.getScrollY();
        HSLog.i("DebugAction", "scroll sx == " + scrollX + "  cx == " + pageCenterX + "  cy == " + pageCenterY + "  w == " + pageWidth );
        mAnimator = LauncherAnimUtils.ofFloat(canvasView, 0f, 1f);
        mAnimator.setDuration(1300);
        mAnimator.setRepeatCount(1);
        mAnimator.setStartDelay(500);
        mAnimator.addUpdateListener(animation -> {
            mAnimDelegate.postAnimationProgress(animation.getAnimatedFraction());
            canvasView.invalidate(mAnimDelegate.getDirtyRect());
            int x = scrollX - (int) (mAnimDelegate.xProgress * Dimensions.pxFromDp(40));
            HSLog.i("DebugAction", "scroll x == " + x + "  sy == " + scrollY);
            canvasView.scrollTo(x, scrollY);
        });
        canvasView.setTag(scrollX);
        mAnimator.addListener(new GestureHintDrawer.AnimatorListener(canvasView));

        switch (gestureType) {
            case TOUCH_RIGHT:
                mAnimDelegate = new AnimationDelegateSingleRight(pageWidth, pageHeight, pageCenterX, pageCenterY);
                break;
            default:
                break;
        }
    }

    public void start() {
        mAnimator.start();
    }

    public boolean isRunning() {
        return mAnimator.isRunning();
    }

    public GestureType getGestureType() {
        return mGestureType;
    }

    public void draw(Canvas canvas) {
        mAnimDelegate.draw(canvas);
    }

    public void cancel() {
        mAnimator.cancel();
    }

    public void requestCancel(GestureHintDrawer.OnCancelCompleteListener cancelCompleteListener) {
        mCancelRequested = true;
        mCancelCompleteListener = cancelCompleteListener;
    }

    private class AnimatorListener extends android.animation.AnimatorListenerAdapter {
        private View mCanvasView;

        AnimatorListener(View canvasView) {
            mCanvasView = canvasView;
        }

        @Override
        public void onAnimationStart(Animator animation) {
            mRepeatCount = 0;
            mCancelRequested = false;
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
            mRepeatCount++;
            if (mCancelRequested && mRepeatCount >= MIN_COUNT) {
                animation.cancel();
                if (mCancelCompleteListener != null) {
                    mCancelCompleteListener.onCancelComplete(GestureHintDrawer.this);
                }
            }
            postClear();
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            postClear();
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            postClear();
        }

        private void postClear() {
            mAnimDelegate.postAnimationProgress(1f);
            mCanvasView.invalidate(mAnimDelegate.getDirtyRect());
            if (mCanvasView.getTag() != null && mCanvasView.getTag() instanceof Integer) {
                try {
                    mCanvasView.scrollTo(Integer.valueOf(mCanvasView.getTag().toString()), mCanvasView.getScrollY());
                } catch (Exception ignore) {}
            }
        }

        private void postEnd() {
            mAnimDelegate.postAnimationProgress(AnimationDelegateBase.STOP_MOVING_PROGRESS);
            mCanvasView.invalidate(mAnimDelegate.getDirtyRect());
            if (mCanvasView.getTag() != null && mCanvasView.getTag() instanceof Integer) {
                try {
                    mCanvasView.scrollTo(Integer.valueOf(mCanvasView.getTag().toString()), mCanvasView.getScrollY());
                } catch (Exception ignore) {}
            }
        }
    }

    /**
     * Delegate for drawing news hint animation.
     */
    private class AnimationDelegateBase {
        protected static final int CIRCLE_COLOR = Color.WHITE;
        protected static final int TAIL_COLOR_UP = 0x7fffffff;
        protected static final int TAIL_COLOR_DOWN = Color.TRANSPARENT;

        protected static final float START_MOVING_PROGRESS = 0.3f;
        protected static final float STOP_MOVING_PROGRESS = 0.8f;
        protected static final float Y_RATIO = 0.2f;
        protected int CIRCLE_RADIUS = Dimensions.pxFromDp(20);

//        protected Bitmap mHand;

        protected float mPageHeight;
        protected float mPageCenterX;
        protected float mPageCenterY;

        protected float mCx;
        protected float mCy;
        protected float xProgress;
        protected int radius = CIRCLE_RADIUS;
        protected RectF mTailRect = new RectF();

        protected Rect mDirtyRect;

        protected Paint mCirclePaint;
        protected Paint mTailPaint;

        AnimationDelegateBase(int pageWidth, int pageHeight, int pageCenterX, int pageCenterY) {
            mPageHeight = pageHeight;
            mPageCenterX = pageCenterX;
            mPageCenterY = pageCenterY;

            mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mCirclePaint.setColor(CIRCLE_COLOR);
            mCirclePaint.setStyle(Paint.Style.FILL);
            mTailPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mTailPaint.setStyle(Paint.Style.FILL);
            mTailPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        }

        void postAnimationProgress(float progress) {
            float circleAlpha = (float) Math.max(0f, Math.min(3f - 6.67f * Math.abs(progress - 0.55), 1f));
            float tailAlpha = (float) Math.max(0f, Math.min(2f - 5f * Math.abs(progress - 0.6), 1f));
            int circleAlphaInt = (int) (0xff * circleAlpha);
            int tailAlphaInt = (int) (0xff * tailAlpha);
            mCirclePaint.setAlpha(circleAlphaInt);
            mTailPaint.setAlpha(tailAlphaInt);
        }

        Rect getDirtyRect() {
            return mDirtyRect;
        }

        public void draw(Canvas canvas) {
        }
    }

    private class AnimationDelegateSingle extends AnimationDelegateBase {
        AnimationDelegateSingle(int pageWidth, int pageHeight, int pageCenterX, int pageCenterY) {
            super(pageWidth, pageHeight, pageCenterX, pageCenterY);

            mDirtyRect = new Rect((int) Math.floor(pageCenterX - CIRCLE_RADIUS * 1.5f),
                    (int) Math.floor(pageCenterY - pageHeight * Y_RATIO - CIRCLE_RADIUS),
                    (int) Math.ceil(pageCenterX + CIRCLE_RADIUS * 1.5f),
                    (int) Math.ceil(pageCenterY + pageHeight * Y_RATIO));
        }

        void postAnimationProgress(float progress) {
            super.postAnimationProgress(progress);

            float moveProgress = (progress - START_MOVING_PROGRESS) / (STOP_MOVING_PROGRESS - START_MOVING_PROGRESS);
            moveProgress = LauncherAnimUtils.ACCELERATE_DECELERATE.getInterpolation(moveProgress);
            float tailY;
            switch (mGestureType) {
                case TOUCH_UP:
                    if (progress < START_MOVING_PROGRESS) {
                        mCy = mPageCenterY + mPageHeight * Y_RATIO * 0.5f;
                        float percent = LauncherAnimUtils.OVERSHOOT.getInterpolation(Math.min((progress + 0.05f) / START_MOVING_PROGRESS, 1f));
                        radius = (int) (CIRCLE_RADIUS * percent);
                    } else if (progress < STOP_MOVING_PROGRESS) {
                        mCy = mPageCenterY + mPageHeight * Y_RATIO * (0.5f - moveProgress);
                        radius = CIRCLE_RADIUS;
                    } else {
                        mCy = mPageCenterY - mPageHeight * Y_RATIO * 0.5f;
                        radius = (int) (CIRCLE_RADIUS * (1 - progress + STOP_MOVING_PROGRESS));
                    }
                    tailY = mPageCenterY + mPageHeight * Y_RATIO * 0.5f;
                    mTailRect.set(mPageCenterX - radius, mCy, mPageCenterX + radius, tailY);
                    break;
                case TOUCH_DOWN:
                case TOUCH_DOWN_FOLDER:
                    if (progress < START_MOVING_PROGRESS) {
                        mCy = mPageCenterY - mPageHeight * Y_RATIO * 0.5f;
                        float percent = LauncherAnimUtils.OVERSHOOT.getInterpolation(Math.min((progress + 0.05f) / START_MOVING_PROGRESS, 1f));
                        radius = (int) (CIRCLE_RADIUS * percent);
                    } else if (progress < STOP_MOVING_PROGRESS) {
                        mCy = mPageCenterY - mPageHeight * Y_RATIO * (0.5f - moveProgress);
                        radius = CIRCLE_RADIUS;
                    } else {
                        mCy = mPageCenterY + mPageHeight * Y_RATIO * 0.5f;
                        radius = (int) (CIRCLE_RADIUS * (1 - progress + STOP_MOVING_PROGRESS));
                    }
                    tailY = mPageCenterY - mPageHeight * Y_RATIO * 0.5f;
                    mTailRect.set(mPageCenterX - radius, tailY, mPageCenterX + radius, mCy);
                    break;
                default:
                    // Default TOUCH_UP
                    tailY = mPageCenterY + mPageHeight * Y_RATIO * 0.5f;
                    mTailRect.set(mPageCenterX - radius, mCy, mPageCenterX + radius, tailY);
                    break;
            }

            mTailPaint.setShader(new LinearGradient(mPageCenterX, mCy, mPageCenterX, tailY,
                    TAIL_COLOR_UP, TAIL_COLOR_DOWN, Shader.TileMode.CLAMP));
        }

        public void draw(Canvas canvas) {
            if (mGestureType != GestureType.TOUCH_RIGHT) {
                canvas.drawRect(mTailRect, mTailPaint);
            }
            canvas.drawCircle(mPageCenterX, mCy, radius, mCirclePaint);
        }
    }

    private class AnimationDelegateSingleRight extends AnimationDelegateBase {
        TimeInterpolator open;
        TimeInterpolator close;
        AnimationDelegateSingleRight(int pageWidth, int pageHeight, int pageCenterX, int pageCenterY) {
            super(pageWidth, pageHeight, pageCenterX, pageCenterY);

            CIRCLE_RADIUS = Dimensions.pxFromDp(14);

            mDirtyRect = new Rect((int) Math.floor(pageCenterX - CIRCLE_RADIUS),
                    (int) Math.floor(pageCenterY - CIRCLE_RADIUS),
                    (int) Math.ceil(pageCenterX + CIRCLE_RADIUS),
                    (int) Math.ceil(pageCenterY + CIRCLE_RADIUS));

            open =  PathInterpolatorCompat.create(.01f, .33f, .14f, .68f);
            close =  LauncherAnimUtils.DECELERATE_QUINT;
        }

        @Override
        void postAnimationProgress(float progress) {

            float stopMovingProgress = STOP_MOVING_PROGRESS + 0.08f;
            float startMovingProgress = START_MOVING_PROGRESS + 0.15f;
            switch (mGestureType) {
                case TOUCH_RIGHT:
                    mCx = mPageCenterX;
                    mCy = mPageCenterY - mPageHeight * Y_RATIO * 0.5f;
                    if (progress < startMovingProgress) {
                        float percent = LauncherAnimUtils.OVERSHOOT.getInterpolation(Math.min((progress + 0.05f) / startMovingProgress, 1f));
                        radius = (int) (CIRCLE_RADIUS * percent);

                        float circleAlpha = Math.max(0f, Math.min(0.8f - 4 * Math.abs(progress - startMovingProgress), 1f));
                        int circleAlphaInt = (int) (0xff * circleAlpha);
                        mCirclePaint.setAlpha(circleAlphaInt);
                    } else if (progress < stopMovingProgress) {
                        float moveProgress = (progress - startMovingProgress) / (stopMovingProgress - startMovingProgress);
                        moveProgress = open.getInterpolation(moveProgress);

                        xProgress = moveProgress;
                        radius = CIRCLE_RADIUS;

                        mCirclePaint.setAlpha(0xff);
                    } else {
                        float p = 1 - (progress - stopMovingProgress) / (1 - stopMovingProgress);
                        xProgress = close.getInterpolation(p);
//                        HSLog.i("DebugAction", "xProgress == " + xProgress + "  p == " + p);
                        radius = (int) (CIRCLE_RADIUS * (1 - progress + stopMovingProgress));

                        mCx = mPageCenterX + (1 - xProgress) * Dimensions.pxFromDp(40);

                        float circleAlpha = Math.max(0f, Math.min(1 - 20f * Math.abs(progress - stopMovingProgress), 1f));
                        int circleAlphaInt = (int) (0xff * circleAlpha);
                        mCirclePaint.setAlpha(circleAlphaInt);
                    }
                    break;
                default:
                    break;
            }
        }

        public void draw(Canvas canvas) {
            canvas.drawCircle(mCx, mCy, radius, mCirclePaint);
        }
    }

    private class AnimationDelegateTwoFingers extends AnimationDelegateBase {
        protected int CIRCLE_RADIUS = Dimensions.pxFromDp(15);
        protected float mCx;
        protected float offset;
        protected Shader leftShader;
        protected Shader rightShader;

        AnimationDelegateTwoFingers(int pageWidth, int pageHeight, int pageCenterX, int pageCenterY) {
            super(pageWidth, pageHeight, pageCenterX, pageCenterY);

            mCx = pageCenterX - 1.1f * CIRCLE_RADIUS;
            offset = 2.2f * CIRCLE_RADIUS;

            mDirtyRect = new Rect((int) Math.floor(mCx - CIRCLE_RADIUS),
                    (int) Math.floor(pageCenterY - pageHeight * Y_RATIO - CIRCLE_RADIUS),
                    (int) Math.ceil(mCx + 4.2f * CIRCLE_RADIUS),
                    (int) Math.ceil(pageCenterY + pageHeight * Y_RATIO + CIRCLE_RADIUS));
        }

        void postAnimationProgress(float progress) {
            super.postAnimationProgress(progress);

            float moveProgress = (progress - START_MOVING_PROGRESS) / (STOP_MOVING_PROGRESS - START_MOVING_PROGRESS);
            moveProgress = LauncherAnimUtils.ACCELERATE_DECELERATE.getInterpolation(moveProgress);
            float tailY;
            switch (mGestureType) {
                case TOUCH_TWO_UP:
                    if (progress < START_MOVING_PROGRESS) {
                        mCy = mPageCenterY + mPageHeight * Y_RATIO * 0.5f;
                        float percent = LauncherAnimUtils.OVERSHOOT.getInterpolation(Math.min((progress + 0.05f) / START_MOVING_PROGRESS, 1f));
                        radius = (int) (CIRCLE_RADIUS * percent);
                    } else if (progress < STOP_MOVING_PROGRESS) {
                        mCy = mPageCenterY + mPageHeight * Y_RATIO * (0.5f - moveProgress);
                        radius = CIRCLE_RADIUS;
                    } else {
                        mCy = mPageCenterY - mPageHeight * Y_RATIO * 0.5f;
                        radius = (int) (CIRCLE_RADIUS * (1 - progress + STOP_MOVING_PROGRESS));
                    }
                    tailY = mPageCenterY + mPageHeight * Y_RATIO * 0.5f;
                    mTailRect.set(mCx - radius, mCy, mCx + radius, tailY);
                    break;
                default:
                    // Default TOUCH_UP
                    tailY = mPageCenterY + mPageHeight * Y_RATIO * 0.5f;
                    mTailRect.set(mCx - CIRCLE_RADIUS, mCy, mCx + CIRCLE_RADIUS, tailY);
                    break;
            }

            leftShader = new LinearGradient(mCx, mCy, mCx, tailY,
                    TAIL_COLOR_UP, TAIL_COLOR_DOWN, Shader.TileMode.CLAMP);

            rightShader = new LinearGradient(mCx + offset, mCy - offset, mCx + offset, tailY - offset,
                    TAIL_COLOR_UP, TAIL_COLOR_DOWN, Shader.TileMode.CLAMP);
        }

        public void draw(Canvas canvas) {
            mTailPaint.setShader(leftShader);
            canvas.drawRect(mTailRect, mTailPaint);
            canvas.drawCircle(mCx, mCy, radius, mCirclePaint);
            mTailRect.offset(offset, -offset);
            mTailPaint.setShader(rightShader);
            canvas.drawRect(mTailRect, mTailPaint);
            canvas.drawCircle(mCx + offset, mCy - offset, radius, mCirclePaint);
        }
    }
}
