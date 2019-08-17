package com.honeycomb.colorphone.customize.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.call.assistant.util.Utils;
import com.colorphone.customize.ICustomizeService;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.base.BaseAppCompatActivity;
import com.honeycomb.colorphone.customize.CustomizeService;
import com.honeycomb.colorphone.customize.theme.ThemeInfo;
import com.honeycomb.colorphone.customize.theme.data.ThemeDataProvider;
import com.honeycomb.colorphone.customize.util.DataCache;
import com.honeycomb.colorphone.customize.util.ServiceHolder;
import com.honeycomb.colorphone.util.ActivityUtils;
import com.honeycomb.colorphone.util.Thunk;

import java.util.List;

/**
 * {@link AppCompatActivity} running in a sub-process that needs a {@link ICustomizeService}
 * connection.
 */
public abstract class BaseCustomizeActivity extends BaseAppCompatActivity implements ServiceConnection, ServiceHolder, DataCache<ThemeInfo> {

    public static final String KEY_PREF_CUSTOMIZE_3D_THEME_APPLIED_RESULT = "customize_3d_theme_applied_result";

    protected ICustomizeService mService;
    protected boolean mHasPendingTheme = false;
    private boolean mIsDestroying;
    private List<ThemeInfo> mCachedAllThemes;
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

        // Bind to CustomizeService
        bindCustomizeService();

        // Make status bar customized
        ActivityUtils.configStatusBarColor(this);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mService = ICustomizeService.Stub.asInterface(service);
        mIsServiceConnected = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mService = null;
        mIsServiceConnected = false;
    }

    @Override
    public ICustomizeService getService() {
        return mService;
    }

    @Override
    protected void onDestroy() {
        mIsDestroying = true;
        unbindService(this);
        super.onDestroy();
    }

    @Override
    public List<ThemeInfo> getCacheData() {
        if (mCachedAllThemes == null) {
            mCachedAllThemes = ThemeDataProvider.getAllThemes(true);
        }
        return mCachedAllThemes;
    }

    @Thunk
    void notifyForInterstitial(boolean shouldShowAd, String source) {

    }

    protected void bindCustomizeService() {
        Intent intent = new Intent(this, CustomizeService.class);
        intent.setAction(CustomizeService.class.getName());
        bindService(intent, this, Context.BIND_AUTO_CREATE);
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
        if (Utils.ATLEAST_LOLLIPOP) {
            getSupportActionBar().setElevation(0);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }
}
