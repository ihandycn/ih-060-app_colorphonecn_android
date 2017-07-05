package com.honeycomb.colorphone;

import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;

import com.acb.call.CPSettings;
import com.acb.call.themes.Type;
import com.honeycomb.colorphone.download.TasksManager;
import com.honeycomb.colorphone.themeselector.ThemeSelectorAdapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Random;

public class ColorPhoneActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private RecyclerView mRecyclerView;
    private SwitchCompat mainSwitch;
    private final static int RECYCLER_VIEW_SPAN_COUNT = 2;
    private ArrayList<Theme> mRecyclerViewData = new ArrayList<Theme>();
    private int defaultThemeId = 1;

    private Runnable UpdateRunnable = new Runnable() {

        @Override
        public void run() {
            if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
                runOnUiThread(this);
                return;
            }

            if (mRecyclerView != null && mRecyclerView.getAdapter() != null) {
                mRecyclerView.getAdapter().notifyDataSetChanged();
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundResource(R.color.white);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        View leftDrawer = findViewById(R.id.left_drawer);
        mainSwitch = (SwitchCompat) leftDrawer.findViewById(R.id.main_switch);
        mainSwitch.setChecked(CPSettings.isScreenFlashModuleEnabled());
        mainSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                CPSettings.setScreenFlashModuleEnabled(isChecked);
            }
        });
        leftDrawer.findViewById(R.id.settings_main_switch).setOnClickListener(this);
        leftDrawer.findViewById(R.id.settings_feedback).setOnClickListener(this);
        leftDrawer.findViewById(R.id.settings_setting).setOnClickListener(this);
        leftDrawer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        initData();
        initRecyclerView();

        TasksManager.getImpl().onCreate(new WeakReference<Runnable>(UpdateRunnable));
    }

    @Override
    protected void onDestroy() {
        TasksManager.getImpl().onDestroy();

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void initData() {
        Type[] themeTypes = Type.values();
        final int count = themeTypes.length;
        int selectedThemeId = CPSettings.getInt(CPSettings.PREFS_SCREEN_FLASH_SELECTOR_INDEX, defaultThemeId);
        Random random = new Random(555517);
        for (int i = 0; i < count; i++) {
            final Type type = themeTypes[i];
            if(type == Type.NONE) {
                continue;
            }
            final Theme theme = new Theme();
            theme.setDownload(random.nextInt(682220));
            theme.setName(getString(ThemeUtils.getThemeNameRes(this, i)));
            theme.setThemeId(type.getValue());
            theme.setImageRes(getThemePreviewImage(type));
            theme.setHot(i < 2);
            if (theme.getThemeId() == selectedThemeId) {
                theme.setSelected(true);
            }
            mRecyclerViewData.add(theme);
            if (type.isGif()) {
                TasksManager.getImpl().addTask(type);
            }

        }
    }

    private int getThemePreviewImage(Type type) {
        switch (type) {
            case NEON:
                return R.drawable.theme_preview_neon;
            case STARS:
                return R.drawable.theme_preview_stars;
            case SUN:
                return R.drawable.theme_preview_sun;
            case TECH:
                return R.drawable.acb_phone_theme_technological_bg;
            default:
                break;
        }
        return 0;
    }

    private void initRecyclerView() {
        View contentView = findViewById(R.id.recycler_view_content);
        mRecyclerView = (RecyclerView) contentView.findViewById(R.id.recycler_view);

        RecyclerView.LayoutManager layoutManager =
                new StaggeredGridLayoutManager(RECYCLER_VIEW_SPAN_COUNT, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        ThemeSelectorAdapter adapter = new ThemeSelectorAdapter(mRecyclerViewData);
//        mRecyclerView.addItemDecoration(new SpaceItemDecoration(getResources().getDimensionPixelSize(R.dimen.theme_card_margin)));
        mRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.settings_main_switch:
                toggle();
                break;
            case R.id.settings_feedback:

                break;
            case R.id.settings_setting:

                break;
        }
    }

    private void toggle() {
        boolean isChecked = mainSwitch.isChecked();
        mainSwitch.setChecked(!isChecked);
    }
}
