package com.honeycomb.colorphone.customize.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;

import com.colorphone.lock.util.ViewUtils;
import com.honeycomb.colorphone.ICustomizeService;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.base.BaseAppCompatActivity;
import com.honeycomb.colorphone.customize.CustomizeService;
import com.honeycomb.colorphone.customize.DrawView;
import com.honeycomb.colorphone.customize.EditWallpaperHintDrawer;
import com.honeycomb.colorphone.customize.WallpaperInfo;
import com.honeycomb.colorphone.customize.WallpaperMgr;
import com.honeycomb.colorphone.customize.util.CustomizeUtils;
import com.honeycomb.colorphone.customize.view.ProgressDialog;
import com.honeycomb.colorphone.customize.wallpaper.WallpaperManagerProxy;
import com.honeycomb.colorphone.util.ActivityUtils;
import com.honeycomb.colorphone.util.Analytics;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Threads;
import com.superapps.util.Toasts;

import java.io.IOException;

import hugo.weaving.DebugLog;


public abstract class WallpaperBaseActivity extends BaseAppCompatActivity implements ServiceConnection {

    private static final String TAG = WallpaperBaseActivity.class.getSimpleName();

    protected ICustomizeService mService = null;
    protected ProgressDialog mDialog;
    protected WallpaperInfo mCurrentWallpaper;
    private boolean mIsSettingWallpaper = false;

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mService = ICustomizeService.Stub.asInterface(service);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mService = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mService == null) {
            Intent intent = new Intent(this, CustomizeService.class);
            intent.setAction(CustomizeService.class.getName());
            bindService(intent, this, Context.BIND_AUTO_CREATE);
        }
        CustomizeUtils.setWallpaperWindowFlags(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(this);
        if (mDialog != null) {
            mDialog.dismiss(true);
        }
    }

    protected void startGuideAnimation(Activity activity, int resId, final int width, final int height) {
        final DrawView drawView = ViewUtils.findViewById(activity, resId);
        if (drawView == null) {
            return;
        }
        drawView.post(new Runnable() {
            @Override
            public void run() {
                EditWallpaperHintDrawer drawer = new EditWallpaperHintDrawer(drawView, width, height);
                drawView.setDrawer(drawer);
                drawer.start();
                drawView.setVisibility(View.VISIBLE);
            }
        });
    }

    protected abstract void refreshButtonState();

    protected boolean isSettingWallpaper() {
        return mIsSettingWallpaper;
    }

    protected void applyWallpaper(final boolean isScroll) {
        applyWallpaper(isScroll, true);
    }

    protected void applyWallpaper(final boolean isScroll, boolean logEvent) {
        if (ActivityUtils.isDestroyed(this)) {
            return;
        }

        mIsSettingWallpaper = true;
        mCurrentWallpaper = getCurrentWallpaper();
        if (logEvent) {
            if (mCurrentWallpaper.getCategory() != null) {
                HSLog.i(mCurrentWallpaper.getCategory().categoryName + "");
            }
        }

        final Handler mHandler = new Handler();
        mDialog = ProgressDialog.createDialog(this, getString(R.string.wallpaper_setting_progress_dialog_text));
        mDialog.show();
        mDialog.setCancelable(false);
        final Bitmap wallpaper = tryGetWallpaperToSet();
        if (wallpaper != null) {
            Threads.postOnThreadPoolExecutor(new Runnable() {
                @Override
                public void run() {
                    final boolean success = setWallpaper(wallpaper, logEvent);
                    int delays = wallpaper.getWidth() * wallpaper.getHeight() / 10000;
                    if (delays > 1000) {
                        delays = 1000;
                    }
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    applyWallPaperFinish(isScroll);
                                }
                            });
                            if (success) {
                                mDialog.dismiss(false);
                            } else {
                                mDialog.dismiss(true);
                                Toasts.showToast(R.string.wallpaper_toast_set_failed);
                            }
                        }
                    }, delays);
                }
            });
        } else {
            mDialog.dismiss(true);
            Toasts.showToast(R.string.wallpaper_toast_set_failed);
            finish();
        }
    }

    private void applyWallPaperFinish(boolean isScroll) {
//        CommonUtils.startLauncherAndSelectWallpaper(WallpaperBaseActivity.this, isScroll);
        finish();
    }

    @DebugLog
    private boolean setWallpaper(Bitmap wallpaper, boolean logEvent) {
        if (wallpaper != null) {
            ICustomizeService service = mService;
            if (service != null) {
                try {
                    service.preChangeWallpaperFromLauncher();
                    WallpaperMgr.getInstance().cleanCurrentWallpaper();
                    WallpaperManagerProxy.getInstance().setSystemBitmap(this, wallpaper);
                    if (logEvent) {
                        Analytics.logEvent("Wallpaper_SetAsWallpaper", "type", "Success");
                    }
                } catch (IOException | RemoteException e) {
                    if (logEvent) {
                        Analytics.logEvent("Wallpaper_SetAsWallpaper", "type", "Failed");
                    }
                    e.printStackTrace();
                }
                mCurrentWallpaper = getCurrentWallpaper();
                if (mCurrentWallpaper != null) {
                    WallpaperMgr.getInstance().saveCurrentWallpaper(mCurrentWallpaper);
                    HSGlobalNotificationCenter.sendNotificationOnMainThread(WallpaperMgr.NOTIFICATION_REFRESH_LOCAL_WALLPAPER);
                }
                return true;
            }
        }
        return false;
    }

    protected abstract Bitmap tryGetWallpaperToSet();

    protected abstract WallpaperInfo getCurrentWallpaper();
}
