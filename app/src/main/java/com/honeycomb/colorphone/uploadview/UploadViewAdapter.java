package com.honeycomb.colorphone.uploadview;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.activity.ThemePreviewActivity;
import com.honeycomb.colorphone.themeselector.ScaleUpTouchListener;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.TransitionUtil;
import com.honeycomb.colorphone.util.Utils;
import com.honeycomb.colorphone.view.GlideApp;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import hugo.weaving.DebugLog;

public class UploadViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "UploadViewAdapter";
    private static final boolean DEBUG_ADAPTER = BuildConfig.DEBUG;

    private Context context;
    public ArrayList<Theme> data;
    private GridLayoutManager layoutManager;
    public List<Theme> mDeleteDataList = new ArrayList<>();

    UploadViewAdapter(Context context, final ArrayList<Theme> data) {
        this.context = context;
        this.data = data;
        layoutManager = new GridLayoutManager(HSApplication.getContext(), 2);
    }

    public GridLayoutManager getLayoutManager() {
        return layoutManager;
    }

    @Override
    public void onAttachedToRecyclerView(@NotNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(@NotNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull final ViewGroup parent, int viewType) {

        if (DEBUG_ADAPTER) {
            HSLog.d(TAG, "onCreateViewHolder : type " + viewType);
        }
        View cardViewContent = LayoutInflater.from(context).inflate(R.layout.upload_list_item, parent, false);
        final ItemCardViewHolder holder = new ItemCardViewHolder(cardViewContent);

        HSGlobalNotificationCenter.addObserver("", new INotificationObserver() {
            @Override
            public void onReceive(String s, HSBundle hsBundle) {
                if ("upload_edit".equals(s) || "publish_edit".equals(s)) {
                    holder.mSelectStatus.setVisibility(View.VISIBLE);
                } else if ("upload_cancel".equals(s) || "publish_cancel".equals(s)) {
                    holder.mSelectStatus.setVisibility(View.GONE);
                }
            }
        });

        View cardView = cardViewContent.findViewById(R.id.item_layout);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCardClick(holder);
            }
        });
        cardView.setOnTouchListener(new ScaleUpTouchListener());

        return holder;
    }

    private void onCardClick(ItemCardViewHolder holder) {
        final int pos = holder.getPositionTag();
        Theme theme = data.get(pos);

        ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.
                makeSceneTransitionAnimation((Activity) context,
                        Pair.create(holder.mPreviewImage, TransitionUtil.getViewTransitionName(TransitionUtil.TAG_PREVIEW_IMAGE, theme))
                );
        ThemePreviewActivity.start(context, pos, activityOptionsCompat.toBundle());
    }

    @DebugLog
    @Override
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder holder, final int position) {
        if (DEBUG_ADAPTER) {
            HSLog.d(TAG, "bindViewHolder : " + position);
        }
        if (holder instanceof ItemCardViewHolder) {

            ItemCardViewHolder cardViewHolder = (ItemCardViewHolder) holder;
            cardViewHolder.setPositionTag(position);

            final Theme curTheme = data.get(position);

            // CardView
            if (curTheme.isSelected()) {
                HSLog.d(TAG, "selected theme start downloading : " + curTheme.getIdName());
                curTheme.setSelected(false);
                curTheme.setPendingSelected(true);
            }

            if (curTheme.isPendingSelected()) {
                curTheme.setSelected(true);
                curTheme.setPendingSelected(false);
            }

            cardViewHolder.updateTheme(curTheme);

            ((ItemCardViewHolder) holder).mSelectStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (((ItemCardViewHolder) holder).getSelectStatus()) {
                        mDeleteDataList.remove(data.get(position));
                        ((ItemCardViewHolder) holder).setSelectStatus(false);
                    } else {
                        mDeleteDataList.add(data.get(position));
                        ((ItemCardViewHolder) holder).setSelectStatus(true);
                    }
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ItemCardViewHolder extends RecyclerView.ViewHolder {

        private static int[] sThumbnailSize = Utils.getThumbnailImageSize();

        private View mContentView;
        private ImageView mPreviewImage;
        private TextView mItemTitle;
        private ImageView mSelectStatus;

        private int mPositionTag;
        private boolean mIsSelect = false;

        void setPositionTag(int position) {
            mPositionTag = position;
        }

        int getPositionTag() {
            return mPositionTag;
        }

        void setSelectStatus(boolean isSelect) {
            mIsSelect = isSelect;
        }

        boolean getSelectStatus() {
            return mIsSelect;
        }

        ItemCardViewHolder(View itemView) {
            super(itemView);
            mContentView = itemView;
            mPreviewImage = itemView.findViewById(R.id.item_preview_img);
            mItemTitle = itemView.findViewById(R.id.item_name);
            mSelectStatus = itemView.findViewById(R.id.select_status);
        }

        @DebugLog
        void updateTheme(final Theme theme) {
            mItemTitle.setText(theme.getName());

            ViewCompat.setTransitionName(mPreviewImage, TransitionUtil.getViewTransitionName(TransitionUtil.TAG_PREVIEW_IMAGE, theme));
            GlideApp.with(mContentView).asBitmap()
                    .centerCrop()
                    .placeholder(theme.getThemePreviewDrawable())
                    .load(theme.getPreviewImage())
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .override(sThumbnailSize[0], sThumbnailSize[1])
                    .listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(mPreviewImage);
            if (mIsSelect) {
                mSelectStatus.setImageDrawable(HSApplication.getContext().getResources().getDrawable(R.drawable.face_1));
            } else {
                mSelectStatus.setImageDrawable(HSApplication.getContext().getResources().getDrawable(R.drawable.face_2));
            }
            HSLog.d(TAG, "load image size : " + sThumbnailSize[0] + ", " + sThumbnailSize[1]);
        }
    }
}
