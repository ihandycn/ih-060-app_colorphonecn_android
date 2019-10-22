package com.honeycomb.colorphone.uploadview;

import android.content.Context;

import com.honeycomb.colorphone.Theme;

import java.util.ArrayList;

public class UploadVideoPresenter implements UploadVideoContract.Presenter {

    private Context context;
    private UploadVideoModel model;
    private UploadVideoContract.View view;

    public UploadVideoPresenter(Context context, UploadVideoModel model, UploadVideoContract.View view) {
        this.context = context;
        this.model = model;
        this.view = view;
    }

    @Override
    public void requestUploadVideoData(String userId) {
        model.requestUploadVideoData(userId, new UploadVideoModel.LoadDataCallBack<ArrayList<Theme>>() {
            @Override
            public void loadData(ArrayList<Theme> data) {
                //todo show upload normal view
                if (data != null) {
                    view.showContentView(data);
                } else {
                    view.showNoContentView();
                }
            }

            @Override
            public void showFail() {
                view.showNoNetView();
            }
        });
    }


    @Override
    public void requestDeleteUploadData(String videoId) {
        model.requestDeleteUploadData(videoId, new UploadVideoModel.LoadDataCallBack<ArrayList<Theme>>() {
            @Override
            public void loadData(ArrayList<Theme> data) {
                //todo update view after delete item

            }

            @Override
            public void showFail() {

            }
        });
    }
}
