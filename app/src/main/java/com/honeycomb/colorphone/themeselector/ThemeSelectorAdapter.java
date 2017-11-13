package com.honeycomb.colorphone.themeselector;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.acb.call.CPSettings;
import com.acb.call.constant.CPConst;
import com.acb.call.themes.Type;
import com.acb.call.views.InCallActionView;
import com.acb.call.views.ThemePreviewWindow;
import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.ColorPhoneApplication;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.activity.ColorPhoneActivity;
import com.honeycomb.colorphone.activity.GuideApplyThemeActivity;
import com.honeycomb.colorphone.activity.ThemePreviewActivity;
import com.honeycomb.colorphone.download.DownloadHolder;
import com.honeycomb.colorphone.download.DownloadViewHolder;
import com.honeycomb.colorphone.download.TasksManager;
import com.honeycomb.colorphone.download.TasksManagerModel;
import com.honeycomb.colorphone.util.ModuleUtils;
import com.honeycomb.colorphone.util.ThemeUtils;
import com.honeycomb.colorphone.util.Utils;
import com.honeycomb.colorphone.view.DownloadProgressBar;
import com.honeycomb.colorphone.view.GlideApp;
import com.honeycomb.colorphone.view.TypefacedTextView;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.util.FileDownloadUtils;

import java.io.File;
import java.util.ArrayList;

import hugo.weaving.DebugLog;

import static com.acb.call.utils.Utils.getTypeByThemeId;
import static com.honeycomb.colorphone.util.Utils.pxFromDp;

public class ThemeSelectorAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private static final String TAG = "ThemeSelectorAdapter";
    private static final boolean DEBUG_ADAPTER = BuildConfig.DEBUG;
    private final Activity activity;
    private final float mTransX;
    private ArrayList<Theme> data = null;
    private GridLayoutManager layoutManager;
    private Handler mHandler = new Handler();

    public static final int THEME_SELECTOR_ITEM_TYPE_THEME_GIF = 0x1;
    public static final int THEME_SELECTOR_ITEM_TYPE_THEME_VIDEO = 0x8;
    public static final int THEME_SELECTOR_ITEM_TYPE_THEME_LED = 0x2;
    public static final int THEME_SELECTOR_ITEM_TYPE_THEME_TECH = 0x3;

    public static final int THEME_SELECTOR_ITEM_TYPE_STATEMENT = 0x20;

    private static final int THEME_TYPE_MASK = 0x0F;

    private INotificationObserver observer = new INotificationObserver() {
        @Override
        public void onReceive(String s, HSBundle hsBundle) {
            if (ThemePreviewActivity.NOTIFY_THEME_DOWNLOAD.equals(s)) {
                notifyItemChanged(getPos(hsBundle));
            } else if (ThemePreviewActivity.NOTIFY_THEME_SELECT.equals(s)) {
                if (hsBundle != null) {
                    onSelectedTheme(getPos(hsBundle), null);
                }
            }

        }

        private int getPos(HSBundle hsBundle) {
            if (hsBundle != null) {
                int themeId = hsBundle.getInt(ThemePreviewActivity.NOTIFY_THEME_KEY);
                for (Theme theme : data) {
                    if (theme.getId() == themeId) {
                        return data.indexOf(theme);
                    }
                }
            }
            throw new IllegalStateException("Not found theme index!");
        }

    };

    public ThemeSelectorAdapter(Activity activity, final ArrayList<Theme> data) {
        this.activity = activity;
        this.data = data;
        GridLayoutManager.SpanSizeLookup spanSizeLookup = new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch (getItemViewType(position)) {
                    case THEME_SELECTOR_ITEM_TYPE_STATEMENT:
                        return 2;
                    default:
                        return 1;
                }
            }
        };
        layoutManager = new GridLayoutManager(HSApplication.getContext(), 2);
        layoutManager.setSpanSizeLookup(spanSizeLookup);
        mTransX = activity.getResources().getDimensionPixelOffset(R.dimen.theme_card_margin_horizontal) * 0.6f;

    }


    public GridLayoutManager getLayoutManager() {
        return layoutManager;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        HSGlobalNotificationCenter.addObserver(ThemePreviewActivity.NOTIFY_THEME_SELECT, observer);
        HSGlobalNotificationCenter.addObserver(ThemePreviewActivity.NOTIFY_THEME_DOWNLOAD, observer);

    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        HSGlobalNotificationCenter.removeObserver(observer);
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {

        if (DEBUG_ADAPTER) {
            HSLog.d(TAG, "onCreateViewHolder : type " + viewType);
        }
        if ((viewType & THEME_TYPE_MASK) == viewType) {
            View cardViewContent = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_theme_selector, null);
            final ThemeCardViewHolder holder = new ThemeCardViewHolder(cardViewContent);
            // Theme
            switch (viewType) {
                case THEME_SELECTOR_ITEM_TYPE_THEME_GIF:
                    holder.mThemeFlashPreviewWindow.updateThemeLayout(Type.GIF_SHELL);
                    if (!Utils.ATLEAST_LOLLIPOP) {
                        holder.mThemeFlashPreviewWindow.setCornerRadius(activity.getResources().getDimensionPixelSize(R.dimen.theme_card_radius));
                    }
                    break;
                case THEME_SELECTOR_ITEM_TYPE_THEME_VIDEO:
                    holder.mThemeFlashPreviewWindow.updateThemeLayout(Type.VIDEO_SHELL);

                    break;
                case THEME_SELECTOR_ITEM_TYPE_THEME_LED:
                    holder.mThemeFlashPreviewWindow.updateThemeLayout(getTypeByThemeId(Type.LED));
                    holder.mThemePreviewImg.setBackgroundResource(R.drawable.card_bg_round_dark);

                    break;
                case THEME_SELECTOR_ITEM_TYPE_THEME_TECH:
                    holder.mThemeFlashPreviewWindow.updateThemeLayout(getTypeByThemeId(Type.TECH));
                    break;
            }

            holder.initChildView();

            // Set text style
            ThemeUtils.updateStyle(holder.getContentView());

            cardViewContent.findViewById(R.id.card_view).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final int pos = holder.getPositionTag();
                    final Theme theme = data.get(pos);
                    ImageView cover = holder.getCoverView(theme);
                    if (cover.getDrawable() instanceof BitmapDrawable) {
                        Bitmap bitmap = ((BitmapDrawable) cover.getDrawable()).getBitmap();
                        ThemePreviewActivity.cacheBitmap = bitmap;
                    }
                    ThemePreviewActivity.start(activity, data, pos);
                }
            });

            holder.mThemeSelectLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getPositionTag();
                    if (onSelectedTheme(pos, holder)) {
                        CPSettings.putInt(CPConst.PREFS_SCREEN_FLASH_THEME_ID, data.get(pos).getId());
                        HSGlobalNotificationCenter.sendNotification(ThemePreviewActivity.NOTIFY_THEME_SELECT);
                        if (ModuleUtils.isNeedGuideAfterApply()) {
                            GuideApplyThemeActivity.start(v.getContext(), false);
                        }
                    }
                }
            });
            holder.setLikeClick(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getPositionTag();
                    Theme theme = data.get(pos);

                    theme.setLike(!theme.isLike());
                    if (theme.isLike()) {
                        theme.setDownload(theme.getDownload() + 1);
                    } else {
                        theme.setDownload(theme.getDownload() - 1);
                    }

                    holder.setLike(theme);

                }
            });

            return holder;

        } else {
            View stateViewContent = LayoutInflater.from((parent.getContext())).inflate(R.layout.card_view_contains_ads_statement, null);
            return new StatementViewHolder(stateViewContent);
        }
    }

    private int getLastSelectedTheme() {
        int prePos = 0;
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

    private boolean onSelectedTheme(final int pos, ThemeCardViewHolder holder) {
        int prePos = 0;
        boolean playAnimation = true;
        // Clear before.
        for (int i = 0; i < data.size(); i++) {
            Theme t = data.get(i);
            if (t.isSelected()) {
                prePos = i;
                break;
            }
        }
        if (prePos == pos) {
            return false;
        } else {
            Theme t = data.get(prePos);
            t.setSelected(false);
            notifyItemChanged(prePos);
        }
        // Reset current.
        Type type = data.get(pos);

        ColorPhoneApplication.getConfigLog().getEvent().onChooseTheme(type.getIdName().toLowerCase());
        Theme selectedTheme = data.get(pos);
        selectedTheme.setSelected(true);
//        if (holder != null) {
//            holder.setSelected(selectedTheme, playAnimation);
//        } else {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                // Ensure selected item rebound after unselected one.
                notifyItemChanged(pos);
            }
        });
//        }
        return true;
    }

    @DebugLog
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (DEBUG_ADAPTER) {
            HSLog.d(TAG, "bindViewHolder : " + position);
        }
        if (holder instanceof ThemeCardViewHolder) {
            ThemeCardViewHolder cardViewHolder = (ThemeCardViewHolder) holder;
            cardViewHolder.setPositionTag(position);

            if (position % 2 == 0) {
                cardViewHolder.getContentView().setTranslationX(mTransX);
            } else {
                cardViewHolder.getContentView().setTranslationX(-mTransX);
            }
            final Theme curTheme = data.get(position);

            cardViewHolder.mThemeTitle.setText(curTheme.getName());
            cardViewHolder.mAvatarName.setText(curTheme.getAvatarName());
            cardViewHolder.mCallActionView.setTheme(curTheme);

            cardViewHolder.updateTheme(curTheme);

            // Download progress
            final TasksManagerModel model = TasksManager.getImpl().getByThemeId(curTheme.getId());

            if (model != null) {
                cardViewHolder.update(model.getId(), position);
                boolean fileExist = updateTaskHolder((ThemeCardViewHolder) holder, model);
                cardViewHolder.switchToReadyState(fileExist);
            } else {
                cardViewHolder.switchToReadyState(true);
            }
        } else {
            HSLog.d("onBindVieHolder", "contains ads statement.");
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position < data.size()) {
            Theme theme = data.get(position);
            if (theme.getValue() == Type.LED) {
                return THEME_SELECTOR_ITEM_TYPE_THEME_LED;
            } else if (theme.getValue() == Type.TECH) {
                return THEME_SELECTOR_ITEM_TYPE_THEME_TECH;
            } else if (theme.isGif()) {
                return THEME_SELECTOR_ITEM_TYPE_THEME_GIF;
            } else if (theme.isVideo()) {
                return THEME_SELECTOR_ITEM_TYPE_THEME_VIDEO;
            } else {
                throw new IllegalStateException("Can not find right view type for theme ：" + theme);
            }
        } else {
            return THEME_SELECTOR_ITEM_TYPE_STATEMENT;
        }
//        return super.getItemViewType(position);
    }

    private boolean updateTaskHolder(ThemeCardViewHolder holder, TasksManagerModel model) {
        final BaseDownloadTask task = TasksManager.getImpl()
                .getTask(holder.id);
        if (task != null) {
            task.setTag(holder);
        }

        holder.setActionEnabled(true);
        boolean showOpen = false;

        if (TasksManager.getImpl().isReady()) {
            final int status = TasksManager.getImpl().getStatus(model.getId(), model.getPath());
            HSLog.d("sundxing", "position " + holder.position + ",download task status: " + status);
            if (TasksManager.getImpl().isDownloading(status)) {
                // start task, but file not created yet
                // Or just downloading
                holder.updateDownloading(status, TasksManager.getImpl().getSoFar(model.getId())
                        , TasksManager.getImpl().getTotal(model.getId()));
            } else if (!new File(model.getPath()).exists() &&
                    !new File(FileDownloadUtils.getTempPath(model.getPath())).exists()) {
                // not exist file
                holder.updateNotDownloaded(status, 0, 0);
            } else if (TasksManager.getImpl().isDownloaded(status)) {
                // already downloaded and exist
                holder.updateDownloaded(false);
                showOpen = true;
            } else {
                // not start
                holder.updateNotDownloaded(status, TasksManager.getImpl().getSoFar(model.getId())
                        , TasksManager.getImpl().getTotal(model.getId()));
            }
        } else {
            holder.setActionEnabled(false);
        }

        return showOpen;
    }

    @Override
    public int getItemCount() {
        // +1 for statement
        return data.size() + 1;
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        if (holder instanceof INotificationObserver) {
            HSGlobalNotificationCenter.removeObserver(ColorPhoneActivity.NOTIFY_WINDOW_INVISIBLE, (INotificationObserver) holder);
            HSGlobalNotificationCenter.removeObserver(ColorPhoneActivity.NOTIFY_WINDOW_VISIBLE, (INotificationObserver) holder);
        }
    }


    static class ThemeCardViewHolder extends RecyclerView.ViewHolder implements DownloadHolder, INotificationObserver {
        private static final boolean DEBUG_PROGRESS = BuildConfig.DEBUG & true;
        ImageView mThemePreviewImg;
        ImageView mThemeLoadingImg;
        ImageView mAvatar;
        TextView mAvatarName;
        ImageView mAccept;
        ImageView mReject;
        TextView mThemeTitle;
        TextView mThemeLikeCount;
        ThemePreviewWindow mThemeFlashPreviewWindow;
        InCallActionView mCallActionView;

        LottieAnimationView mThemeLikeAnim;
        LottieAnimationView mDownloadFinishedAnim;
        LottieAnimationView mThemeSelectedAnim;
        View mThemeSelectLayout;

        DownloadViewHolder mDownloadViewHolder;

        private int mPositionTag;
        private View mContentView;
        private View mThemeHotMark;

        private Handler mHandler = new Handler();
        private boolean pendingToOpen;

        public void setPositionTag(int position) {
            mPositionTag = position;
        }

        public int getPositionTag() {
            return mPositionTag;
        }

        public View getContentView() {
            return mContentView;
        }


        ThemeCardViewHolder(View itemView) {
            super(itemView);
            mContentView = itemView;
            mThemePreviewImg = (ImageView) itemView.findViewById(R.id.card_preview_img);
            mThemeLoadingImg = (ImageView) itemView.findViewById(R.id.place_holder);
            mThemeTitle = (TextView) itemView.findViewById(R.id.card_title);

            mThemeLikeCount = (TextView) itemView.findViewById(R.id.card_like_count_txt);
            mThemeLikeAnim = (LottieAnimationView) itemView.findViewById(R.id.like_count_icon);

            mThemeFlashPreviewWindow = (ThemePreviewWindow) itemView.findViewById(R.id.card_flash_preview_window);
        }

        public void initChildView() {

            mCallActionView = (InCallActionView) itemView.findViewById(R.id.card_in_call_action_view);
            mCallActionView.setAutoRun(false);
            mAvatar = (ImageView) mContentView.findViewById(R.id.caller_avatar);
            mAvatarName = (TextView) mContentView.findViewById(R.id.caller_name);
            mAccept = (ImageView) mContentView.findViewById(R.id.call_accept);
            mReject = (ImageView) mContentView.findViewById(R.id.call_reject);

            mThemeHotMark = itemView.findViewById(R.id.theme_hot_mark);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mThemeHotMark.setElevation(Utils.pxFromDp(6));
                mThemeHotMark.setTranslationX(pxFromDp(-1));
            }

            mDownloadTaskProgressTxt = (TypefacedTextView) itemView.findViewById(R.id.card_downloading_progress_txt);
            mDownloadTaskProgressTxt.setVisibility(View.GONE);
            mDownloadFinishedAnim = (LottieAnimationView) itemView.findViewById(R.id.card_download_finished_anim);
            mDownloadFinishedAnim.setVisibility(View.GONE);
            mThemeSelectedAnim = (LottieAnimationView) itemView.findViewById(R.id.card_theme_selected_anim);
            mThemeSelectLayout = itemView.findViewById(R.id.card_theme_selected_layout);

            DownloadProgressBar pb = (DownloadProgressBar) itemView.findViewById(R.id.card_downloading_progress_bar);

            mDownloadViewHolder = new DownloadViewHolder(pb, pb, mDownloadTaskProgressTxt, mDownloadFinishedAnim);
            mDownloadViewHolder.setStartAnim((LottieAnimationView) itemView.findViewById(R.id.card_download_start_anim));
            mDownloadViewHolder.setProxyHolder(this);
            mDownloadTaskProgressBar = pb;
        }

        private void setSelected(Theme theme, boolean animation) {

            if (mThemeSelectedAnim != null) {
                if (theme.isSelected()) {
                    if (animation) {
                        mThemeSelectedAnim.playAnimation();
                    } else if (!mThemeSelectedAnim.isAnimating()) {
                        setLottieProgress(mThemeSelectedAnim, 1f);
                    }
                } else {
                    mThemeSelectedAnim.cancelAnimation();
                    setLottieProgress(mThemeSelectedAnim, 0f);
                }
            }

            if (theme.isSelected()) {
                mThemeFlashPreviewWindow.playAnimation(theme);
                mThemeFlashPreviewWindow.setAutoRun(true);
                mCallActionView.setAutoRun(true);
                HSGlobalNotificationCenter.addObserver(ColorPhoneActivity.NOTIFY_WINDOW_INVISIBLE, this);
                HSGlobalNotificationCenter.addObserver(ColorPhoneActivity.NOTIFY_WINDOW_VISIBLE, this);
            } else {
                mThemeFlashPreviewWindow.clearAnimation(theme);
                mThemeFlashPreviewWindow.setAutoRun(false);
                mCallActionView.setAutoRun(false);
                if (theme.isVideo()) {
                    getCoverView(theme).setVisibility(View.VISIBLE);
                }
                HSGlobalNotificationCenter.removeObserver(ColorPhoneActivity.NOTIFY_WINDOW_INVISIBLE, this);
                HSGlobalNotificationCenter.removeObserver(ColorPhoneActivity.NOTIFY_WINDOW_VISIBLE, this);
            }
        }

        public void setSelected(Theme selected) {
            setSelected(selected, false);
        }

        public ImageView getCoverView(final Theme theme) {
            return theme.isVideo() ? mThemeFlashPreviewWindow.getImageCover() : mThemePreviewImg;
        }

        @DebugLog
        public void updateTheme(final Theme theme) {
            if (theme.isMedia()) {
                ImageView targetView = getCoverView(theme);
                startLoadingScene();
                GlideApp.with(mContentView).asBitmap()
                        .centerCrop()
                        .placeholder(theme.getThemePreviewDrawable())
                        .load(theme.getPreviewImage())
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .listener(new RequestListener<Bitmap>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                if (theme.isSelected()) {
                                    endLoadingScene();
                                }
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                endLoadingScene();
                                return false;
                            }
                        })
                        .dontAnimate()
                        .into(targetView);
                
                GlideApp.with(mContentView)
                        .load(theme.getAvatar())
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .transition(DrawableTransitionOptions.withCrossFade(200))
                        .into(mAvatar);
            } else {
                endLoadingScene();
            }

            setSelected(theme);
            setHotTheme(theme.isHot());
            setLike(theme, false);

        }

        private void startLoadingScene() {
            mThemeLoadingImg.setVisibility(View.VISIBLE);
            mCallActionView.setVisibility(View.INVISIBLE);
            mThemeFlashPreviewWindow.getCallView().setVisibility(View.INVISIBLE);
        }

        private void endLoadingScene() {
            mThemeLoadingImg.setVisibility(View.INVISIBLE);
            mCallActionView.setVisibility(View.VISIBLE);
            mThemeFlashPreviewWindow.getCallView().setVisibility(View.VISIBLE);
        }

        public void setHotTheme(boolean hot) {
            if (mThemeHotMark != null) {
                mThemeHotMark.setVisibility(hot ? View.VISIBLE : View.INVISIBLE);
            }
        }

        //---------------- For progress ---------
        /**
         * viewHolder position
         */
        private int position;
        /**
         * com.honeycomb.colorphone.download id
         */
        private int id;

        private View mDownloadTaskProgressBar;
        private TypefacedTextView mDownloadTaskProgressTxt;

        private Runnable mAniamtionEndStateRunnable = new Runnable() {
            @Override
            public void run() {
                switchToReadyState(true);
            }
        };

        public void update(final int id, final int position) {
            this.id = id;
            this.position = position;
            this.mDownloadViewHolder.bindTaskId(id);
        }

        @Override
        public void updateDownloaded(final boolean progressFlag) {
            // If file already downloaded, not play animation

            mDownloadViewHolder.updateDownloaded(progressFlag);
            mDownloadTaskProgressBar.removeCallbacks(mAniamtionEndStateRunnable);
            if (progressFlag) {
                mDownloadTaskProgressBar.postDelayed(mAniamtionEndStateRunnable, 600);
            }
            if (DEBUG_PROGRESS) {
                HSLog.d("sundxing", position + " download success!");
            }
        }

        public void updateNotDownloaded(final int status, final long sofar, final long total) {

            if (DEBUG_PROGRESS) {
                HSLog.d("sundxing", position + " download stopped, status = " + status);
            }
            mDownloadViewHolder.updateNotDownloaded(status, sofar, total);
        }

        public void updateDownloading(final int status, final long sofar, final long total) {

            if (DEBUG_PROGRESS) {
                final float percent = sofar
                        / (float) total;
                HSLog.d("sundxing", position + " download process, percent = " + percent);
            }
            mDownloadViewHolder.updateDownloading(status, sofar, total);

        }

        public void switchToReadyState(boolean ready) {
            mDownloadTaskProgressBar.setVisibility(ready ? View.GONE : View.VISIBLE);
            mThemeSelectLayout.setVisibility(ready ? View.VISIBLE : View.GONE);
            if (ready) {
                mDownloadFinishedAnim.setVisibility(View.GONE);
                mDownloadTaskProgressTxt.setVisibility(View.GONE);
            }
            if (ready) {
                mThemeSelectedAnim.setVisibility(View.VISIBLE);
            } else {
                mThemeSelectedAnim.setVisibility(View.GONE);
            }
        }

        public DownloadViewHolder getDownloadHolder() {
            return mDownloadViewHolder;
        }

        public void setActionEnabled(boolean enable) {
            mDownloadTaskProgressBar.setEnabled(enable);
        }

        @Override
        public int getId() {
            return id;
        }

        public void setLikeClick(View.OnClickListener onClickListener) {
            mThemeLikeCount.setOnClickListener(onClickListener);
            mThemeLikeAnim.setOnClickListener(onClickListener);
        }

        public void setLike(Theme theme, boolean anim) {
            if (mThemeLikeAnim.isAnimating()) {
                return;
            }
            if (theme.isLike()) {
                if (anim) {
                    mThemeLikeAnim.playAnimation();
                } else {
                    setLottieProgress(mThemeLikeAnim, 1f);
                }
            } else {
                setLottieProgress(mThemeLikeAnim, 0f);
            }
            mThemeLikeCount.setText(String.valueOf(theme.getDownload()));
        }

        private void setLottieProgress(LottieAnimationView animationView, float v) {
            if (animationView.getProgress() != v) {
                animationView.setProgress(v);
            }
        }

        public void setLike(Theme theme) {
            setLike(theme, true);
        }

        @Override
        public void onReceive(String s, HSBundle hsBundle) {
            if (ColorPhoneActivity.NOTIFY_WINDOW_INVISIBLE.equals(s)) {
                mThemeFlashPreviewWindow.stopAnimations();
                mCallActionView.stopAnimations();
            } else if (ColorPhoneActivity.NOTIFY_WINDOW_VISIBLE.equals(s)) {
                mThemeFlashPreviewWindow.startAnimations();
                mCallActionView.doAnimation();
            }
        }
    }

    static class StatementViewHolder extends RecyclerView.ViewHolder {

        public StatementViewHolder(View itemView) {
            super(itemView);
        }
    }
}