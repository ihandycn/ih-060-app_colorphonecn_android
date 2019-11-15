package com.honeycomb.colorphone.theme;

import com.acb.call.themes.Type;

import java.util.ArrayList;

public class ThemeData {

    private int pageIndex;

    public ThemeData() {
        clear();
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public int getThemeSize() {
        return Type.typeSize();
    }

    public void setThemeList(ArrayList<Type> themeList) {
        Type.clearTypes();
        Type.addTypes(themeList);
    }

    public void appendTheme(ArrayList<Type> themeList) {
        Type.addTypes(themeList);
    }

    public void clear() {
        pageIndex = 1;
        Type.clearTypes();
    }

}
