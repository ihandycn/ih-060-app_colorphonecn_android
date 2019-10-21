package com.acb.libwallpaper.live.customize;


public class CategoryItem {
    private String mName;
    private boolean mIsSelected;

    public CategoryItem(String name, boolean isSelected) {
        super();
        mName = name;
        mIsSelected = isSelected;
    }

    public String getItemName() {
        return mName;
    }

    public boolean isSelected() {
        return mIsSelected;
    }

    public void setSelected(boolean selected) {
        mIsSelected = selected;
    }
}
