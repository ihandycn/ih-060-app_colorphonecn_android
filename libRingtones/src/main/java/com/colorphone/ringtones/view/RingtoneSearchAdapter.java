package com.colorphone.ringtones.view;

import android.content.Context;
import android.support.annotation.NonNull;

import com.colorphone.ringtones.RingtoneApi;
import com.colorphone.ringtones.module.Ringtone;

import java.util.List;


public class RingtoneSearchAdapter extends BaseRingtoneListAdapter {

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
}