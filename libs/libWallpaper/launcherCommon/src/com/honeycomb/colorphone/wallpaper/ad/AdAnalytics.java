package com.honeycomb.colorphone.wallpaper.ad;

import com.honeycomb.colorphone.LauncherAnalytics;
import com.honeycomb.colorphone.BuildConfig;

import java.util.HashMap;

public class AdAnalytics {

    private static final HashMap<String, String> sEventNamePrefixMap = new HashMap<>(32);

    static {
        HashMap<String, String> map = sEventNamePrefixMap;

        /**
         * 7 Flurry Key for ONE Flurry Event at most
         **/

        // Events that uses AcbNativeAdAnalytics#logAppViewEvent directly
        map.put(AdPlacements.CHARGING_SCREEN_EXPRESS_AD_PLACEMENT_NAME, "");
        // boost ad, boost has been removed and this placement CAN NOT be used by others
        map.put(AdPlacements.LOCKER_EXPRESS_AD_PLACEMENT_NAME, "");
        // lucky ad, lucky has been removed and this placement CAN NOT be used by others
        // boost plus ad, boost plus has been removed and this placement CAN NOT be used by others
        // battery ad, battery has been removed and this placement CAN NOT be used by others
        // gift ad, gift has been removed and this placement CAN NOT be used by others

        // Events that add a prefix to event name "AcbAdNative_Viewed_In_App"
        // theme ad, theme has been removed and this placement CAN NOT be used by others
        // wallpaper ad, wallpaper has been removed and this placement CAN NOT be used by others
        // menu ad, menu has been removed and this placement CAN NOT be used by others
        // folder ad, folder has been removed and this placement CAN NOT be used by others
        // app drawer ad, app drawer has been removed and this placement CAN NOT be used by others
        // hub ad, hub has been removed and this placement CAN NOT be used by others
        // nearby, nearby has been removed and this placement CAN NOT be used by others

        // junk cleaner ad, junk cleaner has been removed and this placement CAN NOT be used by others
        // hub nearby ad, hub nearby has been removed and this placement CAN NOT be used by others
        // folder close ad, folder close has been removed and this placement CAN NOT be used by others
        // wallpaper preview ad, wallpaper preview has been removed and this placement CAN NOT be used by others
        // news ad, news has been removed and this placement CAN NOT be used by others
        map.put(AdPlacements.RESULT_PAGE_NATIVE_AD_PLACEMENT_NAME, "Launcher2");
        // app drawer and folder ad, app drawer and folder has been removed and this placement CAN NOT be used by others

        // widget, widget has been removed and this placement CAN NOT be used by others
        // search1, search1 has been removed and this placement CAN NOT be used by others
        // search news ad, search news has been removed and this placement CAN NOT be used by others
        map.put(AdPlacements.EXIT_INTERSTITIAL_AD_PLACEMENT_NAME, "Launcher3");
        map.put(AdPlacements.WALLPAPER_NATIVE_AD_PLACEMENT_NAME, "Launcher3");
        map.put(AdPlacements.HUB_EXPRESS_AD_PLACEMENT_NAME, "Launcher3");
        map.put(AdPlacements.FOLDER_ALL_APPS_NATIVE_AD_PLACEMENT_NAME, "Launcher3");

        // telephone, telephone has been removed and this placement CAN NOT be used by others
        // weather, weather has been removed and this placement CAN NOT be used by others
        map.put(AdPlacements.APP_LOCK_EXPRESS_AD_PLACEMENT_NAME, "Launcher4");


        map.put(AdPlacements.DESKTOP_WIDGET_NATIVE_AD_PLACEMENT_NAME, "Launcher5");
        // SoliGame, it has been removed and this placement CAN NOT be used by others
        map.put(AdPlacements.CHARGING_REPORT_EXPRESS_AD_PLACEMENT_NAME, "Launcher5");

        map.put(AdPlacements.SCREEN_GREETING_INTERSTITIAL_AD_PLACEMENT_NAME, "Launcher6");

        map.put(AdPlacements.EVENT_NAME_LUCKY, "Launcher6");
        map.put(AdPlacements.APP_MANAGER_NATIVE_AD_PLACEMENT_NAME, "Launcher6");

        map.put(AdPlacements.RESULT_PAGE_INTERSTITIAL_AD_PLACEMENT_NAME, "Launcher7");
        map.put(AdPlacements.HUB_EXPRESS_NEW_AD_PLACEMENT_NAME, "Launcher7");

        map.put(AdPlacements.APP_MANAGER_NATIVE_AD_PLACEMENT_NAME, "Launcher6");
        map.put(AdPlacements.MESSAGE_FLOATBALL_AD_PLACEMENT_NAME, "Launcher7");

        map.put(AdPlacements.DESKTOP_TIPS_INTERSTITIAL_AD_PLACEMENT_NAME, "Launcher7");
        map.put(AdPlacements.AD_PLACEMENT_NAME_LOCKER_GIF_EXPRESS, "Launcher7");
        map.put(AdPlacements.AD_PLACEMENT_NANE_LOCKER_GIF_INTERSTITIAL, "Launcher7");

        map.put(AdPlacements.APP_MANAGER_NATIVE_AD_PLACEMENT_NAME, "Launcher6");
        map.put(AdPlacements.HOROSCOPE_MAIN_AD_PLACEMENT_NAME,"Launcher8");
        map.put(AdPlacements.CASH_CENTER_INTERSTITIAL_AD_PLACEMENT_NAME, "Launcher9");
    }

    /**
     * 记录一次广告展示机会使用的利用率，目的有二：1. 是为了统计广告库提供广告的能力 2.结合功能相关事件来对比功能次数和广告展示次数的关系
     * 功能和 AcbNativeAdAnalytics 的 logAppViewEvent 函数一模一样，只不过那个函数的参数个数已经用满了，所以得新开一些事件来记录
     *
     * @param placementName 广告的 placementID
     * @param success       本次广告展示的机会成功取到广告并展示传 true，本次展示广告机会因广告获取失败或者获取太慢而未展示则传 false
     */
    public static void logAppViewEvent(String placementName, boolean success) {
        String prefix = sEventNamePrefixMap.get(placementName);
        if (prefix == null) {
            if (BuildConfig.DEBUG) {
                throw new IllegalArgumentException("Unregistered ad placement name " + placementName + ". "
                        + "Forget to add this placement name to AdAnalytics#sEventNamePrefixMap?");
            }
            return;
        }
        if (prefix.isEmpty()) {
            LauncherAnalytics.logEvent("AcbAdNative_Viewed_In_App", true, placementName, String.valueOf(success));
        } else {
            LauncherAnalytics.logEvent(prefix + "_AcbAdNative_Viewed_In_App", true, placementName, String.valueOf(success));
        }
    }
}
