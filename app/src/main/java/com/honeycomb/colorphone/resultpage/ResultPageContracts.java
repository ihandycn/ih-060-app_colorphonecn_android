package com.honeycomb.colorphone.resultpage;

import android.support.annotation.Nullable;

import com.honeycomb.colorphone.resultpage.data.CardData;

import net.appcloudbox.ads.base.AcbNativeAd;

import java.util.List;

/**
 * Interfaces holder.
 */
class ResultPageContracts {

    /**
     * View interface for MVP architecture.
     */
    interface View {
        /**
         * Configure and show result page view of given content type.
         */
        void show(ResultController.Type content, @Nullable List<CardData> cards);
    }

    /**
     * Presenter interface.
     */
    interface Presenter {
        /**
         * Determine content type and show result page. Caller is responsible for passing a ready ads manager in case
         * an ad shall be shown.
         *
         * Implementation calls {@link View#show(ResultController.Type, List)} and pass content type to view.
         */
        void show(AcbNativeAd ad);
    }
}
