package com.honeycomb.colorphone.view;


import android.view.ViewGroup;
import android.widget.Toast;

import com.honeycomb.colorphone.AdPlacements;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Toasts;

import net.appcloudbox.ads.base.AcbRewardAd;
import net.appcloudbox.ads.common.utils.AcbError;
import net.appcloudbox.ads.rewardad.AcbRewardAdLoader;
import net.appcloudbox.ads.rewardad.AcbRewardAdManager;

import java.util.List;

public class RewardVideoView {

    private static final String TAG = RewardVideoView.class.getSimpleName();

    private final ViewGroup.LayoutParams MATCH_PARENT = new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

    private ViewGroup mRootView;
    private AdLoadingView mAdLoadingView;
    private AcbRewardAdLoader mRewardAdLoader;
    private OnRewarded mOnRewardedCallback;
    private boolean mFullScreenAdLoading;

    public RewardVideoView(ViewGroup root) {
        this(root, null, false);
    }

    public RewardVideoView(ViewGroup root, OnRewarded callback, boolean fullScreenAdLoading) {
        mRootView = root;
        mOnRewardedCallback = callback;
        mFullScreenAdLoading = fullScreenAdLoading;
    }

    private void onShowAdLoading() {
        if (mAdLoadingView == null) {
            mAdLoadingView = new AdLoadingView(mRootView.getContext(), mFullScreenAdLoading);
            mRootView.addView(mAdLoadingView, MATCH_PARENT);
        }
    }

    private void onHideAdLoading() {
        if (mAdLoadingView != null) {
            mRootView.removeView(mAdLoadingView);
            mAdLoadingView = null;
        }
    }

    private void tryShowRewardVideo() {
        mRewardAdLoader = AcbRewardAdManager.createLoaderWithPlacement(
                AdPlacements.AD_REWARD_VIDEO);
        mRewardAdLoader.load(1, new AcbRewardAdLoader.AcbRewardAdLoadListener() {
            @Override
            public void onAdReceived(AcbRewardAdLoader acbRewardAdLoader, List<AcbRewardAd> list) {
                HSLog.d(TAG, "reward video received, ad list empty = " + list.isEmpty());
                if (!list.isEmpty()) {
                    HSLog.d(TAG, "try show reward video ad");
                    final AcbRewardAd ad = list.get(0);
                    ad.setRewardAdListener(new AcbRewardAd.IAcbRewardAdListener() {

                        boolean isRewarded = false;

                        @Override
                        public void onRewarded(int i) {
                            HSLog.d(TAG, "reward video ad rewarded with " + i);

                            if (mOnRewardedCallback != null) {
                                isRewarded = true;
                                mOnRewardedCallback.onRewarded();
                            }
                        }

                        @Override
                        public void onAdClicked() {
                            HSLog.d(TAG, "reward video ad clicked");
                        }

                        @Override
                        public void onAdClosed() {
                            HSLog.d(TAG, "reward video ad closed");
                            ad.release();

                            if (mOnRewardedCallback != null) {
                                mOnRewardedCallback.onAdClose();
                            }

                            if (isRewarded && mOnRewardedCallback != null) {
                                mOnRewardedCallback.onAdCloseAndRewarded();
                            }
                        }

                        @Override
                        public void onAdDisplay() {
                            HSLog.d(TAG, "show reward video ad");
                        }
                    });

                    ad.show();
                    AcbRewardAdManager.preload(1, AdPlacements.AD_REWARD_VIDEO);
                    if (mOnRewardedCallback != null) {
                        mOnRewardedCallback.onAdShow();
                    }
                    onHideAdLoading();
                }
            }

            @Override
            public void onAdFinished(AcbRewardAdLoader acbRewardAdLoader, AcbError acbError) {
                HSLog.d(TAG, "load reward video finish, acbError = " + acbError);
                if (acbError != null) {
                    onHideAdLoading();
                    Toasts.showToast(R.string.load_reward_ad_error, Toast.LENGTH_LONG);
                }
            }
        });
    }

    public void onRequestRewardVideo() {
        onShowAdLoading();
        tryShowRewardVideo();
    }

    public void onCancel() {
        if (mRewardAdLoader != null) {
            mRewardAdLoader.cancel();
            onHideAdLoading();
        }
    }

    public interface OnRewarded {
        void onRewarded();

        void onAdClose();

        void onAdCloseAndRewarded();

        void onAdShow();
    }
}
