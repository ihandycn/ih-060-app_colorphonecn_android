package com.honeycomb.colorphone.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.honeycomb.colorphone.R;

public class UploadProcessView extends FrameLayout {
    private TextView mTv;

    private float mProcess = 1.0f;

    private Canvas mCanvas;
    private Rect mRect;
    private Bitmap mBitmap;


    public UploadProcessView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCanvas = new Canvas();
        mRect = new Rect();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTv = (TextView) getChildAt(0);
    }

    public final void setText(@StringRes int resid) {
        mTv.setText(resid);
    }

    public final void setText(String s) {
        mTv.setText(s);
    }

    public final void setProcess(float process) {
        mProcess  = process;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable drawable = getResources().getDrawable(R.drawable.upload_bg_gradient);
        drawable.setBounds(0, 0, getWidth(), getHeight());
        drawable.draw(canvas);

        if (mBitmap == null && getWidth() > 0 && getHeight() > 0) {
            mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            mCanvas.setBitmap(mBitmap);
            drawable.draw(mCanvas);
            mCanvas.drawColor(0xff5a587a, PorterDuff.Mode.SRC_IN);
        }


        mRect.set((int) (getWidth() * mProcess), 0, getWidth(), getHeight());
        canvas.drawBitmap(mBitmap, mRect, mRect, null);


        super.onDraw(canvas);
    }
}

