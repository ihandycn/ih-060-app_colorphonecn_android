package com.colorphone.lock.lockscreen.locker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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
import com.superapps.util.HomeKeyWatcher;
import com.colorphone.lock.LockerCustomConfig;
import com.colorphone.lock.R;
import com.colorphone.lock.lockscreen.DismissKeyguradActivity;
import com.colorphone.lock.lockscreen.LockScreen;
import com.colorphone.lock.lockscreen.LockScreensLifeCycleRegistry;
import com.colorphone.lock.lockscreen.chargingscreen.ChargingScreenUtils;
import com.colorphone.lock.lockscreen.locker.slidingdrawer.SlidingDrawerContent;
import com.colorphone.lock.lockscreen.locker.slidingup.LockerSlidingUpCallback;
import com.colorphone.lock.lockscreen.locker.statusbar.StatusBar;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.superapps.util.Preferences;

import static com.colorphone.lock.lockscreen.chargingscreen.ChargingScreenSettings.LOCKER_PREFS;

public class Locker extends LockScreen implements INotificationObserver {

    private static final String TAG = "LOCKER_ACTIVITY";

    public static final String EVENT_FINISH_SELF = "locker_event_finish_self";
    public static final String EXTRA_SHOULD_DISMISS_KEYGUARD = "extra_should_dismiss_keyguard";
    public static final String PREF_KEY_CURRENT_WALLPAPER_HD_URL = "current_hd_wallpaper_url";

    ViewPager mViewPager;
    private LockerAdapter mLockerAdapter;
    private ImageView mLockerWallpaper;

    private boolean mIsDestroyed;

    private HomeKeyWatcher mHomeKeyWatcher;
    private boolean mHomeKeyClicked;
    private boolean mIsSetup;

    @Override
    public void setup(ViewGroup root, Bundle extra) {
        super.setup(root, extra);
        LockerCustomConfig.getLogger().logEvent("Locker_Shown");

        mIsSetup = true;
        // ======== onCreate ========
        mHomeKeyWatcher = new HomeKeyWatcher(root.getContext());
        mHomeKeyWatcher.setOnHomePressedListener(new HomeKeyWatcher.OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                mHomeKeyClicked = true;
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

        LockerSettings.increaseLockerShowCount();

        // Life cycle
        LockScreensLifeCycleRegistry.setLockerActive(true);
//        HSGlobalNotificationCenter.sendNotification(NotificationCondition.EVENT_LOCK);
    }

    public void onStart() {
        // ======== onStart ========
        if (mLockerAdapter.lockerMainFrame != null) {
            mLockerAdapter.lockerMainFrame.onStart();
        }
    }

    public void onResume() {
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
        mViewPager = (ViewPager) mRootView.findViewById(R.id.locker_pager);
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
//                HSGlobalNotificationCenter.sendNotification(NotificationCondition.EVENT_UNLOCK);


                if (getContext() instanceof Activity) {
                    final Activity activity = (Activity) getContext();
                    activity.finish();
                    activity.overridePendingTransition(0, 0);
                    if (dismissKeyguard) {
                        DismissKeyguradActivity.startSelfIfKeyguardSecure(activity);
                    }
                } else {
                    doDismiss();
                    Locker.super.dismiss(context, dismissKeyguard);
                }

            }
        });
        fadeOutAnim.start();
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

    @Override public void onReceive(String s, HSBundle hsBundle) {
        switch (s) {
            case EVENT_FINISH_SELF:
                boolean shouldDismissKeyguard = true;
                if (hsBundle != null) {
                    shouldDismissKeyguard = hsBundle.getBoolean(EXTRA_SHOULD_DISMISS_KEYGUARD, true);
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
