package com.honeycomb.colorphone.activity;

import android.app.Activity;

import com.acb.cashcenter.CashCenterCallback;
import com.acb.cashcenter.HSCashCenterManager;
import com.acb.cashcenter.dialog.NoAdDialog;
import com.honeycomb.colorphone.util.Analytics;
import com.ihs.app.framework.HSApplication;

import net.appcloudbox.AcbAds;
import net.appcloudbox.ads.interstitialad.AcbInterstitialAdManager;
import net.appcloudbox.ads.nativead.AcbNativeAdManager;
import net.appcloudbox.ads.rewardad.AcbRewardAdManager;

import java.util.Map;

class CashCenterUtil {
    public static void init(ColorPhoneActivity activity) {
        HSCashCenterManager.getInstance().init(HSApplication.getContext(), new CashCenterCallback() {

            @Override public void onFeastInitFinish(boolean b, int i, String s) {

            }

            @Override public void eventCallBack(String s, String s1) {

            }

            @Override public void onWheelShow() {

            }

            @Override public void onWheelCoinEarn(long l) {

            }

            @Override public void onLogEvent(String s, Map<String, Object> map, boolean b) {

            }

            @Override public void logGameClick() {

            }

            @Override public void onExit() {

            }

            @Override public void onSpinClicked() {
                activity.setTabLayoutClickable(false);
                Analytics.logEvent("CashCenter_Wheel_Spin_Click");
            }

            @Override public void onSpinStop() {
                activity.setTabLayoutClickable(true);
            }

            @Override public void onInterstitialShown(boolean b) {
                Analytics.logEvent("CashCenter_Wire_Ad_Show");

                Analytics.logAdViewEvent("CashWire", b);
            }

            @Override public void onRewardShown() {
                Analytics.logEvent("CashCenter_Reward_Ad_Show");
            }

            @Override public void onNativeShown(boolean b) {
                Analytics.logEvent("CashCenter_Native_Ad_Show");

                Analytics.logAdViewEvent("CashNative", b);
            }

            @Override public void showInterstitialAd(NoAdDialog noAdDialog) {

            }
        });
        HSCashCenterManager.getInstance().setAutoFirstRewardFlag(false);
        HSCashCenterManager.getInstance().setCpid(4);

        AcbAds.getInstance().setActivity(activity);
        AcbAds.getInstance().setForegroundActivity(activity);

        AcbNativeAdManager.getInstance().activePlacementInProcess("CashNative");
        HSCashCenterManager.setNativeAdPlacement("CashNative");
        AcbInterstitialAdManager.getInstance().activePlacementInProcess("CashWire");
        HSCashCenterManager.setInterstitialAdPlacement("CashWire");

        AcbRewardAdManager.getInstance().activePlacementInProcess("CashReward");
        HSCashCenterManager.setRewardAdPlacement("CashReward");
    }

    public static void cleanAds(Activity activity) {
        AcbRewardAdManager.getInstance().cleanActivityAds(activity);
        AcbInterstitialAdManager.getInstance().cleanActivityAds(activity);
    }
}
