package com.colorphone.lock.lockscreen.locker.slidingdrawer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import com.colorphone.lock.R;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Dimensions;

import java.lang.ref.WeakReference;

@SuppressLint("NewApi")
public class SlidingDrawer extends FrameLayout implements OnTouchListener {

    /**
     * Callback invoked when the drawer is scrolled.
     */
    public interface SlidingDrawerListener {
        /**
         * Invoked when the user starts dragging/flinging the drawer's handle.
         */
        void onScrollStarted();

        /**
         * Invoked when the user stops dragging/flinging the drawer's handle.
         */
        void onScrollEnded(boolean expanded);

        void onScroll(float cur, float totle);
    }

    private static class TensionView extends FrameLayout {

        private final WeakReference<SlidingDrawer> mHostViewRef;

        public TensionView(SlidingDrawer hostView) {
            super(hostView.getContext());
            mHostViewRef = new WeakReference<>(hostView);
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            updateViewTension();
        }

        public void updateViewTension() {
            SlidingDrawer hostView = mHostViewRef.get();
            if (null == hostView) {
                return;
            }
            int y = (int) hostView.getY();
            setTop(y + hostView.getHeight());
            setLeft(hostView.getLeft());
            setRight(hostView.getRight());
            setBottom(hostView.getBottom());
        }
    }

    public static final int ORIENTATION_TOP_DOWN = 1 << 0;
    public static final int ORIENTATION_BOTTOM_UP = 1 << 1;
    public static final int ORIENTATION_LEFT_TO_RIGHT = 1 << 2;
    public static final int ORIENTATION_RIGHT_TO_LEFT = 1 << 3;

    public static final int ORIENTATION_VERTICAL_DEFAULT = ORIENTATION_BOTTOM_UP;

    private final int orientation;
    private boolean allowSingleTap;
    private boolean mAnimateOnClick;

    private float mBaseAxisValue;
    private float mBaseTransPosition;

    private int handleId;
    private final int contentId;

    private View handle;
    private View content;

    private ValueAnimator mAnimator;
    private boolean mExpanded;

    private TensionView tensionView;

    private boolean initialized;

    private SlidingDrawerListener listener;
    private boolean addedTensionView;
    private int startPoint = 0;
    private int endPoint = 0;
    private int offset = 0;

    private boolean startMoving = false;
    private float downX;
    private float downY;

    /**
     * Creates a new SlidingDrawer from a specified set of attributes defined in XML.
     */
    public SlidingDrawer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingDrawer(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SlidingDrawer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SlidingDrawer, defStyleAttr, defStyleRes);
        orientation = a.getInt(R.styleable.SlidingDrawer_sdorientation, ORIENTATION_VERTICAL_DEFAULT);
        allowSingleTap = a.getBoolean(R.styleable.SlidingDrawer_allowSingleTap, true);
        mAnimateOnClick = a.getBoolean(R.styleable.SlidingDrawer_animateOnClick, true);

        Drawable tensionDrawable = a.getDrawable(R.styleable.SlidingDrawer_tensionTween);
        if (null != tensionDrawable) {
            tensionView = new TensionView(this);
            tensionView.setBackgroundDrawable(tensionDrawable);
        }

        handleId = a.getResourceId(R.styleable.SlidingDrawer_handle, 0);
        if (0 == handleId) {
            throw new IllegalArgumentException("The handle attribute is required and must refer to a valid child.");
        }
        contentId = a.getResourceId(R.styleable.SlidingDrawer_content, 0);
        if (0 == contentId) {
            throw new IllegalArgumentException("The content attribute is required and must refer to a valid child.");
        }
        if (handleId == contentId) {
            throw new IllegalArgumentException("The content and handle attributes must refer to different children.");
        }
        a.recycle();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (!addedTensionView) {
            ViewGroup parentView = (ViewGroup) getParent();
            if (null == parentView) {
                throw new IllegalArgumentException("The SlidingDrawer must be added to parent View.");
            }
            parentView.addView(tensionView);
            addedTensionView = true;
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        handle = findViewById(handleId);
        if (null == handle) {
            throw new IllegalArgumentException("The handle attribute is must refer to an existing child.");
        }
        if (allowSingleTap) {
            handle.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    toggleDrawer(mAnimateOnClick);
                }
            });
        }
        setOnTouchListener(this);

        content = findViewById(contentId);
        if (null == content) {
            throw new IllegalArgumentException("The content attribute is must refer to an existing child.");
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        adjustLayoutMargin();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (!initialized) {
            setDrawerClosed(false);
            initialized = true;
            initEndPoint(0);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        HSLog.i("SlidingDrawer", "onTouch(), event = " + event.getAction() + " " + event.getRawY());
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onDrawerScrollStarted(getMotionAxisValue(event));
                break;
            case MotionEvent.ACTION_MOVE:
                //onDrawerScroll(getMotionAxisValue(event));
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                onDrawerScrollEnded(getMotionAxisValue(event));
                break;
            default:
                break;
        }
        return true;
    }

    private void onDrawerScrollStarted(float value) {
        mBaseAxisValue = value;
        mBaseTransPosition = getTransPosition();
        if (null != listener) {
            listener.onScrollStarted();
        }
    }

    private void onDrawerScroll(float value) {
        float trans = value - mBaseAxisValue + mBaseTransPosition;
        if (isMiddle(trans, startPoint, endPoint)) {
            setTransPosition(trans);

            if (null != listener) {
                listener.onScroll(Math.abs(trans), Math.abs(endPoint));
            }
        } else {
            if (trans < startPoint) {
                trans = startPoint;
                setTransPosition(trans);

                if (null != listener) {
                    listener.onScroll(Math.abs(trans), Math.abs(endPoint));
                }
                mBaseAxisValue = value + mBaseTransPosition - startPoint;
            } else if (trans > endPoint) {
                trans = endPoint;
                setTransPosition(trans);

                if (null != listener) {
                    listener.onScroll(Math.abs(trans), Math.abs(endPoint));
                }
                mBaseAxisValue = value + mBaseTransPosition - endPoint;
            }
        }
    }

    private void onDrawerScrollEnded(float value) {
        if (Math.abs(value - mBaseAxisValue) == 0) {
            return;
        }

        boolean willBounceBack = shouldBounceBack();
        HSLog.i("SlidingDrawer", "onDrawerScrollEnded(), value = " + value + ", base axis value = " + mBaseAxisValue + ", will bounce back = " + willBounceBack + ", expanded = " + mExpanded);
        if (mExpanded) {
            if (willBounceBack) {
                setDrawerOpen(true);
            } else {
                setDrawerClosed(true);
            }
        } else {
            if (willBounceBack) {
                setDrawerClosed(true);
            } else {
                setDrawerOpen(true);
            }
        }
    }

    private float getMotionAxisValue(MotionEvent event) {
        return (int) (isHorizontal() ? event.getRawX() : event.getRawY());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean ret = super.onInterceptTouchEvent(ev);
        if (MotionEvent.ACTION_DOWN == ev.getAction()) {
            startMoving = false;
            downX = ev.getRawX();
            downY = ev.getRawY();
        }
        if (!startMoving) {
            startMoving = ret;
            if (MotionEvent.ACTION_MOVE == ev.getAction()) {
                if (Math.abs(ev.getRawX() - downX) >= Dimensions.pxFromDp(5) || Math.abs(ev.getRawY() - downY) >= Dimensions.pxFromDp(5)) {
                    onDrawerScrollStarted(getMotionAxisValue(ev));
                    startMoving = true;
                }
            }
        }
        return startMoving;
    }

    public void setListener(SlidingDrawerListener listener) {
        this.listener = listener;
    }

    public void setHandle(int resId, int uiOffset) {
        if (handleId == resId) {
            initEndPoint(uiOffset);
            return;
        }

        handleId = resId;
        handle = findViewById(handleId);
        if (null == handle) {
            throw new IllegalArgumentException("The handle attribute is must refer to an existing child.");
        }
        if (allowSingleTap) {
            handle.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleDrawer(mAnimateOnClick);
                }
            });
        }
        initEndPoint(uiOffset);
    }

    private void initEndPoint(int uiOffset) {
        if (isHorizontal()) {
            if (ORIENTATION_RIGHT_TO_LEFT == orientation) {
                endPoint = (getWidth() - handle.getWidth() + offset - uiOffset);
            } else {
                endPoint = (handle.getWidth() - getWidth() - offset - uiOffset);
            }
        } else {
            if (ORIENTATION_BOTTOM_UP == orientation) {
                endPoint = content.getHeight() + uiOffset;
            } else {
                endPoint = (handle.getHeight() - getHeight() - offset - uiOffset);
            }
        }
    }

    private boolean isHorizontal() {
        return (ORIENTATION_RIGHT_TO_LEFT == orientation) || (ORIENTATION_LEFT_TO_RIGHT == orientation);
    }

    public boolean isVertical() {
        return (ORIENTATION_BOTTOM_UP == orientation) || (ORIENTATION_TOP_DOWN == orientation);
    }

    protected float getTransPosition() {
        if (isHorizontal()) {
            return this.getTranslationX();
        } else {
            return this.getTranslationY();
        }
    }

    protected void setTransPosition(float position) {
        HSLog.e("set drawer translation Y is " + position);
        if (isHorizontal()) {
            setTranslationX(position);
        } else {
            setTranslationY(position);
        }

        if (null != tensionView) {
            tensionView.updateViewTension();
        }
    }

    private void adjustLayoutMargin() {
        ViewGroup.LayoutParams lp = getLayoutParams();
        if (null == lp || !(lp instanceof MarginLayoutParams)) {
            return;
        }

        MarginLayoutParams mlp = (MarginLayoutParams) lp;
        if (isHorizontal()) {
            if (ORIENTATION_RIGHT_TO_LEFT == orientation) {
                mlp.rightMargin = 0;
            } else {
                mlp.leftMargin = 0;
            }
        } else {
            if (ORIENTATION_BOTTOM_UP == orientation) {
                mlp.bottomMargin = 0;
            } else {
                mlp.topMargin = 0;
            }
        }
        setLayoutParams(mlp);
    }

    public void toggleDrawer(boolean animate) {
        if (mExpanded) {
            setDrawerClosed(animate);
        } else {
            setDrawerOpen(animate);
        }

        if (!animate && null != listener) {
            listener.onScrollEnded(mExpanded);
        }
    }

    public void setDrawerOpen(boolean animate) {
        mExpanded = true;

        if (animate) {
            startOpenAnimator(startPoint);
        } else {
            if (isHorizontal()) {
                setTranslationX(startPoint);
            } else {
                setTranslationY(startPoint);
            }
        }
    }

    public void setDrawerClosed(boolean animate) {
        mExpanded = false;

        if (null != listener) {
            listener.onScrollStarted();
        }
        if (animate) {
            startCloseAnimator(endPoint);
        } else {
            if (isHorizontal()) {
                setTranslationX(endPoint);
            } else {
                setTranslationY(endPoint);
            }
        }
    }

    public void startOpenAnimator(final float... position) {
        if (null != mAnimator) {
            mAnimator.cancel();
        }

        mAnimator = ValueAnimator.ofFloat(getTransPosition(), position[0]);
        mAnimator.setInterpolator(new DecelerateInterpolator());
        mAnimator.setDuration((long) Math.abs((position[0] - getTransPosition()) / 3f));
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator arg0) {
                if (null != listener) {
                    listener.onScrollEnded(mExpanded);
                }
            }
        });
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                setTransPosition(value);
                listener.onScroll(Math.abs(value), Math.abs(endPoint));
            }
        });
        mAnimator.start();
    }

    public void startCloseAnimator(final float... position) {
        if (null != mAnimator) {
            mAnimator.cancel();
        }

        mAnimator = ValueAnimator.ofFloat(getTransPosition(), position[0]);
        mAnimator.setInterpolator(new DecelerateInterpolator());
        mAnimator.setDuration((long) Math.abs((position[0] - getTransPosition()) / 3f));
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator arg0) {
                if (null != listener) {
                    listener.onScrollEnded(mExpanded);
                }
            }
        });
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                setTransPosition(value);
                listener.onScroll(Math.abs(value), Math.abs(endPoint));
            }
        });
        mAnimator.start();
    }

    private boolean isMiddle(float value, float begin, float end) {
        if (begin < end) {
            if (begin <= value && value <= end) {
                return true;
            }
        } else {
            if (end <= value && value <= begin) {
                return true;
            }
        }
        return false;
    }

    protected boolean shouldBounceBack() {
        boolean ret;
        // 修改检验方式，以移动距离是否超过1/4总长为判断，未考虑其他方向，以及超出移动范围的问题
        if (mExpanded) {
            ret = getTransPosition() < (startPoint + (endPoint - startPoint) / 6);
        } else {
            ret = getTransPosition() > (endPoint - (endPoint - startPoint) / 6);
        }
        return ret;
    }

    public void closeDrawer(boolean anim) {
        if (mExpanded) {
            setDrawerClosed(anim);
            if (!anim && null != listener) {
                listener.onScrollEnded(mExpanded);
            }
        }
    }

    public void openDrawer(boolean anim) {
        if (!mExpanded) {
            setDrawerOpen(anim);
            if (!anim && null != listener) {
                listener.onScrollEnded(mExpanded);
            }
        }
    }

    public void doBounceUpAnimation() {
        float startTrans = endPoint;
        float endTrans = endPoint - Dimensions.pxFromDp(50);
        ValueAnimator animator1 = ValueAnimator.ofFloat(startTrans, endTrans);
        animator1.setDuration(150);
        animator1.setInterpolator(new DecelerateInterpolator());
        animator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setTransPosition((float) animation.getAnimatedValue());
                if (null != listener) {
                    listener.onScroll(Math.abs((float) animation.getAnimatedValue()), Math.abs(endPoint));
                }
            }
        });
        ValueAnimator animator2 = ValueAnimator.ofFloat(endTrans, startTrans);
        animator2.setDuration(150);
        animator2.setInterpolator(new AccelerateInterpolator());
        animator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setTransPosition((float) animation.getAnimatedValue());
                if (null != listener) {
                    listener.onScroll(Math.abs((float) animation.getAnimatedValue()), Math.abs(endPoint));
                }
            }
        });
        AnimatorSet set = new AnimatorSet();
        set.playSequentially(animator1, animator2);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                findViewById(R.id.blank_handle).setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                findViewById(R.id.blank_handle).setVisibility(View.INVISIBLE);
            }
        });
        set.start();
    }
}
