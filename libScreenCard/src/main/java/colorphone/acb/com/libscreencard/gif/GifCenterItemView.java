package colorphone.acb.com.libscreencard.gif;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.superapps.util.Dimensions;
import com.superapps.util.Threads;

import colorphone.acb.com.libscreencard.ProgressWheel;
import colorphone.acb.com.libscreencard.TextureVideoView;

public class GifCenterItemView extends FrameLayout {

    private String mUrl;

    private ProgressWheel mProgressWheel;
    private TextureVideoView mTextureVideoView;

    public GifCenterItemView(@NonNull Context context, String url) {
        super(context);
        mUrl = url;
        init(context);
    }

    private void init(Context context) {
        mProgressWheel = new ProgressWheel(context);
        mProgressWheel.setBarColor(0x99FFFFFF);
        mProgressWheel.setCircleRadius(Dimensions.pxFromDp(30));
        LayoutParams wheelLp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        wheelLp.gravity = Gravity.CENTER;
        addView(mProgressWheel, wheelLp);
        mProgressWheel.setVisibility(View.INVISIBLE);

        mTextureVideoView = new TextureVideoView(context);
        mTextureVideoView.setScaleType(TextureVideoView.ScaleType.FIT_CENTER);
        mTextureVideoView.setOnCompletionListener(new TextureVideoView.OnCompletionListener() {
            @Override
            public void onCompletion() {
                if (!mTextureVideoView.isPlaying()) {
                    mTextureVideoView.play();
                }
            }
        });
        mTextureVideoView.setOnSurfaceAvailableListener(new TextureVideoView.OnSurfaceAvailableListener() {
            @Override
            public void onSurfaceAvailable() {
                if (!mTextureVideoView.isPlaying()) {
                    mTextureVideoView.play();
                }
            }
        });
        LayoutParams videoLp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addView(mTextureVideoView, videoLp);

        setBackgroundColor(0x80000000);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mProgressWheel.getVisibility() != VISIBLE) {
            mProgressWheel.setVisibility(VISIBLE);
            mProgressWheel.spin();
        }
        if (GifCacheUtils.haveCached(mUrl)) {
            mTextureVideoView.setVideoPath(GifCacheUtils.getCachedGifPath(mUrl));
            mTextureVideoView.play();
        } else {
            mTextureVideoView.setVideoURI(Uri.parse(mUrl));
            mTextureVideoView.setOnErrorListener(new TextureVideoView.onErrorListener() {
                @Override
                public void onError() {
                    mTextureVideoView.play();
                }
            });
            mTextureVideoView.setOnStartListener(new TextureVideoView.onStartListener() {
                @Override
                public void onStart() {
                    Threads.postOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressWheel.stopSpinning();
                            mProgressWheel.setVisibility(INVISIBLE);
                        }
                    });
                }
            });
            mTextureVideoView.play();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mProgressWheel.stopSpinning();
    }
}
