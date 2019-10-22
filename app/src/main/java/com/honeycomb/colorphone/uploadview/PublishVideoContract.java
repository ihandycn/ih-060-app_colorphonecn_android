package com.honeycomb.colorphone.uploadview;

import com.honeycomb.colorphone.Theme;

import java.util.ArrayList;
import java.util.List;

public class PublishVideoContract {
    interface View {
        /**
         * 展示无网的样式
         */
        void showNoNetView();

        /**
         * 展示无内容的样式
         */
        void showNoContentView();

        /**
         * 展示正常内容
         */
        void showContentView(ArrayList<Theme> data);
    }

    interface Presenter {
        /**
         * 请求接口获取发布数据
         */
        void requestPublishVideoData(String userId);

        /**
         * 请求接口删除发布数据
         */
        void requestDeletePublishData(String videoId);
    }
}
