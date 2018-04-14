package com.honeycomb.colorphone.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.honeycomb.colorphone.ColorPhoneApplication;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.preview.ThemePreviewView;
import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.ihs.app.framework.activity.HSAppCompatActivity;

import java.util.ArrayList;
import java.util.List;


public class ThemePreviewActivity extends HSAppCompatActivity {
    public static final String NOTIFY_THEME_SELECT = "notify_theme_select";
    public static final String NOTIFY_THEME_DOWNLOAD = "notify_theme_download";
    public static final String NOTIFY_THEME_KEY = "notify_theme_select_key";

    private Theme mTheme;
    private ArrayList<Theme> mThemes = new ArrayList<>();
    private ViewPager mViewPager;
    private View mNavBack;
    private ThemePagerAdapter mAdapter;
    private List<ThemePreviewView> mViews = new ArrayList<>();
    private MediaPlayer mMediaPlayer;

    public static void start(Context context, int position) {
        Intent starter = new Intent(context, ThemePreviewActivity.class);
        starter.putExtra("position", position);
        if (context instanceof Activity) {
            ((Activity)context).overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
        }
        starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(starter);
    }


    public List<ThemePreviewView> getViews() {
        return mViews;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mThemes.addAll(Theme.themes());
        int pos = getIntent().getIntExtra("position", 0);
        mTheme = mThemes.get(pos);
        ColorPhoneApplication.getConfigLog().getEvent().onThemePreviewOpen(mTheme.getIdName().toLowerCase());
        setContentView(R.layout.activity_theme_preview);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);

        mViewPager = (ViewPager) findViewById(R.id.preview_view_pager);
        mAdapter = new ThemePagerAdapter();
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(1);
        mViewPager.setCurrentItem(pos);

        mNavBack = findViewById(R.id.nav_back);
        mNavBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mMediaPlayer = new MediaPlayer();


        if (mTheme.isLocked()) {
            LauncherAnalytics.logEvent("Colorphone_Theme_Button_Unlock_show", "themeName", mTheme.getName());
        }
    }

    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        for (ThemePreviewView previewView : mViews) {
            previewView.setBlockAnimationForPageChange(false);
            previewView.onStart();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        for (ThemePreviewView previewView : mViews) {
            previewView.onStop();
        }
    }

    @Override
    public void onBackPressed() {
        for (ThemePreviewView previewView : mViews) {
            if (previewView.isRewardVideoLoading()) {
                previewView.stopRewardVideoLoading();
                return;
            }
        }
        super.onBackPressed();
    }

    private class ThemePagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mThemes.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ThemePreviewView controller = new ThemePreviewView(ThemePreviewActivity.this);
            controller.init(ThemePreviewActivity.this, mThemes, position, mNavBack);
            controller.setPageSelectedPos(mViewPager.getCurrentItem());
            if (position == mViewPager.getCurrentItem()) {
                controller.setBlockAnimationForPageChange(false);
            } else {
                controller.setNoTransition(true);
            }
            container.addView(controller);
            controller.setTag(position);
            mViews.add(controller);
            mViewPager.addOnPageChangeListener(controller);

            return controller;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
            mViews.remove(object);
            mViewPager.removeOnPageChangeListener((ViewPager.OnPageChangeListener) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }
}
