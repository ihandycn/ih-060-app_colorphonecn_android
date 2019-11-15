package com.honeycomb.colorphone.wallpaper.base;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;


import java.lang.ref.WeakReference;

public abstract class BaseItemView extends RelativeLayout implements ViewWrapper.Binder<BaseItemData> {

    protected BaseItemData mData;
    protected WeakReference<Context> mContextWeakReference;

    public BaseItemView(Context context) {
        this(context, null);
    }

    public BaseItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContextWeakReference = new WeakReference<>(context);
    }

    @Override
    public void bind(BaseItemData data) {
        if (data != null && data.getData() != null) {
            mData = data;
            refreshUi();
        }
    }

    protected abstract void refreshUi();
}
