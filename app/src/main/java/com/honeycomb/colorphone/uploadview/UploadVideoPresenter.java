package com.honeycomb.colorphone.uploadview;

import android.content.Context;

import com.honeycomb.colorphone.theme.ThemeUpdateListener;

import java.util.List;

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
    public void requestUploadVideoData(boolean isRefresh) {

        model.requestUploadVideoData(isRefresh, new ThemeUpdateListener() {
            @Override
            public void onFailure(String errorMsg) {
                view.showNoNetView(isRefresh);
            }

            @Override
            public void onSuccess(boolean isHasData) {
                if (isHasData) {
                    view.showContentView(isRefresh);
                } else {
                    view.showNoContentView(isRefresh);
                }
            }
        });
    }


    @Override
    public void requestDeleteUploadData(List<Long> themeIdList) {
        model.requestDeleteUploadData(themeIdList, new UploadVideoModel.DeleteDataCallBack() {
            @Override
            public void success() {
                view.updateEditStatusAfterDelete();
            }

            @Override
            public void fail() {
                view.deleteFail();
            }
        });
    }
}
