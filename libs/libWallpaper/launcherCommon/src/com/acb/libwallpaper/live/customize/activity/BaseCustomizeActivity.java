package com.acb.libwallpaper.live.customize.activity;

import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;

import com.acb.libwallpaper.live.base.BaseAppCompatActivity;
import com.acb.libwallpaper.live.util.ActivityUtils;
import com.acb.libwallpaper.live.util.CommonUtils;
 import com.honeycomb.colorphone.R;


public abstract class BaseCustomizeActivity extends BaseAppCompatActivity {

    public static final String KEY_PREF_CUSTOMIZE_3D_THEME_APPLIED_RESULT = "customize_3d_theme_applied_result";

    protected boolean mHasPendingTheme = false;
    private boolean mIsDestroying;
    public boolean mIsServiceConnected = false;

    public Status mCurrentStatus = Status.APPLY;

    public enum Status {
        UPDATE_AIR_LAUNCHER, // Air Launcher version is too low to use this theme
        APPLY,
        CURRENT,
        DOWNLOAD,
        DOWNLOADING,
        UNDETERMINED
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make status bar customized
        ActivityUtils.configStatusBarColor(this);
    }

    @Override
    protected void onDestroy() {
        mIsDestroying = true;
        super.onDestroy();
    }

    public boolean isDestroying() {
        return mIsDestroying;
    }

    void configToolbar(String title, @ColorInt int titleColor, @ColorInt int backgroundColor) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.action_bar);
        if (toolbar == null) {
            return;
        }

        toolbar.setTitle(title);
        toolbar.setTitleTextColor(titleColor);
        toolbar.setBackgroundColor(backgroundColor);
        setSupportActionBar(toolbar);
        if (CommonUtils.ATLEAST_LOLLIPOP) {
            getSupportActionBar().setElevation(0);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }
}
