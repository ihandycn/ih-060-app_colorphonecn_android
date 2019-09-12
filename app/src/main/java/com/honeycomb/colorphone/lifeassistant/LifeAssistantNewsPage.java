package com.honeycomb.colorphone.lifeassistant;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.news.NewsManager;
import com.honeycomb.colorphone.news.NewsPage;
import com.honeycomb.colorphone.util.Analytics;
import com.ihs.commons.utils.HSLog;
import com.ihs.weather.CurrentCondition;
import com.ihs.weather.DailyForecast;
import com.ihs.weather.HSWeatherQueryResult;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Threads;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import colorphone.acb.com.libweather.WeatherActivity;
import colorphone.acb.com.libweather.WeatherClockManager;
import colorphone.acb.com.libweather.WeatherUtils;
import colorphone.acb.com.libweather.util.CommonUtils;

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
                view = LayoutInflater.from(getContext()).inflate(R.layout.news_weather, parent, false);
                return new WeatherViewHolder(view);
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

            if (viewType == NEWS_TYPE_HEAD_TITLE) {
                return;
            }

            if (viewType == NEWS_TYPE_HEAD_WEATHER) {
                ((WeatherViewHolder) holder).bindView();
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

        private class WeatherViewHolder extends ViewHolder {
            private View mNightContainer;
            private View mMorningContainer;
            private View mNoneDataContainer;

            private TextView mTemperature;
            private TextView mTemperatureDes;
            private ImageView mCondition;
            private final List<WeatherDaysItemView> mDays = new ArrayList<>();

            private boolean mDataRequestFinished;
            private volatile HSWeatherQueryResult mData;

            public WeatherViewHolder(View itemView) {
                super(itemView);
                mNightContainer = itemView.findViewById(R.id.night_container);
                mMorningContainer = itemView.findViewById(R.id.morning_container);
                mNoneDataContainer = itemView.findViewById(R.id.none_data_container);

                mTemperature = itemView.findViewById(R.id.detail_weather_temperature);
                mTemperatureDes = itemView.findViewById(R.id.detail_weather_temperature_des);
                mCondition = itemView.findViewById(R.id.detail_weather_icon);

                mDays.add(itemView.findViewById(R.id.weather_days_first));
                mDays.add(itemView.findViewById(R.id.weather_days_second));
                mDays.add(itemView.findViewById(R.id.weather_days_third));
                mDays.add(itemView.findViewById(R.id.weather_days_forth));
                mDays.add(itemView.findViewById(R.id.weather_days_fifth));

                // 在ViewHolder里做数据请求 实在是不对的，暂时先这样吧
                WeatherClockManager.getInstance().updateWeather(new WeatherClockManager.WeatherUpdateListener() {
                    @Override
                    public void onWeatherUpdateFinished() {
                        mData = WeatherClockManager.getInstance().getWeather();
                        synchronized (WeatherViewHolder.this) {
                            mDataRequestFinished = true;
                            WeatherViewHolder.this.notifyAll();
                        }
                    }
                });
            }

            void bindView () {
                itemView.setOnClickListener(view -> {
                    if (mDataRequestFinished && mData != null) {
                        Context context = getContext();
                        Intent intent = new Intent(context, WeatherActivity.class);
                        CommonUtils.startActivitySafely(context, intent);
                    }
                });
                int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                if (hour >= 5 && hour < 11) {
                    itemView.setBackgroundColor(Color.RED);
                    mMorningContainer.setVisibility(VISIBLE);

                    mNightContainer.setVisibility(GONE);
                    mNoneDataContainer.setVisibility(GONE);
                } else {
                    itemView.setBackgroundColor(0xff14131F);
                    mMorningContainer.setVisibility(GONE);
                    mNoneDataContainer.setVisibility(GONE);

                    mNightContainer.setVisibility(VISIBLE);

                    new Thread() {
                        @Override
                        public void run() {
                            synchronized (WeatherViewHolder.this) {
                                while (!mDataRequestFinished) {
                                    try {
                                        WeatherViewHolder.this.wait();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            Threads.postOnMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (mData != null) {
                                        HSWeatherQueryResult weather = mData;
                                        mNoneDataContainer.setVisibility(GONE);
                                        mNightContainer.setVisibility(VISIBLE);

                                        CurrentCondition condition = weather.getCurrentCondition();
                                        mTemperature.setText(condition.getCelsius() + "°");
                                        mTemperatureDes.setText(WeatherClockManager.getInstance().getSimpleConditionDescription(condition.getCondition()));
                                        mCondition.setImageResource(WeatherUtils.getWeatherConditionIconResourceId(weather));

                                        List<DailyForecast> dailyForecasts = weather.getDailyForecasts();
                                        for (int i = 0; i < dailyForecasts.size() && i < mDays.size(); i++) {
                                            DailyForecast dailyForecast = dailyForecasts.get(i);
                                            WeatherDaysItemView daysItemView = mDays.get(i);
                                            daysItemView.bindDailyForecast(dailyForecast, i == 0, i == 1, false);
                                        }

                                    } else {
                                        mNoneDataContainer.setVisibility(VISIBLE);
                                        mNightContainer.setVisibility(GONE);
                                    }
                                }
                            });
                        }
                    }.start();
                }
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
