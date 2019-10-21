package com.honeycomb.colorphone.wallpaper.customize.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.honeycomb.colorphone.wallpaper.LauncherApplication;
 import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.wallpaper.customize.OverlayInstaller;
import com.honeycomb.colorphone.wallpaper.customize.WallpaperMgr;
import com.honeycomb.colorphone.wallpaper.customize.view.LocalCustomizePage;
import com.honeycomb.colorphone.wallpaper.util.ActivityUtils;
import com.honeycomb.colorphone.wallpaper.util.ViewUtils;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.honeycomb.colorphone.wallpaper.customize.activity.CustomizeActivity.NOTIFICATION_CUSTOMIZE_ACTIVITY_DESTROY;
import static com.honeycomb.colorphone.wallpaper.customize.activity.CustomizeActivity.NOTIFICATION_CUSTOMIZE_ACTIVITY_ONPAUSE;
import static com.honeycomb.colorphone.wallpaper.customize.activity.CustomizeActivity.NOTIFICATION_CUSTOMIZE_ACTIVITY_ONRESUME;


public class MyWallpaperActivity extends HSAppCompatActivity
        implements INotificationObserver, OverlayInstaller {

    private List<ActivityResultHandler> mActivityResultHandlers = new ArrayList<>(1);
    private LocalCustomizePage mLocalCustomizePage;

    @Override
    protected void onResume() {
        super.onResume();
        HSGlobalNotificationCenter.sendNotification(NOTIFICATION_CUSTOMIZE_ACTIVITY_ONRESUME);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLocalCustomizePage.reloadLocalWallpaper();
    }

    @Override
    protected void onPause() {
        super.onPause();
        HSGlobalNotificationCenter.sendNotification(NOTIFICATION_CUSTOMIZE_ACTIVITY_ONPAUSE);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityUtils.setStatusBarColor(this, Color.BLACK);
        setContentView(R.layout.local_customize_page);
        ImageView navBack = findViewById(R.id.nav_back);
        navBack.getDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        navBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mLocalCustomizePage = findViewById(R.id.local_customize_page);
        HSGlobalNotificationCenter.addObserver(WallpaperMgr.NOTIFICATION_WALLPAPER_GALLERY_SAVED, this);
        HSGlobalNotificationCenter.addObserver(WallpaperMgr.NOTIFICATION_REFRESH_LOCAL_WALLPAPER, this);


        HSGlobalNotificationCenter.addObserver(LauncherApplication.NOTIFICAITON_TRIM_MEMORY_COMPLETE, this);

        //WallpaperMgr.getInstance().initLocalWallpapers(new CustomizeService(), null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
        super.onSaveInstanceState(outState);
    }


    @Override
    public void installOverlay(View overlay, FrameLayout.LayoutParams params) {
        // Overlay must be installed onto decor view and add status / nav bar inset manually. Adding to activity content
        // root will not work.
        ((ViewGroup) getWindow().getDecorView()).addView(overlay, params);
    }

    @Override
    public void uninstallOverlay(View overlay) {
        ((ViewGroup) getWindow().getDecorView()).removeView(overlay);
    }

    @Override
    public View getOverlayee() {
        // We install overlay onto decor view, but returns activity content root here to exclude status / nav bar
        return ViewUtils.findViewById(this, android.R.id.content);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            default:
                // Dispatch to handlers only if we do not consume the result
                for (ActivityResultHandler handler : mActivityResultHandlers) {
                    handler.handleActivityResult(this, requestCode, resultCode, data);
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (quitEditingMode()) {
            return;
        }

        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();
        quitEditingMode();
    }

    private boolean quitEditingMode() {
        if (mLocalCustomizePage.isEditing()) {
            mLocalCustomizePage.exitEditMode(false);
            return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mActivityResultHandlers.clear();
        HSGlobalNotificationCenter.removeObserver(WallpaperMgr.NOTIFICATION_REFRESH_LOCAL_WALLPAPER, this);
        HSGlobalNotificationCenter.removeObserver(WallpaperMgr.NOTIFICATION_WALLPAPER_GALLERY_SAVED, this);

        HSGlobalNotificationCenter.sendNotification(NOTIFICATION_CUSTOMIZE_ACTIVITY_DESTROY);
        HSGlobalNotificationCenter.removeObserver(this);
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        if (s.equals(LauncherApplication.NOTIFICAITON_TRIM_MEMORY_COMPLETE)) {
            finish();
        }
    }

    public void addActivityResultHandler(ActivityResultHandler handler) {
        Iterator<ActivityResultHandler> iter = mActivityResultHandlers.iterator();
        while (iter.hasNext()) {
            ActivityResultHandler registered = iter.next();
            if (registered.getClass().isInstance(handler)) {
                iter.remove();
            }
        }
        mActivityResultHandlers.add(handler);
    }

    public interface ActivityResultHandler {
        void handleActivityResult(Activity activity, int requestCode, int resultCode, Intent data);
    }

}
