package com.honeycomb.colorphone.uploadview;

import com.honeycomb.colorphone.Theme;

import java.util.ArrayList;

public class UploadVideoModel {

    void requestUploadVideoData(String uid, final LoadDataCallBack listener) {

    }

    void requestDeleteUploadData(String video, final LoadDataCallBack listener) {

    }

    public interface LoadDataCallBack<T> {
        void loadData(T data);

        void showFail();
    }
}
