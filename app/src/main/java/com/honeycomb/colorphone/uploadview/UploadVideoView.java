package com.honeycomb.colorphone.uploadview;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.notification.NotificationConstants;
import com.honeycomb.colorphone.theme.ThemeList;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.MediaSharedElementCallback;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

import java.util.ArrayList;
import java.util.List;

public class UploadVideoView extends RelativeLayout implements UploadVideoContract.View, INotificationObserver, View.OnClickListener {

    private UploadVideoContract.Presenter presenter;
    private RecyclerView recyclerView;
    private SmartRefreshLayout uploadRefreshLayout;
    private RelativeLayout emptyLayout;
    private TextView emptyText;
    private TextView deleteButton;
    private UploadViewAdapter adapter;

    private MediaSharedElementCallback sharedElementCallback;

    public UploadVideoView(Context context) {
        super(context);
    }

    public UploadVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UploadVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    private void init() {
        recyclerView = findViewById(R.id.upload_recycle);
        uploadRefreshLayout = findViewById(R.id.upload_refresh_layout);
        deleteButton = findViewById(R.id.upload_delete_button);
        emptyLayout = findViewById(R.id.empty_layout);
        emptyText = findViewById(R.id.empty_text);
        deleteButton.setOnClickListener(this);
        adapter = new UploadViewAdapter(getContext(), "upload");
        recyclerView.setLayoutManager(adapter.getLayoutManager());
        recyclerView.setAdapter(adapter);

        deleteButton.setBackground(BackgroundDrawables.createBackgroundDrawable(0xfff64e4e, Dimensions.pxFromDp(21), true));

        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) emptyLayout.getLayoutParams();
        lp.topMargin = (int) ((Dimensions.getPhoneHeight(getContext()) - Dimensions.getStatusBarHeight(getContext())
                - Dimensions.getNavigationBarHeight(getContext())) * 0.23);
        emptyLayout.setLayoutParams(lp);

        presenter = new UploadVideoPresenter(getContext(), new UploadVideoModel(), this);

        uploadRefreshLayout.setEnableAutoLoadMore(true);
        uploadRefreshLayout.setRefreshHeader(new ClassicHeader(getContext()));
        uploadRefreshLayout.setRefreshFooter(new ClassicFooter(getContext()));
        uploadRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull final RefreshLayout refreshLayout) {
                uploadRefreshLayout.resetNoMoreData();
                presenter.requestUploadVideoData(true);
            }
        });
        uploadRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull final RefreshLayout refreshLayout) {
                presenter.requestUploadVideoData(false);
            }
        });

        //触发自动刷新
        uploadRefreshLayout.autoRefresh();
    }

    public void onResume() {
        if (adapter != null) {
            RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(adapter.getLastSelectedLayoutPos());
            if (holder instanceof UploadViewAdapter.ItemCardViewHolder) {
                ((UploadViewAdapter.ItemCardViewHolder) holder).startAnimation();
            }
        }
    }

    public void onPause() {
        if (adapter != null) {
            RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(adapter.getLastSelectedLayoutPos());
            if (holder instanceof UploadViewAdapter.ItemCardViewHolder) {
                ((UploadViewAdapter.ItemCardViewHolder) holder).stopAnimation();
            }
        }
    }

    public void onActivityReenter(int exitPos) {
        if (recyclerView != null) {
            ActivityCompat.postponeEnterTransition((Activity) getContext());
            recyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    recyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                    overrideSharedElement(exitPos);
                    ActivityCompat.startPostponedEnterTransition((Activity) getContext());
                    return true;
                }
            });
        }
    }

    private void overrideSharedElement(int exitPos) {
        if (recyclerView != null) {
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(exitPos);
            if (viewHolder != null && viewHolder.itemView != null) {
                ImageView imageView = viewHolder.itemView.findViewById(R.id.item_preview_img);
                sharedElementCallback.setSharedElementViews(imageView);
            }
        }
    }

    public void setSharedElementCallback(MediaSharedElementCallback sharedElementCallback) {
        this.sharedElementCallback = sharedElementCallback;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        HSGlobalNotificationCenter.addObserver("upload_edit", this);
        HSGlobalNotificationCenter.addObserver("upload_cancel", this);
        HSGlobalNotificationCenter.addObserver(NotificationConstants.NOTIFICATION_UPDATE_THEME_IN_USER_UPLOAD, this);
        HSGlobalNotificationCenter.addObserver(NotificationConstants.NOTIFICATION_PREVIEW_UPLOAD_POSITION, this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        recyclerView.setAdapter(null);
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
        if ("upload_edit".equals(s)) {
            setEditMode();
        } else if ("upload_cancel".equals(s)) {
            quitEditMode();
        } else if (NotificationConstants.NOTIFICATION_UPDATE_THEME_IN_USER_UPLOAD.equals(s)) {
            refreshData();
        } else if (NotificationConstants.NOTIFICATION_PREVIEW_UPLOAD_POSITION.equals(s)) {
            if (adapter != null) {
                int pos = hsBundle.getInt("position");
                HSLog.d("preview pos = " + pos);
                recyclerView.scrollToPosition(pos);
            }
        }
    }

    @Override
    public void showNoNetView(boolean isRefresh) {
        if (isRefresh) {
            uploadRefreshLayout.finishRefresh();
        } else {
            uploadRefreshLayout.finishLoadMore(true);
        }

        recyclerView.setVisibility(GONE);
        emptyLayout.setVisibility(VISIBLE);
        emptyText.setText(getResources().getString(R.string.not_network_text));
        HSGlobalNotificationCenter.sendNotification("no_upload_data");
    }

    @Override
    public void showNoContentView(boolean isRefresh) {
        if (isRefresh) {
            uploadRefreshLayout.finishRefresh();
            recyclerView.setVisibility(GONE);
            emptyLayout.setVisibility(VISIBLE);
            emptyText.setText(getResources().getString(R.string.upload_page_empty_text));
            HSGlobalNotificationCenter.sendNotification("no_upload_data");
        } else {
            uploadRefreshLayout.finishLoadMore(true);
            uploadRefreshLayout.finishLoadMoreWithNoMoreData();
        }
    }

    @Override
    public void showContentView(boolean isRefresh) {
        if (isRefresh) {
            uploadRefreshLayout.finishRefresh();
            recyclerView.setVisibility(VISIBLE);
            emptyLayout.setVisibility(GONE);

        } else {
            uploadRefreshLayout.finishLoadMore(true);
        }
        refreshData();
        HSGlobalNotificationCenter.sendNotification("have_upload_data");
    }

    private void refreshData() {
        adapter.updateData(ThemeList.getInstance().getUserUploadTheme());
        adapter.notifyDataSetChanged();
    }

    @Override
    public void updateEditStatusAfterDelete() {
        adapter.data.removeAll(adapter.mDeleteDataList);
        ThemeList.getInstance().getUserUploadTheme().removeAll(adapter.mDeleteDataList);
        adapter.mDeleteDataList.clear();
        quitEditMode();
        if (adapter.data.size() == 0) {
            presenter.requestUploadVideoData(true);
        }
    }

    @Override
    public void deleteFail() {
        adapter.mDeleteDataList.clear();
        quitEditMode();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.upload_delete_button) {
            if (adapter.mDeleteDataList != null && adapter.mDeleteDataList.size() > 0) {
                List<Long> deleteId = new ArrayList<>();
                for (Theme item : adapter.mDeleteDataList) {
                    deleteId.add((long) item.getId());
                }
                presenter.requestDeleteUploadData(deleteId);
                Analytics.logEvent("MyUploads_CallFlash_Delete");
            }
        }
    }

}
