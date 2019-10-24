package com.honeycomb.colorphone.uploadview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.theme.ThemeList;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;

import java.util.ArrayList;
import java.util.List;

public class UploadVideoView extends RelativeLayout implements UploadVideoContract.View, INotificationObserver, View.OnClickListener {

    private UploadVideoContract.Presenter presenter;
    private RecyclerView recyclerView;
    private RelativeLayout emptyLayout;
    private TextView emptyText;
    private TextView deleteButton;
    private UploadViewAdapter adapter;

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
        deleteButton = findViewById(R.id.upload_delete_button);
        emptyLayout = findViewById(R.id.empty_layout);
        emptyText = findViewById(R.id.empty_text);
        deleteButton.setOnClickListener(this);

        presenter = new UploadVideoPresenter(getContext(), new UploadVideoModel(), this);
        presenter.requestUploadVideoData();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        HSGlobalNotificationCenter.addObserver("upload_edit", this);
        HSGlobalNotificationCenter.addObserver("upload_cancel", this);
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
        }
    }

    @Override
    public void showNoNetView() {
        recyclerView.setVisibility(GONE);
        emptyLayout.setVisibility(VISIBLE);
        emptyText.setText(getResources().getString(R.string.not_network_text));
        HSGlobalNotificationCenter.sendNotification("no_upload_data");
    }

    @Override
    public void showNoContentView() {
        recyclerView.setVisibility(GONE);
        emptyLayout.setVisibility(VISIBLE);
        emptyText.setText(getResources().getString(R.string.upload_page_empty_text));
        HSGlobalNotificationCenter.sendNotification("no_upload_data");
    }

    @Override
    public void showContentView(ArrayList<Theme> data) {
        recyclerView.setVisibility(VISIBLE);
        emptyLayout.setVisibility(GONE);
        adapter = new UploadViewAdapter(getContext(), "upload", data);
        ThemeList.setUploadTheme(data);
        recyclerView.setLayoutManager(adapter.getLayoutManager());
        recyclerView.setAdapter(adapter);
        HSGlobalNotificationCenter.sendNotification("have_upload_data");
    }

    @Override
    public void updateEditStatusAfterDelete() {
        adapter.data.removeAll(adapter.mDeleteDataList);
        ThemeList.getUploadTheme().removeAll(adapter.mDeleteDataList);
        adapter.mDeleteDataList.clear();
        quitEditMode();
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
            }
        }
    }

}
