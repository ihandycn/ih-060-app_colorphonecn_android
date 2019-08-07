package com.honeycomb.colorphone.customize.view;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
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

import com.acb.utils.Utils;
import com.colorphone.lock.util.ViewUtils;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.customize.CategoryItem;
import com.honeycomb.colorphone.customize.CustomizeConfig;
import com.honeycomb.colorphone.customize.WallpaperInfo;
import com.honeycomb.colorphone.customize.WallpaperMgr;
import com.honeycomb.colorphone.customize.adapter.CategoryViewAdapter;
import com.honeycomb.colorphone.customize.adapter.OnlineWallpaperGalleryAdapter;
import com.honeycomb.colorphone.customize.util.CustomizeUtils;
import com.honeycomb.colorphone.util.Analytics;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Dimensions;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;
import net.lucode.hackware.magicindicator.abs.IPagerNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.WrapPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OnlineWallpaperPage extends RelativeLayout {

    private static final int TAB_INDEX_LIVE_WALLPAPER = 1;
    private static final int TAB_INDEX_3D_WALLPAPER = 2;


    private WallpaperPagerAdapter mAdapter;

    TabsConfiguration mTabsConfig;

    private MagicIndicator mTabs;
    private GridView mGridView;
    private ImageView mArrowLeftPart;
    private ImageView mArrowRightPart;
    private TextView mCategoriesTitle;
    private List<Integer> mScrollStates = new ArrayList<>();
    private boolean mIsTabNoClickSelected;
    private AnimatorSet mAnimatorSet;
    private ScrollEventLogger mScrollEventLogger = new ScrollEventLogger();
    float sumPositionAndPositionOffset;
    private ViewPager mViewPager;

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
        mViewPager = ViewUtils.findViewById(this, R.id.wallpaper_pager);
        mGridView = ViewUtils.findViewById(this, R.id.categories_grid_view);
        mCategoriesTitle = ViewUtils.findViewById(this, R.id.categories_title);
        mArrowLeftPart = ViewUtils.findViewById(this, R.id.tab_top_arrow_left);
        mArrowRightPart = ViewUtils.findViewById(this, R.id.tab_top_arrow_right);

    }

    public void setup(int initialTabIndex) {
        mAdapter = new WallpaperPagerAdapter(getContext());
        mViewPager.setAdapter(mAdapter);

        mTabs.setNavigator(createTabNavigator());
        ViewPagerHelper.bind(mTabs, mViewPager);

        int indexAbsolute = CustomizeUtils.mirrorIndexIfRtl(mIsRtl, mAdapter.getCount(), initialTabIndex);

        mViewPager.setCurrentItem(indexAbsolute, false);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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
                    Analytics.logEvent("Wallpaper_TopTab_Tab_Selected", true,"type", String.valueOf(mAdapter.getPageTitle(positionAbsolute)));
                }

                mIsTabNoClickSelected = false;
                resetCategoryGrids();
                ((CategoryItem) mGridView.getAdapter().getItem(positionAbsolute)).setSelected(true);
                ((CategoryViewAdapter) mGridView.getAdapter()).notifyDataSetChanged();
                mScrollEventLogger.tryLogScrollLeftEvent();
                mScrollEventLogger.tryLogScrollRightEvent();
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
//            mTabs.scrollToRight();
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
                if (position < 0 || position >= mAdapter.getCount() || position == mViewPager.getCurrentItem()) {
                    return;
                }

                Analytics.logEvent("Wallpaper_TabList_Tab_Selected", true,"type", ((CategoryItem) parent.getAdapter().getItem(position)).getItemName());
                mIsTabNoClickSelected = true;

                ((CategoryViewAdapter) parent.getAdapter()).setTextAnimationEnabled(false);
                ((CategoryItem) parent.getAdapter().getItem(mViewPager.getCurrentItem())).setSelected(false);
                ((CategoryItem) parent.getAdapter().getItem(position)).setSelected(true);
                ((CategoryViewAdapter) parent.getAdapter()).notifyDataSetChanged();

                resetCategoryGrids();
                mViewPager.setCurrentItem(position, true);
                arrowClicked(mGridView, mCategoriesTitle, mArrowLeftPart, mArrowRightPart, "TabClicked");
            }
        });
    }

    private IPagerNavigator createTabNavigator() {
        CommonNavigator commonNavigator = new CommonNavigator(getContext());
        commonNavigator.setScrollPivotX(0.35f);
        commonNavigator.setAdapter(new CommonNavigatorAdapter() {
            @Override
            public int getCount() {
                return mAdapter.getCount();
            }

            @Override
            public IPagerTitleView getTitleView(Context context, final int index) {
                SimplePagerTitleView simplePagerTitleView = new SimplePagerTitleView(context);
                simplePagerTitleView.setTextSize(16);
                simplePagerTitleView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                simplePagerTitleView.setText(mAdapter.getPageTitle(index));
                simplePagerTitleView.setNormalColor(getResources().getColor(R.color.white_80_transparent));
                simplePagerTitleView.setSelectedColor(Color.BLACK);
                simplePagerTitleView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mViewPager.setCurrentItem(index);
                    }
                });
                return simplePagerTitleView;
            }

            @Override
            public IPagerIndicator getIndicator(Context context) {
                WrapPagerIndicator indicator = new WrapPagerIndicator(context);
                indicator.setFillColor(Color.WHITE);
                return indicator;
            }
        });
        return commonNavigator;
    }

    public void setIndex(int index) {
        if (mViewPager != null) {
            mViewPager.setCurrentItem(index, false);
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
        int degree = !mIsRtl ? 90 : -90;
        // shrink
        if (start == 90) {


            ObjectAnimator arrowRotateLeft = ObjectAnimator.ofFloat(arrowLeftPart, "rotation", -degree, 0);
            arrowRotateLeft.setDuration(300);

            ObjectAnimator arrowRotateRight = ObjectAnimator.ofFloat(arrowRightPart, "rotation", degree, 0);
            arrowRotateRight.setDuration(300);

            mTabs.setVisibility(VISIBLE);
            categoryTitle.setVisibility(GONE);
            ((CategoryViewAdapter) categoryView.getAdapter()).setTextAnimationEnabled(false);
            ObjectAnimator transY = ObjectAnimator.ofFloat(categoryView, "translationY", 0, -categoryView.getHeight());
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

            ObjectAnimator arrowRotateLeft = ObjectAnimator.ofFloat(arrowLeftPart, "rotation", 0, -degree);
            arrowRotateLeft.setDuration(300);

            ObjectAnimator arrowRotateRight = ObjectAnimator.ofFloat(arrowRightPart, "rotation", 0, degree);
            arrowRotateRight.setDuration(300);

            Analytics.logEvent("Wallpaper_TabList_Open", true);
            mTabs.setVisibility(GONE);
            categoryTitle.setVisibility(VISIBLE);
            categoryView.setVisibility(VISIBLE);

            categoryView.setTranslationY(-categoryView.getHeight());
            ((CategoryViewAdapter) categoryView.getAdapter()).setTextAnimationEnabled(true);

            ObjectAnimator transY = ObjectAnimator.ofFloat(categoryView, "translationY", -categoryView.getHeight(), 0);
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
        for (int i = 0; i < mAdapter.getCount(); i++) {
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
            mCategoryConfigs = (List<Map<String, ?>>) CustomizeConfig.getList("Application", "Wallpaper",
                    "ImageWallpapers", "Items");
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
            return mCategoryConfigs.size() + mTabsConfig.extraTabsCount;
        }

        @Override
        public CharSequence getPageTitle(int positionAbsolute) {
            int position = CustomizeUtils.mirrorIndexIfRtl(mIsRtl, getCount(), positionAbsolute);
            if (position == mTabsConfig.tabIndexVideo) {
                return mContext.getString(R.string.online_wallpaper_tab_title_video);
            } else if (position == mTabsConfig.tabIndexLive) {
                return mContext.getString(R.string.online_wallpaper_tab_title_live);
            }
            int categoryIndex = position - mTabsConfig.extraTabsCount;
            return Utils.getMultilingualString(mCategoryConfigs.get(categoryIndex), "CategoryName");
        }

        @Override
        public Object instantiateItem(ViewGroup container, int positionAbsolute) {
            int position = CustomizeUtils.mirrorIndexIfRtl(mIsRtl, getCount(), positionAbsolute);
            View initView;

            if (position == mTabsConfig.tabIndexVideo) {
                OnlineWallpaperListView mHotTabContent = (OnlineWallpaperListView) LayoutInflater.from(
                        getContext()).inflate(R.layout.wallpaper_list_page, OnlineWallpaperPage.this, false);
                mHotTabContent.setScenario(WallpaperMgr.Scenario.ONLINE_VIDEO);
                mHotTabContent.setupAdapter();
                mHotTabContent.startLoading();
                initView = mHotTabContent;

            } else if (position == mTabsConfig.tabIndexLive) {
                OnlineWallpaperListView mHotTabContent = (OnlineWallpaperListView) LayoutInflater.from(
                        getContext()).inflate(R.layout.wallpaper_list_page, OnlineWallpaperPage.this, false);
                mHotTabContent.setScenario(WallpaperMgr.Scenario.ONLINE_LIVE);
                mHotTabContent.setupAdapter();
                mHotTabContent.startLoading();
                initView = mHotTabContent;
            } else {
                int categoryIndex = position - mTabsConfig.extraTabsCount;
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
            int position = categoryIndex + mTabsConfig.extraTabsCount;
            int positionAbsolute = CustomizeUtils.mirrorIndexIfRtl(mIsRtl, getCount(), position);
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
                reset();
            }
        }

        void tryLogScrollRightEvent() {

            reset();
        }
    }

    public static class TabsConfiguration {
        final int extraTabsCount;
        public final int tabIndexVideo;
        public final int tabIndexLive;

        public TabsConfiguration() {
            extraTabsCount = 2;
            tabIndexVideo = 0;
            tabIndexLive = 1;
        }
    }
}
