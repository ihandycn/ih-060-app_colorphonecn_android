package com.honeycomb.colorphone;

import com.ihs.commons.config.HSConfig;

/**
 * Created by sundxing on 2018/2/27.
 */

public class Placements {
    public static final String BOOST_DONE = "BoostDone"; // Native ad
    public static final String BOOST_WIRE = "BoostWire"; //（Interstitial，清理全屏）
    public static final String THEME_WIRE = "ThemeWire"; //（主题详情页 全屏）
    public static final String THEME_DETAIL_NATIVE = "ThemeFullAd"; //（主题详情页 全屏native）

    public static final String CASHCENTER = "EarnCashWire";

    public static final String CABLE_DOWN = "CableDone";
    public static final String CABLE_WIRE = "CableWire";

    public static final String AD_CALL_OFF = "Lumen";
    public static final String AD_CHARGING_REPORT = "ChargingReportPlus";
    public static final String AD_MSG = "Texture";

    public static final String AD_REWARD_VIDEO = "Reward";
    public static final String AD_CALL_ASSISTANT_FULL_SCREEN = "CallFinishedWire";

    public static final String AD_CASH_NATIVE = "CashNative";
    public static final String AD_CASH_WIRE = "CashWire";
    public static final String AD_CASH_REWARD = "CashReward";

    public static final String AD_EXIT_TEXTURE_WIRE = "ExitTextureWire";
    public static final String AD_CLEAN_GUIDE = "BoostGuide";  // Express
    public static final String AD_EXIT_WIRE_NEW = "AdExitTextureNew"; // 信息助手退出 Native 拼全屏

    public static final String AD_NEWS_FEED = "NewsFeed";

    public static final String AD_LOCKER_AND_CHARGING = "CableFeed1";

    public static String getAdPlacement(String ad) {
        return getAdPrefix() + ad;
    }

    private static String getAdPrefix() {
        return HSConfig.optString("Air", "Application", "AdPlacementPrefix");
    }
}
