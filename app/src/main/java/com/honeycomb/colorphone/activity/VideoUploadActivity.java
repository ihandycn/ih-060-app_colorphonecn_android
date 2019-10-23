package com.honeycomb.colorphone.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.VideoView;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.ugc.VideoUtils;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;

import org.jetbrains.annotations.NotNull;

public class VideoUploadActivity extends HSAppCompatActivity implements View.OnClickListener, MediaPlayer.OnCompletionListener {

    public static final String KEY_VIDEO_INFORMATION = "key_for_video_information";

    private View mRuleDialog;
    private boolean mRuleDialogShowing = false;

    private VideoView mVideoView;
    private VideoUtils.VideoInfo mVideoInfo;

    private View mPause;


    public static void start(Context context, VideoUtils.VideoInfo info) {
        Intent intent = new Intent(context, VideoUploadActivity.class);
        intent.putExtra(KEY_VIDEO_INFORMATION, info);
        Navigations.startActivitySafely(context, intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activitiy_video_upload);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");

        AppCompatActivity activity = this;

        activity.setSupportActionBar(toolbar);
        final Drawable upArrow = ContextCompat.getDrawable(activity, R.drawable.back_dark);
        upArrow.setColorFilter(ContextCompat.getColor(activity, R.color.colorPrimaryReverse), PorterDuff.Mode.SRC_ATOP);
        activity.getSupportActionBar().setHomeAsUpIndicator(upArrow);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setDisplayShowHomeEnabled(true);

        Utils.applyFontForToolbarTitle(activity, toolbar);

        int statusBarHeight = Dimensions.getStatusBarHeight(this);
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
        layoutParams.topMargin = statusBarHeight;

        mVideoInfo = (VideoUtils.VideoInfo) getIntent().getSerializableExtra(KEY_VIDEO_INFORMATION);
        mVideoView = findViewById(R.id.video_view);
        mVideoView.setVideoPath(mVideoInfo.data);
        mVideoView.setOnClickListener(this);
        mVideoView.setOnCompletionListener(this);

        mPause = findViewById(R.id.pause_button);

        /*mRuleDialog = findViewById(R.id.upload_rule_dialog);
        int phoneHeight = Dimensions.getPhoneHeight(this);
        int statusBarHeight = Dimensions.getStatusBarHeight(this);
        int top = (phoneHeight - statusBarHeight - Dimensions.pxFromDp(271f)) / 2;
        View upload_rule_image_popup = mRuleDialog.findViewById(R.id.upload_rule_image_popup);
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) upload_rule_image_popup.getLayoutParams();
        layoutParams.topMargin = (int) (top - layoutParams.height / 553f * 198);
        upload_rule_image_popup.requestLayout();

        Preferences.getDefault().doOnce(() -> Threads.postOnMainThread(VideoUploadActivity.this::showConfirmDialog),"VideoListActivity showConfirmDialog");*/
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mRuleDialog != null && mRuleDialog.getVisibility() == View.VISIBLE) {
            mRuleDialog.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }



    @SuppressWarnings("unused")
    private void showConfirmDialog() {

        View content = mRuleDialog.findViewById(R.id.bg);
        content.setBackground(BackgroundDrawables.createBackgroundDrawable(0xffffffff, Dimensions.pxFromDp(16), false));
        View iKnow = mRuleDialog.findViewById(R.id.i_know);
        iKnow.setBackground(BackgroundDrawables.createBackgroundDrawable(0xff6c63ff, Dimensions.pxFromDp(21), true));

        iKnow.setOnClickListener(view -> {

        });

        mRuleDialog.setVisibility(View.VISIBLE);
        mRuleDialogShowing = true;

    }

    @Override
    protected void onResume() {
        super.onResume();
        mVideoView.start();
        mPause.setVisibility(View.GONE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVideoView.pause();
        mPause.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVideoView.stopPlayback();
    }

    @Override
    public void onClick(View view) {
        if (mVideoView.isPlaying()) {
            mVideoView.pause();
            mPause.setVisibility(View.VISIBLE);
        } else {
            mVideoView.start();
            mPause.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        mPause.setVisibility(View.VISIBLE);
    }
}
