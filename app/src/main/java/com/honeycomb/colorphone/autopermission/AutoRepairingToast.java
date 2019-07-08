package com.honeycomb.colorphone.autopermission;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.honeycomb.colorphone.R;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

public class AutoRepairingToast {

    private static Toast repairingToast;

    public static void showRepairingToast() {
        HSLog.e("showRepairingToast" );
        if (repairingToast == null) {
            repairingToast = new Toast(HSApplication.getContext().getApplicationContext());

            final View contentView = LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.toast_huawei_auto_repair, null);
            contentView.setAlpha(0.9f);
            contentView.setBackground(BackgroundDrawables.createBackgroundDrawable(Color.parseColor("#000000"),
                    Dimensions.pxFromDp(3), false));

            repairingToast.setView(contentView);
            repairingToast.setMargin(0, 0.36f);
            repairingToast.setDuration(Toast.LENGTH_LONG);
            repairingToast.show();
        }
    }

    public static void cancelRepairingToast() {
        HSLog.e("cancelRepairingToast");
        if (repairingToast != null) {
            repairingToast.cancel();
            repairingToast = null;
        }
    }
}
