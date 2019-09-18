package com.honeycomb.colorphone.lifeassistant;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
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
import com.honeycomb.colorphone.util.ActivityUtils;
import com.honeycomb.colorphone.util.Analytics;
import com.ihs.commons.utils.HSLog;
import com.ihs.weather.CurrentCondition;
import com.ihs.weather.DailyForecast;
import com.ihs.weather.HSWeatherQueryResult;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;
import com.superapps.util.Threads;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import colorphone.acb.com.libweather.WeatherActivity;
import colorphone.acb.com.libweather.WeatherClockManager;
import colorphone.acb.com.libweather.WeatherUtils;

public class LifeAssistantNewsPage extends NewsPage {
    private ImageView closeView;

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

        itemViewPadding = Dimensions.pxFromDp(16);
    }

    @Override protected void onFinishInflate() {
        super.onFinishInflate();
        setEnabled(false);

        newsList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int position = layoutManager.findFirstVisibleItemPosition();
                if (position > 0) {
                    closeView.setVisibility(VISIBLE);
                } else {
                    closeView.setVisibility(GONE);
                }
            }
        });

//        DividerItemDecoration divider = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
//        divider.setDrawable(getResources().getDrawable(R.drawable.empty_divider));
//        newsList.addItemDecoration(divider, 0);
//        newsList.addItemDecoration(divider, 1);

        Analytics.logEvent("Message_News_Show");
    }

    public void setCloseView(ImageView view) {
        closeView = view;
    }

    @Override
    protected void initAdapter() {
        adapter = new LifeAssistantNewsAdapter();
    }

    protected class LifeAssistantNewsAdapter extends NewsAdapter {
        static final int NEWS_TYPE_HEAD_WEATHER = 1001;
        static final int NEWS_TYPE_HEAD_TITLE   = 1002;
        static final int NEWS_TYPE_HEAD_NO_NEWS = 1003;

        boolean isNoNews = false;

        @Override public int getItemViewType(int position) {
            if (position == 0) {
                return NEWS_TYPE_HEAD_WEATHER;
            }

            if (position == 1) {
                return NEWS_TYPE_HEAD_TITLE;
            }

            if (isNoNews && position == 2) {
                return NEWS_TYPE_HEAD_NO_NEWS;
            }

            if (position == getItemCount() - 1) {
                return NEWS_TYPE_FOOT;
            }

            position = position - getHeadCount();

            return super.getItemViewType(position);
        }

        public int getHeadCount() {
            return 2;
        }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = null;

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

            HSLog.i(NewsManager.TAG, "LNP onBindViewHolder is position: " + position + "   type: " + viewType);

            if (viewType == NEWS_TYPE_HEAD_WEATHER) {
                ((WeatherViewHolder) holder).bindView();
                return;
            }

            if (itemViewPadding != 0) {
                holder.itemView.setPadding(itemViewPadding, holder.itemView.getPaddingTop(), itemViewPadding, holder.itemView.getPaddingBottom());
            }

            if (viewType == NEWS_TYPE_HEAD_TITLE) {
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
            private View mNightLoadingContainer;
            private View mMorningLoadingContainer;

            private TextView mNoData;

            private TextView mMTemperature;
            private TextView mMTemperatureDes;
            private TextView mMDate;
            private ImageView mMCondition;

            private TextView mTemperature;
            private TextView mTemperatureDes;
            private ImageView mCondition;
            private final List<WeatherDaysItemView> mDays = new ArrayList<>();

            private TextView welcomeTitle;
            private TextView welcomeContent;
            private ImageView welcomeSetting;
            private ImageView welcomeClose;

            private boolean mDataRequestFinished;
            private volatile HSWeatherQueryResult mData;

            public WeatherViewHolder(View itemView) {
                super(itemView);
                mNightContainer = itemView.findViewById(R.id.night_container);
                mMorningContainer = itemView.findViewById(R.id.morning_container);
                mNoneDataContainer = itemView.findViewById(R.id.none_data_container);
                mNightLoadingContainer = itemView.findViewById(R.id.night_loading_container);
                mMorningLoadingContainer = itemView.findViewById(R.id.morning_loading_container);

                mNoData = itemView.findViewById(R.id.no_data);

                mTemperature = itemView.findViewById(R.id.detail_weather_temperature);
                mTemperatureDes = itemView.findViewById(R.id.detail_weather_temperature_des);
                mCondition = itemView.findViewById(R.id.detail_weather_icon);

                mMTemperature = itemView.findViewById(R.id.morning__weather_temperature);
                mMTemperatureDes = itemView.findViewById(R.id.morning__weather_temperature_des);
                mMCondition = itemView.findViewById(R.id.morning__weather_icon);
                mMDate = itemView.findViewById(R.id.morning__weather_date);

                welcomeTitle = itemView.findViewById(R.id.welcome_title);
                welcomeContent = itemView.findViewById(R.id.welcome_content);
                welcomeSetting = itemView.findViewById(R.id.life_assistant_setting);
                welcomeClose = itemView.findViewById(R.id.life_assistant_close);

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

                welcomeSetting.setOnClickListener(view -> {
                    Analytics.logEvent("Life_Assistant_Settings_Click");
                    Navigations.startActivitySafely(getContext(), LifeAssistantSettingActivity.class);
                });

                welcomeClose.setOnClickListener(view -> {
                    closeView.performClick();
                });

                int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                if (hour >= 5 && hour < 12) {
                    ActivityUtils.setCustomColorStatusBar((Activity) getContext(), 0xFF199FEB);

                    itemView.setBackgroundResource(R.drawable.life_assiatant_morning_weather_bg);
                    mMorningLoadingContainer.setVisibility(VISIBLE);
                    mMorningContainer.setVisibility(INVISIBLE);
                    mMorningContainer.setOnClickListener(view -> {
                        if (mDataRequestFinished && mData != null) {
                            Context context = getContext();
                            Intent intent = new Intent(context, WeatherActivity.class);
                            Navigations.startActivitySafely(context, intent);

                            Analytics.logEvent("Life_Assistant_Weather_Click", "Type", "Morning");
                        }
                    });


                    mNightContainer.setVisibility(GONE);
                    mNoneDataContainer.setVisibility(GONE);

                    welcomeTitle.setAlpha(1f);
                    welcomeContent.setAlpha(1f);
                    welcomeContent.setText(LifeAssistantConfig.getWelcomeStr(true));
                    welcomeSetting.setImageResource(R.drawable.life_assistant_setting_morning);
                    welcomeClose.setImageResource(R.drawable.life_assistant_close_morning);

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
                            Threads.postOnMainThread(() -> applyData(false));
                        }
                    }.start();
                } else {
                    ActivityUtils.setCustomColorStatusBar((Activity) getContext(), 0xFF14131F);
                    itemView.setBackgroundColor(0xFF14131F);
                    mMorningContainer.setVisibility(GONE);
                    mNoneDataContainer.setVisibility(GONE);

                    mNightLoadingContainer.setVisibility(VISIBLE);
                    mNightContainer.setVisibility(INVISIBLE);
                    mNightContainer.setOnClickListener(view -> {
                        if (mDataRequestFinished && mData != null) {
                            Context context = getContext();
                            Intent intent = new Intent(context, WeatherActivity.class);
                            Navigations.startActivitySafely(context, intent);

                            Analytics.logEvent("Life_Assistant_Weather_Click", "Type", "Evening");
                        }
                    });

                    welcomeTitle.setAlpha(0.9f);
                    welcomeContent.setAlpha(0.6f);
                    welcomeContent.setText(LifeAssistantConfig.getWelcomeStr(false));
                    welcomeSetting.setImageResource(R.drawable.life_assistant_setting_night);
                    welcomeClose.setImageResource(R.drawable.life_assistant_close_night);

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
                            Threads.postOnMainThread(() -> applyData(true));
                        }
                    }.start();
                }
            }

            private void applyData(boolean isNight) {
                if (isNight) {
                    if (mData != null) {
                        HSWeatherQueryResult weather = mData;
                        mNoneDataContainer.setVisibility(GONE);
                        mMorningContainer.setVisibility(GONE);
                        mNightLoadingContainer.setVisibility(GONE);
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
                        mNoData.setTextColor(Color.WHITE);
                        mNoneDataContainer.setVisibility(VISIBLE);
                        mNightContainer.setVisibility(INVISIBLE);
                    }
                } else {
                    if (mData != null) {
                        HSWeatherQueryResult weather = mData;
                        mNoneDataContainer.setVisibility(GONE);
                        mNightContainer.setVisibility(GONE);
                        mMorningLoadingContainer.setVisibility(GONE);
                        mMorningContainer.setVisibility(VISIBLE);

                        CurrentCondition condition = weather.getCurrentCondition();
                        mMTemperature.setText(condition.getCelsius() + "°");
                        mMTemperatureDes.setText(WeatherClockManager.getInstance().getSimpleConditionDescription(condition.getCondition()));
                        mMCondition.setImageResource(WeatherUtils.getWeatherConditionIconResourceId(weather));
                        mMDate.setText(getDateString());
                    } else {
                        mNoData.setTextColor(Color.BLACK);
                        mNoneDataContainer.setVisibility(VISIBLE);
                        mMorningContainer.setVisibility(INVISIBLE);
                    }
                }
            }

            private String getDateString() {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH) + 1;
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int day_of_week = calendar.get(Calendar.DAY_OF_WEEK);

                String dayOfWeek;
                Resources resources = getContext().getResources();
                switch (day_of_week) {
                    case 1:
                        dayOfWeek = resources.getString(R.string.sunday);
                        break;
                    case 2:
                        dayOfWeek = resources.getString(R.string.monday);
                        break;
                    case 3:
                        dayOfWeek = resources.getString(R.string.tuesday);
                        break;
                    case 4:
                        dayOfWeek = resources.getString(R.string.wednesday);
                        break;
                    case 5:
                        dayOfWeek = resources.getString(R.string.thursday);
                        break;
                    case 6:
                        dayOfWeek = resources.getString(R.string.friday);
                        break;
                    case 7:
                        dayOfWeek = resources.getString(R.string.saturday);
                        break;
                    default:
                        dayOfWeek = "";
                }

                return month + "月" + day + "日 " + dayOfWeek;

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
            if (!isRefresh) {
                Analytics.logEvent("Life_Assistant_News_LoadMore", Analytics.FLAG_LOG_FABRIC|Analytics.FLAG_LOG_UMENG, "Result", (success ? "Success" : "Fail"));
            }
        }

        protected void logAdClick() {
        }

        protected void logAdShow() {
            Analytics.logEvent("Life_Assistant_News_Ad_Show");
        }

        protected void logShowNewsDetail(boolean hasNetwork, boolean isVideo) {
            if (hasNetwork) {
                Analytics.logEvent("Life_Assistant_News_Details_Show");
            }
        }
    }
}
