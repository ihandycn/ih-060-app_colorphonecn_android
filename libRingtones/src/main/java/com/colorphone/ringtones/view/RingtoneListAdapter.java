package com.colorphone.ringtones.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.colorphone.ringtones.RingtoneApi;
import com.colorphone.ringtones.bean.ColumnBean;
import com.colorphone.ringtones.bean.ColumnResultBean;
import com.colorphone.ringtones.bean.RingtoneBean;
import com.colorphone.ringtones.bean.RingtoneListResultBean;
import com.colorphone.ringtones.module.Banner;
import com.colorphone.ringtones.module.Column;
import com.colorphone.ringtones.module.Ringtone;
import com.ihs.commons.utils.HSLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author sundxing
 */
public class RingtoneListAdapter extends BaseRingtoneListAdapter {

    private Map<String,List<Ringtone>> mDataCacheMap = new HashMap<>();
    private String mRingtoneListId;
    private Column mColumn;

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
                        HSLog.e("Error to get banner");
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
        boolean isChanged = !TextUtils.equals(ringtoneListId, mRingtoneListId);
        if (isChanged) {
            mRingtoneListId = ringtoneListId;
            requestRingtoneList(0, true);
        }
    }

    public void requestRingtoneList(final int pageIndex) {
        requestRingtoneList(pageIndex, false);
    }

    public void requestRingtoneList(final int pageIndex, boolean forceRefresh) {

        List<Ringtone> ringtoneList = mDataCacheMap.get(mRingtoneListId);
        if (ringtoneList == null) {
            ringtoneList = new ArrayList<>();
            mDataCacheMap.put(mRingtoneListId, ringtoneList);
        }

        if (!forceRefresh) {
            // Try hit cache
            if (!ringtoneList.isEmpty() && pageIndex == 0) {
                mDataList.clear();
                mDataList.addAll(ringtoneList);
                return;
            }
        }

        final List<Ringtone> rintoneListServer = ringtoneList;

        mRingtoneApi.requestRingtoneListById(mRingtoneListId, pageIndex, new RingtoneApi.ResultCallback<RingtoneListResultBean>() {
            @Override
            public void onFinish(RingtoneListResultBean bean) {
                setLoading(false);
                if (bean == null) {
                    HSLog.e("Error to get id = " + mRingtoneListId);
                    return;
                }

                List<RingtoneBean> beans = bean.getData();
                if (beans != null) {
                    setSizeTotalCount(bean.getTotal());
                    mKeepOneHolder.reset();
                    // First page, or refresh
                    if (pageIndex == 0) {
                        rintoneListServer.clear();
                    }

                    for (RingtoneBean rb : beans) {
                        Ringtone ringtone = Ringtone.valueOf(rb);
                        ringtone.setColumnSource(mColumn != null ? mColumn.getName() : "Banner");
                        rintoneListServer.add(ringtone);
                    }

                    // bind cache list
                    mDataList.clear();
                    mDataList.addAll(rintoneListServer);

                    RingtoneListAdapter.this.notifyDataSetChanged();
                }
            }
        });
    }

    public void setColumn(Column column) {
        mColumn = column;
    }

    @Override
    protected void loadMore(int pageIndex) {
        requestRingtoneList(pageIndex);
    }

    @Override
    protected void refresh() {
        requestRingtoneList(0);
    }
}