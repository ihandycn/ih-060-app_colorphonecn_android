package com.honeycomb.colorphone.uploadview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.notification.NotificationConstants;
import com.honeycomb.colorphone.theme.ThemeList;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;

public class PublishVideoView extends RelativeLayout implements PublishVideoContract.View, INotificationObserver, View.OnClickListener {

    private RecyclerView recyclerView;
    private SmartRefreshLayout publishRefreshLayout;
    private RelativeLayout emptyLayout;
    private TextView emptyText;
    private TextView deleteButton;
    private PublishVideoContract.Presenter presenter;
    private UploadViewAdapter adapter;

    private int mCurrentRequestPageIndex = 1;
    //保存刷新之前的mCurrentRequestPageIndex，onRefresh会将mCurrentRequestPageIndex 置为1，request失败时，需要将mCurrentRequestPageIndex替换成lastCurrentPage
    private int mLastCurrentPage = 1;

    public PublishVideoView(Context context) {
        super(context);
    }

    public PublishVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PublishVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    private void init() {
        recyclerView = findViewById(R.id.publish_recycle);
        publishRefreshLayout = findViewById(R.id.publish_refresh_layout);
        deleteButton = findViewById(R.id.publish_delete_button);
        emptyLayout = findViewById(R.id.empty_layout);
        emptyText = findViewById(R.id.empty_text);
        deleteButton.setOnClickListener(this);

        presenter = new PublishVideoPresenter(getContext(), new PublishVideoModel(), this);
        adapter = new UploadViewAdapter(getContext(), "publish");
        recyclerView.setLayoutManager(adapter.getLayoutManager());
        recyclerView.setAdapter(adapter);

        publishRefreshLayout.setEnableAutoLoadMore(true);
        publishRefreshLayout.setRefreshHeader(new ClassicHeader(getContext()));
        publishRefreshLayout.setRefreshFooter(new ClassicFooter(getContext()));
        publishRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull final RefreshLayout refreshLayout) {
                mLastCurrentPage = mCurrentRequestPageIndex;
                mCurrentRequestPageIndex = 1;
                publishRefreshLayout.resetNoMoreData();
                presenter.requestPublishVideoData(true);
            }
        });
        publishRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull final RefreshLayout refreshLayout) {
                mLastCurrentPage = mCurrentRequestPageIndex;
                mCurrentRequestPageIndex++;
                presenter.requestPublishVideoData(false);
            }
        });

        //触发自动刷新
        publishRefreshLayout.autoRefresh();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == VISIBLE) {
            if (adapter != null) {
                RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(adapter.getLastSelectedLayoutPos());
                if (holder instanceof UploadViewAdapter.ItemCardViewHolder) {
                    ((UploadViewAdapter.ItemCardViewHolder) holder).startAnimation();
                }
            }
        } else {
            if (adapter != null) {
                RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(adapter.getLastSelectedLayoutPos());
                if (holder instanceof UploadViewAdapter.ItemCardViewHolder) {
                    ((UploadViewAdapter.ItemCardViewHolder) holder).stopAnimation();
                }
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        HSGlobalNotificationCenter.addObserver("publish_edit", this);
        HSGlobalNotificationCenter.addObserver("publish_cancel", this);
        HSGlobalNotificationCenter.addObserver(NotificationConstants.NOTIFICATION_UPDATE_THEME_IN_USER_PUBLISH, this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        HSGlobalNotificationCenter.removeObserver(this);
    }

    /**
     * 进入编辑模式
     */
    private void setEditMode() {
        deleteButton.setVisibility(VISIBLE);
        if (adapter != null) {
            adapter.setIsEdit(true);
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * 退出编辑模式
     */
    private void quitEditMode() {
        deleteButton.setVisibility(GONE);
        if (adapter != null) {
            adapter.setIsEdit(false);
            for (Theme item : adapter.data) {
                item.setDeleteSelected(false);
            }
            adapter.notifyDataSetChanged();
        }
        HSGlobalNotificationCenter.sendNotification("quit_edit_mode");
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        if ("publish_edit".equals(s)) {
            setEditMode();
        } else if ("publish_cancel".equals(s)) {
            quitEditMode();
        } else if (NotificationConstants.NOTIFICATION_UPDATE_THEME_IN_USER_PUBLISH.equals(s)) {
            refreshData();
        }
    }

    @Override
    public void showNoNetView(boolean isRefresh) {
        if (isRefresh) {
            publishRefreshLayout.finishRefresh();
        } else {
            publishRefreshLayout.finishLoadMore(true);
        }
        mCurrentRequestPageIndex = mLastCurrentPage;

        recyclerView.setVisibility(GONE);
        emptyLayout.setVisibility(VISIBLE);
        emptyText.setText(getResources().getString(R.string.not_network_text));
        HSGlobalNotificationCenter.sendNotification("no_publish_data");
    }

    @Override
    public void showNoContentView(boolean isRefresh) {
        if (isRefresh) {
            publishRefreshLayout.finishRefresh();
            recyclerView.setVisibility(GONE);
            emptyLayout.setVisibility(VISIBLE);
            emptyText.setText(getResources().getString(R.string.publish_page_empty_text));
            HSGlobalNotificationCenter.sendNotification("no_publish_data");
        } else {
            publishRefreshLayout.finishLoadMore(true);
            publishRefreshLayout.finishLoadMoreWithNoMoreData();
        }
    }

    @Override
    public void showContentView(boolean isRefresh) {
        if (isRefresh) {
            publishRefreshLayout.finishRefresh();
            recyclerView.setVisibility(VISIBLE);
            emptyLayout.setVisibility(GONE);
        } else {
            publishRefreshLayout.finishLoadMore(true);
        }
        ThemeList.clearPublishTheme();
        ThemeList.setPublishTheme(adapter.data);
        refreshData();
        HSGlobalNotificationCenter.sendNotification("have_publish_data");
    }

    private void refreshData() {
        adapter.updateData(ThemeList.getInstance().getUserPublishTheme());
        adapter.notifyDataSetChanged();
    }

    @Override
    public void updateEditStatusAfterDelete() {
        adapter.data.removeAll(adapter.mDeleteDataList);
        ThemeList.getPublishTheme().removeAll(adapter.mDeleteDataList);
        adapter.mDeleteDataList.clear();
        quitEditMode();
        if (adapter.data.size() == 0) {
            mCurrentRequestPageIndex = 1;
            presenter.requestPublishVideoData(true);
        }
    }

    @Override
    public void deleteFail() {
        adapter.mDeleteDataList.clear();
        quitEditMode();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.publish_delete_button) {
            if (adapter.mDeleteDataList != null && adapter.mDeleteDataList.size() > 0) {
                List<Long> deleteId = new ArrayList<>();
                for (Theme item : adapter.mDeleteDataList) {
                    deleteId.add((long) item.getId());
                }
                presenter.requestDeletePublishData(deleteId);
            }
        }
    }
}
