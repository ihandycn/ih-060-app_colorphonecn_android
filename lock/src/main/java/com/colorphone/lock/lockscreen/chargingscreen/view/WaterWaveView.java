package com.colorphone.lock.lockscreen.chargingscreen.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

public class WaterWaveView extends View {

    private static final float DEFAULT_RATIO_WAVE_LENGTH = 1.0f;
    private static final float DEFAULT_RATIO_WAVE_SHIFT = 0.0f;
    private static final float DEFAULT_RATIO_WAVE_AMPLITUDE = 0.05f;
    private static final float DEFAULT_RATIO_WATER_LEVEL = 0.5f;

    public static final int DEFAULT_COLOR_BEHIND_WAVE = 0x28FFFFFF;
    public static final int DEFAULT_COLOR_FRONT_WAVE = 0x3CFFFFFF;

    private boolean isShowWave;

    private BitmapShader waveShader;
    private Matrix shaderMatrix;
    private Paint viewPaint;
    private Paint borderPaint;

    private float defaultAmplitude;
    private float defaultWaterLevel;
    private float defaultWaveLength;
    private double defaultAngularFrequency;

    private float amplitudeRatio = DEFAULT_RATIO_WAVE_AMPLITUDE;
    private float waveLengthRatio = DEFAULT_RATIO_WAVE_LENGTH;
    private float waterLevelRatio = DEFAULT_RATIO_WATER_LEVEL;
    private float waveShiftRatio = DEFAULT_RATIO_WAVE_SHIFT;

    private int behindWaveColor = DEFAULT_COLOR_BEHIND_WAVE;
    private int frontWaveColor = DEFAULT_COLOR_FRONT_WAVE;

    private PorterDuffXfermode porterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP);

    public WaterWaveView(Context context) {
        super(context);

        init();
    }

    public WaterWaveView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public WaterWaveView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init();
    }

    private void init() {
        shaderMatrix = new Matrix();
        viewPaint = new Paint();
        viewPaint.setAntiAlias(true);
    }

    public float getWaveShiftRatio() {
        return waveShiftRatio;
    }

    /**
     * Shift the wave horizontally according to <code>waveShiftRatio</code>.
     *
     * @param waveShiftRatio Should be 0 ~ 1. Default to be 0.
     *                       Result of waveShiftRatio multiples width of WaveView is the length to shift.
     */
    public void setWaveShiftRatio(float waveShiftRatio) {
        if (this.waveShiftRatio == waveShiftRatio) {
            return;
        }
        this.waveShiftRatio = waveShiftRatio;

        invalidate();
    }

    public float getWaterLevelRatio() {
        return waterLevelRatio;
    }

    /**
     * Set water level according to <code>waterLevelRatio</code>.
     *
     * @param waterLevelRatio Should be 0 ~ 1. Default to be 0.5.
     *                        Ratio of water level to WaveView height.
     */
    public void setWaterLevelRatio(float waterLevelRatio) {
        if (this.waterLevelRatio == waterLevelRatio) {
            return;
        }
        this.waterLevelRatio = waterLevelRatio;

        invalidate();
    }

    public float getAmplitudeRatio() {
        return amplitudeRatio;
    }

    /**
     * Set vertical size of wave according to <code>amplitudeRatio</code>
     *
     * @param amplitudeRatio Default to be 0.05. Result of amplitudeRatio + waterLevelRatio should be less than 1.
     *                       Ratio of amplitude to height of WaveView.
     */
    public void setAmplitudeRatio(float amplitudeRatio) {
        if (this.amplitudeRatio == amplitudeRatio) {
            return;
        }
        this.amplitudeRatio = amplitudeRatio;

        invalidate();
    }

    public float getWaveLengthRatio() {
        return waveLengthRatio;
    }

    /**
     * Set horizontal size of wave according to <code>waveLengthRatio</code>
     *
     * @param waveLengthRatio Default to be 1.
     *                        Ratio of wave length to width of WaveView.
     */
    public void setWaveLengthRatio(float waveLengthRatio) {
        if (this.waveLengthRatio == waveLengthRatio) {
            return;
        }
        this.waveLengthRatio = waveLengthRatio;

        invalidate();
    }

    public boolean isShowWave() {
        return isShowWave;
    }

    public void setShowWave(boolean showWave) {
        isShowWave = showWave;
    }

    public void setBorder(int width, int color) {
        if (borderPaint == null) {
            borderPaint = new Paint();
            borderPaint.setAntiAlias(true);
            borderPaint.setStyle(Style.STROKE);
        }
        borderPaint.setColor(color);
        borderPaint.setStrokeWidth(width);

        invalidate();
    }

    public void setWaveColor(int behindWaveColor, int frontWaveColor) {
        this.behindWaveColor = behindWaveColor;
        this.frontWaveColor = frontWaveColor;

        if (getWidth() > 0 && getHeight() > 0) {
            // need to recreate shader when color changed
            waveShader = null;
            createShader();

            invalidate();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (getWidth() > 0 && getHeight() > 0) {
            // need to recreate shader when color changed
            waveShader = null;
            createShader();

            invalidate();
        }
    }

    /**
     * Create the shader with default waves which repeat horizontally, and clamp vertically
     */
    private void createShader() {
        defaultAngularFrequency = 2.0f * Math.PI / DEFAULT_RATIO_WAVE_LENGTH / getWidth();
        defaultAmplitude = getHeight() * DEFAULT_RATIO_WAVE_AMPLITUDE;
        defaultWaterLevel = getHeight() * DEFAULT_RATIO_WATER_LEVEL;
        defaultWaveLength = getWidth();

        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint wavePaint = new Paint();
        wavePaint.setStrokeWidth(2);
        wavePaint.setAntiAlias(true);

        // Draw default waves into the bitmap
        // y=Asin(ωx+φ)+h
        final int endX = getWidth() + 1;
        final int endY = getHeight() + 1;

        float[] waveY = new float[endX];

        wavePaint.setColor(behindWaveColor);
        for (int beginX = 0; beginX < endX; beginX++) {
            double wx = beginX * defaultAngularFrequency;
            float beginY = (float) (defaultWaterLevel + defaultAmplitude * Math.sin(wx));
            canvas.drawLine(beginX, beginY, beginX, endY, wavePaint);

            waveY[beginX] = beginY;
        }

        wavePaint.setColor(frontWaveColor);
        final int wave2Shift = (int) (defaultWaveLength / 4);
        for (int beginX = 0; beginX < endX; beginX++) {
            canvas.drawLine(beginX, waveY[(beginX + wave2Shift) % endX], beginX, endY, wavePaint);
        }

        // use the bitamp to create the shader
        waveShader = new BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.CLAMP);
        viewPaint.setShader(waveShader);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isInEditMode()) {
            return;
        }

        final int layerId = canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), null, Canvas.ALL_SAVE_FLAG);

        if (!isShowWave || waveShader == null) {
            viewPaint.setShader(null);
            return;
        }

        if (viewPaint.getShader() == null) {
            viewPaint.setShader(waveShader);
        }

        shaderMatrix.setScale(waveLengthRatio / DEFAULT_RATIO_WAVE_LENGTH,
            amplitudeRatio / DEFAULT_RATIO_WAVE_AMPLITUDE, 0, defaultWaterLevel);

        shaderMatrix.postTranslate(waveShiftRatio * getWidth(),
            (DEFAULT_RATIO_WATER_LEVEL - waterLevelRatio) * getHeight());

        waveShader.setLocalMatrix(shaderMatrix);

        final Drawable drawable = getBackground();
        if (drawable != null) {
            drawable.draw(canvas);
        }

        final float borderWidth = borderPaint == null ? 0f : borderPaint.getStrokeWidth();
        if (borderWidth > 0) {
            borderPaint.setXfermode(porterDuffXfermode);
            canvas.drawRect(borderWidth / 2f, borderWidth / 2f, getWidth() - borderWidth / 2f - 0.5f,
                getHeight() - borderWidth / 2f - 0.5f, borderPaint);
            borderPaint.setXfermode(null);
        }
        viewPaint.setXfermode(porterDuffXfermode);
        canvas.drawRect(borderWidth, borderWidth, getWidth() - borderWidth,
            getHeight() - borderWidth, viewPaint);
        viewPaint.setXfermode(null);

        canvas.restoreToCount(layerId);
    }
}
