package com.honeycomb.colorphone.uploadview;

import android.content.Context;

import com.honeycomb.colorphone.Theme;

import java.util.ArrayList;

public class PublishVideoPresenter implements PublishVideoContract.Presenter {

    private Context context;
    private PublishVideoModel model;
    private PublishVideoContract.View view;


    public PublishVideoPresenter(Context context, PublishVideoModel model, PublishVideoContract.View view) {
        this.context = context;
        this.model = model;
        this.view = view;
    }

    @Override
    public void requestPublishVideoData(String userId) {
        model.requestPublishVideoData(userId, new PublishVideoModel.LoadDataCallBack<ArrayList<Theme>>() {
            @Override
            public void loadData(ArrayList<Theme> bean) {
                //todo show publish normal view
                if (bean != null) {
                    view.showContentView(bean);
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
    public void requestDeletePublishData(String videoId) {
        model.requestDeletePublishData(videoId, new PublishVideoModel.LoadDataCallBack<ArrayList<Theme>>() {
            @Override
            public void loadData(ArrayList<Theme> bean) {
                //todo update view after delete item

            }

            @Override
            public void showFail() {

            }
        });
    }
}
