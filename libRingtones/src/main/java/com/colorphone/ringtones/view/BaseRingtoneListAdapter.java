package com.colorphone.ringtones.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.colorphone.ringtones.MusicPlayer;
import com.colorphone.ringtones.R;
import com.colorphone.ringtones.RingtoneApi;
import com.colorphone.ringtones.RingtoneConfig;
import com.colorphone.ringtones.RingtoneDownloadManager;
import com.colorphone.ringtones.RingtoneImageLoader;
import com.colorphone.ringtones.RingtoneManager;
import com.colorphone.ringtones.RingtonePlayManager;
import com.colorphone.ringtones.download2.Downloader;
import com.colorphone.ringtones.module.Banner;
import com.colorphone.ringtones.module.Column;
import com.colorphone.ringtones.module.Ringtone;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Toasts;
import com.zhpan.bannerview.BannerViewPager;
import com.zhpan.bannerview.holder.HolderCreator;

import java.util.ArrayList;
import java.util.List;


/**
 * @author sundxing
 */
public abstract class BaseRingtoneListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements RingtonePlayManager.Callback {

    private static final String TAG = BaseRingtoneListAdapter.class.getSimpleName();
    protected static int TYPE_NORMAL = 1;
    protected static int TYPE_BANNER = 2;
    protected static int TYPE_FOOTER = 3;
    private Application mApplication;

    private LayoutInflater mLayoutInflater;
    final protected Context mContext;

    protected final List<Ringtone> mDataList = new ArrayList<>();
    protected List<Banner> mBannerList = new ArrayList<>();
    protected RingtoneApi mRingtoneApi;
    protected boolean hasHeader = false;
    private boolean isLoading = false;

    protected ExpandableViewHoldersUtil.KeepOneHolder<ViewHolder> mKeepOneHolder = new ExpandableViewHoldersUtil.KeepOneHolder<>();

    /**
     * To Improve view performance
     */
    private final LottieAnimationView mSharedLottieProgress;
    private int mTotalSize;
    private boolean mEnableTop3Badge;
    private Column mColumn;
    private RingtoneManager.RingtoneSetHandler mRingtoneSetHandler;
    private RecyclerView mRecyclerView;

    public BaseRingtoneListAdapter(@NonNull Context context, @NonNull RingtoneApi ringtoneApi) {
        mRingtoneApi = ringtoneApi;
        mContext = context;
        mSharedLottieProgress = (LottieAnimationView) getLayoutInflater(context)
                .inflate(R.layout.stub_download_progress, null);

        Context appContext = HSApplication.getContext().getApplicationContext();
        if (appContext instanceof Application) {
            mApplication = (Application) appContext;
            mApplication.registerActivityLifecycleCallbacks(sActivityLifecycleCallbacks);
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        super.onAttachedToRecyclerView(recyclerView);
        RingtonePlayManager.getInstance().registerCallback(this);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        RingtonePlayManager.getInstance().pause();
        RingtonePlayManager.getInstance().unregisterCallback(this);
        mRecyclerView = null;
    }

    public void setSizeTotalCount(int total) {
        mTotalSize = total;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_NORMAL) {
            final View itemView = getLayoutInflater(mContext).inflate(R.layout.item_ringone, parent, false);
            final ViewHolder holder = new ViewHolder(itemView);
            holder.setSharedLottieProgressView(mSharedLottieProgress);

            itemView.setOnClickListener(view -> {
                int pos = (int) itemView.getTag();
                int lastExpandPos = mKeepOneHolder.getExpandedPos();
                if (lastExpandPos >= 0) {
                    onItemUnselected(lastExpandPos, ((RecyclerView) holder.itemView.getParent()));
                }

                if (lastExpandPos != pos) {
                    onItemSelected(pos, holder);
                }
                // View animation
                mKeepOneHolder.toggle(holder);
            });
            holder.playActionButton.setOnClickListener(view -> togglePlayStatus(holder));

            holder.actionSetRingtone.setOnClickListener(view -> {

                // Handle by listener.
                int pos = (int) itemView.getTag();
                Ringtone ringtone = getRingtoneByAdapterPos(pos);
                boolean isDownloaded = RingtoneDownloadManager.getInstance().isDownloaded(ringtone);

                RingtoneConfig.getInstance().getRemoteLogger().logEvent("Ringtone_Set_Click",
                        "Name", ringtone.getTitle(),
                        "DownloadOK", isDownloaded ? "YES" : "NO",
                        "Type:", ringtone.getColumnSource());

                if (RingtoneConfig.getInstance().getRingtoneSetter().onSetRingtone(ringtone)) {
                    if (!isDownloaded) {
                        Toasts.showToast(R.string.ringtone_download_fail_check);
                        return;
                    }
                    if (mRingtoneSetHandler != null) {
                        mRingtoneSetHandler.onSetRingtone(ringtone);
                    }
                } else {
                    HSLog.d(TAG, "SetRingtone no Permission");
                }
            });

            holder.actionSetRingback.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = (int) itemView.getTag();
                    Ringtone ringtone = getRingtoneByAdapterPos(pos);
                    boolean isDownloaded = RingtoneDownloadManager.getInstance().isDownloaded(ringtone);

                    RingtoneConfig.getInstance().getRemoteLogger().logEvent("RingBackTone_Set_Click",
                            "Name", ringtone.getTitle(),
                            "DownloadOK", isDownloaded ? "YES" : "NO",
                            "Type:", ringtone.getColumnSource());

                    if (!isDownloaded) {
                        Toasts.showToast(R.string.ringtone_download_fail_network);
                        return;
                    }
                    String subscriptionUrl = RingtoneApi.getSubscriptionUrl(ringtone.getRingtoneId());
                    RingtoneConfig.getInstance().startWeb(subscriptionUrl);
                }
            });

            return holder;
        } else if (viewType == TYPE_BANNER) {
            BannerViewPager itemView = (BannerViewPager) getLayoutInflater(mContext).inflate(R.layout.item_ringtone_banner, parent, false);
            ViewGroup.LayoutParams lp = itemView.getLayoutParams();
            lp.height = (int) ((Dimensions.getPhoneWidth(mContext) - Dimensions.pxFromDp(32)) * 0.4f);
            itemView.setLayoutParams(lp);

            itemView.setRoundCorner(Dimensions.pxFromDp(4))
                    .showIndicator(true)
                    .setHolderCreator(new HolderCreator() {
                        @Override
                        public com.zhpan.bannerview.holder.ViewHolder createViewHolder() {
                            return new BannerViewHolder();
                        }
                    })
                    .setOnPageClickListener(new BannerViewPager.OnPageClickListener() {
                        @Override
                        public void onPageClick(int position) {
                            onClickBannerItem(position);
                        }
                    });
            HeaderViewHolder holder = new HeaderViewHolder(itemView);
            return holder;
        } else if (viewType == TYPE_FOOTER) {
            TextView itemView = (TextView) getLayoutInflater(mContext). inflate(
                    R.layout.item_ringtone_footer, parent, false);
            return new FooterViewHolder(itemView);
        }

        throw new IllegalStateException("Invalid type");
    }

    private void onItemUnselected(int pos, RecyclerView parent) {
        Ringtone ringtone = getRingtoneByAdapterPos(pos);
        ViewHolder viewHolder = (ViewHolder) parent.findViewHolderForAdapterPosition(pos);

        if (viewHolder != null) {
            viewHolder.playActionButton.setVisibility(View.GONE);
            viewHolder.hideProgress();
            viewHolder.toggleBadgeSize(false);
            stop(viewHolder, ringtone);
        }

        RingtoneDownloadManager.getInstance().listen(null);
    }

    protected void onItemSelected(int pos, ViewHolder holder) {
        final Ringtone ringtone = getRingtoneByAdapterPos(pos);
        holder.toggleBadgeSize(true);

        boolean isDownloaded = RingtoneDownloadManager.getInstance().isDownloaded(ringtone);
        RingtoneConfig.getInstance().getRemoteLogger().logEvent("Ringtone_Audition_Click",
                "Name", ringtone.getTitle(),
                "DownloadOk", isDownloaded ? "Yes" : "No",
                "Type:", ringtone.getColumnSource());
        if (isDownloaded) {
            play(holder, ringtone);
        } else {
            holder.showProgress();
            RingtoneDownloadManager.RingtoneDownloadListener downloadListener = new RingtoneDownloadManager.RingtoneDownloadListener() {

                @Override
                public void onStart(Downloader.DownloadItem item) {
                }

                @Override
                public void onProgress(Downloader.DownloadItem item, float progress) {

                }

                @Override
                public void onComplete(Downloader.DownloadItem item) {
                    holder.hideProgress();
                    play(holder, ringtone);
                }

                @Override
                public void onCancel(Downloader.DownloadItem item, CancelReason reason) {
                    holder.hideProgress();
                }

                @Override
                public void onFailed(Downloader.DownloadItem item, String errorMsg) {
                    Toasts.showToast(R.string.ringtone_download_fail_network);
                }
            };
            boolean isDownloading = RingtoneDownloadManager.getInstance().isDownloading(ringtone);
            if (isDownloading) {
                // bind progress listener
                RingtoneDownloadManager.getInstance().listen(downloadListener);
            } else {
                // Start download
                RingtoneDownloadManager.getInstance().download(ringtone, pos);
                RingtoneDownloadManager.getInstance().listen(downloadListener);
            }
        }
    }

    private void togglePlayStatus(ViewHolder viewHolder) {
        Ringtone ringtone = viewHolder.getRingtone();
        if (ringtone == null) {
            return;
        }
        if (ringtone.isPlaying()) {
            stop(viewHolder, ringtone, false);
        } else {
            play(viewHolder, ringtone);
        }
    }

    private void stop(ViewHolder viewHolder, Ringtone ringtone) {
        stop(viewHolder, ringtone, true);
    }

    private void stop(ViewHolder viewHolder, Ringtone ringtone, boolean hideActionView) {
        if (ringtone.isPlaying()) {
            viewHolder.onStopMusic(hideActionView);
            ringtone.setPlaying(false);
            RingtonePlayManager.getInstance().dispatch(ringtone, "stop");
        }
    }

    private void play(ViewHolder viewHolder, Ringtone ringtone) {
        if (!ringtone.isPlaying()) {
            viewHolder.onPlayMusic();
            ringtone.setPlaying(true);
            RingtonePlayManager.getInstance().dispatch(ringtone, "play");
        }
    }

    protected void onClickBannerItem(int pos) {
        Banner banner = mBannerList.get(pos);
        if (banner.isAdBannner()) {
            // Webview
            RingtoneConfig.getInstance().startWeb(banner.getLinkUrl());
        } else {
            // Ringtone list
            BannerListActivity.start(mContext, banner);
        }
        RingtoneConfig.getInstance().getRemoteLogger().logEvent("Ringtone_Banner_Click");
    }

    private LayoutInflater getLayoutInflater(Context context) {
        if (mLayoutInflater == null) {
            mLayoutInflater = LayoutInflater.from(context);
        }
        return mLayoutInflater;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            BannerViewPager<Banner,BannerViewHolder> bannerViewPager = (BannerViewPager<Banner, BannerViewHolder>) holder.itemView;
            bannerViewPager.setData(mBannerList);
            bannerViewPager.create();
        } else if (holder instanceof ViewHolder){
            ViewHolder viewHolder = (ViewHolder) holder;
            holder.itemView.setTag(position);
            Ringtone ringtone = getRingtoneByAdapterPos(position);
            viewHolder.title.setText(ringtone.getTitle());
            viewHolder.singer.setText(ringtone.getSinger());
            viewHolder.playTimes.setText(ringtone.getPlayTimes());
            viewHolder.bindRingtone(ringtone);
            RingtoneImageLoader imageLoader = RingtoneConfig.getInstance().getRingtoneImageLoader();
            imageLoader.loadImage(viewHolder.cover.getContext(), ringtone.getImgUrl(), viewHolder.cover, R.drawable.ringtone_item_cover_default);
            mKeepOneHolder.bind(viewHolder,position);

            boolean isExpanded = mKeepOneHolder.isExpanded(viewHolder);
            if (mKeepOneHolder.isExpanded(viewHolder)) {
                // Check play status
                if (RingtonePlayManager.getInstance().isPlaying()) {
                    viewHolder.onPlayMusic();
                } else {
                    viewHolder.onStopMusic(false);
                }
            } else {
                viewHolder.playActionButton.setVisibility(View.GONE);
                viewHolder.hideProgress();
            }

            if (mEnableTop3Badge) {
                viewHolder.setBadge(toDataPos(position), isExpanded);
            } else {
                viewHolder.badge.setVisibility(View.GONE);
            }

        } else if (holder instanceof FooterViewHolder) {
            TextView textView = (TextView) holder.itemView;
            if (mDataList.size() < mRingtoneApi.getPageSize()) {
                // No need show
                textView.setText("");
                return;
            }
            boolean hasMore = mDataList.size() < mTotalSize;
            textView.setText(hasMore ? "努力加载中..." : "没有更多啦");
            if (hasMore) {
                if (!isLoading) {
                    setLoading(true);
                    loadMore(mDataList.size() / mRingtoneApi.getPageSize());
                }
            }
        }
    }

    protected abstract void loadMore(int pageIndex);

    protected abstract void refresh();

    public void setLoading(boolean loading) {
        isLoading = loading;
    }

    public boolean isLoading() {
        return isLoading;
    }

    private Ringtone getRingtoneByAdapterPos(int position) {
        return mDataList.get(toDataPos(position));
    }

    private int toDataPos(int adapterPos) {
        if (showBanner()) {
            adapterPos = adapterPos - 1;
        }
        return adapterPos;
    }

    private int toAdapterPos(int dataPos) {
        if (showBanner()) {
            return dataPos + 1;
        }
        return dataPos;
    }

    @Override
    public int getItemCount() {
        // Add load more footer
        return getDataItemCount() + 1;
    }

    private int getDataItemCount() {
        return mDataList.size() + (showBanner() ? 1 : 0);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && showBanner()) {
            return TYPE_BANNER;
        } else if (position == getDataItemCount()) {
            // Footer
            return TYPE_FOOTER;
        }
        return TYPE_NORMAL;
    }

    private boolean showBanner() {
        return mBannerList.size() > 0;
    }

    @Override
    public void onPlayStateChanged(int state, Ringtone song) {
        boolean notPlaying = state >= MusicPlayer.STATE_PAUSED;

        if (notPlaying && song.isPlaying()) {
            HSLog.d(TAG, song.getTitle() + " playStateChanged:" + state);
            song.setPlaying(false);
            int dataIndex = mDataList.indexOf(song);
            int viewItemIndex = toAdapterPos(dataIndex);

            mKeepOneHolder.clearAllAnimation();

            RecyclerView.ViewHolder viewHolder = mRecyclerView.findViewHolderForAdapterPosition(viewItemIndex);
            if (viewHolder instanceof ViewHolder) {
                ((ViewHolder) viewHolder).onStopMusic(false);
            }
        }
    }

    @Override
    public void onShutdown() {

    }

    public void setEnableTop3Badge(boolean enableTop3Badge) {
        mEnableTop3Badge = enableTop3Badge;
    }

    public boolean getEnableTop3Badge() {
        return mEnableTop3Badge;
    }

    public void setRingtoneSetHandler(RingtoneManager.RingtoneSetHandler ringtoneSetHandler) {
        mRingtoneSetHandler = ringtoneSetHandler;
    }

    public static class FooterViewHolder extends RecyclerView.ViewHolder {
        public FooterViewHolder(View itemView) {
            super(itemView);
        }
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {

        public HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements ExpandableViewHoldersUtil.Expandable {
        private TextView title;
        private TextView singer;
        private TextView playTimes;
        private ImageView cover;
        private TextView badge;

        private ValueAnimator mAnimator;

        private View actionContainer;
        private View actionSetRingtone;
        private View actionSetRingback;

        private Ringtone ringtone;

        private ImageView playActionButton;

        private LottieAnimationView lottieProgressView;
        private ViewGroup progressContainer;

        private int mHeightNormal;
        private int mHeightExpand;
        /**
         * {width, height, textSize(sp), left-top radius}
         */
        private static int[] badgeSmallSize = new int[] {18, 13, 8, 4};
        private static int[] badgeLargeSize = new int[] {28, 20, 14, 4};

        public ViewHolder(View view) {
            super(view);
            mHeightNormal = view.getResources().getDimensionPixelOffset(R.dimen.ringtone_item_height_normal);
            mHeightExpand = view.getResources().getDimensionPixelOffset(R.dimen.ringtone_item_height_expand);

            title = (TextView) view.findViewById(R.id.ringtone_name);
            singer = (TextView) view.findViewById(R.id.ringtone_singer);
            cover = (ImageView) view.findViewById(R.id.cover_image);
            playTimes = view.findViewById(R.id.ringtone_times);
            badge = view.findViewById(R.id.ringtone_top_badge);

            actionContainer = view.findViewById(R.id.ringtone_action_container);
            actionSetRingtone = view.findViewById(R.id.ringtone_action_set);
            actionSetRingback = view.findViewById(R.id.ringtone_action_set_ringback);


            Drawable drawable1 = BackgroundDrawables.createBackgroundDrawable(Color.parseColor(
                    HSConfig.optBoolean(false, "Application", "Ringtone", "IsRingtoneBtnYellow") ? "#FFE048" : "#E8E8E9"),
                    Dimensions.pxFromDp(12), true);

            Drawable drawable2 = BackgroundDrawables.createBackgroundDrawable(Color.parseColor(
                    HSConfig.optBoolean(false, "Application", "Ringtone", "IsRingbacktoneBtnYellow") ? "#FFE048" : "#E8E8E9"),
                    Dimensions.pxFromDp(12), true);

            actionSetRingtone.setBackground(drawable1);
            actionSetRingback.setBackground(drawable2);

            playActionButton = view.findViewById(R.id.ringtone_play_status);
            progressContainer = view.findViewById(R.id.ringtone_download_progress);

            itemView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View view) {

                }

                @Override
                public void onViewDetachedFromWindow(View view) {
                    if (ringtone != null) {
                        ringtone.setPlaying(false);
                        RingtonePlayManager.getInstance().pause(ringtone);
                    }
                    if (mAnimator != null) {
                        mAnimator.cancel();
                        mAnimator.removeAllListeners();
                        mAnimator.removeAllUpdateListeners();
                    }
                    itemView.removeOnAttachStateChangeListener(this);
                }
            });

        }

        public void setBadge(int topIndex, boolean expanded) {
            if (topIndex > 2) {
                badge.setVisibility(View.GONE);
                return;
            }
            badge.setVisibility(View.VISIBLE);

            int[] sizeArray = expanded ? badgeLargeSize : badgeSmallSize;
            ViewGroup.LayoutParams lp = badge.getLayoutParams();
            lp.width = Dimensions.pxFromDp(sizeArray[0]);
            lp.height = Dimensions.pxFromDp(sizeArray[1]);
            badge.setLayoutParams(lp);

            int bgColor = 0;
            switch (topIndex) {
                case 0:
                    bgColor = Color.parseColor("#D43D3D");
                    badge.setText("#1");
                    badge.setTextColor(Color.WHITE);
                    break;
                case 1:
                    bgColor = Color.parseColor("#C9C6DE");
                    badge.setText("#2");
                    badge.setTextColor(Color.BLACK);
                    break;
                case 2:
                    bgColor = Color.parseColor("#FFB55F");
                    badge.setText("#3");
                    badge.setTextColor(Color.BLACK);

                    break;
                default:
                    break;

            }

            Drawable drawable = BackgroundDrawables.createBackgroundDrawable(
                    bgColor,
                    0,
                    Dimensions.pxFromDp(4), 0,0,0,
                    false, false);

            badge.setBackgroundDrawable(drawable);

        }

        public void toggleBadgeSize(boolean expand) {
            if (badge.getVisibility() == View.VISIBLE) {

                final int oW = expand ? badgeSmallSize[0] : badgeLargeSize[0];
                final int oH = expand ? badgeSmallSize[1] : badgeLargeSize[1];
                final int oSize = expand ? badgeSmallSize[2] : badgeLargeSize[2];
                int wStepValue = badgeLargeSize[0] - badgeSmallSize[0];
                int hStepValue = badgeLargeSize[1] - badgeSmallSize[1];
                int sizeStepValue = badgeLargeSize[2] - badgeSmallSize[2];
                int wStep = expand ? wStepValue : -wStepValue;
                int hStep = expand ? hStepValue : -hStepValue;
                int sizeStep = expand ? sizeStepValue : -sizeStepValue;
                ViewGroup.LayoutParams lp = badge.getLayoutParams();

                if (mAnimator == null) {
                    mAnimator = ValueAnimator.ofFloat(0, 1);
                } else {
                    mAnimator.removeAllUpdateListeners();
                    mAnimator.removeAllListeners();
                }

                mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        float fraction = valueAnimator.getAnimatedFraction();
                        lp.height = Dimensions.pxFromDp(oH + hStep * fraction);
                        lp.width = Dimensions.pxFromDp(oW + wStep * fraction);
                        badge.setTextSize(TypedValue.COMPLEX_UNIT_SP, oSize + sizeStep * fraction);
                        badge.setLayoutParams(lp);
                    }
                });

                mAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        lp.height = Dimensions.pxFromDp(oH + hStep);
                        lp.width = Dimensions.pxFromDp(oW + wStep);
                        badge.setTextSize(TypedValue.COMPLEX_UNIT_SP, oSize + sizeStep);
                        badge.setLayoutParams(lp);
                    }
                });
                mAnimator.start();
            }

        }

        public void setSharedLottieProgressView(LottieAnimationView lottieProgressView) {
            this.lottieProgressView = lottieProgressView;
        }

        public Ringtone getRingtone() {
            return ringtone;
        }

        @Override
        public View getExpandView() {
            return actionContainer;
        }

        @Override
        public int[] getItemForcedHeight() {
            return new int[]{mHeightNormal, mHeightExpand};
        }

        public void bindRingtone(Ringtone ringtone) {
            this.ringtone = ringtone;
        }

        public void hideProgress() {
            if (lottieProgressView != null) {
                progressContainer.removeAllViews();
            }
        }

        public void showProgress() {
            if (lottieProgressView != null) {
                if (lottieProgressView.getParent() == null) {
                    progressContainer.addView(lottieProgressView,
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                }
                lottieProgressView.setVisibility(View.VISIBLE);
                lottieProgressView.playAnimation();
            }
        }

        public void onStopMusic(boolean hideView) {
            if (hideView) {
                playActionButton.setVisibility(View.GONE);
            }
            playActionButton.setImageResource(R.drawable.ringtone_control_play);
        }

        public void onPlayMusic() {
            playActionButton.setVisibility(View.VISIBLE);
            playActionButton.setImageResource(R.drawable.ringtone_control_pause);
        }
    }

    public static class BannerViewHolder implements com.zhpan.bannerview.holder.ViewHolder<Banner> {

        @Override
        public View createView(ViewGroup viewGroup, Context context, int position) {
            ImageView imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            return imageView;
        }

        @Override
        public void onBind(View view, Banner data, int position, int size) {
            RingtoneImageLoader imageLoader = RingtoneConfig.getInstance().getRingtoneImageLoader();
            imageLoader.loadImage(view.getContext(), data.getImgUrl(), (ImageView) view, R.drawable.ringtone_banner_bg);
        }
    }


    public Application.ActivityLifecycleCallbacks sActivityLifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {


        @Override
        public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {

        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {

        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {

        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {

        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {
            if (mContext == activity) {
                BaseRingtoneListAdapter.this.onActivityPaused();
            }
        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
            if (activity == mContext) {
                mApplication.unregisterActivityLifecycleCallbacks(this);
            }
        }
    };

    private void onActivityPaused() {
        for (Ringtone ringtone : mDataList) {
            if (ringtone.isPlaying()) {
                RingtonePlayManager.getInstance().pause();
                break;
            }
        }
    }
}