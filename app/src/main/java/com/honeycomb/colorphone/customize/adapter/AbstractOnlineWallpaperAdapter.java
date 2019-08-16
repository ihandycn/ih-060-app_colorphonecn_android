package com.honeycomb.colorphone.customize.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.colorphone.lock.util.ViewUtils;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.customize.CustomizeConstants;
import com.honeycomb.colorphone.customize.WallpaperDownloadEngine;
import com.honeycomb.colorphone.customize.WallpaperInfo;
import com.honeycomb.colorphone.customize.WallpaperMgr;
import com.honeycomb.colorphone.customize.activity.CustomizeActivity;
import com.honeycomb.colorphone.customize.activity.WallpaperPreviewActivity;
import com.honeycomb.colorphone.customize.livewallpaper.WallpaperLoader;
import com.honeycomb.colorphone.customize.util.CustomizeUtils;
import com.honeycomb.colorphone.customize.view.TextureVideoView;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Preferences;

import net.appcloudbox.ads.base.AcbNativeAd;
import net.appcloudbox.ads.nativead.AcbNativeAdLoader;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractOnlineWallpaperAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements View.OnClickListener, CustomizeActivity.ActivityResultHandler {
    Context mContext;
    protected WallpaperMgr.Scenario mScenario;
    protected int mCategoryIndex = -1;
    protected String mCategoryName = "";
    protected List<WallpaperInfo> mOrigNormalDataSet = new ArrayList<>();
    protected List<Object> mDataSet = new ArrayList<>();
    protected GridLayoutManager mLayoutManager;

    //ad related
    protected Handler mAdHandler;
    protected LayoutInflater mInflater;
    protected ArrayList<AcbNativeAd> mCandidateAds = new ArrayList<>();
    protected ArrayList<AcbNativeAd> mAddedAds = new ArrayList<>();
    protected boolean mAdsEnabledByConfig;
    protected int mWallpaperCountBetweenAds = 1;
    protected int mCurrentRequestCount;
    protected int mStartIndex = 0;
    protected int mLastAdIndex = -1;
    protected int mAddedAdsCount = 0;
    protected int mMaxVisiblePosition;
    protected List<Integer> mAdCount = new ArrayList<>();
    protected AcbNativeAdLoader mAdLoader;

    protected WallpaperLoader mWallpaperLoader;
    protected static WallpaperInfo sApplyingWallpaper;

    protected int mScreenWidth;

    protected List<LivePreviewViewHolder> mLivePreviewViewholders = new ArrayList<>();

    @Override
    public void onClick(View v) {
        WallpaperInfo clickedWallpaper = (WallpaperInfo) v.getTag();
        ArrayList<WallpaperInfo> wallpapersToPreview = new ArrayList<>(mOrigNormalDataSet);
        int positionInPreviewWallpapers = 0;
        for (int i = 0; i < wallpapersToPreview.size(); i++) {
            WallpaperInfo item = wallpapersToPreview.get(i);
            if (item.equals(clickedWallpaper)) {
                positionInPreviewWallpapers = i;
                break;
            }
        }

        Preferences preferences = Preferences.get(CustomizeConstants.CUSTOMIZE_PREFS);
        Activity activity = (Activity) mContext;
        boolean isAutoStart = activity.getIntent().getBooleanExtra(
                CustomizeActivity.INTENT_EXTRA_IS_AUTO_START, false);
        switch (clickedWallpaper.getType()) {
            case WallpaperInfo.WALLPAPER_TYPE_3D:
                sApplyingWallpaper = clickedWallpaper;
//                preferences.putString(ShareActivity.PREF_KEY_SHARE_PIC_URL, sApplyingWallpaper.getThumbnailUrl());
                mWallpaperLoader = CustomizeUtils.preview3DWallpaper(activity, clickedWallpaper);
//                if (mScenario == WallpaperMgr.Scenario.ONLINE_NEW) {
//                    LauncherAnalytics.logEvent("Wallpaper_New_3D_Thumbnail_Clicked");
//                } else if (mScenario == WallpaperMgr.Scenario.ONLINE_VIDEO) {
//                    LauncherAnalytics.logEvent("Wallpaper_Live_3D_Thumbnail_Clicked", "Type", "3D", "Name", clickedWallpaper.getName());
//                }
//                if (isAutoStart) {
//                    LauncherAnalytics.logEvent("Opening_Wallpaper_Live_3D_Thumbnail_Clicked",
//                            "Type", "3D", "Name", clickedWallpaper.getSource());
//                }
                break;
            case WallpaperInfo.WALLPAPER_TYPE_LIVE:
                sApplyingWallpaper = clickedWallpaper;
//                preferences.putString(ShareActivity.PREF_KEY_SHARE_PIC_URL, sApplyingWallpaper.getThumbnailUrl());
                mWallpaperLoader = CustomizeUtils.previewLiveWallpaper(activity, clickedWallpaper);
//                if (mScenario == WallpaperMgr.Scenario.ONLINE_NEW) {
//                    LauncherAnalytics.logEvent("Wallpaper_New_live_Thumbnail_Clicked");
//                } else if (mScenario == WallpaperMgr.Scenario.ONLINE_VIDEO) {
//                    LauncherAnalytics.logEvent("Wallpaper_Live_3D_Thumbnail_Clicked", "Type", "live", "Name", clickedWallpaper.getName());
//                }
//                if (isAutoStart) {
//                    LauncherAnalytics.logEvent("Opening_Wallpaper_Live_3D_Thumbnail_Clicked",
//                            "Type", "Live", "Name", clickedWallpaper.getSource());
//                }
                break;

            case WallpaperInfo.WALLPAPER_TYPE_VIDEO:
                mWallpaperLoader = CustomizeUtils.previewLiveWallpaper(activity, clickedWallpaper);
                break;
            default:
//                preferences.putString(ShareActivity.PREF_KEY_SHARE_PIC_URL, "");
                Intent intent = new Intent(activity, WallpaperPreviewActivity.class);
                intent.putExtra(WallpaperPreviewActivity.INTENT_KEY_SCENARIO, mScenario.ordinal());
                intent.putParcelableArrayListExtra(WallpaperPreviewActivity.INTENT_KEY_WALLPAPERS, wallpapersToPreview);
                intent.putExtra(WallpaperPreviewActivity.INTENT_KEY_INDEX, positionInPreviewWallpapers);
                intent.putExtra(WallpaperPreviewActivity.INTENT_KEY_TYPE_NAME, mCategoryName);
//                if (mScenario != WallpaperMgr.Scenario.ONLINE_NEW && mScenario != WallpaperMgr.Scenario.ONLINE_VIDEO) {
//                    LauncherAnalytics.logEvent("Wallpaper_PaperList_Thumbnail_Clicked", "Type", mCategoryName);
//                }
                try {
                    activity.startActivity(intent);
                } catch (Exception e) {
                    // TODO: consider sending wallpaper data through file to avoid TransactionTooLargeException
                    HSLog.e("OnlineWallpaperGalleryAdapter", "Error launching WallpaperPreviewActivity, "
                            + "perhaps wallpaper data is too large to transact through binder.");
                    e.printStackTrace();
                }
//                if (mScenario == WallpaperMgr.Scenario.ONLINE_NEW) {
//                    LauncherAnalytics.logEvent("Wallpaper_New_Pic_Thumbnail_Clicked");
//                }
                break;
        }
    }

    @Override
    public void handleActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
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
        if (mAdLoader != null) {
            mAdLoader.cancel();
        }

        for (AcbNativeAd ad : mAddedAds) {
            if (ad != null) {
                ad.release();
            }
        }
        mAddedAds.clear();

        for (AcbNativeAd ad : mCandidateAds) {
            if (ad != null) {
                ad.release();
            }
        }
        mCandidateAds.clear();

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
            mHotTypeImageView.setVisibility(visible);
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
}
