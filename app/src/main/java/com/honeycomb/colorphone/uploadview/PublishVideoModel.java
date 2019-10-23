package com.honeycomb.colorphone.uploadview;

import com.honeycomb.colorphone.http.HttpManager;
import com.honeycomb.colorphone.http.bean.AllUserThemeBean;
import com.honeycomb.colorphone.http.lib.call.Callback;

import java.util.List;

import okhttp3.ResponseBody;

public class PublishVideoModel {

    void requestDeletePublishData(List<Long> themeIdList, final DeleteDataCallBack listener) {
        HttpManager.getInstance().deleteUserVideos(themeIdList, new Callback<ResponseBody>() {
            @Override
            public void onFailure(String errorMsg) {
                listener.fail();
            }

            @Override
            public void onSuccess(ResponseBody responseBody) {
                listener.success();
            }
        });
    }


    void requestPublishVideoData(final LoadDataCallBack listener) {
        HttpManager.getInstance().getUserPublishedVideos(1, new Callback<AllUserThemeBean>() {
            @Override
            public void onFailure(String errorMsg) {
                listener.showFail();
            }

            @Override
            public void onSuccess(AllUserThemeBean allUserThemeBean) {
                listener.loadData(allUserThemeBean);
            }
        });
    }


    public interface LoadDataCallBack<T> {
        void loadData(T bean);

        void showFail();
    }

    public interface DeleteDataCallBack<T> {
        void success();

        void fail();
    }
}
