package com.honeycomb.colorphone.uploadview;

import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.http.HttpManager;
import com.honeycomb.colorphone.http.bean.AllUserThemeBean;
import com.honeycomb.colorphone.http.lib.call.Callback;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;

public class UploadVideoModel {

    void requestUploadVideoData(int index, final LoadDataCallBack listener) {
        HttpManager.getInstance().getUserUploadedVideos(index, new Callback<AllUserThemeBean>() {
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

    void requestDeleteUploadData(List<Long> themeIdList, final DeleteDataCallBack listener) {
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

    public interface LoadDataCallBack<T> {
        void loadData(T data);

        void showFail();
    }

    public interface DeleteDataCallBack<T> {
        void success();

        void fail();
    }
}
