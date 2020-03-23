package com.colorphone.smartlocker.bean;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BaiduFeedItemsBean {

    private List<BaiduFeedBean> baiduFeedBeans = new ArrayList<>();

    public BaiduFeedItemsBean(JSONObject response) {
        try {
            JSONArray jsonArray = response.getJSONArray("items");

            for (int i = 0; i < jsonArray.length(); i++) {
                baiduFeedBeans.add(new BaiduFeedBean(jsonArray.getString(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<BaiduFeedBean> getBaiduFeedBeans() {
        return baiduFeedBeans;
    }
}
