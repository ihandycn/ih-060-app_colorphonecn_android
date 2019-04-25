package com.honeycomb.colorphone.news;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.colorphone.lock.util.ViewUtils;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.honeycomb.colorphone.util.Utils;
import com.honeycomb.colorphone.view.GlideApp;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Navigations;
import com.superapps.util.Networks;

import net.appcloudbox.ads.base.ContainerView.AcbNativeAdContainerView;
import net.appcloudbox.ads.base.ContainerView.AcbNativeAdIconView;
import net.appcloudbox.ads.base.ContainerView.AcbNativeAdPrimaryView;

public class NewsPage extends ConstraintLayout implements NewsManager.NewsLoadListener, SwipeRefreshLayout.OnRefreshListener {
    private NewsResultBean newsResource;
    private RecyclerView newsList;
    private SwipeRefreshLayout refreshLayout;
    private NewsAdapter adapter;
    private View noNetWorkPage;
    private View loading;
    private boolean isRefreshing = false;

    public NewsPage(Context context) {
        this(context, null);
    }

    public NewsPage(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NewsPage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        NewsManager.getInstance().setNewsLoadListener(this);
    }

    @Override protected void onFinishInflate() {
        super.onFinishInflate();

        refreshLayout = findViewById(R.id.news_swipe_layout);
        newsList = findViewById(R.id.news_list);
        noNetWorkPage = findViewById(R.id.news_no_network);
        loading = findViewById(R.id.news_loading);

        refreshLayout.setOnRefreshListener(this);

        initRecyclerView();

        if (Networks.isNetworkAvailable(-1)) {
            loadNews();
        } else {
            showNoNetworkPage();
        }
    }

    public void showNoNetworkPage() {
        noNetWorkPage.setVisibility(VISIBLE);
        View action = noNetWorkPage.findViewById(R.id.news_no_network_action);
        action.setOnClickListener(v -> {
            loadNews();
        });
        refreshLayout.setVisibility(GONE);
        loading.setVisibility(GONE);
    }

    private void initRecyclerView() {
        adapter = new NewsAdapter();
        newsList.setAdapter(adapter);
        newsList.setLayoutManager(new LinearLayoutManager(getContext()));
        DividerItemDecoration divider = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        divider.setDrawable(getResources().getDrawable(R.drawable.news_divider));
        newsList.addItemDecoration(divider);

        newsList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int lastVisibleItem ;
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                //判断RecyclerView的状态 是空闲时，同时，是最后一个可见的ITEM时才加载
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == adapter.getItemCount()) {
                    HSLog.i(NewsManager.TAG, "NP onScrollStateChanged: " + refreshLayout.isRefreshing());
                    if (!isRefreshing) {
                        isRefreshing = true;
                        NewsManager.getInstance().fetchLaterNews();
                    }
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

    public void loadNews() {
        HSLog.i(NewsManager.TAG, "NP loadNews");
        NewsManager.getInstance().fetchNews();
        refreshLayout.setVisibility(GONE);
        noNetWorkPage.setVisibility(GONE);
        loading.setVisibility(VISIBLE);
    }

    @Override public void onNewsLoaded(NewsResultBean bean) {
        HSLog.i(NewsManager.TAG, "NP onNewsLoaded");
        refreshLayout.setRefreshing(false);
        isRefreshing = false;

        if (bean != null) {
            loading.setVisibility(GONE);
            refreshLayout.setVisibility(VISIBLE);

            newsResource = bean;
            adapter.notifyDataSetChanged();
        } else {
            showNoNetworkPage();
        }
    }

    @Override public void onRefresh() {
        HSLog.i(NewsManager.TAG, "NP onRefresh: " + refreshLayout.isRefreshing());
        isRefreshing = true;
        if (refreshLayout.isRefreshing()) {
            NewsManager.getInstance().fetchNews();
        }
    }

    public void onScrollToTop() {
        HSLog.i(NewsManager.TAG, "onScrollToTop");
        newsList.scrollToPosition(0);
    }

    private class NewsAdapter extends RecyclerView.Adapter {

        static final int NEWS_TYPE_ITEM = 0;
        static final int NEWS_TYPE_BIG = 1;
        static final int NEWS_TYPE_FOOT = 2;
        static final int NEWS_TYPE_NATIVE = 3;

        @Override public int getItemViewType(int position) {
            if (position == getItemCount() - 1) {
                return NEWS_TYPE_FOOT;
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
                    break;
                case NEWS_TYPE_BIG:
                    view = LayoutInflater.from(getContext()).inflate(R.layout.news_big_layout, parent, false);
                    break;
                case NEWS_TYPE_FOOT:
                    view = LayoutInflater.from(getContext()).inflate(R.layout.news_foot_loading, parent, false);
                    return new NewsFootLoadingHolder(view);
                case NEWS_TYPE_NATIVE:
                    return new NewsNativeHolder(new AcbNativeAdContainerView(getContext()));
            }
            NewsBeanItemHolder holder = new NewsBeanItemHolder(view);
            return holder;
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

            NewsBeanItemHolder beanHolder = (NewsBeanItemHolder) holder;
            beanHolder.bindNewsBean(bean, type);
        }

        @Override public int getItemCount() {
            return newsResource != null ? newsResource.totalItems + 1 : 0;
        }
    }

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
            if (type == NewsAdapter.NEWS_TYPE_BIG) {
                url = bean.images.mainImage.url;
            } else {
                url = bean.images.mainImageThumbnail.url;
            }

            title.setText(bean.title);
            time.setText(String.valueOf(" · " + Utils.getNewDate(bean.publishedAt)));
            resource.setText(bean.contentSourceDisplay);
            GlideApp.with(image)
                    .asDrawable()
                    .load(url)
                    .into(image);

            itemView.setOnClickListener(v -> {
                HSLog.i(NewsManager.TAG, "NP onClicked");
                Navigations.startActivitySafely(getContext(), WebViewActivity.newIntent(bean.contentURL, false));

                LauncherAnalytics.logEvent("mainview_newstab_news_click",
                        "type", (type == NewsAdapter.NEWS_TYPE_BIG ? "image" : "imagepreview"),
                        "user", Utils.isNewUser() ? "new" : "upgrade");
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
