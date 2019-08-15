package com.colorphone.lock.lockscreen.locker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.honeycomb.colorphone.customize.util.CustomizeUtils;
import com.honeycomb.colorphone.customize.view.TextureVideoView;
import com.honeycomb.colorphone.view.GlideApp;

/**
 * @author sundxing
 */
public class LockerWallpaperView extends FrameLayout {
    public static final int TYPE_VIDEO = 1;
    public static final int TYPE_IMAGE = 2;

    private String mFilePath;
    private int mType;
    private TextureVideoView textureVideoView;
    private ImageView imageView;

    public LockerWallpaperView(@NonNull Context context) {
        super(context);
        init();
    }

    public LockerWallpaperView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LockerWallpaperView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        textureVideoView = new TextureVideoView(getContext());
        textureVideoView.setVisibility(INVISIBLE);
        textureVideoView.setLooping(true);
        textureVideoView.setScaleType(TextureVideoView.ScaleType.CENTER_CROP);

        addView(textureVideoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        imageView = new ImageView(getContext());
        imageView.setVisibility(INVISIBLE);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        addView(imageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    public void setWallPaperFilePath(String path, boolean isLockerPageShow) {
        if (TextUtils.equals(mFilePath, path)) {
            return;
        }
        mFilePath = path;
        if (path.endsWith("png") || path.endsWith("jpeg") || path.endsWith("jpg")) {
            mType = TYPE_IMAGE;
            setImage(path);
        } else if (path.endsWith(".mp4") || path.contains("Mp4")) {
            mType = TYPE_VIDEO;
            boolean autoPlay = isLockerPageShow;
            setVideo(path, autoPlay);
        }
    }

    public int getType() {
        return mType;
    }

    private void setVideo(String path, boolean autoPlay) {
        boolean hasImageBefore = imageView.getVisibility() == VISIBLE;
        if (hasImageBefore) {
            // Hide image
            ObjectAnimator wallpaperOut = ObjectAnimator.ofFloat(imageView, "alpha", 1f, 0f);
            wallpaperOut.setDuration(300);
            wallpaperOut.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    imageView.setVisibility(INVISIBLE);
                }
            });
            wallpaperOut.start();
        }
        textureVideoView.setVisibility(VISIBLE);
        textureVideoView.setVideoPath(path);
        if (CustomizeUtils.isVideoMute()) {
            textureVideoView.mute();
        } else {
            textureVideoView.resumeVolume();
        }
        if (windowFocus && autoPlay) {
            textureVideoView.play();
        }
    }

    public void mute(boolean mute) {
        if (mute) {
            textureVideoView.mute();
        } else {
            textureVideoView.resumeVolume();
        }
    }

    boolean windowFocus = false;
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        windowFocus = hasWindowFocus;
    }

    private void setImage(String path) {
        GlideApp.with(getContext()).asBitmap()
                .skipMemoryCache(true)
                .load(path).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                doImageSwitchAnimation(resource);
            }
        });
    }

    private void doImageSwitchAnimation(Bitmap bitmap) {
        ImageView wallpaperView = imageView;
        wallpaperView.setVisibility(VISIBLE);

        boolean hasVideoBefore = textureVideoView.getVisibility() == VISIBLE;
        ObjectAnimator wallpaperOut;
        if (hasVideoBefore) {
            wallpaperOut = ObjectAnimator.ofFloat(textureVideoView, "alpha", 1f, 0f);
            wallpaperOut.setDuration(300);
        } else {
            boolean hasBitmapBefore = imageView.getDrawable() != null;
            wallpaperOut = ObjectAnimator.ofFloat(wallpaperView, "alpha", 1f, 0.5f);
            wallpaperOut.setDuration(hasBitmapBefore ? 300 : 10);
        }
        wallpaperOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (hasVideoBefore) {
                    textureVideoView.stop();
                    textureVideoView.setVisibility(INVISIBLE);
                }
                wallpaperView.setImageBitmap(bitmap);
            }
        });

        ObjectAnimator wallpaperIn = ObjectAnimator.ofFloat(wallpaperView, "alpha", 0.5f, 1f);
        wallpaperIn.setDuration(300);

        AnimatorSet change = new AnimatorSet();
        change.playSequentially(wallpaperOut, wallpaperIn);
        change.start();
    }

    public void setImageBitmap(Bitmap resource) {
        imageView.setVisibility(VISIBLE);
        imageView.setImageBitmap(resource);
    }

    public void setImageResource(int resource) {
        imageView.setVisibility(VISIBLE);
        imageView.setImageResource(resource);
    }

    public Drawable getDrawable() {
        return imageView.getDrawable();
    }

    public void onResume() {
        textureVideoView.play();
    }

    public void onPause() {
        textureVideoView.stop();
    }

    public void resumePlay() {
        if (textureVideoView.isPaused()) {
            textureVideoView.resumePlayback();
        }
    }

    public void pausePlay() {
        if (textureVideoView.isPlaying()) {
            textureVideoView.pause();
        }
    }
}
