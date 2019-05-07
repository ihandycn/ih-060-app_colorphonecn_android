package com.honeycomb.colorphone.themeselector;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.honeycomb.colorphone.Ap;
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
import com.honeycomb.colorphone.permission.PermissionChecker;
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
    private int mMaxShowThemeIndex = 2;
    private boolean isForeground;

    public static final int THEME_SELECTOR_ITEM_TYPE_THEME_GIF = 0x1;
    public static final int THEME_SELECTOR_ITEM_TYPE_THEME_VIDEO = 0x8;
    public static final int THEME_SELECTOR_ITEM_TYPE_THEME_LED = 0x2;
    public static final int THEME_SELECTOR_ITEM_TYPE_THEME_TECH = 0x3;
    public static final int THEME_SELECTOR_ITEM_TYPE_THEME_NONE = 0x4;


    public static final int THEME_SELECTOR_ITEM_TYPE_STATEMENT = 0x20;
    public static final int THEME_SELECTOR_ITEM_TYPE_TIP = 0x30;
    public static final int THEME_SELECTOR_ITEM_TYPE_HOT_THEME_HOLDER = 0x40;
    public static final int THEME_SELECTOR_ITEM_TYPE_POPULAR_THEME_HOLDER = 0x80;

    private static final int THEME_TYPE_MASK = 0x0F;

    private INotificationObserver observer = new INotificationObserver() {
        @Override
        public void onReceive(String s, HSBundle hsBundle) {
            if (ThemePreviewActivity.NOTIFY_THEME_DOWNLOAD.equals(s)) {
                if (hsBundle != null) {
                    notifyItemChanged(getAdapterPos(hsBundle));
                }
            } else if (ThemePreviewActivity.NOTIFY_THEME_SELECT.equals(s)) {
                // TODO unclear
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
                    case THEME_SELECTOR_ITEM_TYPE_POPULAR_THEME_HOLDER:
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

    @Deprecated
    public void updateApplyInformationAutoPilotValue() {

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

                case THEME_SELECTOR_ITEM_TYPE_THEME_NONE:
                    holder.mThemeFlashPreviewWindow.updateThemeLayout(getTypeByThemeId(Type.NONE));
                    holder.mThemePreviewImg.setBackgroundResource(R.drawable.card_bg_round_dark);
                    holder.mThemeFlashPreviewWindow.findViewById(R.id.acb_phone_none_system).setVisibility(View.GONE);

                    break;
            }

            holder.initChildView();

            cardViewContent.findViewById(R.id.card_view).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final int pos = holder.getPositionTag();

                    Theme theme = data.get(pos);
                    if (activity instanceof PopularThemeActivity) {
                        LauncherAnalytics.logEvent("ColorPhone_BanboList_ThemeDetail_View", "type", theme.getIdName());
                        PopularThemePreviewActivity.start(activity, pos);
                    } else {
                        LauncherAnalytics.logEvent("ColorPhone_MainView_ThemeDetail_View", "type", theme.getIdName());
                        ThemePreviewActivity.start(activity, pos);
                    }
                }
            });

            if (Ap.DetailAd.enableMainViewDownloadButton()) {
                holder.getThemeSelectedView().setOnClickListener(v -> onClickApply(holder));
            }

            holder.setDownloadedUpdateListener(new ThemeCardViewHolder.DownloadedUpdateListener() {
                @Override
                public void onUpdateDownloaded() {
                    if (recyclerView.isComputingLayout()) {
                        return;
                    }
                    int pos = holder.getPositionTag();
                    final Theme theme = data.get(pos);

                    if (theme.isPendingSelected()) {
                        if (selectTheme(pos, holder, false)) {
                            onThemeSelected(pos);
                        }
                    } else {
                        if (!theme.isSelected()) {
                            holder.removeAnimationEndStateRunnable();
                            holder.switchToReadyState(true, false);
                        }
                        notifyItemSelected(pos, theme, false);
                    }
                }

                @Override
                public void onStartDownload() {

                }

                @Override
                public void onApplyClick() {

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
                holder.mThemeTitle.setTextColor(0xFFffffff);
                holder.mThemeStatusView.setPopularStyle();

            }
            return holder;

        } else if (viewType == THEME_SELECTOR_ITEM_TYPE_STATEMENT) {
            View stateViewContent = activity.getLayoutInflater().inflate(R.layout.card_view_contains_ads_statement, null);
            return new StatementViewHolder(stateViewContent);
        } else if (viewType == THEME_SELECTOR_ITEM_TYPE_TIP) {
            View tipView = activity.getLayoutInflater().inflate(R.layout.notification_access_toast_layout, parent, false);
            tipView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PermissionChecker.getInstance().checkForcely(activity, "Banner");
                }
            });
            return new TopTipViewHolder(tipView);
        } else if (viewType == THEME_SELECTOR_ITEM_TYPE_HOT_THEME_HOLDER) {
            View view = activity.getLayoutInflater().inflate(R.layout.acb_layout_hot_theme_view, parent, false);

            final ImageView target = view.findViewById(R.id.hot_theme_image);

            int w = Dimensions.getPhoneWidth(activity) - Dimensions.pxFromDp(40f);
            int h = (int) (w * (134f / 328f));
            GlideApp.with(activity)
                    .load(HSConfig.optString("#7641DB", "Application", "Special", "SpecialThumbnail"))
                    .fitCenter()
                    .apply(new RequestOptions().transform(new RoundedCorners(Dimensions.pxFromDp(5f))))
                    .override(w, h)
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
        } else if (viewType == THEME_SELECTOR_ITEM_TYPE_POPULAR_THEME_HOLDER) {
            View view = activity.getLayoutInflater().inflate(R.layout.acb_item_popular_theme_bg, parent, false);
            ImageView imageBg = view.findViewById(R.id.image_bg);
            String bgUrl = HSConfig.optString("", "Application", "Special", "SpecialBg");
            String bgColor = HSConfig.optString("#7641DB", "Application", "Special", "SpecialColor");
            GlideApp.with(activity).load(bgUrl)
                    .placeholder(new ColorDrawable(Color.parseColor(bgColor)))
                    .centerCrop().into(imageBg);

            return new PopularThemeBgHolder(view);
        } else {
            throw new IllegalStateException("error viewtype");
        }
    }

    private void onClickApply(ThemeCardViewHolder holder) {
        HSLog.d(TAG, "Apply click");
        int pos = holder.getPositionTag();
        Theme theme = data.get(pos);

        // Check if need download first.
        boolean startDownload = holder.startDownload();
        if (startDownload) {
            theme.setPendingSelected(true);
            PermissionChecker.getInstance().check((Activity) activity, "List");
        } else {
            if (selectTheme(pos, holder, true)) {
                onThemeSelected(pos);
                PermissionChecker.getInstance().check(activity, "SetForAll");
            }
        }

        // LOG
        if (activity instanceof ColorPhoneActivity) {
            LauncherAnalytics.logEvent("ColorPhone_MainView_Apply_Icon_Clicked", "type", theme.getIdName());
        } else if (activity instanceof PopularThemeActivity) {
            LauncherAnalytics.logEvent("ColorPhone_BanboList_Apply_icon_Clicked", "type", theme.getIdName());
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
        int prePos = -1;
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

            int index = themeIndex / 2;
            if (index > mMaxShowThemeIndex) {
                mMaxShowThemeIndex = index;
                LauncherAnalytics.logEventAndFirebase("ColorPhone_Mainview_Slide");
            }

            if (activity instanceof PopularThemeActivity) {
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                if (position == 1 || position == 2) {
                    lp.setMargins(0, -Dimensions.pxFromDp(120f), 0, 0);
                    cardViewHolder.itemView.setLayoutParams(lp);
                } else {
                    lp.setMargins(0, 0, 0, 0);
                    cardViewHolder.itemView.setLayoutParams(lp);
                }
            }

            final Theme curTheme = data.get(themeIndex);

            // Download progress
            final TasksManagerModel model = TasksManager.getImpl().getByThemeId(curTheme.getId());
            boolean fileExist = true;
            if (model != null) {
                cardViewHolder.update(model.getId(), themeIndex);
                final TasksManagerModel ringtoneModel = TasksManager.getImpl().getRingtoneTaskByThemeId(curTheme.getId());
                if (ringtoneModel != null) {
                    cardViewHolder.setRingtoneId(ringtoneModel.getId());
                }
                fileExist = updateTaskHolder((ThemeCardViewHolder) holder, model);

                HSLog.d(TAG, "switchToReadyState" + " " +
                        "fileExist : " + fileExist + " " + curTheme.getIdName() + "，isSelected ： " + curTheme.isSelected());

            }

            // CardView
            if (!fileExist && curTheme.isSelected()) {
                HSLog.d(TAG, "selected theme start downloading : " + curTheme.getIdName());
                curTheme.setSelected(false);
                curTheme.setPendingSelected(true);
                cardViewHolder.startDownload();
            }

            if (fileExist && curTheme.isPendingSelected()) {
                curTheme.setSelected(true);
                curTheme.setPendingSelected(false);
            }

            cardViewHolder.updateTheme(curTheme, fileExist);

            // Update lock status
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
                cardViewHolder.mLockIcon.setVisibility(View.GONE);
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
            if (activity instanceof PopularThemeActivity) {
                return THEME_SELECTOR_ITEM_TYPE_POPULAR_THEME_HOLDER;
            }

            if (position == 0 && isTipHeaderVisible()) {
                return THEME_SELECTOR_ITEM_TYPE_TIP;
            }
            return THEME_SELECTOR_ITEM_TYPE_HOT_THEME_HOLDER;
        }

        int dateIndex = position - headerCount;
        if (dateIndex < data.size()) {
            Theme theme = data.get(dateIndex);
            if (theme.getValue() == Type.NONE) {
                return THEME_SELECTOR_ITEM_TYPE_THEME_NONE;
            } else if (theme.getValue() == Type.LED) {
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
            HSLog.d(ThemeSelectorAdapter.TAG, "TasksManager not ready");
            holder.setActionEnabled(false);
        }

        return showOpen;
    }

    @Override
    public int getItemCount() {
        return data.size() + getHeaderCount() + getFooterCount();
    }

    public void resetShownCount() {
        mMaxShowThemeIndex = 2;
    }

    private int getFooterCount() {
        // +1 for statement
        return 1;
    }

    private int getHeaderCount() {
        if (activity instanceof PopularThemeActivity) {
            return 1;
        }
        int count = 0;
        count += mTipHeaderVisible ? 1 : 0;
        count += mHotThemeHolderVisible ? 1 : 0;
        return count;
    }

    public void markForeground(boolean foreground) {
        isForeground = foreground;
    }

    public static class ThemeCardViewHolder extends RecyclerView.ViewHolder implements DownloadHolder {

        private static final boolean DEBUG_PROGRESS = BuildConfig.DEBUG;
        private static int[] sThumbnailSize = Utils.getThumbnailImageSize();


        public interface DownloadedUpdateListener {
            void onUpdateDownloaded();

            void onStartDownload();

            void onApplyClick();
        }

        // TODO remove
        private boolean mIsDownloading;

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
        ViewGroup mLockActionView;
        ImageView mLockIcon;
        View mActionViewContainer;

        ThemeStatusView mThemeStatusView;

        LottieAnimationView mThemeLikeAnim;

        DownloadedUpdateListener mDownloadedUpdateListener;


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

        public View getCardView() {
            return mContentView.findViewById(R.id.card_view);
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

            mActionViewContainer = itemView.findViewById(R.id.action_view_container);

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

            mThemeStatusView = new ThemeStatusView(itemView, this);
        }

        public boolean startDownload() {
            if (getDownloadHolder().canStartDownload()) {
                getDownloadHolder().startDownload(true);
                return true;
            }
            return false;
        }


        public View getThemeSelectedView() {
            return mThemeStatusView.getApplyButton();
        }

        // TODO outer class
        public static class ThemeStatusView {

            public static int STATUS_INIT = 1;
            public static int STATUS_DOWNLOADING = 2;
            public static int STATUS_FILE_READY = 3;
            public static int STATUS_SELECTED = 4;

            public static int ACTION_DOWNLOAD = 1;
            public static int ACTION_APPLY = 2;
            public static int ACTION_UNSELECTED = 3;

            private LottieAnimationView mApplyClickedAnim;
            private TextView mApplyText;

            private LottieAnimationView mDownloadTaskProgressBar;
            private LottieAnimationView mDownloadFinishedAnim;
            private DownloadViewHolder mDownloadViewHolder;

            private LottieAnimationView mThemeSelectedAnim;

            private int status = STATUS_INIT;

            private AnimatorListenerAdapter applyClickAnimatorListenerAdapter = new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    onApplyAnimEnd();
                }
            };

            public ThemeStatusView(View rootView, DownloadHolder downloadHolder) {

                View itemView = rootView;
                mDownloadFinishedAnim = (LottieAnimationView) itemView.findViewById(R.id.card_download_finished_anim);
                mDownloadFinishedAnim.setVisibility(View.GONE);

                mThemeSelectedAnim = (LottieAnimationView) itemView.findViewById(R.id.card_theme_selected_anim);
                mThemeSelectedAnim.setVisibility(View.VISIBLE);

                mDownloadTaskProgressBar = itemView.findViewById(R.id.card_downloading_progress_bar);
                mDownloadTaskProgressBar.setVisibility(View.GONE);

                mApplyText = itemView.findViewById(R.id.apply_text);
                mApplyClickedAnim = itemView.findViewById(R.id.card_apply_clicked);

                mDownloadViewHolder = new DownloadViewHolder(mDownloadTaskProgressBar, mDownloadFinishedAnim);
                mDownloadViewHolder.setStartAnim(mApplyClickedAnim);
                mDownloadViewHolder.setApplyText(mApplyText);
                mDownloadViewHolder.setProxyHolder(downloadHolder);
            }

            public void onApplyAnimEnd() {
                mThemeSelectedAnim.playAnimation();
                mThemeSelectedAnim.setVisibility(View.VISIBLE);
                mApplyClickedAnim.setVisibility(View.GONE);
            }

            public void setPopularStyle() {
                mApplyText.setTextColor(0xFFffffff);
                mDownloadFinishedAnim.setAnimation("lottie/white/theme_downloaded.json");
                mApplyClickedAnim.setAnimation("lottie/white/theme_apply_clicked.json");
                mThemeSelectedAnim.setAnimation("lottie/white/theme_downloaded.json");
                mDownloadTaskProgressBar.setAnimation("lottie/white/theme_progress.json");
            }

            public void setSelected(Theme theme, boolean isDownloading, boolean animation) {
                boolean buttonEnabled = Ap.DetailAd.enableMainViewDownloadButton();

                if (mApplyClickedAnim != null) {
                    if (theme.isSelected()) {
                        if (animation) {
                            mThemeSelectedAnim.setVisibility(View.INVISIBLE);

                            mApplyClickedAnim.removeAnimatorListener(applyClickAnimatorListenerAdapter);
                            mApplyClickedAnim.addAnimatorListener(applyClickAnimatorListenerAdapter);
                            mApplyClickedAnim.setVisibility(View.VISIBLE);
                            mApplyClickedAnim.playAnimation();

                            mApplyText.setAlpha(1f);
                            mApplyText.animate().alpha(0f).setDuration(100L).start();
                            HSLog.d(TAG, "AppClickedAnim play start : " + theme.getIdName());
                        } else {
                            HSLog.d(TAG, "展示已经apply界面 : " + theme.getIdName());
                            mApplyClickedAnim.setVisibility(View.GONE);
                            mThemeSelectedAnim.setVisibility(View.VISIBLE);
                            mApplyText.setAlpha(0f);
                            setLottieProgress(mThemeSelectedAnim, 1f);
                        }
                    } else {
                        // TODO: 2018/9/18 判断是否文件 ready 即可
                        if (!isDownloading) {
                            if (buttonEnabled) {
                                mApplyClickedAnim.setProgress(0f);
                                mApplyClickedAnim.setVisibility(View.VISIBLE);
                                mApplyText.setAlpha(1f);
                            } else {
                                mApplyClickedAnim.setVisibility(View.GONE);
                                mApplyText.setAlpha(0f);
                            }
                            mDownloadTaskProgressBar.setVisibility(View.GONE);
                        }

                        mDownloadFinishedAnim.setVisibility(View.GONE);
                        mThemeSelectedAnim.setVisibility(View.GONE);
                        mThemeSelectedAnim.cancelAnimation();
                        setLottieProgress(mThemeSelectedAnim, 0f);
                    }
                }
            }

            public void switchToReadyState(boolean ready, boolean isSelected) {
                mDownloadTaskProgressBar.setVisibility(View.GONE);

                boolean enableActionButton = Ap.DetailAd.enableMainViewDownloadButton();
                boolean canDownload = !ready && enableActionButton;
                if (!canDownload) {
                    mDownloadFinishedAnim.setVisibility(View.GONE);
                }
                boolean showSelected = ready && isSelected ;
                if (showSelected) {
                    mThemeSelectedAnim.setVisibility(View.VISIBLE);
                }

                if (!ready) {
                    mThemeSelectedAnim.setVisibility(View.GONE);
                }

                boolean  canApply = enableActionButton && (!ready || !isSelected);
                mApplyClickedAnim.setVisibility(canApply ? View.VISIBLE : View.GONE);
                mApplyText.setAlpha(canApply ? 1f : 0f);
            }

            public void hideAll() {
                mDownloadFinishedAnim.setVisibility(View.GONE);
                mDownloadTaskProgressBar.setVisibility(View.GONE);
                mThemeSelectedAnim.setVisibility(View.GONE);
                mApplyClickedAnim.setVisibility(View.GONE);
                mApplyText.setAlpha(0f);
            }

            public void updateDownloading(final int status, final long sofar, final long total) {
                if (sofar > 0L && sofar < total) {
                    mApplyClickedAnim.setVisibility(View.GONE);
                }
                mApplyText.setAlpha(0f);
                mDownloadViewHolder.updateDownloading(status, sofar, total);
            }

            public View getApplyButton() {
                 return mApplyClickedAnim;
            }
        }

        private void setSelected(Theme theme, boolean animation) {
            mThemeStatusView.setSelected(theme, mIsDownloading, animation);

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
            mThemeStatusView.mDownloadViewHolder.setDownloadUpdateListener(mDownloadedUpdateListener);
        }

        public ImageView getCoverView(final Theme theme) {
            return theme.isVideo() ? mThemeFlashPreviewWindow.getImageCover() : mThemePreviewImg;
        }

        @DebugLog
        public void updateTheme(final Theme theme, boolean fileExist) {
            mThemeTitle.setText(theme.getName());
            mAvatarName.setText(theme.getAvatarName());
            mCallActionView.setTheme(theme);

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
            switchToReadyState(fileExist, theme.isSelected());
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
            this.mThemeStatusView.mDownloadViewHolder.bindTaskId(id);
        }

        // TODO
        public void removeAnimationEndStateRunnable() {
            mThemeStatusView.mDownloadTaskProgressBar.removeCallbacks(mAniamtionEndStateRunnable);
        }

        @Override
        public void updateDownloaded(final boolean progressFlag) {
            // If file already downloaded, not play animation
            mThemeStatusView.mDownloadViewHolder.updateDownloaded(progressFlag);
            mThemeStatusView.mDownloadTaskProgressBar.removeCallbacks(mAniamtionEndStateRunnable);
            if (progressFlag) {
                mThemeStatusView.mDownloadTaskProgressBar.postDelayed(mAniamtionEndStateRunnable, 600);
            }
            if (DEBUG_PROGRESS) {
                HSLog.d("sundxing", position + " download success!");
            }

            if (mDownloadedUpdateListener != null) {
                mDownloadedUpdateListener.onUpdateDownloaded();
            }

            mIsDownloading = false;
        }

        @Override
        public void updateNotDownloaded(final int status, final long sofar, final long total) {

            if (DEBUG_PROGRESS) {
                HSLog.d("sundxing", position + " download stopped, status = " + status);
            }
            mThemeStatusView.mDownloadViewHolder.updateNotDownloaded(status, sofar, total);
            mIsDownloading = false;
        }

        @Override
        public void updateDownloading(final int status, final long sofar, final long total) {
            if (DEBUG_PROGRESS) {
                final float percent = sofar
                        / (float) total;
                HSLog.d("sundxing", position + " download process, percent = " + percent);
            }
            if (sofar > 0L && sofar < total) {
                mIsDownloading = true;
            } else {
                mIsDownloading = false;
            }
            mThemeStatusView.updateDownloading(status, sofar, total);
        }

        public void switchToReadyState(boolean ready, boolean isSelected) {

            mThemeStatusView.switchToReadyState(ready, isSelected);
        }

        public void switchToLockState() {
            mLockIcon.setVisibility(View.VISIBLE);
            mThemeStatusView.hideAll();
        }

        public DownloadViewHolder getDownloadHolder() {
            return mThemeStatusView.mDownloadViewHolder;
        }

        public void setActionEnabled(boolean enable) {
            mThemeStatusView.mDownloadTaskProgressBar.setEnabled(enable);
        }

        public void setRingtoneId(int id) {
            mThemeStatusView.mDownloadViewHolder.bindRingtoneTaskId(id);
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

        private static void setLottieProgress(LottieAnimationView animationView, float v) {
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

    static class PopularThemeBgHolder extends RecyclerView.ViewHolder {

        public PopularThemeBgHolder(View itemView) {
            super(itemView);
        }
    }
}