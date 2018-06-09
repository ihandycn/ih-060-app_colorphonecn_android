package colorphone.acb.com.libscreencard.gif;

import net.appcloudbox.autopilot.AutopilotConfig;

/**
 * Created by sundxing on 2018/6/8.
 */

public class AutoPilotUtils {
    public static final String TOPIC = "topic-1528196849816-295";
    public static void logGIFInterstitialAdShow() {
        // TODO
    }

    public static void logGIFExpressAdShow() {

    }

    public static String getSecurityProtectionRecommendCard() {
        return "all";
    }

    public static void logRecommendCardGIFClick() {
//        AutopilotEvent.logAppEvent("charging_gif_card_click");
    }

    public static void logRecommendCardGIFShow() {
//        AutopilotEvent.logAppEvent("charging_gif_card_show");
    }

    public static void gameClick() {
//        AutopilotEvent.logAppEvent("charging_game_card_click");
    }

    public static void gameShow() {
//        AutopilotEvent.logAppEvent("charging_game_card_show");
    }

    public static void chargingViewShow() {
//        AutopilotEvent.logAppEvent("charging_view_show");
    }

    public static boolean enableScreenModule() {
//       return AutopilotConfig.getBooleanToTestNow(TOPIC, "charging_enable", false);
        return true;
    }

    public static boolean gameCardEnable() {
        return AutopilotConfig.getBooleanToTestNow(TOPIC, "game_card_enable", false);
    }

    public static boolean gifCardEnable() {
        return AutopilotConfig.getBooleanToTestNow(TOPIC, "gif_card_enable", false);
    }
}
