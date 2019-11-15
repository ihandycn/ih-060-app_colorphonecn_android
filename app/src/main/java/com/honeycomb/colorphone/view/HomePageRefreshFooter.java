package com.honeycomb.colorphone.view;

import android.content.Context;
import android.graphics.Paint;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.widget.CircularProgressDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.honeycomb.colorphone.R;
import com.scwang.smartrefresh.layout.api.RefreshFooter;
import com.scwang.smartrefresh.layout.api.RefreshKernel;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;

public class HomePageRefreshFooter extends FrameLayout implements RefreshFooter {

    private TextView refreshTextView;
    private ImageView refreshIcon;
    protected boolean mNoMoreData = false;
    private String successText;
    private String failedText;
    private String refreshingText;
    final CircularProgressDrawable drawable = new CircularProgressDrawable(getContext());

    public HomePageRefreshFooter(Context context) {
        super(context);
        init();
    }

    public HomePageRefreshFooter(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HomePageRefreshFooter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 设置刷新成功文字
     *
     * @param successText 文字
     * @return
     */
    public HomePageRefreshFooter setSuccessText(String successText) {
        this.successText = successText;
        return this;
    }

    /**
     * 设置刷新失败文字
     *
     * @param failedText
     * @return
     */
    public HomePageRefreshFooter setFailedText(String failedText) {
        this.failedText = failedText;
        return this;
    }

    /**
     * 设置刷新时显示的文字
     *
     * @param refreshingText
     * @return
     */
    public HomePageRefreshFooter setRefreshingText(String refreshingText) {
        this.refreshingText = refreshingText;
        return this;
    }

    public HomePageRefreshFooter setSuccessText(@StringRes int successTextId) {
        return setSuccessText(getContext().getString(successTextId));
    }

    public HomePageRefreshFooter setFailedText(@StringRes int failedTextId) {
        return setFailedText(getContext().getString(failedTextId));
    }

    public HomePageRefreshFooter setRefreshingText(@StringRes int refreshingTextId) {
        return setRefreshingText(getContext().getString(refreshingTextId));
    }

    /**
     * 设置文字颜色
     */
    public HomePageRefreshFooter setRefreshTextColor(@ColorRes int color) {

        refreshTextView.setTextColor(getContext().getResources().getColor(color));
        return this;
    }

    private void init() {
        View.inflate(getContext(), R.layout.refresh_main_page_footer, this);
        refreshTextView = findViewById(R.id.tv_text);
        refreshIcon = findViewById(R.id.iv_icon);
        refreshTextView.setTextColor(getContext().getResources().getColor(R.color.refresh_text_view));

        drawable.setStartEndTrim(0, 0.75f);
        drawable.setStrokeWidth(10f);
        drawable.setStrokeCap(Paint.Cap.ROUND);
        drawable.setCenterRadius(50f);
        drawable.setStyle(CircularProgressDrawable.DEFAULT);
        drawable.setColorSchemeColors(0x8481a9, 0x8481a9);

        refreshIcon.setImageDrawable(drawable);
    }

    @NonNull
    @Override
    public View getView() {
        return this;
    }

    @NonNull
    @Override
    public SpinnerStyle getSpinnerStyle() {
        return SpinnerStyle.Translate;
    }

    @Override
    public void setPrimaryColors(int... colors) {

    }

    @Override
    public void onInitialized(@NonNull RefreshKernel kernel, int height, int maxDragHeight) {

    }

    @Override
    public void onMoving(boolean isDragging, float percent, int offset, int height, int maxDragHeight) {

    }

    @Override
    public void onReleased(@NonNull RefreshLayout refreshLayout, int height, int maxDragHeight) {

    }

    @Override
    public void onStartAnimator(@NonNull RefreshLayout refreshLayout, int height, int maxDragHeight) {

    }

    @Override
    public int onFinish(@NonNull RefreshLayout refreshLayout, boolean success) {
        drawable.stop();
        refreshIcon.setVisibility(GONE);
        if (!mNoMoreData) {
            refreshTextView.setText(success ? "加载完成" : "加载失败");
        }
        return 500;
    }

    @Override
    public void onHorizontalDrag(float percentX, int offsetX, int offsetMax) {

    }

    @Override
    public boolean isSupportHorizontalDrag() {
        return false;
    }

    @Override
    public void onStateChanged(@NonNull RefreshLayout refreshLayout, @NonNull RefreshState oldState, @NonNull RefreshState newState) {
        if (!mNoMoreData) {
            switch (newState) {
                case None:
                case PullUpToLoad:
                    refreshTextView.setText("上拉加载更多");
                    break;
                case Loading:
                case LoadReleased:
                    drawable.start();
                    refreshIcon.setVisibility(VISIBLE);
                    refreshTextView.setText("正在加载中...");
                    break;
                case ReleaseToLoad:
                    refreshTextView.setText("释放立即加载");
                    break;
                case Refreshing:
                    refreshTextView.setText("正在刷新...");
                    break;
            }
        }
    }

    @Override
    public boolean setNoMoreData(boolean noMoreData) {
        if (mNoMoreData != noMoreData) {
            mNoMoreData = noMoreData;
            if (noMoreData) {
                refreshTextView.setText("没有更多数据了");
                refreshIcon.setVisibility(GONE);
            } else {
                refreshTextView.setText("上拉加载更多");
                refreshIcon.setVisibility(VISIBLE);
            }
        }
        return true;
    }
}