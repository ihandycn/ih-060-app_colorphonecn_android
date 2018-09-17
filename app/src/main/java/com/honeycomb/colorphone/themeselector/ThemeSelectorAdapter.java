package com.honeycomb.colorphone.themeselector;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.acb.call.customize.ScreenFlashSettings;
import com.acb.call.themes.Type;
import com.acb.call.views.InCallActionView;
import com.acb.call.views.ThemePreviewWindow;
import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.ColorPhoneApplication;
import com.honeycomb.colorphone.ConfigLog;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.activity.ColorPhoneActivity;
import com.honeycomb.colorphone.activity.PopularThemeActivity;
import com.honeycomb.colorphone.activity.PopularThemePreviewActivity;
import com.honeycomb.colorphone.activity.ThemePreviewActivity;
import com.honeycomb.colorphone.contact.ContactManager;
import com.honeycomb.colorphone.download.DownloadHolder;
import com.honeycomb.colorphone.download.DownloadViewHolder;
import com.honeycomb.colorphone.download.TasksManager;
import com.honeycomb.colorphone.download.TasksManagerModel;
import com.honeycomb.colorphone.notification.NotificationUtils;
import com.honeycomb.colorphone.notification.permission.EventSource;
import com.honeycomb.colorphone.notification.permission.PermissionHelper;
import com.honeycomb.colorphone.permission.FloatWindowManager;
import com.honeycomb.colorphone.util.ApplyInfoAutoPilotUtils;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.honeycomb.colorphone.util.RingtoneHelper;
import com.honeycomb.colorphone.util.Utils;
import com.honeycomb.colorphone.view.GlideApp;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.util.FileDownloadUtils;
import com.superapps.util.Dimensions;
import com.superapps.util.Threads;

import java.io.File;
import java.util.ArrayList;

import hugo.weaving.DebugLog;

import static com.acb.call.constant.ScreenFlashConst.PREFS_SCREEN_FLASH_THEME_ID;
import static com.acb.utils.Utils.getTypeByThemeId;
import static com.honeycomb.colorphone.activity.ThemePreviewActivity.NOTIFY_CONTEXT_KEY;
import static com.honeycomb.colorphone.activity.ThemePreviewActivity.NOTIFY_THEME_KEY;
import static com.honeycomb.colorphone.activity.ThemePreviewActivity.NOTIFY_THEME_SELECT;
import static com.honeycomb.colorphone.preview.ThemePreviewView.saveThemeApplys;
import static com.honeycomb.colorphone.util.Utils.pxFromDp;

public class ThemeSelectorAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "ThemeSelectorAdapter";
    private static final boolean DEBUG_ADAPTER = BuildConfig.DEBUG;
    private final Activity activity;
    private RecyclerView recyclerView;
    private float mTransX;
    private ArrayList<Theme> data = null;
    private GridLayoutManager layoutManager;
    private boolean mTipHeaderVisible;
    private boolean mHotThemeHolderVisible;
    private int mUnLockThemeId = -1;

    //autopilot test
    private boolean mIsThemeInformationVisible = true;
    private boolean mIsApplyButtonVisible = true;

    private Handler adapterHandler = new Handler();

    public static final int THEME_SELECTOR_ITEM_TYPE_THEME_GIF = 0x1;
    public static final int THEME_SELECTOR_ITEM_TYPE_THEME_VIDEO = 0x8;
    public static final int THEME_SELECTOR_ITEM_TYPE_THEME_LED = 0x2;
    public static final int THEME_SELECTOR_ITEM_TYPE_THEME_TECH = 0x3;

    public static final int THEME_SELECTOR_ITEM_TYPE_STATEMENT = 0x20;
    public static final int THEME_SELECTOR_ITEM_TYPE_TIP = 0x30;
    public static final int THEME_SELECTOR_ITEM_TYPE_HOT_THEME_HOLDER = 0x40;

    private static final int THEME_TYPE_MASK = 0x0F;

    private INotificationObserver observer = new INotificationObserver() {
        @Override
        public void onReceive(String s, HSBundle hsBundle) {
            if (ThemePreviewActivity.NOTIFY_THEME_DOWNLOAD.equals(s)) {
                if (hsBundle != null) {
                    notifyItemChanged(getAdapterPos(hsBundle));
                }
            } else if (ThemePreviewActivity.NOTIFY_THEME_SELECT.equals(s)) {
                if (hsBundle != null) {
                    int pos = getDataPos(hsBundle);
                    Theme selectedTheme = data.get(pos);

                    if (!selectTheme(pos, null, false)) {
                        if (!activity.equals(hsBundle.getObject(NOTIFY_CONTEXT_KEY))) {
                            notifyDataSetChanged();
                        }
                    }

                    ColorPhoneApplication.getConfigLog().getEvent().onChooseTheme(
                            selectedTheme.getIdName().toLowerCase(),
                            ConfigLog.FROM_DETAIL);
                }
            } else if (ColorPhoneActivity.NOTIFICATION_ON_REWARDED.equals(s)) {
                if (hsBundle != null) {
                    notifyItemChanged(unlockThemeAndGetAdapterPos(hsBundle));
                }
            }
        }

        private int unlockThemeAndGetDatePos(HSBundle hsBundle) {
            if (hsBundle != null) {
                int themeId = hsBundle.getInt(ThemePreviewActivity.NOTIFY_THEME_KEY);
                for (Theme theme : data) {
                    if (theme.getId() == themeId) {
                        theme.setLocked(false);
                        return data.indexOf(theme);
                    }
                }
            }
            return 0;
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

        private int getAdapterPos(HSBundle hsBundle) {
            return getDataPos(hsBundle) + getHeaderCount();
        }

        private int unlockThemeAndGetAdapterPos(HSBundle hsBundle) {
            return unlockThemeAndGetDatePos(hsBundle) + getHeaderCount();
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
                    case THEME_SELECTOR_ITEM_TYPE_TIP:
                    case THEME_SELECTOR_ITEM_TYPE_HOT_THEME_HOLDER:
                        return 2;
                    default:
                        return 1;
                }
            }
        };
        layoutManager = new GridLayoutManager(HSApplication.getContext(), 2);
        layoutManager.setSpanSizeLookup(spanSizeLookup);
        if (TextUtils.equals(BuildConfig.FLAVOR, "colorflash")) {
            mTransX = -Utils.pxFromDp(1);
        } else {
            mTransX = activity.getResources().getDimensionPixelOffset(R.dimen.theme_card_margin_horizontal) * 0.6f;
        }
        if (Dimensions.isRtl()) {
            mTransX = -mTransX;
        }
    }

    public void updateApplyInformationAutoPilotValue() {
        boolean tempIsApplyVisible = ApplyInfoAutoPilotUtils.showApplyButton();
        boolean tempIsThemeInformationVisible = ApplyInfoAutoPilotUtils.showThemeInformation();

        boolean needNotifyDataSetChanged = false;
        if (mIsApplyButtonVisible != tempIsApplyVisible
                || mIsThemeInformationVisible != tempIsThemeInformationVisible) {
            needNotifyDataSetChanged = true;
        }
        if (needNotifyDataSetChanged) {
            mIsApplyButtonVisible = tempIsApplyVisible;
            mIsThemeInformationVisible = tempIsThemeInformationVisible;
            notifyDataSetChanged();
        }

    }

    public int getUnLockThemeId() {
        return mUnLockThemeId;
    }

    public void setHeaderTipVisible(boolean visible) {
        mTipHeaderVisible = visible;
    }

    public boolean isTipHeaderVisible() {
        return mTipHeaderVisible;
    }

    public boolean isHotThemeHolderVisible() {
        return mHotThemeHolderVisible;
    }

    public void setHotThemeHolderVisible(boolean visible) {
        mHotThemeHolderVisible = visible;
    }


    public GridLayoutManager getLayoutManager() {
        return layoutManager;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        HSGlobalNotificationCenter.addObserver(ThemePreviewActivity.NOTIFY_THEME_SELECT, observer);

        HSGlobalNotificationCenter.addObserver(ThemePreviewActivity.NOTIFY_THEME_DOWNLOAD, observer);
        HSGlobalNotificationCenter.addObserver(ColorPhoneActivity.NOTIFICATION_ON_REWARDED, observer);
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
            View cardViewContent = activity.getLayoutInflater().inflate(R.layout.card_view_theme_selector, null);
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

            cardViewContent.findViewById(R.id.card_view).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final int pos = holder.getPositionTag();

                    if (activity instanceof PopularThemeActivity) {
                        LauncherAnalytics.logEvent("ColorPhone_BanboList_ThemeDetail_View");
                        PopularThemePreviewActivity.start(activity, pos);
                    } else {
                        ThemePreviewActivity.start(activity, pos);
                    }
                    ApplyInfoAutoPilotUtils.logThumbnailClicked();
                }
            });

            holder.mThemeSelectLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getPositionTag();
                    if (selectTheme(pos, holder, true)) {
                        onThemeSelected(pos);
                    }
                    ApplyInfoAutoPilotUtils.logApplyButtonClicked();

                    if (activity instanceof ColorPhoneActivity) {
                        LauncherAnalytics.logEvent("ColorPhone_MainView_Apply_Icon_Clicked");
                    } else if (activity instanceof PopularThemeActivity) {
                        LauncherAnalytics.logEvent("ColorPhone_BanboList_Apply_icon_Clicked");
                    }
                }
            });

            holder.mLockIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int pos = holder.getPositionTag();
                    final Theme theme = data.get(pos);

                    theme.setLocked(false);
                    notifyItemSelected(pos, theme);
                }
            });

            holder.setDownloadedUpdateListener(new ThemeCardViewHolder.DownloadedUpdateListener() {
                @Override
                public void onUpdateDownloaded() {
                    if (recyclerView.isComputingLayout()) {
                        return;
                    }
                    int pos = holder.getPositionTag();
                    final Theme theme = data.get(pos);
                    if (!theme.isSelected()) {
                        holder.removeAnimationEndStateRunnable();
                        holder.switchToReadyState(true, false);
                    }
                    notifyItemSelected(pos, theme, false);
                }

                @Override
                public void onStartDownload() {
                    int pos = holder.getPositionTag();
                    if (selectTheme(pos, holder, false)) {
                        onThemeSelected(pos);
                    }
                }
            });


            holder.setLikeClick(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final int pos = holder.getPositionTag();
                    final Theme theme = data.get(pos);

                    theme.setLike(!theme.isLike());
                    if (theme.isLike()) {
                        theme.setDownload(theme.getDownload() + 1);
                    } else {
                        theme.setDownload(theme.getDownload() - 1);
                    }

                    holder.setLike(theme);

                }
            });

            if (activity instanceof PopularThemeActivity) {
                holder.mApplyText.setTextColor(0xFFffffff);
                holder.mThemeTitle.setTextColor(0xFFffffff);
                holder.mDownloadFinishedAnim.setAnimation("lottie/white/theme_downloaded.json");
                holder.mApplyClickedAnim.setAnimation("lottie/white/theme_apply_clicked.json");
                holder.mThemeSelectedAnim.setAnimation("lottie/white/theme_downloaded.json");
                holder.mDownloadTaskProgressBar.setAnimation("lottie/white/theme_progress.json");
            }
            return holder;

        } else if (viewType == THEME_SELECTOR_ITEM_TYPE_STATEMENT) {
            View stateViewContent = activity.getLayoutInflater().inflate(R.layout.card_view_contains_ads_statement, null);
            return new StatementViewHolder(stateViewContent);
        } else if (viewType == THEME_SELECTOR_ITEM_TYPE_TIP) {
            View tipView = activity.getLayoutInflater().inflate(R.layout.notification_access_toast_layout, parent, false);
            TextView textView = tipView.findViewById(R.id.hint_title);
            boolean floatPermission = FloatWindowManager.getInstance().checkPermission(activity);
            if (!floatPermission) {
                textView.setText(R.string.draw_overlay_bar_hint);
            } else {
                textView.setText(R.string.acb_phone_grant_notification_access_title);
            }
            tipView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (PermissionHelper.requestDrawOverlayIfNeeded(EventSource.List)) {
                        PermissionHelper.waitOverlayGranted(EventSource.List, true);
                    } else {
                        PermissionHelper.requestNotificationAccessIfNeeded(EventSource.List, activity);
                    }
                    LauncherAnalytics.logEvent("Colorphone_List_Page_Notification_Alert_Clicked");
                }
            });
            return new TopTipViewHolder(tipView);
        } else if (viewType == THEME_SELECTOR_ITEM_TYPE_HOT_THEME_HOLDER) {
            View view = activity.getLayoutInflater().inflate(R.layout.acb_layout_hot_theme_view, parent, false);

            final ImageView target = view.findViewById(R.id.hot_theme_image);

            GlideApp.with(activity)
                    .load(HSConfig.optString("#7641DB", "Application", "Special", "SpecialThumbnail"))
                    .centerInside()
                    .dontAnimate()
                    .apply(new RequestOptions().transform(new RoundedCorners(Dimensions.pxFromDp(5f))))
                    .into(target);
            target.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, PopularThemeActivity.class);
                    activity.startActivity(intent);
                    LauncherAnalytics.logEvent("ColorPhone_MainView_BanboEntrance_Clicked");
                }
            });

            return new HotThemeHolder(view);
        } else {
            throw new IllegalStateException("error viewtype");
        }
    }

    private void onThemeSelected(int pos) {
        final Theme theme = data.get(pos);
        saveThemeApplys(theme.getId());
        int preId = HSPreferenceHelper.getDefault().getInt(PREFS_SCREEN_FLASH_THEME_ID, Type.NONE);
        if (theme.getId() != preId) {
            HSGlobalNotificationCenter.sendNotification(ThemePreviewActivity.NOTIFY_THEME_SELECT);
            ScreenFlashSettings.putInt(PREFS_SCREEN_FLASH_THEME_ID, theme.getId());
            if (activity instanceof PopularThemeActivity) {
                HSBundle bundle = new HSBundle();
                bundle.putInt(NOTIFY_THEME_KEY, theme.getId());
                bundle.putObject(NOTIFY_CONTEXT_KEY, activity);
                HSGlobalNotificationCenter.sendNotification(NOTIFY_THEME_SELECT, bundle);
                LauncherAnalytics.logEvent("ColorPhone_BanboList_Set_Success", "type", theme.getIdName());
            } else if (activity instanceof ColorPhoneActivity) {
                LauncherAnalytics.logEvent("ColorPhone_MainView_Set_Success", "type", theme.getIdName());
            }

//            GuideApplyThemeActivity.start(activity, false, null);
            NotificationUtils.logThemeAppliedFlurry(data.get(pos));
            ColorPhoneApplication.getConfigLog().getEvent().onChooseTheme(
                    theme.getIdName().toLowerCase(),
                    ConfigLog.FROM_LIST);

            Threads.postOnThreadPoolExecutor(new Runnable() {
                @Override
                public void run() {
                    if (RingtoneHelper.isActive(theme.getId())) {
                        RingtoneHelper.setDefaultRingtone(theme);
                        ContactManager.getInstance().updateRingtoneOnTheme(theme, true);
                    } else {
                        RingtoneHelper.resetDefaultRingtone();
                    }
                }
            });
        }
    }

    public int getLastSelectedLayoutPos() {
        int prePos = 0;
        // Clear before.
        for (int i = 0; i < data.size(); i++) {
            Theme t = data.get(i);
            if (t.isSelected()) {
                prePos = i;
                break;
            }
        }
        return prePos + getHeaderCount();
    }

    private boolean selectTheme(final int pos, ThemeCardViewHolder holder, boolean showApplyClickAnim) {
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
            notifyItemSelected(prePos, t);
        }
        // Reset current.
        Theme selectedTheme = data.get(pos);
        selectedTheme.setSelected(true);
        notifyItemSelected(pos, selectedTheme, showApplyClickAnim);
        return true;
    }

    public void notifyItemSelected(int pos, Theme theme) {
        notifyItemSelected(pos, theme, false);
    }

    public void notifyItemSelected(int pos, Theme theme, boolean showApplyClickAnim) {
        int adapterPos = pos + getHeaderCount();
        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(adapterPos);
        if (holder == null) {
            // Item not visible in screen.
            notifyItemChanged(adapterPos);
        } else if (holder instanceof ThemeSelectorAdapter.ThemeCardViewHolder) {
            HSLog.d(TAG, "notifyItemSelected, setSelected ");
            ((ThemeSelectorAdapter.ThemeCardViewHolder) holder).setSelected(theme, showApplyClickAnim);
        }
    }

    @DebugLog
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (DEBUG_ADAPTER) {
            HSLog.d(TAG, "bindViewHolder : " + position);
        }
        if (holder instanceof ThemeCardViewHolder) {

            int themeIndex = position - getHeaderCount();

            ThemeCardViewHolder cardViewHolder = (ThemeCardViewHolder) holder;
            cardViewHolder.setPositionTag(themeIndex);

            if (themeIndex % 2 == 0) {
                cardViewHolder.getContentView().setTranslationX(mTransX);
            } else {
                cardViewHolder.getContentView().setTranslationX(-mTransX);
            }
            final Theme curTheme = data.get(themeIndex);

            cardViewHolder.mThemeTitle.setText(curTheme.getName());
            cardViewHolder.mAvatarName.setText(curTheme.getAvatarName());
            cardViewHolder.mCallActionView.setTheme(curTheme);
            cardViewHolder.updateTheme(curTheme);

            // Download progress
            final TasksManagerModel model = TasksManager.getImpl().getByThemeId(curTheme.getId());
            if (model != null) {
                cardViewHolder.update(model.getId(), themeIndex);
                final TasksManagerModel ringtoneModel = TasksManager.getImpl().getRingtoneTaskByThemeId(curTheme.getId());
                if (ringtoneModel != null) {
                    cardViewHolder.setRingtoneId(ringtoneModel.getId());
                }
                boolean fileExist = updateTaskHolder((ThemeCardViewHolder) holder, model);

                HSLog .d(TAG, "switchToReadyState" + " fileExist : " + fileExist + " " + curTheme.getIdName() + "isSelected" + curTheme.isSelected());

                cardViewHolder.switchToReadyState(fileExist, curTheme.isSelected());
            } else {
                cardViewHolder.switchToReadyState(true, curTheme.isSelected());
            }

            if (curTheme.isLocked()) {
                cardViewHolder.switchToLockState();
                cardViewHolder.mLockActionView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (activity instanceof ColorPhoneActivity) {
                            ((ColorPhoneActivity) activity).showRewardVideoView(curTheme.getName());
                        }
                        mUnLockThemeId = curTheme.getId();
                        LauncherAnalytics.logEvent("Colorphone_Theme_Unlock_Clicked", "from", "list", "themeName", curTheme.getName());
                    }
                });

            } else {
                cardViewHolder.mLockIcon.setVisibility(View.INVISIBLE);
            }

            if (!mIsApplyButtonVisible) {
                cardViewHolder.mThemeSelectedAnim.setVisibility(View.GONE);
                cardViewHolder.mLockIcon.setVisibility(View.GONE);
                cardViewHolder.mThemeSelectLayout.setVisibility(View.GONE);
                cardViewHolder.mDownloadViewContainer.setVisibility(View.GONE);
            }

            if (!mIsThemeInformationVisible) {
                cardViewHolder.mThemeLikeAnim.setVisibility(View.GONE);
                cardViewHolder.mThemeLikeCount.setVisibility(View.GONE);
                cardViewHolder.mThemeTitle.setVisibility(View.GONE);
            }


        } else if (holder instanceof StatementViewHolder) {
            HSLog.d("onBindVieHolder", "contains ads statement.");
        } else if (holder instanceof TopTipViewHolder) {

        } else if (holder instanceof HotThemeHolder) {

        }


    }

    @Override
    public int getItemViewType(int position) {
        int headerCount = getHeaderCount();
        if (position < headerCount) {
            if (position == 0 && isTipHeaderVisible()) {
                return THEME_SELECTOR_ITEM_TYPE_TIP;
            }
            return THEME_SELECTOR_ITEM_TYPE_HOT_THEME_HOLDER;
        }

        int dateIndex = position - headerCount;
        if (dateIndex < data.size()) {
            Theme theme = data.get(dateIndex);
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
        if (DEBUG_ADAPTER) {
            HSLog.d("SUNDXING", "bind modle Id : " + holder.id
                    + ", task is " + (task != null ? task : " null")
                    + ", tag = " + holder.toString());
        }
        if (task != null) {
            task.setTag(holder);
        }

        holder.setActionEnabled(true);
        boolean showOpen = false;

        if (TasksManager.getImpl().isReady()) {
            final int status = TasksManager.getImpl().getStatus(model.getId(), model.getPath());
            if (DEBUG_ADAPTER) {
                HSLog.d("sundxing", "position " + holder.position + ",download task status: " + status);
            }
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
        return data.size() + getHeaderCount() + getFooterCount();
    }

    private int getFooterCount() {
        // +1 for statement
        return 1;
    }

    private int getHeaderCount() {
        int count = 0;
        count += mTipHeaderVisible ? 1 : 0;
        count += mHotThemeHolderVisible ? 1 : 0;
        return count;
    }

    public static class ThemeCardViewHolder extends RecyclerView.ViewHolder implements DownloadHolder {

        private static final boolean DEBUG_PROGRESS = BuildConfig.DEBUG;
        private static int[] sThumbnailSize = Utils.getThumbnailImageSize();

        public interface DownloadedUpdateListener {
            void onUpdateDownloaded();

            void onStartDownload();
        }

        private AnimatorListenerAdapter applyClickAnimatorListenerAdapter = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mThemeSelectedAnim.playAnimation();
                mThemeSelectedAnim.setVisibility(View.VISIBLE);


                mApplyClickedAnim.setVisibility(View.GONE);

            }
        };

        ImageView mThemePreviewImg;
        ImageView mThemeLoadingImg;
        ImageView mAvatar;
        TextView mAvatarName;
        ImageView mAccept;
        ImageView mReject;
        TextView mThemeTitle;
        TextView mThemeLikeCount;
        TextView mApplyText;
        ThemePreviewWindow mThemeFlashPreviewWindow;
        InCallActionView mCallActionView;
        ViewGroup mLockActionView;
        ImageView mLockIcon;
        View mDownloadViewContainer;

        LottieAnimationView mThemeLikeAnim;
        LottieAnimationView mDownloadFinishedAnim;
        LottieAnimationView mThemeSelectedAnim;
        LottieAnimationView mApplyClickedAnim;
        View mThemeSelectLayout;
        DownloadedUpdateListener mDownloadedUpdateListener;

        DownloadViewHolder mDownloadViewHolder;

        private int mPositionTag;
        private View mContentView;
        private View mThemeHotMark;
        private View mRingtoneMark;

        // Indicates this holder has bound by Adapter. All Views has bounded data.
        // In case, we call start animation before ViewHolder bind.
        private boolean mHolderDataReady;

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

            mLockIcon = (ImageView) itemView.findViewById(R.id.lock_icon);

            mDownloadViewContainer = itemView.findViewById(R.id.download_view_container);

            mThemeFlashPreviewWindow = (ThemePreviewWindow) itemView.findViewById(R.id.card_flash_preview_window);
            mThemeFlashPreviewWindow.setPreviewType(ThemePreviewWindow.PreviewType.PREVIEW);
        }

        public void initChildView() {
            mCallActionView = (InCallActionView) itemView.findViewById(R.id.card_in_call_action_view);
            mLockActionView = itemView.findViewById(R.id.lock_action_view);
            mCallActionView.setAutoRun(false);
            if (TextUtils.equals(BuildConfig.FLAVOR, "colorflash")) {
                mCallActionView.setVisibility(View.INVISIBLE);
            }
            mAvatar = (ImageView) mContentView.findViewById(R.id.caller_avatar);
            mAvatarName = (TextView) mContentView.findViewById(R.id.first_line);
            mAccept = (ImageView) mContentView.findViewById(R.id.call_accept);
            mReject = (ImageView) mContentView.findViewById(R.id.call_reject);

            mRingtoneMark = itemView.findViewById(R.id.theme_ringtone_mark);
            mThemeHotMark = itemView.findViewById(R.id.theme_hot_mark);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mThemeHotMark.setTranslationX(pxFromDp(-1));
            }

            mDownloadFinishedAnim = (LottieAnimationView) itemView.findViewById(R.id.card_download_finished_anim);
            mDownloadFinishedAnim.setVisibility(View.GONE);

            mThemeSelectedAnim = (LottieAnimationView) itemView.findViewById(R.id.card_theme_selected_anim);
            mThemeSelectLayout = itemView.findViewById(R.id.card_theme_selected_layout);
            mThemeSelectedAnim.setVisibility(View.GONE);

            LottieAnimationView pb = itemView.findViewById(R.id.card_downloading_progress_bar);
            pb.setVisibility(View.GONE);

            mApplyClickedAnim = itemView.findViewById(R.id.card_apply_clicked);
            mApplyText = itemView.findViewById(R.id.apply_text);
            mDownloadViewHolder = new DownloadViewHolder(mApplyClickedAnim, pb, mDownloadFinishedAnim);


            mDownloadViewHolder.setStartAnim(mApplyClickedAnim);
            mDownloadViewHolder.setApplyText(mApplyText);
            mDownloadViewHolder.setProxyHolder(this);
            mDownloadTaskProgressBar = pb;
        }

        private void setSelected(Theme theme, boolean animation) {
            if (mApplyClickedAnim != null) {
                if (theme.isSelected()) {
                    if (animation) {
                        mApplyClickedAnim.removeAnimatorListener(applyClickAnimatorListenerAdapter);
                        mApplyClickedAnim.addAnimatorListener(applyClickAnimatorListenerAdapter);
                        mApplyClickedAnim.setVisibility(View.VISIBLE);
                        mApplyClickedAnim.playAnimation();

                        mApplyText.setAlpha(1f);
                        mApplyText.animate().alpha(0f).setDuration(100L).start();

                        HSLog.d(TAG, "AppClickedAnim play start" + theme.getIdName());
                    } else {
                        HSLog.d(TAG, "展示已经apply界面 : " + theme.getIdName());
                        mApplyClickedAnim.setVisibility(View.GONE);
                        mApplyText.setAlpha(0f);
                        mThemeSelectedAnim.setVisibility(View.VISIBLE);
                        setLottieProgress(mThemeSelectedAnim, 1f);
                    }
                } else {
                    mApplyClickedAnim.setProgress(0f);
                    mApplyClickedAnim.setVisibility(View.VISIBLE);
                    mApplyText.setAlpha(1f);

                    mDownloadFinishedAnim.setVisibility(View.GONE);
                    mThemeSelectedAnim.setVisibility(View.GONE);
                    mDownloadTaskProgressBar.setVisibility(View.GONE);
                    mThemeSelectedAnim.cancelAnimation();
                    setLottieProgress(mThemeSelectedAnim, 0f);
                }
            }

            if (theme.isSelected()) {
                HSLog.d(TAG, "selected : " + theme.getIdName());
                mThemeFlashPreviewWindow.playAnimation(theme);
                mThemeFlashPreviewWindow.setAutoRun(true);
                if (!TextUtils.equals(BuildConfig.FLAVOR, "colorflash")) {
                    mCallActionView.setAutoRun(true);
                }
            } else {

                HSLog.d(TAG, "取消 selected : " + theme.getIdName());
                mThemeFlashPreviewWindow.clearAnimation(theme);
                mThemeFlashPreviewWindow.setAutoRun(false);
                mCallActionView.setAutoRun(false);
                if (theme.isVideo()) {
                    getCoverView(theme).setVisibility(View.VISIBLE);
                }
            }
        }

        void setDownloadedUpdateListener(DownloadedUpdateListener listener) {
            this.mDownloadedUpdateListener = listener;
            mDownloadViewHolder.setDownloadUpdateListener(mDownloadedUpdateListener);
        }


        public ImageView getCoverView(final Theme theme) {
            return theme.isVideo() ? mThemeFlashPreviewWindow.getImageCover() : mThemePreviewImg;
        }

        @DebugLog
        public void updateTheme(final Theme theme) {
            if (theme.isMedia()) {
                ImageView targetView = getCoverView(theme);
                if (!theme.isSelected()) {
                    // Default theme media may load from local storage. Not show loading screen.
                    startLoadingScene();
                }
                GlideApp.with(mContentView).asBitmap()
                        .centerCrop()
                        .placeholder(theme.getThemePreviewDrawable())
                        .load(theme.getPreviewImage())
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .override(sThumbnailSize[0], sThumbnailSize[1])
                        .listener(new RequestListener<Bitmap>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                if (theme.isSelected()) {
                                    endLoadingScene(theme.isLocked());
                                }
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                endLoadingScene(theme.isLocked());
                                return false;
                            }
                        })
                        .into(targetView);
                HSLog.d(TAG, "load image size : " + sThumbnailSize[0] + ", " + sThumbnailSize[1]);

            } else {
                endLoadingScene(theme.isLocked());
            }

            if (theme.getId() != Type.TECH) {
                GlideApp.with(mContentView)
                        .load(theme.getAvatar())
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .transition(DrawableTransitionOptions.withCrossFade(200))
                        .into(mAvatar);
            }

            setSelected(theme, false);
            setHotBadge(theme.isHot());
            setRingtoneBadge(theme.hasRingtone());
            setLike(theme, false);
            mHolderDataReady = true;
        }

        private void startLoadingScene() {
            mThemeLoadingImg.setVisibility(View.VISIBLE);
            mCallActionView.setVisibility(View.INVISIBLE);
            mLockActionView.setVisibility(View.INVISIBLE);
            mThemeFlashPreviewWindow.getCallView().setVisibility(View.INVISIBLE);
        }

        private void endLoadingScene(boolean isCurrentThemeLocked) {
            mThemeLoadingImg.setVisibility(View.INVISIBLE);
            if (!TextUtils.equals(BuildConfig.FLAVOR, "colorflash")) {
                mCallActionView.setVisibility(View.VISIBLE);
                if (isCurrentThemeLocked) {
                    mCallActionView.setVisibility(View.INVISIBLE);
                    mLockActionView.setVisibility(View.VISIBLE);
                } else {
                    mCallActionView.setVisibility(View.VISIBLE);
                    mLockActionView.setVisibility(View.INVISIBLE);
                }
            }
            mThemeFlashPreviewWindow.getCallView().setVisibility(View.VISIBLE);
        }

        private void setHotBadge(boolean hot) {
            if (mThemeHotMark != null) {
                mThemeHotMark.setVisibility(hot ? View.VISIBLE : View.INVISIBLE);
            }
        }

        private void setRingtoneBadge(boolean hasRingtone) {
            if (mRingtoneMark != null) {
                mRingtoneMark.setVisibility(hasRingtone ? View.VISIBLE : View.INVISIBLE);
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

        private LottieAnimationView mDownloadTaskProgressBar;

        private Runnable mAniamtionEndStateRunnable = new Runnable() {
            @Override
            public void run() {

                // todo : isSelected 解决
                switchToReadyState(true, true);
            }
        };

        public void update(final int id, final int position) {
            this.id = id;
            this.position = position;
            this.mDownloadViewHolder.bindTaskId(id);
        }

        public void removeAnimationEndStateRunnable() {
            mDownloadTaskProgressBar.removeCallbacks(mAniamtionEndStateRunnable);
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

            if (mDownloadedUpdateListener != null) {
                mDownloadedUpdateListener.onUpdateDownloaded();
            }

        }

        @Override
        public void updateNotDownloaded(final int status, final long sofar, final long total) {

            if (DEBUG_PROGRESS) {
                HSLog.d("sundxing", position + " download stopped, status = " + status);
            }
            mDownloadViewHolder.updateNotDownloaded(status, sofar, total);
        }

        @Override
        public void updateDownloading(final int status, final long sofar, final long total) {
            if (DEBUG_PROGRESS) {
                final float percent = sofar
                        / (float) total;
                HSLog.d("sundxing", position + " download process, percent = " + percent);
            }
            if (sofar > 0L && sofar < total) {
                mApplyClickedAnim.setVisibility(View.GONE);
            }
            mApplyText.setAlpha(0f);
            mDownloadViewHolder.updateDownloading(status, sofar, total);
        }

        public void switchToReadyState(boolean ready, boolean isSelected) {
            mDownloadTaskProgressBar.setVisibility(View.GONE);
            mThemeSelectLayout.setVisibility(ready ? View.VISIBLE : View.GONE);
            if (ready) {
                mDownloadFinishedAnim.setVisibility(View.GONE);
            }
            mThemeSelectedAnim.setVisibility(View.GONE);

            if (ready && !isSelected) {
                mApplyClickedAnim.setVisibility(View.VISIBLE);
                mApplyText.setAlpha(1f);
            } else if (ready) {
                mThemeSelectedAnim.setVisibility(View.VISIBLE);
                mApplyClickedAnim.setVisibility(View.GONE);
                mApplyText.setAlpha(0f);
            } else {
                mApplyClickedAnim.setVisibility(View.VISIBLE);
                mApplyText.setAlpha(1f);
            }
        }

        public void switchToLockState() {
            mLockIcon.setVisibility(View.VISIBLE);
            mDownloadFinishedAnim.setVisibility(View.GONE);
            mDownloadTaskProgressBar.setVisibility(View.GONE);
            mThemeSelectLayout.setVisibility(View.GONE);
            mThemeSelectedAnim.setVisibility(View.GONE);
            mApplyClickedAnim.setVisibility(View.GONE);
            mApplyText.setAlpha(0f);
        }

        public DownloadViewHolder getDownloadHolder() {
            return mDownloadViewHolder;
        }

        public void setActionEnabled(boolean enable) {
            mDownloadTaskProgressBar.setEnabled(enable);
        }

        public void setRingtoneId(int id) {
            mDownloadViewHolder.bindRingtoneTaskId(id);
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

        public void stopAnimation() {
            mThemeFlashPreviewWindow.stopAnimations();
            mCallActionView.stopAnimations();
        }

        public void startAnimation() {
            if (mHolderDataReady) {
                mThemeFlashPreviewWindow.startAnimations();
                if (!TextUtils.equals(BuildConfig.FLAVOR, "colorflash")) {
                    mCallActionView.doAnimation();
                }
            }
        }
    }

    static class StatementViewHolder extends RecyclerView.ViewHolder {
        public StatementViewHolder(View itemView) {
            super(itemView);
        }
    }

    static class TopTipViewHolder extends RecyclerView.ViewHolder {

        public TopTipViewHolder(View itemView) {
            super(itemView);
        }
    }

    static class HotThemeHolder extends RecyclerView.ViewHolder {

        public HotThemeHolder(View itemView) {
            super(itemView);
        }
    }
}