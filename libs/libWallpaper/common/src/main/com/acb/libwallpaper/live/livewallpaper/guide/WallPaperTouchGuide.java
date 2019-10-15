package com.acb.libwallpaper.live.livewallpaper.guide;

import android.animation.Animator;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.acb.libwallpaper.R;
import com.acb.libwallpaper.live.animation.AnimatorListenerAdapter;
import com.acb.libwallpaper.live.dialog.FloatWindowManager;
import com.acb.libwallpaper.live.dialog.FullScreenDialog;
import com.acb.libwallpaper.live.dialog.SafeWindowManager;
import com.acb.libwallpaper.live.livewallpaper.LiveWallpaperConsts;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;

/**
 * Created by sundxing on 2018/5/25.
 */

public class WallPaperTouchGuide extends FullScreenDialog {

    private static final String TAG = "WallPaperTouchGuide";
    private static final String PREF_SUFFIX = "_touch_guide";
    private PathAnimator pathAnimator;
    private boolean mCanceled;
    private long startTimeMills;

    private static WallPaperTouchGuide sGuide;

    public static void show(Context context) {
        hide();
        sGuide = new WallPaperTouchGuide(context);
        FloatWindowManager.getInstance().showDialog(sGuide);
    }

    public static void hide() {
        if (sGuide != null) {
            sGuide.dismiss();
        }
    }


    /**
     * Cancel only after display time reached min-time.
     */
    public static void cancel() {
        if (sGuide != null) {
            sGuide.markCanceled();
        }
    }

    private void markCanceled() {
        mCanceled = true;
    }

    public static final String EVENT_POINT = "com.huandong.wallpaper.live.WallPaperTouchGuide.touch_event";

    public WallPaperTouchGuide(Context context) {
        super(context);
    }

    @Override
    public void onAddedToWindow(SafeWindowManager windowManager) {
        setVisibility(INVISIBLE);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                setVisibility(VISIBLE);
                startGuideIfNeeded();
            }
        }, GuideHelper.TOUCH_GUIDE_DELAY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    @Override
    protected String getGroupTag() {
        return LiveWallpaperConsts.GUIDE_WINDOW_TAG;
    }

    @Override
    public WindowManager.LayoutParams getLayoutParams() {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.type = WindowManager.LayoutParams.TYPE_PHONE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            lp.type = WindowManager.LayoutParams.TYPE_TOAST;
        }
        lp.format = PixelFormat.RGBA_8888;
        lp.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
        lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
//        lp.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

        return lp;
    }

    @Override
    protected boolean IsInitStatusBarPadding() {
        return false;
    }

    private void startGuideIfNeeded() {
        final View view = findViewById(R.id.finger);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        pathAnimator = new PathAnimator();
        pathAnimator.setDuration(1200);
        pathAnimator.setTargetSizePixel(dm.widthPixels, dm.heightPixels);
        pathAnimator.setCallback(new PathAnimator.Callback() {
            @Override
            public void onPoint(float x, float y) {
                view.setTranslationX(x);
                view.setTranslationY(y);

                HSBundle hsBundle = new HSBundle();
                hsBundle.putFloat("x", x);
                hsBundle.putFloat("y", y);
                HSGlobalNotificationCenter.sendNotification(EVENT_POINT, hsBundle);
                if (mCanceled && timeLimitValid(SystemClock.uptimeMillis())) {
                    HSLog.d(TAG, "cancel");
                    pathAnimator.cancel();
                }
            }
        });

        pathAnimator.startAnim();

        pathAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                HSLog.d(TAG, "onAnimationEnd");
                dismiss();
                WallpaperSetGuide.show(getContext(), GuideHelper.TYPE_LIVE_TOUCH);
            }
        });
        startTimeMills = SystemClock.uptimeMillis();
    }

    private boolean timeLimitValid(long now) {
        return now - startTimeMills > 0;
    }

    @Override
    public boolean onBackPressed() {
        dismiss();
        return super.onBackPressed();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        sGuide = null;
    }

    @Override
    protected void onDismiss() {
        if (pathAnimator != null) {
            pathAnimator.cancel();
            pathAnimator = null;
        }
        HSGlobalNotificationCenter.sendNotification(EVENT_POINT);
        sGuide = null;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.wallpaper_guide_touch_me;
    }
}
