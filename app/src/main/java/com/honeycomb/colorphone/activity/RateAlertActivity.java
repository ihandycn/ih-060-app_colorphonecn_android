package com.honeycomb.colorphone.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.honeycomb.colorphone.dialog.FiveStarRateTip;
import com.honeycomb.colorphone.feedback.MarketRateGuideDialogHelper;
import com.ihs.app.alerts.HSAlertMgr;
import com.ihs.app.framework.activity.HSActivity;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.superapps.util.Navigations;

/**
 * Created by zhewang on 30/01/2018.
 */

public class RateAlertActivity extends HSActivity implements INotificationObserver {
    private static final String RATE_FROM = "from";
    private boolean hasJumpedToAppMarket = false;
    private MarketRateGuideDialogHelper marketGuideHelper;

    public static void showRateFrom(Context context, FiveStarRateTip.From from) {
        Intent intent = new Intent(context, RateAlertActivity.class);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.putExtra(RATE_FROM, from.value());
        Navigations.startActivitySafely(context, intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HSGlobalNotificationCenter.addObserver(FiveStarRateTip.FIVE_START_TIP_DISMISS, this);
        HSAlertMgr.delayRateAlert();
        marketGuideHelper = new MarketRateGuideDialogHelper(this);

        Intent intent = getIntent();
        if (intent != null) {
            int fromType = intent.getIntExtra(RATE_FROM, 0);
            FiveStarRateTip.From from = FiveStarRateTip.From.valueOf(fromType);
            if (from != null) {
                showDialog(from);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void showDialog(FiveStarRateTip.From from) {
        FiveStarRateTip.show(this, from, marketGuideHelper);
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        if (TextUtils.equals(FiveStarRateTip.FIVE_START_TIP_DISMISS, s)) {
            if (hsBundle != null) {
                hasJumpedToAppMarket = hsBundle.getBoolean(FiveStarRateTip.FIVE_START_TIP_JUMPED_TO_APP_MARKET);
                if (hasJumpedToAppMarket) {
                    return;
                }
            } else {
                hasJumpedToAppMarket = false;
            }
            finish();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (hasJumpedToAppMarket) {
            marketGuideHelper.unregisterReceiver(this);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        marketGuideHelper.unregisterReceiver(this);
    }
}
