package com.colorphone.lock.lockscreen.locker.slidingdrawer.wallpaper;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.TouchDelegate;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.colorphone.lock.LockerCustomConfig;
import com.colorphone.lock.lockscreen.locker.Locker;
import com.colorphone.lock.lockscreen.locker.LockerMainFrame;
import com.colorphone.lock.lockscreen.locker.slidingdrawer.SlidingDrawerContent;
import com.honeycomb.colorphone.R;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.superapps.util.Dimensions;
import com.superapps.util.Networks;
import com.superapps.util.Toasts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static android.view.animation.Animation.INFINITE;
import static android.view.animation.Animation.RESTART;
import static com.colorphone.lock.lockscreen.chargingscreen.ChargingScreenSettings.LOCKER_PREFS;


public class WallpaperContainer extends LinearLayout implements View.OnClickListener, INotificationObserver {

    private Bitmap bluredBitmap;

    private static class CycleList {
        private int mCurrentIndex = 0;

        // must not be null or empty
        private ArrayList<HashMap> mData = new ArrayList();

        CycleList(@NonNull ArrayList<HashMap<String, String>> list) {
            mData.addAll(list);
        }

        public HashMap<String, String> getNext() {
            mCurrentIndex %= mData.size();
            mCurrentIndex++;
            return mData.get(mCurrentIndex - 1);
        }
    }

    private static final String TAG = "WallpaperContainer";

    private static final String PREF_KEY_WALLPAPER_FIRST_SHOWN = "wallpaper_first_shown";
    private static final String PREF_KEY_WALLPAPER_FIRST_VIEW_LAST_SUCCEED_THUMB = "wallpaper_first_view_thumb_url";
    private static final String PREF_KEY_WALLPAPER_SECOND_VIEW_LAST_SUCCEED_THUMB = "wallpaper_second_view_thumb_url";
    private static final String PREF_KEY_WALLPAPER_THIRD_VIEW_LAST_SUCCEED_THUMB = "wallpaper_third_view_thumb_url";
    private static final String PREF_KEY_WALLPAPER_FOURTH_VIEW_LAST_SUCCEED_THUMB = "wallpaper_fourth_view_thumb_url";

    private static final String PREF_KEY_WALLPAPER_FIRST_VIEW_LAST_SUCCEED_HD = "wallpaper_first_view_hd_url";
    private static final String PREF_KEY_WALLPAPER_SECOND_VIEW_LAST_SUCCEED_HD = "wallpaper_second_view_hd_url";
    private static final String PREF_KEY_WALLPAPER_THIRD_VIEW_LAST_SUCCEED_HD = "wallpaper_third_view_hd_url";
    private static final String PREF_KEY_WALLPAPER_FOURTH_VIEW_LAST_SUCCEED_HD = "wallpaper_fourth_view_hd_url";

    private static final String PREFIX_RESOURCE_DRAWABLE = "android.resource://";
    private static final String FOREWARD_SLASH = "/";

    private static final String WALLPAPER_THUMB = "thumb";
    private static final String WALLPAPER_HD = "HD";
    private static final int MASK_HINT_COLOR_ALPHA = 0x4a;
    private static final int WALLPAPER_COUNT = 4;

    private Locker mLocker;

    private ArrayList<ImageView> mIVImgs = new ArrayList<>(WALLPAPER_COUNT);
    private ArrayList<ImageView> mRefreshImgs = new ArrayList<>(WALLPAPER_COUNT);
    //private ImageView mRefreshView;
    private TextView mRefreshView;
    private RotateAnimation mRefreshRotation;
    private RotateAnimation mChangeWallpaper;
    private RotateAnimation mWallpaperRotation;

    private int mCurrentIndex = -1;
    private boolean mIsDownloadingWallpaper;
    private boolean mIsRefreshSwitchClicked;
    private boolean mFirstAutoRefresh;

    private CycleList mSourceWallpapers;
    private Random mRandomDelay = new Random();
    private ArrayList<Uri> mThumbUrls = new ArrayList<>(WALLPAPER_COUNT);
    private ArrayList<Uri> mHDUrls = new ArrayList<>(WALLPAPER_COUNT);
    private SparseBooleanArray mLoadingFinish = new SparseBooleanArray();
    private SparseBooleanArray mLoadingSucceed = new SparseBooleanArray();
    private HSPreferenceHelper mPrefer = HSPreferenceHelper.create(HSApplication.getContext(), LOCKER_PREFS);

    BroadcastReceiver mNetworkReceiver;

    public WallpaperContainer(Context context) {
        super(context);
    }

    public WallpaperContainer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public WallpaperContainer(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        initView();
        initFlags();
        fetchConfig();
    }

    public void setLocker(Locker locker) {
        mLocker = locker;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mFirstAutoRefresh = true;
        if (!mPrefer.getBoolean(PREF_KEY_WALLPAPER_FIRST_SHOWN, false)) {
            refreshWallpapers();
        } else {
            prepareDownloading();
        }
        HSGlobalNotificationCenter.addObserver(LockerMainFrame.EVENT_SLIDING_DRAWER_OPENED, this);
        IntentFilter wifiFilter = new IntentFilter();
        wifiFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        wifiFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mNetworkReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Networks.isNetworkAvailable(-1)) {
                    mFirstAutoRefresh = false;
                    prepareDownloading();
                }
            }
        };
        getContext().registerReceiver(mNetworkReceiver, wifiFilter);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        try {
            getContext().unregisterReceiver(mNetworkReceiver);
        } catch (Exception ignore){}

        HSGlobalNotificationCenter.removeObserver(this);
    }

    @Override
    public void onClick(View v) {
        if (mIsDownloadingWallpaper || !isLoadingFinish()) {
            return;
        }

        mIsRefreshSwitchClicked = false;
        int i = v.getId();
        if (i == R.id.iv_refresh) {
            mFirstAutoRefresh = false;
            mIsRefreshSwitchClicked = true;
            refreshWallpapers();

        } else if (i == R.id.iv_img1) {
            if (mRefreshImgs.get(0).getVisibility() == GONE) {
                if (mCurrentIndex != 0) {
                    setLockerWallpaper(0);
                }
            }


            refreshWallpaper(0);

        } else if (i == R.id.iv_img1_refresh) {
            refreshWallpaper(0);

        } else if (i == R.id.iv_img2) {
            if (mRefreshImgs.get(1).getVisibility() == GONE) {
                if (mCurrentIndex != 1) {
                    setLockerWallpaper(1);
                }
            }

            refreshWallpaper(1);

        } else if (i == R.id.iv_img2_refresh) {
            refreshWallpaper(1);

        } else if (i == R.id.iv_img3) {
            if (mRefreshImgs.get(2).getVisibility() == GONE) {
                if (mCurrentIndex != 2) {
                    setLockerWallpaper(2);
                }
            }

            refreshWallpaper(2);

        } else if (i == R.id.iv_img3_refresh) {
            refreshWallpaper(2);

        } else if (i == R.id.iv_img4) {
            if (mRefreshImgs.get(3).getVisibility() == GONE) {
                if (mCurrentIndex != 3) {
                    setLockerWallpaper(3);
                }
            }

            refreshWallpaper(3);

        } else if (i == R.id.iv_img4_refresh) {
            refreshWallpaper(3);

        } else {
            HSLog.e(TAG, "wrong view");

        }

    }

    public static void expandViewTouchDelegate(final View view, final int top,
                                               final int bottom, final int left, final int right) {

        ((View) view.getParent()).post(new Runnable() {
            @Override
            public void run() {
                Rect bounds = new Rect();
                view.setEnabled(true);
                view.getHitRect(bounds);

                bounds.top -= top;
                bounds.bottom += bottom;
                bounds.left -= left;
                bounds.right += right;

                TouchDelegate touchDelegate = new TouchDelegate(bounds, view);

                if (View.class.isInstance(view.getParent())) {
                    ((View) view.getParent()).setTouchDelegate(touchDelegate);
                }
            }
        });
    }

    private void initView() {
        mRefreshView = findViewById(R.id.iv_refresh);
        mRefreshView.setOnClickListener(this);
        expandViewTouchDelegate(mRefreshView, Dimensions.pxFromDp(2), Dimensions.pxFromDp(2), Dimensions.pxFromDp(2), Dimensions.pxFromDp(2));

        mRefreshImgs.add((ImageView) findViewById(R.id.iv_img1_refresh));
        mRefreshImgs.add((ImageView) findViewById(R.id.iv_img2_refresh));
        mRefreshImgs.add((ImageView) findViewById(R.id.iv_img3_refresh));
        mRefreshImgs.add((ImageView) findViewById(R.id.iv_img4_refresh));
        for (ImageView view : mRefreshImgs) {
            view.setOnClickListener(this);
        }

        mIVImgs.add((ImageView) findViewById(R.id.iv_img1));
        mIVImgs.add((ImageView) findViewById(R.id.iv_img2));
        mIVImgs.add((ImageView) findViewById(R.id.iv_img3));
        mIVImgs.add((ImageView) findViewById(R.id.iv_img4));
        for (int index = 0; index < mIVImgs.size(); index++) {
            View view = mIVImgs.get(index);
            view.setOnClickListener(this);
            view.setTag(mRefreshImgs.get(index));
        }

        mRefreshRotation = new RotateAnimation(0.0f, -359.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRefreshRotation.setFillAfter(true);
        mRefreshRotation.setInterpolator(new LinearInterpolator());
        mRefreshRotation.setRepeatCount(INFINITE);
        mRefreshRotation.setRepeatMode(RESTART);
        mRefreshRotation.setDuration(600);


        mChangeWallpaper = new RotateAnimation(0.0f, 359.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mChangeWallpaper.setFillAfter(true);
        mChangeWallpaper.setInterpolator(new LinearInterpolator());
        mChangeWallpaper.setRepeatCount(INFINITE);
        mChangeWallpaper.setRepeatMode(RESTART);
        mChangeWallpaper.setDuration(600);

        mWallpaperRotation = new RotateAnimation(0.0f, 359.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mWallpaperRotation.setFillAfter(true);
        mWallpaperRotation.setInterpolator(new LinearInterpolator());
        mWallpaperRotation.setRepeatCount(INFINITE);
        mWallpaperRotation.setRepeatMode(RESTART);
        mWallpaperRotation.setDuration(1000);
    }

    private void initFlags() {

        for (View view : mIVImgs) {
            mLoadingFinish.put(view.getId(), true);
            mLoadingSucceed.put(view.getId(), false);
        }
        mThumbUrls.add(Uri.parse(mPrefer.getString(PREF_KEY_WALLPAPER_FIRST_VIEW_LAST_SUCCEED_THUMB, "")));
        mHDUrls.add(Uri.parse(mPrefer.getString(PREF_KEY_WALLPAPER_FIRST_VIEW_LAST_SUCCEED_HD, "")));

        mThumbUrls.add(Uri.parse(mPrefer.getString(PREF_KEY_WALLPAPER_SECOND_VIEW_LAST_SUCCEED_THUMB, "")));
        mHDUrls.add(Uri.parse(mPrefer.getString(PREF_KEY_WALLPAPER_SECOND_VIEW_LAST_SUCCEED_HD, "")));

        mThumbUrls.add(Uri.parse(mPrefer.getString(PREF_KEY_WALLPAPER_THIRD_VIEW_LAST_SUCCEED_THUMB, "")));
        mHDUrls.add(Uri.parse(mPrefer.getString(PREF_KEY_WALLPAPER_THIRD_VIEW_LAST_SUCCEED_HD, "")));

        mThumbUrls.add(Uri.parse(mPrefer.getString(PREF_KEY_WALLPAPER_FOURTH_VIEW_LAST_SUCCEED_THUMB, "")));
        mHDUrls.add(Uri.parse(mPrefer.getString(PREF_KEY_WALLPAPER_FOURTH_VIEW_LAST_SUCCEED_HD, "")));
    }

    private void fetchConfig() {
        if (mSourceWallpapers == null) {
            ArrayList data = (ArrayList) HSConfig.getList("Application", "Locker", "Wallpapers");
            if (data != null && !data.isEmpty()) {
                mSourceWallpapers = new CycleList(data);
            }
        }
    }

    private void setLockerWallpaper(final int index) {
        mIsDownloadingWallpaper = true;
        mCurrentIndex = index;
        ValueAnimator mask = maskAnimation(mIVImgs.get(index), 0, MASK_HINT_COLOR_ALPHA, 400);
        if (mask == null) {
            return;
        }

        mask.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mLocker.isDestroyed()) {
                    return;
                }
                Glide.with(getContext()).asBitmap().load(mHDUrls.get(index))
                        .into(new SimpleTarget<Bitmap>() {

                            @Override
                            public void onLoadStarted(Drawable placeholder) {
                                super.onLoadStarted(placeholder);
                                ((ImageView) mIVImgs.get(index).getTag()).startAnimation(mChangeWallpaper);
                                ((ImageView) mIVImgs.get(index).getTag()).setVisibility(VISIBLE);
                            }

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                super.onLoadFailed(errorDrawable);
                                ((ImageView) mIVImgs.get(index).getTag()).clearAnimation();
                                ((ImageView) mIVImgs.get(index).getTag()).setVisibility(GONE);
                                ValueAnimator mask = maskAnimation(mIVImgs.get(index), MASK_HINT_COLOR_ALPHA, 0x00, 400);
                                if (mask != null) {
                                    mask.start();
                                }
                                mIsDownloadingWallpaper = false;
                                Toasts.showToast(R.string.wallpaper_network_error);
                                LockerCustomConfig.getLogger().logEvent("Locker_Wallpaper_Preview_Clicked", "name", mHDUrls.get(index).toString(), "result", "fail");
                            }

                            @Override
                            public void onResourceReady(final Bitmap resource, Transition<? super Bitmap> transition) {
                                final ImageView wallpaperView = null;
                                SlidingDrawerContent silde = (SlidingDrawerContent) getParent().getParent();
                                if (wallpaperView == null || silde == null) {
                                    mIsDownloadingWallpaper = false;
                                    return;
                                }
                                silde.setDrawerBg(resource);
                                ObjectAnimator wallpaperOut = ObjectAnimator.ofFloat(wallpaperView, "alpha", 1f, 0.5f);
                                wallpaperOut.setDuration(400);
                                wallpaperOut.addListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        wallpaperView.setImageBitmap(resource);
                                    }
                                });

                                ObjectAnimator wallpaperIn = ObjectAnimator.ofFloat(wallpaperView, "alpha", 0.5f, 1f);
                                wallpaperIn.setDuration(400);
                                wallpaperIn.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                    @Override
                                    public void onAnimationUpdate(ValueAnimator animation) {
                                        int alpha = (int) ((1f - animation.getAnimatedFraction()) * MASK_HINT_COLOR_ALPHA);
                                        mIVImgs.get(index).setColorFilter(Color.argb(alpha, 0, 0, 0));
                                    }
                                });
                                wallpaperIn.addListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        ((ImageView) mIVImgs.get(index).getTag()).clearAnimation();
                                        ((ImageView) mIVImgs.get(index).getTag()).setVisibility(GONE);
                                        mIsDownloadingWallpaper = false;
                                    }
                                });

                                AnimatorSet change = new AnimatorSet();
                                change.playSequentially(wallpaperOut, wallpaperIn);
                                change.start();
                                mPrefer.putString(Locker.PREF_KEY_CURRENT_WALLPAPER_HD_URL, mHDUrls.get(index).toString());
                                LockerCustomConfig.getLogger().logEvent("Locker_Wallpaper_Preview_Clicked", "name", mHDUrls.get(index).toString(), "result", "success");
                            }

                            @Override
                            public void onLoadCleared(Drawable placeholder) {
                                super.onLoadCleared(placeholder);
                                ((ImageView) mIVImgs.get(index).getTag()).clearAnimation();
                                ((ImageView) mIVImgs.get(index).getTag()).setVisibility(GONE);
                                mIsDownloadingWallpaper = false;
                            }
                        });
            }
        });
        mask.start();
    }

    private void prepareDownloading() {
        for (int index = 0; index < mIVImgs.size(); index++) {
            if (mLoadingSucceed.get(mIVImgs.get(index).getId())) {
                continue;
            }
            final int rank = index;
            mIVImgs.get(index).animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(400)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            refreshWallpaper(rank);
                        }
                    })
                    .setStartDelay(mRandomDelay.nextInt(100))
                    .start();
        }
    }

    private void refreshWallpapers() {
        HSLog.d(TAG, "refresh wallpapers");
        fetchConfig();
        resetTags();
        refreshUrls();
        prepareDownloading();
    }

    private void refreshWallpaper(int index) {
        Context context = getContext();
        if (mLocker == null || mLocker.isDestroyed()) {
            return;
        }
        mLoadingFinish.put(mIVImgs.get(index).getId(), false);
        HSLog.d(TAG, "wallpaper index = " + index + "   thumb url = " + mThumbUrls.get(index));
        final ImageView targetIv = mIVImgs.get(index);
        Glide.with(context).asBitmap().load(mThumbUrls.get(index))
                .into(new SimpleTarget<Bitmap>() {

                    @Override
                    public void onLoadStarted(Drawable placeholder) {
                        super.onLoadStarted(placeholder);
                        if (!mIsRefreshSwitchClicked || ((View) targetIv.getTag()).getVisibility() == VISIBLE) {
                            ((View) targetIv.getTag()).startAnimation(mWallpaperRotation);
                        }
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        targetIv.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(400)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        ((View) targetIv.getTag()).clearAnimation();
                                        mLoadingFinish.put(targetIv.getId(), true);
                                        mLoadingSucceed.put(targetIv.getId(), false);
                                        if (isLoadingFinish()) {
                                            //mRefreshView.clearAnimation();
                                            if (!mFirstAutoRefresh) {
                                                Toasts.showToast(R.string.wallpaper_network_error, Toast.LENGTH_LONG);
                                            }

                                            //log flurry
                                            if (mIsRefreshSwitchClicked) {
                                                int succeed = 0;
                                                for (int index = 0; index < mLoadingSucceed.size(); index++) {
                                                    succeed += mLoadingSucceed.get(mLoadingSucceed.keyAt(index)) ? 1 : 0;
                                                }
                                                LockerCustomConfig.getLogger().logEvent("Locker_Wallpaper_Refresh_Clicked", "success", "" + succeed);
                                                mIsRefreshSwitchClicked = false;
                                            }
                                        }
                                    }
                                })
                                .start();
                    }

                    @Override
                    public void onResourceReady(final Bitmap resource, Transition<? super Bitmap> transition) {
                        // Fade in & fade out
                        ObjectAnimator wallpaperOut = ObjectAnimator.ofFloat(targetIv, "alpha", 1f, 0.5f);
                        wallpaperOut.setDuration(400);
                        wallpaperOut.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                int alpha = (int) (animation.getAnimatedFraction() * MASK_HINT_COLOR_ALPHA);
                                targetIv.setColorFilter(Color.argb(alpha, 0, 0, 0));
                            }
                        });
                        wallpaperOut.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                targetIv.setImageBitmap(resource);
                            }
                        });

                        ObjectAnimator wallpaperIn = ObjectAnimator.ofFloat(targetIv, "alpha", 0.5f, 1f);
                        wallpaperIn.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                int alpha = (int) ((1f - animation.getAnimatedFraction()) * MASK_HINT_COLOR_ALPHA);
                                targetIv.setColorFilter(Color.argb(alpha, 0, 0, 0));
                            }
                        });
                        wallpaperIn.setDuration(400);

                        AnimatorSet change = new AnimatorSet();
                        change.playSequentially(wallpaperOut, wallpaperIn);
                        change.addListener(new android.animation.AnimatorListenerAdapter() {

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                // Zoom in thumbnail
                                targetIv.animate()
                                        .scaleX(1f)
                                        .scaleY(1f)
                                        .setDuration(400)
                                        .setListener(new AnimatorListenerAdapter() {
                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                ((View) targetIv.getTag()).clearAnimation();
                                                mLoadingFinish.put(targetIv.getId(), true);
                                                mLoadingSucceed.put(targetIv.getId(), true);
                                                ((ImageView) targetIv.getTag()).setVisibility(GONE);
                                                if (isLoadingFinish()) {
                                                    //mRefreshView.clearAnimation();

                                                    //log flurry
                                                    if (mIsRefreshSwitchClicked) {
                                                        int succeed = 0;
                                                        for (int index = 0; index < mLoadingSucceed.size(); index++) {
                                                            succeed += mLoadingSucceed.get(mLoadingSucceed.keyAt(index)) ? 1 : 0;
                                                        }
                                                        LockerCustomConfig.getLogger().logEvent("Locker_Wallpaper_Refresh_Clicked", "success", "" + succeed);
                                                        mIsRefreshSwitchClicked = false;
                                                    }
                                                }
                                            }
                                        })
                                        .start();
                            }
                        });
                        change.start();
                    }

                    @Override
                    public void onLoadCleared(Drawable placeholder) {
                        super.onLoadCleared(placeholder);
                        ((View) targetIv.getTag()).clearAnimation();
                        mLoadingFinish.put(targetIv.getId(), true);
                        mLoadingSucceed.put(targetIv.getId(), false);
                    }
                });
    }

    private void resetTags() {
        if (mRefreshView != null) {
            mCurrentIndex = -1;
            //mRefreshView.startAnimation(mRefreshRotation);
            for (int i = 0; i < mLoadingFinish.size(); i++) {
                mLoadingFinish.put(mLoadingFinish.keyAt(i), false);
                mLoadingSucceed.put(mLoadingSucceed.keyAt(i), false);
            }
        }
    }

    private void refreshUrls() {
        if (!mPrefer.getBoolean(PREF_KEY_WALLPAPER_FIRST_SHOWN, false)) {
            mPrefer.putBoolean(PREF_KEY_WALLPAPER_FIRST_SHOWN, true);
            mThumbUrls.set(0, Uri.parse(PREFIX_RESOURCE_DRAWABLE + getContext().getPackageName() + FOREWARD_SLASH + R.drawable.wallpaper_locker_thumb));
            mHDUrls.set(0, Uri.parse(PREFIX_RESOURCE_DRAWABLE + getContext().getPackageName() + FOREWARD_SLASH + R.drawable.wallpaper_locker));
            generateUrls(3);
        } else {
            generateUrls(4);
        }

        mPrefer.putString(PREF_KEY_WALLPAPER_FIRST_VIEW_LAST_SUCCEED_THUMB, mThumbUrls.get(0).toString());
        mPrefer.putString(PREF_KEY_WALLPAPER_FIRST_VIEW_LAST_SUCCEED_HD, mHDUrls.get(0).toString());
        mPrefer.putString(PREF_KEY_WALLPAPER_SECOND_VIEW_LAST_SUCCEED_THUMB, mThumbUrls.get(1).toString());
        mPrefer.putString(PREF_KEY_WALLPAPER_SECOND_VIEW_LAST_SUCCEED_HD, mHDUrls.get(1).toString());
        mPrefer.putString(PREF_KEY_WALLPAPER_THIRD_VIEW_LAST_SUCCEED_THUMB, mThumbUrls.get(2).toString());
        mPrefer.putString(PREF_KEY_WALLPAPER_THIRD_VIEW_LAST_SUCCEED_HD, mHDUrls.get(2).toString());
        mPrefer.putString(PREF_KEY_WALLPAPER_FOURTH_VIEW_LAST_SUCCEED_THUMB, mThumbUrls.get(3).toString());
        mPrefer.putString(PREF_KEY_WALLPAPER_FOURTH_VIEW_LAST_SUCCEED_HD, mHDUrls.get(3).toString());
    }

    private void generateUrls(int count) {
        if (mSourceWallpapers != null) {
            for (int index = WALLPAPER_COUNT - count; index < WALLPAPER_COUNT; index++) {
                HashMap<String, String> url = mSourceWallpapers.getNext();
                mThumbUrls.set(index, Uri.parse(url.get(WALLPAPER_THUMB)));
                mHDUrls.set(index, Uri.parse(url.get(WALLPAPER_HD)));
            }
        }
    }

    private boolean isLoadingFinish() {
        boolean result = true;
        for (int i = 0; i < mLoadingFinish.size(); i++) {
            int id = mLoadingFinish.keyAt(i);
            if (id == R.id.iv_img1_refresh) {
                if (!mHDUrls.get(0).toString().startsWith(PREFIX_RESOURCE_DRAWABLE)) {
                    result = result && mLoadingFinish.get(id);
                }
            } else {
                result = result && mLoadingFinish.get(id);
            }
        }
        return result;
    }

    private ValueAnimator maskAnimation(final ImageView view, int startAlpha, int endAlpha, int duration) {
        if (view == null) {
            return null;
        }
        ValueAnimator color = ValueAnimator.ofInt(startAlpha, endAlpha);
        color.setInterpolator(new AccelerateDecelerateInterpolator());
        color.setDuration(duration);
        color.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                view.setColorFilter(Color.argb((Integer) animation.getAnimatedValue(), 0, 0, 0));
            }
        });
        return color;
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        switch (s) {
            case LockerMainFrame.EVENT_SLIDING_DRAWER_OPENED:
                mIsRefreshSwitchClicked = false;
                mFirstAutoRefresh = false;
                if (!mPrefer.getBoolean(PREF_KEY_WALLPAPER_FIRST_SHOWN, false)) {
                    refreshWallpapers();
                } else {
                    prepareDownloading();
                }
                break;
            default:
                break;
        }
    }
}
