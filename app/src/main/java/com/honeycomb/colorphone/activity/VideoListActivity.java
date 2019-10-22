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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.ugc.VideoUtils;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Threads;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class VideoListActivity extends HSAppCompatActivity {

    private static final String TAG = VideoListActivity.class.getSimpleName();

    private View confirmDialog;
    private boolean confirmClose = true;

    private RecyclerView mVideoList;
    private VideoPreviewAdapter mAdapter;

    public static void start(Context context) {
        Intent starter = new Intent(context, VideoListActivity.class);
        context.startActivity(starter);
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
        if (confirmDialog != null && confirmDialog.getVisibility() == View.VISIBLE) {
            confirmDialog.setVisibility(View.GONE);
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
        confirmDialog = findViewById(R.id.close_confirm_dialog);

        View content = confirmDialog.findViewById(R.id.content_layout);
        content.setBackground(BackgroundDrawables.createBackgroundDrawable(0xffffffff, Dimensions.pxFromDp(16), false));

        View btn = confirmDialog.findViewById(R.id.tv_first);
        btn.setBackground(BackgroundDrawables.createBackgroundDrawable(0xff6c63ff, Dimensions.pxFromDp(26), true));
        btn.setOnClickListener(v -> confirmDialog.setVisibility(View.GONE));

        btn = confirmDialog.findViewById(R.id.tv_second);
        btn.setOnClickListener(v -> {
            confirmDialog.setVisibility(View.GONE);
            confirmClose = false;
        });

        confirmDialog.setVisibility(View.VISIBLE);

    }

    private class VideoPreviewAdapter extends RecyclerView.Adapter<VideoPreviewHolder> implements View.OnClickListener {

        private final ArrayList<VideoUtils.VideoInfo> mVideoInfos = new ArrayList<>();
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
            holder.bindData(videoInfo);
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
            int position = (int) view.getTag();
            VideoUtils.VideoInfo videoInfo = mVideoInfos.get(position);
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

        private void bindData(VideoUtils.VideoInfo info) {
            new Thread(){
                @Override
                public void run() {
                    final Bitmap thumbnail = VideoUtils.createVideoThumbnail(info.data, MediaStore.Video.Thumbnails.MINI_KIND);
                    Threads.postOnMainThread(() -> mPreview.setImageBitmap(thumbnail));
                }
            }.start();
            mDuration.setText(getTimeString(info.duration));

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

            if (v > 10) {
                return v + (isS ? "" : ":");
            } else {
                return "0" + v + (isS ? "" : ":");
            }
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
