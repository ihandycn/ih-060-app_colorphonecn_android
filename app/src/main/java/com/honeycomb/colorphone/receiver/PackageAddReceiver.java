package com.honeycomb.colorphone.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ihs.commons.utils.HSLog;


/**
 * Created by jelly on 2017/12/18.
 */

public class PackageAddReceiver extends BroadcastReceiver {

    private static final int INSTALL_NOT_BY_PROMOTE = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())
                || Intent.ACTION_PACKAGE_INSTALL.equals(intent.getAction())) {
            final String pkgAdd = intent.getData().getSchemeSpecificPart();
            HSLog.d("PackageAddReceiver", "Pkg add :" + pkgAdd);
        }
    }
}
