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

import com.acb.call.constant.ScreenFlashConst;
import com.acb.call.customize.ScreenFlashSettings;
import com.acb.call.themes.Type;
import com.acb.call.views.ThemePreviewWindow;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.WatchedUploadScrollListener;
import com.honeycomb.colorphone.activity.ThemePreviewActivity;
import com.honeycomb.colorphone.themeselector.ScaleUpTouchListener;
import com.honeycomb.colorphone.util.TransitionUtil;
import com.honeycomb.colorphone.util.Utils;
import com.honeycomb.colorphone.view.GlideApp;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import hugo.weaving.DebugLog;

import static android.view.View.VISIBLE;

public class UploadViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "UploadViewAdapter";
    private static final boolean DEBUG_ADAPTER = BuildConfig.DEBUG;

    private Context context;
    private String from = "";
    public ArrayList<Theme> data = new ArrayList<>();
    private GridLayoutManager layoutManager;
    private RecyclerView recyclerView;
    public List<Theme> mDeleteDataList = new ArrayList<>();
    private boolean mIsEdit = false;

    public void setIsEdit(boolean isEdit) {
        mIsEdit = isEdit;
    }

    private INotificationObserver observer = new INotificationObserver() {
        @Override
        public void onReceive(String s, HSBundle hsBundle) {
            if (ThemePreviewActivity.NOTIFY_THEME_UPLOAD_SELECT.equals(s) && "upload".equals(from)) {
                if (hsBundle != null) {
                    int pos = getDataPos(hsBundle);
                    selectTheme(pos);
                }
            } else if (ThemePreviewActivity.NOTIFY_THEME_UPLOAD_DOWNLOAD.equals(s) && "upload".equals(from)) {
                if (hsBundle != null) {
                    notifyItemChanged(getAdapterPos(hsBundle));
                }
            } else if (ThemePreviewActivity.NOTIFY_THEME_PUBLISH_SELECT.equals(s) && "publish".equals(from)) {
                if (hsBundle != null) {
                    int pos = getDataPos(hsBundle);
                    selectTheme(pos);
                }
            } else if (ThemePreviewActivity.NOTIFY_THEME_UPLOAD_DOWNLOAD.equals(s) && "publish".equals(from)) {
                if (hsBundle != null) {
                    notifyItemChanged(getAdapterPos(hsBundle));
                }
            }
        }

        private int getAdapterPos(HSBundle hsBundle) {
            return getDataPos(hsBundle);
        }

        private int getDataPos(HSBundle hsBundle) {
            if (hsBundle != null) {
                int themeId = hsBundle.getInt(ThemePreviewActivity.NOTIFY_THEME_KEY);
                for (Theme theme : data) {
                    if (theme.getId() == themeId) {
                        return data.indexOf(theme);
                    }
                }
            }
            return 0;
//            throw new IllegalStateException("Not found theme index!");
        }
    };

    private void selectTheme(final int pos) {
        int prePos = 0;
        // Clear before.
        for (int i = 0; i < data.size(); i++) {
            Theme t = data.get(i);
            if (t.isSelected()) {
                prePos = i;
                break;
            }
        }

        if (prePos == pos) {
            return;
        } else {
            Theme t = data.get(prePos);
            t.setSelected(false);
            notifyItemSelected(prePos, t);
        }
        // Reset current.
        Theme selectedTheme = data.get(pos);
        selectedTheme.setSelected(true);
        notifyItemSelected(pos, selectedTheme);
    }

    public void notifyItemSelected(int pos, Theme theme) {
        int adapterPos = pos;
        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(adapterPos);
        if (holder == null) {
            // Item not visible in screen.
            notifyItemChanged(adapterPos);
        } else if (holder instanceof ItemCardViewHolder) {
            HSLog.d(TAG, "notifyItemSelected, setSelected ");
            ((ItemCardViewHolder) holder).setSelected(theme);
        }
    }

    UploadViewAdapter(Context context, String from) {
        this.context = context;
        this.from = from;
        layoutManager = new GridLayoutManager(HSApplication.getContext(), 2);
    }

    public void updateData(ArrayList<Theme> data) {
        if (data != null && !data.isEmpty()) {
            this.data.clear();
            this.data.addAll(data);
        }
    }

    public GridLayoutManager getLayoutManager() {
        return layoutManager;
    }

    @Override
    public void onAttachedToRecyclerView(@NotNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
        recyclerView.addOnScrollListener(new WatchedUploadScrollListener());

        HSGlobalNotificationCenter.addObserver(ThemePreviewActivity.NOTIFY_THEME_UPLOAD_SELECT, observer);
        HSGlobalNotificationCenter.addObserver(ThemePreviewActivity.NOTIFY_THEME_UPLOAD_DOWNLOAD, observer);
        HSGlobalNotificationCenter.addObserver(ThemePreviewActivity.NOTIFY_THEME_PUBLISH_SELECT, observer);
        HSGlobalNotificationCenter.addObserver(ThemePreviewActivity.NOTIFY_THEME_PUBLISH_DOWNLOAD, observer);
    }

    @Override
    public void onDetachedFromRecyclerView(@NotNull RecyclerView recyclerView) {
        HSGlobalNotificationCenter.removeObserver(observer);
        super.onDetachedFromRecyclerView(recyclerView);
    }

    public int getLastSelectedLayoutPos() {
        int prePos = -1;
        // Clear before.
        for (int i = 0; i < data.size(); i++) {
            Theme t = data.get(i);
            if (t.isSelected()) {
                prePos = i;
                break;
            }
        }
        return prePos;
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull final ViewGroup parent, int viewType) {

        if (DEBUG_ADAPTER) {
            HSLog.d(TAG, "onCreateViewHolder : type " + viewType);
        }
        View cardViewContent = LayoutInflater.from(context).inflate(R.layout.upload_list_item, parent, false);
        final ItemCardViewHolder holder = new ItemCardViewHolder(cardViewContent);
        holder.mThemeFlashPreviewWindow.updateThemeLayout(Type.VIDEO_SHELL);

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
        if (mIsEdit) {
            holder.mSelectStatus.performClick();
        } else {
            final int pos = holder.getPositionTag();
            Theme theme = data.get(pos);

            ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context,
                    Pair.create(holder.mPreviewImage, TransitionUtil.getViewTransitionName(TransitionUtil.TAG_PREVIEW_IMAGE, theme))
            );
            ThemePreviewActivity.start(context, pos, from, activityOptionsCompat.toBundle());
        }
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

            int selectedThemeId = ScreenFlashSettings.getInt(ScreenFlashConst.PREFS_SCREEN_FLASH_THEME_ID, -1);
            final Theme curTheme = data.get(position);

            if (selectedThemeId == curTheme.getId()) {
                curTheme.setSelected(true);
            }

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

            if (mIsEdit) {
                ((ItemCardViewHolder) holder).mSelectStatus.setVisibility(View.VISIBLE);
            } else {
                ((ItemCardViewHolder) holder).mSelectStatus.setVisibility(View.GONE);
                curTheme.setDeleteSelected(false);
            }

            cardViewHolder.updateTheme(curTheme);

            ((ItemCardViewHolder) holder).mSelectStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (curTheme.isDeleteSelected()) {
                        mDeleteDataList.remove(data.get(position));
                        curTheme.setDeleteSelected(false);
                        ((ItemCardViewHolder) holder).mSelectStatus.setImageDrawable(HSApplication.getContext().getResources().getDrawable(R.drawable.icon_uploadpage_unselected));
                    } else {
                        mDeleteDataList.add(data.get(position));
                        curTheme.setDeleteSelected(true);
                        ((ItemCardViewHolder) holder).mSelectStatus.setImageDrawable(HSApplication.getContext().getResources().getDrawable(R.drawable.icon_uploadpage_selected));

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
        ThemePreviewWindow mThemeFlashPreviewWindow;
        private ThemeStatusView mThemeStatusView;

        private int mPositionTag;
        private boolean mHolderDataReady;

        void setPositionTag(int position) {
            mPositionTag = position;
        }

        int getPositionTag() {
            return mPositionTag;
        }

        ItemCardViewHolder(View itemView) {
            super(itemView);
            mContentView = itemView;
            mPreviewImage = itemView.findViewById(R.id.item_preview_img);
            mItemTitle = itemView.findViewById(R.id.item_name);
            mSelectStatus = itemView.findViewById(R.id.select_status);
            mThemeFlashPreviewWindow = itemView.findViewById(R.id.card_flash_preview_window);
            mThemeFlashPreviewWindow.setPreviewType(ThemePreviewWindow.PreviewType.PREVIEW);
            mThemeStatusView = new ThemeStatusView(itemView);
        }

        @DebugLog
        void updateTheme(final Theme theme) {
            mItemTitle.setText(theme.getName());

            ViewCompat.setTransitionName(mPreviewImage, TransitionUtil.getViewTransitionName(TransitionUtil.TAG_PREVIEW_IMAGE, theme));

            ImageView targetView = getCoverView(theme);
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
                    .into(targetView);

            if (theme.isDeleteSelected()) {
                mSelectStatus.setImageDrawable(HSApplication.getContext().getResources().getDrawable(R.drawable.icon_uploadpage_selected));
            } else {
                mSelectStatus.setImageDrawable(HSApplication.getContext().getResources().getDrawable(R.drawable.icon_uploadpage_unselected));
            }
            HSLog.d(TAG, "load image size : " + sThumbnailSize[0] + ", " + sThumbnailSize[1]);
            setSelected(theme);
            switchToReadyState(true, theme.isSelected());
            mHolderDataReady = true;
        }

        public static class ThemeStatusView {

            private TextView mThemeSelected;

            public ThemeStatusView(View rootView) {

                View itemView = rootView;

                mThemeSelected = itemView.findViewById(R.id.card_selected);
                mThemeSelected.setVisibility(VISIBLE);

            }

            public void setSelected(Theme theme) {

                if (theme.isSelected()) {
                    mThemeSelected.setVisibility(VISIBLE);
                } else {
                    mThemeSelected.setVisibility(View.GONE);
                }
            }

            public void switchToReadyState(boolean ready, boolean isSelected) {

                boolean showSelected = ready && isSelected;
                if (showSelected) {
                    mThemeSelected.setVisibility(VISIBLE);
                }
                if (!ready) {
                    mThemeSelected.setVisibility(View.GONE);
                }
            }
        }

        private void setSelected(Theme theme) {
            mThemeStatusView.setSelected(theme);

            if (theme.isSelected()) {

                HSLog.d(TAG, "selected : " + theme.getIdName());
                mThemeFlashPreviewWindow.playAnimation(theme);
                mThemeFlashPreviewWindow.setAutoRun(true);
            } else {
                HSLog.d(TAG, "取消 selected : " + theme.getIdName());
                mThemeFlashPreviewWindow.clearAnimation(theme);
                mThemeFlashPreviewWindow.setAutoRun(false);
                if (theme.isVideo()) {
                    getCoverView(theme).setVisibility(View.VISIBLE);
                }
            }
        }

        public void switchToReadyState(boolean ready, boolean isSelected) {
            mThemeStatusView.switchToReadyState(ready, isSelected);
        }

        public ImageView getCoverView(final Theme theme) {
            return theme.isVideo() ? mThemeFlashPreviewWindow.getImageCover() : mPreviewImage;
        }

        public void stopAnimation() {
            mThemeFlashPreviewWindow.stopAnimations();
        }

        public void startAnimation() {
            if (mHolderDataReady) {
                mThemeFlashPreviewWindow.startAnimations();
            }
        }
    }
}
