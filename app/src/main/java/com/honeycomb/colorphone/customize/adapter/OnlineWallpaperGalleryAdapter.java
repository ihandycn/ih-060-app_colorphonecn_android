package com.honeycomb.colorphone.customize.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.colorphone.lock.util.ViewUtils;
import com.honeycomb.colorphone.Placements;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.customize.CustomizeConfig;
import com.honeycomb.colorphone.customize.WallpaperDownloadEngine;
import com.honeycomb.colorphone.customize.WallpaperInfo;
import com.honeycomb.colorphone.customize.WallpaperMgr;
import com.honeycomb.colorphone.customize.activity.CustomizeActivity;
import com.honeycomb.colorphone.customize.util.GridItemDecoration;
import com.honeycomb.colorphone.customize.view.ImagePressedTouchListener;
import com.honeycomb.colorphone.customize.view.LoadingProgressBar;
import com.honeycomb.colorphone.customize.view.TextureVideoView;
import com.honeycomb.colorphone.view.GlideApp;
import com.superapps.util.Dimensions;
import com.superapps.util.Fonts;
import com.superapps.util.Networks;

import net.appcloudbox.ads.base.AcbNativeAd;
import net.appcloudbox.ads.base.ContainerView.AcbNativeAdContainerView;
import net.appcloudbox.ads.base.ContainerView.AcbNativeAdIconView;
import net.appcloudbox.ads.base.ContainerView.AcbNativeAdPrimaryView;
import net.appcloudbox.ads.common.utils.AcbError;
import net.appcloudbox.ads.nativead.AcbNativeAdLoader;
import net.appcloudbox.ads.nativead.AcbNativeAdManager;

import java.util.List;

public class OnlineWallpaperGalleryAdapter extends AbstractOnlineWallpaperAdapter {

    private static final int WALLPAPER_NORMAL_VIEW = 0;
    private static final int WALLPAPER_LIVE_PREVIEW_VIEW = 1;
    private static final int WALLPAPER_AD_VIEW = 2;
    private static final int WALLPAPER_FOOTER_VIEW_LOAD_MORE = 3;
    private static final int WALLPAPER_FOOTER_VIEW_NO_MORE = 4;

    private static final int WALLPAPER_HEADER_HINT = 11;
    private static final int WALLPAPER_HEADER_SQUARE = 12;

    public static final String AD_TAG = "online_wallpaper_ad_tag";
    private static final int CATEGORY_TAB_COUNT_WITH_ADS = 3;
    private static final int MAX_CONCURRENT_AD_REQUEST_COUNT = 3;


    private FooterViewHolder mFooterViewHolder;

    private WallpaperDownloadEngine.OnLoadWallpaperListener mListener = new WallpaperDownloadEngine.OnLoadWallpaperListener() {
        @Override
        public void onLoadFinished(List<WallpaperInfo> wallpaperInfoList) {
            int lastSize = mDataSet.size();
            mDataSet.addAll(wallpaperInfoList);
            for (int index = lastSize; index < mDataSet.size(); index++) {
                mAdCount.add(index, mAddedAdsCount);
            }
            notifyItemRangeInserted(lastSize, wallpaperInfoList.size());
        }

        @Override
        public void onLoadFailed() {
            if (mFooterViewHolder != null) {
                mFooterViewHolder.mLoadingHint.setVisibility(View.INVISIBLE);
                mFooterViewHolder.mProgressBar.setVisibility(View.INVISIBLE);
                mFooterViewHolder.mProgressBar.stopAnimation();
                mFooterViewHolder.mRetryHint.setVisibility(View.VISIBLE);
                mFooterViewHolder.itemView.setOnClickListener(v -> {
                    loadWallpaper();
                    mFooterViewHolder.itemView.setOnClickListener(null);
                });
            }
        }
    };

    public OnlineWallpaperGalleryAdapter(Context context) {
        super();
        mContext = context;
        mScreenWidth = Dimensions.getPhoneWidth(context);

        ((CustomizeActivity) mContext).addActivityResultHandler(this);

        GridLayoutManager.SpanSizeLookup spanSizeLookup = new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch (getItemViewType(position)) {
                    case WALLPAPER_LIVE_PREVIEW_VIEW:
                    case WALLPAPER_NORMAL_VIEW:
                        return 1;
                    case WALLPAPER_AD_VIEW:
                    case WALLPAPER_FOOTER_VIEW_LOAD_MORE:
                    case WALLPAPER_FOOTER_VIEW_NO_MORE:
                    case WALLPAPER_HEADER_HINT:
                    case WALLPAPER_HEADER_SQUARE:
                        return 2;
                    default:
                        return 1;
                }
            }
        };
        mLayoutManager = new GridLayoutManager(mContext, 2);
        mLayoutManager.setSpanSizeLookup(spanSizeLookup);
        mInflater = LayoutInflater.from(mContext);

        mAdsEnabledByConfig = CustomizeConfig.getBoolean(false, "customizeNativeAds", "Wallpaper", "AdSwitch");
        if (mAdsEnabledByConfig) {
            mAdHandler = new Handler();

            // Config "customizeNativeAds", "Wallpaper", "AdStep": number of rows of wallpapers between two ads
            mWallpaperCountBetweenAds = 2 * CustomizeConfig.getInteger(3, "customizeNativeAds", "Wallpaper", "AdStep");
            mStartIndex = 2 * CustomizeConfig.getInteger(2, "customizeNativeAds", "Wallpaper", "StartIndex");
        }
    }

    @Override
    public void setScenario(WallpaperMgr.Scenario scenario) {
        mScenario = scenario;
    }
    @Override
    public void setCategoryIndex(int index) {
        mCategoryIndex = index;
    }
    @Override
    public void setCategoryName(String categoryName) {
        mCategoryName = categoryName;
    }
    @Override
    public RecyclerView.LayoutManager getLayoutManager() {
        return mLayoutManager;
    }

    @Override
    public RecyclerView.ItemDecoration getItemDecoration() {
        GridItemDecoration decoration = new GridItemDecoration(Dimensions.pxFromDp(2));
        decoration.setAdapter(this);
        return decoration;
    }

    @Override
    public WallpaperDownloadEngine.OnLoadWallpaperListener getLoadWallpaperListener() {
        return mListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case WALLPAPER_NORMAL_VIEW:
                View wallpaperImageView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.online_wallpaper_image_normal_item, parent, false);
                wallpaperImageView.setOnClickListener(this);
                NormalViewHolder holder = new NormalViewHolder(wallpaperImageView);
                wallpaperImageView.setOnTouchListener(new ImagePressedTouchListener(holder.mImageView));
                return holder;
            case WALLPAPER_LIVE_PREVIEW_VIEW:
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.online_wallpaper_image_live_preview_item, parent, false);
                itemView.setOnClickListener(this);
                LivePreviewViewHolder preVieweHolder = new LivePreviewViewHolder(itemView);
                itemView.setOnTouchListener(new ImagePressedTouchListener(preVieweHolder.mImageView));
                mLivePreviewViewholders.add(preVieweHolder);
                return preVieweHolder;
            case WALLPAPER_AD_VIEW:
                final View adView = mInflater.inflate(R.layout.online_theme_and_wallpaper_ad_item, parent, false);
                final LinearLayout container = (LinearLayout) mInflater.inflate(R.layout.online_theme_ad_container, parent, false);
                AcbNativeAdIconView icon = adView.findViewById(R.id.theme_icon);
                icon.setTargetSizePX(Dimensions.pxFromDp(38), Dimensions.pxFromDp(38));
                AdHolder viewHolder = new AdHolder(container, adView);
                AcbNativeAdPrimaryView banner = adView.findViewById(R.id.theme_banner);
                banner.setBitmapConfig(Bitmap.Config.RGB_565);
                // Width match parent
                banner.setTargetSizePX(mScreenWidth, (int) (mScreenWidth / 1.9f));
                return viewHolder;
            case WALLPAPER_FOOTER_VIEW_LOAD_MORE:
                mFooterViewHolder = new FooterViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more_auto, parent, false));
                return mFooterViewHolder;
            case WALLPAPER_FOOTER_VIEW_NO_MORE:
                View noMoreView = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.gallery_no_more_foot_item, parent, false);
                FootViewHolder footHolder = new FootViewHolder(noMoreView);
                footHolder.tvFoot.setText(R.string.online_3d_wallpaper_foot_text);
                return footHolder;

            case WALLPAPER_HEADER_SQUARE:
                View headerSquare = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.wallpaper_item_header_square, parent, false);

                HeaderSquareViewHolder squareViewHolder = new HeaderSquareViewHolder(headerSquare);
                return squareViewHolder;

            case WALLPAPER_HEADER_HINT:
                View headerHint = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.wallpaper_item_header_hint, parent, false);
                HeaderHintViewHolder hintViewHolder = new HeaderHintViewHolder(headerHint);
                return hintViewHolder;

            default:
                throw new IllegalArgumentException("Item type invalid");
        }
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        if (!(holder instanceof LivePreviewViewHolder)) {
            return;
        }

        int position = holder.getAdapterPosition();
        WallpaperInfo info = (WallpaperInfo) mDataSet.get(position);
        ((LivePreviewViewHolder) holder).mVideoView.setVisibility(View.INVISIBLE);
        ((LivePreviewViewHolder) holder).mImageView.setVisibility(View.VISIBLE);
        ((LivePreviewViewHolder) holder).mVideoView.setTag(null);

        if (!TextUtils.isEmpty(info.getVideoUrl())) {
            String videoUrl = info.getVideoUrl();
            ((LivePreviewViewHolder) holder).mVideoView.setTag(videoUrl);
            WallpaperDownloadEngine.getInstance().getPreviewFile(videoUrl, (path) -> {
                if (TextUtils.equals(videoUrl, (CharSequence) ((LivePreviewViewHolder) holder).mVideoView.getTag())) {
                    ((LivePreviewViewHolder) holder).mVideoView.setVideoPath(path);
                    ((LivePreviewViewHolder) holder).mVideoView.setVisibility(View.VISIBLE);
                    ((LivePreviewViewHolder) holder).mVideoView.setPlayListener(new TextureVideoView.PlayListener() {
                        @Override
                        public void onInfo(int what, int extra) {
                            if (TextUtils.equals(videoUrl, (CharSequence) ((LivePreviewViewHolder) holder).mVideoView.getTag())) {
                                ((LivePreviewViewHolder) holder).mImageView.setVisibility(View.INVISIBLE);
                            }
                        }

                        @Override
                        public void onError(int what, int extra) {
                        }

                        @Override
                        public void onCompletion() {
                        }

                        @Override
                        public void onSurfaceDestroyed() {
                        }
                    });
                    if (((LivePreviewViewHolder) holder).mResume) {
                        ((LivePreviewViewHolder) holder).mVideoView.play();
                    }
                }
            });
        }
    }

    protected void clipHotOnlineItemView(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case WALLPAPER_LIVE_PREVIEW_VIEW:
            case WALLPAPER_NORMAL_VIEW:
                clipHotOnlineItemView(holder, position);
                mMaxVisiblePosition = Math.max(mMaxVisiblePosition, position);
                WallpaperInfo info = (WallpaperInfo) mDataSet.get(position);
                GlideApp.with(holder.itemView.getContext()).asBitmap().load(info.getThumbnailUrl()).placeholder(R.drawable.wallpaper_loading)
                        .error(R.drawable.wallpaper_load_failed).format(DecodeFormat.PREFER_RGB_565)
                        .diskCacheStrategy(DiskCacheStrategy.DATA).into(
                        ((BaseViewHolder) holder).mImageView);
                holder.itemView.setTag(position);
                BaseViewHolder imageHolder = (BaseViewHolder) holder;

                imageHolder.setPopularity(
                        mScenario == WallpaperMgr.Scenario.ONLINE_NEW && info.getType() == WallpaperInfo.WALLPAPER_TYPE_ONLINE,
                        info.getPopularity());
                imageHolder.setHotType(info.getType());
                if (shouldShowAds(position)) {
                    loadAds();
                }
                break;
            case WALLPAPER_AD_VIEW:
                AcbNativeAd ad = (AcbNativeAd) mDataSet.get(position);
                AcbNativeAdContainerView adContainerView =
                        ((OnlineWallpaperGalleryAdapter.AdHolder) holder).mAdContentView;
                adContainerView.fillNativeAd(ad,"");
                break;
            case WALLPAPER_FOOTER_VIEW_LOAD_MORE:
                loadWallpaper();
                break;
            case WALLPAPER_FOOTER_VIEW_NO_MORE:
                break;
        }
    }

    private boolean shouldShowAds(int position) {
        boolean isHotTab = mScenario == WallpaperMgr.Scenario.ONLINE_NEW;
        boolean isCategoryTabWithAds = (0 <= mCategoryIndex && mCategoryIndex < CATEGORY_TAB_COUNT_WITH_ADS);
        return mAdsEnabledByConfig
                && mCurrentRequestCount < MAX_CONCURRENT_AD_REQUEST_COUNT
                && (isHotTab || isCategoryTabWithAds)
                && isPositionForAds(position);
    }

    private boolean isPositionForAds(int position) {
        return position > mLastAdIndex && position < mAdCount.size()
                && (position - mAdCount.get(position)) % (mWallpaperCountBetweenAds + 1) == 1
                && mAddedAds.size() < position / (2 * (mWallpaperCountBetweenAds + 1)) + 1;
    }

    public static void setWallpaperInfo(WallpaperInfo wallpaperInfo) {
        sApplyingWallpaper = wallpaperInfo;
    }

    @Override
    public int getItemCount() {
        if (mDataSet.isEmpty()) {
            return 0;
        }
        int extra = 0;
        if (mScenario != WallpaperMgr.Scenario.ONLINE_HOT) {
            extra++; // For a WALLPAPER_FOOTER_VIEW_LOAD_MORE
        }
        return mDataSet.size() + extra;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mDataSet.size()) {
            return WALLPAPER_FOOTER_VIEW_LOAD_MORE;
        } else {
            if (mDataSet.get(position) instanceof WallpaperInfo) {
                return TextUtils.isEmpty(((WallpaperInfo) mDataSet.get(position)).getVideoUrl()) ? WALLPAPER_NORMAL_VIEW : WALLPAPER_LIVE_PREVIEW_VIEW;
            } else {
                return WALLPAPER_AD_VIEW;
            }
        }
    }

    private void loadWallpaper() {
        mFooterViewHolder.mProgressBar.startLoadingAnimation();
        mFooterViewHolder.mProgressBar.setVisibility(View.VISIBLE);
        mFooterViewHolder.mLoadingHint.setVisibility(View.VISIBLE);
        mFooterViewHolder.mRetryHint.setVisibility(View.INVISIBLE);
        if (Networks.isNetworkAvailable(-1)) {
            if (mScenario == WallpaperMgr.Scenario.ONLINE_NEW) {
                WallpaperDownloadEngine.getNextNewWallpaperList(mListener);
            } else {
                WallpaperDownloadEngine.getNextCategoryWallpaperList(mCategoryIndex, mListener);
            }
        } else {
            new Handler().postDelayed(() -> mListener.onLoadFailed(), 1000);
        }
    }

    private void loadAds() {
        mAdHandler.post(() -> {
            for (int i = 0; i < mCandidateAds.size(); i++) {
                if (mCandidateAds.get(i).isExpired()) {
                    mCandidateAds.remove(i);
                    i--;
                }
            }
            if (!mCandidateAds.isEmpty()) {
                arrangeAd(true, mCandidateAds.get(0));
                return;
            }

            mCurrentRequestCount++;

            mAdLoader = AcbNativeAdManager.getInstance().createLoaderWithPlacement(Placements.WALLPAPER_NATIVE_AD_PLACEMENT_NAME);
            mAdLoader.load(1, new AcbNativeAdLoader.AcbNativeAdLoadListener() {
                @Override
                public void onAdReceived(AcbNativeAdLoader acbNativeAdLoader, List<AcbNativeAd> ads) {
                    if (ads.isEmpty()) {
                        logAppViewEvents(false);
                        return;
                    }
                    CustomizeActivity hostActivity = (CustomizeActivity) mContext;
                    if (hostActivity.isDestroying()) {
                        ads.get(0).release();
                        logAppViewEvents(false);
                        return;
                    }
                    logAppViewEvents(true);
                    AcbNativeAd ad = ads.get(0);
                    arrangeAd(false, ad);
                }

                @Override
                public void onAdFinished(AcbNativeAdLoader acbNativeAdLoader, AcbError acbError) {
                    if (--mCurrentRequestCount < 0) {
                        mCurrentRequestCount = 0;
                    }
                }
            });
        });
    }

    @SuppressWarnings("WeakerAccess")
    void logAppViewEvents(boolean adShown) {
//        AdAnalytics.logAppViewEvent(AdPlacements.WALLPAPER_NATIVE_AD_PLACEMENT_NAME, adShown);
//        LauncherAnalytics.logEvent("ThemeAndWallpaperAdAnalysis",
//                "ad_show_from", "WallpaperList_" + adShown);
    }

    private void arrangeAd(boolean shouldRemove, AcbNativeAd ad) {
        boolean added = false;
        int lastBk = mLastAdIndex;
        int position = mMaxVisiblePosition;
        int delta = position - mLastAdIndex;
        if (delta >= mWallpaperCountBetweenAds) {
            mLastAdIndex = position + 1;
        } else {
            mLastAdIndex += mWallpaperCountBetweenAds + 1;
        }
        if (mLastAdIndex < mStartIndex) {
            mLastAdIndex = mStartIndex;
        }
        if (mLastAdIndex == mDataSet.size()) {
            if ((mLastAdIndex - lastBk) % 2 == 1) {
                added = true;
            }
        } else if (mLastAdIndex < mDataSet.size()) {
            if ((mLastAdIndex - lastBk) % 2 == 0) {
                mLastAdIndex++;
            }
            added = true;
        }

        if (added) {
            mDataSet.add(mLastAdIndex, ad);
            prepareAdCount();
            notifyItemRangeChanged(mLastAdIndex, getItemCount() - mLastAdIndex);
            if (shouldRemove) {
                for (int i = 0; i < mCandidateAds.size(); i++) {
                    if (mCandidateAds.get(i) == ad) {
                        mCandidateAds.remove(i);
                        break;
                    }
                }
            }
            mAddedAds.add(ad);
        } else {
            if (!shouldRemove) {
                mCandidateAds.add(ad);
            }
        }

    }

    private void prepareAdCount() {
        mAddedAdsCount++;
        mAdCount.add(mLastAdIndex, mAddedAdsCount);
        for (int index = mLastAdIndex + 1; index < mAdCount.size(); index++) {
            mAdCount.set(index, mAddedAdsCount);
        }
    }

    public List<Integer> getAdCount() {
        return mAdCount;
    }

    private static class FooterViewHolder extends RecyclerView.ViewHolder {
        private LoadingProgressBar mProgressBar;
        private TextView mLoadingHint;
        private TextView mRetryHint;

        public FooterViewHolder(View itemView) {
            super(itemView);
            mProgressBar = ViewUtils.findViewById(itemView, R.id.progress_bar);
            mLoadingHint = ViewUtils.findViewById(itemView, R.id.loading_hint);
            mRetryHint = ViewUtils.findViewById(itemView, R.id.retry_hint);
        }
    }

    private static class AdHolder extends RecyclerView.ViewHolder {
        AcbNativeAdContainerView mAdContentView;

        AdHolder(ViewGroup container, View adView) {
            super(container);
            container.setTag(AD_TAG);

            mAdContentView = new AcbNativeAdContainerView(container.getContext());
            mAdContentView.addContentView(adView);

            AcbNativeAdPrimaryView image = ViewUtils.findViewById(adView, R.id.theme_banner);
            mAdContentView.setAdPrimaryView(image);
            AcbNativeAdIconView icon = ViewUtils.findViewById(adView, R.id.theme_icon);
            mAdContentView.setAdIconView(icon);
            TextView title = ViewUtils.findViewById(adView, R.id.theme_name);
            mAdContentView.setAdTitleView(title);
            TextView description = ViewUtils.findViewById(adView, R.id.theme_description_short);
            mAdContentView.setAdBodyView(description);
            TextView actionBtn = ViewUtils.findViewById(adView, R.id.action_btn);
            actionBtn.setTypeface(Fonts.getTypeface(Fonts.Font.CUSTOM_FONT_SEMIBOLD));
            mAdContentView.setAdActionView(actionBtn);

            FrameLayout choice = ViewUtils.findViewById(adView, R.id.ad_choice_icon);
            mAdContentView.setAdChoiceView(choice);
            container.addView(mAdContentView);
        }
    }

    private static class HeaderSquareViewHolder extends RecyclerView.ViewHolder {

        public HeaderSquareViewHolder(View itemView) {
            super(itemView);
        }
    }


    private static class HeaderHintViewHolder extends RecyclerView.ViewHolder {

        public HeaderHintViewHolder(View itemView) {
            super(itemView);
        }
    }
}
