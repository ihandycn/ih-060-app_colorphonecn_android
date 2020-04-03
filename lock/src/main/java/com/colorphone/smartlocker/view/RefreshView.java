package com.colorphone.smartlocker.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Scroller;

import com.colorphone.smartlocker.RefreshViewState;
import com.colorphone.smartlocker.utils.DisplayUtils;
import com.colorphone.smartlocker.utils.NetworkStatusUtils;
import com.colorphone.smartlocker.utils.NewsUtils;
import com.ihs.commons.utils.HSLog;

public class RefreshView extends LinearLayout {

    public interface RefreshViewListener {
        /**
         * @param isPullDown 是不是由下拉手势引起的刷新，是则返回true，反之则是自动刷新或者是调用{@link #startRefresh()}引起的刷新
         */
        void onStartRefresh(boolean isPullDown);
    }

    private static final String TAG = "RefreshView";
    private static final float OFFSET_RADIO = 1.5f; /*  阻尼系数 */

    private int headerViewHeight; // header view's height
    private int lastY = -1; // save event y
    private int lastX = -1; // save event x
    public boolean pullRefreshing = false; // is refreshing.
    private RefreshViewListener refreshViewListener;
    private RefreshViewHeader refreshViewHeader;

    private int initialMotionY;
    private int touchSlop;

    private MotionEvent lastMoveEvent;
    private boolean hasSendCancelEvent = false;
    private boolean hasSendDownEvent = false;
    private Scroller scroller;
    private boolean moveForHorizontal = false;

    private RefreshViewState refreshViewState = null;
    /**
     * 布局是否准备好了，准备好以后才能进行自动刷新这种操作
     */
    private boolean layoutReady = false;
    private boolean needToRefresh = true;

    private int headMoveDistance;//header可下拉的最大距离

    private boolean isIntercepted = false;
    private int offsetY;
    private RecyclerView recyclerView;
    private boolean stoppingRefresh = false;

    public RefreshView(Context context) {
        super(context);
        initView();
    }

    public RefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public RefreshView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        setClickable(true);
        setLongClickable(true);
        scroller = new Scroller(getContext(), new LinearInterpolator());

        initWithContext();
        setOrientation(VERTICAL);
    }

    private void initWithContext() {
        addHeaderView();
        this.getViewTreeObserver().addOnGlobalLayoutListener(
                new OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {
                        layoutReady = true;
                        if (needToRefresh) {
                            startRefresh();
                        }
                        setHeadMoveLargestDistance();

                        recyclerView = (RecyclerView) getChildAt(1);
                        recyclerView.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);

                        // 移除视图树监听器
                        removeViewTreeObserver(this);
                    }
                });
        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    private void addHeaderView() {
        if (refreshViewHeader == null) {
            refreshViewHeader = new RefreshViewHeader(getContext());
        }
        dealAddHeaderView();
    }

    private void dealAddHeaderView() {
        if (indexOfChild(refreshViewHeader) == -1) {
            NewsUtils.removeViewFromParent(refreshViewHeader);
            addView(refreshViewHeader, 0);
            checkPullRefreshEnable();
        }
    }

    public void removeViewTreeObserver(OnGlobalLayoutListener listener) {
        getViewTreeObserver().removeOnGlobalLayoutListener(listener);
    }

    private void getHeaderHeight() {
        if (refreshViewHeader != null) {
            headerViewHeight = refreshViewHeader.getHeaderHeight();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int childCount = getChildCount();
        final int paddingLeft = getPaddingLeft();
        final int paddingRight = getPaddingRight();
        final int paddingTop = getPaddingTop();
        final int paddingBottom = getPaddingBottom();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            int childWidthSpec = MeasureSpec.makeMeasureSpec(width - lp.leftMargin - lp.rightMargin - paddingLeft - paddingRight, MeasureSpec.EXACTLY);
            int childHeightSpec = getChildMeasureSpec(heightMeasureSpec,
                    paddingTop + paddingBottom + lp.topMargin + lp.bottomMargin, lp.height);
            child.measure(childWidthSpec, childHeightSpec);
        }
        setMeasuredDimension(width, height);
        getHeaderHeight();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t2, int r, int b) {
        HSLog.d(TAG, "onLayout offsetY=" + offsetY);
        try {
            int childCount = getChildCount();
            int top = getPaddingTop() + offsetY;
            int adHeight = 0;
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                LayoutParams margins = (LayoutParams) child.getLayoutParams();
                int topMargin = margins.topMargin;
                int bottomMargin = margins.bottomMargin;
                int leftMargin = margins.leftMargin;
                l = leftMargin + getPaddingLeft();
                top += topMargin;
                r = child.getMeasuredWidth();
                if (child.getVisibility() != View.GONE) {
                    if (i == 0) {
                        adHeight = child.getMeasuredHeight() - headerViewHeight;
                        child.layout(l, top - headerViewHeight, l + r, top + adHeight);
                        top += adHeight;
                    } else if (i == 1) {
                        int childHeight = child.getMeasuredHeight() - adHeight;
                        int bottom = childHeight + top;
                        child.layout(l, top, l + r, bottom);
                        top += childHeight + bottomMargin;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (refreshViewState == RefreshViewState.STATE_REFRESHING) {
            return false;
        }

        final int action = ev.getAction();
        int deltaY;
        int deltaX;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                hasSendCancelEvent = false;
                hasSendDownEvent = false;
                lastY = (int) ev.getRawY();
                lastX = (int) ev.getRawX();
                initialMotionY = lastY;
                break;
            case MotionEvent.ACTION_MOVE:
                lastMoveEvent = ev;
                if (stoppingRefresh || !isEnabled()) {
                    return super.dispatchTouchEvent(ev);
                }
                int currentY = (int) ev.getRawY();
                int currentX = (int) ev.getRawX();
                deltaY = currentY - lastY;
                deltaX = currentX - lastX;
                lastY = currentY;
                lastX = currentX;
                // intercept the MotionEvent only when user is not scrolling
                if (!isIntercepted) {
                    if (Math.abs(currentY - initialMotionY) >= touchSlop) {
                        isIntercepted = true;
                    } else {
                        return super.dispatchTouchEvent(ev);
                    }
                }
                if (!moveForHorizontal && Math.abs(deltaX) > touchSlop
                        && Math.abs(deltaX) > Math.abs(deltaY)) {
                    if (offsetY == 0) {
                        moveForHorizontal = true;
                    }
                }
                if (moveForHorizontal) {
                    return super.dispatchTouchEvent(ev);
                }
                if (deltaY > 0 && offsetY <= headMoveDistance || deltaY < 0) {
                    deltaY = (int) (deltaY / OFFSET_RADIO);
                } else {
                    return super.dispatchTouchEvent(ev);
                }
                if (isRefreshViewOnTop() && ((deltaY > 0) || (deltaY < 0 && hasHeaderPullDown()))) {
                    sendCancelEvent();
                    updateHeaderHeight(deltaY);
                } else if (deltaY != 0 && (isRefreshViewOnTop() && !hasHeaderPullDown())) {
                    if (Math.abs(deltaY) > 0) {
                        sendDownEvent();
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (hasHeaderPullDown()) {
                    if (!stoppingRefresh && !pullRefreshing && offsetY > headerViewHeight) {
                        pullRefreshing = true;
                        refreshViewHeader.onStateRefreshing();
                        refreshViewState = RefreshViewState.STATE_REFRESHING;
                        if (refreshViewListener != null) {
                            refreshViewListener.onStartRefresh(true);
                        }
                    }
                    resetHeaderHeight();
                }
                lastY = -1; // reset
                lastX = -1;
                initialMotionY = 0;
                isIntercepted = false;
                moveForHorizontal = false;
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private void sendCancelEvent() {
        if (!hasSendCancelEvent) {
            HSLog.d(TAG, "sendCancelEvent");
            hasSendCancelEvent = true;
            hasSendDownEvent = false;
            MotionEvent last = lastMoveEvent;
            MotionEvent e = MotionEvent.obtain(
                    last.getDownTime(),
                    last.getEventTime()
                            + ViewConfiguration.getLongPressTimeout(),
                    MotionEvent.ACTION_CANCEL, last.getX(), last.getY(),
                    last.getMetaState());
            dispatchTouchEventSupper(e);
        }
    }

    /**
     * header可下拉的最大距离
     */
    public void setHeadMoveLargestDistance() {
        //手指抬起时的回弹距离
        int headMoveUpOffset = (int) (refreshViewHeader.getHeight() * 0.3f);
        headMoveDistance = refreshViewHeader.getHeight() + headMoveUpOffset;
    }

    private void sendDownEvent() {
        if (!hasSendDownEvent) {
            HSLog.d(TAG, "sendDownEvent");
            hasSendCancelEvent = false;
            hasSendDownEvent = true;
            isIntercepted = false;
            final MotionEvent last = lastMoveEvent;
            if (last == null) {
                return;
            }
            MotionEvent e = MotionEvent.obtain(last.getDownTime(),
                    last.getEventTime(), MotionEvent.ACTION_DOWN, last.getX(),
                    last.getY(), last.getMetaState());
            dispatchTouchEventSupper(e);
        }
    }

    public void dispatchTouchEventSupper(MotionEvent e) {
        super.dispatchTouchEvent(e);
    }

    private void checkPullRefreshEnable() {
        if (refreshViewHeader == null) {
            return;
        }
        refreshViewHeader.show();
    }

    private void updateHeaderHeight(int deltaY) {
        moveView(deltaY);

        if (!pullRefreshing) {
            if (offsetY > headerViewHeight) {
                if (refreshViewState != RefreshViewState.STATE_READY) {
                    refreshViewHeader.onStateReady();
                    refreshViewState = RefreshViewState.STATE_READY;
                }
            } else {
                if (refreshViewState != RefreshViewState.STATE_NORMAL) {
                    refreshViewHeader.onStateNormal();
                    refreshViewState = RefreshViewState.STATE_NORMAL;
                }
            }
        }
    }

    public void startRefresh() {
        if (offsetY != 0 || pullRefreshing || !isEnabled()) {
            return;
        }
        if (layoutReady) {
            needToRefresh = false;
            pullRefreshing = false;
            if (refreshViewListener != null) {
                refreshViewListener.onStartRefresh(false);
            }
        } else {
            needToRefresh = true;
        }
    }

    /**
     * reset header view's height.
     */
    private void resetHeaderHeight() {
        float height = offsetY;
        // refreshing and header isn't shown fully. do nothing.
        if (pullRefreshing && (height <= headerViewHeight || height == 0)) {
            return;
        }
        int offsetY;
        if (pullRefreshing) {
            offsetY = headerViewHeight - this.offsetY;
            startScroll(offsetY, NewsUtils.computeScrollVerticalDuration(offsetY, getHeight()));
        } else {
            offsetY = 0 - this.offsetY;
            startScroll(offsetY, NewsUtils.computeScrollVerticalDuration(offsetY, getHeight()));
        }
        HSLog.d(TAG, "resetHeaderHeight offsetY=" + offsetY);
    }

    public void moveView(int deltaY) {
        offsetY += deltaY;
        refreshViewHeader.offsetTopAndBottom(deltaY);
        recyclerView.offsetTopAndBottom(deltaY);
        ViewCompat.postInvalidateOnAnimation(this);
        if (refreshViewListener != null && (isRefreshViewOnTop() || pullRefreshing)) {
            double headerMovePercent = 1.0 * offsetY / headerViewHeight;
            refreshViewHeader.onHeaderMove(headerMovePercent);
        }
    }

    /**
     * stop refresh, reset header view.
     */
    public void stopRefresh() {
        HSLog.d(TAG, "stopRefresh pullRefreshing=" + pullRefreshing);
        if (pullRefreshing) {
            stoppingRefresh = true;

            refreshViewHeader.onStateFinish();
            refreshViewState = RefreshViewState.STATE_COMPLETE;

            startScroll(-refreshViewHeader.getHeaderHeight() + DisplayUtils.dpToPx(getContext(), 26), 200);

            postDelayed(new Runnable() {
                @Override
                public void run() {
                    pullRefreshing = false;
                    if (stoppingRefresh) {
                        resetHeaderHeight();
                    }
                }
            }, NetworkStatusUtils.isNetworkConnected(getContext()) ? 2000L : 200L);
        }
    }

    /**
     * @param offsetY  滑动偏移量，负数向上滑，正数反之
     * @param duration 滑动持续时间
     */
    public void startScroll(int offsetY, int duration) {
        scroller.startScroll(0, this.offsetY, 0, offsetY, duration);
        post(new Runnable() {
            @Override
            public void run() {
                if (scroller.computeScrollOffset()) {
                    int lastScrollY = RefreshView.this.offsetY;
                    int currentY = scroller.getCurrY();
                    int offsetY = currentY - lastScrollY;
                    moveView(offsetY);
                    int[] location = new int[2];
                    refreshViewHeader.getLocationInWindow(location);
                    HSLog.d(TAG, "currentY=" + currentY + "; offsetY=" + RefreshView.this.offsetY);
                    post(this);
                } else {
                    int currentY = scroller.getCurrY();
                    if (RefreshView.this.offsetY == 0) {
                        stoppingRefresh = false;
                    } else {
                        //有时scroller已经停止了，但是却没有回到应该在的位置，执行下面的方法恢复
                        if (stoppingRefresh && !pullRefreshing) {
                            startScroll(-currentY, NewsUtils.computeScrollVerticalDuration(currentY, getHeight()));
                        }
                    }
                }
            }
        });
    }

    public void setRefreshViewListener(RefreshViewListener refreshViewListener) {
        this.refreshViewListener = refreshViewListener;
    }

    public boolean hasHeaderPullDown() {
        return offsetY > 0;
    }

    public boolean isRefreshViewOnTop() {
        return !(recyclerView.canScrollVertically(-1) || recyclerView.getScrollY() > 0);
    }

}