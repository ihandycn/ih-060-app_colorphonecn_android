package com.honeycomb.colorphone.uploadview;

import java.util.List;

public class UploadVideoContract {
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
         * 请求接口获取上传数据
         */
        void requestUploadVideoData(boolean isRefresh);

        /**
         * 请求接口删除上传数据
         */
        void requestDeleteUploadData(List<Long> themeIdList);
    }
}
