package com.honeycomb.colorphone.uploadview;

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
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshKernel;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;

public class ClassicHeader extends FrameLayout implements RefreshHeader {

    private TextView refreshTextView;
    private ImageView refreshIcon;
    private String successText;
    private String failedText;
    private String refreshingText;
    final CircularProgressDrawable drawable = new CircularProgressDrawable(getContext());

    public ClassicHeader(Context context) {
        super(context);
        init();
    }

    public ClassicHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ClassicHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 设置刷新成功文字
     *
     * @param successText 文字
     * @return
     */
    public ClassicHeader setSuccessText(String successText) {
        this.successText = successText;
        return this;
    }

    /**
     * 设置刷新失败文字
     *
     * @param failedText
     * @return
     */
    public ClassicHeader setFailedText(String failedText) {
        this.failedText = failedText;
        return this;
    }

    /**
     * 设置刷新时显示的文字
     *
     * @param refreshingText
     * @return
     */
    public ClassicHeader setRefreshingText(String refreshingText) {
        this.refreshingText = refreshingText;
        return this;
    }

    public ClassicHeader setSuccessText(@StringRes int successTextId) {
        return setSuccessText(getContext().getString(successTextId));
    }

    public ClassicHeader setFailedText(@StringRes int failedTextId) {
        return setFailedText(getContext().getString(failedTextId));
    }

    public ClassicHeader setRefreshingText(@StringRes int refreshingTextId) {
        return setRefreshingText(getContext().getString(refreshingTextId));
    }

    /**
     * 设置文字颜色
     */
    public ClassicHeader setRefreshTextColor(@ColorRes int color) {

        refreshTextView.setTextColor(getContext().getResources().getColor(color));
        return this;
    }

    private void init() {
        View.inflate(getContext(), R.layout.refresh_classic_header, this);
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
        drawable.start();
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
        return 0;
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
        switch (newState) {
            case None:
            case PullDownToRefresh:
                drawable.start();
                refreshTextView.setText("下拉刷新");
                break;
            case Refreshing:
                refreshTextView.setText("刷新中");
                break;
            case ReleaseToRefresh:
                refreshTextView.setText("松开刷新");
                break;
            default:
                break;
        }
    }
}