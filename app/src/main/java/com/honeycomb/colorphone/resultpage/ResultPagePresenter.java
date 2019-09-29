package com.honeycomb.colorphone.resultpage;

import android.support.annotation.NonNull;

import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.resultpage.data.CardData;
import com.ihs.commons.utils.HSLog;

import net.appcloudbox.ads.base.AcbNativeAd;

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

    private ResultPageContracts.View mView;

    private int mResultType;
    private ResultController.Type mType;

    ResultPagePresenter(@NonNull ResultPageContracts.View view, int resultType) {
        mView = view;
        mResultType = resultType;
    }

    @Override
    public void show(AcbNativeAd ad) {
        List<CardData> cards = null;
        recordFeatureLastUsedTime();

        mType = ResultController.Type.AD;
//        if (!determineWhetherToShowChargeScreen() || DEBUG_ALL_CARDS) {
//            HSLog.d(TAG, "ResultPage Debug all cards current type is " + mType);
//            if (!determineWhetherToShowNotificationCleaner() || DEBUG_ALL_CARDS) {
//                if (!determineWhetherToShowAppLock() || DEBUG_ALL_CARDS) {
//                    if (ad == null) {
//                        HSLog.d(TAG, "Ads not loaded in time, stop ads preloadForExitNews and show other cards");
//                        cards = setupCards();
//                    } else {
//                        mType = ResultController.Type.AD;
//                    }
//                }
//            }
//        }

        HSLog.d(TAG, "ResultPage mType = " + mType + " ad = " + ad + " cards = " + cards);
        mView.show(mType, cards);
    }

    private void recordFeatureLastUsedTime() {
        switch (mResultType) {
        }
    }

    private boolean determineWhetherToShowChargeScreen() {
        return mType == ResultController.Type.CHARGE_SCREEN;
    }

    private boolean determineWhetherToShowNotificationCleaner() {

        return mType == ResultController.Type.NOTIFICATION_CLEANER;
    }

    private boolean determineWhetherToShowAppLock() {

        return mType == ResultController.Type.APP_LOCK;
    }

    private List<CardData> setupCards() {

        List<CardData> cards = new ArrayList<>(4);

        return cards;
    }

    private void addCardIfNeeded(int cardType, List<CardData> urgentCards, List<CardData> normalCards) {
        if (mResultType == cardType) {
            return;
        }
        if (shouldShowCard(cardType)) {
            if (isUrgent(cardType) && urgentCards.size() < 2) {
                urgentCards.add(new CardData(cardType + 10));
            } else {
                normalCards.add(new CardData(cardType));
            }
        }
    }

    private boolean shouldShowCard(int cardType) {
        switch (cardType) {

            default:
                return false;
        }
    }

    private boolean isUrgent(int cardType) {
        switch (cardType) {
            default:
                return false;
        }
    }

}
