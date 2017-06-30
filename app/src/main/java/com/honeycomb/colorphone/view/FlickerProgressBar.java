package com.honeycomb.colorphone.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.honeycomb.colorphone.R;


public class FlickerProgressBar extends View implements Runnable{
    private PorterDuffXfermode xfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP);

    private int borderWidth;

    private float maxProgress = 100f;

    private Paint textPaint;

    private Paint bgPaint;

    private Paint pgPaint;

    private String progressText;

    private Rect textRect;

    private RectF bgRectf;
    private RectF pRectf;

    /**
     * 左右来回移动的滑块
     */
    private Bitmap flikerBitmap;

    /**
     * 滑块移动最左边位置，作用是控制移动
     */
    private float flickerLeft;

    /**
     * 进度条 bitmap ，包含滑块
     */
    private Bitmap pgBitmap;

    private Canvas pgCanvas;

    /**
     * 当前进度
     */
    private float progress;

    private boolean isFinish;

    private boolean isStop;

    /**
     * 下载中颜色
     */
    private int loadingColor;

    /**
     * 暂停时颜色
     */
    private int stopColor;

    /**
     * 进度文本、边框、进度条颜色
     */
    private int progressColor;

    private int borderColor;

    private int textSize;

    private int radius;

    private Thread thread;

    BitmapShader bitmapShader;
    private HintText mHintText;
    private boolean isFlickerEnabled;

    public FlickerProgressBar(Context context) {
        this(context, null, 0);
    }

    public FlickerProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlickerProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(attrs);
    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.FlickerProgressBar);
        try {
            textSize = (int) ta.getDimension(R.styleable.FlickerProgressBar_textSize, 12);
            loadingColor = ta.getColor(R.styleable.FlickerProgressBar_loadingColor, Color.parseColor("#40c4ff"));
            stopColor = ta.getColor(R.styleable.FlickerProgressBar_stopColor, Color.parseColor("#ff9800"));
            radius = ta.getDimensionPixelSize(R.styleable.FlickerProgressBar_radius, 0);
            borderWidth = (int) ta.getDimension(R.styleable.FlickerProgressBar_borderWidth, 0);
            borderColor = ta.getColor(R.styleable.FlickerProgressBar_borderColor, Color.parseColor("#88FFFFFF"));
        } finally {
            ta.recycle();
        }
    }

    private void init() {
        if (mHintText == null) {
            mHintText = new HintText();
        }

        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        bgPaint.setStyle(Paint.Style.STROKE);
        bgPaint.setStrokeWidth(borderWidth);

        pgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pgPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(textSize);

        textRect = new Rect();
        bgRectf = new RectF(0, 0, getMeasuredWidth(), getMeasuredHeight());
        float insect = borderWidth / 2f;
        bgRectf.inset(insect, insect);

        pRectf = new RectF(bgRectf);

        pRectf.inset(insect, insect);

        if(isStop){
            progressColor = stopColor;
        } else{
            progressColor = loadingColor;
        }

        flikerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.flicker);
        flickerLeft = -flikerBitmap.getWidth();

        initPgBimap();
    }

    private void initPgBimap() {
        pgBitmap = Bitmap.createBitmap(getMeasuredWidth() - borderWidth, getMeasuredHeight() - borderWidth, Bitmap.Config.ARGB_8888);
        pgCanvas = new Canvas(pgBitmap);
        thread = new Thread(this);
        thread.start();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(pgBitmap == null){
            init();
        }
        //背景
        drawBackGround(canvas);

        //进度
        drawProgress(canvas);

         //进度text
        drawProgressText(canvas);

        //变色处理
//        drawColorProgressText(canvas);
    }

    /**
     * 边框
     * @param canvas
     */
    private void drawBackGround(Canvas canvas) {
        if (borderWidth <= 0) {
            return;
        }
        bgPaint.setColor(borderColor);
        //left、top、right、bottom不要贴着控件边，否则border只有一半绘制在控件内,导致圆角处线条显粗
        canvas.drawRoundRect(bgRectf, radius, radius, bgPaint);
    }

    /**
     * 进度
     */
    private void drawProgress(Canvas canvas) {
        pgPaint.setColor(progressColor);

        float right = (progress / maxProgress) * getMeasuredWidth();
        pgCanvas.save(Canvas.CLIP_SAVE_FLAG);
        pgCanvas.clipRect(0, 0, right, getMeasuredHeight());
        pgCanvas.drawColor(progressColor);
        pgCanvas.restore();

        if(!isStop && isFlickerEnabled && flikerBitmap != null){
            pgPaint.setXfermode(xfermode);
            pgCanvas.drawBitmap(flikerBitmap, flickerLeft, 0, pgPaint);
            pgPaint.setXfermode(null);
        }

        //控制显示区域
        bitmapShader = new BitmapShader(pgBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        pgPaint.setShader(bitmapShader);
        float r = radius - borderWidth / 2f;
        canvas.drawRoundRect(pRectf, r, r, pgPaint);
    }

    /**
     * 进度提示文本
     * @param canvas
     */
    private void drawProgressText(Canvas canvas) {
        progressText = getProgressText();
        if (TextUtils.isEmpty(progressText)) {
            return;
        }

        textPaint.setColor(Color.WHITE);
        textPaint.getTextBounds(progressText, 0, progressText.length(), textRect);
        int tWidth = textRect.width();
        int tHeight = textRect.height();
        float xCoordinate = (getMeasuredWidth() - tWidth) / 2;
        float yCoordinate = (getMeasuredHeight() + tHeight) / 2;
        canvas.drawText(progressText, xCoordinate, yCoordinate, textPaint);
    }

    /**
     * 变色处理
     * @param canvas
     */
    private void drawColorProgressText(Canvas canvas) {
        textPaint.setColor(Color.WHITE);
        int tWidth = textRect.width();
        int tHeight = textRect.height();
        float xCoordinate = (getMeasuredWidth() - tWidth) / 2;
        float yCoordinate = (getMeasuredHeight() + tHeight) / 2;
        float progressWidth = (progress / maxProgress) * getMeasuredWidth();
        if(progressWidth > xCoordinate){
            canvas.save(Canvas.CLIP_SAVE_FLAG);
            float right = Math.min(progressWidth, xCoordinate + tWidth * 1.1f);
            canvas.clipRect(xCoordinate, 0, right, getMeasuredHeight());
            canvas.drawText(progressText, xCoordinate, yCoordinate, textPaint);
            canvas.restore();
        }
    }

    public void setHintText(HintText hintText) {
        mHintText = hintText;
    }

    public void setProgress(float progress){
        if(!isStop){
            if(progress < maxProgress){
                this.progress = progress;
            } else {
                this.progress = maxProgress;
                finishLoad();
            }
            invalidate();
        }
    }

    public void setStop(boolean stop) {
        isStop = stop;
        if(isStop){
            progressColor = stopColor;
            thread.interrupt();
        } else {
            progressColor = loadingColor;
            thread = new Thread(this);
            thread.start();
        }
        invalidate();
    }

    public void finishLoad() {
        isFinish = true;
        setStop(true);
    }

    public void toggle(){
        if(!isFinish){
            if(isStop){
                setStop(false);
            } else {
                setStop(true);
            }
        }
    }

    @Override
    public void run() {
        int width = flikerBitmap.getWidth();
        try {
            while (!isStop && !thread.isInterrupted()){
                flickerLeft += dp2px(5);
                float progressWidth = (progress / maxProgress) * getMeasuredWidth();
                if(flickerLeft >= progressWidth){
                    flickerLeft = -width;
                }
                postInvalidate();
                Thread.sleep(20);
            }
        }catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 重置
     */
    public void reset(){
        setStop(true);
        progress = 0;
        isFinish = false;
        isStop = false;
        progressColor = loadingColor;
        progressText = "";
        flickerLeft = -flikerBitmap.getWidth();
        initPgBimap();
    }

    public float getProgress() {
        return progress;
    }

    public boolean isStop() {
        return isStop;
    }

    public boolean isFinish() {
        return isFinish;
    }

    private String getProgressText() {
        String text= "";
        if(!isFinish){
            if(!isStop){
                text = mHintText.inProgress;
            } else {
                text = mHintText.paused;
            }
        } else{
            text = mHintText.finished;
        }

        return text;
    }

    private int dp2px(int dp){
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * density);
    }

    public void setFlickerEnabled(boolean flickerEnabled) {
        isFlickerEnabled = flickerEnabled;
    }

    public static class HintText {
        public String inProgress = "";
        public String paused = "";
        public String finished = "";
    }
}