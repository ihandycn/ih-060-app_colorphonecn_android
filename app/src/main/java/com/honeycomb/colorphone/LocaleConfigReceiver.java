package com.honeycomb.colorphone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.acb.call.themes.Type;

/**
 * Created by sundxing on 17/11/17.
 */

public class LocaleConfigReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Type.updateTypes();
    }
}
