package com.honeycomb.colorphone.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Outline;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.ugc.VideoUtils;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.HSNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Calendars;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;
import com.superapps.util.Toasts;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VideoListActivity extends HSAppCompatActivity implements INotificationObserver {
    public static final String KEY_FINISH_VIDEO_LIST = "finish_video_list_activity";

    private View mRuleDialog;

    private RecyclerView mVideoList;
    private VideoPreviewAdapter mAdapter;

    public static void start(Context context) {
        Navigations.startActivity(context, VideoListActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activitiy_video_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.upload_video);

        Utils.configActivityStatusBar(this, toolbar, R.drawable.back_dark);

        mVideoList = findViewById(R.id.video_list);
        GridLayoutManager manager = new GridLayoutManager(this, 3);
        mVideoList.setLayoutManager(manager);
        mVideoList.addItemDecoration(new CustomItemDecoration());

        new Thread() {
            @Override
            public void run() {
                final List<VideoUtils.VideoInfo> videoList = VideoUtils.getVideoList(getApplicationContext());
                runOnUiThread(() -> {
                    mAdapter = new VideoPreviewAdapter(videoList);
                    mVideoList.setAdapter(mAdapter);
                });
            }
        }.start();

        mRuleDialog = findViewById(R.id.upload_rule_dialog);

        Preferences.getDefault().doOnce(() -> Threads.postOnMainThread(VideoListActivity.this::showConfirmDialog),"VideoListActivity showConfirmDialog");

        Analytics.logEvent("Upload_VideoList_Show");
        HSGlobalNotificationCenter.addObserver(KEY_FINISH_VIDEO_LIST, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HSGlobalNotificationCenter.removeObserver(this);
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

    @Override
    protected void onStop() {
        super.onStop();
    }

    @SuppressWarnings("unused")
    private void showConfirmDialog() {

        View content = mRuleDialog.findViewById(R.id.bg);
        content.setBackground(BackgroundDrawables.createBackgroundDrawable(0xffffffff, Dimensions.pxFromDp(16), false));
        View iKnow = mRuleDialog.findViewById(R.id.i_know);
        iKnow.setBackground(BackgroundDrawables.createBackgroundDrawable(0xff6c63ff, Dimensions.pxFromDp(21), true));

        iKnow.setOnClickListener(view -> {
            mRuleDialog.setVisibility(View.GONE);
        });
        mRuleDialog.findViewById(R.id.upload_rule).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigations.startActivitySafely(VideoListActivity.this, AboutActivity.getPrivacyViewIntent(Constants.getUrlUploadRule()));
            }
        });

        content.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (content.getTop() > 0) {
                    content.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    View upload_rule_image_popup = mRuleDialog.findViewById(R.id.upload_rule_image_popup);
                    ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) upload_rule_image_popup.getLayoutParams();
                    layoutParams.topMargin = (int) (content.getTop() - layoutParams.height / 553f * 198);
                    upload_rule_image_popup.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            if (upload_rule_image_popup.getTop() > 100) {
                                upload_rule_image_popup.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                mRuleDialog.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                    upload_rule_image_popup.requestLayout();
                }
            }
        });
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        finish();
    }

    private class VideoPreviewAdapter extends RecyclerView.Adapter<VideoPreviewHolder> implements View.OnClickListener {

        private final ArrayList<VideoUtils.VideoInfo> mVideoInfos = new ArrayList<>();
        private final Map<VideoUtils.VideoInfo, Bitmap> mCache = new HashMap<>();
        private ViewOutlineProvider mOutlineProvider;

        private VideoPreviewAdapter(List<VideoUtils.VideoInfo> infos) {
            mVideoInfos.addAll(infos);
        }

        @NonNull
        @Override
        public VideoPreviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View item = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.video_list_item, parent, false);
            return new VideoPreviewHolder(item);
        }

        @Override
        public void onBindViewHolder(@NonNull VideoPreviewHolder holder, int position) {
            VideoUtils.VideoInfo videoInfo = mVideoInfos.get(position);

            Bitmap bitmap = mCache.get(videoInfo);
            if (bitmap != null) {
                holder.mPreview.setImageBitmap(bitmap);
            } else {
                Threads.postOnThreadPoolExecutor(new Runnable() {
                    @Override
                    public void run() {
                        final Bitmap thumbnail = VideoUtils.createVideoThumbnail(videoInfo.data, MediaStore.Video.Thumbnails.MINI_KIND);
                        mCache.put(videoInfo, thumbnail);
                        Threads.postOnMainThread(() -> {
                            int itemPosition = ((RecyclerView.LayoutParams) holder.itemView.getLayoutParams()).getViewAdapterPosition();
                            if (itemPosition == position) {
                                holder.mPreview.setImageBitmap(thumbnail);
                                mCache.put(videoInfo, thumbnail);
                            }
                        });
                    }
                });
            }

            holder.mDuration.setText(getTimeString(videoInfo.duration));
            holder.itemView.setClipToOutline(true);
            if (mOutlineProvider == null) {
                mOutlineProvider = new ViewOutlineProvider() {
                    @Override
                    public void getOutline(View view, Outline outline) {
                        outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), Dimensions.pxFromDp(6));
                    }
                };
            }
            holder.itemView.setOutlineProvider(mOutlineProvider);

            holder.itemView.setTag(position);
            holder.itemView.setOnClickListener(this);
        }

        @Override
        public int getItemCount() {
            return mVideoInfos.size();
        }

        @Override
        public void onClick(View view) {
            Preferences preferences = Preferences.getDefault();
            int count = preferences.getInt(VideoUploadActivity.KEY_UPLOAD_COUNT, 0);
            long aLong = preferences.getLong(VideoUploadActivity.KEY_UPLOAD_TIME, 0);
            if (count >= 5 && Calendars.isSameDay(aLong, System.currentTimeMillis())) {
                Toasts.showToast("今日上传个数已达上限，请明天再试");
                return;
            }
            int position = (int) view.getTag();
            VideoUtils.VideoInfo videoInfo = mVideoInfos.get(position);
            if (videoInfo.size > 1024 * 1024 * 30) { // 30M
                Toasts.showToast("视频文件过大，请重新选择");
                return;
            }
            Analytics.logEvent("Upload_VideoList_Click");
            VideoUploadActivity.start(VideoListActivity.this, videoInfo);
        }

        private String getTimeString(long ms) {
            long s = ms / 1000;
            long h = (s / (60 * 60));
            long m = s % (60 * 60) / 60;
            long ss = s % 60;
            return getHMSDescription(true, false, h) + getHMSDescription(false, false, m) + getHMSDescription(false, true, ss);
        }

        private String getHMSDescription(boolean isH, boolean isS, long v) {
            if (v == 0) {
                return isH ? "" : (isS ? "00" : "00:");
            }

            if (v >= 10) {
                return v + (isS ? "" : ":");
            } else {
                return "0" + v + (isS ? "" : ":");
            }
        }
    }

    private static class VideoPreviewHolder extends RecyclerView.ViewHolder {
        private ImageView mPreview;
        private TextView mDuration;

        private VideoPreviewHolder(View itemView) {
            super(itemView);
            mPreview = itemView.findViewById(R.id.video_preview);
            mDuration = itemView.findViewById(R.id.video_duration);
        }
    }

    private static class CustomItemDecoration extends RecyclerView.ItemDecoration {
        CustomItemDecoration() {
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int itemPosition = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewAdapterPosition();

            if (itemPosition % 3 == 0) {
                outRect.left = Dimensions.pxFromDp(16);
            } else if (itemPosition % 3 == 2) {
                outRect.right = Dimensions.pxFromDp(16);
            } else {
                outRect.left = Dimensions.pxFromDp(9);
                outRect.right = Dimensions.pxFromDp(9);
            }

            outRect.top =  Dimensions.pxFromDp(8.7f);
        }
    }
}
