package colorphone.acb.com.libscreencard.gif;

import net.appcloudbox.autopilot.AutopilotConfig;
import net.appcloudbox.autopilot.AutopilotEvent;

/**
 * Created by sundxing on 2018/6/8.
 */

public class AutoPilotUtils {
    public static final String TOPIC = "topic-1528196849816-295";
    public static void logGIFInterstitialAdShow() {
        AutopilotEvent.logTopicEvent(TOPIC, "gif_interstitial_ad_show");
    }

    public static void logGIFExpressAdShow() {
        AutopilotEvent.logTopicEvent(TOPIC, "gif_express_ad_show");
    }

    public static void logGameExpressAdShow() {
        AutopilotEvent.logTopicEvent(TOPIC, "game_interstitial_ad_show");
    }

    public static void logFmGameExpressAdShow() {
        AutopilotEvent.logTopicEvent(TOPIC, "fm_game_interstitial_ad_show");
    }

    public static void logGameRewardAdShow() {
        AutopilotEvent.logTopicEvent(TOPIC, "game_reward_ad_show");
    }


    public static String getSecurityProtectionRecommendCard() {
        return "all";
    }

    public static void logRecommendCardGIFClick() {
        AutopilotEvent.logTopicEvent(TOPIC, "charging_gif_card_click");
    }

    public static void logRecommendCardGIFShow() {
        AutopilotEvent.logTopicEvent(TOPIC, "charging_gif_card_show");
    }

    public static void gameClick() {
        AutopilotEvent.logTopicEvent(TOPIC, "charging_game_card_click");
    }

    public static void gameShow() {
        AutopilotEvent.logTopicEvent(TOPIC, "charging_game_card_show");
    }

    public static void chargingViewShow() {
        AutopilotEvent.logTopicEvent(TOPIC, "charging_view_show");
    }

    public static boolean enableScreenModule() {
        try {
            return AutopilotConfig.getBooleanToTestNow(TOPIC, "charging_enable", false);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean gameCardEnable() {
        return AutopilotConfig.getBooleanToTestNow(TOPIC, "game_card_enable", false);
    }

    public static boolean fmCardEnable() {
        return AutopilotConfig.getBooleanToTestNow(TOPIC, "fm_game_card_enable", false);
    }

    public static boolean gifCardEnable() {
        return AutopilotConfig.getBooleanToTestNow(TOPIC, "gif_card_enable", false);
    }

    public static final String TOPIC_FM = "topic-1528955961310-328";

    // FM-Game test
    public static boolean isFmCardFouInOneType() {
        String type = AutopilotConfig.getStringToTestNow(TOPIC_FM, "game_card_type", "One");
        return "FourInOne".equals(type);
    }

    public static void logFmCardShow() {
        AutopilotEvent.logTopicEvent(TOPIC, "charging_fm_game_card_show");
        AutopilotEvent.logTopicEvent(TOPIC_FM, "fm_game_card_show");
    }

    public static void logFmCardClick() {
        AutopilotEvent.logTopicEvent(TOPIC, "charging_fm_game_card_click");
        AutopilotEvent.logTopicEvent(TOPIC_FM, "fm_game_card_clicked");
    }


}
