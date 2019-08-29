package com.colorphone.ringtones.view;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.colorphone.ringtones.RingtoneApi;
import com.colorphone.ringtones.bean.ColumnBean;
import com.colorphone.ringtones.bean.ColumnResultBean;
import com.colorphone.ringtones.bean.RingtoneBean;
import com.colorphone.ringtones.bean.RingtoneListResultBean;
import com.colorphone.ringtones.module.Banner;
import com.colorphone.ringtones.module.Ringtone;
import com.superapps.util.Toasts;

import java.util.List;


/**
 * @author sundxing
 */
public class RingtoneListAdapter extends BaseRingtoneListAdapter {

    public RingtoneListAdapter(@NonNull RingtoneApi ringtoneApi, final String id, boolean hasHeader) {
        super(ringtoneApi);

        requestRingtoneList(id);

        this.hasHeader = hasHeader;
        if (hasHeader) {
            mRingtoneApi.requestBanners(new RingtoneApi.ResultCallback<ColumnResultBean>() {
                @Override
                public void onFinish(@Nullable ColumnResultBean bean) {
                    if (bean == null) {
                        Toasts.showToast("Error to get banner");
                        return;
                    }
                    List<ColumnBean> columnBeans = bean.getData().getCols();
                    if (columnBeans != null) {
                        for (ColumnBean b : columnBeans) {
                            mBannerList.add(Banner.valueOf(b));
                        }
                    }
                    RingtoneListAdapter.this.notifyDataSetChanged();
                }
            });
        }
    }

    public void requestRingtoneList(final String id) {
        mRingtoneApi.requestRingtoneListById(id, new RingtoneApi.ResultCallback<RingtoneListResultBean>() {
            @Override
            public void onFinish(RingtoneListResultBean bean) {
                if (bean == null) {
                    Toasts.showToast("Error to get id = " + id);
                    return;
                }

                List<RingtoneBean> beans = bean.getData();
                if (beans != null) {
                    mKeepOneHolder.reset();
                    mDataList.clear();

                    for (RingtoneBean rb : beans) {
                        mDataList.add(Ringtone.valueOf(rb));
                    }
                    RingtoneListAdapter.this.notifyDataSetChanged();
                }

            }
        });
    }

}