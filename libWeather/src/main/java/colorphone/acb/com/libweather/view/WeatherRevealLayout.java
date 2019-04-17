package colorphone.acb.com.libweather.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

import com.superapps.util.Dimensions;

/**
 * @author sundxing
 */
public class WeatherRevealLayout extends FrameLayout {

    private RectF mRectF = new RectF();
    private Rect mRectTemp = new Rect();

    private RectF mDrawingRectF = new RectF();

    private float mCornerRadius;
    private ValueAnimator animator;

    private Paint mPaint;
    private Paint mStrikePaint;
    private Path mPath;
    private boolean isClipView = false;
    private boolean isViewLarger;

    public WeatherRevealLayout(Context context) {
        this(context, null);
    }

    public WeatherRevealLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeatherRevealLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(0x4d000000);
        mPath = new Path();

        mStrikePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mStrikePaint.setStyle(Paint.Style.STROKE);
        mStrikePaint.setColor(0x33ffffff);
        mStrikePaint.setStrokeWidth(Dimensions.pxFromDp(1));

        animator = ValueAnimator.ofFloat(0, 1).setDuration(500);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = animation.getAnimatedFraction();
                updateDrawingRect(fraction);
            }
        });
    }


    public WeatherRevealLayout setRectF(RectF rectF) {
        mRectF = rectF;
        return this;
    }

    public WeatherRevealLayout addAnimatorListener(Animator.AnimatorListener listener) {
        animator.addListener(listener);
        return this;
    }

    public WeatherRevealLayout addUpdateListener(ValueAnimator.AnimatorUpdateListener listener) {
        animator.addUpdateListener(listener);
        return this;
    }

    public float getCornerRadius() {
        return mCornerRadius;
    }

    public RectF getDrawingRectF() {
        return mDrawingRectF;
    }

    public float getSizeFraction() {
        if (mRectF.height() == 0) {
            return 0;
        }
        return mDrawingRectF.height() / mRectF.height();
    }

    public void setCornerRadius(float cornerRadius) {
        mCornerRadius = cornerRadius;
    }

    private void startAnimation() {
        animator.start();
        isClipView = true;
    }

    public void open() {
        isViewLarger = true;
        startAnimation();
    }

    public void close() {
        isViewLarger = false;
        startAnimation();
    }

    public void openImmediately() {
        isViewLarger = true;
        isClipView = true;
        updateDrawingRect(1f);
    }

    public void closeImmediately() {
        isViewLarger = false;
        isClipView = true;

        updateDrawingRect(1f);
    }

    private void updateDrawingRect(float fraction) {
        if (mRectF.isEmpty()) {
            getDrawingRect(mRectTemp);
            mRectF.set(mRectTemp);
        }
        float df = isViewLarger ? (1 - fraction) : fraction;
        float dx = df * mRectF.width();
        float dy = df * mRectF.height();
        mDrawingRectF.set(mRectF.left + dx, mRectF.top, mRectF.right, mRectF.bottom - dy);
        mStrikePaint.setAlpha((int) (df * df * 255));
        invalidate();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isClipView) {
            mPath.reset();
            mPath.addRoundRect(mDrawingRectF, mCornerRadius, mCornerRadius, Path.Direction.CW);
            canvas.clipPath(mPath);
            canvas.drawPath(mPath, mPaint);
            canvas.drawPath(mPath, mStrikePaint);
        }
    }

}
