package com.colorphone.ringtones.bean;

import java.util.List;

/**
 * @author sundxing
 */
public class RingtoneListResultBean extends BaseResultBean {
    private List<RingtoneBean> data;
    public List<RingtoneBean> getData() {
        return data;
    }

    public void setData(List<RingtoneBean> data) {
        this.data = data;
    }
}
