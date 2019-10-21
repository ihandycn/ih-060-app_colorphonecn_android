package com.honeycomb.colorphone.wallpaper.customize.adapter;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.honeycomb.colorphone.LauncherAnalytics;
import com.honeycomb.colorphone.Manager;
import com.honeycomb.colorphone.WallpaperAnalytics;
import com.honeycomb.colorphone.util.Thunk;
 import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.wallpaper.customize.WallpaperDownloadEngine;
import com.honeycomb.colorphone.wallpaper.customize.WallpaperInfo;
import com.honeycomb.colorphone.wallpaper.customize.WallpaperMgr;
import com.honeycomb.colorphone.wallpaper.customize.WallpaperPicCacheUtils;
import com.honeycomb.colorphone.wallpaper.customize.WallpaperProvider;
import com.honeycomb.colorphone.wallpaper.customize.WallpaperRecommendDialog;
import com.honeycomb.colorphone.wallpaper.customize.activity.CustomizeActivity;
import com.honeycomb.colorphone.wallpaper.customize.activity.WallpaperPreviewActivity;
import com.honeycomb.colorphone.wallpaper.customize.util.CustomizeUtils;
import com.honeycomb.colorphone.wallpaper.desktop.util.SetWallpaperResultLogUtils;
import com.honeycomb.colorphone.wallpaper.livewallpaper.BaseWallpaperService;
import com.honeycomb.colorphone.wallpaper.livewallpaper.LiveWallpaperConsts;
import com.honeycomb.colorphone.wallpaper.livewallpaper.WallpaperLoader;
import com.honeycomb.colorphone.wallpaper.model.LauncherFiles;
import com.honeycomb.colorphone.wallpaper.share.ShareActivity;
import com.honeycomb.colorphone.wallpaper.util.ViewUtils;
import com.honeycomb.colorphone.wallpaper.view.TextureVideoView;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Preferences;
import com.superapps.util.Toasts;


import java.util.ArrayList;
import java.util.List;

public abstract class AbstractOnlineWallpaperAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements View.OnClickListener, INotificationObserver{
    public static final String KEY_ACTIVITY_RESULT = "wallpaper_on_activity_result";
    public static final String KEY_ACTIVITY_RESULT_REQUESTCODE = "requestCode";
    public static final String KEY_ACTIVITY_RESULT_RESULTCODE = "resultCode";
    public static final String KEY_ACTIVITY_RESULT_DATA = "data";

    @Thunk
    Context mContext;
    protected WallpaperMgr.Scenario mScenario;
    protected int mCategoryIndex = -1;
    protected String mCategoryName = "";
    protected List<Object> mDataSet = new ArrayList<>();
    protected GridLayoutManager mLayoutManager;

    //ad related
    protected Handler mAdHandler;
    protected LayoutInflater mInflater;
    protected boolean mAdsEnabledByConfig;
    protected int mWallpaperCountBetweenAds = 1;
    protected int mCurrentRequestCount;
    protected int mStartIndex = 0;
    protected int mLastAdIndex = -1;
    protected int mAddedAdsCount = 0;
    protected int mMaxVisiblePosition;
    protected List<Integer> mAdCount = new ArrayList<>();

    protected WallpaperLoader mWallpaperLoader;
    protected static WallpaperInfo sApplyingWallpaper;

    protected int mScreenWidth;

    protected List<LivePreviewViewHolder> mLivePreviewViewholders = new ArrayList<>();

    @Override
    public void onClick(View v) {
        int positionInAllWallpapers = (int) v.getTag();
        if (mAdCount.size() > 0 && mAdCount.size() > positionInAllWallpapers) {
            positionInAllWallpapers = positionInAllWallpapers - mAdCount.get(positionInAllWallpapers);
        }

        ArrayList<WallpaperInfo> allWallpapers = new ArrayList<>();
        ArrayList<WallpaperInfo> wallpapersToPreview = new ArrayList<>();
        int positionInPreviewWallpapers = positionInAllWallpapers;
        for (Object item : mDataSet) {
            if (item instanceof WallpaperInfo) {
                allWallpapers.add((WallpaperInfo) item);
                if (((WallpaperInfo) item).getType() != WallpaperInfo.WALLPAPER_TYPE_3D
                        && ((WallpaperInfo) item).getType() != WallpaperInfo.WALLPAPER_TYPE_LIVE) {
                    wallpapersToPreview.add((WallpaperInfo) item);
                } else if (wallpapersToPreview.size() < positionInAllWallpapers) {
                    positionInPreviewWallpapers--;
                }
            }
        }
        WallpaperInfo clickedWallpaper = allWallpapers.get(positionInAllWallpapers);
        Preferences preferences = Preferences.get(LauncherFiles.CUSTOMIZE_PREFS);
        preferences.putInt(ShareActivity.PREF_KEY_SHARE_WALLPAPER_TYPE, clickedWallpaper.getType());
        Activity activity = (Activity) mContext;
        boolean isAutoStart = activity.getIntent().getBooleanExtra(
                CustomizeActivity.INTENT_EXTRA_IS_AUTO_START, false);
        switch (clickedWallpaper.getType()) {
            case WallpaperInfo.WALLPAPER_TYPE_3D:
                sApplyingWallpaper = clickedWallpaper;
                preferences.putString(ShareActivity.PREF_KEY_SHARE_PIC_URL, sApplyingWallpaper.getThumbnailUrl());
                mWallpaperLoader = CustomizeUtils.preview3DWallpaper(activity, clickedWallpaper);
                if (mScenario == WallpaperMgr.Scenario.ONLINE_NEW) {
                    LauncherAnalytics.logEvent("Wallpaper_New_3D_Thumbnail_Clicked");
                } else if (mScenario == WallpaperMgr.Scenario.ONLINE_HOT) {
                    LauncherAnalytics.logEvent("Wallpaper_Live_3D_Thumbnail_Clicked", "Type", "3D", "Name", clickedWallpaper.getName());
                }
                if (isAutoStart) {
                    LauncherAnalytics.logEvent("Opening_Wallpaper_Live_3D_Thumbnail_Clicked",
                            "Type", "3D", "Name", clickedWallpaper.getSource());
                }
                break;
            case WallpaperInfo.WALLPAPER_TYPE_LIVE:
                sApplyingWallpaper = clickedWallpaper;
                preferences.putString(ShareActivity.PREF_KEY_SHARE_PIC_URL, sApplyingWallpaper.getThumbnailUrl());
                mWallpaperLoader = CustomizeUtils.previewLiveWallpaper(activity, clickedWallpaper);
                if (mScenario == WallpaperMgr.Scenario.ONLINE_NEW) {
                    LauncherAnalytics.logEvent("Wallpaper_New_live_Thumbnail_Clicked");
                } else if (mScenario == WallpaperMgr.Scenario.ONLINE_HOT) {
                    LauncherAnalytics.logEvent("Wallpaper_Live_3D_Thumbnail_Clicked", "Type", "live", "Name", clickedWallpaper.getName());
                }
                if (isAutoStart) {
                    LauncherAnalytics.logEvent("Opening_Wallpaper_Live_3D_Thumbnail_Clicked",
                            "Type", "Live", "Name", clickedWallpaper.getSource());
                }
                break;
            default:
                preferences.putString(ShareActivity.PREF_KEY_SHARE_PIC_URL, "");
                Intent intent = new Intent(activity, WallpaperPreviewActivity.class);
                intent.putExtra(WallpaperPreviewActivity.INTENT_KEY_SCENARIO, mScenario.ordinal());
                intent.putParcelableArrayListExtra(WallpaperPreviewActivity.INTENT_KEY_WALLPAPERS, wallpapersToPreview);
                intent.putExtra(WallpaperPreviewActivity.INTENT_KEY_INDEX, positionInPreviewWallpapers);
                if (mScenario != WallpaperMgr.Scenario.ONLINE_NEW && mScenario != WallpaperMgr.Scenario.ONLINE_HOT) {
                    LauncherAnalytics.logEvent("Wallpaper_PaperList_Thumbnail_Clicked", "Type", mCategoryName);
                }
                WallpaperAnalytics.logEvent("Wallpaper_Static_Detail_Click");
                try {
                    activity.startActivity(intent);
                } catch (Exception e) {
                    // TODO: consider sending wallpaper data through file to avoid TransactionTooLargeException
                    HSLog.e("OnlineWallpaperGalleryAdapter", "Error launching WallpaperPreviewActivity, "
                            + "perhaps wallpaper data is too large to transact through binder.");
                    e.printStackTrace();
                }
                if (mScenario == WallpaperMgr.Scenario.ONLINE_NEW) {
                    LauncherAnalytics.logEvent("Wallpaper_New_Pic_Thumbnail_Clicked");
                }
                break;
        }
    }

    public void handleActivityResult( int requestCode, int resultCode, Intent data) {
        if (requestCode == CustomizeActivity.REQUEST_CODE_APPLY_3D_WALLPAPER) {
            Preferences prefs = Preferences.getDefault();
            if (CustomizeUtils.isApplySuccessful(mContext, resultCode)) {
                if (sApplyingWallpaper == null) {
                    int type = Preferences.get(LauncherFiles.CUSTOMIZE_PREFS).getInt(WallpaperPicCacheUtils.KEY_READY_TO_SET_WALLPAPER_TYPE, -1);
                    String name = Preferences.get(LauncherFiles.CUSTOMIZE_PREFS).getString(WallpaperPicCacheUtils.KEY_READY_TO_SET_WALLPAPER_NAME, "");
                    if (TextUtils.isEmpty(name)) {
                        return;
                    }
                    if (type == WallpaperRecommendDialog.TYPE_LIVE) {
                        sApplyingWallpaper = WallpaperInfo.newLiveWallpaper(name);
                    } else if (type == WallpaperRecommendDialog.TYPE_THREE_D) {
                        sApplyingWallpaper = WallpaperInfo.new3DWallpaper(name);
                    }

                    if (sApplyingWallpaper != null) {
                        SetWallpaperResultLogUtils.logFabricSetSuccessByWallpaperAward(name, type);
                    }
                }
                if (sApplyingWallpaper != null) {
                    WallpaperInfo wallpaper = sApplyingWallpaper;
                    sApplyingWallpaper = null;
                    String newWallpaperName = wallpaper.getSource();

                    SetWallpaperResultLogUtils.logLiveWallpaperUseEvents(newWallpaperName,
                            wallpaper.getType() == WallpaperInfo.WALLPAPER_TYPE_LIVE);
                    HSLog.d("SetLiveWallpaper", "OWGA, set " + newWallpaperName);
                    prefs.putString(LiveWallpaperConsts.PREF_KEY_WALLPAPER_NAME, newWallpaperName);

                    Bundle bundle = new Bundle();
                    bundle.putParcelable(WallpaperProvider.BUNDLE_KEY_WALLPAPER, wallpaper);
                    ContentResolver contentResolver = HSApplication.getContext().getContentResolver();
                    contentResolver.call(WallpaperProvider.CONTENT_URI, WallpaperProvider.METHOD_APPLY_WALLPAPER, "", bundle);

                    Manager.getInstance().getDelegate().logEvent("Wallpaper_Set_Success", "SettingMode",
                            wallpaper.getType() == WallpaperInfo.WALLPAPER_TYPE_3D ? "3D" : "Live","ClassName", "Hot");
                }

                Toasts.showToast("设置成功");
            }
            prefs.putBoolean(LiveWallpaperConsts.PREF_KEY_IS_PREVIEW_MODE, false);

            new Handler().postDelayed(() -> {
                Intent liveWallpaperApplied = new Intent();
                liveWallpaperApplied.setAction(BaseWallpaperService.LIVE_WALLPAPER_APPLIED);
                mContext.sendBroadcast(liveWallpaperApplied);
            }, 200);
        }
    }

    public void setCategoryName(String categoryName) {
        mCategoryName = categoryName;
    }

    public void setCategoryIndex(int index) {
        mCategoryIndex = index;
    }

    public void setScenario(WallpaperMgr.Scenario scenario) {
        mScenario = scenario;
    }

    public void onDetachedFromWindow() {
        if (mWallpaperLoader != null) {
            mWallpaperLoader.cancel();
            mWallpaperLoader = null;
        }

        for (LivePreviewViewHolder livePreviewViewholder : mLivePreviewViewholders) {
            HSGlobalNotificationCenter.removeObserver(livePreviewViewholder);
        }
        mLivePreviewViewholders.clear();
    }

    public abstract WallpaperDownloadEngine.OnLoadWallpaperListener getLoadWallpaperListener();

    public abstract RecyclerView.LayoutManager getLayoutManager();

    public abstract RecyclerView.ItemDecoration getItemDecoration();

    protected static class BaseViewHolder extends RecyclerView.ViewHolder {

        protected ImageView mImageView;
        private TextView mTvPopularity;
        private ImageView mIvPopularity;
        private ImageView mHotTypeImageView;

        BaseViewHolder(View itemView) {
            super(itemView);
            mImageView = ViewUtils.findViewById(itemView, R.id.iv_wallpaper);
            mTvPopularity = ViewUtils.findViewById(itemView, R.id.tv_popularity);
            mIvPopularity = ViewUtils.findViewById(itemView, R.id.iv_popularity);
            mHotTypeImageView = ViewUtils.findViewById(itemView, R.id.hot_type_image_view);
        }

        void setPopularity(boolean showPopularity, int popularity) {
            if (showPopularity) {
                mTvPopularity.setVisibility(View.VISIBLE);
                mTvPopularity.setText(String.valueOf(popularity));
                mIvPopularity.setVisibility(View.VISIBLE);
            } else {
                mTvPopularity.setVisibility(View.INVISIBLE);
                mIvPopularity.setVisibility(View.INVISIBLE);
            }
        }

        void setHotType(int type) {
            int visible = View.GONE;
            int resourceId = 0;
            switch (type) {
                case WallpaperInfo.WALLPAPER_TYPE_3D:
                    visible = View.VISIBLE;
                    resourceId = R.drawable.wallpaper_type_3d;
                    break;
                case WallpaperInfo.WALLPAPER_TYPE_LIVE:
                    visible = View.VISIBLE;
                    resourceId = R.drawable.wallpaper_type_live;
                    break;
                default:
                    break;
            }
            mHotTypeImageView.setVisibility(visible);
            mHotTypeImageView.setImageResource(resourceId);
        }
    }

    protected static class NormalViewHolder extends BaseViewHolder {

        NormalViewHolder(View itemView) {
            super(itemView);
        }
    }

    protected static class LivePreviewViewHolder extends BaseViewHolder implements INotificationObserver {

        TextureVideoView mVideoView;

        boolean mNeedRePlay;
        boolean mResume = true;

        LivePreviewViewHolder(View itemView) {
            super(itemView);

            mVideoView = ViewUtils.findViewById(itemView, R.id.video_view);

            mVideoView.setLooping(true);

            HSGlobalNotificationCenter.addObserver(CustomizeActivity.NOTIFICATION_CUSTOMIZE_ACTIVITY_DESTROY, this);
            HSGlobalNotificationCenter.addObserver(CustomizeActivity.NOTIFICATION_CUSTOMIZE_ACTIVITY_ONRESUME, this);
            HSGlobalNotificationCenter.addObserver(CustomizeActivity.NOTIFICATION_CUSTOMIZE_ACTIVITY_ONPAUSE, this);
        }

        @Override
        public void onReceive(String s, HSBundle hsBundle) {
            switch (s) {
                case CustomizeActivity.NOTIFICATION_CUSTOMIZE_ACTIVITY_DESTROY:
                    mResume = false;
                    HSGlobalNotificationCenter.removeObserver(this);
                    break;
                case CustomizeActivity.NOTIFICATION_CUSTOMIZE_ACTIVITY_ONPAUSE:
                    mResume = false;
                    mNeedRePlay = true;
                    mVideoView.stop();
                    break;
                case CustomizeActivity.NOTIFICATION_CUSTOMIZE_ACTIVITY_ONRESUME:
                    mResume = true;
                    if (mNeedRePlay) {
                        mNeedRePlay = false;
                        mVideoView.play();
                    }
                    break;
            }
        }
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        int requestCode = hsBundle.getInt(KEY_ACTIVITY_RESULT_REQUESTCODE);
        int resultCode = hsBundle.getInt(KEY_ACTIVITY_RESULT_RESULTCODE);
        Intent data = (Intent) hsBundle.getObject(KEY_ACTIVITY_RESULT_DATA);

        handleActivityResult(requestCode, resultCode, data);
    }
}
