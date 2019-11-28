package com.honeycomb.colorphone.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.superapps.util.Dimensions;

public class RoundRectOverlayView extends View {
    private static int TYPE_BASE = 100;
    private static int TYPE_HOLE = 200;
    Context context;
    OverlayInfo overlayInfo;
    Paint paint;

    public RoundRectOverlayView(Context context, OverlayInfo overlayInfo) {
        super(context);
        this.context = context;
        this.overlayInfo = overlayInfo;
        paint = new Paint();
        paint.setAntiAlias(true);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(getBitmap(TYPE_HOLE), 0, 0, paint);
        canvas.drawBitmap(getBitmap(TYPE_BASE), 0, 0, paint);
        overlayInfo.onDraw();
    }

    private Bitmap getBitmap(int type) {
        Bitmap bitmap = Bitmap.createBitmap(Dimensions.getPhoneWidth(context), Dimensions.getPhoneHeight(context), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        if (type == TYPE_BASE) {
            paint.setColor(overlayInfo.getBaseColor());
            canvas.drawRect(0, 0, Dimensions.getPhoneWidth(context), Dimensions.getPhoneHeight(context), paint);
        } else if (type == TYPE_HOLE) {
            paint.setColor(Color.parseColor("#ff000000"));
            canvas.drawRoundRect(overlayInfo.getHoleRectF(), overlayInfo.getRadius(), overlayInfo.getRadius(), paint);
        }
        return bitmap;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        RectF rectF = overlayInfo.getHoleRectF();
        float x = event.getRawX();
        float y = event.getRawY();
        if (x > rectF.left && x < rectF.right && y > rectF.top && y < rectF.bottom) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                overlayInfo.onHoleClick();
            }
        }
        return true;
    }

    public interface OverlayInfo {
        int getBaseColor();

        RectF getHoleRectF();

        float getRadius();

        void onHoleClick();

        void onDraw();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return true;
    }
}
