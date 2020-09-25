package com.honeycomb.colorphone.debug;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.acb.call.themes.GifAnimationView;
import com.acb.call.themes.ThemeBaseView;
import com.acb.call.utils.FileUtils;
import com.acb.call.views.InCallActionView;
import com.acb.call.views.VideoPlayerView;
import com.acb.call.wechat.WeChatInCallManager;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.theme.ThemeApplyManager;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.Compats;
import com.superapps.util.Preferences;

import java.io.File;
import java.io.IOException;

public class DebugActions {

    private static final String TAG = "DebugActions";

    private static CommentReceiver commentReceiver;

    public static void onVolumeDown(Activity activity) {
//        boolean isGranted = Utils.isAccessibilityGranted();
//        HSLog.e("rango", " isGranted = " + isGranted);
//
//        activity.unregisterReceiver(commentReceiver);

//        WindowManager windowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
//        windowManager.removeView(view);

        WeChatInCallManager.getInstance().checkAndShow("语⾳通话中","textTitle",ThemeApplyManager.getInstance().getWeChatInCallThemeName());
    }

    public static void onVolumeUp(Activity activity) {

//        commentReceiver = new CommentReceiver();
//
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(AccCommentReceiver.ACTION);
//        activity.registerReceiver(commentReceiver, intentFilter);
//
//        launchAppDetail(HSApplication.getContext().getPackageName(), getMarketPkg());

        //openWeChatInCallActivity(activity);

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

//            Threads.postOnMainThreadDelayed(() -> {
//                if (Compats.IS_HUAWEI_DEVICE) {
//                    HuaweiRateGuideDialog.show(HSApplication.getContext());
//                } else if (Compats.IS_XIAOMI_DEVICE) {
//                    XiaomiRateGuideDialog.show(HSApplication.getContext());
//                } else if (Compats.IS_OPPO_DEVICE) {
//                    OppoRateGuideDialog.show(HSApplication.getContext());
//                }
//            }, 2000);
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
