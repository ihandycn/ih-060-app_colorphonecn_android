package com.honeycomb.colorphone.news;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.colorphone.lock.util.ViewUtils;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.honeycomb.colorphone.util.Utils;
import com.honeycomb.colorphone.view.GlideApp;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;
import com.superapps.util.Strings;
import com.superapps.util.Toasts;

import net.appcloudbox.ads.base.ContainerView.AcbNativeAdContainerView;
import net.appcloudbox.ads.base.ContainerView.AcbNativeAdIconView;
import net.appcloudbox.ads.base.ContainerView.AcbNativeAdPrimaryView;

public class NewsPage extends SwipeRefreshLayout implements NewsManager.NewsLoadListener, SwipeRefreshLayout.OnRefreshListener {

    private NewsResultBean newsResource;
    private RecyclerView newsList;
    private SwipeRefreshLayout newsPages;
    private NewsAdapter adapter;

    private boolean isRefreshing = false;
    private boolean showTime;
    private boolean isVideo = false;

    private float startY;
    private float startX;
    // 记录viewPager是否拖拽的标记
    private boolean mIsVpDragger;
    private final int mTouchSlop;
    private DividerItemDecoration divider;
    private String lastNewsContentID;

    public NewsPage(@NonNull Context context) {
        super(context);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public NewsPage(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override protected void onFinishInflate() {
        super.onFinishInflate();
        newsResource = new NewsResultBean();

        newsPages = findViewById(R.id.news_swipe_layout);
        newsList = findViewById(R.id.news_list);

        newsPages.setOnRefreshListener(this);

        initRecyclerView();

        showTime = HSConfig.optBoolean(true, "Application", "News", "NewsUpdateTimeShow");
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // 记录手指按下的位置
                startY = ev.getY();
                startX = ev.getX();
                // 初始化标记
                mIsVpDragger = false;
                break;
            case MotionEvent.ACTION_MOVE:
                // 如果viewpager正在拖拽中，那么不拦截它的事件，直接return false；
                if(mIsVpDragger) {
                    return true;
                }

                // 获取当前手指位置
                float endY = ev.getY();
                float endX = ev.getX();
                float distanceX = Math.abs(endX - startX);
                float distanceY = Math.abs(endY - startY);
                // 如果X轴位移大于Y轴位移，那么将事件交给viewPager处理。
                if(distanceX > mTouchSlop && distanceX > distanceY) {
                    mIsVpDragger = true;
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // 初始化标记
                mIsVpDragger = false;
                break;
        }
        // 如果是Y轴位移大于X轴，事件交给swipeRefreshLayout处理。
        return super.onInterceptTouchEvent(ev);
    }

    private void initRecyclerView() {
        adapter = new NewsAdapter();
        newsList.setAdapter(adapter);
        newsList.setLayoutManager(new LinearLayoutManager(getContext()));

        HSLog.i(NewsManager.TAG, "NP initRecyclerView: " + isVideo);
        divider = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        divider.setDrawable(getResources().getDrawable(R.drawable.news_divider));
        newsList.addItemDecoration(divider);

        newsList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int lastVisibleItem ;
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                //判断RecyclerView的状态 是空闲时，同时，是最后一个可见的ITEM时才加载
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == adapter.getItemCount()) {
                    HSLog.i(NewsManager.TAG, "NP onScrollStateChanged: " + newsPages.isRefreshing());
                    if (!isRefreshing) {
                        isRefreshing = true;
                        NewsManager.getInstance().fetchLaterNews(newsResource, NewsPage.this);
                    }
                }

                if ((newState == RecyclerView.SCROLL_STATE_IDLE)
                        && lastVisibleItem > logBigImageIndex) {
                    LauncherAnalytics.logEvent("mainview_news_tab_slide");
                    if (isVideo) {
                        LauncherAnalytics.logEvent("videonews_video_page_slide");
                    } else {
                        LauncherAnalytics.logEvent("videonews_news_page_slide");
                    }
                    logBigImageIndex = lastVisibleItem;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                //最后一个可见的ITEM
                lastVisibleItem = layoutManager.findLastVisibleItemPosition();
            }
        });
    }

    public void setIsVideo(boolean video) {
        HSLog.i(NewsManager.TAG, "NP setIsVideo: " + video);
        isVideo = video;
        newsList.removeItemDecoration(divider);
    }

    public void loadNews() {
        HSLog.i(NewsManager.TAG, "NP loadNews: " + isVideo);
        if (isVideo) {
            NewsManager.getInstance().fetchVideoNews(newsResource, this);
        } else {
            NewsManager.getInstance().fetchNews(newsResource, this);
        }
    }

    @Override public void onNewsLoaded(NewsResultBean bean) {
        HSLog.i(NewsManager.TAG, "NP onNewsLoaded " + (bean != null ? bean.totalItems : 0));
        newsPages.setRefreshing(false);
        isRefreshing = false;

        if (bean != null) {
            if (bean.content != null && bean.content.size() > 0) {
                String newContentID = bean.content.get(0).contentId;
                if (TextUtils.equals(newContentID, lastNewsContentID)) {
                    Toasts.showToast(R.string.news_no_news_update);
                }
                lastNewsContentID = newContentID;
            }
            newsPages.setVisibility(VISIBLE);

            newsResource = bean;
            adapter.notifyDataSetChanged();
            HSGlobalNotificationCenter.sendNotification(NewsFrame.LOAD_NEWS_SUCCESS);
        } else {
            HSGlobalNotificationCenter.sendNotification(NewsFrame.LOAD_NEWS_FAILED);
        }
    }

    @Override public void onRefresh() {
        HSLog.i(NewsManager.TAG, "NP onRefresh: " + newsPages.isRefreshing());
        isRefreshing = true;
        if (newsPages.isRefreshing()) {
            loadNews();
        }

        LauncherAnalytics.logEvent("mainview_news_tab_pull_to_refresh");

        if (isVideo) {
            LauncherAnalytics.logEvent("videonews_video_page_pull_to_refresh");
        }
    }

    public void scrollToTop() {
        HSLog.i(NewsManager.TAG, "scrollToTop");
        newsList.scrollToPosition(0);
    }

    private class NewsAdapter extends RecyclerView.Adapter {

        static final int NEWS_TYPE_ITEM = 0;
        static final int NEWS_TYPE_BIG = 1;
        static final int NEWS_TYPE_FOOT = 2;
        static final int NEWS_TYPE_NATIVE = 3;
        static final int NEWS_TYPE_VIDEO = 4;

        @Override public int getItemViewType(int position) {
            if (position == getItemCount() - 1) {
                return NEWS_TYPE_FOOT;
            }

            if (isVideo) {
                return NEWS_TYPE_VIDEO;
            }

            if (newsResource != null && newsResource.totalItems > position) {
                NewsBean bean = newsResource.content.get(position);
                if (bean instanceof NewsNativeAdBean) {
                    return NEWS_TYPE_NATIVE;
                }
            }
            return (position % NewsManager.BIG_IMAGE_INTERVAL == 0) ? NEWS_TYPE_BIG : NEWS_TYPE_ITEM;
        }

        @NonNull @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = null;
            switch (viewType) {
                case NEWS_TYPE_ITEM:
                    view = LayoutInflater.from(getContext()).inflate(R.layout.news_item_layout, parent, false);
                    return new NewsBeanItemHolder(view);
                case NEWS_TYPE_BIG:
                    view = LayoutInflater.from(getContext()).inflate(R.layout.news_big_layout, parent, false);
                    return new NewsBeanItemHolder(view);
                case NEWS_TYPE_FOOT:
                    view = LayoutInflater.from(getContext()).inflate(R.layout.news_foot_loading, parent, false);
                    return new NewsFootLoadingHolder(view);
                case NEWS_TYPE_VIDEO:
                    view = LayoutInflater.from(getContext()).inflate(R.layout.news_video_layout, parent, false);
                    return new NewsBeanVideoHolder(view);
                case NEWS_TYPE_NATIVE:
                    return new NewsNativeHolder(new AcbNativeAdContainerView(getContext()));
                default:
                    throw new RuntimeException("error News Type");
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (position == getItemCount() - 1) {
                NewsFootLoadingHolder loadingHolder = (NewsFootLoadingHolder) holder;
                loadingHolder.loading.setAlpha(0);
                loadingHolder.loading.animate().alpha(1).setDuration(200).start();
                return;
            }
            int type = getItemViewType(position);
            NewsBean bean = newsResource.content.get(position);

            if (type == NEWS_TYPE_NATIVE) {
                ((NewsNativeHolder) holder).bindView((NewsNativeAdBean) bean);
                return;
            }

            if (type == NEWS_TYPE_VIDEO) {
                ((NewsBeanVideoHolder) holder).bindNewsBean(bean);
                return;
            }

            NewsBeanItemHolder beanHolder = (NewsBeanItemHolder) holder;
            beanHolder.bindNewsBean(bean, type);
        }

        @Override public int getItemCount() {
            return (newsResource != null && newsResource.totalItems != 0) ? newsResource.totalItems + 1 : 0;
        }
    }
    private int logBigImageIndex = 0;

    private class NewsBeanItemHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView resource;
        TextView time;
        ImageView image;

        NewsBeanItemHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.news_title_tv);
            resource = itemView.findViewById(R.id.news_resource_tv);
            time = itemView.findViewById(R.id.news_time_tv);
            image = itemView.findViewById(R.id.news_icon_iv);
        }

        void bindNewsBean(NewsBean bean, int type) {
            String url = null;
            if (isVideo) {
                url = bean.thumbnail;
            } else {
                if (type == NewsAdapter.NEWS_TYPE_BIG) {
                    url = bean.images.mainImage.url;
                } else {
                    url = bean.images.mainImageThumbnail.url;
                }
            }

            title.setText(bean.title);
            if (showTime) {
                time.setText(String.valueOf(" · " + Utils.getNewsDate(bean.publishedAt)));
            } else {
                time.setVisibility(View.GONE);
            }
            resource.setText(bean.contentSourceDisplay);
            GlideApp.with(image)
                    .asDrawable()
                    .load(url)
                    .into(image);

            itemView.setOnClickListener(v -> {
                HSLog.i(NewsManager.TAG, "NP onClicked");
                Navigations.startActivitySafely(getContext(), WebViewActivity.newIntent(bean.contentURL, false, WebViewActivity.FROM_LIST));

                LauncherAnalytics.logEvent("mainview_newstab_news_click",
                        "type", (type == NewsAdapter.NEWS_TYPE_BIG ? "image" : "imagepreview"),
                        "user", Utils.isNewUser() ? "new" : "upgrade");

                LauncherAnalytics.logEvent("videonews_news_page_news_click", "NewsType", Strings.stringListToCsv(bean.categoriesEnglish));
            });
        }
    }

    private class NewsBeanVideoHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView resource;
        TextView time;
        ImageView image;
        ImageView icon;

        NewsBeanVideoHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.news_resource_icon);
            title = itemView.findViewById(R.id.news_title_tv);
            resource = itemView.findViewById(R.id.news_resource_tv);
            time = itemView.findViewById(R.id.news_time_tv);
            image = itemView.findViewById(R.id.news_icon_iv);
        }

        void bindNewsBean(NewsBean bean) {

            title.setText(bean.title);
            time.setText(Utils.getNewsVideoLength(bean.length));
            time.setBackground(BackgroundDrawables.createBackgroundDrawable(0xCC000000, Dimensions.pxFromDp(5.3f), false));

            resource.setText(bean.contentSourceDisplay);
            GlideApp.with(image)
                    .asDrawable()
                    .load(bean.thumbnail)
                    .into(image);

            if (!TextUtils.isEmpty(bean.contentSourceLogo)) {
                GlideApp.with(icon)
                        .asDrawable()
                        .load(bean.contentSourceLogo)
                        .into(icon);
            } else {
                icon.setVisibility(GONE);
            }

            itemView.setOnClickListener(v -> {
                HSLog.i(NewsManager.TAG, "NP onClicked");
                Navigations.startActivitySafely(getContext(), WebViewActivity.newIntent(bean.contentURL, false, WebViewActivity.FROM_LIST));

                LauncherAnalytics.logEvent("mainview_newstab_news_click",
                        "type", "video",
                        "user", Utils.isNewUser() ? "new" : "upgrade");

                LauncherAnalytics.logEvent("videonews_video_page_video_click", "NewsType", Strings.stringListToCsv(bean.categoriesEnglish));
            });
        }
    }

    private class NewsFootLoadingHolder extends RecyclerView.ViewHolder {
        ProgressBar loading;

        NewsFootLoadingHolder(View itemView) {
            super(itemView);
            loading = itemView.findViewById(R.id.news_foot_loading);
        }
    }

    private class NewsNativeHolder extends RecyclerView.ViewHolder {
        AcbNativeAdContainerView adContainer;
        AcbNativeAdPrimaryView mAdImageContainer;
        ViewGroup mAdChoice;
        AcbNativeAdIconView mAdIconView;
        TextView mTitleTv;
        TextView mDescriptionTv;
        View mActionBtn;

        NewsNativeHolder(View root) {
            super(root);

            adContainer = (AcbNativeAdContainerView) root;
            View view = LayoutInflater.from(getContext()).inflate(R.layout.news_ad_view, adContainer, false);

            mAdImageContainer = ViewUtils.findViewById(view, R.id.news_ad_image);
            mAdChoice = ViewUtils.findViewById(view, R.id.news_ad_choice_icon);
            mAdIconView = ViewUtils.findViewById(view, R.id.news_ad_icon);
            mTitleTv = ViewUtils.findViewById(view, R.id.news_ad_title);
            mDescriptionTv = ViewUtils.findViewById(view, R.id.news_ad_description);
            mActionBtn = ViewUtils.findViewById(view, R.id.news_ad_action_btn);

            adContainer.addContentView(view);
            adContainer.setAdTitleView(mTitleTv);
            adContainer.setAdBodyView(mDescriptionTv);
            adContainer.setAdPrimaryView(mAdImageContainer);
            adContainer.setAdChoiceView(mAdChoice);
            adContainer.setAdIconView(mAdIconView);
            adContainer.setAdActionView(mActionBtn);

//            ((ViewGroup) itemView).addView(adContainer);

        }

        void bindView(NewsNativeAdBean bean) {
            adContainer.fillNativeAd(bean.acbNativeAd);

        }
    }
}
