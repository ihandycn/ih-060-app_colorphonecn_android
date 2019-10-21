package com.acb.libwallpaper.live.base;

public class BaseItemData<T> {
    private T mData;
    private int mType;

    public BaseItemData(T data, int type) {
        mData = data;
        mType = type;
    }

    public T getData() {
        return mData;
    }

    public int getType() {
        return mType;
    }
}
