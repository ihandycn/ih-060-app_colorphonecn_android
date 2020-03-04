package com.colorphone.smartlocker.itemview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

import com.colorphone.lock.LockerCustomConfig;
import com.colorphone.lock.R;
import com.colorphone.smartlocker.utils.AutoPilotUtils;
import com.colorphone.smartlocker.viewholder.AdViewHolder;

import net.appcloudbox.ads.base.AcbAd;
import net.appcloudbox.ads.base.AcbNativeAd;
import net.appcloudbox.ads.nativead.AcbNativeAdManager;

import java.util.List;

public class AdListItem implements IDailyNewsListItem<RecyclerView.ViewHolder> {

    private AcbNativeAd acbNativeAd;

    private AdViewHolder adViewHolder;

    private boolean hasViewed = false;

    private Context context;
    private String category;

    private String appPlacement;

    public AdListItem(String appPlacement, AcbNativeAd acbNativeAd) {
        this.appPlacement = appPlacement;
        this.acbNativeAd = acbNativeAd;
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
        this.context = context;
        adViewHolder = (AdViewHolder) holder;

        adViewHolder.acbNativeAdContainerView.fillNativeAd(acbNativeAd, "");
        acbNativeAd.setNativeClickListener(new AcbNativeAd.AcbNativeClickListener() {
            @Override
            public void onAdClick(AcbAd acbAd) {
                updateAd();
            }
        });
    }

    public void setCategory(String category) {
        this.category = category;
    }

    private void updateAd() {
        List<AcbNativeAd> adList = AcbNativeAdManager.getInstance().fetch(appPlacement, 1);
        if (!adList.isEmpty()) {
            if (acbNativeAd != null) {
                acbNativeAd.release();
            }

//            AcbNativeAdManager.recordAdChanceEvent(appPlacement);
            acbNativeAd = adList.get(0);
            adViewHolder.acbNativeAdContainerView.fillNativeAd(acbNativeAd, "");


            LockerCustomConfig.getLogger().logEvent("SmartLockerFeed2_NativeAd", "type", "AdView");
            if (AutoPilotUtils.getLockerMode().equals("fuse")) {
                LockerCustomConfig.getLogger().logEvent("SmartLockerFeed3_NativeAd", "type", "AdView");
            } else if (AutoPilotUtils.getLockerMode().equals("cable")) {
                LockerCustomConfig.getLogger().logEvent("SmartLockerFeed4_NativeAd", "type", "AdView");
            }
            LockerCustomConfig.getLogger().logEvent("ad_show");
            AutoPilotUtils.logLockerModeAutopilotEvent("ad_show");


            acbNativeAd.setNativeClickListener(new AcbNativeAd.AcbNativeClickListener() {

                @Override
                public void onAdClick(AcbAd acbAd) {
                    LockerCustomConfig.getLogger().logEvent("SmartLockerFeed2_NativeAd", "type", "AdClick");
                    if (AutoPilotUtils.getLockerMode().equals("fuse")) {
                        LockerCustomConfig.getLogger().logEvent("SmartLockerFeed3_NativeAd", "type", "AdClick");
                    } else if (AutoPilotUtils.getLockerMode().equals("cable")) {
                        LockerCustomConfig.getLogger().logEvent("SmartLockerFeed4_NativeAd", "type", "AdClick");
                    }
                    LockerCustomConfig.getLogger().logEvent("ad_click");
                    updateAd();
                }
            });
        }
    }

    @Override
    public void release() {
        if (acbNativeAd != null) {
            acbNativeAd.release();
            acbNativeAd = null;
        }
    }

    @Override
    public void logViewedEvent() {
        if (!hasViewed) {
            hasViewed = true;
            LockerCustomConfig.getLogger().logEvent("feed_ads_viewed", "place", "daily", "channel", category);
        }
    }

    @Override
    public boolean hasViewed() {
        return hasViewed;
    }
}
