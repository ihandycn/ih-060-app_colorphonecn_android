/*
 * The MIT License (MIT)

 * Copyright (c) 2014 Justas Medeisis

 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package colorphone.acb.com.libweather.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.ihs.commons.utils.HSLog;

import colorphone.acb.com.libweather.R;

/**
 * https://github.com/justasm/DragLinearLayout v1.1.0 with Air Launcher modifications.
 * <p/>
 * A LinearLayout that supports children Views that can be dragged and swapped around.
 * See {@link #addDragView(View, View)},
 * {@link #addDragView(View, View, int)},
 * {@link #setViewDraggable(View, View)}, and
 * {@link #removeDragView(View)}.
 * <p/>
 * Currently, no error-checking is done on standard {@link #addView(View)} and
 * {@link #removeView(View)} calls, so avoid using these with children previously
 * declared as draggable to prevent memory leaks and/or subtle bugs.
 */
public class DragLinearLayout extends LinearLayout {

    private static final String TAG = DragLinearLayout.class.getSimpleName();

    private static final long NOMINAL_SWITCH_DURATION = 150;
    private static final long MIN_SWITCH_DURATION = NOMINAL_SWITCH_DURATION;
    private static final long MAX_SWITCH_DURATION = NOMINAL_SWITCH_DURATION * 2;
    private static final float NOMINAL_DISTANCE = 20;

    private final float mNominalDistanceScaled;

    /**
     * Use with {@link #setDragListener(DragListener)}
     * to listen for draggable view swaps.
     */
    public interface DragListener {
        /**
         * Invoked when a drag starts.
         */
        void onDragStart();

        /**
         * Invoked when a drag terminates.
         */
        void onDragStop();

        /**
         * Invoked right before the two items are swapped due to a drag event.
         * After the swap, the firstView will be in the secondPosition, and vice versa.
         * <p/>
         * No guarantee is made as to which of the two has a lesser/greater position.
         */
        void onSwap(View firstView, int firstPosition, View secondView, int secondPosition);
    }

    private DragListener mDragListener;

    private LayoutTransition mLayoutTransition;

    /**
     * Mapping from child index to drag-related info container.
     * Presence of mapping implies the child can be dragged, and is considered for swaps with the
     * currently dragged item.
     */
    private final SparseArray<DraggableChild> mDraggableChildren;

    private static class DraggableChild {
        /**
         * If non-null, a reference to an on-going position animation.
         */
        private ValueAnimator mSwapAnimation;

        public void endExistingAnimation() {
            if (null != mSwapAnimation) mSwapAnimation.end();
        }

        public void cancelExistingAnimation() {
            if (null != mSwapAnimation) mSwapAnimation.cancel();
        }
    }

    /**
     * Holds state information about the currently dragged item.
     * <p/>
     * Rough lifecycle:
     * <li>#startDetectingOnPossibleDrag - #detecting == true</li>
     * <li>     if drag is recognised, #onDragStart - #dragging == true</li>
     * <li>     if drag ends, #onDragStop - #dragging == false, #settling == true</li>
     * <li>if gesture ends without drag, or settling finishes, #stopDetecting - #detecting == false</li>
     */
    private class DragItem {
        private View mView;
        private int mStartVisibility;
        private BitmapDrawable mViewDrawable;
        private int mPosition;
        private int mStartTop;
        private int mHeight;
        private int mTotalDragOffset;
        private int mTargetTopOffset;
        private ValueAnimator mSettleAnimation;

        private boolean mDetecting;
        private boolean mDragging;

        public DragItem() {
            stopDetecting();
        }

        public void startDetectingOnPossibleDrag(final View view, final int position) {
            this.mView = view;
            this.mStartVisibility = view.getVisibility();
            this.mViewDrawable = getDragDrawable(view);
            this.mPosition = position;
            this.mStartTop = view.getTop();
            this.mHeight = view.getHeight();
            this.mTotalDragOffset = 0;
            this.mTargetTopOffset = 0;
            this.mSettleAnimation = null;

            this.mDetecting = true;
        }

        public void onDragStart() {
            mView.setVisibility(View.INVISIBLE);
            this.mDragging = true;
        }

        public void setTotalOffset(int offset) {
            mTotalDragOffset = offset;
            updateTargetTop();
        }

        public void updateTargetTop() {
            if (mView != null) {
                mTargetTopOffset = mStartTop - mView.getTop() + mTotalDragOffset;
            }
        }

        public void onDragStop() {
            this.mDragging = false;
        }

        public boolean settling() {
            return null != mSettleAnimation;
        }

        public void stopDetecting() {
            this.mDetecting = false;
            if (null != mView) mView.setVisibility(mStartVisibility);
            mView = null;
            mStartVisibility = -1;
            mViewDrawable = null;
            mPosition = -1;
            mStartTop = -1;
            mHeight = -1;
            mTotalDragOffset = 0;
            mTargetTopOffset = 0;
            if (null != mSettleAnimation) mSettleAnimation.end();
            mSettleAnimation = null;
        }
    }

    /**
     * The currently dragged item, if {@link DragItem#mDetecting}.
     */
    private final DragItem mDraggedItem;
    private final int mSlop;

    private static final int INVALID_POINTER_ID = -1;
    private int mDownY = -1;
    private int mActivePointerId = INVALID_POINTER_ID;

    /**
     * The shadow to be drawn above the {@link #mDraggedItem}.
     */
    private final Drawable mDragTopShadowDrawable;
    /**
     * The shadow to be drawn below the {@link #mDraggedItem}.
     */
    private final Drawable mDragBottomShadowDrawable;
    private final int mDragShadowHeight;

    /**
     * See {@link #setContainerScrollView(ScrollView)}.
     */
    private ScrollView mContainerScrollView;
    private int mScrollSensitiveAreaHeight;
    private static final int DEFAULT_SCROLL_SENSITIVE_AREA_HEIGHT_DP = 48;
    private static final int MAX_DRAG_SCROLL_SPEED = 16;

    public DragLinearLayout(Context context) {
        this(context, null);
    }

    public DragLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOrientation(LinearLayout.VERTICAL);

        mDraggableChildren = new SparseArray<>();

        mDraggedItem = new DragItem();
        ViewConfiguration vc = ViewConfiguration.get(context);
        mSlop = vc.getScaledTouchSlop();

        final Resources resources = getResources();
        mDragTopShadowDrawable = ContextCompat.getDrawable(context, R.drawable.ab_solid_shadow_holo_flipped);
        mDragBottomShadowDrawable = ContextCompat.getDrawable(context, R.drawable.ab_solid_shadow_holo);
        mDragShadowHeight = resources.getDimensionPixelSize(R.dimen.drag_linear_layout_downwards_drop_shadow_height);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DragLinearLayout, 0, 0);
        try {
            mScrollSensitiveAreaHeight = a.getDimensionPixelSize(R.styleable.DragLinearLayout_scrollSensitiveHeight,
                    (int) (DEFAULT_SCROLL_SENSITIVE_AREA_HEIGHT_DP * resources.getDisplayMetrics().density + 0.5f));
        } finally {
            a.recycle();
        }

        mNominalDistanceScaled = (int) (NOMINAL_DISTANCE * resources.getDisplayMetrics().density + 0.5f);
    }

    @Override
    public void setOrientation(int orientation) {
        // Enforce VERTICAL orientation. Remove if HORIZONTAL support is ever added.
        if (LinearLayout.HORIZONTAL == orientation) {
            throw new IllegalArgumentException("DragLinearLayout must be VERTICAL.");
        }
        super.setOrientation(orientation);
    }

    /**
     * Calls {@link #addView(View)} followed by {@link #setViewDraggable(View, View)}.
     */
    public void addDragView(View child, View dragHandle) {
        addView(child);
        setViewDraggable(child, dragHandle);
    }

    /**
     * Calls {@link #addView(View, int)} followed by
     * {@link #setViewDraggable(View, View)} and correctly updates the
     * drag-ability state of all existing views.
     */
    public void addDragView(View child, View dragHandle, int index) {
        addView(child, index);

        // Update drag-able children mappings
        final int numMappings = mDraggableChildren.size();
        for (int i = numMappings - 1; i >= 0; i--) {
            final int key = mDraggableChildren.keyAt(i);
            if (key >= index) {
                mDraggableChildren.put(key + 1, mDraggableChildren.get(key));
            }
        }

        setViewDraggable(child, dragHandle);
    }

    /**
     * Makes the child a candidate for dragging. Must be an existing child of this layout.
     */
    public void setViewDraggable(View child, View dragHandle) {
        if (null == child || null == dragHandle) {
            throw new IllegalArgumentException(
                    "Draggable children and their drag handles must not be null.");
        }

        if (this == child.getParent()) {
            dragHandle.setOnTouchListener(new DragHandleOnTouchListener(child));
            mDraggableChildren.put(indexOfChild(child), new DraggableChild());
        } else {
            HSLog.e(TAG, child + " is not a child, cannot make draggable");
        }
    }

    /**
     * Calls {@link #removeView(View)} and correctly updates the drag-ability state of
     * all remaining views.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void removeDragView(View child) {
        if (this == child.getParent()) {
            final int index = indexOfChild(child);
            removeView(child);

            // update drag-able children mappings
            final int mappings = mDraggableChildren.size();
            for (int i = 0; i < mappings; i++) {
                final int key = mDraggableChildren.keyAt(i);
                if (key >= index) {
                    DraggableChild next = mDraggableChildren.get(key + 1);
                    if (null == next) {
                        mDraggableChildren.delete(key);
                    } else {
                        mDraggableChildren.put(key, next);
                    }
                }
            }
        }
    }

    @Override
    public void removeAllViews() {
        super.removeAllViews();
        mDraggableChildren.clear();
    }

    /**
     * If this layout is within a {@link ScrollView}, register it here so that it
     * can be scrolled during item drags.
     */
    public void setContainerScrollView(ScrollView scrollView) {
        mContainerScrollView = scrollView;
    }

    /**
     * Sets the height from upper / lower edge at which a container {@link ScrollView},
     * if one is registered via {@link #setContainerScrollView(ScrollView)},
     * is scrolled.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setScrollSensitiveHeight(int height) {
        mScrollSensitiveAreaHeight = height;
    }

    @SuppressWarnings("UnusedDeclaration")
    public int getScrollSensitiveHeight() {
        return mScrollSensitiveAreaHeight;
    }

    /**
     * See {@link DragListener}.
     */
    public void setDragListener(DragListener dragListener) {
        mDragListener = dragListener;
    }

    /**
     * A linear relationship b/w distance and duration, bounded.
     */
    private long getTranslateAnimationDuration(float distance) {
        return Math.min(MAX_SWITCH_DURATION, Math.max(MIN_SWITCH_DURATION,
                (long) (NOMINAL_SWITCH_DURATION * Math.abs(distance) / mNominalDistanceScaled)));
    }

    /**
     * Initiates a new {@link #mDraggedItem} unless the current one is still
     * {@link DragItem#mDetecting}.
     */
    private void startDetectingDrag(View child) {
        if (mDraggedItem.mDetecting) {
            return; // Existing drag in process, only one at a time is allowed
        }

        final int position = indexOfChild(child);

        // Complete any existing animations, both for the newly selected child and the previous dragged one
        mDraggableChildren.get(position).endExistingAnimation();

        mDraggedItem.startDetectingOnPossibleDrag(child, position);
        if (mContainerScrollView != null) {
            mContainerScrollView.requestDisallowInterceptTouchEvent(true);
        }
    }

    private void startDrag() {
        // Remove layout transition, it conflicts with drag animation
        // we will restore it after drag animation end. See onDragStop().
        mLayoutTransition = getLayoutTransition();
        if (mLayoutTransition != null) {
            setLayoutTransition(null);
        }

        if (mDragListener != null) {
            mDragListener.onDragStart();
        }

        mDraggedItem.onDragStart();
        requestDisallowInterceptTouchEvent(true);
    }

    /**
     * Animates the dragged item to its final resting position.
     */
    private void onDragStop() {
        mDraggedItem.mSettleAnimation = ValueAnimator.ofFloat(mDraggedItem.mTotalDragOffset,
                mDraggedItem.mTotalDragOffset - mDraggedItem.mTargetTopOffset)
                .setDuration(getTranslateAnimationDuration(mDraggedItem.mTargetTopOffset));
        mDraggedItem.mSettleAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (!mDraggedItem.mDetecting) return; // already stopped

                mDraggedItem.setTotalOffset(((Float) animation.getAnimatedValue()).intValue());

                final int shadowAlpha = (int) ((1 - animation.getAnimatedFraction()) * 255);
                if (null != mDragTopShadowDrawable) mDragTopShadowDrawable.setAlpha(shadowAlpha);
                mDragBottomShadowDrawable.setAlpha(shadowAlpha);
                invalidate();
            }
        });
        mDraggedItem.mSettleAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mDraggedItem.onDragStop();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!mDraggedItem.mDetecting) {
                    return; // already stopped
                }

                mDraggedItem.mSettleAnimation = null;
                mDraggedItem.stopDetecting();

                if (null != mDragTopShadowDrawable) mDragTopShadowDrawable.setAlpha(255);
                mDragBottomShadowDrawable.setAlpha(255);

                // restore layout transition
                if (mLayoutTransition != null && getLayoutTransition() == null) {
                    setLayoutTransition(mLayoutTransition);
                }

                if (mDragListener != null) {
                    mDragListener.onDragStop();
                }
            }
        });
        mDraggedItem.mSettleAnimation.start();
    }

    /**
     * Updates the dragged item with the given total offset from its starting position.
     * Evaluates and executes draggable view swaps.
     */
    private void onDrag(int offset) {
        offset = handleClamping(offset);

        mDraggedItem.setTotalOffset(offset);
        invalidate();

        int currentTop = mDraggedItem.mStartTop + mDraggedItem.mTotalDragOffset;

        handleContainerScroll(currentTop);

        int belowPosition = nextDraggablePosition(mDraggedItem.mPosition);
        int abovePosition = previousDraggablePosition(mDraggedItem.mPosition);

        View belowView = getChildAt(belowPosition);
        View aboveView = getChildAt(abovePosition);

        final boolean isBelow = (belowView != null) &&
                (currentTop + mDraggedItem.mHeight > belowView.getTop() + belowView.getHeight() / 2);
        final boolean isAbove = (aboveView != null) &&
                (currentTop < aboveView.getTop() + aboveView.getHeight() / 2);

        if (isBelow || isAbove) {
            final View switchView = isBelow ? belowView : aboveView;

            // Swap elements
            final int originalPosition = mDraggedItem.mPosition;
            final int switchPosition = isBelow ? belowPosition : abovePosition;

            mDraggableChildren.get(switchPosition).cancelExistingAnimation();
            final float switchViewStartY = switchView.getY();

            if (mDragListener != null) {
                mDragListener.onSwap(mDraggedItem.mView, mDraggedItem.mPosition, switchView, switchPosition);
            }

            if (isBelow) {
                removeViewAt(originalPosition);
                removeViewAt(switchPosition - 1);

                addView(belowView, originalPosition);
                addView(mDraggedItem.mView, switchPosition);
            } else {
                removeViewAt(switchPosition);
                removeViewAt(originalPosition - 1);

                addView(mDraggedItem.mView, switchPosition);
                addView(aboveView, originalPosition);
            }
            mDraggedItem.mPosition = switchPosition;

            final ViewTreeObserver switchViewObserver = switchView.getViewTreeObserver();
            switchViewObserver.addOnPreDrawListener(new OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    switchViewObserver.removeOnPreDrawListener(this);

                    final ObjectAnimator switchAnimator = ObjectAnimator.ofFloat(switchView, "y",
                            switchViewStartY, switchView.getTop())
                            .setDuration(getTranslateAnimationDuration(switchView.getTop() - switchViewStartY));
                    switchAnimator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            mDraggableChildren.get(originalPosition).mSwapAnimation = switchAnimator;
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mDraggableChildren.get(originalPosition).mSwapAnimation = null;
                        }
                    });
                    switchAnimator.start();

                    return true;
                }
            });

            final ViewTreeObserver observer = mDraggedItem.mView.getViewTreeObserver();
            observer.addOnPreDrawListener(new OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    observer.removeOnPreDrawListener(this);
                    mDraggedItem.updateTargetTop();

                    // TODO test if still necessary..
                    // because draggedItem#view#getTop() is only up-to-date NOW
                    // (and not right after the #addView() swaps above)
                    // we may need to update an ongoing settle animation
                    if (mDraggedItem.settling()) {
                        HSLog.d(TAG, "Updating settle animation");
                        mDraggedItem.mSettleAnimation.removeAllListeners();
                        mDraggedItem.mSettleAnimation.cancel();
                        onDragStop();
                    }
                    return true;
                }
            });
        }
    }

    private int handleClamping(int offset) {
        int minOffset = -mDraggedItem.mStartTop;
        int maxOffset = mDraggedItem.mHeight * (mDraggableChildren.size() - 1) - mDraggedItem.mStartTop;
        return Math.max(minOffset, Math.min(offset, maxOffset));
    }

    private int previousDraggablePosition(int position) {
        int startIndex = mDraggableChildren.indexOfKey(position);
        if (startIndex < 1 || startIndex > mDraggableChildren.size()) return -1;
        return mDraggableChildren.keyAt(startIndex - 1);
    }

    private int nextDraggablePosition(int position) {
        int startIndex = mDraggableChildren.indexOfKey(position);
        if (startIndex < -1 || startIndex > mDraggableChildren.size() - 2) return -1;
        return mDraggableChildren.keyAt(startIndex + 1);
    }

    private Runnable dragUpdater;

    private void handleContainerScroll(final int currentTop) {
        if (null != mContainerScrollView) {
            final int startScrollY = mContainerScrollView.getScrollY();
            final int absTop = getTop() - startScrollY + currentTop;
            final int height = mContainerScrollView.getHeight();

            final int delta;

            if (absTop < mScrollSensitiveAreaHeight) {
                delta = (int) (-MAX_DRAG_SCROLL_SPEED * smootherStep(mScrollSensitiveAreaHeight, 0, absTop));
            } else if (absTop > height - mScrollSensitiveAreaHeight) {
                delta = (int) (MAX_DRAG_SCROLL_SPEED * smootherStep(height - mScrollSensitiveAreaHeight, height, absTop));
            } else {
                delta = 0;
            }

            mContainerScrollView.removeCallbacks(dragUpdater);
            mContainerScrollView.smoothScrollBy(0, delta);
            dragUpdater = new Runnable() {
                @Override
                public void run() {
                    if (mDraggedItem.mDragging && startScrollY != mContainerScrollView.getScrollY()) {
                        onDrag(mDraggedItem.mTotalDragOffset + delta);
                    }
                }
            };
            mContainerScrollView.post(dragUpdater);
        }
    }

    /**
     * By Ken Perlin. See <a href="http://en.wikipedia.org/wiki/Smoothstep">Smoothstep - Wikipedia</a>.
     */
    private static float smootherStep(float edge1, float edge2, float val) {
        val = Math.max(0, Math.min((val - edge1) / (edge2 - edge1), 1));
        return val * val * val * (val * (val * 6 - 15) + 10);
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        super.dispatchDraw(canvas);

        if (mDraggedItem.mDetecting && (mDraggedItem.mDragging || mDraggedItem.settling())) {
            canvas.save();
            canvas.translate(0, mDraggedItem.mTotalDragOffset);
            mDraggedItem.mViewDrawable.draw(canvas);

            final int left = mDraggedItem.mViewDrawable.getBounds().left;
            final int right = mDraggedItem.mViewDrawable.getBounds().right;
            final int top = mDraggedItem.mViewDrawable.getBounds().top;
            final int bottom = mDraggedItem.mViewDrawable.getBounds().bottom;

            mDragBottomShadowDrawable.setBounds(left, bottom, right, bottom + mDragShadowHeight);
            mDragBottomShadowDrawable.draw(canvas);

            if (null != mDragTopShadowDrawable) {
                mDragTopShadowDrawable.setBounds(left, top - mDragShadowHeight, right, top);
                mDragTopShadowDrawable.draw(canvas);
            }

            canvas.restore();
        }
    }

    /*
     * Note regarding touch handling:
     * In general, we have three cases -
     * 1) User taps outside any children.
     *      #onInterceptTouchEvent receives DOWN
     *      #onTouchEvent receives DOWN
     *          draggedItem.detecting == false, we return false and no further events are received
     * 2) User taps on non-interactive drag handle / child, e.g. TextView or ImageView.
     *      #onInterceptTouchEvent receives DOWN
     *      DragHandleOnTouchListener (attached to each draggable child) #onTouch receives DOWN
     *      #startDetectingDrag is called, draggedItem is now detecting
     *      view does not handle touch, so our #onTouchEvent receives DOWN
     *          draggedItem.detecting == true, we #startDrag() and proceed to handle the drag
     * 3) User taps on interactive drag handle / child, e.g. Button.
     *      #onInterceptTouchEvent receives DOWN
     *      DragHandleOnTouchListener (attached to each draggable child) #onTouch receives DOWN
     *      #startDetectingDrag is called, draggedItem is now detecting
     *      view handles touch, so our #onTouchEvent is not called yet
     *      #onInterceptTouchEvent receives ACTION_MOVE
     *      if dy > touch slop, we assume user wants to drag and intercept the event
     *      #onTouchEvent receives further ACTION_MOVE events, proceed to handle the drag
     *
     * For cases 2) and 3), lifting the active pointer at any point in the sequence of events
     * triggers #onTouchEnd and the draggedItem, if detecting, is #stopDetecting.
     */

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (MotionEventCompat.getActionMasked(event)) {
            case MotionEvent.ACTION_DOWN: {
                if (mDraggedItem.mDetecting) return false; // an existing item is (likely) settling
                mDownY = (int) MotionEventCompat.getY(event, 0);
                mActivePointerId = MotionEventCompat.getPointerId(event, 0);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (!mDraggedItem.mDetecting) return false;
                if (INVALID_POINTER_ID == mActivePointerId) break;
                final int pointerIndex = event.findPointerIndex(mActivePointerId);
                final float y = MotionEventCompat.getY(event, pointerIndex);
                final float dy = y - mDownY;
                if (Math.abs(dy) > mSlop) {
                    startDrag();
                    return true;
                }
                return false;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                final int pointerIndex = MotionEventCompat.getActionIndex(event);
                final int pointerId = MotionEventCompat.getPointerId(event, pointerIndex);

                if (pointerId != mActivePointerId)
                    break; // if active pointer, fall through and cancel!
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                onTouchEnd();

                if (mDraggedItem.mDetecting) mDraggedItem.stopDetecting();
                break;
            }
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        switch (MotionEventCompat.getActionMasked(event)) {
            case MotionEvent.ACTION_DOWN: {
                if (!mDraggedItem.mDetecting || mDraggedItem.settling()) return false;
                startDrag();
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                if (!mDraggedItem.mDragging) break;
                if (INVALID_POINTER_ID == mActivePointerId) break;

                int pointerIndex = event.findPointerIndex(mActivePointerId);
                int lastEventY = (int) MotionEventCompat.getY(event, pointerIndex);
                int deltaY = lastEventY - mDownY;

                onDrag(deltaY);
                return true;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                final int pointerIndex = MotionEventCompat.getActionIndex(event);
                final int pointerId = MotionEventCompat.getPointerId(event, pointerIndex);

                if (pointerId != mActivePointerId)
                    break; // if active pointer, fall through and cancel!
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                onTouchEnd();

                if (mDraggedItem.mDragging) {
                    onDragStop();
                } else if (mDraggedItem.mDetecting) {
                    mDraggedItem.stopDetecting();
                }
                return true;
            }
        }
        return false;
    }

    private void onTouchEnd() {
        mDownY = -1;
        mActivePointerId = INVALID_POINTER_ID;
    }

    private class DragHandleOnTouchListener implements OnTouchListener {
        private final View view;

        public DragHandleOnTouchListener(final View view) {
            this.view = view;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (MotionEvent.ACTION_DOWN == MotionEventCompat.getActionMasked(event)) {
                startDetectingDrag(view);
            }
            return false;
        }
    }

    private BitmapDrawable getDragDrawable(View view) {
        int top = view.getTop();
        int left = view.getLeft();

        Bitmap bitmap = getBitmapFromView(view);

        BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);

        drawable.setBounds(new Rect(left, top, left + view.getWidth(), top + view.getHeight()));

        return drawable;
    }

    /**
     * @return a bitmap showing a screenshot of the view passed in.
     */
    private static Bitmap getBitmapFromView(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }
}
