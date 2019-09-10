package com.honeycomb.colorphone.lifeassistant;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.news.NewsManager;
import com.honeycomb.colorphone.news.NewsPage;
import com.honeycomb.colorphone.util.Analytics;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

import java.util.Calendar;

public class LifeAssistantNewsPage extends NewsPage {

    public LifeAssistantNewsPage(@NonNull Context context) {
        super(context);
    }

    public LifeAssistantNewsPage(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override protected void init() {
        super.init();
        onSelected(true);
        logger = new LifeAssistantEventLogger();
    }

    @Override protected void onFinishInflate() {
        super.onFinishInflate();
        setEnabled(false);

        Analytics.logEvent("Message_News_Show");
    }

    @Override
    protected void initAdapter() {
        adapter = new LifeAssistantNewsAdapter();
    }

    protected class LifeAssistantNewsAdapter extends NewsAdapter {
        static final int NEWS_TYPE_HEAD_WELCOME = 1000;
        static final int NEWS_TYPE_HEAD_WEATHER = 1001;
        static final int NEWS_TYPE_HEAD_TITLE   = 1002;
        static final int NEWS_TYPE_HEAD_NO_NEWS = 1003;

        boolean isNoNews = false;

        @Override public int getItemViewType(int position) {
            if (position == 0) {
                return NEWS_TYPE_HEAD_WELCOME;
            }

            if (position == 1) {
                return NEWS_TYPE_HEAD_WEATHER;
            }

            if (position == 2) {
                return NEWS_TYPE_HEAD_TITLE;
            }

            if (isNoNews && position == 3) {
                return NEWS_TYPE_HEAD_NO_NEWS;
            }

            if (position == getItemCount() - 1) {
                return NEWS_TYPE_FOOT;
            }

            position = position - getHeadCount();

            return super.getItemViewType(position);
        }

        public int getHeadCount() {
            return 3;
        }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = null;
            if (viewType == NEWS_TYPE_HEAD_WELCOME) {
                view = LayoutInflater.from(getContext()).inflate(R.layout.news_head_welcome, parent, false);
                return new WelcomeHolder(view);
            }

            if (viewType == NEWS_TYPE_HEAD_TITLE) {
                view = LayoutInflater.from(getContext()).inflate(R.layout.news_head_title, parent, false);
                return new TitleViewHolder(view);
            }

            if (viewType == NEWS_TYPE_HEAD_WEATHER) {
                view = LayoutInflater.from(getContext()).inflate(R.layout.news_head_title, parent, false);
                return new TitleViewHolder(view);
            }

            if (isNoNews && viewType == NEWS_TYPE_HEAD_NO_NEWS) {
                view = LayoutInflater.from(getContext()).inflate(R.layout.news_no_network, parent, false);
                return new NewsNoNetworkHolder(view);
            }

            return super.onCreateViewHolder(parent, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            int viewType = getItemViewType(position);

            HSLog.i(NewsManager.TAG, "ENP onBindViewHolder is position: " + position + "   type: " + viewType);

            if (itemViewPadding != 0) {
                holder.itemView.setPadding(itemViewPadding, holder.itemView.getPaddingTop(), itemViewPadding, holder.itemView.getPaddingBottom());
            }

            if (viewType == NEWS_TYPE_HEAD_TITLE
                    || viewType == NEWS_TYPE_HEAD_WEATHER) {
                return;
            }

            if (viewType == NEWS_TYPE_HEAD_WELCOME) {
                ((WelcomeHolder) holder).bindView();
                return;
            }

            if (isNoNews && viewType == NEWS_TYPE_HEAD_NO_NEWS) {
                ((NewsNoNetworkHolder) holder).bindView();
                return;
            }

            onBindNewsHolder(holder, position - getHeadCount(), viewType);
        }

        @Override public int getItemCount() {
            int size = super.getItemCount();
            if (size == 0) {
                size = 1;
                isNoNews = true;
            } else {
                isNoNews = false;
            }
            size += getHeadCount();
            return size;
        }

        private class TitleViewHolder extends ViewHolder {
            TitleViewHolder(View view) {
                super(view);
                TextView tv = view.findViewById(R.id.title_tv);
                tv.setText(R.string.life_assistant_news_title);
            }
        }

        private class WelcomeHolder extends ViewHolder {
            TextView title;
            TextView content;
            WelcomeHolder(View view) {
                super(view);
                title = view.findViewById(R.id.welcome_title);
                content = view.findViewById(R.id.welcome_content);
            }

            void bindView() {
                int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                if (hour >= 5 && hour < 11) {
                    title.setText(R.string.life_assistant_welcome_moring);
                } else {
                    title.setText(R.string.life_assistant_welcome_afternoon);
                }
                // TODO: 问候语
            }
        }

        private class NewsNoNetworkHolder extends RecyclerView.ViewHolder {
            NewsNoNetworkHolder(View itemView) {
                super(itemView);
            }

            void bindView() {
                View action = itemView.findViewById(R.id.news_no_network_action);
                action.setBackground(BackgroundDrawables.createBackgroundDrawable(0xff696681, Dimensions.pxFromDp(21), true));
                action.setOnClickListener(v -> {
                    loadNews("");
                });
            }
        }
    }

    protected class LifeAssistantEventLogger extends NewsPage.EventLogger {
        protected void logListSlide() {
//            Analytics.logEvent("Message_News_List_Slide");
        }

        protected void logNewsLoad(boolean isRefresh, boolean success) {
//            if (!isRefresh) {
//                Analytics.logEvent("Message_News_LoadMore", Analytics.FLAG_LOG_FABRIC|Analytics.FLAG_LOG_UMENG, "Result", (success ? "Success" : "Fail"));
//            }
        }

        protected void logAdClick() {
        }

        protected void logAdShow() {
//            Analytics.logEvent("Message_News_Ad_Show");
        }

        protected void logShowNewsDetail(boolean hasNetwork, boolean isVideo) {
//            if (hasNetwork) {
//                Analytics.logEvent("Message_News_Details_Show",
//                        "NewsType", (isVideo ? "Video" : "News") );
//            }
        }
    }
}
