package com.honeycomb.colorphone.ringtones.bean;

import java.util.List;

public class ColumListBean extends BaseRingtoneBean {
    private List<ColumBean> data;
    public List<ColumBean> getData() {
        return data;
    }

    public void setData(List<ColumBean> data) {
        this.data = data;
    }
}
