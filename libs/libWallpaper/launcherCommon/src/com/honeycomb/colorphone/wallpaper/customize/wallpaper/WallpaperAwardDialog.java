package com.honeycomb.colorphone.wallpaper.customize.wallpaper;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.honeycomb.colorphone.LauncherAnalytics;
import com.honeycomb.colorphone.view.GlideApp;
 import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.wallpaper.base.BaseDialogFragment;
import com.honeycomb.colorphone.wallpaper.customize.WallpaperInfo;
import com.honeycomb.colorphone.wallpaper.customize.WallpaperPicCacheUtils;
import com.honeycomb.colorphone.wallpaper.customize.util.CustomizeUtils;
import com.honeycomb.colorphone.wallpaper.download.Downloader;
import com.honeycomb.colorphone.wallpaper.model.LauncherFiles;
import com.honeycomb.colorphone.wallpaper.util.PicCache;
import com.honeycomb.colorphone.wallpaper.util.ViewUtils;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;
import com.superapps.view.TypefacedTextView;

import java.io.File;

public class WallpaperAwardDialog extends BaseDialogFragment {

    private static final String KEY_WALLPAPER_AWARD_TYPE = "wallpaper_award_type";
    private static final String TYPE_WALLPAPER = "type_wallpaper";
    public static final int TYPE_LIVE = 0;
    public static final int TYPE_THREE_D = 1;

    public static WallpaperAwardDialog newInstance() {
        Preferences preferenceHelper = Preferences.get(LauncherFiles.CUSTOMIZE_PREFS);
        int type = preferenceHelper.getInt(KEY_WALLPAPER_AWARD_TYPE, TYPE_LIVE);
        if (type == TYPE_LIVE) {
            preferenceHelper.putInt(KEY_WALLPAPER_AWARD_TYPE, TYPE_THREE_D);
        } else {
            preferenceHelper.putInt(KEY_WALLPAPER_AWARD_TYPE, TYPE_LIVE);
        }

        WallpaperAwardDialog dialog = new WallpaperAwardDialog();
        Bundle bundle = new Bundle();
        bundle.putInt(TYPE_WALLPAPER, type);
        dialog.setArguments(bundle);
        return dialog;
    }

    private View mRootView;
    private ImageView mWallpaperImgView;
    private TypefacedTextView mMsgText;
    private TypefacedTextView mPreviewBtn;

    private int mCurrentWallpaperType = -1;
    private String mCurrentWallpaperName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.TransparentDialog);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mCurrentWallpaperType = bundle.getInt(TYPE_WALLPAPER);
        }
        mCurrentWallpaperName = "";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.wallpaper_award_layout, container, false);
        initView();
        initData();
        return mRootView;
    }

    private void initView() {
        mWallpaperImgView = ViewUtils.findViewById(mRootView, R.id.wallpaper_img_view);
        mMsgText = ViewUtils.findViewById(mRootView, R.id.msg_text);
        mPreviewBtn = ViewUtils.findViewById(mRootView, R.id.preview_btn);

        mPreviewBtn.setOnClickListener((View v) -> {
            previewClick();
        });
        mWallpaperImgView.setOnClickListener(v -> {
            previewClick();
        });
        ViewUtils.findViewById(mRootView, R.id.close_btn).setOnClickListener(v -> {
            dismissAllowingStateLoss();
        });
    }

    private void previewClick() {
        if (TextUtils.isEmpty(mCurrentWallpaperName)) {
            return;
        }
        Preferences.get(LauncherFiles.CUSTOMIZE_PREFS).putInt(WallpaperPicCacheUtils.KEY_READY_TO_SET_WALLPAPER_TYPE, mCurrentWallpaperType);
        Preferences.get(LauncherFiles.CUSTOMIZE_PREFS).putString(WallpaperPicCacheUtils.KEY_READY_TO_SET_WALLPAPER_NAME, mCurrentWallpaperName);
        switch (mCurrentWallpaperType) {
            case TYPE_LIVE:
                CustomizeUtils.previewLiveWallpaper(getActivity(), WallpaperInfo.newLiveWallpaper(mCurrentWallpaperName));
                LauncherAnalytics.logEvent("wallpaper_Alert_live_Btn_Clicked", "Type", mCurrentWallpaperName, "Button", "Preview");
                break;
            case TYPE_THREE_D:
                CustomizeUtils.preview3DWallpaper(getActivity(), WallpaperInfo.new3DWallpaper(mCurrentWallpaperName));
                LauncherAnalytics.logEvent("wallpaper_Alert_3D_Btn_Clicked", "Type", mCurrentWallpaperName, "Button", "Preview");
                break;
            default:
                break;
        }
        dismissAllowingStateLoss();
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

        LauncherAnalytics.logEvent("wallpaper_Alert_live_Showed", "Type", mCurrentWallpaperName);
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

        LauncherAnalytics.logEvent("wallpaper_Alert_3D_Showed", "Type", mCurrentWallpaperName);
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
            GlideApp.with(getActivity()).load(file)
                    .placeholder(R.drawable.wallpaper_loading)
                    .dontAnimate().into(mWallpaperImgView);
            return true;
        } else {
            return false;
        }
    }
}
