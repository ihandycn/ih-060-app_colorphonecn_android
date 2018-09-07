package com.honeycomb.colorphone.battery;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import java.util.List;

public class BatteryAppsRecyclerView extends RecyclerView {

    private BatteryAppsAdapter mBatteryAppsAdapter;

    public BatteryAppsRecyclerView(Context context) {
        this(context, null);
    }

    public BatteryAppsRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BatteryAppsRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mBatteryAppsAdapter = new BatteryAppsAdapter(getContext());
        setAdapter(mBatteryAppsAdapter);
        setLayoutManager(mBatteryAppsAdapter.getLayoutManager());
    }

    public void refresh(@NonNull List<BatteryAppInfo> list) {
        if (null != mBatteryAppsAdapter) {
            mBatteryAppsAdapter.refreshData(list);
        }
    }

    public void refreshForRemove(String packageName) {
        if (null != mBatteryAppsAdapter) {
            mBatteryAppsAdapter.refreshForRemove(packageName);
        }
    }
}
