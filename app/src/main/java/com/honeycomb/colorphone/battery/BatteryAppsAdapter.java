package com.honeycomb.colorphone.battery;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.util.Utils;
import com.superapps.util.Navigations;

import java.util.ArrayList;
import java.util.List;

class BatteryAppsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<BatteryAppInfo> mBatteryAppInfoList;
    private LinearLayoutManager mLayoutManager;

    BatteryAppsAdapter(Context context) {
        mContext = context;
        mBatteryAppInfoList = new ArrayList<>();
        mLayoutManager = new LinearLayoutManager(context);
    }

    LinearLayoutManager getLayoutManager() {
        return mLayoutManager;
    }

    List<BatteryAppInfo> getItems() {
        return mBatteryAppInfoList;
    }

    @Override
    public int getItemCount() {
        return mBatteryAppInfoList.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AppViewHolder(LayoutInflater.from(mContext).inflate(R.layout.battery_apps_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        bindApps(holder, position);
    }

    private void bindApps(RecyclerView.ViewHolder holder, int position) {
        final BatteryAppInfo batteryAppInfo = mBatteryAppInfoList.get(position);
        ((AppViewHolder) holder).appIconIv.setImageDrawable(batteryAppInfo.getAppDrawable());
        ((AppViewHolder) holder).appNameTv.setText(batteryAppInfo.getAppName());
        ((AppViewHolder) holder).batteryPercentTv.setText(String.valueOf(Utils.formatNumberTwoDigit(batteryAppInfo.getPercent()) + "%"));
        ((AppViewHolder) holder).operationTv.setText(batteryAppInfo.getIsSystemApp() ? mContext.getString(R.string.battery_view) : mContext.getString(R.string.battery_stop));
        ((AppViewHolder) holder).progressBar.setProgress((int)batteryAppInfo.getPercent());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startToSystemAppInfoActivity(batteryAppInfo);
            }
        });

        ((AppViewHolder) holder).operationTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startToSystemAppInfoActivity(batteryAppInfo);
            }
        });
    }

    private class AppViewHolder extends RecyclerView.ViewHolder {
        ImageView appIconIv;
        TextView appNameTv;
        TextView batteryPercentTv;
        TextView operationTv;
        ProgressBar progressBar;

        AppViewHolder(View view) {
            super(view);
            appIconIv = (ImageView) view.findViewById(R.id.app_icon_iv);
            appNameTv = (TextView) view.findViewById(R.id.app_name_tv);
            batteryPercentTv = (TextView) view.findViewById(R.id.battery_percent_tv);
            operationTv = (TextView) view.findViewById(R.id.operation_tv);
            progressBar = (ProgressBar) view.findViewById(R.id.battery_progress_bar);
        }
    }

    void refreshData(@NonNull List<BatteryAppInfo> appInfoList) {
        mBatteryAppInfoList.clear();
        mBatteryAppInfoList.addAll(appInfoList);
        notifyDataSetChanged();
    }

    void refreshForRemove(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return;
        }
        for (BatteryAppInfo batteryAppInfo : mBatteryAppInfoList) {
            if (null != batteryAppInfo && packageName.equals(batteryAppInfo.getPackageName())) {
                mBatteryAppInfoList.remove(batteryAppInfo);
                break;
            }
        }
        notifyDataSetChanged();
    }

    private void startToSystemAppInfoActivity(BatteryAppInfo batteryAppInfo) {
        if (null == batteryAppInfo) {
            return;
        }
        boolean isSystemApp = batteryAppInfo.getIsSystemApp();
        Navigations.startSystemAppInfoActivity(mContext, batteryAppInfo.getPackageName());
    }
}