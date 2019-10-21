package com.acb.libwallpaper.live.view.recyclerview;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Choreographer;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.ihs.commons.utils.HSLog;

import java.lang.ref.WeakReference;

import static com.acb.libwallpaper.live.view.recyclerview.PullToRefreshLayout.State.DONE;
import static com.acb.libwallpaper.live.view.recyclerview.PullToRefreshLayout.State.INIT;
import static com.acb.libwallpaper.live.view.recyclerview.PullToRefreshLayout.State.LOADING;
import static com.acb.libwallpaper.live.view.recyclerview.PullToRefreshLayout.State.REFRESHING;
import static com.acb.libwallpaper.live.view.recyclerview.PullToRefreshLayout.State.RELEASE_TO_LOAD;
import static com.acb.libwallpaper.live.view.recyclerview.PullToRefreshLayout.State.RELEASE_TO_REFRESH;

/**
 * Imported from library https://github.com/lynnchurch/PullToRefresh with code formatting and implementation tweaks.
 * <p/>
 * License: https://github.com/lynnchurch/PullToRefresh/blob/master/LICENSE
 * --
 * <p/>
 * 自定义的布局，用来管理一个下拉头，一个是包含内容的 pullableView（可以是实现 Pullable 接口的的任何 View）
 * 更多详解见博客 http://blog.csdn.net/zhongkejingwang/article/details/38868463
 */
public class PullToRefreshLayout extends FrameLayout {

    public static final String TAG = "PullToRefreshLayout";

    // 当前状态
    private State mState = INIT;
    // 刷新回调接口
    private OnPullListener mListener;
    // 刷新成功
    public static final int SUCCEED = 0;
    // 刷新失败
    public static final int FAIL = 1;

    // 上一个事件点 Y 坐标
    private float mLastY;
    // 下拉的距离。注意：pullDownY和pullUpY不可能同时不为0
    private float mPullDownY = 0;
    // 上拉的距离
    private float mPullUpY = 0;

    // 释放刷新的距离
    private float mRefreshDist = 200;
    // 释放加载的距离
    private float mLoadMoreDist = 200;

    private RepeatedTimer mRepeatedTimer;
    // 回滚速度
    public float mMoveSpeed = 8;
    // 第一次执行布局
    private boolean mHasLayout = false;
    // 在刷新过程中滑动操作
    private boolean mIsTouching = false;
    // 手指滑动距离与下拉头的滑动距离比，中间会随正切函数变化
    private float mRadio = 2;

    // 实现了 Pullable 接口的View
    private View mPullableView;
    // 过滤多点触碰
    private int mEvents;
    // 这两个变量用来控制pull的方向，如果不加控制，当情况满足可上拉又可下拉时没法下拉
    private boolean mCanPullDown = true;
    private boolean mCanPullUp = true;

    private boolean mPullUpEnable = true;

    // 下拉刷新过程监听器
    private OnPullProcessListener mOnRefreshProcessListener;

    // 下拉头
    private View mRefreshView;

    // 是否已经准备下拉
    private boolean mPreparedPullDown;

    enum State {
        INIT,
        RELEASE_TO_REFRESH,
        REFRESHING,
        RELEASE_TO_LOAD,
        LOADING,
        DONE,
    }

    public PullToRefreshLayout(Context context) {
        this(context, null, 0);
    }

    public PullToRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullToRefreshLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mRepeatedTimer = new RepeatedTimer(this);
    }

    /**
     * 获取下拉触发刷新的距离
     *
     * @return
     */
    public float getRefreshDist() {
        return mRefreshDist;
    }

    /**
     * 获取可拉取的视图
     */
    public View getPullableView() {
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            if (v instanceof Pullable) {
                mPullableView = v;
                return mPullableView;
            }
        }
        return null;
    }

    /**
     * 设置自定义下拉头
     */
    public void setCustomRefreshView(View v) {
        addView(v);
        mRefreshView = v;
    }

    private void hide() {
        mRepeatedTimer.schedule();
    }

    /**
     * 完成刷新操作，显示刷新结果。注意：刷新完成后一定要调用这个方法
     *
     * @param refreshResult PullToRefreshLayout.SUCCEED 代表成功，PullToRefreshLayout.FAIL 代表失败
     */
    public void refreshFinish(final int refreshResult) {
        post(new Runnable() {
            @Override
            public void run() {
                if (mPullDownY > 0) {
                    // 刷新结果不停留
                    new RemainHandler(PullToRefreshLayout.this).sendEmptyMessageDelayed(0, 1000);
                } else {
                    changeState(DONE);
                    hide();
                    if (null != mOnRefreshProcessListener) {
                        mOnRefreshProcessListener.onFinish(mRefreshView, OnPullProcessListener.REFRESH);
                    }
                }
            }
        });
    }

    /**
     * 加载完毕，显示加载结果。注意：加载完成后一定要调用这个方法
     *
     * @param refreshResult PullToRefreshLayout.SUCCEED 代表成功，PullToRefreshLayout.FAIL 代表失败
     */
    public void loadMoreFinish(int refreshResult) {
        post(new Runnable() {
            @Override
            public void run() {
                if (mPullUpY < 0) {
                    // 刷新结果停留 1 秒
                    new RemainHandler(PullToRefreshLayout.this).sendEmptyMessageDelayed(0, 1000);
                } else {
                    changeState(DONE);
                    hide();
                }
            }
        });
    }

    private void changeState(State to) {
        mState = to;
        switch (mState) {
            case INIT:
                mPreparedPullDown = false;
                break;
            case RELEASE_TO_REFRESH:
                if (null != mOnRefreshProcessListener) {
                    mOnRefreshProcessListener.onStart(mRefreshView, OnPullProcessListener.REFRESH);
                }
                break;
            case REFRESHING:
                if (null != mOnRefreshProcessListener) {
                    mOnRefreshProcessListener.onHandling(mRefreshView, OnPullProcessListener.REFRESH);
                }
                break;
            case RELEASE_TO_LOAD:
                break;
            case LOADING:
                break;
            case DONE:
                mRefreshView.setVisibility(View.VISIBLE);
                break;
        }
    }

    /**
     * 不限制上拉或下拉
     */
    private void releasePull() {
        mCanPullDown = true;
        mCanPullUp = true;
    }

    /**
     * 设置是否可上拉刷新
     */
    public void setPullUpEnabled(boolean pullUpEnable) {
        mPullUpEnable = pullUpEnable;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mRepeatedTimer.isRunning()) {
            return super.dispatchTouchEvent(ev);
        }
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = ev.getY();
                mRepeatedTimer.cancel();
                mEvents = 0;
                releasePull();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_POINTER_UP:
                // 过滤多点触碰
                mEvents = -1;
                break;
            case MotionEvent.ACTION_MOVE:
                if (null != mOnRefreshProcessListener && mPullDownY > 0 && mState != REFRESHING && mState != DONE) {
                    if (!mPreparedPullDown) {
                        mPreparedPullDown = true;
                        if (null != mOnRefreshProcessListener) {
                            mOnRefreshProcessListener.onPrepare(mRefreshView, OnPullProcessListener.REFRESH);
                        }
                    }
                    mOnRefreshProcessListener.onPull(mRefreshView, mPullDownY, OnPullProcessListener.REFRESH);
                }
                if (mEvents == 0) {
                    if (mPullDownY > 0
                            || (((Pullable) mPullableView).canPullDown()
                            && mCanPullDown && mState != LOADING)) {
                        // 可以下拉，正在加载时不能下拉
                        // 对实际滑动距离做缩小，造成用力拉的感觉
                        mPullDownY = mPullDownY + (ev.getY() - mLastY) / mRadio;
                        if (mPullDownY < 0) {
                            mPullDownY = 0;
                            mCanPullDown = false;
                            mCanPullUp = true;
                        }
                        if (mPullDownY > getMeasuredHeight()) {
                            mPullDownY = getMeasuredHeight();
                        }
                        if (mState == REFRESHING) {
                            // 正在刷新的时候触摸移动
                            mIsTouching = true;
                        }
                    } else if (mPullUpY < 0
                            || (((Pullable) mPullableView).canPullUp() && mCanPullUp
                            && mPullUpEnable && mState != REFRESHING)) {
                        // 可以上拉，正在刷新时不能上拉
                        mPullUpY = mPullUpY + (ev.getY() - mLastY) / mRadio;
                        if (mPullUpY > 0) {
                            mPullUpY = 0;
                            mCanPullDown = true;
                            mCanPullUp = false;
                        }
                        if (mPullUpY < -getMeasuredHeight()) {
                            mPullUpY = -getMeasuredHeight();
                        }
                        if (mState == LOADING) {
                            // 正在加载的时候触摸移动
                            mIsTouching = true;
                        }
                    } else
                        releasePull();
                } else
                    mEvents = 0;
                mLastY = ev.getY();
                // 根据下拉距离改变比例
                mRadio = (float) (2 + 2 * Math.tan(Math.PI / 2 / getMeasuredHeight() * (mPullDownY + Math.abs(mPullUpY))));
                if (mPullDownY > 0 || mPullUpY < 0) {
                    requestLayout();
                }
                if (mPullDownY > 0) {
                    if (mPullDownY <= mRefreshDist
                            && (mState == RELEASE_TO_REFRESH || mState == DONE)) {
                        // 如果下拉距离没达到刷新的距离且当前状态是释放刷新，改变状态为下拉刷新
                        changeState(INIT);
                    }
                    if (mPullDownY >= mRefreshDist && mState == INIT) {
                        // 如果下拉距离达到刷新的距离且当前状态是初始状态刷新，改变状态为释放刷新
                        changeState(RELEASE_TO_REFRESH);
                    }
                } else if (mPullUpY < 0) {
                    // 下面是判断上拉加载的，同上，注意pullUpY是负值
                    if (-mPullUpY <= mLoadMoreDist
                            && (mState == RELEASE_TO_LOAD || mState == DONE)) {
                        changeState(INIT);
                    }
                    // 上拉操作
                    if (-mPullUpY >= mLoadMoreDist && mState == INIT) {
                        changeState(RELEASE_TO_LOAD);
                    }

                }
                break;
            case MotionEvent.ACTION_UP:
                if (mPullDownY > mRefreshDist || -mPullUpY > mLoadMoreDist) {
                    // 正在刷新时往下拉（正在加载时往上拉），释放后下拉头（上拉头）不隐藏
                    mIsTouching = false;
                }
                if (mState == RELEASE_TO_REFRESH) {
                    changeState(REFRESHING);
                    // 刷新操作
                    if (mListener != null)
                        mListener.onRefresh(this);
                } else if (mState == RELEASE_TO_LOAD) {
                    changeState(LOADING);
                    // 加载操作
                    if (mListener != null)
                        mListener.onLoadMore(this);
                }
                if (mState != DONE && (mPullDownY + Math.abs(mPullUpY) != 0)) {
                    hide();
                }
                break;
            default:
                break;
        }
        // 事件分发交给父类
        try {
            super.dispatchTouchEvent(ev);
        } catch (Exception e) {
            HSLog.w("News.PullToRefreshCrash", "Exception in PullToRefreshLayout touch event: " + e.getMessage());
        }
        return true;
    }

    /**
     * 自动刷新
     */
    public void autoRefresh() {
        changeState(REFRESHING);
        mRefreshView.measure(0, 0);
        mPullDownY = mRefreshView.getMeasuredHeight();
        requestLayout();
        if (mListener != null) {
            mListener.onRefresh(this);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (!mHasLayout) {
            // 这里是第一次进来的时候做一些初始化
            getPullableView();
            mHasLayout = true;
            mRefreshView.measure(0, 0);
        }
        mRefreshDist = mRefreshView.getMeasuredHeight();
        // 改变子控件的布局，这里直接用(pullDownY + pullUpY)作为偏移量，这样就可以不对当前状态作区分
        mRefreshView.layout(0,
                (int) (mPullDownY + mPullUpY) - mRefreshView.getMeasuredHeight(),
                mRefreshView.getMeasuredWidth(), (int) (mPullDownY + mPullUpY));
        try {
            mPullableView.layout(0, (int) (mPullDownY + mPullUpY),
                    mPullableView.getMeasuredWidth(), (int) (mPullDownY + mPullUpY) + mPullableView.getMeasuredHeight());
        } catch (IllegalArgumentException e) {
            // Re-layout
            // FIXME: dirty fix here. Find root cause.
            e.printStackTrace();
            HSLog.w("News.PullToRefreshCrash", "Exception in PullToRefreshLayout layout process: " + e.getMessage());
            requestLayout();
        }
    }

    static class RepeatedTimer {
        private WeakReference<PullToRefreshLayout> mLayout;

        private Choreographer mChoregrapher = Choreographer.getInstance();
        private FrameCallback mFrameCallback = new FrameCallback();

        private boolean mIsRunning;

        RepeatedTimer(PullToRefreshLayout layout) {
            mLayout = new WeakReference<>(layout);
        }

        void schedule() {
            mChoregrapher.postFrameCallback(mFrameCallback);
            mIsRunning = true;
        }

        void cancel() {
            mChoregrapher.removeFrameCallback(mFrameCallback);
            mIsRunning = false;
        }

        boolean isRunning() {
            return mIsRunning;
        }

        private class FrameCallback implements Choreographer.FrameCallback {

            @Override
            public void doFrame(long frameTimeNanos) {
                PullToRefreshLayout layout = mLayout.get();
                if (layout != null) {
                    // Must call this at first
                    mChoregrapher.postFrameCallback(this);
                    layout.mRefreshDist = layout.mRefreshView.getMeasuredHeight();
                    // 回弹速度随下拉距离moveDeltaY增大而增大
                    layout.mMoveSpeed = (float) (16 + 5 * Math.tan(Math.PI / 2
                            / layout.getMeasuredHeight()
                            * (layout.mPullDownY + Math.abs(layout.mPullUpY))));
                    if (!layout.mIsTouching) {
                        // 正在刷新，且没有往上推的话则悬停，显示"正在刷新..."
                        if (layout.mState == REFRESHING
                                && layout.mPullDownY <= layout.mRefreshDist) {
                            layout.mPullDownY = layout.mRefreshDist;
                            layout.mRepeatedTimer.cancel();
                            layout.requestLayout();
                            return;
                        } else if (layout.mState == LOADING
                                && -layout.mPullUpY <= layout.mLoadMoreDist) {
                            layout.mPullUpY = -layout.mLoadMoreDist;
                            layout.mRepeatedTimer.cancel();
                        }

                    }
                    if (layout.mPullDownY > 0) {
                        layout.mPullDownY -= layout.mMoveSpeed;
                    } else if (layout.mPullUpY < 0) {
                        layout.mPullUpY += layout.mMoveSpeed;
                    }
                    if (layout.mPullDownY < 0) {
                        // 已完成回弹
                        layout.mPullDownY = 0;
                        // 隐藏下拉头时有可能还在刷新，只有当前状态不是正在刷新时才改变状态
                        if (layout.mState != REFRESHING && layout.mState != LOADING) {
                            layout.changeState(INIT);
                        }
                        layout.mRepeatedTimer.cancel();
                        layout.requestLayout();
                    }
                    if (layout.mPullUpY > 0) {
                        // 已完成回弹
                        layout.mPullUpY = 0;
                        // 隐藏上拉头时有可能还在刷新，只有当前状态不是正在刷新时才改变状态
                        if (layout.mState != REFRESHING && layout.mState != LOADING) {
                            layout.changeState(INIT);
                        }
                        layout.mRepeatedTimer.cancel();
                        layout.requestLayout();
                    }
                    // 刷新布局,会自动调用onLayout
                    layout.requestLayout();
                    // 没有拖拉或者回弹完成
                    if (layout.mPullDownY + Math.abs(layout.mPullUpY) == 0) {
                        layout.mRepeatedTimer.cancel();
                    }
                }
            }
        }
    }

    /**
     * 刷新结果停留的 handler
     */
    static class RemainHandler extends Handler {
        private WeakReference<PullToRefreshLayout> mLayout;

        RemainHandler(PullToRefreshLayout layout) {
            mLayout = new WeakReference<>(layout);
        }

        @Override
        public void handleMessage(Message msg) {
            PullToRefreshLayout layout = mLayout.get();
            if (null != layout) {
                layout.changeState(DONE);
                layout.hide();
                if (null != layout.mOnRefreshProcessListener) {
                    layout.mOnRefreshProcessListener.onFinish(layout.mRefreshView, OnPullProcessListener.REFRESH);
                }
            }
        }
    }

    public void setOnPullListener(OnPullListener listener) {
        mListener = listener;
    }

    /**
     * 设置下拉刷新过程监听器
     */
    public void setOnRefreshProcessListener(OnPullProcessListener onPullProcessListener) {
        mOnRefreshProcessListener = onPullProcessListener;
    }

    /**
     * 刷新加载回调接口
     *
     * @author chenjing
     */
    public interface OnPullListener {
        /**
         * 刷新操作
         */
        void onRefresh(PullToRefreshLayout pullToRefreshLayout);

        /**
         * 加载操作
         */
        void onLoadMore(PullToRefreshLayout pullToRefreshLayout);
    }

    /**
     * 下拉刷新或上拉加载更多过程监听器
     *
     * @author LynnChurch
     */
    public interface OnPullProcessListener {
        int REFRESH = 1; // 刷新

        /**
         * 准备 （提示下拉刷新或上拉加载更多）
         *
         * @param which 刷新或加载更多
         */
        void onPrepare(View v, int which);

        /**
         * 开始 （提示释放刷新或释放加载更多）
         *
         * @param which 刷新或加载更多
         */
        void onStart(View v, int which);

        /**
         * 处理中
         *
         * @param which 刷新或加载更多
         */
        void onHandling(View v, int which);


        /**
         * 处理返回结果
         *
         * @param success 成功或失败
         * @param count   返回的新条数
         */
        void onHandlingResult(View v, boolean success, int count);

        /**
         * 完成
         *
         * @param which 刷新或加载更多
         */
        void onFinish(View v, int which);

        /**
         * 用于获取拉取的距离
         *
         * @param which 刷新或加载更多
         */
        void onPull(View v, float pullDistance, int which);
    }
}
