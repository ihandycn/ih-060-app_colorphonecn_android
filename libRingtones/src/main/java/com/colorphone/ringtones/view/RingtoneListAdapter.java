package com.colorphone.ringtones.view;

import android.content.Context;
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

    private String mRingtoneListId;
    public RingtoneListAdapter(@NonNull Context context,  @NonNull RingtoneApi ringtoneApi, final String id, boolean hasHeader) {
        super(context, ringtoneApi);

        mRingtoneListId = id;

        requestRingtoneList(0);

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

    public void requestRingtoneList(String ringtoneListId) {
        mRingtoneListId = ringtoneListId;
        requestRingtoneList(0);
    }

    public void requestRingtoneList(final int pageIndex) {
        mRingtoneApi.requestRingtoneListById(mRingtoneListId, pageIndex, new RingtoneApi.ResultCallback<RingtoneListResultBean>() {
            @Override
            public void onFinish(RingtoneListResultBean bean) {
                setLoading(false);
                if (bean == null) {
                    Toasts.showToast("Error to get id = " + mRingtoneListId);
                    return;
                }

                List<RingtoneBean> beans = bean.getData();
                if (beans != null) {
                    setSizeTotalCount(bean.getTotal());
                    mKeepOneHolder.reset();
                    // First page, or refresh
                    if (pageIndex == 0) {
                        mDataList.clear();
                    }

                    for (RingtoneBean rb : beans) {
                        mDataList.add(Ringtone.valueOf(rb));
                    }
                    RingtoneListAdapter.this.notifyDataSetChanged();
                }

            }
        });
    }

    @Override
    protected void loadMore(int pageIndex) {
        requestRingtoneList(pageIndex);
    }
}