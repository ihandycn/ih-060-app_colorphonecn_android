package com.honeycomb.colorphone.wallpaper.customize.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

 import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.wallpaper.customize.CustomizeConfig;
import com.honeycomb.colorphone.wallpaper.customize.WallpaperDownloadEngine;
import com.honeycomb.colorphone.wallpaper.customize.WallpaperInfo;
import com.honeycomb.colorphone.wallpaper.customize.WallpaperMgr;
import com.honeycomb.colorphone.wallpaper.customize.activity.CustomizeActivity;
import com.honeycomb.colorphone.wallpaper.customize.adapter.AbstractOnlineWallpaperAdapter;
import com.honeycomb.colorphone.wallpaper.customize.adapter.HotOnlineWallpaperGalleryAdapterFactory;
import com.honeycomb.colorphone.wallpaper.customize.adapter.OnlineWallpaperGalleryAdapter;
import com.honeycomb.colorphone.wallpaper.model.LauncherFiles;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSBundle;
import com.superapps.util.Arithmetics;
import com.superapps.util.Networks;
import com.superapps.util.Preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OnlineWallpaperListView extends FrameLayout {

    private static final String PREF_KEY_HOT_3D_WALLPAPER_START_INDEX = "hot_3d_wallpaper_start_index";
    private static final String PREF_KEY_HOT_LIVE_WALLPAPER_START_INDEX = "hot_live_wallpaper_start_index";

    private static final int HOT_3D_WALLPAPER_COUNT = 2;
    private static final int HOT_LIVE_WALLPAPER_COUNT = 2;

    public ProgressBar progressBar;
    public LinearLayout retryLayout;
    public RecyclerView mRecyclerView;
    public AbstractOnlineWallpaperAdapter mAdapter;
    public int mCategoryIndex;
    private WallpaperMgr.Scenario mScenario;

    private WallpaperDownloadEngine.OnLoadWallpaperListener mListener = new WallpaperDownloadEngine.OnLoadWallpaperListener() {
        @Override
        public void onLoadFinished(List<WallpaperInfo> wallpaperInfoList) {
            progressBar.setVisibility(View.INVISIBLE);
            retryLayout.setVisibility(View.INVISIBLE);
            if (mAdapter != null) {
                if (mScenario == WallpaperMgr.Scenario.ONLINE_NEW && mAdapter.getItemCount() == 0) {
                    insertSpecialWallpapers(wallpaperInfoList);
                }
                mAdapter.getLoadWallpaperListener().onLoadFinished(wallpaperInfoList);
            }
        }

        @Override
        public void onLoadFailed() {
            progressBar.setVisibility(View.INVISIBLE);
            retryLayout.setVisibility(View.VISIBLE);
            if (mAdapter != null) {
                mAdapter.getLoadWallpaperListener().onLoadFailed();
            }
        }

        /**
         * Insert 3D & live wallpapers into hot wallpaper list {@code toList}.
         */

        private void insertSpecialWallpapers(List<WallpaperInfo> toList) {
            insert3DWallpapers(toList, PREF_KEY_HOT_3D_WALLPAPER_START_INDEX, HOT_3D_WALLPAPER_COUNT);
            insertLiveWallpapers(toList, PREF_KEY_HOT_LIVE_WALLPAPER_START_INDEX, HOT_LIVE_WALLPAPER_COUNT);
        }

        @SuppressWarnings("unchecked")
        private void insert3DWallpapers(List<WallpaperInfo> toList,
                                        String startIndexPrefKey, int count) {
            List<Map<String, ?>> configs = (List<Map<String, ?>>) CustomizeConfig.getList("3DWallpapers", "Items");
            if (configs.size() == 0) {
                return;
            }
            List<WallpaperInfo> inserted = new ArrayList<>(configs.size());
            Preferences prefs = Preferences.get(LauncherFiles.CUSTOMIZE_PREFS);
            int start = prefs.getInt(startIndexPrefKey, count);
            prefs.putInt(startIndexPrefKey, Arithmetics.unsignedIncrement(start, count));
            for (int i = start; i < start + count; i++) {
                Map<String, ?> config = configs.get(i % configs.size());
                String name = (String) config.get("Name");
                inserted.add(WallpaperInfo.new3DWallpaper(name, (String) config.get("VideoUrl")));
            }
            toList.addAll(0, inserted);
        }

        @SuppressWarnings("unchecked")
        private void insertLiveWallpapers(List<WallpaperInfo> toList,
                                          String startIndexPrefKey, int count) {
            List<?> configs = CustomizeConfig.getList("LiveWallpapers", "Items");
            if (configs.size() == 0) {
                return;
            }

            List<WallpaperInfo> inserted = new ArrayList<>(configs.size());
            Preferences prefs = Preferences.get(LauncherFiles.CUSTOMIZE_PREFS);
            int start = prefs.getInt(startIndexPrefKey, count);
            prefs.putInt(startIndexPrefKey, Arithmetics.unsignedIncrement(start, count));
            for (int i = start; i < start + count; i++) {
                String name;
                Object object = configs.get(i % configs.size());
                String videoUrl = null;
                if (object instanceof HashMap) {
                    name = ((HashMap<String, String>) object).get("Name");
                    videoUrl = ((HashMap<String, String>) object).get("VideoUrl");
                } else {
                    name = String.valueOf(object);
                }
                inserted.add(WallpaperInfo.newLiveWallpaper(name, videoUrl));
            }
            toList.addAll(0, inserted);
        }
    };

    public OnlineWallpaperListView(Context context) {
        this(context, null);
    }

    public OnlineWallpaperListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OnlineWallpaperListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mAdapter != null) {
            mAdapter.onDetachedFromWindow();
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        this.mRecyclerView = findViewById(R.id.recycler_view);
        this.retryLayout = findViewById(R.id.retry_downloading_layout);
        retryLayout.setOnClickListener(v -> startLoading());
        this.progressBar = findViewById(R.id.wallpaper_loading_progress_bar);

        mAdapter = new OnlineWallpaperGalleryAdapter(getContext());
        CustomizeActivity.bindScrollListener(getContext(), mRecyclerView, false);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            public static final String NOTIFY_KEY_LIST_SCROLLED = "content_list_scrolled";
            public static final String NOTIFY_KEY_LIST_SCROLLED_TOP = "content_list_scrolled_TOP";
            private HSBundle scrollData = new HSBundle();

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                scrollData.putInt("state", newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                scrollData.putInt("dx", dx);
                scrollData.putInt("dy", dy);
                HSGlobalNotificationCenter.sendNotification(NOTIFY_KEY_LIST_SCROLLED, scrollData);
                if (!recyclerView.canScrollVertically(-1)) {
                    HSGlobalNotificationCenter.sendNotification(NOTIFY_KEY_LIST_SCROLLED_TOP);
                }
            }
        });
    }

    public void setupAdapter() {
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mAdapter.getLayoutManager());
        mRecyclerView.addItemDecoration(mAdapter.getItemDecoration());
    }

    public void startLoading() {
        if (mAdapter.getItemCount() != 0) {
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        retryLayout.setVisibility(View.INVISIBLE);
        if (!Networks.isNetworkAvailable(-1)) {
            postDelayed(() -> {
                progressBar.setVisibility(View.INVISIBLE);
                retryLayout.setVisibility(View.VISIBLE);
            }, 500);
        } else {
            switch (mScenario) {
                case ONLINE_NEW:
                    WallpaperDownloadEngine.getNextNewWallpaperList(mListener);
                    break;
                case ONLINE_HOT:
                    WallpaperDownloadEngine.getHotWallpaperList(mListener);
                    break;
                default:
                    WallpaperDownloadEngine.getNextCategoryWallpaperList(mCategoryIndex, mListener);
            }
        }
    }

    public void setCategoryName(String categoryName) {
        if (mAdapter != null) {
            mAdapter.setCategoryName(categoryName);
        }
    }

    public void setCategoryIndex(int categoryIndex) {
        mCategoryIndex = categoryIndex;
        if (mAdapter != null) {
            mAdapter.setCategoryIndex(categoryIndex);
        }
    }

    private int getHotOnlineAdapterType() {
        return getResources().getInteger(R.integer.hot_online_wallpaper_adapter_type);
    }

    public void setScenario(WallpaperMgr.Scenario scenario) {
        mScenario = scenario;
        if (WallpaperMgr.Scenario.ONLINE_HOT.equals(scenario)) {
            mAdapter = HotOnlineWallpaperGalleryAdapterFactory.createHotOnlineWallpaperGalleryAdapter(getHotOnlineAdapterType(), getContext());
        }
        mAdapter.setScenario(scenario);
    }
}
