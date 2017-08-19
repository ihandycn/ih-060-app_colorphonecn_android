package com.honeycomb.colorphone.preview;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.acb.call.themes.Type;
import com.honeycomb.colorphone.ColorPhoneApplication;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.download.DownloadViewHolder;
import com.honeycomb.colorphone.util.FontUtils;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.framework.activity.HSAppCompatActivity;

import java.util.ArrayList;


public class ThemePreviewActivity extends HSAppCompatActivity {
    public static final String NOTIFY_THEME_SELECT = "notify_theme_select";
    public static final String NOTIFY_THEME_SELECT_KEY = "notify_theme_select_key";
    private Theme mTheme;
    private ArrayList<Theme> mThemes;
    private Type mThemeType;
    private ViewPager mViewPager;
    private View mNavBack;
    private ThemePagerAdapter mAdapter;

    public static void start(Activity context, Theme theme) {
        Intent starter = new Intent(context, ThemePreviewActivity.class);
        starter.putExtra("theme", theme);
//        starter.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        context.overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
        context.startActivity(starter);
    }

    public static void start(Activity context, ArrayList<Theme> theme, int position) {
        Intent starter = new Intent(context, ThemePreviewActivity.class);
        starter.putExtra("themeList", theme);
        starter.putExtra("position", position);
//        starter.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        context.overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
        context.startActivity(starter);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        return super.onTouchEvent(event);
    }

    public void setCustomStyle() {
        TextView name = (TextView) findViewById(R.id.caller_name);
        TextView number = (TextView) findViewById(R.id.caller_number);
        name.setTypeface(FontUtils.getTypeface(FontUtils.Font.PROXIMA_NOVA_REGULAR));
        number.setTypeface(FontUtils.getTypeface(FontUtils.Font.PROXIMA_NOVA_SEMIBOLD));

        name.setShadowLayer(Utils.pxFromDp(2), 0, Utils.pxFromDp(2), Color.BLACK);
        number.setShadowLayer(Utils.pxFromDp(1), 0, Utils.pxFromDp(1), Color.BLACK);

        ImageView avatar = (ImageView) findViewById(R.id.caller_avatar);
        avatar.setImageDrawable(ContextCompat.getDrawable(this, mTheme.getAvatar()));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTheme = (Theme) getIntent().getSerializableExtra("theme");
        mThemes = (ArrayList<Theme>) getIntent().getSerializableExtra("themeList");
        int pos = getIntent().getIntExtra("position", 0);
        mTheme = mThemes.get(pos);
        Type[] types = Type.values();
        for (Type t : types) {
            if (t.getValue() == mTheme.getThemeId()) {
                mThemeType = t;
                break;
            }
        }
        ColorPhoneApplication.getConfigLog().getEvent().onThemePreviewOpen(mThemeType.getIdName().toLowerCase());
        setContentView(R.layout.activity_theme_preview);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);

        mViewPager = (ViewPager) findViewById(R.id.preview_view_pager);
        mAdapter = new ThemePagerAdapter();
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(1);
        mViewPager.setCurrentItem(pos);

//        previewWindow = (ThemePreviewWindow) findViewById(R.id.card_flash_preview_window);
//        previewWindow.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (themeReady && !inTransition) {
//                    boolean isInHide = mApplyButton.getTranslationY() == bottomBtnTransY;
//                    if (isInHide) {
//                        mHandler.sendEmptyMessage(MSG_SHOW);
//                    }
//                    boolean isShown = mApplyButton.getTranslationY() == 0 && themeReady;
//                    if (isShown) {
//                        mHandler.sendEmptyMessage(MSG_HIDE);
//                    } else {
//                        scheduleNextHide();
//                    }
//                }
//            }
//        });
//        callActionView = (InCallActionView) findViewById(R.id.card_in_call_action_view);
//        callActionView.setTheme(mThemeType);
//        callActionView.setAutoRun(false);
//        mApplyButton = (Button) findViewById(R.id.theme_apply_btn);
//        mProgressViewHolder = new ProgressViewHolder();
//        previewImage = (ImageView) findViewById(R.id.preview_bg_img);
//        dimCover = findViewById(R.id.dim_cover);

        mNavBack = findViewById(R.id.nav_back);
        mNavBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

//        mApplyButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (inTransition) {
//                    return;
//                }
//                CPSettings.putInt(CPSettings.PREFS_SCREEN_FLASH_THEME_ID, mTheme.getThemeId());
//                // notify
//                HSBundle bundle = new HSBundle();
//                bundle.putInt(NOTIFY_THEME_SELECT_KEY, mTheme.getThemeId());
//                HSGlobalNotificationCenter.sendNotification(NOTIFY_THEME_SELECT, bundle);
//
//                setButtonState(true);
//                Toast toast = Toast.makeText(ThemePreviewActivity.this, R.string.apply_success, Toast.LENGTH_SHORT);
//                int offsetY = (int) (bottomBtnTransY + Utils.pxFromDp(8));
//                toast.setGravity(Gravity.BOTTOM, 0, offsetY);
//                toast.show();
//            }
//        });
//        bottomBtnTransY = mApplyButton.getTranslationY();
//
//        mInter = new OvershootInterpolator(1.5f);

    }


    @Override
    protected void onStart() {
        super.onStart();


    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    public static void cache(DownloadViewHolder holder) {
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
            if (position == mViewPager.getCurrentItem()) {
                controller.setPageSelectedPos(position);
            }
            container.addView(controller);
            mViewPager.addOnPageChangeListener(controller);

            return controller;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
            mViewPager.removeOnPageChangeListener((ViewPager.OnPageChangeListener) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }
}
