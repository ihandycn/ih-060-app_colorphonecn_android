package colorphone.acb.com.libscreencard.game;

import android.support.annotation.NonNull;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;

import net.appcloudbox.h5game.AcbH5Error;
import net.appcloudbox.h5game.AcbH5GameInfo;
import net.appcloudbox.h5game.AcbH5GameInfoRequest;
import net.appcloudbox.h5game.AcbH5GameInfoResponse;
import net.appcloudbox.h5game.AcbH5GameLocalPackageDownloader;
import net.appcloudbox.h5game.AcbH5GamePlay;
import net.appcloudbox.h5game.AcbH5GameStats;
import net.appcloudbox.h5game.AcbH5ResponseListener;

/**
 * Created by sundxing on 2018/6/9.
 */

public class GameManager {
    private static final java.lang.String TAG = "GameManager";
    private static final String AD_NAME = "Game";

    private GameManager() {}

    public void startGame() {
        HSLog.d(TAG, "startGame");
        new AcbH5GamePlay(HSApplication.getContext(), mBasketBallInfo)
                .setInterstitialAdPlacement(AD_NAME)
                .setAdListener(new AcbH5GamePlay.AdListener() {
                    @Override
                    public void onAdShowChanceArrived(boolean b) {

                    }

                    @Override
                    public void onAdDisplayed(String s) {

                    }

                    @Override
                    public void onAdClicked(String s) {

                    }

                    @Override
                    public void onAdClosed(String s) {

                    }
                })
                .start(new AcbH5GamePlay.PlayListener() {
                    @Override
                    public void onOpenFailure(@NonNull AcbH5Error acbH5Error) {
                        HSLog.e(TAG, "onOpenFailure");

                    }

                    @Override
                    public void onSessionEnd(@NonNull AcbH5GamePlay acbH5GamePlay, @NonNull AcbH5GameStats acbH5GameStats) {

                    }

                    @Override
                    public void onQuit(@NonNull AcbH5GamePlay acbH5GamePlay) {
                        HSLog.d(TAG, "onQuit");
                    }
                });

    }

    private static class InnerClass {
        private static GameManager INSTANCE = new GameManager();
    }

    public static GameManager getInstance() {
        return InnerClass.INSTANCE;
    }

    private static String BasketBallId = "1";

    private AcbH5GameInfo mBasketBallInfo;
    private boolean mLoading;

    public void prepare() {
        HSLog.d(TAG, "start obtain game list ...");
        if (mLoading || mBasketBallInfo != null) {
            return;
        }
        mLoading = true;
        new AcbH5GameInfoRequest().startForFirstPage(false, new AcbH5ResponseListener<AcbH5GameInfoResponse>() {

            @Override
            public void onSuccess(@NonNull AcbH5GameInfoResponse acbH5GameInfoResponse) {
                findBasketballGame( acbH5GameInfoResponse.getItems());
                mLoading = false;
                cache();
            }

            @Override
            public void onFailure(@NonNull AcbH5Error acbH5Error) {
                HSLog.e(acbH5Error.getMessage());
                mLoading = false;
            }
        });
    }

    private void findBasketballGame(AcbH5GameInfo[] items) {
        for (AcbH5GameInfo gameInfo : items) {
            if (BasketBallId.equals(gameInfo.getGameID())) {
                HSLog.d(TAG, "basket ball game get!");
                mBasketBallInfo = gameInfo;
                break;
            }
        }
        if (mBasketBallInfo == null) {
            throw new IllegalStateException("not found basketball game!");
        }
    }

    private void cache() {
        if (isGameReady()) {
            HSLog.d(TAG, "already cache!");

            return;
        }
        new AcbH5GameLocalPackageDownloader(mBasketBallInfo, false, new AcbH5ResponseListener<AcbH5GameInfo>() {
            @Override
            public void onSuccess(@NonNull AcbH5GameInfo acbH5GameInfo) {
                HSLog.d(TAG, "cache ok!");
            }

            @Override
            public void onFailure(@NonNull AcbH5Error acbH5Error) {
                HSLog.d(TAG, "cache fail! " + acbH5Error.getMessage());
            }
        }).start();
    }

    public AcbH5GameInfo getBasketBallInfo() {
        return mBasketBallInfo;
    }

    public boolean isGameReady() {
        return mBasketBallInfo != null && mBasketBallInfo.isLocalPlayPrepared();
    }
}
