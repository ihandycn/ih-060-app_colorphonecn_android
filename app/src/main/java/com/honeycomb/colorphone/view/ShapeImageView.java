package com.honeycomb.colorphone.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.honeycomb.colorphone.R;
import com.superapps.util.Bitmaps;

public class ShapeImageView extends AppCompatImageView {

    private static final int DEFAULT_SHAPE = 0;

    public enum Shape {
        RECTANGLE,
        CIRCLE,
        OVAL
    }

    protected Shape mShape = Shape.RECTANGLE;

    private float mBorderSize = 0;
    private int mBorderColor = Color.WHITE;
    private float mRoundRadius = 0;
    private float mRoundRadiusLeftTop, mRoundRadiusLeftBottom, mRoundRadiusRightTop, mRoundRadiusRightBottom;
    private Paint mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF mDrawRect = new RectF();
    private final Matrix mShaderMatrix = new Matrix();
    private Paint mBitmapPaint = new Paint();
    private BitmapShader mBitmapShader;
    private Bitmap mBitmap;
    private Path mPath = new Path();

    public ShapeImageView(Context context) {
        this(context, null, 0);
    }

    public ShapeImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShapeImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(mBorderSize);
        mBorderPaint.setColor(mBorderColor);
        mBorderPaint.setAntiAlias(true);

        mBitmapPaint.setAntiAlias(true);
        super.setScaleType(ScaleType.CENTER_CROP); // dangerous
    }

    private void init(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs,
                R.styleable.ShapeImageView);
        int value = a.getInt(R.styleable.ShapeImageView_shape, DEFAULT_SHAPE);
        mShape = getShapeFromAttrs(value);
        mRoundRadius = a.getDimension(R.styleable.ShapeImageView_radius, mRoundRadius);
        mBorderSize = a.getDimension(R.styleable.ShapeImageView_border_size, mBorderSize);
        mBorderColor = a.getColor(R.styleable.ShapeImageView_border_color, mBorderColor);

        mRoundRadiusLeftBottom = a.getDimension(R.styleable.ShapeImageView_radius_leftBottom, mRoundRadius);
        mRoundRadiusLeftTop = a.getDimension(R.styleable.ShapeImageView_radius_leftTop, mRoundRadius);
        mRoundRadiusRightBottom = a.getDimension(R.styleable.ShapeImageView_radius_rightBottom, mRoundRadius);
        mRoundRadiusRightTop = a.getDimension(R.styleable.ShapeImageView_radius_rightTop, mRoundRadius);

        a.recycle();
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        mBitmap = Bitmaps.getBitmapFromDrawable(getDrawable());
        setupBitmapShader();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        mBitmap = Bitmaps.getBitmapFromDrawable(drawable);
        setupBitmapShader();
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        if (scaleType != ScaleType.CENTER_CROP) {
            throw new IllegalArgumentException(String.format("ScaleType %s not supported.", scaleType));
        }
    }

    private Shape getShapeFromAttrs(int value) {
        switch (value) {
            case 1:
                return Shape.RECTANGLE;
            case 2:
                return Shape.CIRCLE;
            case 3:
                return Shape.OVAL;
            default:
                return Shape.RECTANGLE;
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (mBitmap != null) {
            if (mShape == Shape.CIRCLE) {
                canvas.drawCircle(mDrawRect.right / 2f, mDrawRect.bottom / 2f,
                        Math.min(mDrawRect.right, mDrawRect.bottom) / 2f, mBitmapPaint);
            } else if (mShape == Shape.OVAL) {
                canvas.drawOval(mDrawRect, mBitmapPaint);
            } else {
                mPath.reset();
                mPath.addRoundRect(mDrawRect, getRadii(), Path.Direction.CW);
                canvas.drawPath(mPath, mBitmapPaint);
            }
        }

        if (mBorderSize > 0) {
            if (mShape == Shape.CIRCLE) {
                canvas.drawCircle(mDrawRect.right / 2f, mDrawRect.bottom / 2f,
                        Math.min(mDrawRect.right, mDrawRect.bottom) / 2f - mBorderSize / 2f, mBorderPaint);
            } else if (mShape == Shape.OVAL) {
                canvas.drawOval(mDrawRect, mBorderPaint);
            } else {
                mPath.reset();
                mPath.addRoundRect(mDrawRect, getRadii(), Path.Direction.CW);
                canvas.drawPath(mPath, mBorderPaint);
            }
        }
    }

    private float[] getRadii() {
        return new float[]{
                mRoundRadiusLeftTop, mRoundRadiusLeftTop,
                mRoundRadiusRightTop, mRoundRadiusRightTop,
                mRoundRadiusRightBottom, mRoundRadiusRightBottom,
                mRoundRadiusLeftBottom, mRoundRadiusLeftBottom,
        };
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initRect();
        setupBitmapShader();
    }

    private void setupBitmapShader() {
        if (mBitmapPaint == null) {
            return;
        }
        if (mBitmap == null) {
            return;
        }
        mBitmapShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        mBitmapPaint.setShader(mBitmapShader);
        mShaderMatrix.set(null);
        float scale = Math.max(getWidth() * 1f / mBitmap.getWidth(), getHeight() * 1f / mBitmap.getHeight());
        float dx = (getWidth() - mBitmap.getWidth() * scale) / 2f;
        float dy = (getHeight() - mBitmap.getHeight() * scale) / 2f;
        mShaderMatrix.setScale(scale, scale);
        mShaderMatrix.postTranslate(dx, dy);
        mBitmapShader.setLocalMatrix(mShaderMatrix);
        invalidate();
    }

    private void initRect() {
        float space = mBorderSize / 2f;
        mDrawRect.top = space;
        mDrawRect.left = space;
        mDrawRect.right = getWidth() - space;
        mDrawRect.bottom = getHeight() - space;
    }

    public float getBorderSize() {
        return mBorderSize;
    }

    public void setBorderSize(int mBorderSize) {
        this.mBorderSize = mBorderSize;
        mBorderPaint.setStrokeWidth(mBorderSize);
        initRect();
        invalidate();
    }

    public void setBorderColor(int mBorderColor) {
        this.mBorderColor = mBorderColor;
        mBorderPaint.setColor(mBorderColor);
        invalidate();
    }

    public void setRadius(float leftTopRadius, float rightTopRadius, float leftBottomRadius, float rightBottomRadius) {
        mRoundRadiusLeftBottom = leftBottomRadius;
        mRoundRadiusLeftTop = leftTopRadius;
        mRoundRadiusRightBottom = rightBottomRadius;
        mRoundRadiusRightTop = rightTopRadius;
    }
}
