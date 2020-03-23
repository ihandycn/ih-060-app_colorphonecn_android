package com.colorphone.smartlocker.itemview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.colorphone.lock.R;
import com.colorphone.smartlocker.bean.IFeedBean;
import com.colorphone.smartlocker.utils.NewsUtils;
import com.colorphone.smartlocker.utils.TouTiaoFeedUtils;
import com.colorphone.smartlocker.viewholder.RightImageViewHolder;


public class RightImageListItem implements INewsListItem<RecyclerView.ViewHolder> {

    private IFeedBean feedBean;
    private boolean hasViewed = false;

    @Nullable
    private IDailyNewsClickListener clickListener;

    public RightImageListItem(String category, IFeedBean feedBean) {
        this.feedBean = feedBean;
    }

    //设置ClickListener 将由外部处理Item点击事件
    public void setClickListener(@Nullable IDailyNewsClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_right_image;
    }

    @Override
    public RecyclerView.ViewHolder createViewHolder(Context context) {
        return new RightImageViewHolder(LayoutInflater.from(context).inflate(getLayoutRes(), null));
    }

    @Override
    public void bindViewHolder(final Context context, RecyclerView.ViewHolder holder, int position) {
        if (!(holder instanceof RightImageViewHolder)) {
            return;
        }

        RightImageViewHolder viewHolder = (RightImageViewHolder) holder;

        if (feedBean.isStick()) {
            viewHolder.stick.setVisibility(View.VISIBLE);
        } else {
            viewHolder.stick.setVisibility(View.GONE);
        }
        viewHolder.title.setText(feedBean.getTitle());
        viewHolder.source.setText(feedBean.getSource());
        viewHolder.comment.setText(context.getString(R.string.comment_count, feedBean.getCommentCount()));
        viewHolder.time.setText(TouTiaoFeedUtils.getTime(feedBean.getPublishTime()));

        Glide.with(context).load(feedBean.getCoverImageList().get(0))
                .apply(new RequestOptions().placeholder(R.drawable.no_image_bg).error(R.drawable.no_image_bg))
                .into(viewHolder.imageView);

        viewHolder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener != null) {
                    clickListener.onClick(feedBean.getArticleUrl());
                    return;
                }

                NewsUtils.jumpToNewsDetail(context, feedBean.getArticleUrl());
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
        }
    }

    @Override
    public void detachedFromWindow() {

    }

    @Override
    public boolean hasViewed() {
        return hasViewed;
    }
}
