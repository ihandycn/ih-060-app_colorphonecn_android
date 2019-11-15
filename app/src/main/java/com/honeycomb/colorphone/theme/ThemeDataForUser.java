package com.honeycomb.colorphone.theme;

import com.honeycomb.colorphone.Theme;

import java.util.ArrayList;
import java.util.List;

public class ThemeDataForUser {

    private int pageIndex;
    private ArrayList<Theme> dataList;

    public ThemeDataForUser() {
        this.pageIndex = 1;
        dataList = new ArrayList<>();
    }

    public void updateData(List<Theme> data) {
        dataList.clear();
        dataList.addAll(data);
    }

    public void appendData(List<Theme> data) {
        dataList.addAll(data);
    }

    public ArrayList<Theme> getDataList() {
        return dataList;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public void clear() {
        pageIndex = 1;
        dataList.clear();
    }

}
