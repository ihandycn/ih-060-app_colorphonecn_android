package com.honeycomb.colorphone.resultpage;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;

import com.colorphone.lock.util.PreferenceHelper;
import com.honeycomb.colorphone.AdPlacements;
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.boost.AdUtils;
import com.honeycomb.colorphone.boost.BoostAutoPilotUtils;
import com.honeycomb.colorphone.resultpage.data.CardData;
import com.honeycomb.colorphone.resultpage.data.ResultConstants;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;

import net.appcloudbox.ads.base.AcbInterstitialAd;
import net.appcloudbox.ads.base.AcbNativeAd;
import net.appcloudbox.ads.interstitialads.AcbInterstitialAdLoader;
import net.appcloudbox.ads.nativeads.AcbNativeAdLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Presents Boost+ / Battery / CPU Cooler / Junk Cleaner / Notification Cleaner result page contents.
 */
public class ResultPagePresenter implements ResultPageContracts.Presenter {

    public static final String TAG = ResultPagePresenter.class.getSimpleName();

    @SuppressWarnings("PointlessBooleanExpression")
    private static final boolean DEBUG_ALL_CARDS = false && BuildConfig.DEBUG;

    private static final String PREF_KEY_CARDS_SHOW_COUNT = "result_page_cards_show_count";

    private static final boolean SHOULD_ADD_GUIDE_CARD = HSConfig.optBoolean(true, "Application", "ResultPage", "ShowFunctionGuideCards");

    private static final int MAX_COUNT_SHOW_BP_PROMOTION_CARD = HSConfig.optInteger(3, "Application", "ResultPagePromotion", "BatteryProtection");
    private static final int MAX_COUNT_SHOW_NC_PROMOTION_CARD = HSConfig.optInteger(3, "Application", "ResultPagePromotion", "NotificationCleaner");
    private static final int MAX_COUNT_SHOW_AL_PROMOTION_CARD = HSConfig.optInteger(3, "Application", "ResultPagePromotion", "APPLock");

    private ResultPageContracts.View mView;

    private int mResultType;
    private ResultController.Type mType;
    private List<CardData> mCards = new ArrayList<>();
    private AcbNativeAd mNativeAd;
    private AcbInterstitialAd mInterstitialAd;
    private boolean mWillShowInterstitialAd = false;

    ResultPagePresenter(@NonNull ResultPageContracts.View view, int resultType) {
        mView = view;
        mResultType = resultType;
    }

    @Override
    public void show() {
        mType = ResultController.Type.DEFAULT_VIEW;

        fetchAds();
        AdUtils.preloadResultPageAds();

        if (mWillShowInterstitialAd) {
            HSLog.i("Boost", "show Interstitial");
            BoostAutoPilotUtils.logBoostPushAdShow();
        } else if (mType == ResultController.Type.AD) {
            HSLog.i("Boost", "show AD");
            BoostAutoPilotUtils.logBoostPushAdShow();
            logPageContent();
            mView.show(mType, mInterstitialAd, mNativeAd, mCards);
//        } else if (mType == ResultController.Type.CARD_VIEW) {
//            if (!tryToShowCardView()) showDefaultView();
        } else {
            HSLog.i("Boost", "show default");
            showDefaultView();
        }
    }

    private void fetchAds() {
        List<AcbInterstitialAd> interstitialAds = AcbInterstitialAdLoader.fetch(HSApplication.getContext(), AdPlacements.AD_RESULT_PAGE_INTERSTITIAL, 1);
        mInterstitialAd = interstitialAds.isEmpty() ? null : interstitialAds.get(0);
        LauncherAnalytics.logEvent("InterstitialAdAnalysis", "ad_show_from", "ResultPage+" + (mInterstitialAd != null));
//        AdAnalytics.logAppViewEvent(AdPlacements.AD_RESULT_PAGE, mInterstitialAd != null);

        String adCombination;
        if (mInterstitialAd != null) {
//            mType = ResultController.Type.CARD_VIEW;
            mType = ResultController.Type.DEFAULT_VIEW;
            mWillShowInterstitialAd = true;

            List<AcbNativeAd> ads = AcbNativeAdLoader.fetch(HSApplication.getContext(), AdPlacements.AD_RESULT_PAGE, 1);
            mNativeAd = ads.isEmpty() ? null : ads.get(0);
//            AdAnalytics.logAppViewEvent(AdPlacements.AD_RESULT_PAGE, mNativeAd != null);
            LauncherAnalytics.logEvent("SixInOneAdAnalysis", "ad_show_from", "ResultPage_" + (mNativeAd != null));
            if (mNativeAd == null) {
                ads = AcbNativeAdLoader.fetch(HSApplication.getContext(), AdPlacements.AD_RESULT_PAGE, 1);
                mNativeAd = ads.isEmpty() ? null : ads.get(0);
//                AdAnalytics.logAppViewEvent(AdPlacements.AD_RESULT_PAGE, mNativeAd != null);
                if (mNativeAd == null) {
                    adCombination = "Weel";
                } else {
                    adCombination = "Weel and SevenInOne";
                }
            } else {
                adCombination = "Weel and SixInOne";
            }
        } else {
            List<AcbNativeAd> ads = AcbNativeAdLoader.fetch(HSApplication.getContext(), AdPlacements.AD_RESULT_PAGE, 1);
            mNativeAd = ads.isEmpty() ? null : ads.get(0);
//            AdAnalytics.logAppViewEvent(AdPlacements.AD_RESULT_PAGE, mNativeAd != null);
            if (mNativeAd != null) {
                mType = ResultController.Type.AD;
                HSLog.d(TAG, "result page ad type is " + mNativeAd.getVendorConfig().name());
                if (AdUtils.isFacebookAd(mNativeAd)) {
                    new Handler().postDelayed(new Runnable() {
                        @Override public void run() {
                            mView.showExitBtn();
                        }
                    }, 2500);
                }
                adCombination = "SevenInOne";
            } else {
                ads = AcbNativeAdLoader.fetch(HSApplication.getContext(), AdPlacements.AD_RESULT_PAGE, 1);
                mNativeAd = ads.isEmpty() ? null : ads.get(0);
//                AdAnalytics.logAppViewEvent(AdPlacements.AD_RESULT_PAGE, mNativeAd != null);
                LauncherAnalytics.logEvent("SixInOneAdAnalysis", "ad_show_from", "ResultPage_" + (mNativeAd != null));
                if (mNativeAd != null) {
                    mType = ResultController.Type.AD;
                    HSLog.d(TAG, "result page ad type is " + mNativeAd.getVendorConfig().name());
                    if (AdUtils.isFacebookAd(mNativeAd)) {
                        new Handler().postDelayed(new Runnable() {
                            @Override public void run() {
                                mView.showExitBtn();
                            }
                        }, 2500);
                    }
                    adCombination = "SixInOne";
                } else {
                    // no ads
//                    mType = ResultController.Type.CARD_VIEW;
                    mType = ResultController.Type.DEFAULT_VIEW;
                    adCombination = "No Ads";
                }
            }
        }

        HSLog.d(TAG, "result page ad combination is " + adCombination);

        boolean isAdShown = !"No Ads".equals(adCombination);
        LauncherAnalytics.logEvent("SevenInOneAdAnalysis", "ad_show_from", getResultTypeDescription(mResultType) + "_" + isAdShown);
        LauncherAnalytics.logEvent("ResultPageAdAnalysisType", "ad_chance", adCombination);
    }

    private boolean tryToShowFullScreenFunctionGuide() {
        boolean success = false;
//        if (determineWhetherToShowChargeScreen()) success = true;
//        else if (determineWhetherToShowNotificationCleaner()) success = true;
//        else if (determineWhetherToShowAppLock()) success = true;
//        else if (determineWhetherToShowUnreadMessage()) success = true;
//        else if (determineWhetherToShowWhatsApp()) success = true;

        if (success) {
            HSLog.d(TAG, "ResultPage Debug all cards current type is " + mType);
            logPageContent();
            mView.show(mType, null, null, mCards);
        }

        return success;
    }

    private boolean tryToShowCardView() {
//        mCards = setupCards();
//        if (mCards.isEmpty()) {
//            return false;
//        }
//
//        logPageContent();
//        mView.show(mType, mInterstitialAd, mNativeAd, mCards);
//        return true;
        return false;
    }

    private void showDefaultView() {
        mType = ResultController.Type.DEFAULT_VIEW;
        logPageContent();
        mView.show(mType, mInterstitialAd, mNativeAd, mCards);
    }

    private String getResultTypeDescription(int resultType) {
//        switch (resultType) {
//            case ResultConstants.RESULT_TYPE_BATTERY:
//                return "BatteryProtection";
//            case ResultConstants.RESULT_TYPE_BOOST_PLUS:
//                return "SuperBoost";
//            case ResultConstants.RESULT_TYPE_JUNK_CLEAN:
//                return "JunkClean";
//            case ResultConstants.RESULT_TYPE_CPU_COOLER:
//                return "CpuCooler";
//            case ResultConstants.RESULT_TYPE_NOTIFICATION_CLEANER:
//                return "NotificationCleaner";
//            case ResultConstants.RESULT_TYPE_BOOST_TOOLBAR:
//                return "Boost";
//            case ResultConstants.RESULT_TYPE_VIRUS_SCAN:
//                return "VirusScan";
//        }
//        return "Others";
        return "Boost";
    }

    private void logPageContent() {
        String pageContentDescription = null;
        switch (mType) {
            case AD:
                pageContentDescription = "AD";
                break;
//            case CHARGE_SCREEN:
//            case NOTIFICATION_CLEANER:
//            case APP_LOCK:
//            case UNREAD_MESSAGE:
//            case WHATS_APP:
//                pageContentDescription = "FullPromotion";
//                break;
            case DEFAULT_VIEW:
                pageContentDescription = "DefaultPage";
                break;
        }
        if (pageContentDescription != null) {
            LauncherAnalytics.logEvent("ResultPage_Content_Show", "type", pageContentDescription);
        }
    }

//    private boolean determineWhetherToShowChargeScreen() {
//        if (!LockSwitch.isLockEnabled()) {
//            return false;
//        }
//
//        if (DEBUG_ALL_CARDS) {
//            mType = ResultController.Type.CHARGE_SCREEN;
//            return true;
//        }
//
//        boolean isChargingScreenEverEnabled = ChargingScreenSettings.isChargingScreenEverEnabled();
//        int intoCount = PreferenceHelper.get(LauncherFiles.BOOST_PREFS).getInt(ResultConstants.PREF_KEY_INTO_BATTERY_PROTECTION_COUNT, 0);
//        boolean alreadyShowedToday = isToday(ResultConstants.PREF_KEY_INTO_BATTERY_PROTECTION_SHOWN_TIME);
//        int batteryProtectionLimitCount = HSConfig.optInteger(1, "Application", "ResultPageCard", "BatteryProtection");
//
//        HSLog.d(TAG, "ResultPage show determineWhetherToShowChargeScreen mResultType = " + mResultType + " intoCount = " + intoCount
//                + " isChargingScreenEverEnabled = " + isChargingScreenEverEnabled + " alreadyShowedToday = " + alreadyShowedToday);
//
//        if (intoCount < batteryProtectionLimitCount
//                && !isChargingScreenEverEnabled && !alreadyShowedToday) {
//            mType = ResultController.Type.CHARGE_SCREEN;
//        }
//        return mType == ResultController.Type.CHARGE_SCREEN;
//    }
//
//    private boolean determineWhetherToShowNotificationCleaner() {
//        if (NotificationCleanerUtils.canUseNotificationCleaner()) {
//            if (DEBUG_ALL_CARDS) {
//                mType = ResultController.Type.NOTIFICATION_CLEANER;
//                return true;
//            }
//
//            if (mResultType != ResultConstants.RESULT_TYPE_NOTIFICATION_CLEANER) {
//                boolean isNotificationCleanerEnabled = NotificationCleanerUtils.isNotificationOrganizerEnabled();
//                int intoCount = PreferenceHelper.get(LauncherFiles.NOTIFICATION_CLEANER_PREFS).getInt(ResultConstants.PREF_KEY_INTO_NOTIFICATION_CLEANER_COUNT, 0);
//                boolean alreadyShowedToday = isToday(ResultConstants.PREF_KEY_INTO_NOTIFICATION_CLEANER_SHOWN_TIME);
//                int notificationCleanerLimitCount = HSConfig.optInteger(1, "Application", "ResultPageCard", "NotificationCleaner");
//
//                HSLog.d(TAG, "ResultPage show determineWhetherToShowNotificationCleaner mResultType = " + mResultType + " intoCount = " + intoCount
//                        + " alreadyShowedToday = " + alreadyShowedToday);
//
//                if (intoCount < notificationCleanerLimitCount
//                        && !isNotificationCleanerEnabled
//                        && !alreadyShowedToday) {
//                    mType = ResultController.Type.NOTIFICATION_CLEANER;
//                }
//            }
//        }
//        return mType == ResultController.Type.NOTIFICATION_CLEANER;
//    }
//
//    private boolean determineWhetherToShowAppLock() {
//        if (DEBUG_ALL_CARDS) {
//            mType = ResultController.Type.APP_LOCK;
//            return true;
//        }
//
//        if (!AppLockProvider.isPasswordAlreadySet()) {
//            int intoCount = PreferenceHelper.get(LauncherFiles.COMMON_PREFS).getInt(ResultConstants.PREF_KEY_INTO_APP_LOCK_COUNT, 0);
//            boolean alreadyShowedToday = isToday(ResultConstants.PREF_KEY_INTO_APP_LOCK_SHOWN_TIME);
//            int appLockLimitCount = HSConfig.optInteger(1, "Application", "ResultPageCard", "AppLock");
//
//            HSLog.d(TAG, "ResultPage show determineWhetherToShowNotificationCleaner mResultType = " + mResultType + " intoCount = " + intoCount
//                    + " alreadyShowedToday = " + alreadyShowedToday);
//
//            if (intoCount < appLockLimitCount && !alreadyShowedToday) {
//                mType = ResultController.Type.APP_LOCK;
//            }
//        }
//        return mType == ResultController.Type.APP_LOCK;
//    }
//
//    private boolean determineWhetherToShowUnreadMessage() {
//        if (DEBUG_ALL_CARDS) {
//            mType = ResultController.Type.UNREAD_MESSAGE;
//            return true;
//        }
//
//        if (!NotificationCleanerUtils.isNotificationAccessGranted(HSApplication.getContext())) {
//            int intoCount = PreferenceHelper.get(LauncherFiles.COMMON_PREFS).getInt(ResultConstants.PREF_KEY_INTO_UNREAD_MESSAGE_COUNT, 0);
//            int unreadMessageLimitCount = HSConfig.optInteger(1, "Application", "ResultPageCard", "UnreadMessage");
//
//            HSLog.d(TAG, "ResultPage show determineWhetherToShowUnreadMessage mResultType = " + mResultType + " intoCount = " + intoCount);
//
//            if (intoCount < unreadMessageLimitCount) {
//                mType = ResultController.Type.UNREAD_MESSAGE;
//            }
//        }
//        return mType == ResultController.Type.UNREAD_MESSAGE;
//    }
//
//    private boolean determineWhetherToShowWhatsApp() {
//        if (DEBUG_ALL_CARDS) {
//            mType = ResultController.Type.WHATS_APP;
//            return true;
//        }
//
//        if (new LauncherCallFactoryImpl().getNotificationConfig().enable()
//                && !NotificationCleanerUtils.isNotificationAccessGranted(HSApplication.getContext())) {
//            int intoCount = PreferenceHelper.get(LauncherFiles.COMMON_PREFS).getInt(ResultConstants.PREF_KEY_INTO_WHATS_APP_COUNT, 0);
//            int whatsAppLimitCount = HSConfig.optInteger(1, "Application", "ResultPageCard", "WhatsApp");
//
//            HSLog.d(TAG, "ResultPage show determineWhetherToShowWhatsApp mResultType = " + mResultType + " intoCount = " + intoCount);
//
//            if (intoCount < whatsAppLimitCount) {
//                mType = ResultController.Type.WHATS_APP;
//            }
//        }
//        return mType == ResultController.Type.WHATS_APP;
//    }
//
//    private List<CardData> setupCards() {
//        PreferenceHelper.get(Constants.DESKTOP_PREFS).incrementAndGetInt(PREF_KEY_CARDS_SHOW_COUNT);
//
//        List<CardData> cards = new ArrayList<>(4);
//        if (DEBUG_ALL_CARDS) {
//            cards.addAll(cards);
//            dumpCards(cards, "Debugging, all cards are displayed");
//            return cards;
//        }
//
//        // generate urgent cards and normal cards
//        List<CardData> urgentCards = new ArrayList<>();
//        List<CardData> normalCards = new ArrayList<>();
//
//        addCardIfNeeded(ResultConstants.CARD_VIEW_TYPE_CPU_COOLER, urgentCards, normalCards);
//        addCardIfNeeded(ResultConstants.CARD_VIEW_TYPE_BATTERY, urgentCards, normalCards);
//        addCardIfNeeded(ResultConstants.CARD_VIEW_TYPE_JUNK_CLEANER, urgentCards, normalCards);
//        addCardIfNeeded(ResultConstants.CARD_VIEW_TYPE_BOOST_PLUS, urgentCards, normalCards);
//
//        if (!urgentCards.isEmpty() || !normalCards.isEmpty()) {
//            // add guide card if needed
//            CardData guideCard = null;
//            if (SHOULD_ADD_GUIDE_CARD) {
////                guideCard = getGuideCard();
//            }
//
//            if (guideCard != null) {
//                cards.add(guideCard);
//            }
//            cards.addAll(urgentCards);
//            cards.addAll(normalCards);
//        }
//
//        dumpCards(cards, "Displayed cards in final order");
//        return cards;
//    }
//
//    private void addCardIfNeeded(int cardType, List<CardData> urgentCards, List<CardData> normalCards) {
//        if (shouldShowCard(cardType)) {
//            if (isUrgent(cardType) && urgentCards.size() < 2) {
//                urgentCards.add(new CardData(cardType + 10));
//            } else {
//                normalCards.add(new CardData(cardType));
//            }
//        }
//    }

    private boolean shouldShowCard(int cardType) {
//        switch (cardType) {
//            case ResultConstants.CARD_VIEW_TYPE_CPU_COOLER:
//                return shouldShowCpuCoolerCard();
//            case ResultConstants.CARD_VIEW_TYPE_BATTERY:
//                return shouldShowBatteryCard();
//            case ResultConstants.CARD_VIEW_TYPE_JUNK_CLEANER:
//                return shouldShowJunkCleanCard();
//            case ResultConstants.CARD_VIEW_TYPE_BOOST_PLUS:
//                return shouldShowBoostPlusCard();
//            default:
//                return false;
//        }
        return false;
    }

//    private boolean shouldShowBatteryCard() {
//        if (!LockSwitch.isLockEnabled()) {
//            return false;
//        }
//        long lastBatteryUsedTime = PreferenceHelper.get(LauncherFiles.BATTERY_PREFS)
//                .getLong(ResultConstants.PREF_KEY_LAST_BATTERY_USED_TIME, -1);
//        long timeSinceLastUse = (System.currentTimeMillis() - lastBatteryUsedTime) / 1000 / 60;
//        HSLog.d(TAG, timeSinceLastUse + " minutes since last used battery");
//        int interval = HSConfig.optInteger(10, "Application", "ResultPage", "BatteryCard", "ShowResultCardInterval");
//        return timeSinceLastUse > interval;
//    }
//
//    private boolean shouldShowBoostPlusCard() {
//        long lastBoostPlusUsedTime = PreferenceHelper.get(LauncherFiles.BOOST_PREFS)
//                .getLong(ResultConstants.PREF_KEY_LAST_BOOST_PLUS_USED_TIME, -1);
//        long timeSinceLastUse = (System.currentTimeMillis() - lastBoostPlusUsedTime) / 1000 / 60;
//        HSLog.d(TAG, timeSinceLastUse + " minutes since last used Boost+");
//        int interval = HSConfig.optInteger(10, "Application", "ResultPage", "BoostCard", "ShowResultCardInterval");
//        return timeSinceLastUse > interval;
//    }
//
//    private boolean shouldShowJunkCleanCard() {
//        long lastJunkCleanUsedTime = PreferenceHelper.get(LauncherFiles.JUNK_CLEAN_PREFS)
//                .getLong(ResultConstants.PREF_KEY_LAST_JUNK_CLEAN_USED_TIME, -1);
//        long timeSinceLastUse = (System.currentTimeMillis() - lastJunkCleanUsedTime) / 1000 / 60;
//        HSLog.d(TAG, timeSinceLastUse + " minutes since last used Junk Clean");
//        int interval = HSConfig.optInteger(10, "Application", "ResultPage", "JunkCard", "ShowResultCardInterval");
//        return timeSinceLastUse > interval;
//    }
//
//    private boolean shouldShowCpuCoolerCard() {
//        long lastCpuCoolerUsedTime = PreferenceHelper.get(LauncherFiles.CPU_COOLER_PREFS)
//                .getLong(ResultConstants.PREF_KEY_LAST_CPU_COOLER_USED_TIME, -1);
//        long timeSinceLastUse = (System.currentTimeMillis() - lastCpuCoolerUsedTime) / 1000 / 60;
//        HSLog.d(TAG, timeSinceLastUse + " minutes since last used Cpu Cooler");
//        int interval = HSConfig.optInteger(10, "Application", "ResultPage", "CPUCard", "ShowResultCardInterval");
//        int temperature = CpuCoolerManager.getInstance().fetchCpuTemperature();
//        return timeSinceLastUse > interval && temperature >= 40;
//    }
//
    private boolean isUrgent(int cardType) {
//        switch (cardType) {
//            case ResultConstants.CARD_VIEW_TYPE_CPU_COOLER:
//                return isCpuUrgent();
//            case ResultConstants.CARD_VIEW_TYPE_BATTERY:
//                return isBatteryUrgent();
//            case ResultConstants.CARD_VIEW_TYPE_JUNK_CLEANER:
//                return isJunkUrgent();
//            case ResultConstants.CARD_VIEW_TYPE_BOOST_PLUS:
//                return isBoostUrgent();
//            default:
//                return false;
//        }
        return false;
    }
//
//    private boolean isBatteryUrgent() {
//        int batteryLevel = DeviceManager.getInstance().getBatteryLevel();
//        int condition = HSConfig.optInteger(30, "Application", "ResultPage", "BatteryCard", "MaxBatteryLevelToShowUrgentCard");
//        return batteryLevel <= condition;
//    }
//
//    private boolean isCpuUrgent() {
//        int temperature = CpuCoolerManager.getInstance().fetchCpuTemperature();
//        int condition = HSConfig.optInteger(40, "Application", "ResultPage", "CPUCard", "MinimumCPUTemperatureToShowUrgentCard");
//        return temperature >= condition;
//    }
//
//    private boolean isJunkUrgent() {
//        long memJunkSize = JunkManager.getInstance().getMemoryJunkSize();
//        long adJunkSize = JunkManager.getInstance().getAdJunkSize();
//        long appJunkSize = JunkManager.getInstance().getAppJunkSize();
//
//        long totalJunkSize = memJunkSize + adJunkSize + appJunkSize;
//        int condition = HSConfig.optInteger(300, "Application", "ResultPage", "JunkCard", "MinimumJunkSizeToShowUrgentCard");
//        if (totalJunkSize / (1024 * 1024) >= condition) {
//            PreferenceHelper.getDefault().putLong(ResultConstants.PREF_KEY_RESULTPAGE_CARD_JUNK_CLEAN_TOTAL_JUNK_SIZE, totalJunkSize);
//            PreferenceHelper.getDefault().putLong(ResultConstants.PREF_KEY_RESULTPAGE_CARD_JUNK_CLEAN_MEM_JUNK_SIZE, memJunkSize);
//            PreferenceHelper.getDefault().putLong(ResultConstants.PREF_KEY_RESULTPAGE_CARD_JUNK_CLEAN_AD_JUNK_SIZE, adJunkSize);
//            PreferenceHelper.getDefault().putLong(ResultConstants.PREF_KEY_RESULTPAGE_CARD_JUNK_CLEAN_APP_JUNK_SIZE, appJunkSize);
//            return true;
//        } else {
//            return false;
//        }
//    }
//
//    private boolean isBoostUrgent() {
//        int ramUsage = RamUsageDisplayUpdater.getInstance().getDisplayedRamUsage();
//        int condition = HSConfig.optInteger(55, "Application", "ResultPage", "BoostCard", "MinimumRamUsageToShowUrgentCard");
//        return ramUsage >= condition;
//    }
//
//    /**
//     * Judge whether a function guiding card should be popped up.
//     * If there's any, return its prototype; otherwise return null.
//     *
//     * @return
//     */
//    private CardData getGuideCard() {
//        int lastShownCardType = PreferenceHelper.get(LauncherFiles.COMMON_PREFS).getInt(
//                ResultConstants.PREF_KEY_GUIDE_CARD_SHOW_INDEX, ResultConstants.CARD_VIEW_TYPE_GUIDE_BP - 1);
//        for (int i = lastShownCardType + 1; i <= lastShownCardType + 5; i++) {
//            int type = i;
//            if (type > ResultConstants.CARD_VIEW_TYPE_GUIDE_APPLOCK_PERMISSION) {
//                type = type - 5;
//            }
//            if (shouldGuideCardShow(type)) {
//                PreferenceHelper.get(LauncherFiles.COMMON_PREFS).putInt(
//                        ResultConstants.PREF_KEY_GUIDE_CARD_SHOW_INDEX, type);
//                return new CardData(type);
//            }
//        }
//        return null;
//    }
//
//    private boolean shouldGuideCardShow(int cardType) {
//        boolean shouldShow = false;
//        switch (cardType) {
//            case ResultConstants.CARD_VIEW_TYPE_GUIDE_BP:
//                shouldShow = !ChargingScreenSettings.isChargingScreenEverEnabled()
//                        && PreferenceHelper.get(LauncherFiles.COMMON_PREFS).getInt(ResultConstants.PREF_KEY_GUIDE_BP_SHOW_COUNT, 0) < MAX_COUNT_SHOW_BP_PROMOTION_CARD
//                        && !isToday(ResultConstants.PREF_KEY_BP_PROMOTION_CARD_SHOWN_TIME);
//                break;
//            case ResultConstants.CARD_VIEW_TYPE_GUIDE_NC:
//                shouldShow = NotificationCleanerUtils.isNotificationCleanerNeedShow()
//                        && PreferenceHelper.get(LauncherFiles.COMMON_PREFS).getInt(ResultConstants.PREF_KEY_GUIDE_NC_SHOW_COUNT, 0) < MAX_COUNT_SHOW_NC_PROMOTION_CARD
//                        && !isToday(ResultConstants.PREF_KEY_NC_PROMOTION_CARD_SHOWN_TIME);
//                break;
//            case ResultConstants.CARD_VIEW_TYPE_GUIDE_APPLOCK_1:
//                shouldShow = false && !AppLockProvider.isPasswordAlreadySet();
//                break;
//            case ResultConstants.CARD_VIEW_TYPE_GUIDE_APPLOCK_2:
//                shouldShow = !AppLockProvider.isPasswordAlreadySet()
//                        && PreferenceHelper.get(LauncherFiles.COMMON_PREFS).getInt(ResultConstants.PREF_KEY_GUIDE_APP_LOCK_2_SHOW_COUNT, 0) < MAX_COUNT_SHOW_AL_PROMOTION_CARD
//                        && !isToday(ResultConstants.PREF_KEY_AL_PROMOTION_CARD_SHOWN_TIME);
//                break;
//            case ResultConstants.CARD_VIEW_TYPE_GUIDE_APPLOCK_PERMISSION:
//                shouldShow = AppLockProvider.isPasswordAlreadySet()
//                        && PermissionUtils.shouldEnforceUsageAccessPermission()
//                        && PreferenceHelper.get(LauncherFiles.COMMON_PREFS).getInt(ResultConstants.PREF_KEY_GUIDE_APP_LOCK_PERMISSION_SHOW_COUNT, 0) < MAX_COUNT_SHOW_AL_PROMOTION_CARD
//                        && !isToday(ResultConstants.PREF_KEY_AL_PROMOTION_CARD_SHOWN_TIME);
//                break;
//        }
//        return shouldShow;
//    }

    private void addCard(List<CardData> cards, int cardType) {
        cards.add(new CardData(cardType));
    }

    private void dumpCards(List<CardData> cards, String dumpName) {
        StringBuilder str = new StringBuilder(dumpName).append(": \n==========\n");
//        Stream.of(cards).forEach(new Consumer<List<CardData>>() {
//            @Override public void accept(List<CardData> card) {
//                str.append(card.toString()).append('\n');
//            }
//        });
        for (CardData card : cards) {
            str.append(card.toString()).append('\n');
        }
        HSLog.d(TAG, str.append("==========\n\n").toString());
    }

    private static boolean isToday(String prefsKey) {
//        long now = System.currentTimeMillis();
        long recorded = PreferenceHelper.getDefault().getLong(prefsKey, 0);
//        return Utils.getDayDifference(now, recorded) == 0;
        return DateUtils.isToday(recorded);
    }

    private int getResultPageShownCount() {
        return PreferenceHelper.get(Constants.NOTIFICATION_PREFS).getInt(ResultConstants.PREF_KEY_RESULT_PAGE_SHOWN_COUNT, 1);
    }
}
