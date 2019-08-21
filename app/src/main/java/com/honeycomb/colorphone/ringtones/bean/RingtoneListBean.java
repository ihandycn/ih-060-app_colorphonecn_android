package com.honeycomb.colorphone.ringtones.bean;

import java.util.List;

public class RingtoneListBean extends BaseRingtoneBean {
    private List<RingtoneBean> data;
    public List<RingtoneBean> getData() {
        return data;
    }

    public void setData(List<RingtoneBean> data) {
        this.data = data;
    }
}
