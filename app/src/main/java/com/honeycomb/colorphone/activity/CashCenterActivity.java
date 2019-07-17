package com.honeycomb.colorphone.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.acb.cashcenter.CashCenterLayout;
import com.acb.cashcenter.HSCashCenterManager;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.superapps.util.Navigations;

public class CashCenterActivity extends HSAppCompatActivity {
    private static final String EXTRA_NAVIGATE_TO_WHEEL = "EXTRA_NAVIGATE_TO_WHEEL";
    private CashCenterLayout cashCenterLayout;

    public static void start(Context context, boolean navigationToWheel) {
        Intent intent = new Intent(context, com.acb.cashcenter.CashCenterActivity.class);
        intent.putExtra(EXTRA_NAVIGATE_TO_WHEEL, navigationToWheel);
        Navigations.startActivitySafely(context, intent);
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(0, 0);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(com.acb.cashcenter.R.layout.activity_cash_center);
      /*  TelephonyManager TelephonyMgr = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        imei = TelephonyMgr.getDeviceId();
        Log.i("imei是",imei);*/
//        if (getIntent() != null && getIntent().getBooleanExtra(EXTRA_NAVIGATE_TO_WHEEL, false)) {
//            Navigations.startActivitySafely(this, new Intent(this, LotteryWheelActivity.class));
//        }

        cashCenterLayout = findViewById(com.acb.cashcenter.R.id.cash_center_layout);
        cashCenterLayout.setCashCenterTaskListener(new CashCenterLayout.CashCenterTaskListener() {
            @Override public void onBackIconClick() {
                finish();
            }

            @Override public void onTaskClick(String s) {
                if (TextUtils.equals(s, CashCenterLayout.TASK_BIG_WHEEL)) {
                    HSCashCenterManager.getInstance().logEvent("CashCenter_CashWheel_Click");
                    HSCashCenterManager.getInstance().logEvent("CashCenter_CashWheel_Show", "CashCenterWheel", true);
                    Navigations.startActivity(CashCenterActivity.this, ColorPhoneActivity.class);
                    finish();
                }
            }
        });
    }

    @Override protected void onDestroy() {
        super.onDestroy();

        cashCenterLayout.onDestory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cashCenterLayout.onResume();
    }
}
