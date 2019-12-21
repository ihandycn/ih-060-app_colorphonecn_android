package com.honeycomb.colorphone.themeselector;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.honeycomb.colorphone.WatchedScrollListener;
import com.honeycomb.colorphone.activity.ColorPhoneActivity;
import com.honeycomb.colorphone.activity.PopularThemeActivity;
import com.honeycomb.colorphone.activity.PopularThemePreviewActivity;
import com.honeycomb.colorphone.activity.ThemePreviewActivity;
import com.honeycomb.colorphone.autopermission.RuntimePermissionActivity;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.TransitionUtil;
import com.honeycomb.colorphone.util.Utils;
import com.honeycomb.colorphone.view.GlideApp;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;

import java.util.ArrayList;

import hugo.weaving.DebugLog;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.acb.utils.Utils.getTypeByThemeId;
import static com.honeycomb.colorphone.activity.ThemePreviewActivity.NOTIFY_CONTEXT_KEY;

public class ThemeSelectorAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "ThemeSelectorAdapter";
    private static final boolean DEBUG_ADAPTER = BuildConfig.DEBUG;
    private final Activity activity;
    private int pageIndex;
    private RecyclerView recyclerView;

    private ArrayList<Theme> data = null;
    private GridLayoutManager layoutManager;
    private boolean mTipHeaderVisible;
    private boolean mHotThemeHolderVisible;
    private int mMaxShowThemeIndex = 2;

    public static final int THEME_SELECTOR_ITEM_TYPE_THEME_GIF = 0x1;
    public static final int THEME_SELECTOR_ITEM_TYPE_THEME_VIDEO = 0x8;
    // TODO remove
    public static final int THEME_SELECTOR_ITEM_TYPE_THEME_LED = 0x2;
    // TODO remove
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
            if (pageIndex != ((ColorPhoneActivity) activity).mainPagerPosition){
                return;
            }
                if (ThemePreviewActivity.NOTIFY_THEME_DOWNLOAD.equals(s)) {
                    if (hsBundle != null) {
                        notifyItemChanged(getAdapterPos(hsBundle));
                    }
                } else if (ThemePreviewActivity.NOTIFY_THEME_SELECT.equals(s)) {
                    // TODO unclear
                    if (hsBundle != null) {
                        int pos = getDataPos(hsBundle);
                        if (data != null && data.size() > pos) {
                            Theme selectedTheme = data.get(pos);

                            if (!selectTheme(pos)) {
                                if (!activity.equals(hsBundle.getObject(NOTIFY_CONTEXT_KEY))) {
                                    notifyDataSetChanged();
                                }
                            }

                            ColorPhoneApplication.getConfigLog().getEvent().onChooseTheme(
                                    selectedTheme.getIdName().toLowerCase(),
                                    ConfigLog.FROM_DETAIL);
                        }
                    }
                } else if (ColorPhoneActivity.NOTIFICATION_ON_REWARDED.equals(s)) {
                    if (hsBundle != null) {
                        notifyItemChanged(unlockThemeAndGetAdapterPos(hsBundle));
                    }
                } else if (ThemePreviewActivity.NOTIFY_LIKE_COUNT_CHANGE.equals(s)) {
                    if (hsBundle != null) {
                        int pos = getDataPos(hsBundle);
                        Theme theme = data.get(pos);
                        int adapterPos = themePositionToAdapterPosition(pos);
                        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(adapterPos);

                        if (holder instanceof ThemeCardViewHolder) {
                            ThemeCardViewHolder cardViewHolder = (ThemeCardViewHolder) holder;
                            cardViewHolder.mThemeLikeCount.setText(String.valueOf(theme.getDownload()));
                            if (theme.isLike()) {
                                cardViewHolder.mThemeLikeAnim.setProgress(1f);
                            } else {
                                cardViewHolder.mThemeLikeAnim.setProgress(0f);
                            }
                        }

                    }
                } else if (ThemePreviewActivity.NOTIFY_THEME_UPLOAD_SELECT.equals(s) || ThemePreviewActivity.NOTIFY_THEME_PUBLISH_SELECT.equals(s)) {
                    //user selected theme on the upload page, so delete current theme tip of home page
                    if (data != null && data.size() > 0) {
                        int prePos = -1;
                        for (int i = 0; i < data.size(); i++) {
                            Theme t = data.get(i);
                            if (t.isSelected()) {
                                prePos = i;
                                break;
                            }
                        }
                        if (prePos != -1) {
                            Theme t = data.get(prePos);
                            t.setSelected(false);
                            notifyItemSelected(prePos, t);
                        }
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
            return themePositionToAdapterPosition(getDataPos(hsBundle));
        }

        private int unlockThemeAndGetAdapterPos(HSBundle hsBundle) {
            return themePositionToAdapterPosition(unlockThemeAndGetDatePos(hsBundle));
        }

    };

    public ThemeSelectorAdapter(Activity activity, final ArrayList<Theme> data, int pageIndex) {
        this.activity = activity;
        this.data = data;
        this.pageIndex = pageIndex;
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

    }

    public void setData(ArrayList<Theme> data, int pageIndex) {
        this.data = data;
        this.pageIndex = pageIndex;
    }

    public void setHeaderTipVisible(boolean visible) {
        mTipHeaderVisible = visible;
    }

    public boolean isTipHeaderVisible() {
        return mTipHeaderVisible;
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
        recyclerView.addOnScrollListener(new WatchedScrollListener());

        HSGlobalNotificationCenter.addObserver(ThemePreviewActivity.NOTIFY_THEME_SELECT, observer);
        HSGlobalNotificationCenter.addObserver(ThemePreviewActivity.NOTIFY_THEME_PUBLISH_SELECT, observer);
        HSGlobalNotificationCenter.addObserver(ThemePreviewActivity.NOTIFY_THEME_UPLOAD_SELECT, observer);
        HSGlobalNotificationCenter.addObserver(ThemePreviewActivity.NOTIFY_LIKE_COUNT_CHANGE, observer);

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
            View cardViewContent = activity.getLayoutInflater().inflate(R.layout.card_view_theme_selector, parent, false);

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
            View cardView = cardViewContent.findViewById(R.id.card_view);
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onCardClick(holder, view);
                }
            });
            cardView.setOnTouchListener(new ScaleUpTouchListener());

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

            }
            return holder;

        } else if (viewType == THEME_SELECTOR_ITEM_TYPE_STATEMENT) {
            View stateViewContent = activity.getLayoutInflater().inflate(R.layout.card_view_contains_ads_statement, null);
            return new StatementViewHolder(stateViewContent);
        } else if (viewType == THEME_SELECTOR_ITEM_TYPE_TIP) {
            View tipView = activity.getLayoutInflater().inflate(R.layout.notification_access_toast_layout, parent, false);
            tipView.setBackground(BackgroundDrawables.createBackgroundDrawable(0xff000000, Dimensions.pxFromDp(27), false));
            tipView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Navigations.startActivitySafely(v.getContext(), RuntimePermissionActivity.class);
                    Analytics.logEvent("List_Page_Permission_Alert_Click");
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
                    Analytics.logEvent("ColorPhone_MainView_BanboEntrance_Clicked");
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

    private void onCardClick(ThemeCardViewHolder holder, View view) {
        final int pos = holder.getPositionTag();
        Theme theme = data.get(pos);
        if (activity instanceof PopularThemeActivity) {
            Analytics.logEvent("ColorPhone_BanboList_ThemeDetail_View", "type", theme.getIdName());
            PopularThemePreviewActivity.start(activity, pos);
        } else {
            Analytics.logEvent("MainView_ThemeDetail_View", "type", theme.getIdName());
            ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(activity,
                            Pair.create(holder.mThemePreviewImg, TransitionUtil.getViewTransitionName(TransitionUtil.TAG_PREVIEW_IMAGE, theme))
                    );
            ColorPhoneActivity colorPhoneActivity = (ColorPhoneActivity) activity;
            if (!colorPhoneActivity.isRefreshing()&&colorPhoneActivity.getCategoryList() != null && colorPhoneActivity.getCategoryList().get(colorPhoneActivity.mainPagerPosition) != null) {
                ThemePreviewActivity.start(activity, pos, "main", colorPhoneActivity.getCategoryList().get(colorPhoneActivity.mainPagerPosition).getId(), activityOptionsCompat.toBundle());
            }
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
        return themePositionToAdapterPosition(prePos);
    }

    private boolean selectTheme(final int pos) {
        int prePos = -1;
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
            if (prePos != -1) {
                Theme t = data.get(prePos);
                t.setSelected(false);
                notifyItemSelected(prePos, t);
            }
        }
        // Reset current.
        Theme selectedTheme = data.get(pos);
        selectedTheme.setSelected(true);
        notifyItemSelected(pos, selectedTheme);
        return true;
    }

    public void notifyItemSelected(int pos, Theme theme) {
        int adapterPos = themePositionToAdapterPosition(pos);
        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(adapterPos);
        if (holder == null) {
            // Item not visible in screen.
            notifyItemChanged(adapterPos);
        } else if (holder instanceof ThemeSelectorAdapter.ThemeCardViewHolder) {
            HSLog.d(TAG, "notifyItemSelected, setSelected ");
            ((ThemeSelectorAdapter.ThemeCardViewHolder) holder).setSelected(theme);
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


            int index = themeIndex / 2;
            if (index > mMaxShowThemeIndex) {
                mMaxShowThemeIndex = index;
                Analytics.logEvent("ColorPhone_Mainview_Slide");
                if (activity instanceof ColorPhoneActivity) {
                    ColorPhoneActivity colorPhoneActivity = (ColorPhoneActivity) activity;
                    Analytics.logEvent("ThemeCategory_Page_Slide", "Category", colorPhoneActivity.getCategoryList().get(colorPhoneActivity.mainPagerPosition).getName());
                }
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

            cardViewHolder.updateTheme(curTheme, true);

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

    public int themePositionToAdapterPosition(int themePos) {
        return themePos + getHeaderCount();
    }

    public static class ThemeCardViewHolder extends RecyclerView.ViewHolder {

        private static int[] sThumbnailSize = Utils.getThumbnailImageSize();

        ImageView mThemePreviewImg;
        ImageView mThemeLoadingImg;
        ImageView mAvatar;

        TextView mThemeTitle;
        TextView mThemeLikeCount;
        ThemePreviewWindow mThemeFlashPreviewWindow;
        InCallActionView mCallActionView;
        ViewGroup mLockActionView;

        ThemeStatusView mThemeStatusView;

        LottieAnimationView mThemeLikeAnim;

        private int mPositionTag;
        private View mContentView;
        private View mThemeHotMark;
        private View mRingtoneMark;

        // Indicates this holder has bound by Adapter. All Views has bounded data.
        // In case, we call start animation before ViewHolder bind.
        private boolean mHolderDataReady;
        private View mButtonHolderForTrans;

        public void setPositionTag(int position) {
            mPositionTag = position;
        }

        public int getPositionTag() {
            return mPositionTag;
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
            mThemeFlashPreviewWindow.setPreviewType(ThemePreviewWindow.PreviewType.PREVIEW);
        }

        public void initChildView() {
            mLockActionView = itemView.findViewById(R.id.lock_action_view);
            mAvatar = (ImageView) mContentView.findViewById(R.id.caller_avatar);
            mRingtoneMark = itemView.findViewById(R.id.theme_ringtone_mark);
            mThemeHotMark = itemView.findViewById(R.id.theme_hot_mark);
            mButtonHolderForTrans = itemView.findViewById(R.id.button_transition_element);
            mThemeStatusView = new ThemeStatusView(itemView);
        }

        // TODO outer class
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
                if (mCallActionView != null) {
                    mCallActionView.setAutoRun(true);
                }
            } else {
                HSLog.d(TAG, "取消 selected : " + theme.getIdName());
                mThemeFlashPreviewWindow.clearAnimation(theme);
                mThemeFlashPreviewWindow.setAutoRun(false);
                if (mCallActionView != null) {
                    mCallActionView.setAutoRun(false);
                }
                if (theme.isVideo()) {
                    getCoverView(theme).setVisibility(View.VISIBLE);
                }
            }
        }


        public ImageView getCoverView(final Theme theme) {
            return theme.isVideo() ? mThemeFlashPreviewWindow.getImageCover() : mThemePreviewImg;
        }

        @DebugLog
        public void updateTheme(final Theme theme, boolean fileExist) {
            mThemeTitle.setText(theme.getName());

            if (mCallActionView != null) {
                mCallActionView.setTheme(theme);
            }
            ViewCompat.setTransitionName(mThemePreviewImg, TransitionUtil.getViewTransitionName(TransitionUtil.TAG_PREVIEW_IMAGE, theme));
            ViewCompat.setTransitionName(mRingtoneMark, TransitionUtil.getViewTransitionName(TransitionUtil.TAG_PREIVIEW_RINTONE, theme));

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

            if (theme.getId() != Type.TECH &&
                    mAvatar != null) {
                GlideApp.with(mContentView)
                        .load(theme.getAvatar())
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .transition(DrawableTransitionOptions.withCrossFade(200))
                        .into(mAvatar);
            }

            setSelected(theme);
            switchToReadyState(fileExist, theme.isSelected());
            setHotBadge(theme.isHot());
            setRingtoneBadge(theme.hasRingtone());
            setLike(theme, false);
            mHolderDataReady = true;
        }

        private void startLoadingScene() {
            mThemeLoadingImg.setVisibility(View.VISIBLE);
            setVisibleSafely(mCallActionView, INVISIBLE);
            mLockActionView.setVisibility(View.INVISIBLE);
            setVisibleSafely(mThemeFlashPreviewWindow.getCallView(), View.INVISIBLE);
        }

        private void setVisibleSafely(View view, int visible) {
            if (view != null) {
                view.setVisibility(visible);
            }
        }

        private void endLoadingScene(boolean isCurrentThemeLocked) {
            mThemeLoadingImg.setVisibility(View.INVISIBLE);
            if (!TextUtils.equals(BuildConfig.FLAVOR, "colorflash")) {
                setVisibleSafely(mCallActionView, VISIBLE);
                if (isCurrentThemeLocked) {
                    setVisibleSafely(mCallActionView, INVISIBLE);
                    mLockActionView.setVisibility(View.VISIBLE);
                } else {
                    setVisibleSafely(mCallActionView, VISIBLE);
                    mLockActionView.setVisibility(View.INVISIBLE);
                }
            }
            setVisibleSafely(mThemeFlashPreviewWindow.getCallView(), View.VISIBLE);

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

        public void switchToReadyState(boolean ready, boolean isSelected) {

            mThemeStatusView.switchToReadyState(ready, isSelected);
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
            if (mCallActionView != null) {
                mCallActionView.stopAnimations();
            }
        }

        public void startAnimation() {
            if (mHolderDataReady) {
                mThemeFlashPreviewWindow.startAnimations();
                if (mCallActionView != null) {
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