package com.colorphone.smartlocker.itemview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.colorphone.lock.LockerCustomConfig;
import com.colorphone.lock.R;
import com.colorphone.smartlocker.bean.IFeedBean;
import com.colorphone.smartlocker.utils.DailyNewsUtils;
import com.colorphone.smartlocker.utils.TouTiaoFeedUtils;
import com.colorphone.smartlocker.viewholder.NoImageViewHolder;
import com.ihs.commons.utils.HSLog;

public class NoImageListItem implements IDailyNewsListItem<RecyclerView.ViewHolder> {

    private String category;
    private IFeedBean feedBean;
    private boolean hasViewed = false;
    private Context context;

    public NoImageListItem(String category, IFeedBean feedBean) {
        this.category = category;
        this.feedBean = feedBean;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_no_image;
    }

    @Override
    public RecyclerView.ViewHolder createViewHolder(Context context) {
        return new NoImageViewHolder(LayoutInflater.from(context).inflate(getLayoutRes(), null));
    }

    @Override
    public void bindViewHolder(final Context context, RecyclerView.ViewHolder holder, int position) {
        if (!(holder instanceof NoImageViewHolder)) {
            return;
        }

        this.context = context;

        NoImageViewHolder viewHolder = (NoImageViewHolder) holder;

        if (feedBean.isStick()) {
            viewHolder.stick.setVisibility(View.VISIBLE);
        } else {
            viewHolder.stick.setVisibility(View.GONE);
        }
        viewHolder.title.setText(feedBean.getTitle());
        viewHolder.source.setText(feedBean.getSource());
        viewHolder.comment.setText(context.getString(R.string.comment_count, feedBean.getCommentCount()));
        viewHolder.time.setText(TouTiaoFeedUtils.getTime(feedBean.getPublishTime()));

        viewHolder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LockerCustomConfig.getLogger().logEvent("feed_news_clicked", "place", "daily", "content", "text", "channel", category);
                LockerCustomConfig.getLogger().logEvent("NewsFeed_Clicked");

                DailyNewsUtils.jumpToNewsDetail(context, feedBean.getArticleUrl());
            }
        });
    }

    @Override
    public void release() {

    }

    @Override
    public void logViewedEvent() {
        if (!hasViewed) {
            hasViewed = true;
            HSLog.d("Daily news viewed ", "category = " + category + "  item is " + getClass().getSimpleName());
            LockerCustomConfig.getLogger().logEvent("feed_news_viewed", "place", "daily", "content", "text", "channel", category);
        }
    }

    @Override
    public boolean hasViewed() {
        return hasViewed;
    }
}
