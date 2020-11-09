package com.colorphone.smartlocker;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.colorphone.lock.BuildConfig;
import com.colorphone.lock.LockerCustomConfig;
import com.colorphone.lock.PopupView;
import com.colorphone.lock.R;
import com.colorphone.lock.RipplePopupView;
import com.colorphone.lock.lockscreen.LockScreen;
import com.colorphone.lock.lockscreen.LockScreenStarter;
import com.colorphone.lock.lockscreen.chargingscreen.ChargingScreenSettings;
import com.colorphone.lock.lockscreen.locker.LockerSettings;
import com.colorphone.lock.util.ViewUtils;
import com.colorphone.smartlocker.baidu.BaiduFeedManager;
import com.colorphone.smartlocker.bean.BaiduFeedBean;
import com.colorphone.smartlocker.bean.BaiduFeedItemsBean;
import com.colorphone.smartlocker.h5.BaiduH5Config;
import com.colorphone.smartlocker.h5.ProgressWebView;
import com.colorphone.smartlocker.itemview.INewsItemClickListener;
import com.colorphone.smartlocker.itemview.INewsListItem;
import com.colorphone.smartlocker.itemview.LoadMoreItem;
import com.colorphone.smartlocker.itemview.RightImageListItem;
import com.colorphone.smartlocker.itemview.SmartLockerAdListItem;
import com.colorphone.smartlocker.itemview.ThreeImageListItem;
import com.colorphone.smartlocker.utils.DisplayUtils;
import com.colorphone.smartlocker.utils.NetworkStatusUtils;
import com.colorphone.smartlocker.utils.NewsUtils;
import com.colorphone.smartlocker.utils.StatusBarUtils;
import com.colorphone.smartlocker.utils.TouTiaoFeedUtils;
import com.colorphone.smartlocker.view.NewsDetailView;
import com.colorphone.smartlocker.view.RefreshView;
import com.colorphone.smartlocker.view.SlidingFinishLayout;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.libcharging.HSChargingManager;
import com.superapps.util.Dimensions;
import com.superapps.util.Threads;

import net.appcloudbox.UnreleasedAdWatcher;
import net.appcloudbox.ads.base.AcbNativeAd;
import net.appcloudbox.ads.common.utils.AcbError;
import net.appcloudbox.ads.nativead.AcbNativeAdLoader;
import net.appcloudbox.ads.nativead.AcbNativeAdManager;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.ihs.device.common.utils.Utils.getPackageName;

public class SmartLockerScreen extends LockScreen implements INotificationObserver {
    private static final String TAG = "SmartLockerScreen";

    public static final String EXTRA_INT_BATTERY_LEVEL_PERCENT = "EXTRA_INT_BATTERY_LEVEL_PERCENT";

    private static final long REFRESH_MIN_DURATION = 1000L;

    private long refreshStartTime;
    private long viewedStartTime;

    private boolean isLongScreen;
    private boolean isLoading;
    private boolean isFirstLoadData = true;
    private boolean isNormalFinishing = true;
    private boolean currentPowerConnected;
    private boolean isRefreshAdShow = false; //记录广告的机会利用率使用

    private int startType;
    private String appPlacement;
    private String categoryParam;

    private RelativeLayout rootLayout;
    private SlidingFinishLayout smartLockerContainer;

    private NewsDetailView newsDetailView;

    private TextView chargingTipTextView;
    private TextView chargingPercentTextView;
    private TextView chargingDateWeekTextView;

    private TextView normalTimeTextView;
    private TextView normalDateWeekTextView;

    private ViewGroup chargingTopContainer;
    private ViewGroup normalTopContainer;

    private RefreshView refreshView;
    private RecyclerView recyclerView;
    private NewsAdapter feedAdapter;
    private LinearLayoutManager linearLayoutManager;

    private int newsCount = 0;  //已加载的新闻数量
    private int targetNewsCount = 2;    //新闻数量达到多少条时增加广告
    private int adIntervalIndex = 0;
    private int[] adInterval = {2, 3};
    private int onStartTimes = 0; //新闻第一次出现时不刷新广告
    private boolean isResume;

    @Nullable
    private AcbNativeAdLoader adLoader;
    private List<AcbNativeAd> nativeAdList = new ArrayList<>();

    @Nullable
    private ProgressWebView webView;

    @Nullable
    private RipplePopupView menuPopupWindow;

    private PopupView mCloseLockerPopupView;

    private Context context;
    private Handler handler = new Handler();

    private BroadcastReceiver timeTickReceiver;

    private BroadcastReceiver powerStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TextUtils.isEmpty(intent.getAction())) {
                return;
            }
            switch (Objects.requireNonNull(intent.getAction())) {
                case Intent.ACTION_POWER_CONNECTED:
                    HSLog.d(TAG, "processPowerStateChanged Intent.ACTION_POWER_CONNECTED");
                    processPowerStateChanged(true);
                    break;
                case Intent.ACTION_POWER_DISCONNECTED:
                    HSLog.d(TAG, "processPowerStateChanged Intent.ACTION_POWER_DISCONNECTED");
                    processPowerStateChanged(false);
                    break;
                default:
                    break;
            }
        }
    };

    private BroadcastReceiver onHomeClickReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(intent.getAction())) {
                String reason = intent.getStringExtra("reason");
                if (null != reason && reason.equals("homekey")) {
                    isNormalFinishing = true;
                    dismiss(getContext(), true);
                }
            }
        }
    };

    private Runnable displaySuccessChecker = new Runnable() {
        @Override
        public void run() {
            LockScreenStarter.getInstance().onScreenDisplayed();
        }
    };

    private HSChargingManager.IChargingListener chargingListener = new HSChargingManager.IChargingListener() {
        @Override
        public void onBatteryLevelChanged(int preBatteryLevel, int curBatteryLevel) {
            HSLog.d(TAG, "onBatteryLevelChanged() preBatteryLevel=" + preBatteryLevel + " curBatteryLevel=" + curBatteryLevel);
            updateBatteryState(curBatteryLevel);
        }

        @Override
        public void onChargingStateChanged(HSChargingManager.HSChargingState preChargingState, HSChargingManager.HSChargingState curChargingState) {
            HSLog.d(TAG, "processPowerStateChanged onChargingStateChanged()");
        }

        @Override
        public void onChargingRemainingTimeChanged(int chargingRemainingMinutes) {
            HSLog.d(TAG, "onChargingRemainingTimeChanged() chargingRemainingMinutes" + chargingRemainingMinutes);
        }

        @Override
        public void onBatteryTemperatureChanged(float preBatteryTemperature, float batteryTemperature) {
        }
    };

    private BroadcastReceiver screenOnReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isResume && (newsDetailView == null || newsDetailView.getVisibility() == View.GONE)) {
                refreshAd();
            }
        }
    };

    public void setup(ViewGroup root, Bundle extra) {
        super.setup(root, extra);
        HSLog.d(TAG, "SmartLockerScreen onCreate");

        context = root.getContext();

        registerScreenOn();

        HSGlobalNotificationCenter.addObserver(SmartLockerConstants.NOTIFICATION_AD_ITEM_CHANGED, this);
        HSGlobalNotificationCenter.addObserver(SmartLockerConstants.NOTIFICATION_FEED_PAGE_SLIDE, this);

        categoryParam = BaiduFeedManager.CATEGORY_ALL;
        isLongScreen = (DisplayUtils.getScreenWithNavigationBarHeight() * 1f / DisplayUtils.getScreenWidth(context)) > 16 / 9f;
        startType = extra.getInt(SmartLockerManager.EXTRA_START_TYPE, SmartLockerManager.EXTRA_VALUE_START_BY_LOCKER);

        appPlacement = LockerCustomConfig.get().getNewsFeedAdName();

        rootLayout = root.findViewById(R.id.activity_smart_locker_feeds);
        smartLockerContainer = root.findViewById(R.id.locker_container);
        smartLockerContainer.setEnableScrollUp(false);
        smartLockerContainer.setSlidingFinishListener(new SlidingFinishLayout.OnSlidingFinishListener() {
            @Override
            public void onSlidingFinish(int slidingState) {
                isNormalFinishing = true;
                dismiss(context, true);

                HSLog.d(TAG, "activity finish by onSlidingFinish");
            }
        });


        smartLockerContainer.setPadding(0, StatusBarUtils.getStatusBarHeight(context), 0, 0);

        chargingTipTextView = root.findViewById(R.id.charging_tip_text_view);
        chargingPercentTextView = root.findViewById(R.id.charging_percent_text_view);
        chargingDateWeekTextView = root.findViewById(R.id.charging_date_week_text_view);
        ImageView chargingSettingsImageView = root.findViewById(R.id.charging_setting_image_view);

        normalDateWeekTextView = root.findViewById(R.id.normal_date_week_text_view);
        normalTimeTextView = root.findViewById(R.id.normal_time_text_view);
        ImageView normalSettingsImageView = root.findViewById(R.id.normal_setting_image_view);

        View.OnClickListener settingsListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenuPopupWindow(context, v);
            }
        };
        chargingSettingsImageView.setOnClickListener(settingsListener);
        normalSettingsImageView.setOnClickListener(settingsListener);

        chargingTopContainer = root.findViewById(R.id.charging_top_container);
        normalTopContainer = root.findViewById(R.id.normal_top_container);
        currentPowerConnected = startType != SmartLockerManager.EXTRA_VALUE_START_BY_LOCKER;
        chargingTopContainer.setVisibility(currentPowerConnected ? View.VISIBLE : View.GONE);
        normalTopContainer.setVisibility(currentPowerConnected ? View.GONE : View.VISIBLE);

        if (SmartLockerManager.isShowH5NewsLocker()) {
            initNewsWebView(root);
        } else {
            initNewsNativeView(root);
        }

        initPhoneStateListener();

        viewedStartTime = System.currentTimeMillis();

        LockerCustomConfig.getLogger().logEvent("news_show");
        if (startType == SmartLockerManager.EXTRA_VALUE_START_BY_LOCKER) {
            LockerCustomConfig.getLogger().logEvent("LockScreen_News_Show");
        } else {
            LockerCustomConfig.getLogger().logEvent("ChargingScreen_News_Show");
        }

        if (BuildConfig.DEBUG) {
            UnreleasedAdWatcher.getInstance().setEnabled(false);
        }

        if (!isActivityHost()) {
            onStart();
            onResume();
        }
    }

    private void initNewsWebView(ViewGroup root) {
        webView = root.findViewById(R.id.webview);
        webView.setVisibility(View.VISIBLE);
        webView.initWebView();
        webView.setWebViewStatusChangedListener(new ProgressWebView.WebViewStatusChangedListener() {
            @Override
            public void onWebUrlChange(boolean canGoForward, String currentUrl) {
                super.onWebUrlChange(canGoForward, currentUrl);
            }
        });
        webView.loadUrl(BaiduH5Config.getH5UrlConfig());
    }

    private void initNewsNativeView(ViewGroup root) {
        refreshView = root.findViewById(R.id.refresh_view);
        refreshView.setVisibility(View.VISIBLE);
        recyclerView = root.findViewById(R.id.feeds_recycler_view);
        linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (isLoading) {
                    return;
                }
                if (!recyclerView.canScrollVertically(1)) {
                    loadData(false);
                }
            }
        });
        feedAdapter = new NewsAdapter(context, new ArrayList<INewsListItem<RecyclerView.ViewHolder>>());
        recyclerView.setAdapter(feedAdapter);

        refreshView.setRefreshViewListener(new RefreshView.RefreshViewListener() {
            @Override
            public void onStartRefresh(boolean isPullDown) {
                refreshStartTime = System.currentTimeMillis();
                if (!NetworkStatusUtils.isNetworkConnected(context)) {
                    Toast.makeText(context, context.getString(R.string.no_network_now), Toast.LENGTH_SHORT).show();
                    refreshView.stopRefresh();
                    return;
                }

                isFirstLoadData = true;
                if (isPullDown || feedAdapter.getItemCount() <= 0) {
                    resetNewsAdData();
                    loadData(true);
                }

                if (isPullDown) {
                    if (startType == SmartLockerManager.EXTRA_VALUE_START_BY_LOCKER) {
                        LockerCustomConfig.getLogger().logEvent("LockScreen_News_Refresh");
                    } else {
                        LockerCustomConfig.getLogger().logEvent("ChargingScreen_News_Refresh");
                    }
                }
            }
        });
        resetNewsAdData();
        loadOldData();
    }

    private void resetNewsAdData() {
        newsCount = 0;
        adIntervalIndex = 0;
        targetNewsCount = isLongScreen ? 2 : 1;
    }

    private void initPhoneStateListener() {
        IntentFilter powerStateIntentFilter = new IntentFilter();
        powerStateIntentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        powerStateIntentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        context.registerReceiver(powerStateReceiver, powerStateIntentFilter);

        context.registerReceiver(onHomeClickReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

        HSChargingManager.getInstance().addChargingListener(chargingListener);
        HSChargingManager.getInstance().start();
    }

    private void showMenuPopupWindow(Context context, View parentView) {
        if (menuPopupWindow == null) {
            menuPopupWindow = new RipplePopupView(context, mRootView);
            View view = LayoutInflater.from(context).inflate(R.layout.charging_screen_popup_window, mRootView, false);
            view.setBackground(ContextCompat.getDrawable(context, R.drawable.charging_screen_feeds_popup_window_bg));

            View smartLockerCloseView = view.findViewById(R.id.tv_close);
            smartLockerCloseView.setOnClickListener(new View.OnClickListener() {
                private long lastClickTime;

                @Override
                public void onClick(View v) {
                    if (isFastDoubleClick()) {
                        return;
                    }
                    if (menuPopupWindow != null) {
                        menuPopupWindow.dismiss();
                    }
                    showCloseDialog();
                }

                private boolean isFastDoubleClick() {
                    long currentClickTime = System.currentTimeMillis();
                    long intervalClick = currentClickTime - lastClickTime;
                    if (0 < intervalClick && intervalClick < 500L) {
                        return true;
                    }
                    lastClickTime = currentClickTime;
                    return false;
                }
            });

            menuPopupWindow.setOutSideBackgroundColor(Color.TRANSPARENT);
            menuPopupWindow.setContentView(view);
            menuPopupWindow.setOutSideClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    menuPopupWindow.dismiss();
                }
            });
        }

        menuPopupWindow.showAsDropDown(parentView, -context.getResources().getDimensionPixelSize(R.dimen.charging_feeds_popmenu_margin_right),
                -(context.getResources().getDimensionPixelOffset(R.dimen.charging_screen_menu_to_top_height) + parentView.getHeight()) >> 1);
    }

    private void showCloseDialog() {
        if (mCloseLockerPopupView == null) {
            mCloseLockerPopupView = new PopupView(context, rootLayout);
            View content = LayoutInflater.from(context).inflate(R.layout.locker_popup_dialog, null);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams((int) (Dimensions
                    .getPhoneWidth(context) * 0.872f), WRAP_CONTENT);
            content.setLayoutParams(layoutParams);
            TextView title = ViewUtils.findViewById(content, R.id.title);
            TextView hintContent = ViewUtils.findViewById(content, R.id.hint_content);
            AppCompatButton buttonYes = ViewUtils.findViewById(content, R.id.button_yes);
            AppCompatButton buttonNo = ViewUtils.findViewById(content, R.id.button_no);
            buttonNo.setTextColor(context.getResources().getColor(R.color.primary_green));
            if (startType == SmartLockerManager.EXTRA_VALUE_START_BY_LOCKER) {
                title.setText(R.string.locker_disable_confirm);
                hintContent.setText(R.string.locker_disable_confirm_detail);
            } else {
                title.setText(R.string.charging_screen_close_dialog_title);
                hintContent.setText(R.string.charging_screen_close_dialog_content);
            }
            buttonNo.setText(R.string.charging_screen_close_dialog_positive_action);
            buttonNo.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mCloseLockerPopupView.dismiss();
                }
            });
            buttonYes.setText(R.string.charging_screen_close_dialog_negative_action);
            GradientDrawable mask = new GradientDrawable();
            mask.setColor(Color.WHITE);
            GradientDrawable shape = new GradientDrawable();
            shape.setColor(Color.TRANSPARENT);
            Drawable buttonYesDrawable = new RippleDrawable(ColorStateList.valueOf(rootLayout.getResources().getColor(R.color.ripples_ripple_color)), shape, mask);
            Drawable buttonNoDrawable = new RippleDrawable(ColorStateList.valueOf(rootLayout.getResources().getColor(R.color.ripples_ripple_color)), shape, mask);

            buttonNo.setBackground(buttonYesDrawable);
            buttonYes.setBackground(buttonNoDrawable);
            buttonYes.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (startType == SmartLockerManager.EXTRA_VALUE_START_BY_LOCKER) {
                        LockerSettings.setLockerEnabled(false);
                        LockerCustomConfig.getLogger().logEvent("LockScreen_Disabled");
                    } else {
                        ChargingScreenSettings.setChargingScreenEnabled(false);
                        LockerCustomConfig.getLogger().logEvent("ChargingScreen_Disabled");
                    }
                    HSLog.d(TAG, "activity finish by turn off");
                    isNormalFinishing = true;
                    mCloseLockerPopupView.dismiss();
                    dismiss(context, true);
                }
            });
            mCloseLockerPopupView.setOutSideBackgroundColor(0xB3000000);
            mCloseLockerPopupView.setContentView(content);
            mCloseLockerPopupView.setOutSideClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mCloseLockerPopupView.dismiss();
                }
            });
        }
        mCloseLockerPopupView.showInCenter();
    }

    private void updateTimeAndDateView() {
        Date date = new Date();
        String strTimeFormat = Settings.System.getString(context.getContentResolver(), Settings.System.TIME_12_24);
        String pattern = "24".equals(strTimeFormat) ? "HH:mm" : "hh:mm";
        String timeText = new SimpleDateFormat(pattern, Locale.getDefault()).format(date);
        normalTimeTextView.setText(timeText);

        String dateWeek = new SimpleDateFormat("MM/dd EEEE", Locale.getDefault()).format(date);
        chargingDateWeekTextView.setText(dateWeek);
        normalDateWeekTextView.setText(dateWeek);
    }

    @SuppressLint("SetTextI18n")
    private void updateBatteryState(int batteryLevel) {
        chargingPercentTextView.setText(batteryLevel + "%");
        String chargingLeftText;
        int chargingLeftMinutes = HSChargingManager.getInstance().getChargingLeftMinutes();
        if (chargingLeftMinutes > 60) {
            chargingLeftText = chargingLeftMinutes / 60 + "小时" + chargingLeftMinutes % 60 + "分钟";
        } else {
            chargingLeftText = chargingLeftMinutes + "分钟";
        }
        chargingTipTextView.setText(context.getString(R.string.charging_screen_charged_left_describe2, chargingLeftText));
    }

    private void loadOldData() {
        JSONObject jsonObject = NewsUtils.getLastNews(categoryParam);
        if (jsonObject == null || !NetworkStatusUtils.isNetworkConnected(context)) {
            return;
        }
        feedAdapter.addItems(feedAdapter.getItemCount(), parseBaiduNewsJson(jsonObject));
    }

    private void loadData(final boolean isPullDown) {

        if (!NetworkStatusUtils.isNetworkConnected(HSApplication.getContext())) {
            LockerCustomConfig.getLogger().logEvent("New_Fetch", "reason", "Network");
            return;
        }

        if (isLoading) {
            return;
        }
        isLoading = true;
        BaiduFeedManager.getInstance().loadNews(categoryParam, isFirstLoadData
                ? BaiduFeedManager.LOAD_REFRESH : BaiduFeedManager.LOAD_MORE, new BaiduFeedManager.DataBackListener() {
            @Override
            public void onDataBack(JSONObject response) {
                if (response == null) {
                    isLoading = false;
                    if (NetworkStatusUtils.isNetworkConnected(context)) {
                        Toast.makeText(context, context.getString(R.string.sdk_response_err), Toast.LENGTH_SHORT).show();
                        LockerCustomConfig.getLogger().logEvent("New_Fetch", "reason", "ResponseNull");
                    } else {
                        Toast.makeText(context, context.getString(R.string.no_network_now), Toast.LENGTH_SHORT).show();
                        LockerCustomConfig.getLogger().logEvent("New_Fetch", "reason", "Network");
                    }
                    refreshView.stopRefresh();
                    if (!isPullDown) {
                        if (startType == SmartLockerManager.EXTRA_VALUE_START_BY_LOCKER) {
                            LockerCustomConfig.getLogger().logEvent("LockScreen_News_Loadmore", "result", "no");
                        } else if (startType == SmartLockerManager.EXTRA_VALUE_START_BY_CHARGING_SCREEN_OFF) {
                            LockerCustomConfig.getLogger().logEvent("ChargingScreen_News_Loadmore", "result", "no");
                        }
                    }
                    return;
                }

                if (NewsUtils.getCountOfResponse(response.toString()) < 5) {
                    LockerCustomConfig.getLogger().logEvent("New_Fetch", "reason", "Count");
                } else {
                    LockerCustomConfig.getLogger().logEvent("New_Fetch", "reason", "Success");
                }

                if (!isPullDown) {
                    if (startType == SmartLockerManager.EXTRA_VALUE_START_BY_LOCKER) {
                        LockerCustomConfig.getLogger().logEvent("LockScreen_News_Loadmore", "result", "yes");
                    } else if (startType == SmartLockerManager.EXTRA_VALUE_START_BY_CHARGING_SCREEN_OFF) {
                        LockerCustomConfig.getLogger().logEvent("ChargingScreen_News_Loadmore", "result", "yes");
                    }
                }

                parseData(isPullDown, response);
                isLoading = false;
            }
        });
    }

    private void parseData(boolean isPullDown, JSONObject response) {
        final List<INewsListItem<? extends RecyclerView.ViewHolder>> dailyNewsListItems = parseBaiduNewsJson(response);

        if (isPullDown) {
            feedAdapter.removeAllItem();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    refreshView.stopRefresh();
                }
            }, REFRESH_MIN_DURATION - (System.currentTimeMillis() - refreshStartTime));
        }

        if (isFirstLoadData) {
            feedAdapter.addItem(feedAdapter.getItemCount(), new LoadMoreItem(true));
        }

        feedAdapter.addItems(feedAdapter.getItemCount() - 1, dailyNewsListItems);
        isFirstLoadData = false;
    }

    private List<INewsListItem<? extends RecyclerView.ViewHolder>> parseBaiduNewsJson(JSONObject response) {
        List<INewsListItem<? extends RecyclerView.ViewHolder>> listItems = new ArrayList<>();
        try {
            BaiduFeedItemsBean baiduFeedItemsBean = new BaiduFeedItemsBean(response);
            List<BaiduFeedBean> baiduFeedBeanList = baiduFeedItemsBean.getBaiduFeedBeans();
            if (baiduFeedBeanList == null || baiduFeedBeanList.isEmpty()) {
                return listItems;
            }

            INewsItemClickListener clickListener = new INewsItemClickListener() {
                @Override
                public void onClick(String articleUrl) {
                    if (startType == SmartLockerManager.EXTRA_VALUE_START_BY_LOCKER) {
                        LockerCustomConfig.getLogger().logEvent("LockScreen_News_Click");
                    } else {
                        LockerCustomConfig.getLogger().logEvent("ChargingScreen_News_Click");
                    }
                    showNewsDetail(articleUrl);
                }
            };
            for (int i = 0; i < baiduFeedBeanList.size(); i++) {
                BaiduFeedBean baiduNewsItemData = baiduFeedBeanList.get(i);
                if (baiduNewsItemData.getNewsType() == TouTiaoFeedUtils.COVER_MODE_THREE_IMAGE) {
                    ThreeImageListItem threeImageListItem = new ThreeImageListItem(baiduNewsItemData);
                    threeImageListItem.setClickListener(clickListener);
                    listItems.add(threeImageListItem);
                    newsCount++;
                    HSLog.d(TAG, "parseItem: newsCount = " + newsCount + ", targetNewsCount = " + targetNewsCount);
                    if (newsCount == targetNewsCount) {
                        listItems.add(getAdItem());
                    }
                } else if (baiduNewsItemData.getNewsType() == TouTiaoFeedUtils.COVER_MODE_RIGHT_IMAGE) {
                    RightImageListItem rightImageListItem = new RightImageListItem(baiduNewsItemData);
                    rightImageListItem.setClickListener(clickListener);
                    listItems.add(rightImageListItem);
                    newsCount++;
                    HSLog.d(TAG, "parseItem: newsCount = " + newsCount + ", targetNewsCount = " + targetNewsCount);
                    if (newsCount == targetNewsCount) {
                        listItems.add(getAdItem());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return listItems;
    }

    @NonNull
    private SmartLockerAdListItem getAdItem() {
        SmartLockerAdListItem adListItem = new SmartLockerAdListItem();
        targetNewsCount = newsCount + adInterval[(++adIntervalIndex) % adInterval.length];
        return adListItem;
    }

    private void showNewsDetail(String newsUrl) {
        if (newsDetailView == null) {
            newsDetailView = new NewsDetailView(context);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            rootLayout.addView(newsDetailView, lp);
        }
        newsDetailView.setVisibility(View.VISIBLE);
        newsDetailView.loadUrl(newsUrl);
    }

    private void refreshAd() {
        if (mActivityMode) {
            if (!LockerCustomConfig.get().getNewsLockerManager().isRefresh()) {
                return;
            }
        } else {
            if (!HSConfig.optBoolean(false, "Application", "LockerAutoRefreshAdsEnable")) {
                return;
            }
        }

        if (onStartTimes <= 2) {
            return;
        }

        if (linearLayoutManager != null && feedAdapter != null) {
            int lastItemPosition = linearLayoutManager.findLastVisibleItemPosition();
            int firstItemPosition = linearLayoutManager.findFirstVisibleItemPosition();

            if (firstItemPosition >= 0 && firstItemPosition < feedAdapter.getItemCount()
                    && lastItemPosition < feedAdapter.getItemCount()) {
                for (int i = firstItemPosition; i <= lastItemPosition; i++) {
                    final INewsListItem feedListItem = feedAdapter.getItem(i);
                    if (feedListItem instanceof SmartLockerAdListItem) {

                        logAdChance();
                        isRefreshAdShow = false;

                        List<AcbNativeAd> adList = AcbNativeAdManager.getInstance().fetch(appPlacement, 1);
                        if (!adList.isEmpty()) {
                            ((SmartLockerAdListItem) feedListItem).setLoadNativeAd(adList.get(0));

                            refreshAdView(adList, feedListItem);

                            logAdShow();
                            logAdUseRatio("True");
                        } else {
                            adLoader = AcbNativeAdManager.getInstance().createLoaderWithPlacement(appPlacement);
                            adLoader.load(1, new AcbNativeAdLoader.AcbNativeAdLoadListener() {
                                @Override
                                public void onAdReceived(AcbNativeAdLoader acbNativeAdLoader, List<AcbNativeAd> list) {
                                    adLoader = null;
                                    if (list == null || list.isEmpty()) {
                                        return;
                                    }

                                    ((SmartLockerAdListItem) feedListItem).setLoadNativeAd(list.get(0));
                                    refreshAdView(list, feedListItem);

                                    logAdShow();
                                    isRefreshAdShow = true;
                                }

                                @Override
                                public void onAdFinished(AcbNativeAdLoader acbNativeAdLoader, AcbError acbError) {
                                    adLoader = null;
                                    logAdUseRatio(isRefreshAdShow ? "True" : "False");
                                }
                            });
                        }

                        AcbNativeAdManager.getInstance().preload(1, appPlacement);
                    }
                }
            }
        }
    }

    private void refreshAdView(List<AcbNativeAd> adList, final INewsListItem feedListItem) {
        nativeAdList.addAll(adList);
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                if (((SmartLockerAdListItem) feedListItem).getCurrentPosition() != -1) {
                    feedAdapter.notifyItemChanged(((SmartLockerAdListItem) feedListItem).getCurrentPosition());
                }
            }
        });
    }

    private void registerScreenOn() {
        final IntentFilter screenFilter = new IntentFilter();
        screenFilter.addAction(Intent.ACTION_SCREEN_ON);
        HSApplication.getContext().registerReceiver(screenOnReceiver, screenFilter);
    }

    public void onBackPressed() {
        if (newsDetailView != null) {
            if (!newsDetailView.onBackClicked()) {
                newsDetailView.closeNewsDetailPage();
            }
        }

        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        }
    }

    protected void onNewIntent(Intent intent) {
        HSLog.d(TAG, "SmartLockerScreen onNewIntent");
        viewedStartTime = System.currentTimeMillis();

        int newType = intent.getIntExtra(SmartLockerManager.EXTRA_START_TYPE, SmartLockerManager.EXTRA_VALUE_START_BY_LOCKER);
        if (newType != startType) {
            if (newType == SmartLockerManager.EXTRA_VALUE_START_BY_LOCKER) {
                LockerCustomConfig.getLogger().logEvent("LockScreen_News_Show");
            } else {
                LockerCustomConfig.getLogger().logEvent("ChargingScreen_News_Show");
            }
        }
    }

    protected void onStart() {
        HSLog.d(TAG, "SmartLockerScreen onStart");

        onStartTimes++;

        if (timeTickReceiver == null) {
            context.registerReceiver(timeTickReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (Intent.ACTION_TIME_TICK.equals(intent.getAction())) {
                        updateTimeAndDateView();
                    }
                }
            }, new IntentFilter(Intent.ACTION_TIME_TICK));
        }
        updateTimeAndDateView();

        updateBatteryState(HSChargingManager.getInstance().getBatteryRemainingPercent());
    }

    protected void onResume() {
        isResume = true;
        if (startType == SmartLockerManager.EXTRA_VALUE_START_BY_CHARGING_SCREEN_OFF) {
            Threads.postOnMainThreadDelayed(displaySuccessChecker, 1000);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (startType == SmartLockerManager.EXTRA_VALUE_START_BY_CHARGING_SCREEN_OFF) {
            Threads.removeOnMainThread(displaySuccessChecker);
        }
        isResume = false;
    }

    @Override
    public void dismiss(Context context, boolean dismissKeyguard) {
        super.dismiss(context, dismissKeyguard);
    }

    @Override
    public void onStop() {
        super.onStop();
        long duration = (System.currentTimeMillis() - viewedStartTime) / 1000;
        if (startType == SmartLockerManager.EXTRA_VALUE_START_BY_LOCKER) {
            LockerCustomConfig.getLogger().logEvent("LockScreen_News_StayTime", "time", getFlurryDuration(duration));
        } else if (startType == SmartLockerManager.EXTRA_VALUE_START_BY_CHARGING_SCREEN_OFF) {
            LockerCustomConfig.getLogger().logEvent("ChargingScreen_News_StayTime", "time", getFlurryDuration(duration));
        }

        if (timeTickReceiver != null) {
            context.unregisterReceiver(timeTickReceiver);
            timeTickReceiver = null;
        }

        if (adLoader != null) {
            adLoader.cancel();
            adLoader = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //重置滑动事件判断条件
        SmartLockerManager.getInstance().setShowAdCount(0);

        HSApplication.getContext().unregisterReceiver(screenOnReceiver);
        HSGlobalNotificationCenter.removeObserver(this);

        if (startType == SmartLockerManager.EXTRA_VALUE_START_BY_LOCKER) {
            LockerCustomConfig.getLogger().logEvent("LockScreen_News_Close");
        } else if (startType == SmartLockerManager.EXTRA_VALUE_START_BY_CHARGING_SCREEN_OFF) {
            LockerCustomConfig.getLogger().logEvent("ChargingScreen_News_Close");
        }

        context.sendBroadcast(new Intent("ACTION_CHARGING_SCREEN_ON_DESTROY")
                .putExtra("EXTRA_CHARGING_SCREEN_ON_DESTROY_NORMAL", isNormalFinishing)
                .setPackage(getPackageName()));

        context.unregisterReceiver(powerStateReceiver);
        context.unregisterReceiver(onHomeClickReceiver);

        HSChargingManager.getInstance().removeChargingListener(chargingListener);

        if (menuPopupWindow != null) {
            menuPopupWindow.dismiss();
        }

        if (adLoader != null) {
            adLoader.cancel();
        }

        for (AcbNativeAd ad : nativeAdList) {
            ad.release();
        }

        if (null != webView) {
            //百度H5新闻流不允许清除cookie
            webView.onDestroy(false);
            webView = null;
        }
    }

    private void processPowerStateChanged(boolean isPowerConnected) {
        HSLog.d(TAG, "processPowerStateChanged: currentPowerConnected = " + currentPowerConnected
                + ", isPowerConnected = " + isPowerConnected);
        if (currentPowerConnected == isPowerConnected) {
            return;
        }
        currentPowerConnected = isPowerConnected;
        try {
            Fade fade = new Fade();
            TransitionManager.beginDelayedTransition(smartLockerContainer, fade);
        } catch (Exception e) {
            e.printStackTrace();
        }
        chargingTopContainer.setVisibility(isPowerConnected ? View.VISIBLE : View.GONE);
        normalTopContainer.setVisibility(isPowerConnected ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onReceive(String s, final HSBundle hsBundle) {
        if (SmartLockerConstants.NOTIFICATION_AD_ITEM_CHANGED.equals(s)) {
            if (recyclerView != null) {
                recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (feedAdapter != null) {
                            int position = -1;
                            if (hsBundle != null) {
                                position = hsBundle.getInt(SmartLockerConstants.NOTIFICATION_AD_ITEM_ID);
                            }
                            if (position != -1 && !recyclerView.isComputingLayout()) {
                                feedAdapter.notifyItemChanged(position);
                            }
                        }
                    }
                });
            }
        } else if (SmartLockerConstants.NOTIFICATION_FEED_PAGE_SLIDE.equals(s)) {
            logViewSlide();
        }
    }

    @Override
    public boolean isActivityHost() {
        return false;
    }


    private String getFlurryDuration(long duration) {
        if (duration < 1L) {
            return "0s-1s";
        } else if (duration < 5L) {
            return "1s-5s";
        } else if (duration < 10L) {
            return "5s-10s";
        } else if (duration < 20L) {
            return "10s-20s";
        } else if (duration < 30L) {
            return "20s-30s";
        } else if (duration < 60L) {
            return "20s-60s";
        } else if (duration < 300L) {
            return "1m-5m";
        } else if (duration < 600L) {
            return "5m-10m";
        } else if (duration < 1200L) {
            return "10m-20m";
        } else if (duration < 1800L) {
            return "20m-30m";
        } else if (duration < 3600L) {
            return "30m-60m";
        } else {
            return "60m+";
        }
    }

    private void logViewSlide() {
        LockerCustomConfig.getLogger().logEvent(startType == SmartLockerManager.EXTRA_VALUE_START_BY_LOCKER
                ? "LockScreen_News_Slide" : "ChargingScreen_News_Slide");
    }

    private void logAdChance() {
        LockerCustomConfig.getLogger().logEvent("SmartLockerFeed2_NativeAd", "type", "Chance");
        LockerCustomConfig.getLogger().logEvent("ad_chance");
        if (mActivityMode) {
            LockerCustomConfig.get().getNewsLockerManager().logAirNewsFeedAdChance();
        } else {
            LockerCustomConfig.get().getNewsLockerManager().logCableFeed1AdChance();
        }
    }

    private void logAdShow() {
        LockerCustomConfig.getLogger().logEvent("SmartLockerFeed2_NativeAd", "type", "AdView");
        LockerCustomConfig.getLogger().logEvent("ad_show");
        if (mActivityMode) {
            LockerCustomConfig.get().getNewsLockerManager().logAirNewsFeedAdShow();
        } else {
            LockerCustomConfig.get().getNewsLockerManager().logCableFeed1AdShow();
        }
    }

    private void logAdUseRatio(String result) {
        LockerCustomConfig.getLogger().logEvent("AcbAdNative_Viewed_In_App", LockerCustomConfig.get().getNewsFeedAdName(), result);
    }
}
