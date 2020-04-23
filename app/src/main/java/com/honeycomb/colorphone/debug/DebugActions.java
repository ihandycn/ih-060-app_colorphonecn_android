package com.honeycomb.colorphone.debug;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.text.TextUtils;

import com.honeycomb.colorphone.feedback.HuaweiRateGuideDialog;
import com.honeycomb.colorphone.feedback.OppoRateGuideDialog;
import com.honeycomb.colorphone.feedback.XiaomiRateGuideDialog;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.permission.Utils;
import com.ihs.permission.acc.AccCommentReceiver;
import com.superapps.util.Compats;
import com.superapps.util.Threads;

public class DebugActions {

    private static final String TAG = "DebugActions";

    private static CommentReceiver commentReceiver;

    public static void onVolumeDown(Activity activity) {
        boolean isGranted = Utils.isAccessibilityGranted();
        HSLog.e("rango", " isGranted = " + isGranted);

        activity.unregisterReceiver(commentReceiver);
    }

    public static void onVolumeUp(Activity activity) {

        commentReceiver = new CommentReceiver();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AccCommentReceiver.ACTION);
        activity.registerReceiver(commentReceiver, intentFilter);

        launchAppDetail(HSApplication.getContext().getPackageName(), getMarketPkg());
    }

    private static void launchAppDetail(String appPkg, String marketPkg) {
        try {
            if (TextUtils.isEmpty(appPkg) || TextUtils.isEmpty(marketPkg)) {
                return;
            }

            Uri uri = Uri.parse("market://details?id=" + appPkg);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            if (!TextUtils.isEmpty(marketPkg)) {
                intent.setPackage(marketPkg);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            HSApplication.getContext().startActivity(intent);

            Threads.postOnMainThreadDelayed(() -> {
                if (Compats.IS_HUAWEI_DEVICE) {
                    HuaweiRateGuideDialog.show(HSApplication.getContext());
                } else if (Compats.IS_XIAOMI_DEVICE) {
                    XiaomiRateGuideDialog.show(HSApplication.getContext());
                } else if (Compats.IS_OPPO_DEVICE) {
                    OppoRateGuideDialog.show(HSApplication.getContext());
                }
            }, 2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getMarketPkg() {
        if (Compats.IS_HUAWEI_DEVICE) {
            return "com.huawei.appmarket";
        } else if (Compats.IS_XIAOMI_DEVICE) {
            return "com.xiaomi.market";
        } else if (Compats.IS_OPPO_DEVICE) {
            return "com.oppo.market";
        } else {
            return "";
        }
    }
}
