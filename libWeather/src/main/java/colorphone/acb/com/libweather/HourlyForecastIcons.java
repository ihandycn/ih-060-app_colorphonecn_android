package colorphone.acb.com.libweather;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;

import com.ihs.weather.HSWeatherQueryResult;
import com.ihs.weather.HourlyForecast;
import com.superapps.util.Dimensions;
import com.superapps.util.Fonts;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import colorphone.acb.com.libweather.util.Utils;

public class HourlyForecastIcons extends View {

    private static final float TEXT_SIZE_DP = 12;
    private static final float PADDING_BOTTOM_DP = 10;
    private static final float ICON_PADDING_DP = 6;
    private static final float TEXT_PADDING_BOTTOM_DP = 16;
    private static final float POINT_RADIUS = 2.5f;

    private List<HourlyForecast> mData = new ArrayList<>();
    private CityData.AstronomyInfo mAstronomyInfo;

    private SparseArray<Bitmap> mIconCache = new SparseArray<>();
    private Paint mIconPaint = new Paint(Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);
    private Paint mClockTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mPointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF mRect = new RectF();
    private RectF mIconDst = new RectF();
    private Bitmap mLinePointNow;

    /**
     * Size limit for single draw-call when drawing horizontal line at bottom, in order not to break OpenGL layer size
     * limit.
     *
     * @see {@link com.honeycomb.launcher.weather.HourlyForecastCurve.LongPath}.
     */
    private float mSectionWidth;

    private boolean mIsRtl;

    public HourlyForecastIcons(Context context) {
        this(context, null);
    }

    public HourlyForecastIcons(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (isInEditMode()) {
            return;
        }
        mIsRtl = Dimensions.isRtl();

        mClockTextPaint.setTextSize(Dimensions.pxFromDp(TEXT_SIZE_DP));
        mClockTextPaint.setTextAlign(Paint.Align.CENTER);
        mClockTextPaint.setColor(ContextCompat.getColor(getContext(), R.color.weather_secondary));
        mClockTextPaint.setTypeface(Fonts.getTypeface(Fonts.Font.CUSTOM_FONT_REGULAR));

        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setColor(ContextCompat.getColor(getContext(), R.color.weather_primary));

        mPointPaint.setStyle(Paint.Style.FILL);
        mPointPaint.setColor(ContextCompat.getColor(getContext(), R.color.weather_primary));

        Point screenSize = Utils.getScreenSize((Activity) getContext());
        mSectionWidth = Math.min(screenSize.x, screenSize.y);

        mLinePointNow = BitmapFactory.decodeResource(context.getResources(), R.drawable.weather_detail_time_now);
    }

    public void bindHourlyForecasts(List<HourlyForecast> hourlyForecasts, CityData.AstronomyInfo astronomyInfo) {
        mAstronomyInfo = astronomyInfo;
        mData.clear();
        for (int i = 0, count = hourlyForecasts.size(); i < count && i < HourlyForecastCurve.DATA_LIMIT; i++) {
            mData.add(hourlyForecasts.get(i));
        }
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int width = Dimensions.pxFromDp(HourlyForecastCurve.DATA_UNIT_WIDTH_DP) * mData.size();
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
        float unitHalfWidth = mRect.width() / (2.0f * mData.size());
        float linePosY = getHeight() - Dimensions.pxFromDp(PADDING_BOTTOM_DP);
        float textPosY = linePosY - Dimensions.pxFromDp(TEXT_PADDING_BOTTOM_DP);
        float circleRadius = Dimensions.pxFromDp(POINT_RADIUS);
        float iconTop = Dimensions.pxFromDp(ICON_PADDING_DP);
        float iconBottom = textPosY - Dimensions.pxFromDp(TEXT_SIZE_DP) - Dimensions.pxFromDp(ICON_PADDING_DP);

        drawHorizontalLine(canvas, linePosY);
        for (int i = 0, count = mData.size(); i < count; ++i) {
            HourlyForecast forecast = mData.get(i);
            Bitmap icon = getWeatherIcon(forecast.getCondition(), forecast.getHour());
            float iconHalfWidth = (iconBottom - iconTop) * icon.getWidth() / (2f * icon.getHeight());
            float x;
            if (mIsRtl) {
                x = mRect.right - unitHalfWidth * (i * 2 + 1);
            }  else {
                x = mRect.left + unitHalfWidth * (i * 2 + 1);
            }
            mIconDst.set(x - iconHalfWidth, iconTop, x + iconHalfWidth, iconBottom);
            canvas.drawBitmap(icon, null, mIconDst, mIconPaint);
            if (i == 0) {
                canvas.drawBitmap(mLinePointNow, x - mLinePointNow.getWidth() * 0.5f,
                        linePosY - mLinePointNow.getHeight() * 0.5f, mIconPaint);
                mClockTextPaint.setColor(ContextCompat.getColor(getContext(), R.color.weather_primary));
            } else {
                canvas.drawCircle(x, linePosY, circleRadius, mPointPaint);
                mClockTextPaint.setColor(ContextCompat.getColor(getContext(), R.color.weather_secondary));
            }
            canvas.drawText(String.format(Locale.getDefault(), "%02d:00", forecast.getHour()), x, textPosY,
                    mClockTextPaint);
        }
    }

    private void drawHorizontalLine(Canvas canvas, float linePosY) {
        final float totalWidth = getWidth();

        float sectionEnd = 0f;
        do {
            float sectionStart = sectionEnd;
            sectionEnd = Math.min(sectionEnd + mSectionWidth, totalWidth);
            canvas.drawLine(sectionStart, linePosY, sectionEnd, linePosY, mLinePaint);
        } while (sectionEnd < totalWidth - 0.5f);
    }

    private Bitmap getWeatherIcon(HSWeatherQueryResult.Condition condition, int hour) {
        int resId = WeatherUtils.getWeatherConditionSmallIconResourceId(condition,
                WeatherUtils.isNight(mAstronomyInfo, hour, 0));
        Bitmap icon = mIconCache.get(resId);
        if (icon != null) {
            return icon;
        }
        icon = BitmapFactory.decodeResource(getContext().getResources(), resId);
        mIconCache.put(resId, icon);
        return icon;
    }
}
