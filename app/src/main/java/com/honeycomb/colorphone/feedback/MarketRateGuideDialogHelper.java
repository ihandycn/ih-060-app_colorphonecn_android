package com.honeycomb.colorphone.feedback;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.honeycomb.colorphone.autopermission.AutoPermissionChecker;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.permission.acc.AccCommentReceiver;
import com.superapps.util.Compats;
import com.superapps.util.Threads;

import org.jetbrains.annotations.NotNull;

public class MarketRateGuideDialogHelper {
    public static final String TAG = MarketRateGuideDialogHelper.class.getSimpleName();
    public MarketCommentReceiver receiver;
    private boolean broadcastReceived = false;
    private final Context context;

    public MarketRateGuideDialogHelper(Context context) {
        this.context = context;
    }

    public void tryToShowRateGuide() {
        try {
            String appPkg = HSApplication.getContext().getPackageName();
            String marketPkg = getMarketPkg();
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

            if (accPermissionAccessible()) {
                showGuideWithAcc(context);
            } else {
                Threads.postOnMainThreadDelayed(() -> showGuideWithoutAcc(context), 1600);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unregisterReceiver(Context context) {
        if (receiver != null) {
            context.unregisterReceiver(receiver);
            receiver = null;
        }
    }

    private void registerReceiver(Context context) {
        if (receiver == null) {
            receiver = new MarketCommentReceiver();
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AccCommentReceiver.ACTION);
        context.registerReceiver(receiver, intentFilter);
    }

    private boolean accPermissionAccessible() {
        return AutoPermissionChecker.isAccessibilityGranted() && Utils.isNewUser();
    }

    private void showGuideWithAcc(Context context) {
        registerReceiver(context);
        Threads.postOnMainThreadDelayed(() -> {
            if (broadcastReceived) {
                broadcastReceived = false;
                return;
            }
            unregisterReceiver(context);
            showGuideWithoutAcc(context);
        }, 3000);
    }

    private void showGuideWithoutAcc(Context context) {
        if (Compats.IS_HUAWEI_DEVICE) {
            showHuaweiGuideDialog(context);
        } else if (Compats.IS_XIAOMI_DEVICE) {
            showXiaomiGuideDialog(context);
        } else if (Compats.IS_OPPO_DEVICE) {
            showOppoGuideDialog(context);
        }
    }

    private void showOppoGuideDialog(Context context) {
        OppoRateGuideDialog.show(context);
    }

    private void showXiaomiGuideDialog(Context context) {
        XiaomiRateGuideDialog.show(context);
    }

    private void showHuaweiGuideDialog(Context context) {
        HuaweiRateGuideDialog.show(context);
    }

    @NotNull
    private String getMarketPkg() {
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

    private class MarketCommentReceiver extends AccCommentReceiver {

        @Override
        public void onCommentFirstStep(@NonNull Rect rect) {
            HSLog.d(TAG, "processName = " + HSApplication.getProcessName() + ", onCommentFirstStep: " + rect.toString());
            broadcastReceived = true;
            RateGuideDialogWithAcc1.show(HSApplication.getContext(), rect);
        }

        @Override
        public void onCommentXiaoMiSecondStep(Rect rect) {
            HSLog.d(TAG, "processName = " + HSApplication.getProcessName() + ", onCommentXiaoMiSecondStep: click, rect = " + rect.toString());
            broadcastReceived = true;
            RateGuideDialogWithAccXiaomi2.show(HSApplication.getContext(), rect);
        }

        @Override
        public void onCommentOppoSecondStep(@NonNull Rect rect) {
            HSLog.d(TAG, "processName = " + HSApplication.getProcessName() + ", onCommentOppoSecondStep: " + rect.toString());
            RateGuideDialogWithAccOppo2.show(HSApplication.getContext(), rect);
        }
    }
}
