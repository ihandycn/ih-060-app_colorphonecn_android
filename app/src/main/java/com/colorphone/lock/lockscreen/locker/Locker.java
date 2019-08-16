package com.colorphone.lock.lockscreen.locker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.colorphone.lock.LockerCustomConfig;
import com.colorphone.lock.lockscreen.BaseKeyguardActivity;
import com.colorphone.lock.lockscreen.FloatWindowController;
import com.colorphone.lock.lockscreen.LockScreen;
import com.colorphone.lock.lockscreen.LockScreensLifeCycleRegistry;
import com.colorphone.lock.lockscreen.chargingscreen.ChargingScreenUtils;
import com.colorphone.lock.lockscreen.locker.slidingdrawer.SlidingDrawerContent;
import com.colorphone.lock.lockscreen.locker.slidingup.LockerSlidingUpCallback;
import com.colorphone.lock.lockscreen.locker.statusbar.StatusBar;
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.customize.util.CustomizeUtils;
import com.honeycomb.colorphone.customize.view.OnlineWallpaperPage;
import com.honeycomb.colorphone.util.Analytics;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Commons;
import com.superapps.util.Dimensions;
import com.superapps.util.HomeKeyWatcher;
import com.superapps.util.Threads;

public class Locker extends LockScreen implements INotificationObserver {

    private static final String TAG = "LOCKER_ACTIVITY";

    public static final String EVENT_FINISH_SELF = "locker_event_finish_self";
    public static final String EVENT_WALLPAPER_CHANGE = "locker_event_wallpaper_changed";

    public static final String EXTRA_SHOULD_DISMISS_KEYGUARD = "extra_should_dismiss_keyguard";
    public static final String EXTRA_DISMISS_REASON = "dismiss_reason";
    public static final String PREF_KEY_CURRENT_WALLPAPER_HD_URL = "current_hd_wallpaper_url";

    // Parent
    ViewPager mWallpaperViewPaper;

    // Child
    ViewPagerFixed mViewPager;
    OnlineWallpaperPage mOnlineWallpaperPage;
    private LockerAdapter mLockerAdapter;
    private LockerWallpaperView mLockerWallpaper;

    private boolean mIsDestroyed;

    private HomeKeyWatcher mHomeKeyWatcher;
    private boolean mHomeKeyClicked;
    private boolean mIsSetup;
    private String mDismissReason = "Unkown";
    private boolean mActivityMode;
    private ImageView mWallpaperIcon;
    private View mDimCover;
    private boolean isLockerPageShow = true;

    @Override
    public void setup(ViewGroup root, Bundle extra) {
        super.setup(root, extra);
        mIsSetup = true;
        // ======== onCreate ========
        mHomeKeyWatcher = new HomeKeyWatcher(root.getContext());
        mHomeKeyWatcher.setOnHomePressedListener(new HomeKeyWatcher.OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                mHomeKeyClicked = true;
                mDismissReason = "Home";
                dismiss(getContext(), false);
            }

            @Override
            public void onRecentsPressed() {
            }
        });
        mHomeKeyWatcher.startWatch();

        mLockerWallpaper = mRootView.findViewById(R.id.locker_wallpaper_view);

        try {
            initLockerWallpaper();
        } catch (Exception e) {
            // LauncherGlideModule is not GlideModule
            // only happened on SamSung, android OS 5.0
            if (BuildConfig.DEBUG) {
                throw e;
            }
            mDismissReason = "WallpaperFail";
            dismiss(root.getContext(), false);
        }
        configLockViewPager();

        ViewGroup container = (ViewGroup) mRootView.findViewById(R.id.activity_locker);
        StatusBar statusBar = (StatusBar) LayoutInflater.from(mRootView.getContext())
                .inflate(R.layout.locker_status_bar, container, false);
        container.addView(statusBar);

        if (getContext() instanceof Activity) {
            if (ChargingScreenUtils.isNativeLollipop()) {
                statusBar.setVisibility(View.GONE);
            }
        }

        HSGlobalNotificationCenter.addObserver(EVENT_FINISH_SELF, this);
        HSGlobalNotificationCenter.addObserver(EVENT_WALLPAPER_CHANGE, this);
        HSGlobalNotificationCenter.addObserver(LockerMainFrame.EVENT_RINGTONE_CLICK_MUTE, this);
        HSGlobalNotificationCenter.addObserver(LockerMainFrame.EVENT_RINGTONE_CLICK_MUTE_OFF, this);
        LockerSettings.increaseLockerShowCount();

        // Life cycle
        LockScreensLifeCycleRegistry.setLockerActive(true);
//        HSGlobalNotificationCenter.sendNotification(NotificationCondition.EVENT_LOCK);
    }

    public static String getDeviceInfo() {
        if (Build.VERSION.SDK_INT >= 26) {
            return "8";
        } else if (Build.VERSION.SDK_INT >= 24) {
            return "7";
        } else if (Build.VERSION.SDK_INT >= 23) {
            return "6";
        } else if (Build.VERSION.SDK_INT >= 21) {
            return "5";
        } else {
            return "4";
        }
    }

    public View getDimCover() {
        return mDimCover;
    }

    public void onStart() {
        // ======== onStart ========
        if (mLockerAdapter.lockerMainFrame != null) {
            mLockerAdapter.lockerMainFrame.onStart();
        }
    }

    private Handler mHandler = new Handler();

    private Runnable foregroundEventLogger = new Runnable() {
        private boolean logOnceFlag = false;

        @Override
        public void run() {
            String suffix = ChargingScreenUtils.isFromPush ? "_Push" : "";
            if (!logOnceFlag) {
                LockerCustomConfig.getLogger().logEvent("ColorPhone_LockScreen_Show" + suffix,
                        "Brand", Build.BRAND.toLowerCase(),
                        "DeviceVersion", getDeviceInfo());
                logOnceFlag = true;
                Analytics.logEvent(Analytics.upperFirstCh("lockscreen_show"), "Wallpaper",
                        LockerEventUtils.getWallpaperType(mLockerWallpaper.getType()));
            }
            LockerCustomConfig.getLogger().logEvent("LockScreen_Show_Foreground" + suffix,
                    "Brand", Build.BRAND.toLowerCase(),
                    "DeviceVersion", getDeviceInfo());
        }
    };

    public void onResume() {
        // ======== onResume ========
        if (mHomeKeyClicked && mLockerAdapter != null && mLockerAdapter.lockerMainFrame != null) {
            mHomeKeyClicked = false;
            mLockerAdapter.lockerMainFrame.closeDrawer();
        }
        mLockerWallpaper.onResume(isLockerPageShow);

        mHandler.postDelayed(foregroundEventLogger, 1000);
    }

    private void initLockerWallpaper() {
        String path = CustomizeUtils.getLockerWallpaperPath();

        if (!TextUtils.isEmpty(path)) {
            mLockerWallpaper.setWallPaperFilePath(path, isLockerPageShow);
        } else {
            mLockerWallpaper.setImageResource(R.drawable.wallpaper_locker);
        }
        Threads.postOnMainThreadDelayed(new Runnable() {
            @Override
            public void run() {
                HSGlobalNotificationCenter.sendNotification(SlidingDrawerContent.EVENT_REFRESH_BLUR_WALLPAPER);
            }
        }, 300);
    }

    private void configLockViewPager() {
        Context context = mRootView.getContext();
        mDimCover = mRootView.findViewById(R.id.dim_cover);

        mViewPager = new ViewPagerFixed(getContext());
        mViewPager.setClipChildren(false);

        mOnlineWallpaperPage = mRootView.findViewById(R.id.online_wallpaper_page_container);
        mOnlineWallpaperPage.setHeaderPage(mViewPager);
        mOnlineWallpaperPage.setDimBackground(mDimCover);
        mOnlineWallpaperPage.setup(0);
        mOnlineWallpaperPage.setPadding(0, Dimensions.getStatusBarHeight(getContext()), 0, 0);
        mOnlineWallpaperPage.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                isLockerPageShow = position == 0;
                if (isLockerPageShow) {
                    mLockerWallpaper.resumePlay();
                } else {
                    mLockerWallpaper.pausePlay();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mViewPager.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mLockerAdapter = new LockerAdapter(context, this, new LockerSlidingUpCallback(this));
        mViewPager.setAdapter(mLockerAdapter);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.requestFocus();
        mViewPager.setCurrentItem(LockerAdapter.PAGE_INDEX_MAINFRAME);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (LockerAdapter.PAGE_INDEX_UNLOCK == position) {
                    mDismissReason = "Slide";
                    dismiss(getContext(), true);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mWallpaperIcon = mRootView.findViewById(R.id.wallpaper_icon_entrance);
        mWallpaperIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnlineWallpaperPage.comeOrGo();
            }
        });
        mOnlineWallpaperPage.setTransitionTabIcon(mWallpaperIcon);
    }

    @Override
    public void dismiss(final Context context, final boolean dismissKeyguard) {
        if (!mIsSetup) {
            return;
        }
        mIsSetup = false;
        HSLog.i("LockManager", "L dismiss: " + mDismissReason + "  KG: " + dismissKeyguard + "  context: " + context);
        LockerCustomConfig.getLogger().logEvent("ColorPhone_LockScreen_Close",
                "Reason", mDismissReason,
                "Brand", Build.BRAND.toLowerCase(), "DeviceVersion", getDeviceInfo());

        mRootView.findViewById(R.id.bottom_layer).setVisibility(View.GONE);
        ObjectAnimator fadeOutAnim = ObjectAnimator.ofFloat(mLockerWallpaper, View.ALPHA, 0f);
        fadeOutAnim.setDuration(300);
        fadeOutAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLockerWallpaper.setImageResource(android.R.color.transparent);
                if (mLockerAdapter != null && mLockerAdapter.lockerMainFrame != null) {
                    mLockerAdapter.lockerMainFrame.clearDrawerBackground();
                }

                // Life cycle
                LockScreensLifeCycleRegistry.setLockerActive(false);

                if (getContext() instanceof BaseKeyguardActivity) {
                    final BaseKeyguardActivity activity = (BaseKeyguardActivity) getContext();
                    if (dismissKeyguard) {
                        activity.tryDismissKeyguard(true);
                    } else {
                        activity.finish();
                        activity.overridePendingTransition(0, 0);
                    }
                } else {
                    doDismiss();
                    Locker.super.dismiss(context, dismissKeyguard);
                }

                LockerCustomConfig.getLogger().logEvent("ColorPhone_LockScreen_Close",
                        "type", Commons.isKeyguardLocked(getContext(), false) ? "locked" : "unlocked");

                if (!Commons.isKeyguardLocked(context, false)) {
                    HSGlobalNotificationCenter.sendNotification(FloatWindowController.NOTIFY_KEY_LOCKER_DISMISS);
                }

            }
        });
        fadeOutAnim.start();
    }

    @Override
    public boolean isActivityHost() {
        return mActivityMode;
    }

    private void doDismiss() {
        onStop();
        onDestroy();
    }

    public void onDestroy() {
        // ======== onDestroy ========
        mHomeKeyWatcher.stopWatch();
        HSGlobalNotificationCenter.removeObserver(this);
        mIsDestroyed = true;
    }

    public void onPause() {
        // ======== onPause ========
        if (mLockerAdapter.lockerMainFrame != null) {
            mLockerAdapter.lockerMainFrame.onPause();
        }
        mLockerWallpaper.onPause();
    }

    public void onStop() {
        // ======== onStop ========
        if (mLockerAdapter.lockerMainFrame != null) {
            mLockerAdapter.lockerMainFrame.onStop();
        }
    }

    public void onBackPressed() {
        if (mLockerAdapter != null && mLockerAdapter.lockerMainFrame != null) {
            mLockerAdapter.lockerMainFrame.onBackPressed();
        }
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        switch (s) {
            case LockerMainFrame.EVENT_RINGTONE_CLICK_MUTE:
                mLockerWallpaper.mute(true);
                break;
            case LockerMainFrame.EVENT_RINGTONE_CLICK_MUTE_OFF:
                mLockerWallpaper.mute(false);
                break;
            case EVENT_WALLPAPER_CHANGE:
                initLockerWallpaper();
                break;
            case EVENT_FINISH_SELF:
                boolean shouldDismissKeyguard = true;

                if (hsBundle != null) {
                    shouldDismissKeyguard = hsBundle.getBoolean(EXTRA_SHOULD_DISMISS_KEYGUARD, true);
                    String reason = hsBundle.getString(Locker.EXTRA_DISMISS_REASON, "");
                    if (!TextUtils.isEmpty(reason)) {
                        mDismissReason = reason;
                    }
                }
                dismiss(getContext(), shouldDismissKeyguard);
                break;
            default:
                break;
        }
    }

    public LockerWallpaperView getIvLockerWallpaper() {
        return mLockerWallpaper;
    }

    public boolean isDestroyed() {
        return mIsDestroyed;
    }

    public void setActivityMode(boolean activityMode) {
        mActivityMode = activityMode;
    }
}
