package com.colorphone.lock.lockscreen.locker;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.PowerManager;
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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.colorphone.lock.LockerCustomConfig;
import com.colorphone.lock.PopupView;
import com.colorphone.lock.R;
import com.colorphone.lock.RipplePopupView;
import com.colorphone.lock.ScreenStatusReceiver;
import com.colorphone.lock.lockscreen.FloatWindowCompat;
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
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.flashlight.FlashlightManager;
import com.superapps.util.Dimensions;
import com.superapps.util.Preferences;

import net.appcloudbox.ads.expressad.AcbExpressAdView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;


public class LockerMainFrame extends RelativeLayout implements INotificationObserver, SlidingDrawer.SlidingDrawerListener{

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

    private NotificationWindowHolder mNotificationWindowHolder;
    private View mMenuMore;
    private RipplePopupView menuPopupView;
    private PopupView mCloseLockerPopupView;

    private TextView mTvTime;
    private TextView mTvDate;
    private AcbExpressAdView expressAdView;
    private boolean mAdShown;
    private long mOnStartTime;

    private int lockerCount = 0;
    private ImageView mGameIconEntrance;

    private LottieAnimationView mGameLottieEntrance;
    private View mGameLottieTitleEntrance;
    private String gameEntranceType;

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
            mDrawerHandleUp.setAlpha(1);
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
        mNotificationWindowHolder = new NotificationWindowHolder();
        mDimCover = findViewById(R.id.dim_cover);
        mSlidingDrawerContent = (SlidingDrawerContent) findViewById(R.id.sliding_drawer_content);
        mDrawerHandleUp = findViewById(R.id.handle_action_up);
        mDrawerHandleDown = findViewById(R.id.handle_action_down);
        mBottomOperationArea = findViewById(R.id.bottom_operation_area);
        mSlidingDrawer = (SlidingDrawer) findViewById(R.id.operation_area);
        mCameraContainer = findViewById(R.id.camera_container);
        mToolBarContainer = findViewById(R.id.toolbar_container);
        mWallpaperContainer = findViewById(R.id.wallpaper_container);
        mAdContainer = ViewUtils.findViewById(this, R.id.rl_ad_container);
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
        mDrawerHandleUp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mIsSlidingDrawerOpened) {
                    mSlidingDrawer.doBounceUpAnimation();
                }
            }
        });

        mToolBarContainer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mIsSlidingDrawerOpened) {
                    mSlidingDrawer.doBounceUpAnimation();
                }
            }
        });

        mUnlockText = (ShimmerTextView) findViewById(R.id.unlock_text);
        mShimmer = new Shimmer();
        mShimmer.setDuration(1200);

        mTvTime = (TextView) findViewById(R.id.tv_time);
        mTvDate = (TextView) findViewById(R.id.tv_date);
        refreshClock();
        mAdShown = false;
        LockerCustomConfig.get().onEventLockerShow();
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        ViewTreeObserver viewTreeObserver = getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeGlobalOnLayoutListener(this);
                mSlidingDrawer.setTranslationY(mSlidingDrawer.getHeight() - Dimensions.pxFromDp(48));
            }
        });

        requestFocus();

        HSGlobalNotificationCenter.addObserver(ScreenStatusReceiver.NOTIFICATION_SCREEN_OFF, this);
        HSGlobalNotificationCenter.addObserver(ScreenStatusReceiver.NOTIFICATION_SCREEN_ON, this);
        HSGlobalNotificationCenter.addObserver(SlidingDrawerContent.EVENT_SHOW_BLACK_HOLE, this);

        requestAds();

        PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
        if (pm.isScreenOn()) {
            mShimmer.start(mUnlockText);
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
        LockerCustomConfig.getLogger().logEvent("LockScreen_GameCenter_Clicked","type", gameEntranceType);
    }

    private void increaseLockerCounter() {
        lockerCount++;
        if (lockerCount > 20){
            lockerCount = 0;
        }
        Preferences.get(ChargingScreenSettings.LOCKER_PREFS).putInt("locker_game_count", lockerCount);
    }

    private void updateLockerEntrance(){
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
        expressAdView = new AcbExpressAdView(getContext(), LockerCustomConfig.get().getLockerAdName());
        expressAdView.setExpressAdViewListener(new AcbExpressAdView.AcbExpressAdViewListener() {
            @Override
            public void onAdShown(AcbExpressAdView acbExpressAdView) {
                mAdShown = true;
                LockerCustomConfig.get().onEventLockerAdShow();
            }

            @Override
            public void onAdClicked(AcbExpressAdView acbExpressAdView) {
                LockerCustomConfig.get().onEventLockerAdClick();
                HSBundle bundle = new HSBundle();
                bundle.putString(Locker.EXTRA_DISMISS_REASON, "AdClick");
                HSGlobalNotificationCenter.sendNotification(Locker.EVENT_FINISH_SELF, bundle);
            }
        });
        expressAdView.setAutoSwitchAd(AcbExpressAdView.AutoSwitchAd_All);
    }

    private void showExpressAd() {
        if (expressAdView != null && expressAdView.getParent() == null) {
            mAdContainer.setVisibility(View.VISIBLE);
            mAdContainer.addView(expressAdView, new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));
            expressAdView.switchAd();
        }
    }

    public void onStart() {
        mOnStartTime = System.currentTimeMillis();
    }

    public void onResume() {
        if (expressAdView != null && HSConfig.optBoolean(false, "Application", "LockerAutoRefreshAdsEnable")) {
            expressAdView.switchAd();
        }
    }

    public void onPause() {
//        if (expressAdView != null && HSConfig.optBoolean(false, "Application", "LockerAutoRefreshAdsEnable")) {
//            expressAdView.pauseDisplayNewAd();
//        }
    }

    public void onStop() {
        if (System.currentTimeMillis() - mOnStartTime > DateUtils.SECOND_IN_MILLIS) {
            LockerCustomConfig.getLogger().logEvent("AcbAdNative_Viewed_In_App", new String[]{LockerCustomConfig.get().getLockerAdName(), String.valueOf(mAdShown)});
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
                    onPause();
                    onStop();
                }

                if (mShimmer.isAnimating()) {
                    mShimmer.cancel();
                }
                break;
            case ScreenStatusReceiver.NOTIFICATION_SCREEN_ON:


                if (expressAdView == null) {
                    requestAds();
                    showExpressAd();
                } else if (expressAdView.getParent() == null) {
                    showExpressAd();
                } else {
                    onResume();
                }

                if (!mShimmer.isAnimating()) {
                    mShimmer.start(mUnlockText);
                }

                // toggle guide
                if (!LockerSettings.isLockerToggleGuideShown()) {
                    if (mDrawerHandleUp == null) {
                        return;
                    }

                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            int bounceTranslationY = -Dimensions.pxFromDp(13);
                            ObjectAnimator bounceAnimator = ObjectAnimator.ofFloat(mDrawerHandleUp,
                                    View.TRANSLATION_Y,
                                    0, bounceTranslationY, 0, bounceTranslationY, 0, bounceTranslationY, 0, bounceTranslationY, 0);
                            bounceAnimator.setDuration(3500);
                            bounceAnimator.setInterpolator(new LinearInterpolator());
                            bounceAnimator.start();
                        }
                    }, 300);
                }
                break;
            default:
                break;
        }
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
        DateFormat format = new SimpleDateFormat("M月dd日\tEEE", Locale.getDefault());
        mTvDate.setText(format.format(new Date()));
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
            mAdContainer.setVisibility(View.INVISIBLE);
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
        mDrawerHandleUp.setAlpha(cur / total);
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

    private class NotificationWindowHolder {
        private RelativeLayout mNotificationWindow;
        private SlidingNotificationLayout mSlidingWindow;
        private ImageView mSourceAppAvatar;
        private TextView mAppNameAndSendTime;
        private TextView mSenderName;
        private ImageView mSenderAvatar;
        private TextView mNoticationContent;

        public NotificationWindowHolder() {
            mSlidingWindow = findViewById(R.id.lock_sliding_window);
            mSlidingWindow.setClickable(true);
            mNotificationWindow = findViewById(R.id.lock_notification_window);
            mSourceAppAvatar = findViewById(R.id.source_app_avatar);
            mAppNameAndSendTime = findViewById(R.id.source_app_name_and_send_time);
            mSenderAvatar = findViewById(R.id.sender_avatar);
            mSenderName = findViewById(R.id.sender_name);
            mNoticationContent = findViewById(R.id.notification_content);

        }
    }
}
