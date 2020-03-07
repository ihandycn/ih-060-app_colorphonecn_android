package com.colorphone.smartlocker.itemview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.colorphone.lock.LockerCustomConfig;
import com.colorphone.lock.R;
import com.colorphone.smartlocker.utils.AutoPilotUtils;
import com.colorphone.smartlocker.viewholder.AdViewHolder;

import net.appcloudbox.ads.base.AcbAd;
import net.appcloudbox.ads.base.AcbNativeAd;
import net.appcloudbox.ads.nativead.AcbNativeAdManager;

import java.util.List;

public class SmartLockerAdListItem implements IDailyNewsListItem<RecyclerView.ViewHolder> {

    @Nullable
    private AcbNativeAd fetchNativeAd, loadNativeAd;

    private AdViewHolder adViewHolder;

    private boolean hasViewed = false;

    private Context context;
    private String category;

    private String appPlacement;

    public SmartLockerAdListItem(String appPlacement, @Nullable AcbNativeAd fetchNativeAd) {
        this.appPlacement = appPlacement;
        this.fetchNativeAd = fetchNativeAd;
    }

    public void setLoadNativeAd(@Nullable AcbNativeAd loadNativeAd) {
        this.loadNativeAd = loadNativeAd;
    }

    public boolean isNativeAdNull() {
        return fetchNativeAd == null && loadNativeAd == null;
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
        if (fetchNativeAd == null && loadNativeAd == null) {
            adViewHolder.adContainer.setVisibility(View.GONE);
            return;
        }
        adViewHolder.adContainer.setVisibility(View.VISIBLE);
        AcbNativeAd nativeAd = fetchNativeAd != null ? fetchNativeAd : loadNativeAd;
        adViewHolder.acbNativeAdContainerView.fillNativeAd(nativeAd, "");
        assert nativeAd != null;
        nativeAd.setNativeClickListener(new AcbNativeAd.AcbNativeClickListener() {
            @Override
            public void onAdClick(AcbAd ad) {
                logAdClick();
                updateAd();
            }
        });
    }

    public void setCategory(String category) {
        this.category = category;
    }

    private void updateAd() {
        logAdChance();
        List<AcbNativeAd> adList = AcbNativeAdManager.getInstance().fetch(appPlacement, 1);
        if (!adList.isEmpty()) {
            if (fetchNativeAd != null) {
                fetchNativeAd.release();
            }
            logAdShow();

            fetchNativeAd = adList.get(0);
            adViewHolder.acbNativeAdContainerView.fillNativeAd(fetchNativeAd, "");

            assert fetchNativeAd != null;
            fetchNativeAd.setNativeClickListener(new AcbNativeAd.AcbNativeClickListener() {
                @Override
                public void onAdClick(AcbAd ad) {
                    logAdClick();
                    updateAd();
                }
            });
        }
    }

    @Override
    public void release() {
        if (fetchNativeAd != null) {
            fetchNativeAd.release();
            fetchNativeAd = null;
        }
        if (loadNativeAd != null) {
            loadNativeAd.release();
            loadNativeAd = null;
        }
    }

    @Override
    public void logViewedEvent() {
        if (!hasViewed) {
            logAdChance();
            if (fetchNativeAd != null || loadNativeAd != null) {
                logAdShow();
            }
            hasViewed = true;
        }
    }

    @Override
    public boolean hasViewed() {
        return hasViewed;
    }

    private void logAdChance() {
        switch (AutoPilotUtils.getLockerMode()) {
            case "cableandfuse":
                LockerCustomConfig.getLogger().logEvent("SmartLockerFeed2_NativeAd", "type", "Chance");
                break;
            case "fuse":
                LockerCustomConfig.getLogger().logEvent("SmartLockerFeed3_NativeAd", "type", "Chance");
                break;
            case "cable":
                LockerCustomConfig.getLogger().logEvent("SmartLockerFeed4_NativeAd", "type", "Chance");
                break;
        }
        LockerCustomConfig.getLogger().logEvent("ad_chance");
        AutoPilotUtils.logLockerModeAutopilotEvent("ad_chance");
    }

    private void logAdShow() {
        switch (AutoPilotUtils.getLockerMode()) {
            case "cableandfuse":
                LockerCustomConfig.getLogger().logEvent("SmartLockerFeed2_NativeAd", "type", "AdView");
                break;
            case "fuse":
                LockerCustomConfig.getLogger().logEvent("SmartLockerFeed3_NativeAd", "type", "AdView");
                break;
            case "cable":
                LockerCustomConfig.getLogger().logEvent("SmartLockerFeed4_NativeAd", "type", "AdView");
                break;
        }
        LockerCustomConfig.getLogger().logEvent("ad_show");
        AutoPilotUtils.logLockerModeAutopilotEvent("ad_show");
    }

    private void logAdClick() {
        switch (AutoPilotUtils.getLockerMode()) {
            case "cableandfuse":
                LockerCustomConfig.getLogger().logEvent("SmartLockerFeed2_NativeAd", "type", "AdClick");
                break;
            case "fuse":
                LockerCustomConfig.getLogger().logEvent("SmartLockerFeed3_NativeAd", "type", "AdClick");
                break;
            case "cable":
                LockerCustomConfig.getLogger().logEvent("SmartLockerFeed4_NativeAd", "type", "AdClick");
                break;
        }
    }
}
