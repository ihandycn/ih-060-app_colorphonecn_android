package com.acb.libwallpaper.live.customize.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.acb.libwallpaper.live.LauncherAnalytics;
import com.acb.libwallpaper.live.WallpaperAnalytics;
import com.honeycomb.colorphone.view.GlideApp;
import com.acb.libwallpaper.live.livewallpaper.LiveWallpaperConsts;
import com.acb.libwallpaper.live.util.ActivityUtils;
import com.acb.libwallpaper.live.util.CommonUtils;
import com.acb.libwallpaper.live.util.ViewUtils;
import com.acb.libwallpaper.R;
import com.acb.libwallpaper.live.customize.WallpaperInfo;
import com.acb.libwallpaper.live.customize.WallpaperMgr;
import com.acb.libwallpaper.live.customize.crop.CropImageOptions;
import com.acb.libwallpaper.live.customize.crop.CropOverlayView;
import com.acb.libwallpaper.live.customize.wallpaper.WallpaperUtils;
import com.acb.libwallpaper.live.dialog.CustomAlertActivity;
import com.acb.libwallpaper.live.util.Utils;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Threads;
import com.superapps.util.Toasts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import hugo.weaving.DebugLog;

public class WallpaperEditActivity extends WallpaperBaseActivity implements View.OnClickListener {

    public static final String INTENT_KEY_WALLPAPER_INFO = "wallpaper_info";
    private static final String TAG = WallpaperEditActivity.class.getSimpleName();
    public static final String INTENT_KEY_WALLPAPER_URI = "wallpaperData";

    static final String NOTIFICATION_WALLPAPER_APPLIED_FROM_EDIT = "wallpaper_applied_from_edit";

    private static final int REQUEST_CODE_SYSTEM_THEME_ALERT = 1;

    private int mBitmapHeight;
    private int mBitmapWidth;

    private ImageView mWallpaperView;
    private TextView mApplyButton;
    private float[] mMatrixValues = new float[9];
    private float mStartX = 0;
    private float mStartY = 0;
    private float mWidth = 0;
    private float mHeight = 0;
    private Bitmap mBitmap;
    private RectF mOverlayBounds;
    private CropOverlayView mCropOverlayView;
    private boolean mIsScrollable;

    private View mFixedBtn;
    private View mScrollBtn;
    private View mResetBtn;
    private Matrix mCurrentMatrix;
    private boolean mIsFromLocalGallery = false;


    public static Intent getLaunchIntent(Context context, WallpaperInfo wallpaperInfo) {
        Intent intent = new Intent(context, WallpaperEditActivity.class);
        intent.putExtra(WallpaperEditActivity.INTENT_KEY_WALLPAPER_INFO, wallpaperInfo);
        return intent;
    }

    public static Intent getLaunchIntent(Context context, Intent uriIntent) {
        Intent intent = new Intent(context, WallpaperEditActivity.class);
        intent.putExtra(WallpaperEditActivity.INTENT_KEY_WALLPAPER_URI, uriIntent);
        return intent;
    }

    @DebugLog
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper_edit_new);
        mWallpaperView = ViewUtils.findViewById(this, R.id.wallpaper_edit_image);
        mCropOverlayView = ViewUtils.findViewById(this, R.id.wallpaper_overlay_view);
        mApplyButton = ViewUtils.findViewById(this, R.id.wallpaper_edit_apply_button);
        mFixedBtn = ViewUtils.findViewById(this, R.id.wallpaper_edit_fixed);
        mScrollBtn = ViewUtils.findViewById(this, R.id.wallpaper_edit_scroll);
        mResetBtn = ViewUtils.findViewById(this, R.id.wallpaper_edit_reset_button);
        findViewById(R.id.wallpaper_view_return).setOnClickListener(this);
        init();
    }

    private void bindEvents() {
        mResetBtn.setOnClickListener(this);
        mFixedBtn.setOnClickListener(this);
        mScrollBtn.setOnClickListener(this);
        mApplyButton.setOnClickListener(this);
        mCropOverlayView.setCropWindowChangeListener(new CropOverlayView.CropWindowChangeListener() {
            @Override
            public void onCropWindowChanged(boolean b) {
                if (b) {
                    showResetBtn();
                }
            }
        });
    }

    private void showError(@StringRes final int messageId) {
        Threads.postOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (mDialog != null && mDialog.isShowing() && !ActivityUtils.isDestroyed(WallpaperEditActivity.this)) {
                    mDialog.dismiss();
                }
                if (messageId != 0) {
                    Toasts.showToast(messageId);
                } else {
                    Toasts.showToast(R.string.local_wallpaper_pick_error);
                }
                finish();
            }
        });
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

    private void errorState(String msg) {
        HSLog.w(msg);
        finish();
    }

    private void init() {
        Intent intent = getIntent();
        if (intent.getExtras() == null) {
            errorState("intent.getExtras() == null");
            return;
        }
        if (getIntent().getExtras().containsKey(INTENT_KEY_WALLPAPER_URI)) {
            loadFromGallery();
            mIsScrollable = false;
            mIsFromLocalGallery = true;
        } else if (intent.getExtras().containsKey(INTENT_KEY_WALLPAPER_INFO)) {
            mCurrentWallpaper = getIntent().getParcelableExtra(INTENT_KEY_WALLPAPER_INFO);
            mIsFromLocalGallery = false;
            initData();
            mIsScrollable = TextUtils.isEmpty(mCurrentWallpaper.getPath());
        }
        mFixedBtn.setSelected(!mIsScrollable);
        mScrollBtn.setSelected(mIsScrollable);
        hideResetBtn();
    }

    private void loadFromGallery() {
        Threads.postOnThreadPoolExecutor(new Runnable() {
            @Override
            public void run() {
                Uri selectedImage = ((Intent) (getIntent().getParcelableExtra(INTENT_KEY_WALLPAPER_URI))).getData();
                if (selectedImage == null) {
                    showError(0);
                    errorState("wallpaper uri not passed correctly");
                    return;
                }
                List<WallpaperInfo> wallpaperInfos = WallpaperMgr.getInstance().getLocalWallpapers();
                final String path = selectedImage.getPath();
                for (WallpaperInfo wallpaperInfo : wallpaperInfos) {
                    if (TextUtils.equals(wallpaperInfo.getSource(), path)) {
                        mCurrentWallpaper = wallpaperInfo;
                        Threads.postOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                initData();
                            }
                        });
                        return;
                    }
                }

                InputStream imageStream;
                byte[] type = new byte[4];
                try {
                    imageStream = getContentResolver().openInputStream(selectedImage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    showError(0);
                    errorState("local wallpaper image file not found");
                    return;
                } catch (SecurityException e) {
                    e.printStackTrace();
                    showError(0);
                    errorState("cannot read local wallpaper image file due to device security model");
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    showError(0);
                    errorState("other errors when opening local wallpaper image file");
                    return;
                }
                try {
                    //noinspection ConstantConditions
                    int size = imageStream.read(type);
                    if (size != 4) {
                        errorState("local wallpaper Cannot get 4 bytes file header.");
                        throw new IOException("Cannot get 4 bytes file header.");
                    }
                } catch (IOException | NullPointerException e) {
                    e.printStackTrace();
                    showError(0);
                    errorState("local wallpaper IOException | NullPointerException e");
                    return;
                }

                if (isGif(type)) {
                    showError(R.string.local_wallpaper_pick_error_gif_not_supported);
                    errorState("local wallpaper image file is gif");
                    return;
                }

                String fileName = Utils.md5(selectedImage.toString() + "-" + System.currentTimeMillis());
                File storedWallpaper = new File(CommonUtils.getDirectory(LiveWallpaperConsts.Files.LOCAL_DIRECTORY), fileName);

                if (!Utils.saveInputStreamToFile(type, imageStream, storedWallpaper)) {
                    showError(0);
                    errorState("local wallpaper file save failed");
                    return;
                }
                final String storedPath = storedWallpaper.getAbsolutePath();
                Threads.postOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mDialog != null && mDialog.isShowing() && !ActivityUtils.isDestroyed(WallpaperEditActivity.this)) {
                            mDialog.dismiss();
                        }
                        if (ActivityUtils.isDestroyed(WallpaperEditActivity.this)) {
                            return;
                        }
                        mCurrentWallpaper = WallpaperInfo.newWallpaper(WallpaperInfo.WALLPAPER_TYPE_GALLERY, storedPath, path);
                        initData();
                    }
                });
            }
        });

    }

    private void initData() {
        HSLog.i("wallpaper type " + mCurrentWallpaper.getType());
        String url;
        if (mCurrentWallpaper.getType() == WallpaperInfo.WALLPAPER_TYPE_LUCKY) {
            File luckyFile = new File(mCurrentWallpaper.getPath());
            if (luckyFile.exists()) {
                url = Uri.fromFile(luckyFile).toString();
            } else {
                url = mCurrentWallpaper.getSource();
            }
        } else if (mCurrentWallpaper.getType() == WallpaperInfo.WALLPAPER_TYPE_ONLINE) {
            url = mCurrentWallpaper.getSource();
        } else if (mCurrentWallpaper.getType() == WallpaperInfo.WALLPAPER_TYPE_GALLERY) {
            url = Uri.fromFile(new File(mCurrentWallpaper.getPath())).toString();
        } else if (mCurrentWallpaper.getType() == WallpaperInfo.WALLPAPER_TYPE_BUILT_IN) {
            url = "android.resource://" + getPackageName() + "/" + mCurrentWallpaper.getBuiltInDrawableId();
        } else {
            errorState("not allowed wallpaperType " + mCurrentWallpaper.getType());
            finish();
            return;
        }
        mWallpaperView.setEnabled(false);
        GlideApp.with(WallpaperEditActivity.this).asBitmap().load(url).into(new ImageViewTarget<Bitmap>(mWallpaperView) {

            @Override
            public void onResourceReady(Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                mWallpaperView.setImageBitmap(resource);
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadBitmapComplete(view, resource);
                    }
                }, 10);
            }

            @Override
            protected void setResource(@Nullable Bitmap resource) {

            }

            @Override
            public void onLoadFailed(Drawable errorDrawable) {
                super.onLoadFailed(errorDrawable);
                if (mDialog != null && !ActivityUtils.isDestroyed(WallpaperEditActivity.this)) {
                    mDialog.dismiss();
                }
                HSLog.w(TAG, "Load local image failed");
                finish();
            }

            @Override
            public void onLoadCleared(Drawable placeholder) {
                super.onLoadCleared(placeholder);
                if (mDialog != null && !ActivityUtils.isDestroyed(WallpaperEditActivity.this)) {
                    mDialog.dismiss();
                }
                HSLog.w(TAG, "Load local image cancelled, finish");
                finish();
            }
        });
        refreshButtonState();
    }

    private void loadBitmapComplete(View view, Bitmap bitmap) {
        HSLog.i("onLoadingComplete " + " " + bitmap + " " + view.getWidth() + " " + view.getMeasuredWidth());
        mBitmap = bitmap;
        int vWidth = mWallpaperView.getWidth() == 0 ? mWallpaperView.getMeasuredWidth() : mWallpaperView.getWidth();
        int vHeight = mWallpaperView.getHeight() == 0 ? mWallpaperView.getMeasuredHeight() : mWallpaperView.getHeight();
        mCropOverlayView.setVisibility(View.VISIBLE);
        centerInside();
        updateOverlayView();
        mWallpaperView.setEnabled(true);
        bindEvents();
    }

    private void centerInside() {
        mBitmapWidth = mBitmap.getWidth();
        mBitmapHeight = mBitmap.getHeight();
        mOverlayBounds = new RectF(0, 0, mWallpaperView.getWidth(), mWallpaperView.getHeight());
        RectF bitmapRect = new RectF(0, 0, mBitmapWidth, mBitmapHeight);
        mCurrentMatrix = new Matrix();
        mCurrentMatrix.setRectToRect(bitmapRect, mOverlayBounds, Matrix.ScaleToFit.CENTER);
        mCurrentMatrix.mapRect(mOverlayBounds, bitmapRect);
        mWallpaperView.setImageMatrix(mCurrentMatrix);
        initOverlayView();
        HSLog.i(mOverlayBounds + "");
    }

    private void initOverlayView() {
        //初始化配置
        mCropOverlayView.setInitialAttributeValues(new CropImageOptions());
        //GuideLine显示状态
        mCropOverlayView.setGuidelines(CropImageOptions.Guidelines.OFF);
        //Crop 形状
        mCropOverlayView.setCropShape(CropImageOptions.CropShape.RECTANGLE);
        //是否多点触控
        mCropOverlayView.setMultiTouchEnabled(true);

    }

    private void updateOverlayViewBounds() {
        float[] bounds = new float[8];
        mapRectToPoint(mOverlayBounds, bounds);
        mCropOverlayView.setBounds(bounds, (int) mOverlayBounds.right, (int) mOverlayBounds.bottom);
        mCropOverlayView.setCropWindowLimits(mOverlayBounds.right, mOverlayBounds.bottom, 2, 2);
    }

    private void updateOverlayView() {
        updateOverlayViewBounds();
        PointF ratio = getRatio();
        HSLog.i("ratio x = " + ratio.x + " y = " + ratio.y);
        if (mIsScrollable) {
            mCropOverlayView.setAspectRatio((int) ratio.x, (int) ratio.y);
            mCropOverlayView.setCropWindowRect(getOverlayViewRect());
            mCropOverlayView.invalidate();
        } else {
            mCropOverlayView.setAspectRatio((int) ratio.x, (int) ratio.y);
            mCropOverlayView.setCropWindowRect(getOverlayViewRect());
            mCropOverlayView.invalidate();
        }
    }

    private RectF getOverlayViewRect() {
        PointF point = getRatio();
        float k = point.x / point.y;
        float width = mOverlayBounds.width();
        float height = mOverlayBounds.height();
        float overlayHeight;
        float overlayWidth;
        if (mIsScrollable) {
            if (width > height * k) {
                overlayHeight = height;
                overlayWidth = overlayHeight * k;
            } else {
                overlayWidth = width;
                overlayHeight = width / k;
            }
        } else {
            if (height > width / k) {
                overlayWidth = width;
                overlayHeight = width / k;
            } else {
                overlayHeight = height;
                overlayWidth = height * k;
            }
        }
        RectF overlayRect = new RectF(0, 0, overlayWidth, overlayHeight);
        HSLog.i("overlayRect " + overlayRect);
        Matrix matrix = new Matrix();
        matrix.setRectToRect(overlayRect, mOverlayBounds, Matrix.ScaleToFit.CENTER);
        matrix.mapRect(overlayRect);
        return overlayRect;
    }

    private static void mapRectToPoint(RectF rect, float[] points) {
        points[0] = rect.left;
        points[1] = rect.top;
        points[2] = rect.right;
        points[3] = rect.top;
        points[4] = rect.right;
        points[5] = rect.bottom;
        points[6] = rect.left;
        points[7] = rect.bottom;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.wallpaper_edit_reset_button) {
            LauncherAnalytics.logEvent("Wallpaper_Edit_Reset_Icon_Clicked");
            reset();
        } else if (i == R.id.wallpaper_edit_apply_button) {
            apply();
        } else if (i == R.id.wallpaper_view_return) {
            cancel();
        } else if (i == R.id.wallpaper_edit_fixed) {
            mIsScrollable = false;
            reset();
        } else if (i == R.id.wallpaper_edit_scroll) {
            mIsScrollable = true;
            reset();
        }
    }

    private void calculateCropParams(Matrix matrix) {
        RectF viewRect = new RectF(0, 0, mBitmapWidth, mBitmapHeight);
        matrix.mapRect(viewRect);
        RectF cropOverlayRectF = mCropOverlayView.getCropWindowRect();
        matrix.getValues(mMatrixValues);
        float scale = (float) Math.sqrt(mMatrixValues[Matrix.MSCALE_X] * mMatrixValues[Matrix.MSCALE_Y] -
                mMatrixValues[Matrix.MSKEW_X] * mMatrixValues[Matrix.MSKEW_Y]);
        //Anticlockwise rotation
        if (mMatrixValues[Matrix.MSCALE_X] > 0) {
            //0try
            mStartX = Math.abs(cropOverlayRectF.left - viewRect.left) / scale;
            mStartY = Math.abs(cropOverlayRectF.top - viewRect.top) / scale;

            mWidth = cropOverlayRectF.width() / scale;
            mHeight = cropOverlayRectF.height() / scale;
        } else if (mMatrixValues[Matrix.MSCALE_X] < 0) {
            // 180
            mStartX = Math.abs(cropOverlayRectF.right - viewRect.right) / scale;
            mStartY = Math.abs(cropOverlayRectF.bottom - viewRect.bottom) / scale;

            mWidth = cropOverlayRectF.width() / scale;
            mHeight = cropOverlayRectF.height() / scale;
        } else if (mMatrixValues[Matrix.MSKEW_X] > 0) {
            //90
            mStartX = Math.abs(cropOverlayRectF.bottom - viewRect.bottom) / scale;
            mStartY = Math.abs(cropOverlayRectF.left - viewRect.left) / scale;

            mWidth = cropOverlayRectF.height() / scale;
            mHeight = cropOverlayRectF.width() / scale;
        } else if (mMatrixValues[Matrix.MSKEW_X] < 0) {
            // 270
            mStartX = Math.abs(cropOverlayRectF.top - viewRect.top) / scale;
            mStartY = Math.abs(cropOverlayRectF.right - viewRect.right) / scale;

            mWidth = cropOverlayRectF.height() / scale;
            mHeight = cropOverlayRectF.width() / scale;
        }
    }

    private void apply() {
        if (mCurrentWallpaper == null) {
            return;
        }
        mApplyButton.setTextColor(0x80ffffff);
        mApplyButton.setClickable(false);

        String edit = getEdit();
        mCurrentWallpaper.setEdit(edit);
        mCurrentWallpaper.onEdit();
        mCurrentWallpaper.setApplied(true);
        applyWallpaper(mIsScrollable);

        WallpaperAnalytics.logEvent("Wallpaper_Set_Success", "SettingMode", mIsScrollable ? "Rolling" : "Fixed");

        HSGlobalNotificationCenter.sendNotification(NOTIFICATION_WALLPAPER_APPLIED_FROM_EDIT);
    }

    private void cancel() {
        finish();
    }

    public void reset() {

        // resetImage(mWallpaperView, true);
        // refreshButtonState();
        mFixedBtn.setSelected(!mIsScrollable);
        mScrollBtn.setSelected(mIsScrollable);
        updateOverlayView();
        hideResetBtn();
    }

    private void hideResetBtn() {
        mResetBtn.setVisibility(View.INVISIBLE);
    }

    private void showResetBtn() {
        mResetBtn.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case WallpaperEditActivity.REQUEST_CODE_SYSTEM_THEME_ALERT:
                int alertResult = CustomAlertActivity.RESULT_INTENT_VALUE_CANCEL;
                if (data != null) {
                    alertResult = data.getIntExtra(CustomAlertActivity.RESULT_INTENT_KEY_USER_SELECTED_INDEX,
                            CustomAlertActivity.RESULT_INTENT_VALUE_CANCEL);
                }
                if (alertResult == CustomAlertActivity.RESULT_INTENT_VALUE_OK) {
                    finish();
                }
                break;
        }
    }

    @Override
    protected Bitmap tryGetWallpaperToSet() {
        if (mCurrentWallpaper == null) {
            return null;
        }
        calculateCropParams(mWallpaperView.getImageMatrix());
        HSLog.i("mStart X " + mStartX + " mStartY " + mStartY + " mWidth " + mWidth + " mHeight " + mHeight);
        Bitmap bitmap = mBitmap;

        int startX = Math.round(mStartX);
        int startY = Math.round(mStartY);
        int width = Math.round(mWidth);
        int height = Math.round(mHeight);
        if (startX + width > bitmap.getWidth()) {
            // Clamp to avoid out-of-range error due to rounding
            width = bitmap.getWidth() - startX;
        }
        if (startY + height > bitmap.getHeight()) {
            height = bitmap.getHeight() - startY;
        }
        Matrix matrix = new Matrix();
        Bitmap wallpaper;
        try {
            wallpaper = Bitmap.createBitmap(bitmap, startX, startY, width, height, matrix, false);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        HSLog.i("wallpaper width " + wallpaper.getWidth() + " height " + wallpaper.getHeight());
        return wallpaper;
    }

    private String getEdit() {
        StringBuilder sb = new StringBuilder();
        mWallpaperView.getImageMatrix().getValues(mMatrixValues);
        sb.append("").append(mMatrixValues[Matrix.MSCALE_X]).append(",")
                .append(mMatrixValues[Matrix.MSCALE_Y]).append(",")
                .append(mMatrixValues[Matrix.MSKEW_X]).append(",")
                .append(mMatrixValues[Matrix.MSKEW_Y]).append(",")
                .append(mMatrixValues[Matrix.MTRANS_X]).append(",")
                .append(mMatrixValues[Matrix.MTRANS_Y]);
        return sb.toString();
    }

    protected void refreshButtonState() {
        mApplyButton.setClickable(true);
        mApplyButton.setAlpha(1.0f);
    }

    private PointF getRatio() {
        Point point = WallpaperUtils.getWindowSize(this);
        PointF pointF = new PointF(point);
        if (mIsScrollable) {
            pointF.x = pointF.x * 2;
        }
        return pointF;
    }

    @Override
    protected WallpaperInfo getCurrentWallpaper() {
        return mCurrentWallpaper;
    }
}
