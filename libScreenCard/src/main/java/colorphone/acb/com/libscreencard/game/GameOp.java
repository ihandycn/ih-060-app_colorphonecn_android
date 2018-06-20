package colorphone.acb.com.libscreencard.game;

import net.appcloudbox.h5game.AcbH5GameInfo;

import colorphone.acb.com.libscreencard.CardCustomConfig;

/**
 * Created by sundxing on 2018/6/20.
 */

public abstract class GameOp {
    private static final String AD_NAME = "Game";
    private static final String AD_REWARD_NAME = "Reward";

    private AcbH5GameInfo mAcbH5GameInfo;

    protected GameOp(AcbH5GameInfo acbH5GameInfo) {
        mAcbH5GameInfo = acbH5GameInfo;
    }


    public void logGameChance(boolean adShow) {
        CardCustomConfig.logAdViewEvent(getAdName(false), adShow);
    }

    public String getAdName(boolean reward) {
        return reward ? AD_REWARD_NAME : AD_NAME;
    };

    public abstract void onGameShow();
    public abstract void onGameClick();
    public abstract void onGameAdShow(boolean reward);
}
