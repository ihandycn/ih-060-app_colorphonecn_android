package com.honeycomb.colorphone.triviatip;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.honeycomb.colorphone.Ap;
import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.Placements;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.superapps.occasion.OccasionManager;
import com.superapps.util.Navigations;
import com.superapps.util.Networks;
import com.superapps.util.Preferences;

import net.appcloudbox.ads.interstitialad.AcbInterstitialAdManager;
import net.appcloudbox.ads.nativead.AcbNativeAdManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import colorphone.acb.com.libscreencard.gif.Downloader;
import colorphone.acb.com.libscreencard.gif.SingleDownloadTask;

public class TriviaTip implements INotificationObserver, TriviaTipLayout.onTipDismissListener {

    // TODO
    private static final int MIN_VERSION_CODE = 445;

    private static final String TAG = TriviaTip.class.getSimpleName();
    static final String DOWNLOAD_DIRECTORY = "trivia_tip";

    private static final int FLAG_INIT = 0x00000001;
    private static final int FLAG_HANDLE_OCCASION = 0x00000002;
    private static final int FLAG_OCCASION_TARGET_SHOW = 0x00000004;
    private static final int FLAG_BOOST_TIP_SHOW = 0x00000008;
    private static final int FLAG_AD_DISMISS_JUST_NOW = 0x00000010;
    private static final int FLAG_PERMISSION_GUIDE_SHOW = 0x00000020;
    private static final int FLAG_FIVE_STAR_RATE_TIP_SHOW = 0x00000040;
    private static final int FLAG_IN_SHOW_INTERVAL = 0x00000080;
    private static final int FLAG_REACH_MAX_SHOW_TIME = 0x000000100;
    private static final int FLAG_NEW_USER = 0x000000200;
    private static final int FLAG_CHARGING_SHOW = 0x000000400;

    private static final String PREF_KEY_LAST_SHOW_TIME = "last_show_time";
    private static final String PREF_KEY_ALREADY_SHOW_TIME = "already_show_time";
    private static final String PREF_KEY_INTERSTITIAL_AD_DISMISS_TIME = "key_triviaTip_ad_dismiss_time";
    public static String PREF_KEY_TRIVIA_TIP_DISABLE_CLICKED = "key_triviaTip_user_disabled";

    private int mFlag = FLAG_INIT;
    private int mMaxShowTime = 3;

    private Handler mHandler;

    private TriviaDataManager mDataManager;

    private List<OnTipShowListener> mOnTipShowListeners = new ArrayList<>();


    public TriviaTip() {
        init();
    }

    public static boolean isModuleEnable() {
        boolean notDisableByUser = !Preferences.get(Constants.DESKTOP_PREFS).getBoolean(PREF_KEY_TRIVIA_TIP_DISABLE_CLICKED, false);
//        boolean masterSwitch = HSConfig.optBoolean(false, "Application", "TriviaFact", "MasterSwitch");
//        boolean defaultSwitch = HSConfig.optBoolean(false, "Application", "TriviaFact", "DefaultSwitch");
        return notDisableByUser
                && laterThanMinVersionCode();
    }

    public static void cacheImagesFirstTime() {
        // First time
        if (isModuleEnable()) {
            List<TriviaItem> items = TriviaDataManager.getInstance().getItems(3);
            for (TriviaItem item : items) {
                downloadTopImage(item);
            }
        }
    }

    public void updateConfig() {
        mMaxShowTime = HSConfig.optInteger(3, "Application", "TriviaFact", "MaxShowTime");
    }

    private void init() {
        mDataManager = new TriviaDataManager();
        addOnTipShowListeners(mDataManager);
        TriviaItem currentItem = mDataManager.getCurrentItem();
        if (currentItem != null
                && !Downloader.isCachedSuccess(DOWNLOAD_DIRECTORY, currentItem.imgUrl)) {
            downloadTopImage(currentItem);
        }
        HSGlobalNotificationCenter.addObserver(OccasionManager.NOTIFICATION_OCCASION_TARGET_SHOW, this);
        HSGlobalNotificationCenter.addObserver(OccasionManager.NOTIFICATION_HANDLE_OCCASION, this);

        mHandler = new PendingHandler(this);
        mMaxShowTime = HSConfig.optInteger(3, "Application", "TriviaFact", "MaxShowTime");
    }

    public void show() {
        boolean hasMessagePendingShow = mHandler.hasMessages(PendingHandler.PENDING_SHOW);
        if (hasFlag(FLAG_HANDLE_OCCASION) || hasMessagePendingShow) {
            HSLog.d(TAG, "Trivia tip can not show because pending occasion");
            if (!hasMessagePendingShow) {
                mHandler.sendEmptyMessageDelayed(PendingHandler.PENDING_SHOW, 550);
            }
            setFlag(FLAG_HANDLE_OCCASION, false);
            return;
        }

        if (hasFlag(FLAG_OCCASION_TARGET_SHOW)) {
            HSLog.d(TAG, "Trivia tip can not show because showing occasion target");
            return;
        }

        if (hasFlag(FLAG_BOOST_TIP_SHOW)) {
            HSLog.d(TAG, "Trivia tip can not show because showing boost tip");
            return;
        }

        if (hasFlag(FLAG_PERMISSION_GUIDE_SHOW)) {
            HSLog.d(TAG, "Trivia tip can not show because showing permission guide");
            return;
        }

        if (hasFlag(FLAG_CHARGING_SHOW)) {
            HSLog.d(TAG, "Trivia tip can not show because showing charging function");
            setFlag(FLAG_CHARGING_SHOW, false);
            return;
        }

        refreshFlag();

        if (hasFlag(FLAG_NEW_USER)) {
            HSLog.d(TAG, "Trivia tip can not show because the new user doesn't have this tip for an hour");
            return;
        }

        if (hasFlag(FLAG_AD_DISMISS_JUST_NOW)) {
            HSLog.d(TAG, "Trivia tip can not show because interstitial ad dismiss just now");
            return;
        }

        if (hasFlag(FLAG_IN_SHOW_INTERVAL)) {
            HSLog.d(TAG, "Trivia tip can not show because less than an hour since last show");
            return;
        }

        if (hasFlag(FLAG_REACH_MAX_SHOW_TIME)) {
            HSLog.d(TAG, "Trivia tip can not show because reach max show times");
            return;
        }


        TriviaItem currentItem = mDataManager.getCurrentItem();
        if (currentItem != null) {
            boolean cachedSuccess = Downloader.isCachedSuccess(DOWNLOAD_DIRECTORY, currentItem.imgUrl);
            if (cachedSuccess) {
                showTip(currentItem);
                Preferences.get(Constants.DESKTOP_PREFS).putLong(PREF_KEY_LAST_SHOW_TIME, System.currentTimeMillis());
                int showTime = Preferences.get(Constants.DESKTOP_PREFS).incrementAndGetInt(PREF_KEY_ALREADY_SHOW_TIME);
                HSLog.d(TAG, "The " + showTime + " times show today");
                for (OnTipShowListener onTipShowListener : mOnTipShowListeners) {
                    onTipShowListener.onShow(currentItem);
                }
                downloadTopImage(mDataManager.getCurrentItem());
            } else {
                downloadTopImage(mDataManager.getCurrentItem());
                HSLog.d(TAG, "Trivia tip can not show because image not cache success");
            }
        } else {
            HSLog.d(TAG, "Config prepare not finish");
        }
    }

    private void showTip(TriviaItem triviaItem) {
        preloadAd();
        Intent intent = new Intent(getContext(), TriviaTipActivity.class);
        intent.putExtra(TriviaTipActivity.EXTRA_ITEM, triviaItem);
        Navigations.startActivitySafely(getContext(), intent);
    }

    private void preloadAd() {
        AcbInterstitialAdManager.getInstance().activePlacementInProcess(Placements.TRIVIA_TIP_INTERSTITIAL_AD_PLACEMENT_NAME);
        AcbInterstitialAdManager.preload(1, Placements.TRIVIA_TIP_INTERSTITIAL_AD_PLACEMENT_NAME);
        AcbNativeAdManager.getInstance().activePlacementInProcess(Placements.TRIVIA_TIP_NATIVE_AD_PLACEMENT_NAME);
        AcbNativeAdManager.preload(1, Placements.TRIVIA_TIP_NATIVE_AD_PLACEMENT_NAME);
    }

    private Context getContext() {
        return HSApplication.getContext();
    }

    private void refreshFlag() {
        Preferences prefs = Preferences.get(Constants.DESKTOP_PREFS);

        // TODO
        long currentTime = System.currentTimeMillis();

        long lastShowTime = prefs.getLong(PREF_KEY_LAST_SHOW_TIME, -1);
        boolean inShowInterval = currentTime - lastShowTime < Ap.TriviaTip.intervalMins() * DateUtils.MINUTE_IN_MILLIS;
        setFlag(FLAG_IN_SHOW_INTERVAL, inShowInterval);

        boolean isToday = DateUtils.isToday(lastShowTime);
        HSLog.d(TAG, "isToday: " + isToday);
        if (!isToday) {
            prefs.putInt(PREF_KEY_ALREADY_SHOW_TIME, 0);
        }
        int alreadyShowTimes = prefs.getInt(PREF_KEY_ALREADY_SHOW_TIME, 0);
        HSLog.d(TAG, "Already show " + alreadyShowTimes + " times today");
        setFlag(FLAG_REACH_MAX_SHOW_TIME, alreadyShowTimes >= Ap.TriviaTip.maxCountDaily());

        long now = System.currentTimeMillis();
        setFlag(FLAG_NEW_USER, now - Utils.getAppInstallTimeMillis() < Ap.TriviaTip.installTimeHours() * DateUtils.HOUR_IN_MILLIS);
    }

    private boolean hasFlag(int flag) {
        return (mFlag & flag) != 0;
    }

    private void setFlag(int flag, boolean isEnabled) {
        if (isEnabled) {
            mFlag |= flag;
        } else {
            mFlag &= ~flag;
        }
    }

    private static String getDownloadPath(String url) {
        return Downloader.getDownloadPath(DOWNLOAD_DIRECTORY, url);
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        switch (s) {
            case OccasionManager.NOTIFICATION_OCCASION_TARGET_SHOW:
                setFlag(FLAG_OCCASION_TARGET_SHOW, hsBundle.getBoolean(OccasionManager.HS_BUNDLE_KEY_OCCASION_TARGET_SHOW, false));
                break;
            case OccasionManager.NOTIFICATION_HANDLE_OCCASION:
                setFlag(FLAG_HANDLE_OCCASION, true);
                break;
        }
    }

    private static void downloadTopImage(TriviaItem triviaItem) {
        if (triviaItem == null) {
            HSLog.d(TAG, "Can not read imgUrl from a null trivia item");
            return;
        }

        if (TextUtils.isEmpty(triviaItem.imgUrl)) {
            HSLog.d(TAG, "Empty image url: " + triviaItem.id);
            return;
        }
        if (Networks.isNetworkAvailable(-1)) {
            SingleDownloadTask downloadTask = new SingleDownloadTask(new Downloader.DownloadItem(triviaItem.imgUrl,
                    getDownloadPath(triviaItem.imgUrl)), new SingleDownloadTask.SingleTaskListener() {

                @Override
                public void onStart() {
                    HSLog.d(TAG, "Start download " + triviaItem.id);
                }

                @Override
                public void onSuccess(Downloader.DownloadItem item) {
                    HSLog.d(TAG, "Download success" + triviaItem.id);
                }

                @Override
                public void onFailed(Downloader.DownloadItem item) {
                    HSLog.d(TAG, "Download failed" + triviaItem.id);
                }
            });
            Downloader.getInstance().download(downloadTask, null);
        }
    }

    public void onDestroy() {
        HSLog.d("TriviaTip", "onDestroy");
        HSGlobalNotificationCenter.removeObserver(this);
        mHandler.removeCallbacksAndMessages(null);
        mOnTipShowListeners.clear();
    }


    @Override
    public void onDismiss() {
    }

    private static class PendingHandler extends Handler {

        private WeakReference<TriviaTip> mReference;

        static final int PENDING_SHOW = 1;

        PendingHandler(TriviaTip triviaTip) {
            mReference = new WeakReference<>(triviaTip);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PENDING_SHOW:
                    TriviaTip triviaTip = mReference.get();
                    if (triviaTip != null) {
                        triviaTip.show();
                    }
                    break;
            }
        }
    }

    public void addOnTipShowListeners(OnTipShowListener onTipShowListener) {
        mOnTipShowListeners.add(onTipShowListener);
    }

    public interface OnTipShowListener {
        void onShow(TriviaItem item);
    }

    public static boolean laterThanMinVersionCode() {
        return HSApplication.getFirstLaunchInfo().appVersionCode >= MIN_VERSION_CODE;
    }
}
