package com.honeycomb.colorphone.news;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.util.Utils;
import com.honeycomb.colorphone.view.GlideApp;
import com.ihs.commons.utils.HSLog;

public class NewsPage extends ConstraintLayout implements NewsManager.NewsLoadListener, SwipeRefreshLayout.OnRefreshListener {
    private NewsResultBean newsResource;
    private RecyclerView newsList;
    private SwipeRefreshLayout refreshLayout;
    private NewsAdapter adapter;
    private View noNetWorkPage;
    private View loading;

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

        showNoNetworkPage();
    }

    public void showNoNetworkPage() {
        noNetWorkPage.setVisibility(VISIBLE);
        View action = noNetWorkPage.findViewById(R.id.news_no_network_action);
        action.setOnClickListener(v -> {
            loadNews();
        });
        newsList.setVisibility(GONE);
        loading.setVisibility(GONE);
    }

    private void initRecyclerView() {
        adapter = new NewsAdapter();
        newsList.setAdapter(adapter);
        newsList.setLayoutManager(new LinearLayoutManager(getContext()));

        newsList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int lastVisibleItem ;
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                //判断RecyclerView的状态 是空闲时，同时，是最后一个可见的ITEM时才加载
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == adapter.getItemCount()) {
                    HSLog.i(NewsManager.TAG, "NP onScrollStateChanged: " + refreshLayout.isRefreshing());
                    if (!refreshLayout.isRefreshing()) {
                        refreshLayout.setRefreshing(true);
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
        newsList.setVisibility(GONE);
        noNetWorkPage.setVisibility(GONE);
        loading.setVisibility(VISIBLE);
    }

    @Override public void onNewsLoaded(NewsResultBean bean) {
        HSLog.i(NewsManager.TAG, "NP onNewsLoaded");
        refreshLayout.setRefreshing(false);

        if (bean != null) {
            loading.setVisibility(GONE);
            newsList.setVisibility(VISIBLE);

            newsResource = bean;
            adapter.notifyDataSetChanged();
        } else {
            showNoNetworkPage();
        }
    }

    @Override public void onRefresh() {
        HSLog.i(NewsManager.TAG, "NP onRefresh: " + refreshLayout.isRefreshing());
        if (refreshLayout.isRefreshing()) {
            NewsManager.getInstance().fetchNews();
        }
    }

    private class NewsAdapter extends RecyclerView.Adapter {

        private static final int NEWS_TYPE_ITEM = 0;
        private static final int NEWS_TYPE_BIG = 1;

        @Override public int getItemViewType(int position) {
//            return super.getItemViewType(position);
            return (position % 5 == 0) ? NEWS_TYPE_BIG : NEWS_TYPE_ITEM;
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
            }
            NewsBeanItemHolder holder = new NewsBeanItemHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            NewsBean bean = newsResource.content.get(position);
            NewsBeanItemHolder beanHolder = (NewsBeanItemHolder) holder;
            String url = null;
            int type = getItemViewType(position);
            if (type == NEWS_TYPE_BIG) {
                url = bean.images.mainImage.url;
            } else {
                url = bean.images.mainImageThumbnail.url;
            }

            beanHolder.title.setText(bean.title);
            beanHolder.time.setText(String.valueOf(" · " + Utils.getNewDate(bean.publishedAt)));
            beanHolder.resource.setText(bean.contentSourceDisplay);
            GlideApp.with(beanHolder.image)
                    .asDrawable()
                    .load(url)
                    .into(beanHolder.image);
        }

        @Override public int getItemCount() {
            return newsResource != null ? newsResource.totalItems : 0;
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
    }
}
