package com.colorphone.smartlocker;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.colorphone.lock.BuildConfig;
import com.colorphone.lock.LockerCustomConfig;
import com.colorphone.lock.PopupView;
import com.colorphone.lock.R;
import com.colorphone.lock.lockscreen.KeyguardHandler;
import com.colorphone.lock.lockscreen.LockScreenStarter;
import com.colorphone.lock.lockscreen.chargingscreen.ChargingScreenSettings;
import com.colorphone.lock.lockscreen.locker.LockerSettings;
import com.colorphone.lock.util.ViewUtils;
import com.colorphone.smartlocker.baidu.BaiduFeedManager;
import com.colorphone.smartlocker.bean.BaiduFeedBean;
import com.colorphone.smartlocker.bean.BaiduFeedItemsBean;
import com.colorphone.smartlocker.itemview.IDailyNewsClickListener;
import com.colorphone.smartlocker.itemview.INewsListItem;
import com.colorphone.smartlocker.itemview.LoadMoreItem;
import com.colorphone.smartlocker.itemview.RightImageListItem;
import com.colorphone.smartlocker.itemview.SmartLockerAdListItem;
import com.colorphone.smartlocker.itemview.ThreeImageListItem;
import com.colorphone.smartlocker.utils.AutoPilotUtils;
import com.colorphone.smartlocker.utils.DisplayUtils;
import com.colorphone.smartlocker.utils.NetworkStatusUtils;
import com.colorphone.smartlocker.utils.NewsUtils;
import com.colorphone.smartlocker.utils.StatusBarUtils;
import com.colorphone.smartlocker.utils.TouTiaoFeedUtils;
import com.colorphone.smartlocker.view.NewsDetailView;
import com.colorphone.smartlocker.view.RefreshView;
import com.colorphone.smartlocker.view.SlidingFinishLayout;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.config.HSConfig;
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

public class SmartLockerFeedsActivity extends HSAppCompatActivity {

    public static final String EXTRA_INT_BATTERY_LEVEL_PERCENT = "EXTRA_INT_BATTERY_LEVEL_PERCENT";

    private static class SafePhoneStateListener extends PhoneStateListener {
        private SmartLockerFeedsActivity refActivity;

        private synchronized void bindActivity(SmartLockerFeedsActivity activity) {
            this.refActivity = activity;
        }

        private synchronized void unbindActivity() {
            refActivity = null;
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            HSLog.d(TAG, "onCallStateChanged(), state " + state + ", incomingNumber = " + incomingNumber);

            switch (state) {
                case TelephonyManager.CALL_STATE_OFFHOOK:
                case TelephonyManager.CALL_STATE_RINGING:
                    synchronized (this) {
                        if (refActivity == null || refActivity.isFinishing()) {
                            return;
                        }
                        HSLog.d(TAG, "onCallStateChanged(), finish activity");
                        refActivity.isNormalFinishing = false;
                        refActivity.dismiss();
                    }
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                default:
                    break;
            }
        }
    }

    private static final String TAG = "SmartLockerFeedsActivity";

    private static final long REFRESH_MIN_DURATION = 1000L;

    private long refreshStartTime;
    private long viewedStartTime;

    private boolean recordSlideFlurry;
    private boolean isLongScreen;
    private boolean isLoading;
    private boolean isFirstLoadData = true;
    private boolean isNormalFinishing = true;
    private boolean currentPowerConnected;
    public static boolean exist = false;

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

    @Nullable
    private PopupWindow menuPopupWindow;

    private PopupView mCloseLockerPopupView;

    private Context context;
    private Handler handler = new Handler();

    protected KeyguardHandler mKeyguardHandler;

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
                    dismiss();
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
            if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                refreshAd();
            }
        }
    };

    private SafePhoneStateListener phoneStateListener = new SafePhoneStateListener();

    private int firstWaitInsertAdPosition = -1;

    private int newsCount = 0;  //已加载的新闻数量
    private int targetNewsCount = 2;    //新闻数量达到多少条时增加广告
    private int emptyAdItemCount = 0;
    private int adIntervalIndex = 0;
    private int[] adInterval = {2, 3};
    private int onStartTimes = 0; //新闻第一次出现时不刷新广告

    @Nullable
    private AcbNativeAdLoader adLoader;
    private List<AcbNativeAd> nativeAdList = new ArrayList<>();

    private Handler loadAdHandler = new Handler();
    private Runnable loadAdRunnable = new Runnable() {
        @Override
        public void run() {

            if (emptyAdItemCount <= 0) {
                loadAdHandler.postDelayed(loadAdRunnable, 500L);
                return;
            }
            if (adLoader != null) {
                return;
            }
            adLoader = AcbNativeAdManager.getInstance().createLoaderWithPlacement(appPlacement);
            adLoader.load(1, new AcbNativeAdLoader.AcbNativeAdLoadListener() {
                @Override
                public void onAdReceived(AcbNativeAdLoader acbNativeAdLoader, List<AcbNativeAd> list) {
                    adLoader = null;
                    if (list == null || list.isEmpty()) {
                        loadAdHandler.postDelayed(loadAdRunnable, 1000L);
                        HSLog.d(TAG, "TryToLoadAd onAdReceived: adList == null || adList.isEmpty()");
                        return;
                    }
                    nativeAdList.addAll(list);
                    tryToInsertAdToItem(list.get(0));
                    loadAdHandler.postDelayed(loadAdRunnable, 500L);
                }

                @Override
                public void onAdFinished(AcbNativeAdLoader acbNativeAdLoader, AcbError acbError) {
                    adLoader = null;
                    loadAdHandler.postDelayed(loadAdRunnable, 1000L);
                    HSLog.d(TAG, "TryToLoadAd onAdFinished: msg = " + (acbError != null
                            ? acbError.getMessage() : "acbError == null"));
                }
            });
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HSLog.d(TAG, "SmartLockerFeedsActivity onCreate");

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        mKeyguardHandler = new KeyguardHandler(this);
        mKeyguardHandler.onInit();

        setContentView(R.layout.activity_smart_locker_feeds);

        registerScreenOn();

        context = this;
        categoryParam = BaiduFeedManager.CATEGORY_ALL;
        isLongScreen = (DisplayUtils.getScreenWithNavigationBarHeight() * 1f / DisplayUtils.getScreenWidth(this)) > 16 / 9f;
        startType = getIntent().getIntExtra(SmartLockerManager.EXTRA_START_TYPE, SmartLockerManager.EXTRA_VALUE_START_BY_LOCKER);
        SmartLockerManager.getInstance().setStartType(startType);

        switch (AutoPilotUtils.getLockerMode()) {
            case "fuse":
                appPlacement = LockerCustomConfig.get().getSmartLockerAdName3();
                break;
            case "cable":
                appPlacement = LockerCustomConfig.get().getSmartLockerAdName4();
                break;
            default:
                appPlacement = LockerCustomConfig.get().getSmartLockerAdName2();
                break;
        }

        AcbNativeAdManager.getInstance().activePlacementInProcess(appPlacement);

        rootLayout = findViewById(R.id.root_layout);
        smartLockerContainer = findViewById(R.id.locker_container);
        smartLockerContainer.setEnableScrollUp(false);
        smartLockerContainer.setSlidingFinishListener(new SlidingFinishLayout.OnSlidingFinishListener() {
            @Override
            public void onSlidingFinish(int slidingState) {
                isNormalFinishing = true;
                dismiss();

                HSLog.d(TAG, "activity finish by onSlidingFinish");
            }
        });

        StatusBarUtils.setTransparent(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            StatusBarUtils.addTranslucentView(this, StatusBarUtils.DEFAULT_STATUS_BAR_ALPHA);
        }
        smartLockerContainer.setPadding(0, StatusBarUtils.getStatusBarHeight(this), 0, 0);

        chargingTipTextView = findViewById(R.id.charging_tip_text_view);
        chargingPercentTextView = findViewById(R.id.charging_percent_text_view);
        chargingDateWeekTextView = findViewById(R.id.charging_date_week_text_view);
        ImageView chargingSettingsImageView = findViewById(R.id.charging_setting_image_view);

        normalDateWeekTextView = findViewById(R.id.normal_date_week_text_view);
        normalTimeTextView = findViewById(R.id.normal_time_text_view);
        ImageView normalSettingsImageView = findViewById(R.id.normal_setting_image_view);

        View.OnClickListener settingsListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenuPopupWindow(context, v);
            }
        };
        chargingSettingsImageView.setOnClickListener(settingsListener);
        normalSettingsImageView.setOnClickListener(settingsListener);

        chargingTopContainer = findViewById(R.id.charging_top_container);
        normalTopContainer = findViewById(R.id.normal_top_container);
        currentPowerConnected = startType != SmartLockerManager.EXTRA_VALUE_START_BY_LOCKER;
        chargingTopContainer.setVisibility(currentPowerConnected ? View.VISIBLE : View.GONE);
        normalTopContainer.setVisibility(currentPowerConnected ? View.GONE : View.VISIBLE);

        refreshView = findViewById(R.id.refresh_view);
        recyclerView = findViewById(R.id.feeds_recycler_view);
        linearLayoutManager = new LinearLayoutManager(this);
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
                logViewedEvent();
            }
        });
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            private float y;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (recordSlideFlurry) {
                    return false;
                }
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        y = event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (!recordSlideFlurry && Math.abs(event.getY() - y) > 24f) {
                            LockerCustomConfig.getLogger().logEvent(startType == SmartLockerManager.EXTRA_VALUE_START_BY_LOCKER
                                    ? "LockScreen_News_Slide" : "ChargingScreen_News_Slide");
                            AutoPilotUtils.logLockerModeAutopilotEvent(startType == SmartLockerManager.EXTRA_VALUE_START_BY_LOCKER
                                    ? "lock_news_slide" : "charging_news_slide");
                            recordSlideFlurry = true;
                        }
                        break;
                }
                return false;
            }
        });
        feedAdapter = new NewsAdapter(this, new ArrayList<INewsListItem<RecyclerView.ViewHolder>>());
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
            }
        });
        resetNewsAdData();
        loadOldData();

        initPhoneStateListener();

        viewedStartTime = System.currentTimeMillis();

        LockerCustomConfig.getLogger().logEvent("news_show");
        AutoPilotUtils.logLockerModeAutopilotEvent("news_show");
        if (startType == SmartLockerManager.EXTRA_VALUE_START_BY_LOCKER) {
            LockerCustomConfig.getLogger().logEvent("LockScreen_News_Show");
            AutoPilotUtils.logLockerModeAutopilotEvent("lock_news_show");
        } else {
            LockerCustomConfig.getLogger().logEvent("ChargingScreen_News_Show");
            AutoPilotUtils.logLockerModeAutopilotEvent("charging_news_show");
        }

        exist = true;

        if (BuildConfig.DEBUG) {
            UnreleasedAdWatcher.getInstance().setEnabled(false);
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        HSLog.d(TAG, "SmartLockerFeedsActivity onNewIntent");
        viewedStartTime = System.currentTimeMillis();

        int newType = getIntent().getIntExtra(SmartLockerManager.EXTRA_START_TYPE, SmartLockerManager.EXTRA_VALUE_START_BY_LOCKER);
        if (newType != startType) {
            if (newType == SmartLockerManager.EXTRA_VALUE_START_BY_LOCKER) {
                LockerCustomConfig.getLogger().logEvent("LockScreen_News_Show");
                AutoPilotUtils.logLockerModeAutopilotEvent("lock_news_show");
            } else {
                LockerCustomConfig.getLogger().logEvent("ChargingScreen_News_Show");
                AutoPilotUtils.logLockerModeAutopilotEvent("charging_news_show");
            }
        }
    }

    private void resetNewsAdData() {
        newsCount = 0;
        emptyAdItemCount = 0;
        adIntervalIndex = 0;
        targetNewsCount = isLongScreen ? 2 : 1;
    }

    @Override
    protected void onStart() {
        super.onStart();
        HSLog.d(TAG, "SmartLockerFeedsActivity onStart");

        onStartTimes++;

        if (timeTickReceiver == null) {
            registerReceiver(timeTickReceiver = new BroadcastReceiver() {
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

        loadAdHandler.postDelayed(loadAdRunnable, 500L);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (startType == SmartLockerManager.EXTRA_VALUE_START_BY_CHARGING_SCREEN_OFF) {
            Threads.postOnMainThreadDelayed(displaySuccessChecker, 1000);
        }
    }

    @Override
    protected void onPause() {
        if (startType == SmartLockerManager.EXTRA_VALUE_START_BY_CHARGING_SCREEN_OFF) {
            Threads.removeOnMainThread(displaySuccessChecker);
        }
        super.onPause();
    }

    private void initPhoneStateListener() {
        IntentFilter powerStateIntentFilter = new IntentFilter();
        powerStateIntentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        powerStateIntentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(powerStateReceiver, powerStateIntentFilter);

        registerReceiver(onHomeClickReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

        try {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
                phoneStateListener.bindActivity(this);
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        HSChargingManager.getInstance().addChargingListener(chargingListener);
        HSChargingManager.getInstance().start();
    }

    private void showMenuPopupWindow(Context context, View parentView) {
        if (menuPopupWindow == null) {
            View view = LayoutInflater.from(context).inflate(R.layout.charging_screen_popup_menu_view, null);
            view.setBackground(ContextCompat.getDrawable(this, R.drawable.charging_screen_feeds_popup_window_bg));
            View feedbackView = view.findViewById(R.id.smart_locker_feedback);
            feedbackView.setVisibility(View.GONE);

            View smartLockerCloseView = view.findViewById(R.id.txt_close_charging_boost);
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

            menuPopupWindow = new PopupWindow(view);
            menuPopupWindow.setWidth(WRAP_CONTENT);
            menuPopupWindow.setHeight(WRAP_CONTENT);
            menuPopupWindow.setFocusable(true);
            menuPopupWindow.setOutsideTouchable(true);
            menuPopupWindow.setBackgroundDrawable(new BitmapDrawable());
            menuPopupWindow.update();
        }

        if (menuPopupWindow.isShowing()) {
            return;
        }
        menuPopupWindow.showAsDropDown(parentView, -getResources().getDimensionPixelSize(R.dimen.charging_feeds_popmenu_margin_right),
                -(getResources().getDimensionPixelOffset(R.dimen.charging_screen_menu_to_top_height) + parentView.getHeight()) >> 1);
    }

    private void showCloseDialog() {
        if (mCloseLockerPopupView == null) {
            mCloseLockerPopupView = new PopupView(this, rootLayout);
            View content = LayoutInflater.from(this).inflate(R.layout.locker_popup_dialog, null);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams((int) (Dimensions
                    .getPhoneWidth(this) * 0.872f), WRAP_CONTENT);
            content.setLayoutParams(layoutParams);
            TextView title = ViewUtils.findViewById(content, R.id.title);
            TextView hintContent = ViewUtils.findViewById(content, R.id.hint_content);
            AppCompatButton buttonYes = ViewUtils.findViewById(content, R.id.button_yes);
            AppCompatButton buttonNo = ViewUtils.findViewById(content, R.id.button_no);
            buttonNo.setTextColor(getResources().getColor(R.color.primary_green));
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
                        AutoPilotUtils.logLockerModeAutopilotEvent("lock_disabled");
                    } else {
                        ChargingScreenSettings.setChargingScreenEnabled(false);
                        LockerCustomConfig.getLogger().logEvent("ChargingScreen_Disabled");
                        AutoPilotUtils.logLockerModeAutopilotEvent("charging_disabled");
                    }
                    HSLog.d(TAG, "activity finish by turn off");
                    isNormalFinishing = true;
                    mCloseLockerPopupView.dismiss();
                    dismiss();
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
        String strTimeFormat = Settings.System.getString(getContentResolver(), Settings.System.TIME_12_24);
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
        chargingTipTextView.setText(getString(R.string.charging_screen_charged_left_describe2, chargingLeftText));
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

    private void loadOldData() {
        JSONObject jsonObject = NewsUtils.getLastNews(categoryParam);
        if (jsonObject == null || !NetworkStatusUtils.isNetworkConnected(context)) {
            return;
        }
        feedAdapter.addItems(feedAdapter.getItemCount(), parseBaiduNewsJson(jsonObject, categoryParam));
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
                ? BaiduFeedManager.LOAD_FIRST : BaiduFeedManager.LOAD_MORE, new BaiduFeedManager.DataBackListener() {
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
                            AutoPilotUtils.logLockerModeAutopilotEvent("lock_news_loadmore");
                        } else if (startType == SmartLockerManager.EXTRA_VALUE_START_BY_CHARGING_SCREEN_OFF) {
                            LockerCustomConfig.getLogger().logEvent("ChargingScreen_News_Loadmore", "result", "no");
                            AutoPilotUtils.logLockerModeAutopilotEvent("charging_news_loadmore");
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
                        AutoPilotUtils.logLockerModeAutopilotEvent("lock_news_loadmore");
                    } else if (startType == SmartLockerManager.EXTRA_VALUE_START_BY_CHARGING_SCREEN_OFF) {
                        LockerCustomConfig.getLogger().logEvent("ChargingScreen_News_Loadmore", "result", "yes");
                        AutoPilotUtils.logLockerModeAutopilotEvent("charging_news_loadmore");
                    }
                }

                parseData(isPullDown, response);
                isLoading = false;
            }
        });
    }

    private void parseData(boolean isPullDown, JSONObject response) {
        final List<INewsListItem<? extends RecyclerView.ViewHolder>> dailyNewsListItems = parseBaiduNewsJson(response, categoryParam);

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
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    logViewedEvent();
                }
            }, 500L);
        }

        feedAdapter.addItems(feedAdapter.getItemCount() - 1, dailyNewsListItems);
        isFirstLoadData = false;
    }

    private List<INewsListItem<? extends RecyclerView.ViewHolder>> parseBaiduNewsJson(JSONObject response, String category) {
        List<INewsListItem<? extends RecyclerView.ViewHolder>> listItems = new ArrayList<>();
        try {
            BaiduFeedItemsBean baiduFeedItemsBean = new BaiduFeedItemsBean(response);
            List<BaiduFeedBean> baiduFeedBeanList = baiduFeedItemsBean.getBaiduFeedBeans();
            if (baiduFeedBeanList == null || baiduFeedBeanList.isEmpty()) {
                return listItems;
            }

            IDailyNewsClickListener clickListener = new IDailyNewsClickListener() {
                @Override
                public void onClick(String articleUrl) {
                    if (startType == SmartLockerManager.EXTRA_VALUE_START_BY_LOCKER) {
                        LockerCustomConfig.getLogger().logEvent("LockScreen_News_Click");
                        AutoPilotUtils.logLockerModeAutopilotEvent("lock_news_click");
                    } else {
                        LockerCustomConfig.getLogger().logEvent("ChargingScreen_News_Click");
                        AutoPilotUtils.logLockerModeAutopilotEvent("charging_news_click");
                    }
                    showNewsDetail(articleUrl);
                }
            };
            for (int i = 0; i < baiduFeedBeanList.size(); i++) {
                BaiduFeedBean baiduNewsItemData = baiduFeedBeanList.get(i);
                if (baiduNewsItemData.getNewsType() == TouTiaoFeedUtils.COVER_MODE_THREE_IMAGE) {
                    ThreeImageListItem threeImageListItem = new ThreeImageListItem(category, baiduNewsItemData);
                    threeImageListItem.setClickListener(clickListener);
                    listItems.add(threeImageListItem);
                    newsCount++;
                    HSLog.d(TAG, "parseItem: newsCount = " + newsCount + ", targetNewsCount = " + targetNewsCount);
                    if (newsCount == targetNewsCount) {
                        listItems.add(getAdItem());
                    }
                } else if (baiduNewsItemData.getNewsType() == TouTiaoFeedUtils.COVER_MODE_RIGHT_IMAGE) {
                    RightImageListItem rightImageListItem = new RightImageListItem(category, baiduNewsItemData);
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
        SmartLockerAdListItem adListItem;
        List<AcbNativeAd> adList = AcbNativeAdManager.getInstance().fetch(appPlacement, 1);
        if (!adList.isEmpty()) {
            adListItem = new SmartLockerAdListItem(appPlacement, adList.get(0));
        } else {
            if (newsCount == (isLongScreen ? 2 : 1)) {
                firstWaitInsertAdPosition = (isLongScreen ? 2 : 1);
            }
            emptyAdItemCount++;
            adListItem = new SmartLockerAdListItem(appPlacement, null);
        }

        targetNewsCount = newsCount + adInterval[(++adIntervalIndex) % adInterval.length];

        return adListItem;
    }

    private void tryToInsertAdToItem(AcbNativeAd nativeAd) {
        HSLog.d(TAG, "TryToLoadAd tryToInsertAdToItem: ");
        if (linearLayoutManager == null || feedAdapter == null) {
            return;
        }
        int firstPosition = linearLayoutManager.findFirstVisibleItemPosition();
        int lastPosition = linearLayoutManager.findLastVisibleItemPosition();
        if (firstWaitInsertAdPosition >= 0
                && firstPosition <= firstWaitInsertAdPosition
                && firstWaitInsertAdPosition <= lastPosition) {
            // 第一个广告位可见且没有广告则插入广告
            if (feedAdapter.getData().get(firstWaitInsertAdPosition) instanceof SmartLockerAdListItem) {
                SmartLockerAdListItem adListItem = (SmartLockerAdListItem) feedAdapter.getData().get(firstWaitInsertAdPosition);
                if (adListItem.isNativeAdNull()) {
                    emptyAdItemCount--;
                    adListItem.setLoadNativeAd(nativeAd);
                    recyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            feedAdapter.notifyItemChanged(firstWaitInsertAdPosition);
                            firstWaitInsertAdPosition = -1;
                        }
                    });
                    HSLog.d(TAG, "tryToInsertAdToItem: notifyItemChanged firstWaitInsertAdPosition = " + firstWaitInsertAdPosition);
                    return;
                }
            }
        }
        for (int pos = lastPosition + 1; pos < feedAdapter.getItemCount(); pos++) {
            if (pos >= 0 && feedAdapter.getData().get(pos) instanceof SmartLockerAdListItem) {
                SmartLockerAdListItem adListItem = (SmartLockerAdListItem) feedAdapter.getData().get(pos);
                if (adListItem.isNativeAdNull()) {
                    emptyAdItemCount--;
                    adListItem.setLoadNativeAd(nativeAd);
                    final int finalPos = pos;
                    recyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            feedAdapter.notifyItemChanged(finalPos);
                        }
                    });
                    HSLog.d(TAG, "tryToInsertAdToItem: notifyItemChanged pos = " + pos);
                    break;
                }
            }
        }
    }

    private void showNewsDetail(String newsUrl) {
        if (newsDetailView == null) {
            newsDetailView = new NewsDetailView(this);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            rootLayout.addView(newsDetailView, lp);
        }
        newsDetailView.setVisibility(View.VISIBLE);
        newsDetailView.loadUrl(newsUrl);
    }

    private void logViewedEvent() {
        if (linearLayoutManager != null && feedAdapter != null) {
            int lastItemPosition = linearLayoutManager.findLastVisibleItemPosition();
            int firstItemPosition = linearLayoutManager.findFirstVisibleItemPosition();

            if (firstItemPosition >= 0 && firstItemPosition < feedAdapter.getItemCount()
                    && lastItemPosition < feedAdapter.getItemCount()) {
                for (int i = firstItemPosition; i <= lastItemPosition; i++) {
                    INewsListItem feedListItem = feedAdapter.getItem(i);
                    feedListItem.logViewedEvent();
                }
            }
        }
    }

    private void refreshAd() {
        if (!HSConfig.optBoolean(false, "Application", "LockerAutoRefreshAdsEnable")) {
            return;
        }

        if (onStartTimes < 2) {
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

                        List<AcbNativeAd> adList = AcbNativeAdManager.getInstance().fetch(appPlacement, 1);
                        if (!adList.isEmpty()) {
                            ((SmartLockerAdListItem) feedListItem).setLoadNativeAd(adList.get(0));
                            recyclerView.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (((SmartLockerAdListItem) feedListItem).getCurrentPosition() != -1) {
                                        feedAdapter.notifyItemChanged(((SmartLockerAdListItem) feedListItem).getCurrentPosition());
                                    }
                                }
                            });

                            logAdShow();
                        }
                        AcbNativeAdManager.getInstance().preload(1, appPlacement);
                    }
                }
            }
        }
    }

    private void registerScreenOn() {
        final IntentFilter screenFilter = new IntentFilter();
        screenFilter.addAction(Intent.ACTION_SCREEN_ON);
        HSApplication.getContext().registerReceiver(screenOnReceiver, screenFilter);
    }

    @Override
    public void onBackPressed() {
        if (newsDetailView != null) {
            if (!newsDetailView.onBackClicked()) {
                newsDetailView.closeNewsDetailPage();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        recordSlideFlurry = false;
        long duration = (System.currentTimeMillis() - viewedStartTime) / 1000;
        if (startType == SmartLockerManager.EXTRA_VALUE_START_BY_LOCKER) {
            LockerCustomConfig.getLogger().logEvent("LockScreen_News_StayTime", "time", getFlurryDuration(duration));
        } else if (startType == SmartLockerManager.EXTRA_VALUE_START_BY_CHARGING_SCREEN_OFF) {
            LockerCustomConfig.getLogger().logEvent("ChargingScreen_News_StayTime", "time", getFlurryDuration(duration));
        }

        if (timeTickReceiver != null) {
            unregisterReceiver(timeTickReceiver);
            timeTickReceiver = null;
        }

        if (adLoader != null) {
            adLoader.cancel();
            adLoader = null;
        }
        loadAdHandler.removeCallbacks(loadAdRunnable);
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

    private void dismiss() {
        mKeyguardHandler.tryDismissKeyguard(true, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        HSApplication.getContext().unregisterReceiver(screenOnReceiver);

        mKeyguardHandler.onViewDestroy();

        if (startType == SmartLockerManager.EXTRA_VALUE_START_BY_LOCKER) {
            LockerCustomConfig.getLogger().logEvent("LockScreen_News_Close");
            AutoPilotUtils.logLockerModeAutopilotEvent("lock_news_close");
        } else if (startType == SmartLockerManager.EXTRA_VALUE_START_BY_CHARGING_SCREEN_OFF) {
            LockerCustomConfig.getLogger().logEvent("ChargingScreen_News_Close");
            AutoPilotUtils.logLockerModeAutopilotEvent("charging_news_close");
        }

        sendBroadcast(new Intent("ACTION_CHARGING_SCREEN_ON_DESTROY")
                .putExtra("EXTRA_CHARGING_SCREEN_ON_DESTROY_NORMAL", isNormalFinishing)
                .setPackage(getPackageName()));

        unregisterReceiver(powerStateReceiver);
        unregisterReceiver(onHomeClickReceiver);

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
            phoneStateListener.unbindActivity();
        }
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
        exist = false;

    }

    private void logAdChance() {
        switch (AutoPilotUtils.getLockerMode()) {
            case "cableandfuse":
                LockerCustomConfig.getLogger().logEvent("SmartLockerFeed2_NativeAd", "type", "Chance");
                break;
            case "fuse":
                LockerCustomConfig.getLogger().logEvent("SmartLockerFeed3_NativeAd", "type", "Chance");
                break;
            case "cable":
                LockerCustomConfig.getLogger().logEvent("SmartLockerFeed4_NativeAd", "type", "Chance");
                break;
        }
        LockerCustomConfig.getLogger().logEvent("ad_chance");
        AutoPilotUtils.logLockerModeAutopilotEvent("ad_chance");
    }

    private void logAdShow() {
        switch (AutoPilotUtils.getLockerMode()) {
            case "cableandfuse":
                LockerCustomConfig.getLogger().logEvent("SmartLockerFeed2_NativeAd", "type", "AdView");
                break;
            case "fuse":
                LockerCustomConfig.getLogger().logEvent("SmartLockerFeed3_NativeAd", "type", "AdView");
                break;
            case "cable":
                LockerCustomConfig.getLogger().logEvent("SmartLockerFeed4_NativeAd", "type", "AdView");
                break;
        }
        LockerCustomConfig.getLogger().logEvent("ad_show");
        AutoPilotUtils.logLockerModeAutopilotEvent("ad_show");
    }
}
