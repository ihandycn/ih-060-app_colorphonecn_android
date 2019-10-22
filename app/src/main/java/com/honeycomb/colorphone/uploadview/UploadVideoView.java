package com.honeycomb.colorphone.uploadview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.Theme;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;

import java.util.ArrayList;

public class UploadVideoView extends RelativeLayout implements UploadVideoContract.View, INotificationObserver,View.OnClickListener {

    private UploadVideoContract.Presenter presenter;
    private RecyclerView recyclerView;
    private TextView deleteButton;
    private UploadViewAdapter adapter;
    private ArrayList<Theme> data = null;

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
        deleteButton.setOnClickListener(this);

        presenter = new UploadVideoPresenter(getContext(), new UploadVideoModel(), this);
        presenter.requestUploadVideoData("user id");
    }

    /**
     * 进入编辑模式
     */
    private void setEditMode() {

    }

    /**
     * 退出编辑模式
     */
    private void quitEditMode() {

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
                    presenter.requestDeleteUploadData(String.valueOf(item.getId()));
                }
                quitEditMode();
            }
        }
    }

}
