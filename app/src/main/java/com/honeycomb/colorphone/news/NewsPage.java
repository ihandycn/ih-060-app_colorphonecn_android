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
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.WatchedScrollListener;
import com.honeycomb.colorphone.activity.ColorPhoneActivity;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.view.GlideApp;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;
import com.superapps.util.Networks;
import com.superapps.util.Toasts;

import net.appcloudbox.ads.base.ContainerView.AcbNativeAdContainerView;
import net.appcloudbox.ads.base.ContainerView.AcbNativeAdIconView;
import net.appcloudbox.ads.base.ContainerView.AcbNativeAdPrimaryView;

import java.net.URLDecoder;
import java.util.ArrayList;

public class NewsPage extends SwipeRefreshLayout implements NewsManager.NewsLoadListener, SwipeRefreshLayout.OnRefreshListener {

    protected NewsResultBean newsResource;
    protected RecyclerView newsList;
    private SwipeRefreshLayout newsPages;
    protected NewsAdapter adapter;

    private boolean isRefreshing = false;
    private boolean showTime;
    protected boolean isVideo = false;

    private float startY;
    private float startX;
    // 记录viewPager是否拖拽的标记
    private boolean mIsVpDragger;
    private final int mTouchSlop;
    private DividerItemDecoration divider;
    private String lastNewsContentID;
    private int lastNewsSize;
    private boolean mSelected;

    protected int itemViewPadding = 0;
    protected EventLogger logger;

    public NewsPage(@NonNull Context context) {
        this(context, null);
    }

    public NewsPage(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    protected void init() {
        logger = new EventLogger();
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

    protected void initAdapter() {
        adapter = new NewsAdapter();
    }

    private void initRecyclerView() {
        initAdapter();
        newsList.setAdapter(adapter);
        newsList.setLayoutManager(new LinearLayoutManager(getContext()));

        HSLog.i(NewsManager.TAG, "NP initRecyclerView: " + isVideo);
        divider = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        divider.setDrawable(getResources().getDrawable(R.drawable.news_divider));
        newsList.addItemDecoration(divider);

        newsList.addOnScrollListener(new WatchedScrollListener() {
            int lastVisibleItem ;
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                //判断RecyclerView的状态 是空闲时，同时，是最后一个可见的ITEM时才加载
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == adapter.getItemCount()) {
                    HSLog.i(NewsManager.TAG, "NP onScrollStateChanged: " + newsPages.isRefreshing());
                    if (!isRefreshing) {
                        if (Networks.isNetworkAvailable(-1)) {
                            isRefreshing = true;
                            NewsManager.getInstance().fetchLaterNews(newsResource, NewsPage.this, isVideo);
                            NewsManager.logNewsListShow("LoadMore");
                        } else {
                            showToast(R.string.news_network_failed_toast);
                        }
                    }
                }

                if ((newState == RecyclerView.SCROLL_STATE_IDLE)
                        && lastVisibleItem > logBigImageIndex) {
                    logger.logListSlide();
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

    public void refreshNews(String from) {
        HSLog.i(NewsManager.TAG, "NP refreshNews: " + isVideo);
        newsPages.setRefreshing(true);
        isRefreshing = true;
        loadNews(from);
    }

    public void loadNews(String from) {
        HSLog.i(NewsManager.TAG, "NP loadNews: " + isVideo);
        if (Networks.isNetworkAvailable(-1)) {
            NewsManager.getInstance().fetchNews(newsResource, this, isVideo);
            NewsManager.logNewsListShow(from);
        } else {
            showToast(R.string.news_network_failed_toast);
        }
    }

    @Override public void onNewsLoaded(NewsResultBean bean, int size) {
        HSLog.i(NewsManager.TAG, "NP onNewsLoaded " + (bean != null ? bean.articlesList.size() : 0));

        isRefreshing = false;

        if (bean != null && bean.articlesList != null && bean.articlesList.size() > 0) {
            String newContentID = bean.articlesList.get(0).recoid;
            if (newsPages.isRefreshing()) {
                if (TextUtils.equals(newContentID, lastNewsContentID)) {
                    showToast(R.string.news_no_news_update);
                } else {
                    if (size > 0) {
                        showToast(String.format(getResources().getString(R.string.news_news_update), String.valueOf(size)));
                    }
                    newsList.scrollToPosition(0);
                }
            } else {
                if (size > 0) {
                    showToast(String.format(getResources().getString(R.string.news_news_update), String.valueOf(size)));
                }
            }
            lastNewsContentID = newContentID;

            if (newsResource != null && newsResource.articlesList != null && newsResource != bean) {
                for (NewsArticle article : newsResource.articlesList) {
                    if (article.item_type == 8 && article instanceof NewsNativeAdBean) {
                        ((NewsNativeAdBean) article).acbNativeAd.release();
                    }
                }
            }

            newsResource = bean;
            lastNewsSize = newsResource.articlesList.size();
            adapter.notifyDataSetChanged();
            HSGlobalNotificationCenter.sendNotification(NewsFrame.LOAD_NEWS_SUCCESS);
            if (newsPages.isRefreshing()) {
                logger.logNewsLoad(true, true);
            } else {
                logger.logNewsLoad(false, true);
            }
        } else {
            HSGlobalNotificationCenter.sendNotification(NewsFrame.LOAD_NEWS_FAILED);
            if (newsPages.isRefreshing()) {
                logger.logNewsLoad(true, false);
            } else {
                logger.logNewsLoad(false, false);
            }
        }

        newsPages.setRefreshing(false);
    }

    @Override public void onRefresh() {
        HSLog.i(NewsManager.TAG, "NP onRefresh: " + newsPages.isRefreshing());
        isRefreshing = true;
        if (newsPages.isRefreshing()) {
            loadNews("Refresh");
        }
    }

    public void scrollToTop() {
        HSLog.i(NewsManager.TAG, "scrollToTop");
        newsList.scrollToPosition(0);
    }

    public void onSelected(boolean onSelected) {
        mSelected = onSelected;
        if (mSelected) {
            for (Runnable doWhenSelectedTask : mSelectedRunnable) {
                doWhenSelectedTask.run();
            }
            mSelectedRunnable.clear();
        }
    }

    protected void showToast(int toast) {
        if (getContext() instanceof ColorPhoneActivity) {
            if (((ColorPhoneActivity) getContext()).isNewsTab()){
                Toasts.showToast(toast);
            }
        }
    }

    protected void showToast(String toast) {
        if (getContext() instanceof ColorPhoneActivity) {
            if (((ColorPhoneActivity) getContext()).isNewsTab()){
                Toasts.showToast(toast);
            }
        }
    }

    protected class NewsAdapter extends RecyclerView.Adapter {

        static final int NEWS_TYPE_ITEM     = 100;
        static final int NEWS_TYPE_BIG      = 101;
        public static final int NEWS_TYPE_FOOT     = 102;
        static final int NEWS_TYPE_NATIVE   = 103;
        static final int NEWS_TYPE_VIDEO    = 104;

        static final int NEWS_TYPE_NONE     = 0;
        static final int NEWS_TYPE_RIGHT    = 1;
        static final int NEWS_TYPE_LEFT     = 2;
        static final int NEWS_TYPE_BOTTOM   = 3;
        static final int NEWS_TYPE_TEXT     = 4;
        static final int NEWS_TYPE_IMAGES   = 5;

        @Override public int getItemViewType(int position) {
            if (position == NewsPage.NewsAdapter.this.getItemCount() - 1) {
                return NEWS_TYPE_FOOT;
            }

            if (isVideo) {
                return NEWS_TYPE_VIDEO;
            }

            if (newsResource.articlesList.size() > position) {
                NewsArticle article = newsResource.articlesList.get(position);

                if (article.item_type == 8) {
                    return NEWS_TYPE_NATIVE;
                }

                if (article.style_type == 0) {
                    if (article.thumbnails != null) {
                        if (article.thumbnails.size() > 2) {
                            return NEWS_TYPE_IMAGES;
                        } else if (article.thumbnails.size() > 0) {
                            return NEWS_TYPE_RIGHT;
                        }
                    }
                    return NEWS_TYPE_TEXT;
                } else {
                    return article.style_type;
                }
            }

            return NEWS_TYPE_ITEM;
        }

        public int getHeadCount() {
            return 0;
        }

        @NonNull @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = null;
            switch (viewType) {
                case NEWS_TYPE_ITEM:
                case NEWS_TYPE_RIGHT:
                case NEWS_TYPE_TEXT:
                    view = LayoutInflater.from(getContext()).inflate(R.layout.news_item_layout, parent, false);
                    return new NewsBeanItemHolder(view);
                case NEWS_TYPE_LEFT:
                    view = LayoutInflater.from(getContext()).inflate(R.layout.news_left_layout, parent, false);
                    return new NewsBeanItemHolder(view);
                case NEWS_TYPE_BOTTOM:
                    view = LayoutInflater.from(getContext()).inflate(R.layout.news_bottom_layout, parent, false);
                    return new NewsBeanItemHolder(view);
                case NEWS_TYPE_IMAGES:
                    view = LayoutInflater.from(getContext()).inflate(R.layout.news_image_layout, parent, false);
                    return new NewsBeanImageHolder(view);
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
                    if (BuildConfig.DEBUG) {
                        throw new RuntimeException("error News Type: " + viewType);
                    } else {
                        view = LayoutInflater.from(getContext()).inflate(R.layout.news_item_layout, parent, false);
                        return new NewsBeanItemHolder(view);
                    }
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            int type = NewsPage.NewsAdapter.this.getItemViewType(position);
            HSLog.i(NewsManager.TAG, "NP onBindViewHolder is position: " + position + "   type: " + type);

            onBindNewsHolder(holder, position, type);
        }

        protected void onBindNewsHolder(RecyclerView.ViewHolder holder, int position, int type) {
            if (itemViewPadding != 0) {
                holder.itemView.setPadding(itemViewPadding, holder.itemView.getPaddingTop(), itemViewPadding, holder.itemView.getPaddingBottom());
            }

            if (type == NEWS_TYPE_FOOT) {
                NewsFootLoadingHolder loadingHolder = (NewsFootLoadingHolder) holder;
                loadingHolder.loading.setAlpha(0);
                loadingHolder.loading.animate().alpha(1).setDuration(200).start();
                return;
            }

            NewsArticle article = null;
            if (newsResource.articlesList.size() > position) {
                article = newsResource.articlesList.get(position);
                if (article == null) {
                    return;
                }
            }

            if (type == NEWS_TYPE_NATIVE) {
                if (holder instanceof NewsNativeHolder && article instanceof NewsNativeAdBean) {
                    ((NewsNativeHolder) holder).bindView((NewsNativeAdBean) article);
                } else {
                    HSLog.w(NewsManager.TAG, "NewsNative is error: " + article + "   \n holder " + holder);
                }
                return;
            }

            NewsBeanItemHolder beanHolder = (NewsBeanItemHolder) holder;
            beanHolder.bindNewsBean(article, type);
        }

        @Override public int getItemCount() {
            return (newsResource != null && newsResource.articlesList.size() > 0) ? newsResource.articlesList.size() + 1 : 0;
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

            image.setBackground(BackgroundDrawables.createBackgroundDrawable(getResources().getColor(R.color.black_10_transparent), Dimensions.pxFromDp(4), false));
        }

        void bindNewsBean(NewsArticle bean, int type) {
            String url = null;
            if (bean == null) {
                HSLog.w(NewsManager.TAG, "NewsBeanItemHolder bindView bean ==  null");
                return;
            }

            if (bean.thumbnails != null && bean.thumbnails.size() > 0) {
                url = bean.thumbnails.get(0).getUrl();
            }

            title.setText(bean.title);
            time.setVisibility(View.GONE);

            resource.setText(bean.source_name);
            if (type == NewsAdapter.NEWS_TYPE_TEXT) {
                image.setVisibility(GONE);
            } else {
                GlideApp.with(image)
                        .asDrawable()
                        .load(url)
                        .into(image);
            }

            itemView.setOnClickListener(v -> {
                HSLog.i(NewsManager.TAG, "NP onClicked: " + bean.url);

                showNewsDetail(bean.url, type);
             });
        }
    }

    private class NewsBeanImageHolder extends NewsBeanItemHolder {
        ImageView image2;
        ImageView image3;

        NewsBeanImageHolder(View itemView) {
            super(itemView);
            image2 = itemView.findViewById(R.id.news_icon_iv2);
            image3 = itemView.findViewById(R.id.news_icon_iv3);

            image2.setBackground(BackgroundDrawables.createBackgroundDrawable(getResources().getColor(R.color.black_10_transparent), Dimensions.pxFromDp(4), false));
            image3.setBackground(BackgroundDrawables.createBackgroundDrawable(getResources().getColor(R.color.black_10_transparent), Dimensions.pxFromDp(4), false));
        }

        void bindNewsBean(NewsArticle bean, int type) {
            super.bindNewsBean(bean, type);
            if (bean.thumbnails != null && bean.thumbnails.size() > 2) {
                String url = bean.thumbnails.get(1).getUrl();
                GlideApp.with(image2)
                        .asDrawable()
                        .load(url)
                        .into(image2);

                url = bean.thumbnails.get(2).getUrl();
                GlideApp.with(image3)
                        .asDrawable()
                        .load(url)
                        .into(image3);
            }
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
        TextView resource;
        TextView time;

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

            resource = view.findViewById(R.id.news_resource_tv);
            time = view.findViewById(R.id.news_time_tv);

            adContainer.addContentView(view);
            adContainer.setAdTitleView(mTitleTv);
            adContainer.setAdBodyView(mDescriptionTv);
            adContainer.setAdPrimaryView(mAdImageContainer);
            adContainer.setAdChoiceView(mAdChoice);
            adContainer.setAdIconView(mAdIconView);
            adContainer.setAdActionView(mActionBtn);

        }

        void bindView(NewsNativeAdBean bean) {
            adContainer.fillNativeAd(bean.acbNativeAd, "");
            bean.acbNativeAd.setNativeClickListener(acbAd -> {
                logger.logAdClick();
            });

            mDescriptionTv.setText(bean.acbNativeAd.getTitle());
            String title = bean.acbNativeAd.getBody();
            if (TextUtils.isEmpty(title)) {
                title = bean.acbNativeAd.getSubtitle();
            }
            if (TextUtils.isEmpty(title)) {
                title = bean.acbNativeAd.getTitle();
            }
            mTitleTv.setText(title);

            resource.setText(bean.acbNativeAd.getTitle());
            time.setVisibility(GONE);

            if (mSelected) {
                logger.logAdShow();
            } else {
                addSelectedRunnableOnce(() -> logger.logAdShow());
            }
        }
    }

    private ArrayList<Runnable> mSelectedRunnable = new ArrayList<>();
    private void addSelectedRunnableOnce(Runnable runnable) {
        mSelectedRunnable.add(runnable);
    }

    private void showNewsDetail(String url, int type) {
        if (Networks.isNetworkAvailable(-1)) {
            HSLog.i(NewsManager.TAG, "UrlDecode:" + URLDecoder.decode(url));
            Navigations.startActivitySafely(getContext(), WebViewActivity.newIntent(URLDecoder.decode(url), false, WebViewActivity.FROM_LIST));

            logger.logShowNewsDetail(true, type == NewsAdapter.NEWS_TYPE_VIDEO);
        } else {
            logger.logShowNewsDetail(false, type == NewsAdapter.NEWS_TYPE_VIDEO);
            showToast(R.string.news_network_failed_toast);
        }
    }

    protected class EventLogger {

        protected void logListSlide() {
            Analytics.logEvent("News_List_Slide");
        }

        protected void logNewsLoad(boolean isRefresh, boolean success) {
            if (isRefresh) {
                Analytics.logEvent("News_List_Refresh", Analytics.FLAG_LOG_FABRIC|Analytics.FLAG_LOG_UMENG, "Result", (success ? "Success" : "Fail"));
            } else {
                Analytics.logEvent("News_List_LoadMore", Analytics.FLAG_LOG_FABRIC|Analytics.FLAG_LOG_UMENG, "Result", (success ? "Success" : "Fail"));
            }
        }

        protected void logAdClick() {
            Analytics.logEvent("News_List_Ad_Click");
        }

        protected void logAdShow() {
            Analytics.logEvent("News_List_Ad_Show");
        }

        protected void logShowNewsDetail(boolean hasNetwork, boolean isVideo) {
            if (hasNetwork) {
                Analytics.logEvent("News_Details_Show",
                        "NewsType", (isVideo ? "Video" : "News") );
            } else {
                Analytics.logEvent("Network_Connection_Failed", Analytics.FLAG_LOG_FABRIC | Analytics.FLAG_LOG_UMENG);
            }
        }

    }
}
