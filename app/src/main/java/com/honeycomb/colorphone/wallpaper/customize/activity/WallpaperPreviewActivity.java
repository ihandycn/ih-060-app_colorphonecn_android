package com.honeycomb.colorphone.wallpaper.customize.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.widget.AppCompatDrawableManager;
import android.support.v7.widget.AppCompatImageView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.honeycomb.colorphone.wallpaper.Manager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.honeycomb.colorphone.wallpaper.LauncherAnalytics;
import com.honeycomb.colorphone.view.GlideApp;
import com.honeycomb.colorphone.view.GlideRequest;
import com.honeycomb.colorphone.util.ActivityUtils;
import com.honeycomb.colorphone.util.Thunk;
 import com.honeycomb.colorphone.BuildConfig;
 import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.util.Utils;
import com.honeycomb.colorphone.wallpaper.ad.AdAnalytics;
import com.honeycomb.colorphone.wallpaper.ad.AdPlacements;
import com.honeycomb.colorphone.wallpaper.animation.LauncherAnimUtils;
import com.honeycomb.colorphone.wallpaper.customize.CategoryInfo;
import com.honeycomb.colorphone.wallpaper.customize.CustomizeConfig;
import com.honeycomb.colorphone.wallpaper.customize.WallpaperInfo;
import com.honeycomb.colorphone.wallpaper.customize.WallpaperMgr;
import com.honeycomb.colorphone.wallpaper.customize.activity.report.SelectReportReasonActivity;
import com.honeycomb.colorphone.wallpaper.customize.view.PreviewViewPage;
import com.honeycomb.colorphone.wallpaper.customize.wallpaper.WallpaperUtils;
import com.honeycomb.colorphone.wallpaper.customize.wallpaperpackage.WallpaperPackageCoverView;
import com.honeycomb.colorphone.wallpaper.customize.wallpaperpackage.WallpaperPackageInfo;
import com.honeycomb.colorphone.wallpaper.livewallpaper.LiveWallpaperConsts;
import com.honeycomb.colorphone.wallpaper.model.LauncherFiles;
import com.honeycomb.colorphone.wallpaper.util.CommonUtils;
import com.honeycomb.colorphone.wallpaper.util.ViewUtils;
import com.honeycomb.colorphone.wallpaper.view.PopupView;
import com.honeycomb.colorphone.wallpaper.view.ProgressDialog;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.superapps.util.Dimensions;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;
import com.superapps.util.Toasts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import hugo.weaving.DebugLog;

public class WallpaperPreviewActivity extends WallpaperBaseActivity
        implements OnPageChangeListener, PreviewViewPage.PreviewPageListener, OnClickListener,
        INotificationObserver {

    private static final String TAG = WallpaperPreviewActivity.class.getSimpleName();
    private static final boolean DEBUG = true && BuildConfig.DEBUG;

    public static final String INTENT_KEY_SCENARIO = "scenario";
    public static final String INTENT_KEY_CATEGORY = "category";
    public static final String INTENT_KEY_WALLPAPERS = "wallpapers";
    public static final String INTENT_KEY_INDEX = "index";
    public static final String INTENT_KEY_WALLPAPER_DATA = "wallpaperData";
    public static final String INTENT_KEY_WALLPAPER_PACKAGE_INFO = "wallpaperPackageInfo";
    @SuppressWarnings("PointlessBooleanExpression")
    private static final String PREF_KEY_PREVIEW_GUIDE_SHOWN = "wallpaper_preview_guide_shown";
    private static final String PREF_KEY_PREVIEW_WALLPAPER_SHOWN_MODE = "pref_key_preview_wallpaper_shown_mode";

    private final static String FULL_SCREEN = "FULL_SCREEN";
    private final static String FULL_IMAGE = "FULL_IMAGE";

    private static final int MODE_GALLERY_WALLPAPER = 0;
    private static final int MODE_LOCAL_WALLPAPER = 1;

    private final static int TOP_MARGIN = Dimensions.pxFromDp(15);

    private static final long IMAGE_ZOOM_DURATION = 230;

    private boolean mInitialized;
    private boolean mIsGuide;
    private boolean mIsOnLineWallpaper;
    private int mPaperIndex;
    private int mWallpaperMode = MODE_LOCAL_WALLPAPER;
    float sumPositionAndPositionOffset;

    @Thunk
    ViewPager mViewPager;
    private List<Object> mWallpapers = new ArrayList<>();
    private WallpaperInfo mCurrentWallpaper;
    private TextView mSetWallpaperButton;
    private ProgressDialog mDialog;
    private View mEdit;
    private View mMenu;
    private View mReturnArrow;
    private View mAdCloseBtn;
    private PopupView mMenuPopupView;
    private PreviewViewPagerAdapter mAdapter;
    private ScrollEventLogger mScrollEventLogger = new ScrollEventLogger();
    private SparseBooleanArray mLoadMap = new SparseBooleanArray();

    //selectZoomBtn true zoom_out 缩小 mIsCenterCrop state true
    //selectZoomBtn false zoom_in 放大 mIsCenterCrop state false
    private ImageView mZoomBtn;
    private boolean mIsCenterCrop = false;

    private CategoryInfo mCategoryInfo;
    private WallpaperPackageInfo mWallpaperPackageInfo;

    //ad related
    private static final int MAX_CONCURRENT_AD_REQUEST_COUNT = 3;
    private boolean mShouldShowAds = false;
    private int mAdStep = 5;
    private int mCurrentRequestCount;
    private int mStartIndex = 0;
    private int mLastAdIndex = -1;
    private int mMaxVisiblePosition;
    private boolean mDestroying = false;
    private ValueAnimator mPackageGuideLeftAnimator;
    private ValueAnimator mPackageGuideRightAnimator;
    private boolean mIsGuideInterrupted = false;

    private ValueAnimator mZoomAnimator;

    public static Intent getLaunchIntent(Context context,
                                         WallpaperMgr.Scenario scenario,
                                         CategoryInfo category, int position, Intent data) {
        Intent intent = new Intent(context, WallpaperPreviewActivity.class);
        intent.putExtra(WallpaperPreviewActivity.INTENT_KEY_SCENARIO, scenario.ordinal());
        intent.putExtra(WallpaperPreviewActivity.INTENT_KEY_CATEGORY, category);
        intent.putExtra(WallpaperPreviewActivity.INTENT_KEY_INDEX, position);
        intent.putExtra(WallpaperPreviewActivity.INTENT_KEY_WALLPAPER_DATA, data);
        return intent;
    }

    @DebugLog
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper_preview);
        initView();
        mIsCenterCrop = HSPreferenceHelper.getDefault().getBoolean(PREF_KEY_PREVIEW_WALLPAPER_SHOWN_MODE, mIsCenterCrop);
        if (mService != null) {
            if (initData()) {
                refreshButtonState();
            } else {
                finish();
                return;
            }
        }

        mShouldShowAds = CustomizeConfig.getBoolean(false, "customizeNativeAds", "WallpaperPreview", "AdSwitch");
        if (mShouldShowAds) {
            mAdStep = CustomizeConfig.getInteger(4, "customizeNativeAds", "WallpaperPreview", "AdStep");
            mStartIndex = CustomizeConfig.getInteger(2, "customizeNativeAds", "WallpaperPreview", "StartIndex");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(INTENT_KEY_INDEX, mPaperIndex);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mPaperIndex = savedInstanceState.getInt(INTENT_KEY_INDEX);
    }

    @SuppressWarnings("RestrictedApi")
    private void initView() {
        mReturnArrow = findViewById(R.id.wallpaper_view_return);
        mReturnArrow.setOnClickListener(this);
        mReturnArrow.setBackgroundResource(R.drawable.moment_round_material_compat_dark);
        mEdit = findViewById(R.id.preview_edit_btn);
        mEdit.setOnClickListener(this);
        mEdit.setBackgroundResource(R.drawable.moment_round_material_compat_dark);
        ((AppCompatImageView) mEdit).setImageDrawable(AppCompatDrawableManager.get().getDrawable(HSApplication.getContext(), R.drawable.wallpaper_edit_svg));
        mSetWallpaperButton = (TextView) findViewById(R.id.set_wallpaper_button);
        mSetWallpaperButton.setOnClickListener(this);
        mMenu = findViewById(R.id.preview_menu_btn);
        mMenu.setOnClickListener(this);
        mMenu.setBackgroundResource(R.drawable.moment_round_material_compat_dark);


        mMenuPopupView = new PopupView(this);
        View reportContainer = LayoutInflater.from(this).inflate(R.layout.layout_wallpaper_preview_menu_settings, findViewById(R.id.container), false);

        mMenuPopupView.setOutSideBackgroundColor(Color.TRANSPARENT);
        mMenuPopupView.setContentView(reportContainer);
        mMenuPopupView.setOutSideClickListener((View view) -> {
            mMenuPopupView.dismiss();
        });
        TextView report = reportContainer.findViewById(R.id.tv_report);
        report.setOnClickListener((View view) -> {
            mMenuPopupView.dismiss();
            Intent reportIntent = new Intent(WallpaperPreviewActivity.this, SelectReportReasonActivity.class);
            reportIntent.putExtra(SelectReportReasonActivity.INTENT_KEY_WALLPAPER_URL, mCurrentWallpaper.getThumbnailUrl());
            startActivity(reportIntent);
            overridePendingTransition(R.anim.app_lock_slide_in_bottom, 0);
        });

        mZoomBtn = (ImageView) findViewById(R.id.preview_zoom_btn);
        mZoomBtn.setOnClickListener(this);
        mZoomBtn.setBackgroundResource(R.drawable.moment_round_material_compat_dark);

        mAdCloseBtn = findViewById(R.id.preview_ad_close_btn);

        selectZoomBtn(!mIsCenterCrop);

        if (mIsOnLineWallpaper) {
            mZoomBtn.setVisibility(View.VISIBLE);
        } else {
            mZoomBtn.setVisibility(View.INVISIBLE);
        }

        mViewPager = (ViewPager) findViewById(R.id.preview_view_pager);
        mAdapter = new PreviewViewPagerAdapter();
        mViewPager.setAdapter(mAdapter);
        mViewPager.setFocusable(true);
        mViewPager.setClickable(true);
        mViewPager.setLongClickable(true);
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN && mPackageGuideLeftAnimator != null) {
                    mIsGuideInterrupted = true;
                }
                return false;
            }
        });
    }

    @SuppressWarnings("RestrictedApi")
    private void selectZoomBtn(boolean isSelected) {
        if (isSelected) {
            mZoomBtn.setImageDrawable(AppCompatDrawableManager.get().getDrawable(HSApplication.getContext(), R.drawable.wallpaper_zoom_out_svg));
        } else {
            mZoomBtn.setImageDrawable(AppCompatDrawableManager.get().getDrawable(HSApplication.getContext(), R.drawable.wallpaper_zoom_in_svg));
        }
    }

    private void setViewsVisibility(int visibility) {
        mSetWallpaperButton.setVisibility(visibility);
        mZoomBtn.setVisibility(visibility);
        mEdit.setVisibility(visibility);
        mMenu.setVisibility(visibility);
        View draw = ViewUtils.findViewById(this, R.id.preview_guide_draw_view);
        if (draw != null) {
            draw.setVisibility(visibility);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mIsOnLineWallpaper) {
            HSPreferenceHelper.getDefault().putBoolean(PREF_KEY_PREVIEW_WALLPAPER_SHOWN_MODE, mIsCenterCrop);
        }
        if (mDialog != null && !ActivityUtils.isDestroyed(this)) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDestroying = true;

        if (mPackageGuideLeftAnimator != null) mPackageGuideLeftAnimator.cancel();
        if (mPackageGuideRightAnimator != null) mPackageGuideRightAnimator.cancel();

        HSGlobalNotificationCenter.removeObserver(this);
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (mPackageGuideLeftAnimator == null && positionOffset != 0) {
            mIsGuideInterrupted = true;
        }
        if (position + positionOffset > sumPositionAndPositionOffset) {
            mScrollEventLogger.prepareLeft();
        } else if (position != 0 || positionOffset != 0 || sumPositionAndPositionOffset != 0 || positionOffsetPixels != 0) {
            mScrollEventLogger.prepareRight();
        }
        sumPositionAndPositionOffset = position + positionOffset;
    }

    @Override
    public void onPageSelected(int position) {
        String current_category_name = Preferences.get(LauncherFiles.CUSTOMIZE_PREFS).getString("current_category_name", "Food");
        Manager.getInstance().getDelegate().logEvent("Wallpaper_PreviewPage_Show", "ClassName", current_category_name,
                "Form", "Slide");
        int index = position;
        if (mWallpaperPackageInfo != null) {
            if (position == 0) {
                setViewsVisibility(View.GONE);
                findViewById(R.id.wallpaper_view_return).setVisibility(View.VISIBLE);
                mAdCloseBtn.setVisibility(View.GONE);
                setCurrentWallpaper(null);
                return;
            } else {
                index = position - 1;
            }
        }

        setViewsVisibility(View.VISIBLE);
        mReturnArrow.setVisibility(View.VISIBLE);
        mAdCloseBtn.setVisibility(View.GONE);
        if (mIsOnLineWallpaper && mWallpaperPackageInfo == null) {
            LauncherAnalytics.logEvent("Wallpaper_Preview_Shown");
        }
        mPaperIndex = index;
        setCurrentWallpaper((WallpaperInfo) getWallpaperInfoByIndex(mPaperIndex));
        refreshButtonState();

        if (mInitialized && mWallpaperMode == MODE_LOCAL_WALLPAPER) {
            WallpaperMgr.getInstance().getSharedPreferences().putBoolean(PREF_KEY_PREVIEW_GUIDE_SHOWN, true);
        }
        mScrollEventLogger.tryLogScrollLeftEvent();
        mScrollEventLogger.tryLogScrollRightEvent();
    }

    protected void refreshButtonState() {
        if (mCurrentWallpaper == null) {
            return;
        }
        mEdit.setVisibility(View.VISIBLE);
        mMenu.setVisibility(View.VISIBLE);
        if (isSucceed()) {
            mEdit.setAlpha(1f);
            mEdit.setClickable(true);
            mZoomBtn.setAlpha(1f);
            mZoomBtn.setClickable(true);
            mMenu.setAlpha(1f);
            mMenu.setClickable(true);
        } else {
            mEdit.setAlpha(0.5f);
            mEdit.setClickable(false);
            mZoomBtn.setAlpha(0.5f);
            mZoomBtn.setClickable(false);
            mMenu.setAlpha(0.5f);
            mMenu.setClickable(false);
        }
        selectZoomBtn(!mIsCenterCrop);
        if (mIsOnLineWallpaper) {
            mZoomBtn.setVisibility(View.VISIBLE);
        } else {
            mZoomBtn.setVisibility(View.INVISIBLE);
        }
        mSetWallpaperButton.setVisibility(View.VISIBLE);
        mSetWallpaperButton.setText(R.string.online_wallpaper_apply_btn);
        boolean isWallpaperReady = mCurrentWallpaper.getType() == WallpaperInfo.WALLPAPER_TYPE_BUILT_IN
                || isSucceed() && !isSettingWallpaper();

        if (isWallpaperReady) {
            if (mIsGuide) {
                mIsGuide = false;
                startGuideAnimation(WallpaperPreviewActivity.this, R.id.preview_guide_draw_view
                        , Dimensions.pxFromDp(300), Dimensions.pxFromDp(200));
            }
            mSetWallpaperButton.setTextColor(0xffffffff);
        } else {
            mSetWallpaperButton.setTextColor(0x80ffffff);
        }
    }

    private boolean initData() {
        WallpaperMgr.Scenario scenario = WallpaperMgr.Scenario.valueOfOrdinal(
                getIntent().getIntExtra(INTENT_KEY_SCENARIO, 0));
        mCategoryInfo = getIntent().getParcelableExtra(INTENT_KEY_CATEGORY);
        mPaperIndex = getIntent().getIntExtra(INTENT_KEY_INDEX, 0);
        switch (scenario) {
            case ONLINE_NEW:
            case ONLINE_CATEGORY:
                ArrayList<Parcelable> wallpapers = getIntent()
                        .getParcelableArrayListExtra(INTENT_KEY_WALLPAPERS);
                if (wallpapers == null) {
                    return false;
                }
                mWallpapers.clear();
                mWallpapers.addAll(wallpapers);
                mIsOnLineWallpaper = true;
                break;
            case LOCAL:
                if (mPaperIndex < 0) {
                    mWallpaperMode = MODE_GALLERY_WALLPAPER;
                    mPaperIndex = 0;
                    if (ActivityUtils.isDestroyed(this)) return false;
                    mDialog = ProgressDialog.createDialog(this, getString(R.string.wallpaper_setting_progress_dialog_text));
                    mDialog.show();
                    Threads.postOnThreadPoolExecutor(new Runnable() {
                        @Override
                        public void run() {
                            Uri selectedImage = ((Intent) (getIntent().getParcelableExtra(INTENT_KEY_WALLPAPER_DATA))).getData();

                            InputStream imageStream;
                            byte[] type = new byte[4];
                            try {
                                imageStream = getContentResolver().openInputStream(selectedImage);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                                HSLog.d(TAG, "local wallpaper image file not found");
                                showError(0);
                                return;
                            }
                            try {
                                //noinspection ConstantConditions
                                int size = imageStream.read(type);
                                if (size != 4) {
                                    throw new IOException("Cannot get 4 bytes file header.");
                                }
                            } catch (IOException | NullPointerException e) {
                                e.printStackTrace();
                                showError(0);
                                return;
                            }

                            if (isGif(type)) {
                                HSLog.d(TAG, "local wallpaper image file is gif");
                                showError(R.string.local_wallpaper_pick_error_gif_not_supported);
                                return;
                            }

                            String fileName = Utils.md5(selectedImage.toString() + "-" + System.currentTimeMillis());
                            File storedWallpaper = new File(CommonUtils.getDirectory(LiveWallpaperConsts.Files.LOCAL_DIRECTORY), fileName);

                            if (!com.honeycomb.colorphone.wallpaper.util.Utils.saveInputStreamToFile(type, imageStream, storedWallpaper)) {
                                HSLog.d(TAG, "local wallpaper file save failed");
                                showError(0);
                                return;
                            }

                            final String storedPath = storedWallpaper.getAbsolutePath();
                            final WallpaperInfo wallpaperInfo = WallpaperInfo.newGalleryWallpaper(storedPath);
                            Threads.postOnMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (mDialog != null && mDialog.isShowing() && !ActivityUtils.isDestroyed(WallpaperPreviewActivity.this)) {
                                        mDialog.dismiss();
                                    }
                                    mWallpapers = new ArrayList<>();
                                    mWallpapers.add(0, wallpaperInfo);
                                    prepareData();
                                }
                            });
                        }
                    });
                } else {
                    mWallpaperMode = MODE_LOCAL_WALLPAPER;
                    mWallpapers.clear();
                    mWallpapers.addAll(WallpaperMgr.getInstance().getLocalWallpapers());
                    LauncherAnalytics.logEvent("Personalize_PersonalCenter_Wallpaper_Thumbnail_Clicked");
                }
                break;
            case PACKAGE:
                mWallpaperPackageInfo = getIntent().getParcelableExtra(INTENT_KEY_WALLPAPER_PACKAGE_INFO);
                mWallpapers.addAll(mWallpaperPackageInfo.mWallpaperList);
                mIsOnLineWallpaper = true;

                performPackageGuideAnimation();
                break;
        }

        if (mWallpaperMode == MODE_LOCAL_WALLPAPER) {
            if (mWallpapers.isEmpty()) {
                HSLog.e(TAG, "Wallpaper not prepared when launch WallpaperPreviewActivity, finish");
                finish();
                return false;
            }
            prepareData();
        }
        mInitialized = true;
        return true;
    }

    private void performPackageGuideAnimation() {
        if (Preferences.get(LauncherFiles.CUSTOMIZE_PREFS).getBoolean(WallpaperMgr.PREF_KEY_PACKAGE_GUIDE_SHOWN, false)) {
            return;
        }
        Preferences.get(LauncherFiles.CUSTOMIZE_PREFS).putBoolean(WallpaperMgr.PREF_KEY_PACKAGE_GUIDE_SHOWN, true);

        Handler handler = new Handler();
        Runnable animatorRunnable = () -> {
            if (mIsGuideInterrupted) return;

            HSGlobalNotificationCenter.sendNotification(WallpaperPackageCoverView.EVENT_PACKAGE_GUIDE_START);
            mPackageGuideLeftAnimator = ValueAnimator.ofFloat(0, 250);
            mPackageGuideLeftAnimator.addUpdateListener(animation -> {
                if (mIsGuideInterrupted) return;
                float trans = (float) animation.getAnimatedValue();
                mViewPager.scrollTo((int) trans, 0);
            });
            mPackageGuideLeftAnimator.setInterpolator(new DecelerateInterpolator());
            mPackageGuideLeftAnimator.setDuration(400);
            mPackageGuideLeftAnimator.setStartDelay(350);
            mPackageGuideLeftAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    handler.postDelayed(() -> {
                        HSGlobalNotificationCenter.sendNotification(WallpaperPackageCoverView.EVENT_PACKAGE_GUIDE_END);

                        if (mIsGuideInterrupted) return;
                        mPackageGuideRightAnimator = ValueAnimator.ofFloat(250, 0);
                        mPackageGuideRightAnimator.addUpdateListener(animation1 -> {
                            if (mIsGuideInterrupted) return;
                            float trans = (float) animation1.getAnimatedValue();
                            mViewPager.scrollTo((int) trans, 0);
                        });
                        mPackageGuideRightAnimator.setInterpolator(new LinearInterpolator());
                        mPackageGuideRightAnimator.setDuration(210);
                        mPackageGuideRightAnimator.setStartDelay(200);
                        mPackageGuideRightAnimator.start();
                    }, 200);
                }
            });
            mPackageGuideLeftAnimator.start();
        };
        handler.postDelayed(animatorRunnable, 1350);
        handler.postDelayed(animatorRunnable, 2950);
    }

    private boolean isGif(byte[] header) {
        StringBuilder stringBuilder = new StringBuilder();
        if (header == null || header.length <= 0) {
            return false;
        }
        for (byte aByte : header) {
            int v = aByte & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        // 47494638 is hex number for string "gif"
        return stringBuilder.toString().toUpperCase().contains("47494638");
    }

    private void showError(@StringRes final int messageId) {
        Threads.postOnMainThread(new Runnable() {
            @SuppressLint("ResourceType")
            @Override
            public void run() {
                if (mDialog != null && mDialog.isShowing() && !ActivityUtils.isDestroyed(WallpaperPreviewActivity.this)) {
                    mDialog.dismiss();
                }
                if (messageId > 0) {
                    Toasts.showToast(messageId);
                } else {
                    Toasts.showToast(R.string.local_wallpaper_pick_error);
                }
                finish();
            }
        });
    }

    private void prepareData() {
        setCurrentWallpaper((WallpaperInfo) getWallpaperInfoByIndex(mPaperIndex));
        mAdapter.notifyDataSetChanged();
        mViewPager.setCurrentItem(mPaperIndex, false);
        if (mPaperIndex == 0) {
            // Manually invoke this as view pager will not do it for us in such case
            onPageSelected(0);
        }

        if (mWallpaperMode == MODE_LOCAL_WALLPAPER &&
                !WallpaperMgr.getInstance().getSharedPreferences().getBoolean(PREF_KEY_PREVIEW_GUIDE_SHOWN, false)) {
            mIsGuide = true;
        }
    }

    private Object getWallpaperInfoByIndex(int index) {
        if (mWallpapers.size() == 0) {
            return null;
        } else if (index < 0) {
            return mWallpapers.get(0);
        } else if (index >= mWallpapers.size()) {
            return mWallpapers.get(mWallpapers.size() - 1);
        } else {
            return mWallpapers.get(index);
        }
    }

    private void setCurrentWallpaper(WallpaperInfo wallpaper) {
        mCurrentWallpaper = wallpaper;
        if (wallpaper != null) {
            HSLog.i("WallpaperUrl", "Current wallpaper: " + wallpaper.getSource()
                    + ", thumb: " + wallpaper.getThumbnailUrl());
        }
    }

    private void displayPage(int index, PreviewViewPage page) {
        final ImageView imageView = page.largeWallpaperImageView;
        page.retryLayout.setVisibility(View.INVISIBLE);
        WallpaperInfo info = (WallpaperInfo) getWallpaperInfoByIndex(index);
        Uri uri = null;
        String thumbUrl = null;
        switch (info.getType()) {
            case WallpaperInfo.WALLPAPER_TYPE_BUILT_IN:
                if (DEBUG) HSLog.d(TAG, "Display built-in wallpaper: " + index);
                page.loadingView.setVisibility(View.INVISIBLE);
                uri = Uri.parse("android.resource://" + getPackageName() + "/" + info.getBuiltInDrawableId());
                break;
            case WallpaperInfo.WALLPAPER_TYPE_ONLINE:
                uri = Uri.parse(info.getSource());
                thumbUrl = info.getThumbnailUrl();
                break;
            case WallpaperInfo.WALLPAPER_TYPE_GALLERY:
                File file = new File(info.getPath());
                uri = Uri.fromFile(file);
                break;
            case WallpaperInfo.WALLPAPER_TYPE_LUCKY:
                File luckyFile = new File(info.getPath());
                if (luckyFile.exists()) {
                    uri = Uri.fromFile(luckyFile);
                } else {
                    uri = Uri.parse(info.getSource());
                }
                break;
        }
        if (uri == null) {
            return;
        }

        GlideRequest thumbRequest = GlideApp.with(this).asBitmap().load(thumbUrl);
        thumbRequest.listener(new RequestListener<Bitmap>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap bitmap, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                imageView.setImageBitmap(bitmap);
                PreviewViewPage page = (PreviewViewPage) imageView.getTag(R.id.wallpaper_position);
                page.width = bitmap.getWidth();
                page.height = bitmap.getHeight();

                if (info.textLightUnkown()) {
                    info.setTextLight(WallpaperUtils.textColorLightForWallPaper(bitmap));
                }

                if (mIsCenterCrop && mIsOnLineWallpaper) {
                    imageView.setImageMatrix(WallpaperUtils.centerCrop(bitmap.getWidth(), bitmap.getHeight(),
                            imageView));
                } else {
                    if (mEdit.getBottom() == 0 || mSetWallpaperButton.getTop() == 0) {
                        imageView.setImageMatrix(WallpaperUtils.centerInside(bitmap.getWidth(), bitmap.getHeight(),
                                Dimensions.pxFromDp(80) + TOP_MARGIN, getResources().getDisplayMetrics().heightPixels - Dimensions.pxFromDp(68) - TOP_MARGIN));
                    } else {
                        imageView.setImageMatrix(WallpaperUtils.centerInside(bitmap.getWidth(), bitmap.getHeight(),
                                mEdit.getBottom() + TOP_MARGIN, mSetWallpaperButton.getTop() - TOP_MARGIN));
                    }
                }
                return true;
            }
        }).diskCacheStrategy(DiskCacheStrategy.DATA);

        Glide.with(WallpaperPreviewActivity.this).asBitmap().load(uri).thumbnail(thumbRequest).into(new CustomImageLoadingTarget(imageView));
    }

    @Override
    protected WallpaperInfo getCurrentWallpaper() {
        return mCurrentWallpaper;
    }

    private void animate(ImageView imageView, Matrix oldMatrix, Matrix newMatrix) {
        long duration = IMAGE_ZOOM_DURATION;
        ValueAnimator oldAnimator = mZoomAnimator;
        if (oldAnimator != null && oldAnimator.isRunning()) {
            duration = (long) (oldAnimator.getAnimatedFraction() * oldAnimator.getDuration());
            oldAnimator.cancel();
        }
        mZoomAnimator = ObjectAnimator.ofObject(imageView, "imageMatrix",
                new MatrixEvaluator(), oldMatrix, newMatrix);
        mZoomAnimator.setDuration(duration);
        mZoomAnimator.setInterpolator(LauncherAnimUtils.DECELERATE_QUAD);
        mZoomAnimator.start();
    }

    private static class MatrixEvaluator implements TypeEvaluator<Matrix> {
        private float[] mStartVal = new float[9];
        private float[] mEndVal = new float[9];
        private float[] mInterVal = new float[9];

        @Override
        public Matrix evaluate(float fraction, Matrix start, Matrix end) {
            start.getValues(mStartVal);
            end.getValues(mEndVal);
            for (int i = 0; i < 9; i++) {
                mInterVal[i] = (1f - fraction) * mStartVal[i] + fraction * mEndVal[i];
            }
            Matrix inter = new Matrix();
            inter.setValues(mInterVal);
            return inter;
        }
    }

    @Override
    public void onClick(View v) {
        int i1 = v.getId();
        if (i1 == R.id.wallpaper_view_return) {
            finish();
        } else if (i1 == R.id.preview_menu_btn) {
            mMenuPopupView.showAsDropDown(mMenu, Dimensions.pxFromDp(-11f), Dimensions.pxFromDp(1f));
        } else if (i1 == R.id.preview_zoom_btn) {
            for (int i = 0; i < mViewPager.getChildCount(); i++) {
                if (!(mViewPager.getChildAt(i) instanceof PreviewViewPage)) {
                    continue;
                }
                PreviewViewPage page = (PreviewViewPage) mViewPager.getChildAt(i);
                Matrix matrix;
                if (mIsCenterCrop) {
                    matrix = WallpaperUtils.centerInside(page.width, page.height,
                            mEdit.getBottom() + TOP_MARGIN, mSetWallpaperButton.getTop() - TOP_MARGIN);
                } else {
                    matrix = WallpaperUtils.centerCrop(page.width, page.height, page.largeWallpaperImageView);
                }
                if ((int) page.getTag() == mPaperIndex) {
                    animate(page.largeWallpaperImageView, page.largeWallpaperImageView.getImageMatrix(), matrix);
                } else {
                    page.largeWallpaperImageView.setImageMatrix(matrix);
                }
            }

            selectZoomBtn(mIsCenterCrop);
            LauncherAnalytics.logEvent("Wallpaper_Preview_Zoom_Icon_Clicked");
            mIsCenterCrop = !mIsCenterCrop;
        } else if (i1 == R.id.preview_edit_btn) {
            if (mCurrentWallpaper == null) {
                return;
            }
            LauncherAnalytics.logEvent("Wallpaper_Preview_Edit_Icon_Clicked");
            Intent intent = WallpaperEditActivity.getLaunchIntent(this, mCurrentWallpaper);
            startActivity(intent);
            HSGlobalNotificationCenter.addObserver(
                    WallpaperEditActivity.NOTIFICATION_WALLPAPER_APPLIED_FROM_EDIT, this);
        } else if (i1 == R.id.set_wallpaper_button) {
            if (mCurrentWallpaper == null) {
                return;
            }

            boolean isWallpaperReady = mCurrentWallpaper.getType() == WallpaperInfo.WALLPAPER_TYPE_BUILT_IN
                    || isSucceed() && !isSettingWallpaper();
            if (!isWallpaperReady) {
                Toasts.showToast(R.string.online_wallpaper_loading);
                return;
            }

            if (mWallpaperPackageInfo == null && mCurrentWallpaper.getType() == WallpaperInfo.WALLPAPER_TYPE_ONLINE) {
                LauncherAnalytics.logEvent("Wallpaper_Preview_SetAsWallpaper_Btn_Clicked", "Type", mCurrentWallpaper.getSource());
            }
            mSetWallpaperButton.setTextColor(0x80ffffff);
            mSetWallpaperButton.setClickable(false);
            mCurrentWallpaper.setEdit("");
            mCurrentWallpaper.setApplied(true);
            if (mCurrentWallpaper.getCategory() == null) {
                mCurrentWallpaper.setCategory(mCategoryInfo);
            }
            applyWallpaper(mCurrentWallpaper.getType() != WallpaperInfo.WALLPAPER_TYPE_GALLERY, false);
            Preferences preferences = Preferences.get(LauncherFiles.CUSTOMIZE_PREFS);
            String string = preferences.getString("current_category_name", "Food");
            Manager.getInstance().getDelegate().logEvent("Wallpaper_Set_Success", "ClassName", string, "SettingMode", "Rolling");
        }
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        if (WallpaperEditActivity.NOTIFICATION_WALLPAPER_APPLIED_FROM_EDIT.equals(s)) {
            finish();
        }
    }

    @Override
    protected Bitmap tryGetWallpaperToSet() {
        if (mCurrentWallpaper == null) {
            return null;
        }
        Bitmap wallpaper = null;
        switch (mCurrentWallpaper.getType()) {
            case WallpaperInfo.WALLPAPER_TYPE_BUILT_IN:
                wallpaper = BitmapFactory.decodeResource(getResources(), mCurrentWallpaper.getBuiltInDrawableId());
                break;
            case WallpaperInfo.WALLPAPER_TYPE_ONLINE:
            case WallpaperInfo.WALLPAPER_TYPE_LUCKY:
                for (int i = 0; i < mViewPager.getChildCount(); i++) {
                    if (!(mViewPager.getChildAt(i) instanceof PreviewViewPage)) {
                        continue;
                    }
                    PreviewViewPage page = getPreviewPage(i);
                    if (page != null && (int) page.getTag() == mPaperIndex) {
                        BitmapDrawable drawable = ((BitmapDrawable) page.largeWallpaperImageView.getDrawable());
                        if (drawable != null) {
                            wallpaper = drawable.getBitmap();
                        }
                        break;
                    }
                }
                break;
            case WallpaperInfo.WALLPAPER_TYPE_GALLERY:
                for (int i = 0; i < mViewPager.getChildCount(); i++) {
                    PreviewViewPage page = getPreviewPage(i);
                    if (page != null && (int) page.getTag() == mPaperIndex) {
                        wallpaper = ((BitmapDrawable) page.largeWallpaperImageView.getDrawable()).getBitmap();
                        wallpaper = WallpaperUtils.centerInside(wallpaper);
                        break;
                    }
                }
                break;
        }
        return wallpaper;
    }

    private @Nullable
    PreviewViewPage getPreviewPage(int index) {
        View view = mViewPager.getChildAt(index);
        if (view instanceof PreviewViewPage) {
            return (PreviewViewPage) view;
        }
        return null;
    }

    @Override
    public void onRetryButtonPressed(PreviewViewPage page) {
        int index = (int) (page.getTag());
        displayPage(index, page);
    }

    private PreviewViewPage getPreviewPage(ViewGroup view, int paperIndex) {
        PreviewViewPage page = (PreviewViewPage) this.getLayoutInflater().inflate(R.layout.item_wallpaper_page, view, false);
        page.setListener(this);
        page.setTag(paperIndex);
        page.largeWallpaperImageView.setTag(R.id.wallpaper_position, page);
        return page;
    }

    public class PreviewViewPagerAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            if (null == mWallpapers) {
                return 0;
            }
            if (mWallpaperPackageInfo != null) {
                return mWallpapers.size() > 0 ? mWallpapers.size() + 1 : 0;
            } else {
                return mWallpapers.size();
            }
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup view, int position) {
            int index = position;
            if (mWallpaperPackageInfo != null) {
                if (position == 0) {
                    WallpaperPackageCoverView coverView = (WallpaperPackageCoverView) getLayoutInflater().inflate(R.layout.wallpaper_package_cover, view, false);
                    coverView.setPackageInfo(mWallpaperPackageInfo);
                    view.addView(coverView, 0);
                    return coverView;
                }
                index = position - 1;
            }

            mMaxVisiblePosition = Math.max(mMaxVisiblePosition, index);


            if (mShouldShowAds
                    && mCurrentRequestCount < MAX_CONCURRENT_AD_REQUEST_COUNT
                    && position > mLastAdIndex
                    && position % (mAdStep + 1) == 1) {
                loadAds();
            }

            PreviewViewPage page = getPreviewPage(view, index);
            displayPage(index, page);
            view.addView(page, 0);
            return page;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
        }

        private void loadAds() {
        }

        void logAppViewEvents(boolean adShown) {
            AdAnalytics.logAppViewEvent(AdPlacements.WALLPAPER_NATIVE_AD_PLACEMENT_NAME, adShown);
            LauncherAnalytics.logEvent("ThemeAndWallpaperAdAnalysis",
                    "ad_show_from", "WallpaperDetail_" + adShown);
        }

    }

    private class ScrollEventLogger {
        private boolean mLeftEnabled;
        private boolean mRightEnabled;

        void prepareLeft() {
            mLeftEnabled = true;
            mRightEnabled = false;
        }

        void prepareRight() {
            mLeftEnabled = false;
            mRightEnabled = true;
        }

        void reset() {
            mLeftEnabled = false;
            mRightEnabled = false;
        }

        void tryLogScrollLeftEvent() {
            if (mLeftEnabled) {
                if (mWallpaperPackageInfo != null) {
                } else {
                    if (mIsOnLineWallpaper) {
                        String label = (mIsCenterCrop ? FULL_SCREEN : FULL_IMAGE) + "";
                        LauncherAnalytics.logEvent("Wallpaper_Preview_Slided", "type", label);
                    }
                }
                reset();
            }
        }

        void tryLogScrollRightEvent() {
            reset();
        }
    }

    private boolean isSucceed() {
        return mLoadMap.get(mPaperIndex);
    }

    private class CustomImageLoadingTarget extends ImageViewTarget<Bitmap> {

        private boolean mShouldLogEvent;

        public CustomImageLoadingTarget(ImageView view) {
            super(view);
        }

        @Override
        public void onLoadStarted(Drawable placeholder) {
            super.onLoadStarted(placeholder);
            if (view == null) {
                return;
            }
            PreviewViewPage page = (PreviewViewPage) view.getTag(R.id.wallpaper_position);
            page.loadingView.setVisibility(View.VISIBLE);
            page.retryLayout.setVisibility(View.INVISIBLE);
            mLoadMap.put((int) (page.getTag()), false);
            mShouldLogEvent = true;
        }

        @Override
        public void onLoadFailed(Drawable errorDrawable) {
            super.onLoadFailed(errorDrawable);
            if (view == null) {
                return;
            }
            final PreviewViewPage page = (PreviewViewPage) view.getTag(R.id.wallpaper_position);
            mLoadMap.put((int) (page.getTag()), false);
            page.postDelayed(new Runnable() {
                @Override
                public void run() {
                    page.loadingView.setVisibility(View.INVISIBLE);
                    page.retryLayout.setVisibility(View.VISIBLE);
                    page.largeWallpaperImageView.setImageResource(android.R.color.transparent);
                    mSetWallpaperButton.setVisibility(View.INVISIBLE);
                }
            }, 600);
            if (mShouldLogEvent) {
                mShouldLogEvent = false;
            }
            if (mIsOnLineWallpaper && mWallpaperPackageInfo == null) {
                LauncherAnalytics.logEvent("Wallpaper_Preview_LoadResult", "Type", "Failed");
            }
        }

        @Override
        public void onResourceReady(Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
            if (view == null) {
                return;
            }
            view.setImageBitmap(bitmap);
            PreviewViewPage page = (PreviewViewPage) view.getTag(R.id.wallpaper_position);
            page.width = bitmap.getWidth();
            page.height = bitmap.getHeight();

            WallpaperInfo info = (WallpaperInfo) getWallpaperInfoByIndex((int) (page.getTag()));
            info.setTextLight(WallpaperUtils.textColorLightForWallPaper(bitmap));

            if (mIsCenterCrop && mIsOnLineWallpaper) {
                ((ImageView) view).setImageMatrix(WallpaperUtils.centerCrop(bitmap.getWidth(), bitmap.getHeight(), (ImageView) view));
            } else {
                if (mEdit.getBottom() == 0 || mSetWallpaperButton.getTop() == 0) {
                    ((ImageView) view).setImageMatrix(WallpaperUtils.centerInside(bitmap.getWidth(), bitmap.getHeight(),
                            Dimensions.pxFromDp(80) + TOP_MARGIN, getResources().getDisplayMetrics().heightPixels - Dimensions.pxFromDp(68) - TOP_MARGIN));
                } else {
                    ((ImageView) view).setImageMatrix(WallpaperUtils.centerInside(bitmap.getWidth(), bitmap.getHeight(),
                            mEdit.getBottom() + TOP_MARGIN, mSetWallpaperButton.getTop() - TOP_MARGIN));
                }
            }
            mLoadMap.put((int) (page.getTag()), true);
            refreshButtonState();
            page.loadingView.setVisibility(View.INVISIBLE);
            page.retryLayout.setVisibility(View.INVISIBLE);
            if (mShouldLogEvent) {
                mShouldLogEvent = false;
            }
            if (mIsOnLineWallpaper && mWallpaperPackageInfo == null) {
                LauncherAnalytics.logEvent("Wallpaper_Preview_LoadResult", "Type", "Success");
            }
        }

        @Override
        protected void setResource(Bitmap bitmap) {

        }
    }
}
