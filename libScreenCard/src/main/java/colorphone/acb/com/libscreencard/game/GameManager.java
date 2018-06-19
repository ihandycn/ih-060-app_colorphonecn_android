package colorphone.acb.com.libscreencard.game;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;

import net.appcloudbox.ads.interstitialad.AcbInterstitialAdManager;
import net.appcloudbox.ads.rewardad.AcbRewardAdManager;
import net.appcloudbox.h5game.AcbH5Error;
import net.appcloudbox.h5game.AcbH5GameInfo;
import net.appcloudbox.h5game.AcbH5GameInfoRequest;
import net.appcloudbox.h5game.AcbH5GameInfoResponse;
import net.appcloudbox.h5game.AcbH5GameLocalPackageDownloader;
import net.appcloudbox.h5game.AcbH5GamePlay;
import net.appcloudbox.h5game.AcbH5GameStats;
import net.appcloudbox.h5game.AcbH5ResponseListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import colorphone.acb.com.libscreencard.CardCustomConfig;
import colorphone.acb.com.libscreencard.gif.AutoPilotUtils;

/**
 * Created by sundxing on 2018/6/9.
 */

public class GameManager {
    private static final java.lang.String TAG = "GameManager";
    private static final String AD_NAME = "Game";
    private static final String AD_REWARD_NAME = "Reward";

    private final List<AcbH5GameInfo> mRandomGames = new ArrayList<>(4);

    private static String BasketBallId = "1";

    private AcbH5GameInfo mBasketBallInfo;
    private boolean mLoading;
    private List<AcbH5GameInfo> gameHasPicPool = new ArrayList<>();

    private GameManager() {}

    public void startGame() {
        HSLog.d(TAG, "startGame");
        AcbInterstitialAdManager.getInstance().activePlacementInProcess(AD_NAME);
        AcbRewardAdManager.getInstance().activePlacementInProcess(AD_REWARD_NAME);

        new AcbH5GamePlay(HSApplication.getContext(), mBasketBallInfo)
                .setInterstitialAdPlacement(AD_NAME)
                .setRewardedVideoAdPlacement(AD_REWARD_NAME)
                .setAdListener(new AcbH5GamePlay.AdListener() {
                    @Override
                    public void onAdShowChanceArrived(boolean b) {
                        CardCustomConfig.logAdViewEvent(AD_NAME, b);

                    }

                    @Override
                    public void onAdDisplayed(String s) {
                        if (AD_NAME.equals(s)) {
                            AutoPilotUtils.logGameExpressAdShow();
                        } else if (AD_REWARD_NAME.equals(s)) {
                            AutoPilotUtils.logGameRewardAdShow();
                        }

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
                        AcbInterstitialAdManager.getInstance().deactivePlacementInProcess(AD_NAME);
                        AcbRewardAdManager.getInstance().deactivePlacementInProcess(AD_REWARD_NAME);
                    }
                });

    }

    private static class InnerClass {
        private static GameManager INSTANCE = new GameManager();
    }

    public static GameManager getInstance() {
        return InnerClass.INSTANCE;
    }


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
                HSLog.e(TAG, acbH5Error.getMessage());
                mLoading = false;
            }
        });
    }

    public void prepareRandomGames() {
        HSLog.d(TAG, "prepare random games");
        if (gameHasPicPool.size() > 0) {
            confirmGameList();
            return;
        }
        new AcbH5GameInfoRequest().startForFirstPage(false, new AcbH5ResponseListener<AcbH5GameInfoResponse>() {
            @Override
            public void onSuccess(@NonNull AcbH5GameInfoResponse acbH5GameInfoResponse) {
                AcbH5GameInfo[] gameInfos = acbH5GameInfoResponse.getItems();
                for (AcbH5GameInfo gameInfo : gameInfos) {
                    if (!TextUtils.isEmpty(gameInfo.getLargePictureURL())
                            && !TextUtils.equals(BasketBallId, gameInfo.getGameID())) {
                        gameHasPicPool.add(gameInfo);
                    }
                }
                confirmGameList();
            }

            @Override
            public void onFailure(@NonNull AcbH5Error acbH5Error) {
                HSLog.e(TAG, acbH5Error.getMessage());
            }
        });
    }

    private void confirmGameList() {
        int[] gameIndexList = randomCommon(0, gameHasPicPool.size() - 1, 4);
        mRandomGames.clear();
        for (int index : gameIndexList) {
            HSLog.d(TAG, "random games index: " + index);
            AcbH5GameInfo gameInfo = gameHasPicPool.get(index);
            downloadPic(gameInfo);
        }
    }

    private void downloadPic(AcbH5GameInfo gameInfo) {
        Glide.with(HSApplication.getContext()).downloadOnly().load(gameInfo.getLargePictureURL()).into(new SimpleTarget<File>() {
            @Override
            public void onResourceReady(File resource, Transition<? super File> transition) {
                HSLog.d(TAG, "gameInfo pic download : " + gameInfo.getTitle());
                mRandomGames.add(gameInfo);
            }
        });
    }

    public List<AcbH5GameInfo> getRandomGames() {
        return mRandomGames;
    }

    public boolean isRandomGamesReady() {
        return mRandomGames.size() == 4;
    }

    public static int[] randomCommon(int min, int max, int n){
        if (n > (max - min + 1) || max < min) {
            throw new IllegalStateException("random min max value invalid");
        }
        int[] result = new int[n];
        int count = 0;
        while(count < n) {
            int num = (int) (Math.random() * (max - min)) + min;
            boolean flag = true;
            for (int j = 0; j < n; j++) {
                if(num == result[j]){
                    flag = false;
                    break;
                }
            }
            if(flag){
                result[count] = num;
                count++;
            }
        }
        return result;
    }

    private void findBasketballGame(AcbH5GameInfo[] items) {
        HSLog.d(TAG, "items size = " + items.length);
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
