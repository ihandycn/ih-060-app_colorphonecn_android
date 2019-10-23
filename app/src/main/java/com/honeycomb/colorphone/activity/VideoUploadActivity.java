package com.honeycomb.colorphone.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.VideoView;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.http.HttpManager;
import com.honeycomb.colorphone.http.lib.call.Callable;
import com.honeycomb.colorphone.http.lib.upload.UploadFileCallback;
import com.honeycomb.colorphone.ugc.VideoUtils;
import com.honeycomb.colorphone.util.Utils;
import com.honeycomb.colorphone.view.UploadProcessView;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;
import com.superapps.util.Threads;
import com.superapps.util.Toasts;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import okhttp3.ResponseBody;

public class VideoUploadActivity extends HSAppCompatActivity implements View.OnClickListener, MediaPlayer.OnCompletionListener {

    public static final String KEY_VIDEO_INFORMATION = "key_for_video_information";

    private View mSetNameDialog;

    private VideoView mVideoView;
    private VideoUtils.VideoInfo mVideoInfo;

    private View mPause;
    private UploadProcessView mUpload;

    private EditText mName;
    private View mOk;
    private View mClose;
    private View mCancel;

    private Callable<ResponseBody> mUploadCall = null;


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

        mCancel = findViewById(R.id.cancel);
        mPause = findViewById(R.id.pause_button);
        mSetNameDialog = findViewById(R.id.set_name);
        mUpload = findViewById(R.id.upload_button);
        mUpload.setOnClickListener(view -> showSetNameDialog());
        mUpload.getChildAt(0).setOnClickListener(view -> showSetNameDialog());
        mCancel.setOnClickListener(view -> cancel());
        mCancel.setBackground(BackgroundDrawables.createBackgroundDrawable(0xff5a587a, Dimensions.pxFromDp(100f), true));
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
        if (mSetNameDialog != null && mSetNameDialog.getVisibility() == View.VISIBLE) {
            mSetNameDialog.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("unused")
    private void showSetNameDialog() {
        View content = mSetNameDialog.findViewById(R.id.bg);
        content.setBackground(BackgroundDrawables.createBackgroundDrawable(0xffffffff, Dimensions.pxFromDp(16), false));
        mOk = mSetNameDialog.findViewById(R.id.ok_btn);
        mOk.setBackground(BackgroundDrawables.createBackgroundDrawable(0xff6c63ff, Dimensions.pxFromDp(21), true));

        mClose = mSetNameDialog.findViewById(R.id.close);
        mName = mSetNameDialog.findViewById(R.id.edit_name);
        mOk.setOnClickListener(view -> {
            Editable text = mName.getText();
            if (text == null || text.length() == 0) {
                Toasts.showToast(R.string.please_input_name);
                return;
            }
            mSetNameDialog.setVisibility(View.GONE);
            String name = text.toString();
            upload(name);

        });
        mClose.setOnClickListener(view -> mSetNameDialog.setVisibility(View.GONE));

        mSetNameDialog.setVisibility(View.VISIBLE);
        mName.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                InputMethodManager manager = ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE));
                if (manager != null)
                    manager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            } else {
                InputMethodManager manager = ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE));
                if (manager != null)
                    manager.showSoftInput(view, 0);
            }
        });
        mName.requestFocus();

    }

    private boolean mConvertFailed;
    private String jpegName;
    private String mp3;

    private void upload(String name) {
        mConvertFailed = false;

        jpegName = getCacheDir().getAbsolutePath() + File.separator + System.currentTimeMillis() + ".jpeg";
        mp3 = getCacheDir().getAbsolutePath() + File.separator + System.currentTimeMillis() + ".mp3";
        final CountDownLatch begin = new CountDownLatch(1);
        final CountDownLatch end = new CountDownLatch(2);
        new Thread() {
            @Override
            public void run() {
                try {
                    begin.await();
                    Bitmap thumbnail = VideoUtils.createVideoThumbnail(mVideoInfo.data, MediaStore.Video.Thumbnails.MINI_KIND);
                    if (thumbnail != null) {
                        thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(jpegName));
                    } else {
                        mConvertFailed = true;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    mConvertFailed = true;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    mConvertFailed = true;
                } finally {
                    end.countDown();
                }
            }
        }.start();
        new Thread() {
            @Override
            public void run() {
                try {
                    begin.await();
                    VideoUtils.doExtractAudioFromVideo(mVideoInfo.data, mp3, -1, -1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    mConvertFailed = true;
                } catch (IOException e) {
                    e.printStackTrace();
                    mConvertFailed = true;
                } finally {
                    end.countDown();
                }
            }
        }.start();

        new Thread() {
            @Override
            public void run() {
                try {
                    begin.countDown();
                    mUpload.setText(R.string.convert_ing);
                    mUpload.setEnabled(false);
                    end.await();

                    Threads.postOnMainThread(() -> mCancel.setVisibility(View.VISIBLE));

                    if (!mConvertFailed) {
                        String videoFilePath = mVideoInfo.data;
                        mUploadCall = HttpManager.getInstance().uploadVideos(videoFilePath, mp3, jpegName, name, new UploadFileCallback() {
                            @Override
                            public void onSuccess() {
                                success();
                            }

                            @Override
                            public void onUpload(long length, long current, boolean isDone) {
                                String progress = (int) (current / (float) length * 100) + "%";
                                mUpload.setText(getString(R.string.upload_ing, progress));
                                mUpload.setProcess(current * 1.0f / length);
                                HSLog.e("upload", "oUpload: length = " + length + ", current = " + current + ", progress = " + progress + ", isDone = " + isDone);
                            }

                            @Override
                            public void onFailure(String errorMsg) {
                                failure(errorMsg);
                            }
                        });
                    } else {
                        failure("convert failed");
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void deleteTempFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }

    private void cancel() {
        if (mUploadCall != null) {
            mUploadCall.cancel();
        }

        failure("");
    }

    private void success() {
        mUpload.setText(getString(R.string.upload_ing, "100%"));
        mUpload.setProcess(1);
        mUpload.setEnabled(true);
        mCancel.setVisibility(View.GONE);
        mUpload.setVisibility(View.GONE);
        deleteTempFile(mp3);
        deleteTempFile(jpegName);
    }

    private void failure(String errorMsg) {
        mUpload.setText(getString(R.string.upload));
        mUpload.setProcess(1);
        mCancel.setVisibility(View.GONE);
        mUpload.setEnabled(true);
        deleteTempFile(mp3);
        deleteTempFile(jpegName);
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
