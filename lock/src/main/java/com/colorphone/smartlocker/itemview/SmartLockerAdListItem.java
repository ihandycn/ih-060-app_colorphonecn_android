package com.colorphone.smartlocker.itemview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.colorphone.lock.LockerCustomConfig;
import com.colorphone.lock.R;
import com.colorphone.smartlocker.SmartLockerConstants;
import com.colorphone.smartlocker.SmartLockerManager;
import com.colorphone.smartlocker.utils.AutoPilotUtils;
import com.colorphone.smartlocker.viewholder.AdViewHolder;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSBundle;

import net.appcloudbox.ads.base.AcbAd;
import net.appcloudbox.ads.base.AcbNativeAd;
import net.appcloudbox.ads.common.utils.AcbError;
import net.appcloudbox.ads.nativead.AcbNativeAdLoader;
import net.appcloudbox.ads.nativead.AcbNativeAdManager;

import java.util.List;

public class SmartLockerAdListItem implements INewsListItem<RecyclerView.ViewHolder> {


    @Nullable
    private AcbNativeAd nativeAd;

    private AdViewHolder adViewHolder;

    private boolean hasLogShow = false;
    private boolean isDetachedFromWindow = true;

    public SmartLockerAdListItem() {
    }

    public void setLoadNativeAd(@Nullable AcbNativeAd nativeAd) {
        this.nativeAd = nativeAd;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.daily_news_ad_container;
    }

    @Override
    public AdViewHolder createViewHolder(Context context) {
        adViewHolder = new AdViewHolder(context, LayoutInflater.from(context).inflate(getLayoutRes(), null));
        return adViewHolder;
    }

    @Override
    public void bindViewHolder(final Context context, RecyclerView.ViewHolder holder, int position) {
        if (!(holder instanceof AdViewHolder)) {
            return;
        }
        adViewHolder = (AdViewHolder) holder;
        if (nativeAd == null) {
            adViewHolder.adContainer.findViewById(R.id.ad_container).setVisibility(View.GONE);
            return;
        }
        adViewHolder.adContainer.findViewById(R.id.ad_container).setVisibility(View.VISIBLE);
        adViewHolder.acbNativeAdContainerView.fillNativeAd(nativeAd, "");
        assert nativeAd != null;
        nativeAd.setNativeClickListener(new AcbNativeAd.AcbNativeClickListener() {
            @Override
            public void onAdClick(AcbAd ad) {
                logAdClick();
            }
        });
    }

    public int getCurrentPosition() {
        if (adViewHolder != null) {
            return adViewHolder.getAdapterPosition();
        }
        return -1;
    }

    @Override
    public void release() {
        if (nativeAd != null) {
            nativeAd.release();
            nativeAd = null;
        }
    }

    @Override
    public void attachedToWindow() {
        if (!hasLogShow) {
            logAdChance();
            //当第二个广告展示时，记录滑动事件
            if (SmartLockerManager.getInstance().getShowAdCount() == 1) {
                HSGlobalNotificationCenter.sendNotification(SmartLockerConstants.NOTIFICATION_FEED_PAGE_SLIDE);
            }
            SmartLockerManager.getInstance().setShowAdCount(SmartLockerManager.getInstance().getShowAdCount() + 1);
        }

        if (nativeAd == null) {
            List<AcbNativeAd> adList = AcbNativeAdManager.getInstance().fetch(LockerCustomConfig.get().getNewsFeedAdName(), 1);
            if (!adList.isEmpty()) {
                nativeAd = adList.get(0);
                HSBundle hsBundle = new HSBundle();
                hsBundle.putInt(SmartLockerConstants.NOTIFICATION_AD_ITEM_ID, adViewHolder.getAdapterPosition());
                HSGlobalNotificationCenter.sendNotification(SmartLockerConstants.NOTIFICATION_AD_ITEM_CHANGED, hsBundle);
                logAdShow();
                logAdUseRatio("True");
            } else {
                AcbNativeAdLoader adLoader = AcbNativeAdManager.getInstance().createLoaderWithPlacement(LockerCustomConfig.get().getNewsFeedAdName());
                adLoader.load(1, new AcbNativeAdLoader.AcbNativeAdLoadListener() {
                    @Override
                    public void onAdReceived(AcbNativeAdLoader acbNativeAdLoader, List<AcbNativeAd> list) {
                        if (list == null || list.isEmpty()) {
                            return;
                        }
                        nativeAd = list.get(0);
                        HSBundle hsBundle = new HSBundle();
                        hsBundle.putInt(SmartLockerConstants.NOTIFICATION_AD_ITEM_ID, adViewHolder.getAdapterPosition());
                        HSGlobalNotificationCenter.sendNotification(SmartLockerConstants.NOTIFICATION_AD_ITEM_CHANGED, hsBundle);
                        logAdShow();
                        logAdUseRatio("True");
                    }

                    @Override
                    public void onAdFinished(AcbNativeAdLoader acbNativeAdLoader, AcbError acbError) {
                    }
                });
            }

            AcbNativeAdManager.getInstance().preload(1, LockerCustomConfig.get().getNewsFeedAdName());
        }

        isDetachedFromWindow = false;
    }

    @Override
    public void detachedFromWindow() {
        //如果是从屏幕可见到屏幕不可见，并且这个view曾经展示过，并且广告没有展示时才上报false
        //主要是为了排除该方法的回调其他的时机：
        // 1.例如不在屏幕内，但是该item还没被recycle（曾展示过，但是被用户滑上去了），亮灭屏时会回调该方法，导致事件多报
        // 2.例如不在屏幕内，该item还有没有展示过（已经准备好，在当前屏幕最后一条的下面），亮灭屏时时也会回调该方法，导致事件多报
        if (!isDetachedFromWindow && !hasLogShow) {
            logAdUseRatio("False");
        }
        isDetachedFromWindow = true;
    }

    private void logAdChance() {
        LockerCustomConfig.getLogger().logEvent("SmartLockerFeed2_NativeAd", "type", "Chance");
        LockerCustomConfig.getLogger().logEvent("ad_chance");
        AutoPilotUtils.logLockerModeAutopilotEvent("ad_chance");
    }

    private void logAdShow() {
        LockerCustomConfig.getLogger().logEvent("SmartLockerFeed2_NativeAd", "type", "AdView");
        LockerCustomConfig.getLogger().logEvent("ad_show");
        AutoPilotUtils.logLockerModeAutopilotEvent("ad_show");

        hasLogShow = true;
    }

    private void logAdUseRatio(String result) {
        LockerCustomConfig.getLogger().logEvent("ColorPhone_News_AcbAdNative_Viewed_In_App", LockerCustomConfig.get().getNewsFeedAdName(), result);
    }

    private void logAdClick() {
        LockerCustomConfig.getLogger().logEvent("SmartLockerFeed2_NativeAd", "type", "AdClick");
    }
}
