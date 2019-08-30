package com.colorphone.ringtones.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.colorphone.ringtones.R;
import com.colorphone.ringtones.RingtoneApi;
import com.colorphone.ringtones.RingtoneConfig;
import com.colorphone.ringtones.RingtoneDownloadManager;
import com.colorphone.ringtones.RingtoneImageLoader;
import com.colorphone.ringtones.RingtoneManager;
import com.colorphone.ringtones.RingtonePlayManager;
import com.colorphone.ringtones.download2.Downloader;
import com.colorphone.ringtones.module.Banner;
import com.colorphone.ringtones.module.Ringtone;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.zhpan.bannerview.BannerViewPager;
import com.zhpan.bannerview.holder.HolderCreator;

import java.util.ArrayList;
import java.util.List;


/**
 * @author sundxing
 */
public class BaseRingtoneListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected static int TYPE_NORMAL = 1;
    protected static int TYPE_BANNER = 2;

    private LayoutInflater mLayoutInflater;

    protected final List<Ringtone> mDataList = new ArrayList<>();
    protected List<Banner> mBannerList = new ArrayList<>();
    protected RingtoneApi mRingtoneApi;
    protected boolean hasHeader = false;

    protected ExpandableViewHoldersUtil.KeepOneHolder<ViewHolder> mKeepOneHolder = new ExpandableViewHoldersUtil.KeepOneHolder<>();

    /**
     * To Improve view performance
     */
    private final LottieAnimationView mSharedLottieProgress;

    public BaseRingtoneListAdapter(@NonNull RingtoneApi ringtoneApi) {
        mRingtoneApi = ringtoneApi;
        mSharedLottieProgress = (LottieAnimationView) getLayoutInflater(HSApplication.getContext())
                .inflate(R.layout.stub_download_progress, null);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_NORMAL) {
            final View itemView = getLayoutInflater(parent.getContext()).inflate(R.layout.item_ringone, parent, false);
            final ViewHolder holder = new ViewHolder(itemView);
            holder.setSharedLottieProgressView(mSharedLottieProgress);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
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
                }
            });
            holder.playActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    togglePlayStatus(holder);
                }
            });

            holder.actionSetRingone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Handle by listener.
                    int pos = (int) itemView.getTag();
                    Ringtone ringtone = getRingtoneByAdapterPos(pos);
                    RingtoneManager.getInstance().onSetRingtone(ringtone);

                }
            });

            holder.actionSetRingback.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = (int) itemView.getTag();
                    Ringtone ringtone = getRingtoneByAdapterPos(pos);
                    String subscriptionUrl = RingtoneApi.getSubscriptionUrl(ringtone.getRingtoneId());
                    RingtoneConfig.getInstance().startWeb(subscriptionUrl);
                }
            });

            return holder;
        } else if (viewType == TYPE_BANNER) {
            BannerViewPager itemView = (BannerViewPager) getLayoutInflater(parent.getContext()).inflate(R.layout.item_banner, parent, false);
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
        }

        throw new IllegalStateException("Invalid type");
    }

    private void onItemUnselected(int pos, RecyclerView parent) {
        Ringtone ringtone = getRingtoneByAdapterPos(pos);
        ViewHolder viewHolder = (ViewHolder) parent.findViewHolderForAdapterPosition(pos);

        if (viewHolder != null) {
            viewHolder.playActionButton.setVisibility(View.GONE);
            viewHolder.hideProgress();
            stop(viewHolder, ringtone);
        }

        RingtoneDownloadManager.getInstance().listen(null);
    }

    protected void onItemSelected(int pos, ViewHolder holder) {
        final Ringtone ringtone = getRingtoneByAdapterPos(pos);

        boolean isDownloaded = RingtoneDownloadManager.getInstance().isDownloaded(ringtone);
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
                    holder.hideProgress();

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
            // TODO
        }
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
        }
    }

    private Ringtone getRingtoneByAdapterPos(int position) {
        if (showBanner()) {
            position = position - 1;
        }
        return mDataList.get(position);
    }

    @Override
    public int getItemCount() {
        return mDataList.size() + (showBanner() ? 1 : 0);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && showBanner()) {
            return TYPE_BANNER;
        }
        return TYPE_NORMAL;
    }

    private boolean showBanner() {
        return mBannerList.size() > 0;
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

        private View actionContainer;
        private View actionSetRingone;
        private View actionSetRingback;

        private Ringtone ringtone;

        private ImageView playActionButton;

        private LottieAnimationView lottieProgressView;
        private ViewGroup progressContainer;

        public ViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.ringtone_name);
            singer = (TextView) view.findViewById(R.id.ringtone_singer);
            cover = (ImageView) view.findViewById(R.id.cover_image);
            playTimes = view.findViewById(R.id.ringtone_times);

            actionContainer = view.findViewById(R.id.ringtone_action_container);
            actionSetRingone = view.findViewById(R.id.ringtone_action_set);
            actionSetRingback = view.findViewById(R.id.ringtone_action_set_ringback);

            Drawable drawable1 = BackgroundDrawables.createBackgroundDrawable(Color.parseColor("#FFE048"),
                    Dimensions.pxFromDp(12), true);

            Drawable drawable2 = BackgroundDrawables.createBackgroundDrawable(Color.parseColor("#E8E8E9"),
                    Dimensions.pxFromDp(12), true);

            actionSetRingone.setBackground(drawable1);
            actionSetRingback.setBackground(drawable2);

            playActionButton = view.findViewById(R.id.ringtone_play_status);
            progressContainer = view.findViewById(R.id.ringtone_download_progress);
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
            return new int[]{Dimensions.pxFromDp(72), Dimensions.pxFromDp(100)};
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
            imageLoader.loadImage(view.getContext(), data.getImgUrl(), (ImageView) view, 0);
        }
    }


}