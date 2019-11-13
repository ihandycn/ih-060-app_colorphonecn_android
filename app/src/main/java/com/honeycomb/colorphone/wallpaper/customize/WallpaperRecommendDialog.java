package com.honeycomb.colorphone.wallpaper.customize;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.honeycomb.colorphone.wallpaper.LauncherAnalytics;
import com.honeycomb.colorphone.view.GlideApp;
 import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.wallpaper.base.BaseFullScreenDialogFragment;
import com.honeycomb.colorphone.wallpaper.customize.activity.CustomizeActivity;
import com.honeycomb.colorphone.wallpaper.customize.util.CustomizeUtils;
import com.honeycomb.colorphone.wallpaper.download.Downloader;
import com.honeycomb.colorphone.wallpaper.model.LauncherFiles;
import com.honeycomb.colorphone.wallpaper.theme.ThemeConstants;
import com.honeycomb.colorphone.wallpaper.util.PicCache;
import com.honeycomb.colorphone.wallpaper.util.ViewUtils;
import com.superapps.util.Dimensions;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;
import com.superapps.view.TypefacedTextView;

import java.io.File;

public class WallpaperRecommendDialog extends BaseFullScreenDialogFragment {

    private static final String TYPE_WALLPAPER = "type_wallpaper";
    public static final int TYPE_LIVE = 0;
    public static final int TYPE_THREE_D = 1;

    public static WallpaperRecommendDialog newInstance(int type) {
        WallpaperRecommendDialog dialog = new WallpaperRecommendDialog();
        Bundle bundle = new Bundle();
        bundle.putInt(TYPE_WALLPAPER, type);
        dialog.setArguments(bundle);
        return dialog;
    }

    private View mRootView;
    private ImageView mWallpaperImgView;
    private TypefacedTextView mMsgText;
    private TypefacedTextView mPreviewBtn;
    private TypefacedTextView mMoreBtn;
    private TypefacedTextView mTitleTextView;

    private int mCurrentWallpaperType = -1;
    private String mCurrentWallpaperName;
    private boolean mDismissOneTab;
    private OnHideListener mOnHideListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mCurrentWallpaperType = bundle.getInt(TYPE_WALLPAPER);
        }
        mCurrentWallpaperName = "";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.shuffle_recommend_wallpaper, container, false);
        initView();
        initData();
        return mRootView;
    }

    private void initView() {
        mWallpaperImgView = ViewUtils.findViewById(mRootView, R.id.wallpaper_img_view);
        mMsgText = ViewUtils.findViewById(mRootView, R.id.msg_text);
        mPreviewBtn = ViewUtils.findViewById(mRootView, R.id.preview_btn);
        mMoreBtn = ViewUtils.findViewById(mRootView, R.id.more_btn);
        mTitleTextView = ViewUtils.findViewById(mRootView, R.id.title);
        initMargin();

        mPreviewBtn.setOnClickListener((View v) -> {
            previewClick();
        });
        mMoreBtn.setOnClickListener(v -> {
            moreClick();
        });
        mWallpaperImgView.setOnClickListener(v -> {
            previewClick();
        });
        ViewUtils.findViewById(mRootView, R.id.close_btn).setOnClickListener(v -> {
            dismissSelf(false);
        });
    }

    private void initMargin() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mTitleTextView.getLayoutParams();
        params.topMargin = (Dimensions.getPhoneHeight(getActivity()) - Dimensions.getStatusBarHeight(getActivity()) - Dimensions.pxFromDp(437)) / 2;
    }

    private void dismissSelf(boolean isDismiss) {
        mDismissOneTab = isDismiss;
        dismissAllowingStateLoss();
    }

    private void previewClick() {
        if (TextUtils.isEmpty(mCurrentWallpaperName)) {
            return;
        }
        Preferences.get(LauncherFiles.DESKTOP_PREFS).putInt(WallpaperPicCacheUtils.KEY_READY_TO_SET_WALLPAPER_TYPE, mCurrentWallpaperType);
        Preferences.get(LauncherFiles.DESKTOP_PREFS).putString(WallpaperPicCacheUtils.KEY_READY_TO_SET_WALLPAPER_NAME, mCurrentWallpaperName);
        switch (mCurrentWallpaperType) {
            case TYPE_LIVE:
                CustomizeUtils.previewLiveWallpaper(getActivity(), WallpaperInfo.newLiveWallpaper(mCurrentWallpaperName));
                LauncherAnalytics.logEvent("Alert_Shuffle_Live_Clicked", "Type", mCurrentWallpaperName);
                break;
            case TYPE_THREE_D:
                CustomizeUtils.preview3DWallpaper(getActivity(), WallpaperInfo.new3DWallpaper(mCurrentWallpaperName));
                LauncherAnalytics.logEvent("Alert_Shuffle_3D_Clicked", "Type", mCurrentWallpaperName);
                break;
            default:
                break;
        }
        dismissSelf(true);
    }

    private void moreClick() {
        switch (mCurrentWallpaperType) {
            case TYPE_LIVE:
                gotoWallpaper(1);
                LauncherAnalytics.logEvent("Alert_Shuffle_Live_Clicked", "Type");
                break;
            case TYPE_THREE_D:
                gotoWallpaper(1);
                LauncherAnalytics.logEvent("Alert_Shuffle_3D_Clicked", "Type", mCurrentWallpaperName);
                break;
            default:
                break;
        }
        dismissSelf(true);
    }

    private void gotoWallpaper(int index) {
        Intent intent = CustomizeActivity.getLaunchIntent(getActivity(), "ShuffleAlert", CustomizeActivity.TAB_INDEX_WALLPAPER);
        intent.putExtra(ThemeConstants.INTENT_KEY_WALLPAPER_TAB, index);
        getActivity().startActivity(intent);
    }

    private void initData() {
        switch (mCurrentWallpaperType) {
            case TYPE_LIVE:
                configLiveData();
                break;
            case TYPE_THREE_D:
                config3DData();
                break;
            default:
                break;
        }
    }

    private void configLiveData() {
        mMsgText.setText(getResources().getString(R.string.won_live_wallpaper));
        String cacheUrl = WallpaperPicCacheUtils.getCacheWallpaperUrl();
        String cacheKey = WallpaperPicCacheUtils.getCacheWallpaperName();
        if (TextUtils.isEmpty(cacheKey) || TextUtils.isEmpty(cacheUrl) || !WallpaperPicCacheUtils.isUseful(cacheKey)) {
            String[] strings = WallpaperPicCacheUtils.getLiveWallpaperNameAndPicUrl();
            mCurrentWallpaperName = strings[0];
            setWallpaperPic(strings[1]);
        } else {
            mCurrentWallpaperName = cacheKey;
            setWallpaperImgByFile(cacheUrl);
        }
        Threads.postOnThreadPoolExecutor(() -> WallpaperPicCacheUtils.downloadPic(WallpaperPicCacheUtils.TYPE_3D_WALLPAPER));

        LauncherAnalytics.logEvent("Alert_Shuffle_Live_Showed", "Type", mCurrentWallpaperName);
    }

    private void config3DData() {
        mMsgText.setText(getResources().getString(R.string.won_three_d_wallpaper));
        String cacheUrl = WallpaperPicCacheUtils.getCacheWallpaperUrl();
        String cacheKey = WallpaperPicCacheUtils.getCacheWallpaperName();
        if (TextUtils.isEmpty(cacheKey) || TextUtils.isEmpty(cacheUrl) || !WallpaperPicCacheUtils.isUseful(cacheKey)) {
            String[] strings = WallpaperPicCacheUtils.get3DWallpaperNameAndPicUrl();
            mCurrentWallpaperName = strings[0];
            setWallpaperPic(strings[1]);
        } else {
            mCurrentWallpaperName = cacheKey;
            setWallpaperImgByFile(cacheUrl);
        }
        Threads.postOnThreadPoolExecutor(() -> WallpaperPicCacheUtils.downloadPic(WallpaperPicCacheUtils.TYPE_LIVE_WALLPAPER));

        LauncherAnalytics.logEvent("Alert_Shuffle_3D_Showed", "Type", mCurrentWallpaperName);
    }


    private void setWallpaperPic(String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        if (setWallpaperImgByFile(url)) {
            return;
        }

        PicCache.getInstance().downloadAndCachePic(url, new PicCache.OnCacheSuccessListener() {
            @Override
            public void onSuccess(Downloader.DownloadItem item) {
                setWallpaperImgByFile(item.getUrl());
            }

            @Override
            public void onFailure(Downloader.DownloadItem item) {

            }
        });
    }

    private boolean setWallpaperImgByFile(String url) {
        File file = PicCache.getInstance().getCacheFile(url);
        if (getActivity() != null && mWallpaperImgView != null && file != null) {
            GlideApp.with(getActivity()).load(file).dontAnimate().into(mWallpaperImgView);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (mOnHideListener != null) {
            mOnHideListener.onHide(mDismissOneTab);
        }
        super.onDismiss(dialog);
    }

    public void setOnShowListener(OnHideListener listener) {
        mOnHideListener = listener;
    }

    interface OnHideListener {
        void onHide(boolean isDismiss);
    }
}
