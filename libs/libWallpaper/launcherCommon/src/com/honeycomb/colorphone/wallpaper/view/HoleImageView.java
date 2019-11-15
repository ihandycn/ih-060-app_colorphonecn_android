package com.honeycomb.colorphone.wallpaper.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.honeycomb.colorphone.wallpaper.animation.LauncherAnimUtils;
import com.ihs.commons.utils.HSLog;

public class HoleImageView extends View {

    private static final String TAG = HoleImageView.class.getSimpleName();

    private static final float RATIO_HOLE_CENTER_Y = 0.6f;

    private ValueAnimator mDigAnimator;
    private Paint mDigPaint;
    private Paint mPaint;
    private ColorMatrix mDimMatrix;
    private Canvas mDigCanvas;
    private Bitmap mBitmap;

    private float mHoleCenterX;
    private float mHoleCenterY;
    private float mDiagonal;

    private Rect mSrcRect;
    private RectF mDstRectF;
    private float mAnimatedFraction;
    private OnDismissListener mOnDismissListener;

    public HoleImageView(Context context) {
        this(context, null);
    }

    public HoleImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HoleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
        mSrcRect = new Rect();
        mDstRectF = new RectF();
        mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
        mDimMatrix = new ColorMatrix();
        mDigPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDigPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        mDigAnimator = LauncherAnimUtils.ofFloat(this, 0.0f, 1.0f);
        mDigAnimator.setDuration(350);
        mDigAnimator.setInterpolator(LauncherAnimUtils.ACCELERATE_QUAD);
        mDigAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAnimatedFraction = animation.getAnimatedFraction();
                digHoleOnBitmap();
                invalidate();
            }
        });
        mDigAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Post to show the last frame before invoking this callback
                post(new Runnable() {
                    @Override
                    public void run() {
                        if (mOnDismissListener != null) {
                            mOnDismissListener.onDismiss();
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBitmap != null) {
            mDstRectF.set(0, 0, getWidth(), getHeight());
            float aspectRatio = (float) getWidth() / getHeight();
            computeSourceRect(mSrcRect, mBitmap.getWidth(), mBitmap.getHeight(), aspectRatio);
            canvas.drawBitmap(mBitmap, mSrcRect, mDstRectF, mPaint);
        }
    }

    /**
     * Calculate the source rect of a sub-bitmap respecting {@link ImageView.ScaleType#CENTER_CROP}.
     *
     * @param outRect     Result source rect used in {@link Canvas#drawBitmap(Bitmap, Rect, RectF, Paint)},
     *                    must be allocated by caller
     * @param srcWidth    Width of original bitmap
     * @param srcHeight   Height of original bitmap
     * @param aspectRatio Aspect ratio to pick out of original bitmap, width / height
     */
    private static void computeSourceRect(Rect outRect, int srcWidth, int srcHeight, float aspectRatio) {
        float srcAspectRatio = (float) srcWidth / srcHeight;
        if (srcAspectRatio > aspectRatio) {
            float halfSrcRectWidth = 0.5f * srcHeight * aspectRatio;
            float middle = 0.5f * srcWidth;
            outRect.set((int) (middle - halfSrcRectWidth), 0,
                    (int) (middle + halfSrcRectWidth), srcHeight);
        } else {
            float halfSrcRectHeight = 0.5f * srcWidth / aspectRatio;
            float middle = 0.5f * srcHeight;
            outRect.set(0, (int) (middle - halfSrcRectHeight),
                    srcWidth, (int) (middle + halfSrcRectHeight));
        }
    }

    public void setDimAlpha(float dimAlpha) {
        float colorScale = 1f - dimAlpha;
        mDimMatrix.setScale(colorScale, colorScale, colorScale, 1f);
        mPaint.setColorFilter(new ColorMatrixColorFilter(mDimMatrix));
        invalidate();
    }

    public void setImageResource(int resId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeResource(getResources(), resId, options);
        } catch (OutOfMemoryError ignored) {
        }
        if (bitmap != null) {
            setBitmapInternal(bitmap);
        } else {
            HSLog.w(TAG, "Skip setImageResource as failed to decode resource");
        }
    }

    public void setImageDrawable(BitmapDrawable drawable) {
        Bitmap immutableBitmap = drawable.getBitmap();
        Bitmap copy = null;
        try {
            copy = immutableBitmap.copy(Bitmap.Config.ARGB_8888, true);
        } catch (OutOfMemoryError ignored) {
        }
        if (copy != null) {
            setBitmapInternal(copy);
        } else {
            HSLog.w(TAG, "Skip setImageDrawable as failed to make bitmap copy");
        }
    }

    private void setBitmapInternal(Bitmap bitmap) {
        mBitmap = bitmap;

        // Update dig canvas and drawing geometry
        mDigCanvas = new Canvas(mBitmap);
        mHoleCenterX = mBitmap.getWidth() / 2f;
        mHoleCenterY = mBitmap.getHeight() * RATIO_HOLE_CENTER_Y;
        mDiagonal = (float) Math.hypot(mHoleCenterX, mHoleCenterY);
    }

    private void digHoleOnBitmap() {
        if (mDigCanvas != null) {
            mDigCanvas.drawCircle(mHoleCenterX, mHoleCenterY, mDiagonal * mAnimatedFraction, mDigPaint);
        }
    }

    public void setOnDismissListener(OnDismissListener onDismissListener) {
        mOnDismissListener = onDismissListener;
    }

    public void dismiss() {
        if (mDigAnimator.isRunning()) {
            mDigAnimator.cancel();
        }
        mDigAnimator.start();
    }

    public interface OnDismissListener {
        void onDismiss();
    }
}
