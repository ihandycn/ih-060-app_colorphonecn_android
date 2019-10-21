package com.honeycomb.colorphone.wallpaper.customize.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.honeycomb.colorphone.LauncherAnalytics;
 import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.util.Utils;
import com.honeycomb.colorphone.wallpaper.customize.WallpaperInfo;
import com.honeycomb.colorphone.wallpaper.customize.WallpaperMgr;
import com.honeycomb.colorphone.wallpaper.customize.view.ProgressDialog;
import com.honeycomb.colorphone.wallpaper.livewallpaper.LiveWallpaperConsts;
import com.honeycomb.colorphone.wallpaper.util.CommonUtils;
import com.honeycomb.colorphone.wallpaper.util.ViewUtils;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Navigations;
import com.superapps.util.Threads;
import com.superapps.util.Toasts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class UploadWallpaperActivity extends BaseCustomizeActivity {
    private static final int REQUEST_CODE_UPLOAD_WALLPAPER = 1;

    private TextView tvDeclaration;
    private ImageView ivUploadWallpaper;

    private boolean hasSelectedImage = false;
    private WallpaperInfo mCurrentWallpaper;
    private ProgressDialog mProgressDialog;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.wallpaper_upload);

        Toolbar toolbar = (Toolbar) findViewById(R.id.action_bar);
        // Title
        toolbar.setTitle(getString(R.string.upload_wallpaper));
        toolbar.setTitleTextColor(0xff4d4d4d);
        toolbar.setBackgroundColor(0xffffffff);
        setSupportActionBar(toolbar);
        if (CommonUtils.ATLEAST_LOLLIPOP) {
            getSupportActionBar().setElevation(0);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        tvDeclaration = ViewUtils.findViewById(this, R.id.tv_declaration);
        SpannableString declaration = new SpannableString(getString(R.string.upload_wallpaper_declaration));
        SpannableString termsOfService = new SpannableString(getString(R.string.terms_of_service));

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override public void onClick(View widget) {
                Navigations.openBrowser(UploadWallpaperActivity.this,
                        HSConfig.optString("", "Application", "TermsOfServiceURL"));
            }
        };
        termsOfService.setSpan(clickableSpan, 0, termsOfService.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvDeclaration.setText(TextUtils.concat(declaration, " ", termsOfService, "."));
        tvDeclaration.getPaint().linkColor = 0xff448aff;
        tvDeclaration.setMovementMethod(LinkMovementMethod.getInstance());

        ivUploadWallpaper = ViewUtils.findViewById(this, R.id.iv_upload_wallpaper);
        findViewById(R.id.upload_image_container).setOnClickListener((v) -> {
            LauncherAnalytics.logEvent("Wallpaper_Userupload_AddWallpaper_Clicked");
            Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickIntent.setType("image/*");
            Intent chooserIntent = Intent.createChooser(pickIntent, "Select Image");
            Navigations.startActivityForResultSafely(UploadWallpaperActivity.this, chooserIntent, REQUEST_CODE_UPLOAD_WALLPAPER);
        });

        findViewById(R.id.upload_wallpaper).setEnabled(false);
        findViewById(R.id.upload_wallpaper).setOnClickListener((v) -> {
            if (!hasSelectedImage || mCurrentWallpaper == null) {
                return;
            }
            LauncherAnalytics.logEvent("Wallpaper_Userupload_Btn_Clicked");
            mProgressDialog = ProgressDialog.createDialog(this, "Uploading...");
            mProgressDialog.show();
            mProgressDialog.setCancelable(false);
            new Handler().postDelayed(() -> {
                mProgressDialog.setOnDismissListener(dialog -> {
                    WallpaperMgr.getInstance().addLocalWallpaperSync(mCurrentWallpaper);
                    HSGlobalNotificationCenter.sendNotification(WallpaperMgr.NOTIFICATION_WALLPAPER_GALLERY_SAVED);
                    Toasts.showToast(R.string.upload_wallpaper_success);
                    finish();
                });
                mProgressDialog.dismiss(false);
            }, 1200);
        });
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_UPLOAD_WALLPAPER
                && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Toasts.showToast(R.string.local_wallpaper_pick_error);
                return;
            }

            Uri selectedImage = data.getData();
            InputStream imageStream;
            byte[] type = new byte[4];
            try {
                imageStream = getContentResolver().openInputStream(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                showError(0);
                errorState("local wallpaper image file not found");
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

            if (!com.honeycomb.colorphone.wallpaper.util.Utils.saveInputStreamToFile(type, imageStream, storedWallpaper)) {
                showError(0);
                errorState("local wallpaper file save failed");
                return;
            }

            mCurrentWallpaper = WallpaperInfo.newWallpaper(WallpaperInfo.WALLPAPER_TYPE_GALLERY,
                    storedWallpaper.getAbsolutePath(), selectedImage.getPath());
            String url = Uri.fromFile(storedWallpaper).toString();
            Glide.with(UploadWallpaperActivity.this).load(url).into(ivUploadWallpaper);
            findViewById(R.id.upload_image_container).setClickable(false);
            findViewById(R.id.upload_wallpaper).setEnabled(true);
            findViewById(R.id.add_wallpaper_container).setVisibility(View.INVISIBLE);
            hasSelectedImage = true;
        }
    }

    private void showError(@StringRes final int messageId) {
        Threads.postOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (messageId > 0) {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
