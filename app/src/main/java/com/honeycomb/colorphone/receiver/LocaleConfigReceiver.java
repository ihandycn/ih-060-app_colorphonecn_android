package com.honeycomb.colorphone.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.honeycomb.colorphone.Theme;

/**
 * Created by sundxing on 17/11/17.
 */

public class LocaleConfigReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Theme.updateThemes();
    }
}
