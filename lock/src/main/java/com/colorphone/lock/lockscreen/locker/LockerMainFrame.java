package com.colorphone.lock.lockscreen.locker;

import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.AppCompatButton;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.colorphone.lock.BuildConfig;
import com.colorphone.lock.LockerCustomConfig;
import com.colorphone.lock.PopupView;
import com.colorphone.lock.R;
import com.colorphone.lock.RipplePopupView;
import com.colorphone.lock.ScreenStatusReceiver;
import com.colorphone.lock.lockscreen.FloatWindowCompat;
import com.colorphone.lock.lockscreen.KeyguardHandler;
import com.colorphone.lock.lockscreen.LockNotificationManager;
import com.colorphone.lock.lockscreen.LockScreen;
import com.colorphone.lock.lockscreen.chargingscreen.ChargingScreenSettings;
import com.colorphone.lock.lockscreen.chargingscreen.ChargingScreenUtils;
import com.colorphone.lock.lockscreen.locker.shimmer.Shimmer;
import com.colorphone.lock.lockscreen.locker.shimmer.ShimmerTextView;
import com.colorphone.lock.lockscreen.locker.slidingdrawer.SlidingDrawer;
import com.colorphone.lock.lockscreen.locker.slidingdrawer.SlidingDrawerContent;
import com.colorphone.lock.lockscreen.locker.slidingup.SlidingUpCallback;
import com.colorphone.lock.lockscreen.locker.slidingup.SlidingUpTouchListener;
import com.colorphone.lock.util.ViewUtils;
import com.colorphone.smartlocker.utils.AutoPilotUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.flashlight.FlashlightManager;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Preferences;

import net.appcloudbox.ads.expressad.AcbExpressAdView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import colorphone.acb.com.libweather.WeatherClockManager;
import colorphone.acb.com.libweather.WeatherUtils;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.colorphone.lock.ScreenStatusReceiver.NOTIFICATION_SCREEN_ON;


public class LockerMainFrame extends RelativeLayout implements INotificationObserver, SlidingDrawer.SlidingDrawerListener, NotificationWindowHolder.NotificationClickCallback {

    public static final String EVENT_SLIDING_DRAWER_OPENED = "EVENT_SLIDING_DRAWER_OPENED";
    public static final String EVENT_SLIDING_DRAWER_CLOSED = "EVENT_SLIDING_DRAWER_CLOSED";

    private boolean mIsSlidingDrawerOpened = false;
    private boolean mIsBlackHoleShowing = false;

    private LockScreen mLockScreen;

    private View mDimCover;
    private SlidingDrawer mSlidingDrawer;
    private SlidingDrawerContent mSlidingDrawerContent;
    private View mDrawerHandleUp;
    private View mDrawerHandleDown;
    private Shimmer mShimmer;
    private ShimmerTextView mUnlockText;

    private View mBottomOperationArea;
    private View mCameraContainer;
    private View mToolBarContainer;
    private View mWallpaperContainer;
    private RelativeLayout mAdContainer;
    private RelativeLayout mBottomlayout;

    private NotificationWindowHolder mNotificationWindowHolder;
    private View mMenuMore;
    private RipplePopupView menuPopupView;
    private PopupView mCloseLockerPopupView;

    private TextView mTvTime;
    private TextView mTvDate;
    private TextView mTvWeather;
    private ImageView mConditionIcon;
    private AcbExpressAdView expressAdView;
    private boolean mAdShown;
    private long mOnStartTime;

    private int lockerCount = 0;
    private boolean ifRegisterForTime = false;
    private ImageView mGameIconEntrance;

    private LottieAnimationView mGameLottieEntrance;
    private View mGameLottieTitleEntrance;
    private String gameEntranceType;

    private Handler mHandler = new Handler();
    private Runnable foregroundEventLogger = new Runnable() {
        private boolean logOnceFlag = false;

        @Override
        public void run() {
            String suffix = ChargingScreenUtils.isFromPush ? "_Push" : "";
            if (!logOnceFlag) {
                LockerCustomConfig.getLogger().logEvent("ColorPhone_LockScreen_Show" + suffix,
                        "Brand", Build.BRAND.toLowerCase(),
                        "DeviceVersion", Locker.getDeviceInfo());
                AutoPilotUtils.logLockerModeAutopilotEvent("lock_show");
                logOnceFlag = true;
            }
            if (ScreenStatusReceiver.isScreenOn()) {
                LockerCustomConfig.getLogger().logEvent("LockScreen_Show_Foreground" + suffix,
                        "Brand", Build.BRAND.toLowerCase(),
                        "DeviceVersion", Locker.getDeviceInfo());
            }
        }
    };
    private boolean isStarted;

    public LockerMainFrame(Context context) {
        this(context, null);
    }

    public LockerMainFrame(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LockerMainFrame(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    void setLockScreen(LockScreen lockScreen) {
        mLockScreen = lockScreen;
        mSlidingDrawerContent.setLockScreen((Locker) mLockScreen);
    }

    public void onBackPressed() {
        if (!mIsBlackHoleShowing && mIsSlidingDrawerOpened && mSlidingDrawer != null) {
            mSlidingDrawer.closeDrawer(true);
        }
    }

    public void clearDrawerBackground() {
        if (mSlidingDrawerContent != null) {
            mSlidingDrawerContent.clearBlurredBackground();
        }
    }

    public void closeDrawer() {
        if (mSlidingDrawer != null) {
            mSlidingDrawer.closeDrawer(false);
            onScrollStarted();
            onScrollEnded(false);
            mBottomOperationArea.setAlpha(1);
            mToolBarContainer.setAlpha(1);
            mDrawerHandleDown.setAlpha(0);
            mDimCover.setAlpha(0);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        ImageView appIcon = findViewById(R.id.app_custom_icon);
        appIcon.setImageResource(LockerCustomConfig.get().getCustomScreenIcon());
        if (!FloatWindowCompat.needsSystemErrorFloatWindow()) {
            setPadding(0, 0, 0, Dimensions.getNavigationBarHeight(HSApplication.getContext()));
        }
        mNotificationWindowHolder = new NotificationWindowHolder(this, NotificationWindowHolder.SOURCE_LOCKER, this);
        mDimCover = findViewById(R.id.dim_cover);
        mSlidingDrawerContent = (SlidingDrawerContent) findViewById(R.id.sliding_drawer_content);
        //mDrawerHandleUp = findViewById(R.id.handle_action_up);
        mDrawerHandleDown = findViewById(R.id.handle_action_down);
        mBottomOperationArea = findViewById(R.id.bottom_operation_area);
        mSlidingDrawer = (SlidingDrawer) findViewById(R.id.operation_area);
        mCameraContainer = findViewById(R.id.camera_container);
        mToolBarContainer = findViewById(R.id.toolbar_container);
        mWallpaperContainer = findViewById(R.id.wallpaper_container);
        mAdContainer = ViewUtils.findViewById(this, R.id.rl_ad_container);
        mBottomlayout = findViewById(R.id.bottom_layout);
        mBottomlayout.setPadding(0, 0, 0, Dimensions.getNavigationBarHeight(getContext()));
        LockerMainFrame.LayoutParams layoutParams = (LayoutParams) mAdContainer.getLayoutParams();
        layoutParams.bottomMargin = Dimensions.pxFromDp(54) + Dimensions.getNavigationBarHeight(getContext());
        mAdContainer.setLayoutParams(layoutParams);
        SlidingDrawer.LayoutParams params = (FrameLayout.LayoutParams) mSlidingDrawerContent.getLayoutParams();
        params.height = Dimensions.pxFromDp(340) + Dimensions.getNavigationBarHeight(getContext());
        mSlidingDrawerContent.setLayoutParams(params);
        mMenuMore = findViewById(R.id.ic_menu);
        mMenuMore.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LockerMainFrame.this.showMenuPopupWindow(getContext(), mMenuMore);
                LockerCustomConfig.getLogger().logEvent("Locker_Menu_Clicked");
            }
        });

        mGameIconEntrance = findViewById(R.id.lock_game_view);
        mGameLottieEntrance = (LottieAnimationView) findViewById(R.id.animation_game_view);
        mGameLottieTitleEntrance = findViewById(R.id.animation_game_view_hint);

        View.OnClickListener clickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                onGameClick();
            }
        };

        mGameIconEntrance.setOnClickListener(clickListener);
        mGameLottieEntrance.setOnClickListener(clickListener);
        mGameLottieTitleEntrance.setOnClickListener(clickListener);

        if (isGameEntranceEnable()) {
            updateLockerEntrance();
            onGameShow();
        } else {
            mGameIconEntrance.setVisibility(GONE);
            mGameLottieEntrance.setVisibility(GONE);
            mGameLottieTitleEntrance.setVisibility(GONE);
        }

        mSlidingDrawer.setListener(this);
        mSlidingDrawer.setHandle(R.id.blank_handle, 0);
        mDrawerHandleDown.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mSlidingDrawer.closeDrawer(true);
            }
        });
        /*mDrawerHandleUp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mIsSlidingDrawerOpened) {
                    mSlidingDrawer.doBounceUpAnimation();
                }
            }
        });*/

        mToolBarContainer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mIsSlidingDrawerOpened) {
                    mSlidingDrawer.openDrawer(true);
                    mDimCover.setVisibility(View.VISIBLE);
                }
            }
        });

        mUnlockText = (ShimmerTextView) findViewById(R.id.unlock_text);
        mUnlockText.setCompoundDrawablePadding(Dimensions.pxFromDp(4));
        mShimmer = new Shimmer();
        mShimmer.setDuration(1200);

        mTvTime = (TextView) findViewById(R.id.tv_time);
        mTvDate = (TextView) findViewById(R.id.tv_date);
        mTvWeather = (TextView) findViewById(R.id.tv_weather);
        mConditionIcon = (ImageView) findViewById(R.id.iv_weather_icon);
        refreshClock();
        registerReceiverForClock();
        mAdShown = false;
        LockerCustomConfig.get().onEventLockerShow();
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        LockNotificationManager.getInstance().registerForThemeStateChange(mNotificationWindowHolder);
        LockNotificationManager.getInstance().registerForLockerSpaceNotEnough(observer);
        HSGlobalNotificationCenter.addObserver(NOTIFICATION_SCREEN_ON, mNotificationWindowHolder);


        ViewTreeObserver viewTreeObserver = getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeGlobalOnLayoutListener(this);
                //mSlidingDrawer.setTranslationY(mSlidingDrawer.getHeight() - Dimensions.pxFromDp(48));
                mSlidingDrawer.setTranslationY(mSlidingDrawer.getHeight());
            }
        });

        requestFocus();

        HSGlobalNotificationCenter.addObserver(ScreenStatusReceiver.NOTIFICATION_SCREEN_OFF, this);
        HSGlobalNotificationCenter.addObserver(ScreenStatusReceiver.NOTIFICATION_SCREEN_ON, this);
        HSGlobalNotificationCenter.addObserver(SlidingDrawerContent.EVENT_SHOW_BLACK_HOLE, this);
        HSGlobalNotificationCenter.addObserver(KeyguardHandler.EVENT_KEYGUARD_UNLOCKED, this);
        HSGlobalNotificationCenter.addObserver(KeyguardHandler.EVENT_KEYGUARD_LOCKED, this);
        requestAds();

        PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
        if (pm.isScreenOn()) {
            mShimmer.start(mUnlockText);
        }

        if (mLockScreen != null && !mLockScreen.isActivityHost()) {
            onStart();
        }
    }


    private boolean isGameEntranceEnable() {
        return LockerCustomConfig.get().isGameEntranceEnable();
    }

    private void onGameShow() {
        LockerCustomConfig.getLogger().logEvent("LockScreen_GameCenter_Shown", "type", gameEntranceType);
    }

    private void onGameClick() {
        LockerCustomConfig.get().getGameCallback().startGameCenter(getContext());
        LockerCustomConfig.getLogger().logEvent("LockScreen_GameCenter_Clicked", "type", gameEntranceType);
    }

    private void increaseLockerCounter() {
        lockerCount++;
        if (lockerCount > 20) {
            lockerCount = 0;
        }
        Preferences.get(ChargingScreenSettings.LOCKER_PREFS).putInt("locker_game_count", lockerCount);
    }

    private void updateLockerEntrance() {
        lockerCount = Preferences.get(ChargingScreenSettings.LOCKER_PREFS)
                .getInt("locker_game_count", 0);

        final int count = lockerCount;
        if (count == 0) {
            showGameAsLottie(true);
            gameEntranceType = "Tetris";
            mGameLottieEntrance.setAnimation("tetris.json");
            mGameLottieEntrance.setImageAssetsFolder("tetrisImages");
            mGameLottieEntrance.playAnimation();

        }
        if (count > 0 && count <= 6) {
            gameEntranceType = "GamePad";
            showGameAsLottie(false);
        }
        if (count == 7) {
            showGameAsLottie(true);
            gameEntranceType = "RacingCar";
            mGameLottieEntrance.setAnimation("racing.json");
            mGameLottieEntrance.setImageAssetsFolder("racingImages");
            mGameLottieEntrance.playAnimation();
        }
        if (count > 7 && count <= 13) {
            gameEntranceType = "GamePad";
            showGameAsLottie(false);
        }
        if (count == 14) {
            gameEntranceType = "Basketball";
            showGameAsLottie(true);
            mGameLottieEntrance.setAnimation("dunk.json");
            mGameLottieEntrance.setImageAssetsFolder("dunkImages");
            mGameLottieEntrance.playAnimation();
        }
        if (count > 14 && count <= 20) {
            gameEntranceType = "GamePad";
            showGameAsLottie(false);
        }

        increaseLockerCounter();
    }

    private void showGameAsLottie(boolean showLottie) {
        //mGameIconEntrance.setVisibility(showLottie ? GONE : VISIBLE);
        //mGameLottieEntrance.setVisibility(showLottie ? VISIBLE : GONE);
        //mGameLottieTitleEntrance.setVisibility(showLottie ? VISIBLE : GONE);
    }

    private void requestAds() {
        if (!HSConfig.optBoolean(false, "Application", "LockerAutoRefreshAdsEnable")) {
            LockerCustomConfig.getLogger().logEvent("SmartLockerFeed1_NativeAd", "type", "Chance");
            LockerCustomConfig.getLogger().logEvent("ad_chance");
            AutoPilotUtils.logLockerModeAutopilotEvent("ad_chance");
        }
        expressAdView = new AcbExpressAdView(getContext(), LockerCustomConfig.get().getSmartLockerAdName1(), "");
        expressAdView.setExpressAdViewListener(new AcbExpressAdView.AcbExpressAdViewListener() {
            @Override
            public void onAdShown(AcbExpressAdView acbExpressAdView) {
                LockerCustomConfig.getLogger().logEvent("SmartLockerFeed1_NativeAd", "type", "AdView");
                LockerCustomConfig.getLogger().logEvent("ad_show");
                AutoPilotUtils.logLockerModeAutopilotEvent("ad_show");
                mAdShown = true;
                LockerCustomConfig.get().onEventLockerAdShow();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mAdContainer.setBackground(BackgroundDrawables.createBackgroundDrawable(Color.WHITE, Dimensions.pxFromDp(8), false));
                    mAdContainer.setPadding(Dimensions.pxFromDp(10), Dimensions.pxFromDp(10), Dimensions.pxFromDp(10), Dimensions.pxFromDp(0));
                }
            }

            @Override
            public void onAdClicked(AcbExpressAdView acbExpressAdView) {
                LockerCustomConfig.getLogger().logEvent("SmartLockerFeed1_NativeAd", "type", "AdClick");
                LockerCustomConfig.get().onEventLockerAdClick();
                HSBundle bundle = new HSBundle();
                bundle.putString(Locker.EXTRA_DISMISS_REASON, "AdClick");
                HSGlobalNotificationCenter.sendNotification(Locker.EVENT_FINISH_SELF, bundle);
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
            mAdContainer.addView(expressAdView, new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        }

        if (expressAdView != null && HSConfig.optBoolean(false, "Application", "LockerAutoRefreshAdsEnable")) {
            LockerCustomConfig.getLogger().logEvent("SmartLockerFeed1_NativeAd", "type", "Chance");
            LockerCustomConfig.getLogger().logEvent("ad_chance");
            AutoPilotUtils.logLockerModeAutopilotEvent("ad_chance");

            expressAdView.switchAd();
        }
    }

    public void onStart() {
        PowerManager powerManager = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = powerManager != null && powerManager.isScreenOn();
        if (!isScreenOn) {
            return;
        }

        if (isStarted) {
            return;
        }
        isStarted = true;

        mOnStartTime = System.currentTimeMillis();
        refreshClock();
        registerReceiverForClock();

        mHandler.postDelayed(foregroundEventLogger, 1000);

        // onResume
        showExpressAd();
    }

    public void onStop() {
        isStarted = false;
        if (ifRegisterForTime) {
            unregisterReceiverForClock();
        }
        if (System.currentTimeMillis() - mOnStartTime > DateUtils.SECOND_IN_MILLIS) {
            LockerCustomConfig.getLogger().logEvent("AcbAdNative_Viewed_In_App", new String[]{LockerCustomConfig.get().getSmartLockerAdName1(), String.valueOf(mAdShown)});
            mAdShown = false;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (expressAdView != null) {
            expressAdView.destroy();
        }

        HSGlobalNotificationCenter.removeObserver(this);
        mShimmer.cancel();
        LockNotificationManager.getInstance().unregisterForThemeStateChange(mNotificationWindowHolder);
        LockNotificationManager.getInstance().unregisterForLockerSpaceNotEnough(observer);
        HSGlobalNotificationCenter.removeObserver(mNotificationWindowHolder);

        super.onDetachedFromWindow();
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        switch (s) {
            case SlidingDrawerContent.EVENT_SHOW_BLACK_HOLE:
//                if (mIsBlackHoleShowing) {
//                    break;
//                }
//
//                mIsBlackHoleShowing = true;
//                postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (mBlackHole != null) {
//                            mBlackHole.startAnimation();
//                        }
//                    }
//                }, SlidingDrawerContent.DURATION_BALL_DISAPPEAR);
                break;

            case ScreenStatusReceiver.NOTIFICATION_SCREEN_OFF:
                if (mLockScreen != null && !mLockScreen.isActivityHost()) {
                    onStop();
                }

                if (mShimmer.isAnimating()) {
                    mShimmer.cancel();
                }
                break;

            case ScreenStatusReceiver.NOTIFICATION_SCREEN_ON:

                if (mLockScreen != null && !mLockScreen.isActivityHost()) {
                    onStart();
                }

                if (!mShimmer.isAnimating()) {
                    mShimmer.start(mUnlockText);
                }

                // toggle guide
                if (!LockerSettings.isLockerToggleGuideShown()) {
                    if (mToolBarContainer == null) {
                        return;
                    }

                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            int bounceTranslationY = -Dimensions.pxFromDp(13);
                            ObjectAnimator bounceAnimator = ObjectAnimator.ofFloat(mToolBarContainer,
                                    View.TRANSLATION_Y,
                                    0, bounceTranslationY, 0, bounceTranslationY, 0, bounceTranslationY, 0, bounceTranslationY, 0);
                            bounceAnimator.setDuration(3500);
                            bounceAnimator.setInterpolator(new LinearInterpolator());
                            bounceAnimator.start();
                        }
                    }, 300);
                }
                break;
            case KeyguardHandler.EVENT_KEYGUARD_UNLOCKED:
                mUnlockText.setText(R.string.unlock_tint_no_keyguard);
                mUnlockText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unlock_icon, 0, 0, 0);
                break;
            case KeyguardHandler.EVENT_KEYGUARD_LOCKED:
                mUnlockText.setText(R.string.unlock_tint_keyguard);
                mUnlockText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                break;
            default:
                break;
        }
    }

    private void registerReceiverForClock() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        getContext().registerReceiver(timeChangeReceiver, filter);
        ifRegisterForTime = true;
    }

    private void unregisterReceiverForClock() {
        getContext().unregisterReceiver(timeChangeReceiver);
        ifRegisterForTime = false;
    }

    private void refreshClock() {

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        boolean is24HourFormat = false;
        try {
            is24HourFormat = android.text.format.DateFormat.is24HourFormat(getContext());
        } catch (Exception ignore) {
        }
        if (!is24HourFormat && hour != 12) {
            hour = hour % 12;
        }
        mTvTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
        DateFormat format = new SimpleDateFormat("M月d日\tEEE", Locale.getDefault());
        mTvDate.setText(format.format(new Date()));

        if (WeatherClockManager.getInstance().getWeather() != null) {
            mTvWeather.setVisibility(VISIBLE);
            mConditionIcon.setVisibility(VISIBLE);
            String simpleConditionDesc = WeatherClockManager.getInstance().getSimpleConditionDescription(
                    WeatherClockManager.getInstance().getWeather().getCurrentCondition().getCondition());
            mTvWeather.setText(simpleConditionDesc);
            mConditionIcon.setImageResource(WeatherUtils.getWeatherConditionSmallIconResourceId(
                    WeatherClockManager.getInstance().getWeather().getCurrentCondition().getCondition(), false));
        } else {
            mTvWeather.setVisibility(GONE);
            mConditionIcon.setVisibility(GONE);
        }
    }


    @Override
    public void onScrollStarted() {
        mBottomOperationArea.setVisibility(View.VISIBLE);
        mDimCover.setVisibility(View.VISIBLE);
        //mAdContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onScrollEnded(boolean expanded) {
        LockerSettings.setLockerToggleGuideShown();
        mIsSlidingDrawerOpened = expanded;

        if (mIsSlidingDrawerOpened) {
            mBottomOperationArea.setVisibility(View.INVISIBLE);
            HSGlobalNotificationCenter.sendNotification(EVENT_SLIDING_DRAWER_OPENED);
            LockerCustomConfig.getLogger().logEvent("Locker_Toggle_Slided");
        } else {
            mDimCover.setVisibility(View.INVISIBLE);
            HSGlobalNotificationCenter.sendNotification(EVENT_SLIDING_DRAWER_CLOSED);
        }
    }

    @Override
    public void onScroll(float cur, float total) {
        float heightToDisappear = Dimensions.pxFromDp(24);
        float alpha = (heightToDisappear + cur - total) / heightToDisappear;
        alpha = alpha < 0 ? 0 : (alpha > 1 ? 1 : alpha);
        mBottomOperationArea.setAlpha(alpha);
        mToolBarContainer.setAlpha(cur / total);
        mDrawerHandleDown.setAlpha(1f - cur / total);
        mDimCover.setAlpha(1f - cur / total);
        mSlidingDrawerContent.onScroll(cur, total);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mIsSlidingDrawerOpened) {
            if (ev.getAction() == MotionEvent.ACTION_DOWN
                    && !LockerUtils.isTouchInView(mSlidingDrawer, ev)
                    && !mIsBlackHoleShowing) {
                mSlidingDrawer.closeDrawer(true);
                return true;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public void setSlidingUpCallback(SlidingUpCallback callback) {
        final SlidingUpTouchListener rightListener = new SlidingUpTouchListener(SlidingUpTouchListener.TYPE_RIGHT, callback);
        mCameraContainer.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!FlashlightManager.getInstance().isOn() && !mIsSlidingDrawerOpened) {
                    rightListener.onTouch(v, event);
                }
                return true;
            }
        });

        final SlidingUpTouchListener leftListener = new SlidingUpTouchListener(SlidingUpTouchListener.TYPE_LEFT, callback);
        mWallpaperContainer.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                leftListener.onTouch(v, event);
                return true;
            }
        });
    }

    private void showMenuPopupWindow(Context context, View anchorView) {
        if (menuPopupView == null) {
            menuPopupView = new RipplePopupView(context, mLockScreen.getRootView());
            View view = LayoutInflater.from(context).inflate(R.layout.charging_screen_popup_window,
                    mLockScreen.getRootView(), false);
            TextView txtCloseChargingBoost = (TextView) view.findViewById(R.id.tv_close);
            txtCloseChargingBoost.setText(getResources().getString(R.string.locker_menu_disable));
            txtCloseChargingBoost.requestLayout();
            txtCloseChargingBoost.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ChargingScreenUtils.isFastDoubleClick()) {
                        return;
                    }
                    LockerCustomConfig.getLogger().logEvent("Locker_DisableLocker_Clicked");
                    menuPopupView.dismiss();
                    LockerMainFrame.this.showLockerCloseDialog();
                }
            });

            menuPopupView.setOutSideBackgroundColor(Color.TRANSPARENT);
            menuPopupView.setContentView(view);
            menuPopupView.setOutSideClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    menuPopupView.dismiss();
                }
            });
        }

        menuPopupView.showAsDropDown(anchorView,
                -(getResources().getDimensionPixelOffset(R.dimen.lock_screen_pop_menu_offset_x) - anchorView.getWidth()),
                -(getResources().getDimensionPixelOffset(R.dimen.charging_screen_menu_to_top_height)
                        + anchorView.getHeight()) / 2);
    }

    private void showLockerCloseDialog() {
        if (mCloseLockerPopupView == null) {
            mCloseLockerPopupView = new PopupView(getContext(), mLockScreen.getRootView());
            View content = LayoutInflater.from(getContext()).inflate(R.layout.locker_popup_dialog, null);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams((int) (Dimensions
                    .getPhoneWidth(getContext()) * 0.872f), WRAP_CONTENT);
            content.setLayoutParams(layoutParams);
            TextView title = ViewUtils.findViewById(content, R.id.title);
            TextView hintContent = ViewUtils.findViewById(content, R.id.hint_content);
            AppCompatButton buttonYes = ViewUtils.findViewById(content, R.id.button_yes);
            AppCompatButton buttonNo = ViewUtils.findViewById(content, R.id.button_no);
            title.setText(R.string.locker_disable_confirm);
            hintContent.setText(R.string.locker_disable_confirm_detail);
            buttonNo.setText(R.string.charging_screen_close_dialog_positive_action);
            buttonNo.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    mCloseLockerPopupView.dismiss();
                }
            });
            buttonYes.setText(R.string.charging_screen_close_dialog_negative_action);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                GradientDrawable mask = new GradientDrawable();
                mask.setColor(Color.WHITE);
                GradientDrawable shape = new GradientDrawable();
                shape.setColor(Color.TRANSPARENT);
                Drawable buttonYesDrawable = new RippleDrawable(ColorStateList.valueOf(getResources().getColor(R.color.ripples_ripple_color)), shape, mask);
                Drawable buttonNoDrawable = new RippleDrawable(ColorStateList.valueOf(getResources().getColor(R.color.ripples_ripple_color)), shape, mask);

                buttonNo.setBackground(buttonYesDrawable);
                buttonYes.setBackground(buttonNoDrawable);
            }
            buttonYes.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    LockerSettings.setLockerEnabled(false);
                    mLockScreen.dismiss(getContext(), false);
                    Toast.makeText(getContext(), R.string.locker_diabled_success, Toast.LENGTH_SHORT).show();
                    LockerCustomConfig.getLogger().logEvent("Locker_DisableLocker_Alert_TurnOff");
                    mCloseLockerPopupView.dismiss();
                }
            });
            mCloseLockerPopupView.setOutSideBackgroundColor(0xB3000000);
            mCloseLockerPopupView.setContentView(content);
            mCloseLockerPopupView.setOutSideClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    mCloseLockerPopupView.dismiss();
                }
            });
        }
        mCloseLockerPopupView.showInCenter();
    }


    @Override
    public void onNotificationClick() {
        mLockScreen.dismiss(getContext(), true);
    }

    private final BroadcastReceiver timeChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_TIME_TICK)) {
                //request data for update weather
                WeatherClockManager.getInstance().updateWeatherIfNeeded();

                refreshClock();
            }
        }
    };

    private TimeTextSizeChangeObserver observer = new TimeTextSizeChangeObserver() {
        @Override
        public void update(int showNumber, int yCoordinateOfAboveNotification) {
            if (BuildConfig.DEBUG) {
                HSLog.e("AutoSizing For time " + "yCoordinatesOfNotificationAbove " + yCoordinateOfAboveNotification);
            }

            if (showNumber == 2) {
                int[] position = new int[2];
                mTvDate.getLocationOnScreen(position);
                Rect rect = new Rect();
                mTvDate.getLocalVisibleRect(rect);
                int yCoordinatesOfBottomForDate = rect.height() + position[1];
                int phoneHeight = Dimensions.getPhoneHeight(getContext());

                if (BuildConfig.DEBUG) {
                    HSLog.e("AutoSizing For time " + "yCoordinatesOfBottomForDate " + yCoordinatesOfBottomForDate);
                    HSLog.e("AutoSizing For time " + "phoneHeight " + phoneHeight);
                }

                if (yCoordinateOfAboveNotification <= yCoordinatesOfBottomForDate) {
                    if (phoneHeight <= 1920) {
                        mTvTime.setTextSize(60 * phoneHeight / 1920);
                        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) mTvTime.getLayoutParams();
                        layoutParams.verticalBias = 0.12f * phoneHeight / 1920;
                        mTvTime.setLayoutParams(layoutParams);
                    }
                }
            } else {
                mTvTime.setTextSize(60);
            }

        }
    };
}
