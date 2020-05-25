package com.colorphone.lock.lockscreen.locker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.colorphone.lock.BuildConfig;
import com.colorphone.lock.LockerCustomConfig;
import com.colorphone.lock.R;
import com.colorphone.lock.ScreenStatusReceiver;
import com.colorphone.lock.lockscreen.LockScreen;
import com.colorphone.lock.lockscreen.LockScreensLifeCycleRegistry;
import com.colorphone.lock.lockscreen.chargingscreen.ChargingScreenUtils;
import com.colorphone.lock.lockscreen.locker.slidingdrawer.SlidingDrawerContent;
import com.colorphone.lock.lockscreen.locker.slidingup.LockerSlidingUpCallback;
import com.colorphone.lock.lockscreen.locker.statusbar.StatusBar;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Commons;
import com.superapps.util.HomeKeyWatcher;
import com.superapps.util.Preferences;

import java.util.Calendar;

import static com.colorphone.lock.lockscreen.chargingscreen.ChargingScreenSettings.LOCKER_PREFS;

public class Locker extends LockScreen implements INotificationObserver {

    private static final String TAG = "LOCKER_ACTIVITY";

    public static final String EVENT_FINISH_SELF = "locker_event_finish_self";
    public static final String EXTRA_SHOULD_DISMISS_KEYGUARD = "extra_should_dismiss_keyguard";
    public static final String EXTRA_DISMISS_REASON = "dismiss_reason";
    public static final String PREF_KEY_CURRENT_WALLPAPER_HD_URL = "current_hd_wallpaper_url";

    ViewPagerFixed mViewPager;
    private LockerAdapter mLockerAdapter;
    private ImageView mLockerWallpaper;

    private boolean mIsDestroyed;

    private HomeKeyWatcher mHomeKeyWatcher;
    private boolean mHomeKeyClicked;
    private boolean mIsSetup;
    private String mDismissReason = "Unkown";

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

        mLockerWallpaper = (ImageView) mRootView.findViewById(R.id.locker_wallpaper_view);

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
        if (!isActivityHost()) {
            HSGlobalNotificationCenter.addObserver(ScreenStatusReceiver.NOTIFICATION_SCREEN_ON, this);
            HSGlobalNotificationCenter.addObserver(ScreenStatusReceiver.NOTIFICATION_SCREEN_OFF, this);
        }
        LockerSettings.increaseLockerShowCount();

        // Life cycle
        LockScreensLifeCycleRegistry.setLockerActive(true);

//        HSGlobalNotificationCenter.sendNotification(NotificationCondition.EVENT_LOCK);

        if (!isActivityHost()) {
            onStart();
        }
    }

    public static String getDeviceInfo() {
        return String.valueOf(Build.VERSION.SDK_INT);
    }

    public void onStart() {
        // ======== onStart ========
        if (mLockerAdapter.lockerMainFrame != null) {
            mLockerAdapter.lockerMainFrame.onStart();
        }

        // ======== onResume ========
        if (mHomeKeyClicked && mLockerAdapter != null && mLockerAdapter.lockerMainFrame != null) {
            mHomeKeyClicked = false;
            mLockerAdapter.lockerMainFrame.closeDrawer();
        }
    }

    private void initLockerWallpaper() {
        String wallpaperUrl = Preferences.get(LOCKER_PREFS).getString(PREF_KEY_CURRENT_WALLPAPER_HD_URL, "");
        if (!TextUtils.isEmpty(wallpaperUrl)) {
            Glide.with(mRootView.getContext()).asBitmap().load(wallpaperUrl)
                    .into(new SimpleTarget<Bitmap>() {

                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            mLockerWallpaper.setImageBitmap(resource);
                            HSGlobalNotificationCenter.sendNotification(SlidingDrawerContent.EVENT_REFRESH_BLUR_WALLPAPER);
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            mLockerWallpaper.setImageResource(R.drawable.wallpaper_locker);
                            HSGlobalNotificationCenter.sendNotification(SlidingDrawerContent.EVENT_REFRESH_BLUR_WALLPAPER);
                        }
                    });
        } else {
            mLockerWallpaper.setImageResource(R.drawable.wallpaper_locker);
        }
    }

    private void configLockViewPager() {
        Context context = mRootView.getContext();
        mViewPager = (ViewPagerFixed) mRootView.findViewById(R.id.locker_pager);
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
                "Brand", Build.BRAND.toLowerCase(),
                "DeviceVersion", getDeviceInfo(),
                "Time", String.valueOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)));

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

                Locker.super.dismiss(context, dismissKeyguard);

                LockerCustomConfig.getLogger().logEvent("ColorPhone_Screen_Close",
                        "type", Commons.isKeyguardLocked(getContext(), false) ? "locked" : "unlocked",
                        "Time", String.valueOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)));
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
        if (mIsDestroyed) {
            return;
        }
        super.onDestroy();
        mHomeKeyWatcher.stopWatch();
        HSGlobalNotificationCenter.removeObserver(this);
        mIsDestroyed = true;
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

    public ImageView getIvLockerWallpaper() {
        return mLockerWallpaper;
    }

    public boolean isDestroyed() {
        return mIsDestroyed;
    }
}
