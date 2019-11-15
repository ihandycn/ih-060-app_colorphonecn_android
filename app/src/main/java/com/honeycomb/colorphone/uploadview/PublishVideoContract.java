package com.honeycomb.colorphone.uploadview;

import java.util.List;

public class PublishVideoContract {
    interface View {
        /**
         * 展示无网的样式
         */
        void showNoNetView(boolean isRefresh);

        /**
         * 展示无内容的样式
         */
        void showNoContentView(boolean isRefresh);

        /**
         * 展示正常内容
         */
        void showContentView(boolean isRefresh);

        /**
         * 删除item后进行ui更新
         */
        void updateEditStatusAfterDelete();

        /**
         * 删除item后进行ui更新
         */
        void deleteFail();
    }

    interface Presenter {
        /**
         * 请求接口获取发布数据
         */
        void requestPublishVideoData(boolean isRefresh);

        /**
         * 请求接口删除发布数据
         */
        void requestDeletePublishData(List<Long> themeIdList);
    }
}
