package com.honeycomb.colorphone.uploadview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.Theme;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;

import java.util.ArrayList;
import java.util.List;

public class PublishVideoView extends RelativeLayout implements PublishVideoContract.View, INotificationObserver, View.OnClickListener {

    private RecyclerView recyclerView;
    private TextView deleteButton;
    private ArrayList<Theme> data = null;
    private PublishVideoContract.Presenter presenter;

    private UploadViewAdapter adapter;

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
        deleteButton = findViewById(R.id.publish_delete_button);
        deleteButton.setOnClickListener(this);

        presenter = new PublishVideoPresenter(getContext(), new PublishVideoModel(), this);
        presenter.requestPublishVideoData("user id");
    }

    /**
     * 进入编辑模式
     */
    private void setEditMode() {
        deleteButton.setVisibility(VISIBLE);
    }

    /**
     * 退出编辑模式
     */
    private void quitEditMode() {
        deleteButton.setVisibility(GONE);
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        if ("publish_edit".equals(s)) {
            setEditMode();
        } else if ("publish_cancel".equals(s)) {
            quitEditMode();
        }
    }

    @Override
    public void showNoNetView() {

    }

    @Override
    public void showNoContentView() {

    }

    @Override
    public void showContentView(ArrayList<Theme> data) {
        this.data = data;
        adapter = new UploadViewAdapter(getContext(), data);
        recyclerView.setLayoutManager(adapter.getLayoutManager());
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.publish_delete_button) {
            if (adapter.mDeleteDataList != null && adapter.mDeleteDataList.size() > 0) {
                for (Theme item : adapter.mDeleteDataList) {
                    adapter.data.remove(item);
                    presenter.requestDeletePublishData(String.valueOf(item.getId()));
                }
            }
            quitEditMode();
        }
    }
}
