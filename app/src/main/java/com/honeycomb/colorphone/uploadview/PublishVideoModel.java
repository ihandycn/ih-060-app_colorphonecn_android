package com.honeycomb.colorphone.uploadview;

public class PublishVideoModel {

    void requestDeletePublishData(String video, final LoadDataCallBack listener) {

    }



    void requestPublishVideoData(String uid, final LoadDataCallBack listener) {

    }



    public interface LoadDataCallBack<T> {
        void loadData(T bean);

        void showFail();
    }
}
