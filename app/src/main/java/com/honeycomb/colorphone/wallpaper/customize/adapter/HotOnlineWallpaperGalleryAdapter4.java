package com.honeycomb.colorphone.wallpaper.customize.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.honeycomb.colorphone.view.GlideApp;
 import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.view.ImagePressedTouchListener;
import com.honeycomb.colorphone.wallpaper.customize.WallpaperDownloadEngine;
import com.honeycomb.colorphone.wallpaper.customize.WallpaperInfo;
import com.honeycomb.colorphone.wallpaper.customize.WallpaperMgr;
import com.honeycomb.colorphone.wallpaper.view.TextureVideoView;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.superapps.util.Dimensions;

import java.util.List;

public class HotOnlineWallpaperGalleryAdapter4 extends AbstractOnlineWallpaperAdapter {

    private static final int WALLPAPER_HOT_ONLINE = 5;

    private LinearLayoutManager mLinearLayoutManager;

    private WallpaperDownloadEngine.OnLoadWallpaperListener mListener = new WallpaperDownloadEngine.OnLoadWallpaperListener() {
        @Override
        public void onLoadFinished(List<WallpaperInfo> wallpaperInfoList) {
            int lastSize = mDataSet.size();
            mDataSet.addAll(wallpaperInfoList);
            for (int index = lastSize; index < mDataSet.size(); index++) {
                mAdCount.add(index, mAddedAdsCount);
            }
            notifyDataSetChanged();
        }

        @Override
        public void onLoadFailed() {
        }
    };

    protected int mBigItemWidth;
    protected int mBigItemHeight;
    protected int mSmallItemWidth;
    protected int mSmallItemHeight;
    protected int mNormalMargin;

    public HotOnlineWallpaperGalleryAdapter4(Context context) {
        super();
        mContext = context;
        mScreenWidth = Dimensions.getPhoneWidth(context);

        mBigItemWidth = (int) (mScreenWidth * 0.6657f + 0.5f);
        mBigItemHeight = (int) (mBigItemWidth * 0.8968f + 0.5f);
        mSmallItemWidth = mScreenWidth - mBigItemWidth - Dimensions.pxFromDp(1f);
        mSmallItemHeight = (mBigItemHeight - Dimensions.pxFromDp(1f)) / 2;
        mNormalMargin = Dimensions.pxFromDp(1f);

        mLinearLayoutManager = new LinearLayoutManager(context);
        mInflater = LayoutInflater.from(mContext);

        HSGlobalNotificationCenter.addObserver(KEY_ACTIVITY_RESULT, this);
    }

    @Override
    public RecyclerView.LayoutManager getLayoutManager() {
        return mLinearLayoutManager;
    }

    @Override
    public RecyclerView.ItemDecoration getItemDecoration() {
        return new CustomItemDecoration();
    }

    public WallpaperDownloadEngine.OnLoadWallpaperListener getLoadWallpaperListener() {
        return mListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View hotOnline = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.wallpaper_hot_online, parent, false);

        HotOnlineViewHolder hotOnlineViewHolder = new HotOnlineViewHolder(hotOnline);
        hotOnlineViewHolder.setUpClickListener(this);
        return hotOnlineViewHolder;
    }

    void showPreview(LivePreviewViewHolder holder, int position) {
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

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        /*if (!(holder instanceof LivePreviewViewHolder)) {
            return;
        }*/

        int position = holder.getAdapterPosition();
        HotOnlineViewHolder viewHolder = (HotOnlineViewHolder) holder;
        if (position * 3 < mDataSet.size()) {
            boolean isNormal = TextUtils.isEmpty(((WallpaperInfo) mDataSet.get(position * 3)).getVideoUrl());
            if (!isNormal) {
                showPreview(viewHolder.mHolder1_live, position * 3);
            }
        }

        if (position * 3 + 1 < mDataSet.size()) {
            boolean isNormal = TextUtils.isEmpty(((WallpaperInfo) mDataSet.get(position * 3 + 1)).getVideoUrl());
            if (!isNormal) {
                showPreview(viewHolder.mHolder2_live, position * 3 + 1);
            }
        }

        if (position * 3 + 2 < mDataSet.size()) {
            boolean isNormal = TextUtils.isEmpty(((WallpaperInfo) mDataSet.get(position * 3 + 2)).getVideoUrl());
            if (!isNormal) {
                showPreview(viewHolder.mHolder3_live, position * 3 + 2);
            }
        }

    }

    private void load(RecyclerView.ViewHolder holder, int position) {
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
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case WALLPAPER_HOT_ONLINE:
                HotOnlineViewHolder viewHolder = (HotOnlineViewHolder) holder;
                updateItemLayout(viewHolder, position);
                if (position * 3 < mDataSet.size()) {
                    boolean isNormal = TextUtils.isEmpty(((WallpaperInfo) mDataSet.get(position * 3)).getVideoUrl());
                    if (isNormal) {
                        viewHolder.mHolder1_normal.itemView.setVisibility(View.VISIBLE);
                        viewHolder.mHolder1_live.itemView.setVisibility(View.GONE);
                        load(viewHolder.mHolder1_normal, position * 3);

                    } else {
                        viewHolder.mHolder1_normal.itemView.setVisibility(View.GONE);
                        viewHolder.mHolder1_live.itemView.setVisibility(View.VISIBLE);
                        load(viewHolder.mHolder1_live, position * 3);
                        mLivePreviewViewholders.add((LivePreviewViewHolder) viewHolder.mHolder1_live);
                    }
                }

                if (position * 3 + 1 < mDataSet.size()) {
                    boolean isNormal = TextUtils.isEmpty(((WallpaperInfo) mDataSet.get(position * 3 + 1)).getVideoUrl());
                    if (isNormal) {
                        viewHolder.mHolder2_normal.itemView.setVisibility(View.VISIBLE);
                        viewHolder.mHolder2_live.itemView.setVisibility(View.GONE);
                        load(viewHolder.mHolder2_normal, position * 3 + 1);

                    } else {
                        viewHolder.mHolder2_normal.itemView.setVisibility(View.GONE);
                        viewHolder.mHolder2_live.itemView.setVisibility(View.VISIBLE);
                        load(viewHolder.mHolder2_live, position * 3 + 1);
                        mLivePreviewViewholders.add((LivePreviewViewHolder) viewHolder.mHolder2_live);
                    }
                } else {
                    viewHolder.mHolder2_normal.itemView.setVisibility(View.GONE);
                    viewHolder.mHolder2_live.itemView.setVisibility(View.GONE);
                }
                if (position * 3 + 2 < mDataSet.size()) {
                    boolean isNormal = TextUtils.isEmpty(((WallpaperInfo) mDataSet.get(position * 3 + 2)).getVideoUrl());
                    if (isNormal) {
                        viewHolder.mHolder3_normal.itemView.setVisibility(View.VISIBLE);
                        viewHolder.mHolder3_live.itemView.setVisibility(View.GONE);
                        load(viewHolder.mHolder3_normal, position * 3 + 2);

                    } else {
                        viewHolder.mHolder3_normal.itemView.setVisibility(View.GONE);
                        viewHolder.mHolder3_live.itemView.setVisibility(View.VISIBLE);
                        load(viewHolder.mHolder3_live, position * 3 + 2);
                        mLivePreviewViewholders.add((LivePreviewViewHolder) viewHolder.mHolder3_live);
                    }
                } else {
                    viewHolder.mHolder3_normal.itemView.setVisibility(View.GONE);
                    viewHolder.mHolder3_live.itemView.setVisibility(View.GONE);
                }
                break;
        }
    }

    protected void updateItemLayout(HotOnlineViewHolder holder, int position) {
        if (position % 2 == 0) {
            // Apply 1 2 layout.
            RelativeLayout.LayoutParams l1 = (RelativeLayout.LayoutParams) holder.mV1.getLayoutParams();
            if (l1 == null) {
                l1 = new RelativeLayout.LayoutParams(mBigItemWidth, mBigItemHeight);
            } else {
                l1.width = mBigItemWidth;
                l1.height = mBigItemHeight;
            }
            l1.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            l1.addRule(RelativeLayout.ALIGN_PARENT_TOP);

            RelativeLayout.LayoutParams l2 = (RelativeLayout.LayoutParams) holder.mV2.getLayoutParams();
            if (l2 == null) {
                l2 = new RelativeLayout.LayoutParams(mSmallItemWidth, mSmallItemHeight);
            } else {
                l2.width = mSmallItemWidth;
                l2.height = mSmallItemHeight;
            }
            l2.topMargin = 0;
            l2.leftMargin = mNormalMargin;
            l2.addRule(RelativeLayout.BELOW, 0);
            l2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
            l2.addRule(RelativeLayout.RIGHT_OF, holder.mV1.getId());
            l2.addRule(RelativeLayout.ALIGN_PARENT_TOP);

            RelativeLayout.LayoutParams l3 = (RelativeLayout.LayoutParams) holder.mV3.getLayoutParams();
            if (l3 == null) {
                l3 = new RelativeLayout.LayoutParams(mSmallItemWidth, mBigItemHeight - mNormalMargin - mSmallItemHeight);
            } else {
                l3.width = mSmallItemWidth;
                l3.height = mBigItemHeight - mNormalMargin - mSmallItemHeight;
            }
            l3.rightMargin = 0;
            l3.leftMargin = mNormalMargin;
            l3.topMargin = mNormalMargin;
            l3.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            l3.addRule(RelativeLayout.RIGHT_OF, holder.mV1.getId());
            l3.addRule(RelativeLayout.BELOW, holder.mV2.getId());
            l3.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        } else {
            // Apply 2 1 layout.
            RelativeLayout.LayoutParams l1 = (RelativeLayout.LayoutParams) holder.mV1.getLayoutParams();
            if (l1 == null) {
                l1 = new RelativeLayout.LayoutParams(mSmallItemWidth, mSmallItemHeight);
            } else {
                l1.width = mSmallItemWidth;
                l1.height = mSmallItemHeight;
            }
            l1.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            l1.addRule(RelativeLayout.ALIGN_PARENT_TOP);

            RelativeLayout.LayoutParams l2 = (RelativeLayout.LayoutParams) holder.mV2.getLayoutParams();
            if (l2 == null) {
                l2 = new RelativeLayout.LayoutParams(mSmallItemWidth, mBigItemHeight - mNormalMargin - mSmallItemHeight);
            } else {
                l2.width = mSmallItemWidth;
                l2.height = mBigItemHeight - mNormalMargin - mSmallItemHeight;
            }
            l2.leftMargin = 0;
            l2.topMargin = mNormalMargin;
            l2.addRule(RelativeLayout.RIGHT_OF, 0); // first remove rule
            l2.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0); // first remove rule
            l2.addRule(RelativeLayout.BELOW, holder.mV1.getId());
            l2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

            RelativeLayout.LayoutParams l3 = (RelativeLayout.LayoutParams) holder.mV3.getLayoutParams();
            if (l3 == null) {
                l3 = new RelativeLayout.LayoutParams(mBigItemWidth, mBigItemHeight);
            } else {
                l3.width = mBigItemWidth;
                l3.height = mBigItemHeight;
            }
            l3.topMargin = 0;
            l3.leftMargin = mNormalMargin;
            l3.addRule(RelativeLayout.BELOW, 0);
            l3.addRule(RelativeLayout.RIGHT_OF, holder.mV1.getId());
            l3.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        }

        holder.itemView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
        holder.itemView.getLayoutParams().height = mBigItemHeight;
        holder.mV1.requestLayout();
        holder.mV2.requestLayout();
        holder.mV3.requestLayout();
        holder.itemView.requestLayout();
    }

    public static void setWallpaperInfo(WallpaperInfo wallpaperInfo) {
        sApplyingWallpaper = wallpaperInfo;
    }

    @Override
    public int getItemCount() {
        if (mDataSet.isEmpty()) {
            return 0;
        }
        return (mDataSet.size() - 1) / 3 + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return WALLPAPER_HOT_ONLINE;
    }

    protected static class HotOnlineViewHolder extends RecyclerView.ViewHolder {

        NormalViewHolder mHolder1_normal;
        LivePreviewViewHolder mHolder1_live;
        NormalViewHolder mHolder2_normal;
        LivePreviewViewHolder mHolder2_live;
        NormalViewHolder mHolder3_normal;
        LivePreviewViewHolder mHolder3_live;
        FrameLayout mV1;
        FrameLayout mV2;
        FrameLayout mV3;

        public HotOnlineViewHolder(View itemView) {
            super(itemView);
            mHolder1_normal = new NormalViewHolder(itemView.findViewById(R.id.hot_online_1_normal));
            mHolder1_live = new LivePreviewViewHolder(itemView.findViewById(R.id.hot_online_1_live));
            mHolder2_normal = new NormalViewHolder(itemView.findViewById(R.id.hot_online_2_normal));
            mHolder2_live = new LivePreviewViewHolder(itemView.findViewById(R.id.hot_online_2_live));
            mHolder3_normal = new NormalViewHolder(itemView.findViewById(R.id.hot_online_3_normal));
            mHolder3_live = new LivePreviewViewHolder(itemView.findViewById(R.id.hot_online_3_live));
            mV1 = itemView.findViewById(R.id.hot_online_1);
            mV2 = itemView.findViewById(R.id.hot_online_2);
            mV3 = itemView.findViewById(R.id.hot_online_3);
            setUpOnTouchListener(mHolder1_normal);
            setUpOnTouchListener(mHolder1_live);
            setUpOnTouchListener(mHolder2_normal);
            setUpOnTouchListener(mHolder2_live);
            setUpOnTouchListener(mHolder3_normal);
            setUpOnTouchListener(mHolder3_live);
        }

        void setUpOnTouchListener(BaseViewHolder holder) {
            holder.itemView.setOnTouchListener(new ImagePressedTouchListener(holder.mImageView));
        }

        void setUpClickListener(View.OnClickListener l) {
            mHolder1_normal.itemView.setOnClickListener(l);
            mHolder1_live.itemView.setOnClickListener(l);
            mHolder2_normal.itemView.setOnClickListener(l);
            mHolder2_live.itemView.setOnClickListener(l);
            mHolder3_normal.itemView.setOnClickListener(l);
            mHolder3_live.itemView.setOnClickListener(l);
        }
    }


    private static class CustomItemDecoration extends RecyclerView.ItemDecoration {
        CustomItemDecoration() {
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.left = 0;
            outRect.top = 0;
            outRect.right = 0;
            outRect.bottom = Dimensions.pxFromDp(1);
        }
    }
}
