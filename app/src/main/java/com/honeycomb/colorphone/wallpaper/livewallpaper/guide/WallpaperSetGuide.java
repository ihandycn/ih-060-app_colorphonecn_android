package com.honeycomb.colorphone.wallpaper.livewallpaper.guide;

import android.animation.Animator;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.wallpaper.animation.AnimatorListenerAdapter;
import com.honeycomb.colorphone.wallpaper.dialog.FloatWindowDialog;
import com.honeycomb.colorphone.wallpaper.dialog.FloatWindowManager;
import com.honeycomb.colorphone.wallpaper.dialog.SafeWindowManager;
import com.honeycomb.colorphone.wallpaper.livewallpaper.LiveWallpaperConsts;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.superapps.util.Compats;
import com.superapps.util.Dimensions;

/**
 * Created by sundxing on 2018/5/26.
 */

public class WallpaperSetGuide extends FloatWindowDialog {

    private int mType;

    private static SparseArray<WallpaperSetGuide> sGuideMap = new SparseArray<>();

    public static void hide(int type) {
        if (sGuideMap.get(type) != null) {
            sGuideMap.get(type).dismiss();
        }
    }

    public static void hideOnApplySuccess(int type) {
        if (sGuideMap.size() > 0) {
            if (sGuideMap.size() > 1 && BuildConfig.DEBUG) {
                throw new IllegalStateException("wallpaper set-guide more than one!");
            }
            WallpaperSetGuide guide = sGuideMap.get(type);
            if (guide != null) {
                guide.die();
            } else {
                HSLog.e("ApplySuccess : " + type + ", no found exist window");
            }
        } else {
            HSPreferenceHelper.getDefault().putBoolean(getPrefsKey(type), true);
        }
    }

    public static void show(Context context, int type) {
        hide(type);

        boolean hasShown = HSPreferenceHelper.getDefault().getBoolean(getPrefsKey(type), false);
        if (hasShown) {
            HSLog.d("Type = " + type + ", has shown before.");
            return;
        }

        WallpaperSetGuide guide = new WallpaperSetGuide(context, type);
        sGuideMap.put(type, guide);
        FloatWindowManager.getInstance().showDialog(guide);
        GuideHelper.logTipShowEvent(type);
    }

    public WallpaperSetGuide(Context context, int type) {
        super(context);
        mType = type;
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.wallpaper_guide_set,this);
        v.setVisibility(INVISIBLE);
        v.findViewById(R.id.close).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                die();
                GuideHelper.logTipCloseEvent(mType);
            }
        });
    }

    @Override
    protected String getGroupTag() {
        return LiveWallpaperConsts.GUIDE_WINDOW_TAG;
    }

    @Override
    public void dismiss() {
        this.animate().alpha(0).setDuration(200).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setVisibility(INVISIBLE);
                FloatWindowManager.getInstance().removeDialog(WallpaperSetGuide.this);
            }
        }).start();
    }

    @Override
    public boolean onBackPressed() {
        dismiss();
        return false;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        sGuideMap.remove(mType);
    }

    /**
     * No need to show this type of guide ever again.
     */
    public void die() {
        HSPreferenceHelper.getDefault().putBoolean(getPrefsKey(mType), true);
        dismiss();
    }

    public static String getPrefsKey(int type) {
        String keyName = "";
        switch (type) {
            case GuideHelper.TYPE_3D :
                keyName = LiveWallpaperConsts.PREF_KEY_TYPE_3D;
                break;
            case GuideHelper.TYPE_LIVE_TOUCH:
                keyName = LiveWallpaperConsts.PREF_KEY_TYPE_LIVE_TOUCH;
                break;
            case GuideHelper.TYPE_NORMAL:
                keyName = LiveWallpaperConsts.PREF_KEY_TYPE_NORMAL;
                break;
        }
        return keyName + "_set_guide";
    }

    @Override
    public WindowManager.LayoutParams getLayoutParams() {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.type = WindowManager.LayoutParams.TYPE_PHONE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            lp.type = WindowManager.LayoutParams.TYPE_TOAST;
        }
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.y = Dimensions.pxFromDp(Compats.IS_VIVO_DEVICE ? 200 : 72);
        lp.gravity = Gravity.BOTTOM;
        lp.format = PixelFormat.RGBA_8888;
        lp.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
        lp.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        return lp;
    }

    @Override
    public boolean shouldDismissOnLauncherStop() {
        return false;
    }

    @Override
    public void onAddedToWindow(SafeWindowManager windowManager) {
        setVisibility(VISIBLE);
        setAlpha(0.1f);
        animate().alpha(1).setDuration(200).start();
    }

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        mType = type;
    }


}
