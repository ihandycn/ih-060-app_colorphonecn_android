package com.honeycomb.colorphone.battery;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.boost.DeviceManager;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Bitmaps;
import com.superapps.util.Dimensions;
import com.superapps.util.Fonts;

/**
 * Drawable for animated battery icon.
 */
public class BatteryIconDrawable extends Drawable {

    public static final String TAG = "BatteryIconDrawable";

    private static final float PERCENT_NUMBER_TEXT_SIZE_RATIO_SINGLE = 0.45f;
    private static final float PERCENT_SYMBOL_TEXT_SIZE_RATIO_SINGLE = 0.26f;

    private static final float PERCENT_NUMBER_TEXT_SIZE_RATIO_DOUBLE = 0.378f;
    private static final float PERCENT_SYMBOL_TEXT_SIZE_RATIO_DOUBLE = 0.221f;

    private static final float PERCENT_NUMBER_TEXT_SIZE_RATIO_MAX = 0.282f;
    private static final float PERCENT_SYMBOL_TEXT_SIZE_RATIO_MAX = 0.208f;

    private static final float POSITION_NORMAL_PERCENT_TEXT_BOTTOM_RATIO = 0.5f;

    private int mBatteryNumber;
    private Paint mBitmapPaint;
    private Paint mPercentNumberPaint;
    private Paint mPercentSymbolPaint;

    private Rect mTempRect;
    private RectF mTempRectF;

    private static Bitmap mBatteryBitmap;

    private float mPercentNumberTextSize, mPercentSymbolTextSize;

    private int mIconSize;
    private int mCenterX, mCenterY;
    private float mIconRadius;

    BatteryIconDrawable(int iconSize, int batteryNumber, boolean isNeedReloadIcon) {
        mIconSize = iconSize;
        mBatteryNumber = batteryNumber;
        mCenterX = mCenterY = Math.round(iconSize / 2f);
        mIconRadius = iconSize / 2f;
        mPercentNumberTextSize = iconSize * PERCENT_NUMBER_TEXT_SIZE_RATIO_MAX;
        mPercentSymbolTextSize = iconSize * PERCENT_SYMBOL_TEXT_SIZE_RATIO_MAX;

        mBitmapPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
        mBitmapPaint.setAlpha(0xff);

        mPercentNumberPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPercentSymbolPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mPercentNumberPaint.setTypeface(Fonts.getTypeface(Fonts.Font.CUSTOM_FONT_REGULAR)); // Condensed
        mPercentNumberPaint.setFakeBoldText(false);

        mPercentSymbolPaint.setTypeface(Fonts.getTypeface(Fonts.Font.CUSTOM_FONT_REGULAR)); // Condensed
        mPercentSymbolPaint.setFakeBoldText(false);

        mTempRect = new Rect();
        mTempRectF = new RectF();

        HSLog.d(TAG, "BatteryIconDrawable isNeedReloadIcon = " + isNeedReloadIcon + " mBatteryBitmap = " + mBatteryBitmap);

        if (isNeedReloadIcon || null == mBatteryBitmap) {
            mBatteryBitmap = getBatteryCurrentBitmap(batteryNumber);
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (null != mBatteryBitmap) {
            mTempRectF.set(mCenterX - mIconRadius, mCenterY - mIconRadius, mCenterX + mIconRadius, mCenterY + mIconRadius);
            canvas.drawBitmap(mBatteryBitmap, null, mTempRectF, mBitmapPaint);
        }
        drawProgressText(canvas, mBatteryNumber, getBatteryProgressTextColor(), 1f, 0xff);
    }

    private void drawProgressText(Canvas canvas, int percentage, @ColorInt int color, float scale, int alpha) {
        float[] textSizes = calculateTextSize();

        mPercentNumberPaint.setAlpha(alpha);
        mPercentNumberPaint.setColor(color);
        mPercentNumberPaint.setTextSize(textSizes[0] * scale);

        mPercentSymbolPaint.setAlpha(alpha);
        mPercentSymbolPaint.setColor(color);
        mPercentSymbolPaint.setTextSize(textSizes[1] * scale);

        String percentString = String.valueOf(percentage);
        mPercentNumberPaint.getTextBounds(percentString, 0, percentString.length(), mTempRect);

        float[] positions = calculateTextPosition();
        canvas.drawText(percentString, positions[0], positions[1], mPercentNumberPaint);
        canvas.drawText("%", positions[2], positions[3], mPercentSymbolPaint);
    }

    private float[] calculateTextPosition() {
        int numberWidth = mTempRect.width();
        int numberHeight = mTempRect.height();
        float[] positions = new float[4];
        if (mBatteryNumber == 1) {
            positions[0] = (float) Math.floor(mCenterX - 1.23f * numberWidth);
            positions[1] = (float) Math.floor(mCenterY + POSITION_NORMAL_PERCENT_TEXT_BOTTOM_RATIO * numberHeight);
            positions[2] = (float) Math.floor(mCenterX + 0.39f * numberWidth);
            positions[3] = (float) Math.floor(mCenterY + POSITION_NORMAL_PERCENT_TEXT_BOTTOM_RATIO * numberHeight);
        } else if (mBatteryNumber < 10) {
            positions[0] = (float) Math.floor(mCenterX - 0.99f * numberWidth);
            positions[1] = (float) Math.floor(mCenterY + POSITION_NORMAL_PERCENT_TEXT_BOTTOM_RATIO * numberHeight);
            positions[2] = (float) Math.floor(mCenterX + 0.15f * numberWidth);
            positions[3] = (float) Math.floor(mCenterY + POSITION_NORMAL_PERCENT_TEXT_BOTTOM_RATIO * numberHeight);
        } else if (mBatteryNumber >= 10 && mBatteryNumber <= 40 || (mBatteryNumber > 70 && mBatteryNumber < 100)) {
            positions[0] = (float) Math.floor(mCenterX - 0.7f * numberWidth);
            positions[1] = (float) Math.floor(mCenterY + POSITION_NORMAL_PERCENT_TEXT_BOTTOM_RATIO * numberHeight);
            positions[2] = (float) Math.floor(mCenterX + 0.4f * numberWidth);
            positions[3] = (float) Math.floor(mCenterY + POSITION_NORMAL_PERCENT_TEXT_BOTTOM_RATIO * numberHeight);
        } else if (mBatteryNumber > 40 && mBatteryNumber <= 70) {
            positions[0] = (float) Math.floor(mCenterX - 0.7f * numberWidth);
            positions[1] = (float) Math.floor(mCenterY + POSITION_NORMAL_PERCENT_TEXT_BOTTOM_RATIO * numberHeight);
            positions[2] = (float) Math.floor(mCenterX + 0.4f * numberWidth);
            positions[3] = (float) Math.floor(mCenterY + POSITION_NORMAL_PERCENT_TEXT_BOTTOM_RATIO * numberHeight);
        } else {
            positions[0] = (float) Math.floor(mCenterX - 0.7f * numberWidth);
            positions[1] = (float) Math.floor(mCenterY + POSITION_NORMAL_PERCENT_TEXT_BOTTOM_RATIO * numberHeight);
            positions[2] = (float) Math.floor(mCenterX + 0.4f * numberWidth);
            positions[3] = (float) Math.floor(mCenterY + POSITION_NORMAL_PERCENT_TEXT_BOTTOM_RATIO * numberHeight);
        }
        return positions;
    }

    private float[] calculateTextSize() {
        float[] positions = new float[2];
        if (mBatteryNumber < 10) {
            mPercentNumberTextSize = mIconSize * PERCENT_NUMBER_TEXT_SIZE_RATIO_SINGLE;
            mPercentSymbolTextSize = mIconSize * PERCENT_SYMBOL_TEXT_SIZE_RATIO_SINGLE;
        } else if (mBatteryNumber < 100) {
            mPercentNumberTextSize = mIconSize * PERCENT_NUMBER_TEXT_SIZE_RATIO_DOUBLE;
            mPercentSymbolTextSize = mIconSize * PERCENT_SYMBOL_TEXT_SIZE_RATIO_DOUBLE;
        } else {
            mPercentNumberTextSize = mIconSize * PERCENT_NUMBER_TEXT_SIZE_RATIO_MAX;
            mPercentSymbolTextSize = mIconSize * PERCENT_SYMBOL_TEXT_SIZE_RATIO_MAX;
        }
        positions[0] = mPercentNumberTextSize;
        positions[1] = mPercentSymbolTextSize;
        return positions;
    }

    private Bitmap getBatteryCurrentBitmap(int batteryNumber) {
        Resources resources = HSApplication.getContext().getResources();
        Bitmap batteryBitmap;

        if (batteryNumber <= BatteryUtils.BATTERY_STATUS_A_LEVEL) {
            batteryBitmap = BitmapFactory.decodeResource(resources, R.drawable.battery_icon_first);
        } else if (batteryNumber <= BatteryUtils.BATTERY_STATUS_B_LEVEL) {
            batteryBitmap = BitmapFactory.decodeResource(resources, R.drawable.battery_icon_second);
        } else if (batteryNumber <= BatteryUtils.BATTERY_STATUS_C_LEVEL) {
            batteryBitmap = BitmapFactory.decodeResource(resources, R.drawable.battery_icon_third);
        } else {
            batteryBitmap = BitmapFactory.decodeResource(resources, R.drawable.battery_icon_forth);
        }
        return batteryBitmap;
    }

    private int getBatteryProgressTextColor() {
        int color;
        if (mBatteryNumber <= BatteryUtils.BATTERY_STATUS_A_LEVEL) {
            color = ContextCompat.getColor(HSApplication.getContext(), R.color.battery_icon_text_color);
        } else if (mBatteryNumber <= BatteryUtils.BATTERY_STATUS_B_LEVEL) {
            color = ContextCompat.getColor(HSApplication.getContext(), R.color.battery_icon_text_color);
        } else if (mBatteryNumber <= BatteryUtils.BATTERY_STATUS_C_LEVEL) {
            color = ContextCompat.getColor(HSApplication.getContext(), R.color.battery_icon_text_color);
        } else {
            color = ContextCompat.getColor(HSApplication.getContext(), android.R.color.white);
        }
        return color;
    }

    public static Bitmap getBatteryBitmap(boolean isNeedReloadIcon) {
//        int iconSize = IconSettings.getWorkspaceScaledIconSize();
        int iconSize = Dimensions.pxFromDp(48);
        return Bitmaps.drawable2Bitmap(getBatteryDrawable(isNeedReloadIcon), iconSize, iconSize);
    }

    public static Bitmap getBatteryBitmapFromDrawable(BatteryIconDrawable batteryIconDrawable) {
        int iconSize = batteryIconDrawable.mIconSize;
        return Bitmaps.drawable2Bitmap(batteryIconDrawable, iconSize, iconSize);
    }

    public static BatteryIconDrawable getBatteryDrawable(int batteryNumber, boolean isNeedReloadIcon) {
//        int iconSize = IconSettings.getWorkspaceScaledIconSize();
        int iconSize = Dimensions.pxFromDp(48);
        return new BatteryIconDrawable(iconSize, batteryNumber, isNeedReloadIcon);
    }

    private static Drawable getBatteryDrawable(boolean isNeedReloadIcon) {
//        int iconSize = IconSettings.getWorkspaceScaledIconSize();
        int iconSize = Dimensions.pxFromDp(48);
        return new BatteryIconDrawable(iconSize, DeviceManager.getInstance().getBatteryLevel(), isNeedReloadIcon);
    }

    @Override
    public void setAlpha(int i) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.UNKNOWN;
    }

}
