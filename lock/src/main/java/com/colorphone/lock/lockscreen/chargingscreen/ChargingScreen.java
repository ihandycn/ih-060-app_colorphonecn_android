package com.colorphone.lock.lockscreen.chargingscreen;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.colorphone.lock.BuildConfig;
import com.colorphone.lock.LauncherPhoneStateListener;
import com.colorphone.lock.LockerCustomConfig;
import com.colorphone.lock.PopupView;
import com.colorphone.lock.R;
import com.colorphone.lock.RipplePopupView;
import com.colorphone.lock.ScreenStatusReceiver;
import com.colorphone.lock.lockscreen.FloatWindowCompat;
import com.colorphone.lock.lockscreen.KeyguardHandler;
import com.colorphone.lock.lockscreen.LockNotificationManager;
import com.colorphone.lock.lockscreen.LockScreen;
import com.colorphone.lock.lockscreen.LockScreenStarter;
import com.colorphone.lock.lockscreen.LockScreensLifeCycleRegistry;
import com.colorphone.lock.lockscreen.chargingscreen.tipview.ToolTipRelativeLayout;
import com.colorphone.lock.lockscreen.chargingscreen.tipview.ToolTipView;
import com.colorphone.lock.lockscreen.chargingscreen.view.ChargingBubbleView;
import com.colorphone.lock.lockscreen.chargingscreen.view.ChargingQuantityView;
import com.colorphone.lock.lockscreen.chargingscreen.view.SlidingFinishRelativeLayout;
import com.colorphone.lock.lockscreen.locker.NotificationWindowHolder;
import com.colorphone.lock.util.ViewUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.libcharging.HSChargingManager;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Bitmaps;
import com.superapps.util.Commons;
import com.superapps.util.Dimensions;
import com.superapps.util.HomeKeyWatcher;
import com.superapps.util.Threads;

import net.appcloudbox.ads.expressad.AcbExpressAdView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.colorphone.lock.ScreenStatusReceiver.NOTIFICATION_SCREEN_ON;
import static com.colorphone.lock.lockscreen.locker.Locker.getDeviceInfo;
import static com.ihs.libcharging.HSChargingManager.HSChargingState.STATE_DISCHARGING;


public class ChargingScreen extends LockScreen implements INotificationObserver, NotificationWindowHolder.NotificationClickCallback {

    private static final String TAG = "CHARGING_SCREEN_ACTIVITY";

    @SuppressWarnings("PointlessBooleanExpression")
    public static final boolean LOG_VERBOSE = true && BuildConfig.DEBUG;

    private static final long DURATION_CHARGING_STATE_TIP_ICON_ANIMATOR = 2080;

    private static final float CHARGING_STATE_TIP_ICON_POSITIVE_ALPHA = 0.8f;
    private static final float CHARGING_STATE_TIP_ICON_NEGATIVE_ALPHA = 0.2f;
    private static final float CHARGING_STATE_TIP_ICON_ANIMATOR_MAX_ALPHA = 1.0f;
    private static final float CHARGING_STATE_TIP_ICON_ANIMATOR_MIN_ALPHA = 0.1f;

    public static final String EVENT_CHARGING_FINISH_SELF = "event_charging_finish_self";

    public static final String EXTRA_BOOLEAN_IS_CHARGING = "EXTRA_BOOLEAN_IS_CHARGING";
    public static final String EXTRA_BOOLEAN_IS_CHARGING_FULL = "EXTRA_BOOLEAN_IS_CHARGING_FULL";
    public static final String EXTRA_INT_BATTERY_LEVEL_PERCENT = "EXTRA_INT_BATTERY_LEVEL_PERCENT";
    public static final String EXTRA_INT_CHARGING_LEFT_MINUTES = "EXTRA_INT_CHARGING_LEFT_MINUTES";
    public static final String EXTRA_BOOLEAN_IS_CHARGING_STATE_CHANGED = "EXTRA_BOOLEAN_IS_CHARGING_STATE_CHANGED";

    public static final String EXTRA_SKIN_TYPE = "EXTRA_SKIN_TYPE";
    public static final int SKIN_TYPE_BLUE_BACKGROUND = 0;
    public static final int SKIN_TYPE_BLACK_BACKGROUND = 1;
    public static final int SKIN_TYPE_WALLPAPER_BACKGROUND = 2;

    private RipplePopupView menuPopupView;
    private ImageView menuImageView;
    private PopupView mCloseLockerPopupView;

    private TextView timeTextView;
    private TextView dateTextView;

    private TextView fullChargeLeftDescribeTextView;

    private ImageView speedChargeStateImageView;
    private ImageView continuousChargeStateImageView;
    private ImageView trickleChargeStateImageView;

    private TextView tipTextView;

    private ToolTipView speedChargeToolTipView;
    private ToolTipView continuousChargeToolTipView;
    private ToolTipView trickleChargeToolTipView;

    private NotificationWindowHolder mNotificationWindowHolder;

    private SlidingFinishRelativeLayout slidingFinishRelativeLayout;
    private RelativeLayout advertisementContainer;
    private ChargingQuantityView chargingQuantityView;
    private ChargingBubbleView chargingBubbleView;
    private ImageView imageBackgroundView;

    private ObjectAnimator chargingStateAlphaAnimator;

    private boolean isChargingOnInit;

    private Handler handler = new Handler();
    private AcbExpressAdView expressAdView;
    private boolean mAdShown;
    private long onStartTime;

    private boolean isPowerConnected;
    private boolean isStart;
    private boolean mIsSetup = false;

    private String mDismissReason = "Unkown";

    private BroadcastReceiver timeTickReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || TextUtils.isEmpty(intent.getAction())) {
                return;
            }

            if (Intent.ACTION_TIME_TICK.equals(intent.getAction())) {
                updateTimeAndDateView();
            }
        }
    };

    private HSChargingManager.IChargingListener chargingListener = new HSChargingManager.IChargingListener() {
        @Override
        public void onBatteryLevelChanged(int preBatteryLevel, int curBatteryLevel) {
            HSLog.d(TAG, "onBatteryLevelChanged() preBatteryLevel=" + preBatteryLevel
                    + " curBatteryLevel=" + curBatteryLevel);

            chargingQuantityView.setTextValue(curBatteryLevel);

            updateChargingStateTipIconAnimator();
        }

        @Override
        public void onChargingStateChanged(HSChargingManager.HSChargingState preChargingState, HSChargingManager.HSChargingState curChargingState) {
            HSLog.d(TAG, "onChargingStateChanged()");

            if (HSChargingManager.getInstance().isCharging()) {
                if (preChargingState == STATE_DISCHARGING) {
                    processPowerStateChanged(true);

                    updateChargingStateTipIconAnimator();
                }
            } else {
                if (preChargingState != STATE_DISCHARGING) {
                    processPowerStateChanged(false);
                }
            }
        }

        @Override
        public void onChargingRemainingTimeChanged(int chargingRemainingMinutes) {
            HSLog.d(TAG, "onChargingRemainingTimeChanged() chargingRemainingMinutes"
                    + chargingRemainingMinutes);

            Context context = getContext();
            if (HSChargingManager.getInstance().getChargingState() == HSChargingManager.HSChargingState.STATE_CHARGING_FULL) {
                fullChargeLeftDescribeTextView.setText(context.getString(R.string.charging_screen_charged_full));
            } else if (chargingRemainingMinutes > 0) {
                fullChargeLeftDescribeTextView.setText(context.getString(R.string.charging_screen_charged_left_describe,
                        getChargingLeftTimeString(HSChargingManager.getInstance().getChargingLeftMinutes())));
            }
        }

        @Override
        public void onBatteryTemperatureChanged(float v, float v1) {
        }
    };
    private HomeKeyWatcher mHomeKeyWatcher;
    private TextView unlockTextView;

    private void processPowerStateChanged(boolean isPowerConnected) {
        if (this.isPowerConnected == isPowerConnected) {
            return;
        }
        this.isPowerConnected = isPowerConnected;

        if (isPowerConnected) {
            chargingBubbleView.setPopupBubbleFlag(true);
            updateChargingStateTipIconAnimator();

        } else {
            chargingBubbleView.setPopupBubbleFlag(false);

            final int chargeRemainPercent = ChargingScreenUtils.getBatteryPercentage(HSApplication.getContext());
            speedChargeStateImageView.setAlpha(1.0f);
            if (chargeRemainPercent > 80) {
                continuousChargeStateImageView.setAlpha(1.0f);
            }
            if (chargeRemainPercent >= 100) {
                trickleChargeStateImageView.setAlpha(1.0f);
            }
        }
    }

    @Override
    public void setup(ViewGroup root, Bundle extra) {
        super.setup(root, extra);

        if (root.getContext() instanceof Activity) {
            root.findViewById(R.id.charging_screen_container).setPadding(0, 0, 0, Dimensions.getNavigationBarHeight(root.getContext()));
        } else if (!FloatWindowCompat.needsSystemErrorFloatWindow()) {
            root.findViewById(R.id.charging_screen_container).setPadding(0, 0, 0, Dimensions.getNavigationBarHeight(HSApplication.getContext()));
        }

        // ======== onCreate ========
        HSLog.d(TAG, "onCreate()");

        if (!isActivityHost()) {
            LockScreenStarter.getInstance().onScreenDisplayed();
        }

        final Context context = root.getContext();

        if (mHomeKeyWatcher == null) {
            mHomeKeyWatcher = new HomeKeyWatcher(root.getContext());
            mHomeKeyWatcher.setOnHomePressedListener(new HomeKeyWatcher.OnHomePressedListener() {
                @Override
                public void onHomePressed() {
                    mDismissReason = "Home";
                    dismiss(getContext(), false);
                }

                @Override
                public void onRecentsPressed() {
                }
            });
        }
        mHomeKeyWatcher.startWatch();

        initView(extra);

        menuImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenuPopupWindow(context, menuImageView);

                LockerCustomConfig.getLogger().logEvent("ChargingScreen_Setting_Clicked");
            }
        });

        slidingFinishRelativeLayout.setSlidingFinishListener(
                new SlidingFinishRelativeLayout.OnSlidingFinishListener() {
                    @Override
                    public void onSlidingFinish(@SlidingFinishRelativeLayout.SlidingState int slidingState) {
                        mDismissReason = "Slide";
                        dismiss(getContext(), true);
                    }
                });

        updateTimeAndDateView();

        if (!mIsSetup) {
            context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        }

        if (extra == null) {
            isChargingOnInit = false;
            chargingQuantityView.setTextValue(100);
            fullChargeLeftDescribeTextView.setText(context.getString(R.string.charging_screen_charged_left_describe, ""));
        } else {
            isChargingOnInit = extra.getBoolean(EXTRA_BOOLEAN_IS_CHARGING, false);
            chargingQuantityView.setTextValue(extra.getInt(EXTRA_INT_BATTERY_LEVEL_PERCENT, 100));
            fullChargeLeftDescribeTextView.setText(extra.getBoolean(EXTRA_BOOLEAN_IS_CHARGING_FULL)
                    ? context.getString(R.string.charging_screen_charged_full) :
                    context.getString(R.string.charging_screen_charged_left_describe,
                            getChargingLeftTimeString(extra.getInt(EXTRA_INT_CHARGING_LEFT_MINUTES))));
        }

        updateChargingStateTipIconAnimator();
        processPowerStateChanged(true);

        if (!isActivityHost()) {
            HSGlobalNotificationCenter.addObserver(ScreenStatusReceiver.NOTIFICATION_SCREEN_ON, this);
            HSGlobalNotificationCenter.addObserver(ScreenStatusReceiver.NOTIFICATION_SCREEN_OFF, this);
        }
        HSGlobalNotificationCenter.addObserver(LauncherPhoneStateListener.NOTIFICATION_CALL_RINGING, this);
        HSGlobalNotificationCenter.addObserver(KeyguardHandler.EVENT_KEYGUARD_UNLOCKED, this);
        HSGlobalNotificationCenter.addObserver(KeyguardHandler.EVENT_KEYGUARD_LOCKED, this);
        HSGlobalNotificationCenter.addObserver(EVENT_CHARGING_FINISH_SELF, this);

        HSGlobalNotificationCenter.addObserver(NOTIFICATION_SCREEN_ON, mNotificationWindowHolder);
        LockNotificationManager.getInstance().registerForThemeStateChange(mNotificationWindowHolder);
        LockNotificationManager.getInstance().registerForChargingScreenChange(observer);

        // Life cycle
        LockScreensLifeCycleRegistry.setChargingScreenActive(true);
        LockerCustomConfig.get().onEventChargingViewShow();
        String suffix = ChargingScreenUtils.isFromPush ? "_Push" : "";
        LockerCustomConfig.getLogger().logEvent("ChargingScreen_Shown_Init" + suffix,
                "Brand", Build.BRAND.toLowerCase(), "DeviceVersion", getDeviceInfo());

        mIsSetup = true;

        if (!isActivityHost()) {
            onStart();
        }
    }


    public void onStart() {
        PowerManager powerManager = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = powerManager != null && powerManager.isScreenOn();
        if (!isScreenOn) {
            return;
        }

        if (isStart) {
            return;
        }

        // ======== onStart ========
        isStart = true;
        HSLog.d(TAG, "onStart()");

        onStartTime = System.currentTimeMillis();

        HSChargingManager.getInstance().addChargingListener(chargingListener);
        getContext().registerReceiver(timeTickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));

        updateTipTextRandomValue();
        updateTimeAndDateView();

        if (isChargingOnInit || HSChargingManager.getInstance().isCharging()) {
            isChargingOnInit = false;
            updateChargingStateTipIconAnimator();
        }


        // ======== onResume ========

        if (chargingBubbleView != null) {
            chargingBubbleView.resumeAnim();
        }

        if (speedChargeToolTipView != null) {
            speedChargeToolTipView.remove();
            speedChargeToolTipView = null;
        }

        if (continuousChargeToolTipView != null) {
            continuousChargeToolTipView.remove();
            continuousChargeToolTipView = null;
        }

        if (trickleChargeToolTipView != null) {
            trickleChargeToolTipView.remove();
            trickleChargeToolTipView = null;
        }

        // ======== onAttachedToWindow ========
        if (!isActivityHost()) {
            onAttachedToWindow();
        }

        showExpressAd();
    }

    public void onAttachedToWindow() {
        LockerCustomConfig.getLogger().logEvent("ChargingScreen_Show");
        requestAds();
    }

    private void requestAds() {
        if (!HSConfig.optBoolean(false, "Application", "LockerAutoRefreshAdsEnable")) {
            LockerCustomConfig.getLogger().logEvent("SmartLockerFeed1_NativeAd", "type", "Chance");
            LockerCustomConfig.getLogger().logEvent("ad_chance");
        }

        expressAdView = new AcbExpressAdView(getContext(), LockerCustomConfig.get().getLockerAndChargingAdName(), "");
        expressAdView.setExpressAdViewListener(new AcbExpressAdView.AcbExpressAdViewListener() {
            @Override
            public void onAdShown(AcbExpressAdView acbExpressAdView) {
                LockerCustomConfig.getLogger().logEvent("SmartLockerFeed1_NativeAd", "type", "AdView");
                LockerCustomConfig.getLogger().logEvent("ad_show");
                mAdShown = true;
                LockerCustomConfig.get().onEventChargingAdShow();
                advertisementContainer.setBackground(BackgroundDrawables.createBackgroundDrawable(Color.WHITE, Dimensions.pxFromDp(8), false));
                advertisementContainer.setPadding(Dimensions.pxFromDp(10), Dimensions.pxFromDp(10), Dimensions.pxFromDp(10), Dimensions.pxFromDp(0));
            }

            @Override
            public void onAdClicked(AcbExpressAdView acbExpressAdView) {
                LockerCustomConfig.getLogger().logEvent("SmartLockerFeed1_NativeAd", "type", "AdClick");
                mDismissReason = "AdClick";
                dismiss(getContext(), true);
                LockerCustomConfig.get().onEventChargingAdClick();
            }

        });

        expressAdView.prepareAdPlus(new AcbExpressAdView.PrepareAdPlusListener() {
            @Override
            public void onAdReady(AcbExpressAdView acbExpressAdView, float v) {

            }
        });

        expressAdView.setAutoSwitchAd(AcbExpressAdView.AutoSwitchAd_All);
    }

    private void showExpressAd() {
        if (expressAdView != null && expressAdView.getParent() == null) {
            advertisementContainer.removeAllViews();
            advertisementContainer.addView(expressAdView, new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));
            expressAdView.switchAd();
        }

        if (expressAdView != null && HSConfig.optBoolean(false, "Application", "LockerAutoRefreshAdsEnable")) {
            LockerCustomConfig.getLogger().logEvent("SmartLockerFeed1_NativeAd", "type", "Chance");
            LockerCustomConfig.getLogger().logEvent("ad_chance");

            expressAdView.switchAd();
        }
    }

    /**
     * handle back key
     */
    public void onBackPressed() {
        mDismissReason = "Back";
        dismiss(getContext(), true);
    }

    private void cancelChargingStateAlphaAnimation() {
        if (chargingStateAlphaAnimator != null) {
            chargingStateAlphaAnimator.cancel();
        }
    }

    private void initView(Bundle extra) {
        final Context context = getContext();
        ImageView appIcon = mRootView.findViewById(R.id.app_custom_icon);
        appIcon.setImageResource(LockerCustomConfig.get().getCustomScreenIcon());
        int chargingQuantityUpColor;
        int chargingQuantityBottomColor;
        int chargingBubbleColor;

        imageBackgroundView = mRootView.findViewById(R.id.charging_screen_bg);
        final int skinType = extra == null ? -1 : extra.getInt(EXTRA_SKIN_TYPE, -1);
        switch (skinType) {

            default:
            case SKIN_TYPE_BLUE_BACKGROUND: {

                chargingQuantityUpColor = ContextCompat.getColor(getContext(), R.color.charging_screen_blue_quantity_up);
                chargingQuantityBottomColor = ContextCompat.getColor(context, R.color.charging_screen_blue_quantity_bottom);
                chargingBubbleColor = ContextCompat.getColor(context, R.color.charging_screen_blue_bubble);

                imageBackgroundView.setBackgroundColor(ContextCompat.getColor(context, R.color.charging_screen_blue_background));

                break;
            }

            case SKIN_TYPE_BLACK_BACKGROUND: {

                chargingQuantityUpColor = ContextCompat.getColor(context, R.color.charging_screen_black_quantity_up);
                chargingQuantityBottomColor = ContextCompat.getColor(context, R.color.charging_screen_black_quantity_bottom);
                chargingBubbleColor = ContextCompat.getColor(context, R.color.charging_screen_black_bubble);

                imageBackgroundView.setBackgroundColor(ContextCompat.getColor(context, R.color.charging_screen_black_background));

                View blackBackgroundView = mRootView.findViewById(R.id.charging_screen_bg_mask);
                blackBackgroundView.setVisibility(View.VISIBLE);
                blackBackgroundView.setAlpha(0.9f);

                Threads.postOnThreadPoolExecutor(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bitmap = ChargingScreen.this.getFitScreenWallpaperBitmap();

                        if (bitmap == null || bitmap.getWidth() <= 0 || bitmap.getHeight() <= 0) {
                            return;
                        }

                        final Bitmap blurBitmap = Bitmaps.fastBlur(bitmap, 10, 5);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                imageBackgroundView.setImageBitmap(blurBitmap);
                            }
                        });
                    }
                });

                break;
            }

            case SKIN_TYPE_WALLPAPER_BACKGROUND: {

                chargingQuantityUpColor = ContextCompat.getColor(context, R.color.charging_screen_wallpaper_quantity_up);
                chargingQuantityBottomColor = ContextCompat.getColor(context, R.color.charging_screen_wallpaper_quantity_bottom);
                chargingBubbleColor = ContextCompat.getColor(context, R.color.charging_screen_wallpaper_bubble);

                imageBackgroundView.setBackgroundColor(ContextCompat.getColor(context, R.color.charging_screen_black_background));

                View blackBackgroundView = mRootView.findViewById(R.id.charging_screen_bg_mask);
                blackBackgroundView.setVisibility(View.VISIBLE);
                blackBackgroundView.setAlpha(0.7f);

                Threads.postOnThreadPoolExecutor(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bitmap = ChargingScreen.this.getFitScreenWallpaperBitmap();
                        if (bitmap == null) {
                            return;
                        }

                        final Bitmap blurBitmap = Bitmaps.fastBlur(bitmap, 10, 10);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                imageBackgroundView.setImageBitmap(blurBitmap);
                            }
                        });
                    }
                });
                break;
            }
        }

        menuImageView = mRootView.findViewById(R.id.charging_screen_menu);
        slidingFinishRelativeLayout = mRootView.findViewById(R.id.slidingFinishLayout);

        timeTextView = mRootView.findViewById(R.id.charging_screen_time);
        dateTextView = mRootView.findViewById(R.id.charging_screen_date);

        chargingQuantityView = mRootView.findViewById(R.id.charging_screen_battery_level_percent);
        chargingQuantityView.setMaskOpColor(chargingQuantityUpColor, chargingQuantityBottomColor);
        if (context.getResources().getDisplayMetrics().densityDpi <= DisplayMetrics.DENSITY_HIGH) {
            chargingQuantityView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 86);
        }

        fullChargeLeftDescribeTextView = mRootView.findViewById(R.id.charging_screen_full_charge_left_describe);

        speedChargeStateImageView = mRootView.findViewById(R.id.charging_screen_speed_charge_state_icon);
        continuousChargeStateImageView = mRootView.findViewById(R.id.charging_screen_continuous_charge_state_icon);
        trickleChargeStateImageView = mRootView.findViewById(R.id.charging_screen_trickle_charge_state_icon);

        tipTextView = mRootView.findViewById(R.id.charging_screen_tip);
        unlockTextView = mRootView.findViewById(R.id.unlock_tv);
        unlockTextView.setCompoundDrawablePadding(Dimensions.pxFromDp(4));
        ToolTipRelativeLayout toolTipContainer = mRootView.findViewById(R.id.charging_screen_show_tip_container);

        advertisementContainer = mRootView.findViewById(R.id.charging_screen_advertisement_container);
        mNotificationWindowHolder = new NotificationWindowHolder(mRootView, NotificationWindowHolder.SOURCE_CHARGING, this);

        chargingBubbleView = mRootView.findViewById(R.id.charging_screen_bubble_view);
        chargingBubbleView.setPopupBubbleColor(chargingBubbleColor);

        toolTipContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (speedChargeToolTipView != null) {
                        speedChargeToolTipView.remove();
                        speedChargeToolTipView = null;
                    }

                    if (continuousChargeToolTipView != null) {
                        continuousChargeToolTipView.remove();
                        continuousChargeToolTipView = null;
                    }

                    if (trickleChargeToolTipView != null) {
                        trickleChargeToolTipView.remove();
                        trickleChargeToolTipView = null;
                    }
                }

                return false;
            }
        });
    }

    private void showMenuPopupWindow(Context context, View anchorView) {
        if (menuPopupView == null) {
            menuPopupView = new RipplePopupView(context, mRootView);
            View view = LayoutInflater.from(context).inflate(R.layout.charging_screen_popup_window, mRootView, false);
            TextView txtCloseChargingBoost = view.findViewById(R.id.tv_close);
            txtCloseChargingBoost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ChargingScreenUtils.isFastDoubleClick()) {
                        return;
                    }
                    menuPopupView.dismiss();
                    showChargingScreenCloseDialog();
                }
            });

            menuPopupView.setOutSideBackgroundColor(Color.TRANSPARENT);
            menuPopupView.setContentView(view);
            menuPopupView.setOutSideClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    menuPopupView.dismiss();
                }
            });
        }
        menuPopupView.showAsDropDown(anchorView,
                -(getContext().getResources().getDimensionPixelOffset(R.dimen.lock_screen_pop_menu_offset_x) - anchorView.getWidth()),
                -(getContext().getResources().getDimensionPixelOffset(R.dimen.charging_screen_menu_to_top_height) + anchorView.getHeight()) / 2);

    }

    private String getChargingLeftTimeString(int chargingLeftMinutes) {
        boolean isChina = Locale.getDefault().getCountry().equals(Locale.CHINA.getCountry());

        String leftTime = "";
        if (chargingLeftMinutes / 60 > 0) {
            leftTime += chargingLeftMinutes / 60 + (isChina ? "小时 " : "h ");
        }
        if (chargingLeftMinutes % 60 > 0) {
            leftTime += chargingLeftMinutes % 60 + (isChina ? "分钟" : "m");
        }

        return leftTime;
    }

    private void updateTimeAndDateView() {
        String txtHour;
        String strTimeFormat = Settings.System.getString(getContext().getContentResolver(), Settings.System.TIME_12_24);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat();

        if ("24".equals(strTimeFormat)) {
            simpleDateFormat.applyPattern("HH");
            txtHour = simpleDateFormat.format(new Date());
        } else {
            simpleDateFormat.applyPattern("hh");
            txtHour = simpleDateFormat.format(new Date());
        }

        simpleDateFormat.applyPattern("mm");
        String txtMinute = simpleDateFormat.format(new Date());

        String txtDay = String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        String txtWeek = Calendar.getInstance().getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault());
        String txtMonth = Calendar.getInstance().getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault());

        timeTextView.setText(getContext().getString(R.string.charging_screen_time, txtHour, txtMinute));
        dateTextView.setText(getContext().getString(R.string.charging_screen_date, txtMonth, txtDay, txtWeek));
    }

    private void updateChargingStateTipIconAnimator() {
        final int chargingRemainingPercent = ChargingScreenUtils.getBatteryPercentage(HSApplication.getContext());

        if (chargingRemainingPercent < 80) {
            speedChargeStateImageView.setAlpha(CHARGING_STATE_TIP_ICON_POSITIVE_ALPHA);
            continuousChargeStateImageView.setAlpha(CHARGING_STATE_TIP_ICON_NEGATIVE_ALPHA);
            trickleChargeStateImageView.setAlpha(CHARGING_STATE_TIP_ICON_NEGATIVE_ALPHA);
            startChargingStateTipIconAnimator(speedChargeStateImageView);
        } else if (chargingRemainingPercent < 100) {
            speedChargeStateImageView.setAlpha(CHARGING_STATE_TIP_ICON_POSITIVE_ALPHA);
            continuousChargeStateImageView.setAlpha(CHARGING_STATE_TIP_ICON_POSITIVE_ALPHA);
            trickleChargeStateImageView.setAlpha(CHARGING_STATE_TIP_ICON_NEGATIVE_ALPHA);
            startChargingStateTipIconAnimator(continuousChargeStateImageView);
        } else {
            speedChargeStateImageView.setAlpha(CHARGING_STATE_TIP_ICON_POSITIVE_ALPHA);
            continuousChargeStateImageView.setAlpha(CHARGING_STATE_TIP_ICON_POSITIVE_ALPHA);
            trickleChargeStateImageView.setAlpha(CHARGING_STATE_TIP_ICON_POSITIVE_ALPHA);
            startChargingStateTipIconAnimator(trickleChargeStateImageView);
        }
    }

    private void startChargingStateTipIconAnimator(View tipIconView) {
        cancelChargingStateAlphaAnimation();

        chargingStateAlphaAnimator = ObjectAnimator.ofFloat(tipIconView, "alpha",
                CHARGING_STATE_TIP_ICON_ANIMATOR_MIN_ALPHA,
                CHARGING_STATE_TIP_ICON_ANIMATOR_MAX_ALPHA,
                CHARGING_STATE_TIP_ICON_ANIMATOR_MIN_ALPHA);

        chargingStateAlphaAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        chargingStateAlphaAnimator.setRepeatMode(ObjectAnimator.REVERSE);
        chargingStateAlphaAnimator.setDuration(DURATION_CHARGING_STATE_TIP_ICON_ANIMATOR);
        chargingStateAlphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        if (LOG_VERBOSE) {
            chargingStateAlphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                }
            });
        }
        chargingStateAlphaAnimator.start();
    }

    private void updateTipTextRandomValue() {
        Context context = getContext();
        final String[] tips = new String[]{
                context.getString(R.string.charging_screen_charging_tip2),
                context.getString(R.string.charging_screen_charging_tip5),
        };

        tipTextView.setText(tips[new Random().nextInt(tips.length)]);
    }

    private void showChargingScreenCloseDialog() {
        if (mCloseLockerPopupView == null) {
            mCloseLockerPopupView = new PopupView(getContext(), mRootView);
            View content = LayoutInflater.from(getContext()).inflate(R.layout.locker_popup_dialog, null);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams((int) (Dimensions
                    .getPhoneWidth(getContext()) * 0.872f), WRAP_CONTENT);
            content.setLayoutParams(layoutParams);
            TextView title = ViewUtils.findViewById(content, R.id.title);
            TextView hintContent = ViewUtils.findViewById(content, R.id.hint_content);
            AppCompatButton buttonYes = ViewUtils.findViewById(content, R.id.button_yes);
            AppCompatButton buttonNo = ViewUtils.findViewById(content, R.id.button_no);
            title.setText(R.string.charging_screen_close_dialog_title);
            hintContent.setText(R.string.charging_screen_close_dialog_content);
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
            Drawable buttonYesDrawable = new RippleDrawable(ColorStateList.valueOf(mRootView.getResources().getColor(R.color.ripples_ripple_color)), shape, mask);
            Drawable buttonNoDrawable = new RippleDrawable(ColorStateList.valueOf(mRootView.getResources().getColor(R.color.ripples_ripple_color)), shape, mask);

            buttonNo.setBackground(buttonYesDrawable);
            buttonYes.setBackground(buttonNoDrawable);
            buttonYes.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    ChargingScreenSettings.setChargingScreenEnabled(false);
                    mCloseLockerPopupView.dismiss();
                    mDismissReason = "TurnOff";
                    dismiss(getContext(), false);
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

    private Bitmap getFitScreenWallpaperBitmap() {
        Drawable wallpaperDrawable = null;
        try {
            wallpaperDrawable = WallpaperManager.getInstance(getContext()).getDrawable();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (wallpaperDrawable == null) {
            return null;
        }

        Bitmap wallpaperBitmap = ((BitmapDrawable) wallpaperDrawable).getBitmap();
        if (wallpaperBitmap == null) {
            return null;
        }

        final int resultWidth = Math.min(Dimensions.getPhoneWidth(getContext()), wallpaperBitmap.getWidth());
        final int resultHeight = Math.min(Dimensions.getPhoneHeight(getContext()), wallpaperBitmap.getHeight());

        if (resultWidth <= 0 || resultHeight <= 0) {
            return null;
        }

        return Bitmap.createBitmap(wallpaperBitmap, 0, 0, resultWidth, resultHeight);
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        switch (s) {
            case ScreenStatusReceiver.NOTIFICATION_SCREEN_ON:
                onStart();
                break;
            case ScreenStatusReceiver.NOTIFICATION_SCREEN_OFF:
                onStop();
                break;
            case LauncherPhoneStateListener.NOTIFICATION_CALL_RINGING:
                mDismissReason = "Ringing";
                dismiss(getContext(), false);
                break;
            case KeyguardHandler.EVENT_KEYGUARD_UNLOCKED:
                unlockTextView.setText(R.string.unlock_tint_no_keyguard);
                unlockTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unlock_icon, 0, 0, 0);
                break;
            case KeyguardHandler.EVENT_KEYGUARD_LOCKED:
                unlockTextView.setText(R.string.unlock_tint_keyguard);
                unlockTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                break;
            case ChargingScreen.EVENT_CHARGING_FINISH_SELF:
                dismiss(getContext(), true);
                break;
            default:
                break;
        }
    }

    @Override
    public void onStop() {
        // ======== onPause ========
        isStart = false;

        if (chargingBubbleView != null) {
            chargingBubbleView.pauseAnim();
        }

        // ======== onStop ========
        HSLog.d(TAG, "onStop()");
        if (System.currentTimeMillis() - onStartTime > DateUtils.SECOND_IN_MILLIS) {
            LockerCustomConfig.getLogger().logEvent("AcbAdNative_Viewed_In_App", new String[]{LockerCustomConfig.get().getLockerAndChargingAdName(), String.valueOf(mAdShown)});
            mAdShown = false;
        }

        HSChargingManager.getInstance().removeChargingListener(chargingListener);
        try {
            getContext().unregisterReceiver(timeTickReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }

        cancelChargingStateAlphaAnimation();

    }

    public void onDestroy() {
        super.onDestroy();
        // ======== onDestroy ========
        HSLog.d(TAG, "onDestroy()");

        if (mHomeKeyWatcher != null) {
            mHomeKeyWatcher.stopWatch();
        }

        advertisementContainer.removeAllViews();
        if (expressAdView != null) {
            expressAdView.destroy();
        }
        // Life cycle
        LockScreensLifeCycleRegistry.setChargingScreenActive(false);
        HSGlobalNotificationCenter.removeObserver(this);
        LockNotificationManager.getInstance().unregisterForThemeStateChange(mNotificationWindowHolder);
        LockNotificationManager.getInstance().unregisterForChargingScreenChange(observer);
        HSGlobalNotificationCenter.removeObserver(mNotificationWindowHolder);

    }

    @Override
    public boolean isActivityHost() {
        return mActivityMode;
    }

    @Override
    public void dismiss(Context context, boolean dismissKeyguard) {
        boolean mDismissed = true;
        HSLog.i("LockManager", "C dismiss: " + mDismissReason + "  KG: " + dismissKeyguard + "  context: " + context);

        LockerCustomConfig.getLogger().logEvent("ColorPhone_Screen_Close",
                "type", Commons.isKeyguardLocked(getContext(), false) ? "locked" : "unlocked",
                "Time", String.valueOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)));

        LockerCustomConfig.getLogger().logEvent("ChargingScreen_Close",
                "Reason", mDismissReason,
                "Brand", Build.BRAND.toLowerCase(), "DeviceVersion", getDeviceInfo());

        if (!mIsSetup) {
            return;
        }
        mIsSetup = false;

        super.dismiss(context, dismissKeyguard);
    }

    @Override
    public void onNotificationClick() {
        dismiss(getContext(), true);
    }

    private CharingScreenChangeObserver observer = new CharingScreenChangeObserver() {
        @Override
        public void onReceive(int showNumber) {
            int phoneHeight = Dimensions.getPhoneHeight(getContext());
            if (showNumber == 2) {
                if (phoneHeight <= 1920) {
                    chargingQuantityView.setTextSize(80 * phoneHeight / 1920 - 5);
                }
            } else {
                chargingQuantityView.setTextSize(90);
            }
        }
    };
}
