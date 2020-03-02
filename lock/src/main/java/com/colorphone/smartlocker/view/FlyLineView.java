package com.colorphone.smartlocker.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.colorphone.lock.R;
import com.colorphone.smartlocker.utils.DisplayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hao.li on 2019/4/24.
 */

public class FlyLineView extends View {

    private class RandomRectF {
        private RectF rectF = new RectF();
        private long delayTime;
        private int marginLeft;
        private ValueAnimator valueAnimator;
        private boolean isNeedRepeat = true;
        private boolean isStopping = false;

        private Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (STOP_ANIM == msg.what) {
                    isNeedRepeat = false;
                }
            }
        };

        private void initAnim() {
            valueAnimator = ValueAnimator.ofFloat(-lineHeight, height);
            valueAnimator.setInterpolator(new LinearInterpolator());

            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (isNeedRepeat) {
                        valueAnimator.start();
                    } else {
                        isStopping = false;
                    }
                }
            });

            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    rectF.set(marginLeft, value, marginLeft + lineWidth, value + lineHeight);

                    invalidate();
                }
            });
            valueAnimator.setStartDelay(delayTime);
            valueAnimator.setDuration(ANIM_DURATION);
        }

        private void startAnim() {
            isStopping = false;
            isNeedRepeat = true;
            valueAnimator.start();
        }

        private void stopAnim() {
            isStopping = true;
            handler.sendEmptyMessage(STOP_ANIM);
        }
    }

    private static final long ANIM_DURATION = 300L;
    private static final int LINE_COUNT = 10;
    private static final int STOP_ANIM = 1;

    private Paint linePaint;
    private Bitmap lineBitmap;
    private float width;
    private float height;
    private int lineHeight, lineWidth;

    private List<RandomRectF> randomRectFS = new ArrayList<>();

    public FlyLineView(Context context) {
        super(context);
        init();
    }

    public FlyLineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FlyLineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        lineBitmap = DisplayUtils.drawable2Bitmap(getResources().getDrawable(R.drawable.daily_news_refresh_line));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = getMeasuredWidth();
        height = getMeasuredHeight();

        lineHeight = (int) (height / 1.2f);
        lineWidth = 3;

        linePaint = new Paint();
        linePaint.setAntiAlias(true);

        initRandomRectF();
    }

    private void initRandomRectF() {

        for (int i = 0; i < LINE_COUNT; i++) {
            RandomRectF randomRectF = new RandomRectF();
            randomRectF.delayTime = (long) (Math.random() * ANIM_DURATION);
            randomRectF.marginLeft = (int) (Math.random() * width);
            randomRectF.initAnim();
            randomRectFS.add(randomRectF);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (RandomRectF randomRectF : randomRectFS) {
            canvas.drawBitmap(lineBitmap, null, randomRectF.rectF, linePaint);
        }
    }

    public void startAnim() {
        for (RandomRectF randomRectF : randomRectFS) {
            if (!randomRectF.isStopping) {
                randomRectF.startAnim();
            }
        }
    }

    public void stopAnim() {
        for (RandomRectF randomRectF : randomRectFS) {
            randomRectF.stopAnim();
        }
    }

}
