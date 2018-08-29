package com.honeycomb.colorphone.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.IntDef;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.util.AttributeSet;
import android.view.View;

import com.honeycomb.colorphone.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ThreeStatesCheckBox extends View {

    @IntDef({ALL_UNCHECKED, PART_CHECKED, ALL_CHECKED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CheckedState{}

    public static final int ALL_UNCHECKED = 0;
    public static final int PART_CHECKED = 1;
    public static final int ALL_CHECKED = 2;

    public interface OnCheckBoxClickListener {
        void onClick(ThreeStatesCheckBox checkBox, @CheckedState int checkState);
    }

    private static final float SCALE_CHECKBOX_IN_VIEW = 0.6f;

    private int checkedState = ALL_UNCHECKED;
    private OnCheckBoxClickListener listener;

    private Paint paint;
    private Bitmap allUncheckedBitmap;
    private Bitmap partCheckedBitmap;
    private Bitmap allCheckedBitmap;

    public ThreeStatesCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();
        paint.setAntiAlias(true);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkedState == ALL_CHECKED) {
                    checkedState = ALL_UNCHECKED;
                } else {
                    checkedState = ALL_CHECKED;
                }
                invalidate();

                if (null != listener) {
                    listener.onClick(ThreeStatesCheckBox.this, checkedState);
                }
            }
        });
    }

    public void setCheckedState(@CheckedState int checkedState) {
        this.checkedState = checkedState;
        invalidate();
    }

    public int getCheckedState() {
        return checkedState;
    }

    public void setOnCheckBoxClickListener(OnCheckBoxClickListener onCheckBoxClickListener) {
        listener = onCheckBoxClickListener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        allUncheckedBitmap = createBitmap(R.drawable.clean_checkbox_unchecked_svg, width, height);
        partCheckedBitmap = createBitmap(R.drawable.clean_part_checked_svg, width, height);
        allCheckedBitmap = createBitmap(R.drawable.clean_checkbox_checked_svg, width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        switch (checkedState) {
            case ALL_UNCHECKED:
                canvas.drawBitmap(allUncheckedBitmap, 0, 0, paint);
                break;
            case PART_CHECKED:
                canvas.drawBitmap(partCheckedBitmap, 0, 0, paint);
                break;
            case ALL_CHECKED:
                canvas.drawBitmap(allCheckedBitmap, 0, 0, paint);
                break;
            default:
                canvas.drawBitmap(allUncheckedBitmap, 0, 0, paint);
        }
    }

    private Bitmap createBitmap(int drawableResId, int width, int height) {
        final VectorDrawableCompat drawable = VectorDrawableCompat.create(getResources(), drawableResId, null);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds((int)(width * (1 - SCALE_CHECKBOX_IN_VIEW) / 2),
                (int)(height * (1 - SCALE_CHECKBOX_IN_VIEW) / 2),
                (int)(width * (1 + SCALE_CHECKBOX_IN_VIEW) / 2),
                (int)(height * (1 + SCALE_CHECKBOX_IN_VIEW) / 2));
        drawable.draw(canvas);
        return bitmap;
    }
}
