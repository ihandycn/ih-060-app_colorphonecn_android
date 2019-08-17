package com.honeycomb.colorphone.customize.view;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.colorphone.lock.lockscreen.locker.Locker;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.customize.CustomizeConfig;
import com.honeycomb.colorphone.customize.CustomizeConstants;
import com.honeycomb.colorphone.customize.WallpaperDownloadEngine;
import com.honeycomb.colorphone.customize.WallpaperInfo;
import com.honeycomb.colorphone.customize.WallpaperMgr;
import com.honeycomb.colorphone.customize.activity.CustomizeActivity;
import com.honeycomb.colorphone.customize.adapter.AbstractOnlineWallpaperAdapter;
import com.honeycomb.colorphone.customize.adapter.HotOnlineWallpaperGalleryAdapterFactory;
import com.honeycomb.colorphone.customize.adapter.OnlineWallpaperGalleryAdapter;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.superapps.util.Arithmetics;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Networks;
import com.superapps.util.Preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OnlineWallpaperListView extends FrameLayout implements INotificationObserver {

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
//                if (mScenario == WallpaperMgr.Scenario.ONLINE_NEW && mAdapter.getItemCount() == 0) {
//                    insertSpecialWallpapers(wallpaperInfoList);
//                }
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
            Preferences prefs = Preferences.get(CustomizeConstants.CUSTOMIZE_PREFS);
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
            List<?> configs = CustomizeConfig.getList("Application", "Wallpaper", "LiveWallpapers", "Items");
            if (configs.size() == 0) {
                return;
            }

            List<WallpaperInfo> inserted = new ArrayList<>(configs.size());
            Preferences prefs = Preferences.get(CustomizeConstants.CUSTOMIZE_PREFS);
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
    private View hintView;

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
        HSGlobalNotificationCenter.addObserver(Locker.EVENT_WALLPAPER_CHANGE, this);
    }

    @Override
    protected void onDetachedFromWindow() {
        HSGlobalNotificationCenter.removeObserver(this);
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
                case ONLINE_LIVE:
                    WallpaperDownloadEngine.getHotWallpaperList(mListener);
                    break;
                case ONLINE_VIDEO:
                    WallpaperDownloadEngine.getVideoWallpaperList(mListener);
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
        if (WallpaperMgr.Scenario.ONLINE_VIDEO.equals(scenario)) {
            mAdapter = HotOnlineWallpaperGalleryAdapterFactory.createHotOnlineWallpaperGalleryAdapter(getHotOnlineAdapterType(), getContext());
        }
        mAdapter.setScenario(scenario);
    }

    public void showWallpaperHint() {
        int hintHeight = getResources().getDimensionPixelOffset(R.dimen.wallpaper_hint_height);
        final View transDownView = findViewById(R.id.recycler_view_container);
        transDownView.setTranslationY(hintHeight + Dimensions.pxFromDp(24));

        ViewStub stub = findViewById(R.id.stub_wallpaper_hint);
        hintView = stub.inflate();
        hintView.setBackground(BackgroundDrawables.createBackgroundDrawable(
                getResources().getColor(R.color.black_70_transparent),
                0,
                1, Color.parseColor("#aa696681"),
                Dimensions.pxFromDp(16),
                false,
                false));

        View closeBtn = hintView.findViewById(R.id.locker_wallpaper_hint_close);
        closeBtn.setBackground(BackgroundDrawables.createBackgroundDrawable(
                Color.parseColor("#33c9c6de"),
                getResources().getColor(R.color.material_ripple_white),
                Dimensions.pxFromDp(18),
                false,
                true));
        closeBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                closeBtn.setClickable(false);
                hintView.animate().alpha(0).setDuration(200).start();
                transDownView.animate().translationY(0).setDuration(200).start();
                Preferences.get(CustomizeConstants.CUSTOMIZE_PREFS).putBoolean(CustomizeConstants.PREFS_LOCKER_HINT_NEED_SHOW, false);
            }
        });

    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        switch (s) {
            case Locker.EVENT_WALLPAPER_CHANGE:
                // Hide wallpaper-set hint
                if (hintView != null) {
                    hintView.setVisibility(GONE);
                    View transDownView = findViewById(R.id.recycler_view_container);
                    transDownView.setTranslationY(0);
                    Preferences.get(CustomizeConstants.CUSTOMIZE_PREFS).putBoolean(CustomizeConstants.PREFS_LOCKER_HINT_NEED_SHOW, false);
                }
                break;

            default:
                break;
        }
    }
}
