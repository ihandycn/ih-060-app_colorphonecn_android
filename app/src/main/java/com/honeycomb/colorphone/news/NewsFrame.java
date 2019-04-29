package com.honeycomb.colorphone.news;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.honeycomb.colorphone.view.ViewPagerFixed;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Fonts;
import com.superapps.util.Networks;
import com.superapps.view.TypefacedTextView;

import java.util.ArrayList;

public class NewsFrame extends ConstraintLayout implements INotificationObserver {
    static final String LOAD_NEWS_SUCCESS = "load_news_success";
    static final String LOAD_NEWS_FAILED = "load_news_failed";

    private static final int MOVABLE_COUNT = 4;

    private TabLayout tabLayout;
    private ViewPagerFixed newsPager;
    private View noNetWorkPage;
    private View loading;
    
    private ArrayList<String> tabsTitle;
    private ArrayList<NewsPage> newsPages;
    private int currentIndex;

    public NewsFrame(Context context) {
        this(context, null);
    }

    public NewsFrame(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NewsFrame(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override protected void onFinishInflate() {
        super.onFinishInflate();

        tabLayout = findViewById(R.id.news_tabs);
        newsPager = findViewById(R.id.news_pages);
        noNetWorkPage = findViewById(R.id.news_no_network);
        loading = findViewById(R.id.news_loading);

        tabLayout.setupWithViewPager(newsPager);

        initDatas();
        initTabLayout();
        initViewPager();

        if (!Networks.isNetworkAvailable(-1)) {
            showNoNetworkPage();
        }

        HSGlobalNotificationCenter.addObserver(LOAD_NEWS_FAILED, this);
        HSGlobalNotificationCenter.addObserver(LOAD_NEWS_SUCCESS, this);
    }

    @Override public void onReceive(String s, HSBundle hsBundle) {
        if (TextUtils.equals(s, LOAD_NEWS_FAILED)) {
            showNoNetworkPage();
        } else if (TextUtils.equals(s, LOAD_NEWS_SUCCESS)) {
            newsPager.setVisibility(VISIBLE);
            loading.setVisibility(GONE);
            noNetWorkPage.setVisibility(GONE);
        }
    }

    public void showNoNetworkPage() {
        noNetWorkPage.setVisibility(VISIBLE);
        View action = noNetWorkPage.findViewById(R.id.news_no_network_action);
        action.setOnClickListener(v -> {
            loadNews();
        });
        newsPager.setVisibility(GONE);
        loading.setVisibility(GONE);
    }

    private void loadNews() {
        if (0 <= currentIndex && currentIndex < newsPages.size()) {
            newsPages.get(currentIndex).loadNews();
        }

        noNetWorkPage.setVisibility(GONE);
        loading.setVisibility(VISIBLE);
    }

    public void scrollToTop() {
        if (0 <= currentIndex && currentIndex < newsPages.size()) {
            newsPages.get(currentIndex).scrollToTop();
        }
    }

    private void initTabLayout() {
        //MODE_FIXED标签栏不可滑动，各个标签会平分屏幕的宽度
        tabLayout.setTabMode(tabsTitle.size() <= MOVABLE_COUNT ? TabLayout.MODE_FIXED : TabLayout.MODE_SCROLLABLE);
        //指示条的颜色
        tabLayout.setSelectedTabIndicatorColor(0xffff4a4a);
        tabLayout.setSelectedTabIndicatorHeight(7);
        //关联tabLayout和ViewPager,两者的选择和滑动状态会相互影响

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getCustomView() instanceof TextView) {
                    ((TextView) tab.getCustomView()).setTextSize(24);
                }
                newsPager.setCurrentItem(tab.getPosition());
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {
                if (tab.getCustomView() instanceof TextView) {
                    ((TextView) tab.getCustomView()).setTextSize(18);
                }
            }

            @Override public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        //自定义标签布局
        for (int i = 0; i < tabsTitle.size(); i++) {
            TabLayout.Tab tab = tabLayout.newTab();
            tabLayout.addTab(tab);
            TypefacedTextView tv = (TypefacedTextView) LayoutInflater.from(getContext()).inflate(R.layout.news_tabview, tabLayout, false);
            tv.setText(tabsTitle.get(i));
            tv.setTypeface(Fonts.getTypeface(Fonts.Font.CUSTOM_FONT_BOLD));
            tab.setCustomView(tv);
        }
    }

    private void initViewPager() {
        newsPager.setAdapter(new NewsPagerAdapter());
        newsPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override public void onPageSelected(int position) {
                currentIndex = position;
                if (currentIndex == 0) {
                    LauncherAnalytics.logEvent("videonews_video_page_show");
                } else {
                    LauncherAnalytics.logEvent("videonews_news_page_show");
                }
            }

            @Override public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void initDatas() {
        tabsTitle = new ArrayList<>();
        tabsTitle.add(getContext().getString(R.string.menu_item_news_video));
        tabsTitle.add(getContext().getString(R.string.menu_item_news));

        newsPages = new ArrayList<>();
        newsPages.add((NewsPage) LayoutInflater.from(getContext()).inflate(R.layout.news_page, null, false));
        newsPages.add((NewsPage) LayoutInflater.from(getContext()).inflate(R.layout.news_page, null, false));
        newsPages.get(0).setIsVideo(true);
    }

    private class NewsPagerAdapter extends PagerAdapter {
        @Override public int getCount() {
            return newsPages.size();
        }

        @Override public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Nullable @Override public CharSequence getPageTitle(int position) {
            return tabsTitle.get(position);
        }

        @NonNull @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            HSLog.i(NewsManager.TAG, "instantiateItem");
            NewsPage curNewsPages = newsPages.get(position);
            curNewsPages.loadNews();
            container.addView(curNewsPages);
            return curNewsPages;
        }
    }
}
