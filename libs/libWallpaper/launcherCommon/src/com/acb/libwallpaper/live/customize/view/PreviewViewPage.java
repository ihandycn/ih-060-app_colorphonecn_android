package com.acb.libwallpaper.live.customize.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

 import com.honeycomb.colorphone.R;

public class PreviewViewPage extends FrameLayout {

    public interface PreviewPageListener {
        void onRetryButtonPressed(PreviewViewPage page);
    }

    public ImageView largeWallpaperImageView;
    public FrameLayout loadingView;
    public LinearLayout retryLayout;
    public int width;
    public int height;
    private PreviewPageListener listener = null;

    public void setListener(PreviewPageListener listener) {
        this.listener = listener;
    }

    public PreviewViewPage(Context context) {
        super(context);
    }

    public PreviewViewPage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PreviewViewPage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setProgress(String progress) {

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.largeWallpaperImageView = (ImageView) findViewById(R.id.large_wallpaper_image_view);
        this.loadingView = (FrameLayout) findViewById(R.id.wallpaper_preview_loading_view);
        this.retryLayout = (LinearLayout) findViewById(R.id.retry_downloading_layout);

        retryLayout.findViewById(R.id.retry_downloading_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onRetryButtonPressed(PreviewViewPage.this);
                }
            }
        });
    }

}
