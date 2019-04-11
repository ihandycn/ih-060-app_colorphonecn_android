package colorphone.acb.com.libweather;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import com.ihs.weather.HourlyForecast;
import com.superapps.util.Dimensions;
import com.superapps.util.Fonts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import colorphone.acb.com.libweather.util.Thunk;
import colorphone.acb.com.libweather.util.Utils;

public class HourlyForecastCurve extends View {

    public static final float DATA_LIMIT = 24;
    public static final float DATA_UNIT_WIDTH_DP = 55;

    private static final int PER_STEP_DP = 4;
    private static final float TEXT_SIZE_DP = 15;
    private static final float CURVE_STROKE_WIDTH = 2.3f;
    private static final float TEXT_ELEVATE_DP = 13;

    // Mke this a little less than 1f to avoid overlapping with daily temperature text
    private static final float MAX_PROGRESS = 0.92f;

    private static final long CURVE_ANIMATION_DELAY_AFTER_FIRST_DRAW = 110;

    public interface CurveAnimationCoordinator {
        boolean shouldStartCurveAnimation();
    }

    private CurveAnimationCoordinator mAnimCoordinator;

    private List<Float> mData = new ArrayList<>();
    private List<Float> mCoordX = new ArrayList<>();
    private List<Float> mCoordY = new ArrayList<>();
    private int mStepPx;
    private Float mMaxValue, mMinValue;
    private boolean mDataReady = false;
    private float mProgress = 0f;
    private RectF mRect = new RectF();
    private Paint mCurvePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mPointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private LongPath mPath;
    private SplineInterpolator mSplineInterpolator;
    @Thunk
    ObjectAnimator mCurveAnimator;
    private Bitmap mCurvePointNow;
    private Handler mHandler;

    private boolean mIsRtl;
    private boolean mCurveAnimationPending = false;

    public HourlyForecastCurve(Context context) {
        this(context, null);
    }

    public HourlyForecastCurve(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (isInEditMode()) {
            return;
        }

        mIsRtl = Dimensions.isRtl();
        mStepPx = Dimensions.pxFromDp(PER_STEP_DP);

        mHandler = new Handler(Looper.getMainLooper());
        mCurveAnimator = ObjectAnimator.ofFloat(this, "progress", 0f, MAX_PROGRESS);
        mCurveAnimator.setDuration(350);
        mCurveAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
//        mAnimCoordinator = (CurveAnimationCoordinator) context;

        mCurvePaint.setStyle(Paint.Style.STROKE);
        mCurvePaint.setStrokeCap(Paint.Cap.ROUND);
        mCurvePaint.setStrokeWidth(Dimensions.pxFromDp(CURVE_STROKE_WIDTH));
        mCurvePaint.setColor(ContextCompat.getColor(context, android.R.color.white));

        mPath = new LongPath((Activity) context, mCurvePaint);

        mTextPaint.setTextSize(Dimensions.pxFromDp(TEXT_SIZE_DP));
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setColor(ContextCompat.getColor(context, android.R.color.white));
        mTextPaint.setTypeface(Fonts.getTypeface(Fonts.Font.CUSTOM_FONT_REGULAR));

        mCurvePointNow = BitmapFactory.decodeResource(context.getResources(), R.drawable.weather_detail_temperature_now);
    }

    public void bindHourlyForecasts(List<HourlyForecast> hourlyForecasts) {
        mData.clear();

        boolean fahrenheit = WeatherSettings.shouldDisplayFahrenheit();
        for (int i = 0, count = hourlyForecasts.size(); i < count && i < DATA_LIMIT; i++) {
            HourlyForecast forecast = hourlyForecasts.get(i);
            if (fahrenheit) {
                mData.add((float) forecast.getFahrenheit());
            } else {
                mData.add((float) forecast.getCelsius());
            }
        }
        mMaxValue = Collections.max(mData);
        mMinValue = Collections.min(mData);
        mSplineInterpolator = new SplineInterpolator(mData.size() + 2);

        mDataReady = true;
//        if (mAnimCoordinator.shouldStartCurveAnimation()) {
//            mCurveAnimationPending = true;
//        } else
            if (mCurveAnimationPending) {
            // Wait for anim start.
        } else {
            mProgress = MAX_PROGRESS;
        }

        // Re-measure width of the curve based on amount of data
        requestLayout();
    }

    public void setProgress(float progress) {
        mProgress = progress;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int width = Dimensions.pxFromDp(DATA_UNIT_WIDTH_DP) * mData.size();
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mRect.set(0, 0, getWidth(), getHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!mDataReady) return;
        mPath.reset();
        mCoordX.clear();
        mCoordY.clear();
        float unitHalfWidth = mRect.width() / (2.0f * mData.size());
        float animBaseLine = mRect.bottom;
        float ratio = (mRect.bottom - mRect.top) / (mMinValue - mMaxValue);
        float textElevatePx = Dimensions.pxFromDp(TEXT_ELEVATE_DP);
        float pointNowX = 0, pointNowY = 0;
        for (int i = 0, count = mData.size(); i < count; ++i) {
            float x;
            if (mIsRtl) {
                x = mRect.right - unitHalfWidth * (i * 2 + 1);
            } else {
                x = mRect.left + unitHalfWidth * (i * 2 + 1);
            }
            float y = (mData.get(i) - mMaxValue) * ratio + mRect.top;
            y = mProgress * (y - animBaseLine) + animBaseLine;
            mCoordX.add(x);
            mCoordY.add(y);
            if (i == 0) {
                pointNowX = x;
                pointNowY = y;
            }
            canvas.drawText(String.format(Locale.getDefault(), "%.0f\u00b0", mData.get(i)),
                    x, y - textElevatePx, mTextPaint);
        }
        mCoordX.add(0, mIsRtl ? mRect.right : mRect.left);
        mCoordY.add(0, 1.5f * mCoordY.get(0) - mCoordY.get(1) * 0.5f);
        mCoordX.add(mIsRtl ? mRect.left : mRect.right);
        mCoordY.add(1.5f * mCoordY.get(mCoordY.size() - 1) - mCoordY.get(mCoordY.size() - 2) * 0.5f);
        if (mIsRtl) {
            Collections.reverse(mCoordX);
            Collections.reverse(mCoordY);
        }
        mSplineInterpolator.load(mCoordX, mCoordY);
        int stepCount = (int) (Math.abs(mCoordX.get(mCoordX.size() - 1) - mCoordX.get(0)) / mStepPx);
        float currentX = mCoordX.get(0);
        float currentY = mSplineInterpolator.interpolate(currentX);
        mPath.moveTo(currentX, currentY);
        for (int i = 0; i < stepCount; ++i) {
            drawStep(canvas, currentX, currentY);
            currentX += mStepPx;
            currentY = mSplineInterpolator.interpolate(currentX);
        }
        drawStep(canvas, currentX, currentY);
        canvas.drawPath(mPath.getPath(), mCurvePaint);
        canvas.drawBitmap(mCurvePointNow, pointNowX - mCurvePointNow.getWidth() * 0.5f,
                pointNowY - mCurvePointNow.getHeight() * 0.5f, mPointPaint);

        if (mCurveAnimationPending) {
            mCurveAnimationPending = false;
            mHandler.postDelayed(() -> mCurveAnimator.start(), CURVE_ANIMATION_DELAY_AFTER_FIRST_DRAW);
        }
    }

    private void drawStep(Canvas canvas, float x, float y) {
        mPath.lineTo(canvas, x, y);
    }

    /**
     * A simplified {@link Path} wrapper that can draw a very long path (longer than device's OpenGL texture limit).
     */
    private static class LongPath {
        private Path mPath = new Path();

        private Paint mPaint;

        private float mInitialX;
        private float mInitialY;

        private final float mSizeLimit;

        // Activity parameter used as context to obtain screen size to make a safe guess of texture limit
        public LongPath(Activity activity, Paint paint) {
            mPaint = paint;

            Point screenSize = Utils.getScreenSize(activity);
            mSizeLimit = Math.min(screenSize.x, screenSize.y);
        }

        public void reset() {
            mPath.reset();
        }

        public void moveTo(float x, float y) {
            mPath.moveTo(x, y);

            // Record initial position for detecting over-size. Note that this LongPath only supports drawing
            // consecutive curve (which means moveTo() should not be called more than once between draw calls).
            mInitialX = x;
            mInitialY = y;
        }

        public void lineTo(Canvas canvas, float x, float y) {
            mPath.lineTo(x, y);

            if (Math.abs(x - mInitialX) > mSizeLimit || Math.abs(y - mInitialY) > mSizeLimit) {
                // Draw in advance and clear the path when over-size is detected
                canvas.drawPath(mPath, mPaint);
                mPath.reset();
                moveTo(x, y);
            }
        }

        public Path getPath() {
            return mPath;
        }
    }
}
