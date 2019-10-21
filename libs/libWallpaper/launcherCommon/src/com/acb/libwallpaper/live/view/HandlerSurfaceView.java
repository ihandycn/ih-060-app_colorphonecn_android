package com.acb.libwallpaper.live.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.ihs.commons.utils.HSLog;

public abstract class HandlerSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = HandlerSurfaceView.class.getSimpleName();

    private HandlerThread mHandlerThread;

    @Nullable protected Handler mHandler;

    // Clear background
    private Paint mPaint;
    private boolean mDrawOk;
    private final Object mDrawOKLocker = new Object();

    public HandlerSurfaceView(Context context) {
        this(context, null);
    }

    public HandlerSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HandlerSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        HSLog.d(TAG, "surfaceCreated()");
        synchronized (this) {
            if (mHandlerThread == null) {
                mHandlerThread = new HandlerThread(TAG);
                mHandlerThread.setPriority(HandlerThread.MAX_PRIORITY);
                mHandlerThread.start();
                mHandler = new Handler(mHandlerThread.getLooper());
            }
        }

        onSurfaceCreated(holder);

        synchronized (mDrawOKLocker) {
            mDrawOk = true;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        HSLog.d(TAG, "surfaceChanged()");

        onSurfaceChanged(holder, format, width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        HSLog.d(TAG, "surfaceDestroyed()");

        synchronized (mDrawOKLocker) {
            mDrawOk = false;
        }

        onSurfaceDestroyed(holder);

        synchronized (this) {
            mHandler.post(() -> {
                synchronized (HandlerSurfaceView.this) {
                    if (mHandlerThread == null) {
                        return;
                    }
                    mHandlerThread.quit();
                    mHandlerThread = null;
                    mHandler = null;
                }
            });
        }
    }

    public void updateSurfaceView(final boolean isClearCanvas) {
        synchronized (this) {
            if (mHandler == null) {
                return;
            }

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    surfaceViewDraw(isClearCanvas);
                }
            });
        }
    }

    private void init() {
        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);

        mPaint = new Paint();
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    private void surfaceViewDraw(final boolean isClearCanvas) {
        synchronized (mDrawOKLocker) {
            if (!mDrawOk) {
                return;
            }

            if (getVisibility() != SurfaceView.VISIBLE) {
                return;
            }

            SurfaceHolder surfaceHolder = getHolder();
            if (surfaceHolder == null || surfaceHolder.isCreating()) {
                return;
            }

            Canvas canvas = null;
            try {
                canvas = surfaceHolder.lockCanvas();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (canvas == null) {
                return;
            }

            if (isClearCanvas) {
                canvas.drawPaint(mPaint);
            }
            onSurfaceViewDraw(canvas);

            try {
                surfaceHolder.unlockCanvasAndPost(canvas);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public abstract void onSurfaceCreated(SurfaceHolder holder);

    public abstract void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height);

    public abstract void onSurfaceDestroyed(SurfaceHolder holder);

    public abstract void onSurfaceViewDraw(Canvas canvas);
}
