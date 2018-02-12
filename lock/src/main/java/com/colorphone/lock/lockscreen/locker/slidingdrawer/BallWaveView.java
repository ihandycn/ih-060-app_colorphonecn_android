package com.colorphone.lock.lockscreen.locker.slidingdrawer;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.colorphone.lock.ScreenStatusReceiver;
import com.colorphone.lock.lockscreen.locker.LockerMainFrame;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.superapps.util.Threads;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class BallWaveView extends View implements INotificationObserver {

    private final float X_SPACE = 20;
    private final double PI2 = 2 * Math.PI;

    private int mProgress;
    private boolean isInitialized = false;
    private boolean isDrawerOpened = false;

    private Path mClipPath = new Path();
    private Path mWavePath = new Path();
    private Path mWavePath2 = new Path();
    private Paint mWavePaint = new Paint();
    private Paint mWavePaint2 = new Paint();

    private int mWaveColor;
    private float mWaveMultiple;
    private float mWaveLength;
    private int mWaveHeight;
    private float mMaxRight;
    private float mWaveHz;
    private float mOffset = 1.0f;
    private int left, right, bottom;
    private double omega;

    private RefreshProgressRunnable mRefreshProgressRunnable;

    public BallWaveView(Context context, AttributeSet attrs) {
        super(context, attrs);

        post(new Runnable() {
            @Override
            public void run() {
                init();
            }
        });
        // As Canvas.clipPath() is not supported when hardware acceleration opened
        // until api level 18, we just close hardware acceleration function on pre-18 devices.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }
    }

    private void init() {
        int width = getWidth();
        mClipPath.reset();
        mClipPath.addCircle(width / 2, width / 2 - (int) (width * (1f - mProgress / 100f)), width / 2, Path.Direction.CCW);
        mWaveLength = width * mWaveMultiple;
        left = getLeft();
        right = getRight();
        bottom = getBottom() + 2;
        mMaxRight = right + X_SPACE;
        omega = PI2 / mWaveLength;
        isInitialized = true;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        HSGlobalNotificationCenter.addObserver(LockerMainFrame.EVENT_SLIDING_DRAWER_CLOSED, this);
        HSGlobalNotificationCenter.addObserver(LockerMainFrame.EVENT_SLIDING_DRAWER_OPENED, this);
        HSGlobalNotificationCenter.addObserver(ScreenStatusReceiver.NOTIFICATION_SCREEN_OFF, this);
        HSGlobalNotificationCenter.addObserver(ScreenStatusReceiver.NOTIFICATION_SCREEN_ON, this);
    }

    @Override
    protected void onDetachedFromWindow() {
        stopWaveAnimation();
        HSGlobalNotificationCenter.removeObserver(this);

        super.onDetachedFromWindow();
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        switch (s) {
            case LockerMainFrame.EVENT_SLIDING_DRAWER_CLOSED:
                stopWaveAnimation();
                isDrawerOpened = false;
                break;
            case LockerMainFrame.EVENT_SLIDING_DRAWER_OPENED:
                init();
                startWaveAnimation();
                isDrawerOpened = true;
                break;
            case ScreenStatusReceiver.NOTIFICATION_SCREEN_OFF:
                if (isDrawerOpened) {
                    stopWaveAnimation();
                }
                break;
            case ScreenStatusReceiver.NOTIFICATION_SCREEN_ON:
                if (isDrawerOpened) {
                    startWaveAnimation();
                }
                break;
            default:
                break;
        }
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!isInitialized) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                // ANR guard
                Threads.callWithTimeout(new Callable<Object>() {
                    @TargetApi(Build.VERSION_CODES.KITKAT)
                    @Override
                    public Object call() throws Exception {
                        mWavePath.op(mClipPath, Path.Op.INTERSECT);
                        return null;
                    }
                }, 100, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                return;
            }
        } else {
            canvas.clipPath(mClipPath);
        }
        canvas.drawPath(mWavePath, mWavePaint);
    }

    public void setWaveColor(int waveColor) {
        this.mWaveColor = waveColor;
    }

    public void initializeWaveSize(int waveHeight, float waveMultiple, float waveHz) {
        mWaveMultiple = waveMultiple;
        mWaveHeight = waveHeight;
        mWaveHz = waveHz;
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        setLayoutParams(params);
    }

    public void initializePainters() {
        mWavePaint.setColor(mWaveColor);
        mWavePaint.setStyle(Paint.Style.FILL);
        mWavePaint.setAntiAlias(true);
        mWavePaint2.setColor(mWaveColor);
        mWavePaint2.setStyle(Paint.Style.FILL);
        mWavePaint2.setAntiAlias(true);
    }

    public void startWaveAnimation() {
        removeCallbacks(mRefreshProgressRunnable);
        mRefreshProgressRunnable = new RefreshProgressRunnable();
        post(mRefreshProgressRunnable);
    }

    public void stopWaveAnimation() {
        removeCallbacks(mRefreshProgressRunnable);
    }

    /**
     * calculate wave track
     */
    private void calculatePath() {
        mWavePath.reset();
        mWavePath2.reset();

        getWaveOffset();

        float y;
        float y2;
        mWavePath.moveTo(left, bottom);
        mWavePath2.moveTo(left, bottom);
        for (float x = 0; x <= mMaxRight; x += X_SPACE) {
            y = (float) (mWaveHeight * Math.sin(omega * x - mOffset) + mWaveHeight);
            y2 = (float) (mWaveHeight / 1.5f * Math.sin(omega * x - mOffset - 1) + mWaveHeight);
            mWavePath.lineTo(x, y);
            mWavePath2.lineTo(x, y2);
        }
        mWavePath.lineTo(right, bottom);
        mWavePath2.lineTo(right, bottom);
    }

    private void getWaveOffset() {
        if (mOffset > Float.MAX_VALUE - 100) {
            mOffset = 0;
        } else {
            mOffset += mWaveHz;
        }
    }

    public void setProgress(int progress) {
        this.mProgress = progress;
        int width = getWidth();
        this.mClipPath.reset();
        this.mClipPath.addCircle(width / 2, width / 2 - (int) (width * (1f - mProgress / 100f)), width / 2, Path.Direction.CCW);
    }

    public void setColor(int color) {
        mWavePaint.setColor(color);
        mWavePaint2.setColor(getDarkerColor(color));
    }

    private int getDarkerColor(int color) {
        return Color.rgb((int) (Color.red(color) / 1.5f), (int) (Color.green(color) / 1.5f), (int) (Color.blue(color) / 1.5f));
    }

    private class RefreshProgressRunnable implements Runnable {
        public void run() {
            synchronized (BallWaveView.this) {
                long start = System.currentTimeMillis();

                calculatePath();

                invalidate();

                long gap = 40 - (System.currentTimeMillis() - start);
                postDelayed(this, gap < 0 ? 0 : gap);
            }
        }
    }
}
