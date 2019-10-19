package com.acb.libwallpaper.live.livewallpaper.guide;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.acb.libwallpaper.live.LauncherAnalytics;
 import com.honeycomb.colorphone.R;
import com.acb.libwallpaper.live.dialog.FloatWindowManager;
import com.acb.libwallpaper.live.dialog.FullScreenDialog;
import com.acb.libwallpaper.live.dialog.SafeWindowManager;
import com.acb.libwallpaper.live.livewallpaper.LiveWallpaperConsts;
import com.ihs.commons.utils.HSPreferenceHelper;

/**
 * Created by sundxing on 2018/5/25.
 */

public class WallPaper3dGuide extends FullScreenDialog {

    private static final String PREF_SUFFIX = "_rotate_guide";
    private RotationView rotationView;
    private RotationMaker mRotationMaker;

    private static WallPaper3dGuide sGuide;
    public static void show(Context context, RotationMaker rotationMaker) {
        boolean hasShow = HSPreferenceHelper.getDefault().getBoolean(
                LiveWallpaperConsts.PREF_KEY_TYPE_3D + PREF_SUFFIX, false);
        if (hasShow) {
            return;
        }
        hide();
        sGuide = new WallPaper3dGuide(context);
        sGuide.setRotationMaker(rotationMaker);
        FloatWindowManager.getInstance().showDialog(sGuide);
    }

    public static void hide() {
        if (sGuide != null) {
            sGuide.dismiss();
            sGuide = null;
        }
    }

    public static void hideOnApplySuccess() {
        hide();
        HSPreferenceHelper.getDefault().putBoolean(LiveWallpaperConsts.PREF_KEY_TYPE_3D + PREF_SUFFIX, true);
    }

    public WallPaper3dGuide(Context context) {
        super(context);
    }

    @Override
    public void onAddedToWindow(SafeWindowManager windowManager) {
        startGuideIfNeeded();
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
    protected void onDismiss() {
        if (rotationView != null) {
            rotationView.endRotate();
        }
        super.onDismiss();
        sGuide = null;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        sGuide = null;
    }

    @Override
    protected boolean IsInitStatusBarPadding() {
        return false;
    }

    private void startGuideIfNeeded() {
        LauncherAnalytics.logEvent("Wallpaper_3D_Gesture_Preview_Shown");
        if (rotationView == null) {
            rotationView = new RotationView(
                    findViewById(R.id.rotate_cover_above),
                    findViewById(R.id.rotate_cover_back),
                    mRotationMaker);
            mRotationMaker.addCallback(new RotationMaker.Callback() {
                @Override
                public void onRotateX(float angle) {

                }

                @Override
                public void onRotateY(float angle) {

                }

                @Override
                public void onRotateEnd() {
                    dismiss();
                    WallpaperSetGuide.show(getContext(), GuideHelper.TYPE_3D);
                }
            });
        }
        rotationView.startRotate();
    }

    public void setRotationMaker(RotationMaker rotationMaker) {
        mRotationMaker = rotationMaker;
    }

    @Override
    public boolean onBackPressed() {
        dismiss();
        return super.onBackPressed();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.wallpaper_guide_3d;
    }
}
