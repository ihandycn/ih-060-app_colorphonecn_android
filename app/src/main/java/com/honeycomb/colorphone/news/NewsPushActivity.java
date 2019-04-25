package com.honeycomb.colorphone.news;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.colorphone.lock.PopupView;
import com.colorphone.lock.RipplePopupView;
import com.colorphone.lock.lockscreen.chargingscreen.ChargingScreenUtils;
import com.colorphone.lock.util.ViewUtils;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.activity.ColorPhoneActivity;
import com.honeycomb.colorphone.util.Utils;
import com.honeycomb.colorphone.view.GlideApp;
import com.honeycomb.colorphone.view.RoundImageVIew;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;

import java.text.SimpleDateFormat;
import java.util.Date;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class NewsPushActivity extends HSAppCompatActivity {
    private NewsResultBean newsResource;
    private boolean pushTypeAsNewsTab = false;

    private RipplePopupView menuPopupView;
    private PopupView mCloseLockerPopupView;
    private ViewGroup rootView;

    public static void start(Context context) {
//        if (NewsTest.canShowNewsAlert()) {
            Navigations.startActivity(context, NewsPushActivity.class);
//        } else {
//            HSLog.w(NewsManager.TAG, "NewsPushActivity not start");
//        }
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_news);
        rootView = findViewById(R.id.container);

        pushTypeAsNewsTab = !NewsTest.isNewsAlertWithBigPic();

        newsResource = NewsManager.getInstance().getPushBean();
        initRecyclerView();

        View view = findViewById(R.id.news_push_close);
        view.setOnClickListener(v -> {
            finish();
        });

        view = findViewById(R.id.news_push_more);
        view.setOnClickListener(v -> {
            showMenuPopupWindow(NewsPushActivity.this, v);
        });

        view = findViewById(R.id.news_push_view_more);
        view.setOnClickListener(v -> {
            Navigations.startActivitySafely(NewsPushActivity.this, ColorPhoneActivity.newIntent(NewsPushActivity.this));
            finish();
            NewsTest.logNewsEvent("news_alert_morebtn_click");
        });

        TextView timeView = findViewById(R.id.toolbar_time_tv);
        TextView title = findViewById(R.id.toolbar_title_tv);

        AppBarLayout appBarLayout = findViewById(R.id.appbar_layout);
        appBarLayout.addOnOffsetChangedListener((appBar, verticalOffset) -> {
            float progress = verticalOffset * 1f / (appBar.getHeight() - Dimensions.pxFromDp(48));
            HSLog.i(NewsManager.TAG, "AppBarLayout progress: " + progress);
            timeView.setAlpha(1 + progress);
            title.setTextSize((24 - 18) * (1 + progress) + 18);
        });

        configTextView(timeView);

        NewsTest.logNewsEvent("news_alert_show");
        NewsTest.recordShowNewsAlertTime();
    }

    @Override public void onBackPressed() {
        if (NewsTest.isNewsAlertAllowBack()) {
            NewsTest.recordShowNewsAlertTime();
            super.onBackPressed();
        } else {
            HSLog.w(NewsManager.TAG, "isNewsAlertAllowBack false");
        }
    }

    @Override public void finish() {
        super.finish();
        NewsTest.recordShowNewsAlertTime();
    }

    private void initRecyclerView() {
        RecyclerView newsList = findViewById(R.id.news_list);
        newsList.setLayoutManager(new LinearLayoutManager(this));
        newsList.setAdapter(new NewsAdapter());

        if (pushTypeAsNewsTab) {
            DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
            divider.setDrawable(getResources().getDrawable(R.drawable.news_divider));
            newsList.addItemDecoration(divider);

            newsList.setPadding(Dimensions.pxFromDp(18), Dimensions.pxFromDp(8), Dimensions.pxFromDp(18), Dimensions.pxFromDp(8));
            newsList.setBackground(BackgroundDrawables.createBackgroundDrawable(Color.WHITE, Dimensions.pxFromDp(12), false));
        }
    }

    private void configTextView(TextView timeView) {
        if (newsResource != null && newsResource.totalItems > 0) {
            NewsBean news = newsResource.content.get(0);
            long newsTime = news.publishedAt * DateUtils.SECOND_IN_MILLIS;
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d");
            timeView.setText(sdf.format(new Date(newsTime)));
        }
    }

    private class NewsAdapter extends RecyclerView.Adapter {

        private static final int NEWS_TYPE_ITEM = 0;
        private static final int NEWS_TYPE_BIG = 1;

        @Override public int getItemViewType(int position) {
            if (pushTypeAsNewsTab) {
                return (position % 5 == 0) ? NEWS_TYPE_BIG : NEWS_TYPE_ITEM;
            } else {
                return NEWS_TYPE_BIG;
            }
        }

        @NonNull @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = null;
            switch (viewType) {
                case NEWS_TYPE_ITEM:
                    view = LayoutInflater.from(NewsPushActivity.this).inflate(R.layout.news_item_layout, parent, false);
                    break;
                case NEWS_TYPE_BIG:
                    view = LayoutInflater.from(NewsPushActivity.this).inflate(R.layout.news_big_layout, parent, false);
                    break;
            }
            return new NewsBeanItemHolder(view);
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

            float radius = Dimensions.pxFromDp(8);

            if (!pushTypeAsNewsTab) {
                beanHolder.itemView.setPadding(0, 5, 0, 5);
                if (position == 0) {
                    beanHolder.image.setRadius(radius, radius, 0, 0);
                } else if (position == getItemCount() - 1) {
                    beanHolder.image.setRadius(0, 0, radius, radius);
                } else {
                    beanHolder.image.setRadius(0, 0, 0, 0);
                }
            }

            holder.itemView.setOnClickListener(v -> {
                HSLog.i(NewsManager.TAG, "NP onClicked: " + position);

                Intent[] intents = new Intent[] {
                        ColorPhoneActivity.newIntent(NewsPushActivity.this),
                        WebViewActivity.newIntent(bean.contentURL, false)
                };
                Navigations.startActivitiesSafely(NewsPushActivity.this, intents);
                finish();

                NewsTest.logNewsEvent("news_alert_news_click");
            });
        }

        @Override public int getItemCount() {
            return newsResource != null ? newsResource.totalItems : 0;
        }
    }

    private class NewsBeanItemHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView resource;
        TextView time;
        RoundImageVIew image;

        NewsBeanItemHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.news_title_tv);
            resource = itemView.findViewById(R.id.news_resource_tv);
            time = itemView.findViewById(R.id.news_time_tv);
            image = itemView.findViewById(R.id.news_icon_iv);
        }
    }

    private void showMenuPopupWindow(Context context, View anchorView) {
        if (menuPopupView == null) {
            menuPopupView = new RipplePopupView(context, rootView);
            View view = LayoutInflater.from(context).inflate(com.colorphone.lock.R.layout.charging_screen_popup_window,
                    rootView, false);
            TextView txtCloseChargingBoost = view.findViewById(com.colorphone.lock.R.id.tv_close);
            txtCloseChargingBoost.requestLayout();
            txtCloseChargingBoost.setOnClickListener(v -> {
                if (ChargingScreenUtils.isFastDoubleClick()) {
                    return;
                }
//                LockerCustomConfig.getLogger().logEvent("Locker_DisableLocker_Clicked");
                menuPopupView.dismiss();
                showLockerCloseDialog();
            });

            menuPopupView.setOutSideBackgroundColor(Color.TRANSPARENT);
            menuPopupView.setContentView(view);
            menuPopupView.setOutSideClickListener(v -> menuPopupView.dismiss());
        }

        menuPopupView.showAsDropDown(anchorView,
                -(getResources().getDimensionPixelOffset(R.dimen.news_push_pop_menu_offset_x) - anchorView.getWidth()),
                -(getResources().getDimensionPixelOffset(R.dimen.news_push_pop_menu_to_top_height)
                        + anchorView.getHeight()) / 2);
    }

    private void showLockerCloseDialog() {
        if (mCloseLockerPopupView == null) {
            mCloseLockerPopupView = new PopupView(NewsPushActivity.this, rootView);
            View content = LayoutInflater.from(NewsPushActivity.this).inflate(com.colorphone.lock.R.layout.locker_popup_dialog, null);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams((int) (Dimensions
                    .getPhoneWidth(NewsPushActivity.this) * 0.872f), WRAP_CONTENT);
            content.setLayoutParams(layoutParams);
            TextView title = ViewUtils.findViewById(content, com.colorphone.lock.R.id.title);
            TextView hintContent = ViewUtils.findViewById(content, com.colorphone.lock.R.id.hint_content);
            AppCompatButton buttonYes = ViewUtils.findViewById(content, com.colorphone.lock.R.id.button_yes);
            AppCompatButton buttonNo = ViewUtils.findViewById(content, com.colorphone.lock.R.id.button_no);
            title.setText(R.string.news_push_disable_confirm);
            hintContent.setText(R.string.news_push_disable_confirm_detail);
            buttonNo.setText(com.colorphone.lock.R.string.charging_screen_close_dialog_positive_action);
            buttonNo.setOnClickListener(v -> mCloseLockerPopupView.dismiss());
            buttonYes.setText(com.colorphone.lock.R.string.charging_screen_close_dialog_negative_action);
            buttonYes.setOnClickListener(v -> {
                finish();
//                Toast.makeText(NewsPushActivity.this, com.colorphone.lock.R.string.locker_diabled_success, Toast.LENGTH_SHORT).show();
                mCloseLockerPopupView.dismiss();
            });
            mCloseLockerPopupView.setOutSideBackgroundColor(0xB3000000);
            mCloseLockerPopupView.setContentView(content);
            mCloseLockerPopupView.setOutSideClickListener(v -> mCloseLockerPopupView.dismiss());
        }
        mCloseLockerPopupView.showInCenter();
    }
}
