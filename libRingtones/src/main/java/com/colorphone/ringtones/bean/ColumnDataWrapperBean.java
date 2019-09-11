package com.colorphone.ringtones.bean;

import java.util.List;


public class ColumnDataWrapperBean extends ColumnBean {

    private List<ColumnBean> cols;

    public List<ColumnBean> getCols() {
        return cols;
    }

    public void setCols(List<ColumnBean> cols) {
        this.cols = cols;
    }
}
