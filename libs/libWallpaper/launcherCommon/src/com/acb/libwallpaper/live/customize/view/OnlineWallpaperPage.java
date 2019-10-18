package com.acb.libwallpaper.live.customize.view;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.acb.libwallpaper.live.LauncherAnalytics;
import com.acb.libwallpaper.live.Manager;
import com.acb.libwallpaper.live.WallpaperAnalytics;
import com.acb.libwallpaper.live.model.LauncherFiles;
import com.acb.libwallpaper.live.util.LauncherConfig;
import com.acb.libwallpaper.live.util.Thunk;
import com.acb.libwallpaper.live.util.ViewUtils;
import com.acb.libwallpaper.R;
import com.acb.libwallpaper.live.animation.LauncherAnimUtils;
import com.acb.libwallpaper.live.customize.CategoryItem;
import com.acb.libwallpaper.live.customize.CustomizeConfig;
import com.acb.libwallpaper.live.customize.WallpaperInfo;
import com.acb.libwallpaper.live.customize.WallpaperMgr;
import com.acb.libwallpaper.live.customize.activity.CustomizeActivity;
import com.acb.libwallpaper.live.customize.adapter.CategoryViewAdapter;
import com.acb.libwallpaper.live.customize.adapter.OnlineWallpaperGalleryAdapter;
import com.acb.libwallpaper.live.customize.util.CustomizeUtils;
import com.acb.libwallpaper.live.util.Utils;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Dimensions;
import com.superapps.util.Fonts;
import com.superapps.util.Preferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OnlineWallpaperPage extends RelativeLayout {

    private static final int TAB_INDEX_LIVE_WALLPAPER = 1;
    private static final int TAB_INDEX_3D_WALLPAPER = 2;


    private WallpaperPagerAdapter mAdapter;

    @Thunk
    TabsConfiguration mTabsConfig;

    private OnlineWallpaperTabLayout mTabs;
    private GridView mGridView;
    private ImageView mArrowLeftPart;
    private ImageView mArrowRightPart;
    private TextView mCategoriesTitle;
    private List<Integer> mScrollStates = new ArrayList<>();
    private boolean mIsTabNoClickSelected;
    private AnimatorSet mAnimatorSet;
    private ScrollEventLogger mScrollEventLogger = new ScrollEventLogger();
    float sumPositionAndPositionOffset;
    private ViewPager mViewPage;

    private boolean mIsRtl;

    public OnlineWallpaperPage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTabsConfig = new TabsConfiguration();
        mIsRtl = Dimensions.isRtl();
        setupViews();
    }

    public void loadWallpaper(int tabIndex, String paperName) {

        this.post(() -> {
            if (mAdapter != null) {
                mAdapter.setApplyWallpaperInfo(tabIndex, paperName);
            }
            switch (tabIndex) {
                case TAB_INDEX_LIVE_WALLPAPER:
                    CustomizeUtils.previewLiveWallpaper((Activity) getContext(), WallpaperInfo.newLiveWallpaper(paperName));
                    break;
                case TAB_INDEX_3D_WALLPAPER:
                    CustomizeUtils.preview3DWallpaper((Activity) getContext(), WallpaperInfo.new3DWallpaper(paperName));
                    break;
                default:
                    break;
            }
        });
    }

    private void setupViews() {
        mTabs = ViewUtils.findViewById(this, R.id.wallpaper_tabs);
        mViewPage = ViewUtils.findViewById(this, R.id.wallpaper_pager);
        mGridView = ViewUtils.findViewById(this, R.id.categories_grid_view);
        mCategoriesTitle = ViewUtils.findViewById(this, R.id.categories_title);
        mArrowLeftPart = ViewUtils.findViewById(this, R.id.tab_top_arrow_left);
        mArrowRightPart = ViewUtils.findViewById(this, R.id.tab_top_arrow_right);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ViewUtils.findViewById(this, R.id.tab_layout_container)
                    .setElevation(Dimensions.pxFromDp(1));
        }
    }

    public void setup(int initialTabIndex) {
        mAdapter = new WallpaperPagerAdapter(getContext());
        mViewPage.setAdapter(mAdapter);
        mTabs.setupWithViewPager(mViewPage);
        Utils.configTabLayoutText(mTabs, Fonts.getTypeface(Fonts.Font.CUSTOM_FONT_SEMIBOLD), 14f);
        mTabs.setOnScrollListener((isScrollLeft, isScrollRight) -> LauncherAnalytics.logEvent("Wallpaper_TopTab_Slided"));

        int indexAbsolute = Utils.mirrorIndexIfRtl(mIsRtl, mAdapter.getCount(), initialTabIndex);

        boolean has_record_wallpaper_class_show_firstly = Preferences.get(LauncherFiles.CUSTOMIZE_PREFS).getBoolean("has_record_Wallpaper_Class_Show_firstly", true);
        if (!has_record_wallpaper_class_show_firstly) {
            Manager.getInstance().getDelegate().logEvent("Wallpaper_Class_Show", "ClassName", mAdapter.getDefaultCategoryName(indexAbsolute),
                    "From", "TabClick");
        }

        Preferences preferences = Preferences.get(LauncherFiles.CUSTOMIZE_PREFS);
        preferences.putString("current_category_name", mAdapter.getDefaultCategoryName(indexAbsolute));

        mViewPage.setCurrentItem(indexAbsolute, false);
        mViewPage.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position + positionOffset > sumPositionAndPositionOffset) {
                    mScrollEventLogger.prepareLeft();
                } else if (position != 0 || positionOffset != 0 || sumPositionAndPositionOffset != 0 || positionOffsetPixels != 0) {
                    mScrollEventLogger.prepareRight();
                }
                sumPositionAndPositionOffset = position + positionOffset;
            }

            @Override
            public void onPageSelected(final int positionAbsolute) {
                if (!mIsTabNoClickSelected) {
                    LauncherAnalytics.logEvent("Wallpaper_TopTab_Tab_Selected", "type", String.valueOf(mAdapter.getPageTitle(positionAbsolute)));
                }

                resetCategoryGrids();
                ((CategoryItem) mGridView.getAdapter().getItem(positionAbsolute)).setSelected(true);
                ((CategoryViewAdapter) mGridView.getAdapter()).notifyDataSetChanged();
                mScrollEventLogger.tryLogScrollLeftEvent();
                mScrollEventLogger.tryLogScrollRightEvent();

                Preferences preferences = Preferences.get(LauncherFiles.CUSTOMIZE_PREFS);
                preferences.putString("current_category_name", mAdapter.getDefaultCategoryName(positionAbsolute));
                WallpaperAnalytics.logEvent("Wallpaper_Class_Show", "ClassName", mAdapter.getDefaultCategoryName(positionAbsolute));

                Manager.getInstance().getDelegate().logEvent("Wallpaper_Class_Show", "ClassName", mAdapter.getDefaultCategoryName(positionAbsolute),
                        "From", mIsTabNoClickSelected ? "Slide" : "TabClick");

                mIsTabNoClickSelected = false;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                    mIsTabNoClickSelected = true;
                }
                mScrollStates.add(state);
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    for (Integer stateItem : mScrollStates) {
                        if (stateItem == ViewPager.SCROLL_STATE_DRAGGING) {
                            break;
                        }
                    }
                    mScrollStates.clear();
                }

            }
        });

        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                ((CategoryViewAdapter) mGridView.getAdapter()).setTextAnimationEnabled(false);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });

        if (mIsRtl) {
            mTabs.scrollToRight();
            mArrowLeftPart.setImageResource(R.drawable.wallpapers_toptab_arrow_right);
            mArrowRightPart.setImageResource(R.drawable.wallpapers_toptab_arrow_left);
        }
        setArrowOnClickAnimation(mGridView, mCategoriesTitle, mArrowLeftPart, mArrowRightPart);

        List<CategoryItem> data = new ArrayList<>();
        for (int i = 0; i < mAdapter.getCount(); i++) {
            CategoryItem item = new CategoryItem(mAdapter.getPageTitle(i).toString(), i == indexAbsolute);
            data.add(item);
        }
        final CategoryViewAdapter categoryViewAdapter = new CategoryViewAdapter(getContext(), data);
        mGridView.setAdapter(categoryViewAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < 0 || position >= mTabs.getTabCount() || position == mTabs.getSelectedTabPosition()) {
                    return;
                }

                LauncherAnalytics.logEvent("Wallpaper_TabList_Tab_Selected", "type", ((CategoryItem) parent.getAdapter().getItem(position)).getItemName());
                mIsTabNoClickSelected = false;

                ((CategoryViewAdapter) parent.getAdapter()).setTextAnimationEnabled(false);
                ((CategoryItem) parent.getAdapter().getItem(mTabs.getSelectedTabPosition())).setSelected(false);
                ((CategoryItem) parent.getAdapter().getItem(position)).setSelected(true);
                ((CategoryViewAdapter) parent.getAdapter()).notifyDataSetChanged();

                resetCategoryGrids();
                mViewPage.setCurrentItem(position, true);
                arrowClicked(mGridView, mCategoriesTitle, mArrowLeftPart, mArrowRightPart, "TabClicked");
            }
        });
    }

    public void setIndex(int index) {
        if (mViewPage != null) {
            mViewPage.setCurrentItem(index, false);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (Math.abs((int) mArrowLeftPart.getRotation() % 360) != 0) {
            arrowClicked(mGridView, mCategoriesTitle, mArrowLeftPart, mArrowRightPart, "TabClicked");
        }
    }

    private void setArrowOnClickAnimation(final GridView categoryView,
                                          final TextView categoryTitle,
                                          final ImageView arrowLeftPart,
                                          final ImageView arrowRightPart) {
        LinearLayout arrowContainer = ViewUtils.findViewById(this, R.id.arrow_container);
        arrowContainer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                arrowClicked(categoryView, categoryTitle, arrowLeftPart, arrowRightPart, "ArrowClicked");
            }
        });
    }

    private void arrowClicked(final GridView categoryView,
                              final TextView categoryTitle,
                              final ImageView arrowLeftPart,
                              final ImageView arrowRightPart, final String flurryClickType) {
        if (mAnimatorSet != null && mAnimatorSet.isRunning()) {
            mAnimatorSet.end();
        }
        final int start = Math.abs((int) arrowLeftPart.getRotation() % 360);
        HSLog.d("WallpaperAnimator ", "start value " + start + "");
        float startAlpha = start == 0 ? 0 : 1;
        ObjectAnimator alpha = ObjectAnimator.ofFloat(categoryView, "alpha", startAlpha, 1f - startAlpha);
        alpha.setDuration(300);
        alpha.setInterpolator(LauncherAnimUtils.DECELERATE_QUAD);
        int degree = !mIsRtl ? 90 : -90;
        // shrink
        if (start == 90) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ViewUtils.findViewById(this, R.id.tab_layout_container)
                        .setElevation(Dimensions.pxFromDp(1));
            }

            ObjectAnimator arrowRotateLeft = ObjectAnimator.ofFloat(arrowLeftPart, "rotation", -degree, 0);
            arrowRotateLeft.setDuration(300);

            ObjectAnimator arrowRotateRight = ObjectAnimator.ofFloat(arrowRightPart, "rotation", degree, 0);
            arrowRotateRight.setDuration(300);

            mTabs.setVisibility(VISIBLE);
            categoryTitle.setVisibility(GONE);
            ((CategoryViewAdapter) categoryView.getAdapter()).setTextAnimationEnabled(false);
            ObjectAnimator transY = ObjectAnimator.ofFloat(categoryView, "translationY", 0, -categoryView.getHeight());
            transY.setInterpolator(LauncherAnimUtils.ACCELERATE_QUAD);
            transY.setDuration(160);

            transY.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    categoryView.setVisibility(GONE);
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                    categoryView.setVisibility(GONE);
                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            mAnimatorSet = new AnimatorSet();
            mAnimatorSet.playTogether(arrowRotateLeft, arrowRotateRight, transY);
            mAnimatorSet.start();
        }
        // expand
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ViewUtils.findViewById(this, R.id.tab_layout_container)
                        .setElevation(0);
            }

            ObjectAnimator arrowRotateLeft = ObjectAnimator.ofFloat(arrowLeftPart, "rotation", 0, -degree);
            arrowRotateLeft.setDuration(300);

            ObjectAnimator arrowRotateRight = ObjectAnimator.ofFloat(arrowRightPart, "rotation", 0, degree);
            arrowRotateRight.setDuration(300);

            LauncherAnalytics.logEvent("Wallpaper_TabList_Open");
            mTabs.setVisibility(GONE);
            categoryTitle.setVisibility(VISIBLE);
            categoryView.setVisibility(VISIBLE);

            categoryView.setTranslationY(-categoryView.getHeight());
            ((CategoryViewAdapter) categoryView.getAdapter()).setTextAnimationEnabled(true);

            ObjectAnimator transY = ObjectAnimator.ofFloat(categoryView, "translationY", -categoryView.getHeight(), 0);
            transY.setInterpolator(LauncherAnimUtils.DECELERATE_QUAD);
            transY.setDuration(300);

            AnimatorSet title = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), R.animator.online_wallpaper_categories_title_in);
            title.setTarget(categoryTitle);

            mAnimatorSet = new AnimatorSet();
            mAnimatorSet.playTogether(arrowRotateLeft, arrowRotateRight, title, alpha, transY);
            mAnimatorSet.start();
            ((CategoryViewAdapter) categoryView.getAdapter()).notifyDataSetChanged();
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void resetCategoryGrids() {
        for (int i = 0; i < mTabs.getTabCount(); i++) {
            ((CategoryItem) mGridView.getAdapter().getItem(i)).setSelected(false);
        }
    }

    public boolean isShowingCategories() {
        return mCategoriesTitle.getVisibility() != GONE;
    }

    public void hideCategoriesView() {
        if (isShowingCategories()) {
            arrowClicked(mGridView, mCategoriesTitle, mArrowLeftPart, mArrowRightPart, "Navigation bar_Back");
        }
    }

    private class WallpaperPagerAdapter extends PagerAdapter {
        private final List<Map<String, ?>> mCategoryConfigs;

        private Context mContext;

        @SuppressWarnings("unchecked")
        WallpaperPagerAdapter(Context context) {
            mContext = context;
            mCategoryConfigs = (List<Map<String, ?>>) CustomizeConfig.getList("Wallpapers");
        }

        void setApplyWallpaperInfo(int tabIndex, String paperName) {
            switch (tabIndex) {
                case TAB_INDEX_LIVE_WALLPAPER:
                    OnlineWallpaperGalleryAdapter.setWallpaperInfo(WallpaperInfo.newLiveWallpaper(paperName));
                    break;
                case TAB_INDEX_3D_WALLPAPER:
                    OnlineWallpaperGalleryAdapter.setWallpaperInfo(WallpaperInfo.new3DWallpaper(paperName));
                    break;
                default:
                    break;
            }
        }

        @Override
        public int getCount() {
            return mCategoryConfigs.size() + (CustomizeConfig.getBoolean(true, "IsHotEnabled") ? 0 : mTabsConfig.extraTabsCount);
        }

        @Override
        public CharSequence getPageTitle(int positionAbsolute) {
            int position = Utils.mirrorIndexIfRtl(mIsRtl, getCount(), positionAbsolute);
            if (CustomizeConfig.getBoolean(true, "IsHotEnabled") && position == mTabsConfig.tabIndexHot) {
                return mContext.getString(R.string.online_wallpaper_tab_title_hot);
            }
            int categoryIndex = position - (CustomizeConfig.getBoolean(true, "IsHotEnabled") ?
                    0 : mTabsConfig.extraTabsCount);
            return LauncherConfig.getMultilingualString(mCategoryConfigs.get(categoryIndex), "CategoryName");
        }

        @Override
        public Object instantiateItem(ViewGroup container, int positionAbsolute) {
            int position = Utils.mirrorIndexIfRtl(mIsRtl, getCount(), positionAbsolute);
            View initView;

            if (CustomizeConfig.getBoolean(true, "IsHotEnabled") && position == mTabsConfig.tabIndexHot) {
                OnlineWallpaperListView mHotTabContent = (OnlineWallpaperListView) LayoutInflater.from(
                        getContext()).inflate(R.layout.wallpaper_list_page, OnlineWallpaperPage.this, false);
                mHotTabContent.setScenario(WallpaperMgr.Scenario.ONLINE_HOT);
                mHotTabContent.setupAdapter();
                mHotTabContent.startLoading();
                initView = mHotTabContent;

            } else {
                int categoryIndex = position - (CustomizeConfig.getBoolean(true, "IsHotEnabled") ?
                        0 : mTabsConfig.extraTabsCount);
                OnlineWallpaperListView list = createSingleCategoryTabContent(categoryIndex);
                list.setupAdapter();
                list.startLoading();
                initView = list;

            }
            container.addView(initView);
            return initView;
        }

        @SuppressLint("InflateParams")
        private OnlineWallpaperListView createSingleCategoryTabContent(final int categoryIndex) {
            OnlineWallpaperListView categoryListView = (OnlineWallpaperListView) LayoutInflater.from(getContext())
                    .inflate(R.layout.wallpaper_list_page, OnlineWallpaperPage.this, false);

            String categoryName = "";
            int position = categoryIndex + (CustomizeConfig.getBoolean(true, "IsHotEnabled") ?
                    0 : mTabsConfig.extraTabsCount);
            int positionAbsolute = Utils.mirrorIndexIfRtl(mIsRtl, getCount(), position);
            CharSequence categoryNameCs = getPageTitle(positionAbsolute);
            if (null != categoryNameCs) {
                categoryName = categoryNameCs.toString();
            }
            categoryListView.setCategoryName(categoryName);
            categoryListView.setCategoryIndex(categoryIndex);
            categoryListView.setScenario(WallpaperMgr.Scenario.ONLINE_CATEGORY);
            return categoryListView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        public String getDefaultCategoryName(int positionAbsolute) {
            int position = Utils.mirrorIndexIfRtl(mIsRtl, getCount(), positionAbsolute);
            if (CustomizeConfig.getBoolean(true, "IsHotEnabled") && position == mTabsConfig.tabIndexHot) {
                return mContext.getString(R.string.online_wallpaper_tab_title_hot);
            }
            int categoryIndex = position - (CustomizeConfig.getBoolean(true, "IsHotEnabled") ?
                    0 : mTabsConfig.extraTabsCount);
            return LauncherConfig.getDefaultString(mCategoryConfigs.get(categoryIndex), "CategoryName");
        }
    }

    private static class ScrollEventLogger {
        private boolean mLeftEnabled;
        private boolean mRightEnabled;

        void prepareLeft() {
            mLeftEnabled = true;
            mRightEnabled = false;
        }

        void prepareRight() {
            mLeftEnabled = false;
            mRightEnabled = true;
        }

        void reset() {
            mLeftEnabled = false;
            mRightEnabled = false;
        }

        void tryLogScrollLeftEvent() {
            if (mLeftEnabled) {
                LauncherAnalytics.logEvent("Wallpaper_PaperList_R&L_Slided", "type", "Left");
                reset();
            }
        }

        void tryLogScrollRightEvent() {
            if (mRightEnabled) {
                LauncherAnalytics.logEvent("Wallpaper_PaperList_R&L_Slided", "type", "Right");
            }
            reset();
        }
    }

    public static class TabsConfiguration {
        final int extraTabsCount;
        public final int tabIndexHot;

        public TabsConfiguration() {
            extraTabsCount = CustomizeConfig.getBoolean(true, "IsHotEnabled") ? 1 : 0;
            tabIndexHot = 0;
        }
    }
}
