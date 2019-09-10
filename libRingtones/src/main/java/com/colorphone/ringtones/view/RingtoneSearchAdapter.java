package com.colorphone.ringtones.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.colorphone.ringtones.RingtoneApi;
import com.colorphone.ringtones.bean.RingtoneBean;
import com.colorphone.ringtones.bean.RingtoneListResultBean;
import com.colorphone.ringtones.module.Ringtone;

import java.util.ArrayList;
import java.util.List;


/**
 * @author sundxing
 */
public class RingtoneSearchAdapter extends BaseRingtoneListAdapter {

    private String mSearchText;

    public RingtoneSearchAdapter(@NonNull Context context, @NonNull RingtoneApi ringtoneApi) {
        super(context, ringtoneApi);
    }

    public void updateDate(List<Ringtone> results) {
        mKeepOneHolder.reset();
        if (results == null || results.isEmpty()) {
            // No result
            mDataList.clear();
        } else {
            mDataList.clear();
            mDataList.addAll(results);
        }
        notifyDataSetChanged();
    }


    @Override
    protected void loadMore(int pageIndex) {
        mRingtoneApi.search(mSearchText, pageIndex, new RingtoneApi.ResultCallback<RingtoneListResultBean>() {
            @Override
            public void onFinish(@Nullable RingtoneListResultBean bean) {
                setLoading(false);

                List<Ringtone> results = new ArrayList<>();
                if (bean != null) {
                    List<RingtoneBean> beans = bean.getData();
                    if (beans != null) {
                        for (RingtoneBean rb : beans) {
                            Ringtone ringtone = Ringtone.valueOf(rb);
                            ringtone.setColumnSource("Search");
                            results.add(ringtone);
                        }
                    }
                }
                mDataList.addAll(results);
            }
        });
    }

    @Override
    protected void refresh() {
        mRingtoneApi.search(mSearchText, 0, new RingtoneApi.ResultCallback<RingtoneListResultBean>() {
            @Override
            public void onFinish(@Nullable RingtoneListResultBean bean) {
                setLoading(false);
                mDataList.clear();
                List<Ringtone> results = new ArrayList<>();
                if (bean != null) {
                    List<RingtoneBean> beans = bean.getData();
                    if (beans != null) {
                        for (RingtoneBean rb : beans) {
                            Ringtone ringtone = Ringtone.valueOf(rb);
                            ringtone.setColumnSource("Search");
                            results.add(ringtone);
                        }
                    }
                }
                mDataList.addAll(results);
            }
        });
    }

    public String getSearchText() {
        return mSearchText;
    }

    public void setSearchText(String searchText) {
        mSearchText = searchText;
    }
}