package com.honeycomb.colorphone.news;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.call.assistant.ui.RipplePopupView;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.activity.ColorPhoneActivity;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.honeycomb.colorphone.util.Utils;
import com.honeycomb.colorphone.view.GlideApp;
import com.honeycomb.colorphone.view.RoundImageVIew;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;

import java.text.SimpleDateFormat;
import java.util.Date;

public class NewsPushActivity extends HSAppCompatActivity {
    private NewsResultBean newsResource;
    private boolean pushTypeAsNewsTab = false;

    private RipplePopupView menuPopupView;
    private AlertDialog closeDialog;
    private ViewGroup rootView;
    private boolean showTime = true;

    public static void start(Context context) {
        Navigations.startActivity(context, NewsPushActivity.class);
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
            LauncherAnalytics.logEvent("news_alert_settings_click");
        });

        TextView tv = findViewById(R.id.news_push_view_more);
        tv.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);

        tv.setOnClickListener(v -> {
            Navigations.startActivitySafely(NewsPushActivity.this, ColorPhoneActivity.newIntent(NewsPushActivity.this));
            finish();
            NewsTest.logAutopilotEvent("news_alert_morebtn_click");
            LauncherAnalytics.logEvent("news_alert_morebtn_click", "type", NewsTest.getNewsAlertType());
        });

        TextView timeView = findViewById(R.id.toolbar_time_tv);
        TextView title = findViewById(R.id.toolbar_title_tv);

        AppBarLayout appBarLayout = findViewById(R.id.appbar_layout);
        appBarLayout.addOnOffsetChangedListener((appBar, verticalOffset) -> {
            float progress = verticalOffset * 1f / (appBar.getHeight() - Dimensions.pxFromDp(48));
            timeView.setAlpha(1 + progress);
            title.setTextSize((24 - 18) * (1 + progress) + 18);
        });

        configTextView(timeView);

        NewsTest.logAutopilotEvent("news_alert_show");
        LauncherAnalytics.logEvent("news_alert_show", "type", NewsTest.getNewsAlertType());
        NewsTest.recordShowNewsAlertTime();

        showTime = HSConfig.optBoolean(true, "Application", "News", "NewsUpdateTimeShow");
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

    @Override protected void onDestroy() {
        super.onDestroy();
        if (closeDialog != null) {
            closeDialog.dismiss();
            closeDialog = null;
        }
    }

    private void initRecyclerView() {
        RecyclerView newsList = findViewById(R.id.news_list);
        newsList.setLayoutManager(new LinearLayoutManager(this));
        newsList.setAdapter(new NewsAdapter());

        newsList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    LauncherAnalytics.logEvent("news_alert_slide", "type", NewsTest.getNewsAlertType());
                }
            }

            @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

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
            if (showTime) {
                beanHolder.time.setText(String.valueOf(" · " + Utils.getNewsDate(bean.publishedAt)));
            } else {
                beanHolder.time.setVisibility(View.GONE);
            }
            beanHolder.resource.setText(bean.contentSourceDisplay);
            GlideApp.with(beanHolder.image)
                    .asDrawable()
                    .load(url)
                    .into(beanHolder.image);

            float radius = Dimensions.pxFromDp(8);

            if (!pushTypeAsNewsTab) {
                if (beanHolder.mark != null) {
                    beanHolder.mark.setBackground(null);
                    beanHolder.mark.setImageDrawable(new ColorDrawable(getResources().getColor(android.R.color.black)));
                    beanHolder.mark.setAlpha(0.3f);
                }

                beanHolder.itemView.setPadding(0, 5, 0, 5);
                if (position == 0) {
                    beanHolder.image.setRadius(radius, radius, 0, 0);
                    if (beanHolder.mark != null) {
                        beanHolder.mark.setRadius(radius, radius, 0, 0);
                    }
                } else if (position == getItemCount() - 1) {
                    beanHolder.image.setRadius(0, 0, radius, radius);
                    if (beanHolder.mark != null) {
                        beanHolder.mark.setRadius(0, 0, radius, radius);
                    }
                } else {
                    beanHolder.image.setRadius(0, 0, 0, 0);
                    if (beanHolder.mark != null) {
                        beanHolder.mark.setRadius(0, 0, 0, 0);
                    }
                }
            }

            holder.itemView.setOnClickListener(v -> {
                HSLog.i(NewsManager.TAG, "NP onClicked: " + position);

                Intent[] intents = new Intent[] {
                        ColorPhoneActivity.newIntent(NewsPushActivity.this),
                        WebViewActivity.newIntent(bean.contentURL, false, WebViewActivity.FROM_ALERT)
                };
                Navigations.startActivitiesSafely(NewsPushActivity.this, intents);
                finish();

                NewsTest.logAutopilotEvent("news_alert_news_click");
                LauncherAnalytics.logEvent("news_alert_news_click", "type", NewsTest.getNewsAlertType());
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
        RoundImageVIew mark;
        RoundImageVIew image;

        NewsBeanItemHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.news_title_tv);
            resource = itemView.findViewById(R.id.news_resource_tv);
            time = itemView.findViewById(R.id.news_time_tv);
            image = itemView.findViewById(R.id.news_icon_iv);
            mark = itemView.findViewById(R.id.news_mark_view);
        }
    }

    private void showMenuPopupWindow(Context context, View anchorView) {
        if (menuPopupView == null) {
            menuPopupView = new RipplePopupView(context, rootView);
            View view = LayoutInflater.from(context).inflate(R.layout.screen_popup_window,
                    rootView, false);
            TextView txtCloseChargingBoost = view.findViewById(R.id.tv_close);
            txtCloseChargingBoost.requestLayout();
            txtCloseChargingBoost.setOnClickListener(v -> {
                if (Utils.isFastDoubleClick()) {
                    return;
                }
//                LockerCustomConfig.getLogger().logEvent("Locker_DisableLocker_Clicked");
                menuPopupView.dismiss();
//                showLockerCloseDialog();
                showCloseDialog();

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

    private void showCloseDialog() {
        if (closeDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CloseDialogTheme);

            String title = getString(R.string.news_push_disable_confirm);
            SpannableString spannableStringTitle = new SpannableString(title);
            spannableStringTitle.setSpan(
                    new ForegroundColorSpan(0xDF000000),
                    0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setTitle(title);

            String message = getString(R.string.news_push_disable_confirm_detail);
            SpannableString spannableStringMessage = new SpannableString(message);
            spannableStringMessage.setSpan(
                    new ForegroundColorSpan(0x8A000000),
                    0, message.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setMessage(spannableStringMessage);

            builder.setPositiveButton(getString(R.string.acb_phone_alert_close_dialog_positive_action),
                    (dialogInterface, i) -> {
                if (closeDialog == null) {
                    return;
                }
                closeDialog.dismiss();
                closeDialog = null;
            });

            builder.setNegativeButton(getString(R.string.acb_phone_alert_close_dialog_negative_action),
                    (dialog, i) -> {
                if (closeDialog == null) {
                    return;
                }

                closeDialog.dismiss();
                closeDialog = null;

                finish();
                LauncherAnalytics.logEvent("news_alert_settings_disable_success");
                NewsTest.setNewsEnable(false);
            });

            closeDialog = builder.create();

            closeDialog.setOnShowListener(dialog -> {
                Button negativeButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                negativeButton.setTextColor(ContextCompat.getColor(HSApplication.getContext(),
                        R.color.textLightGray));
            });
        }
        closeDialog.show();
    }
}
