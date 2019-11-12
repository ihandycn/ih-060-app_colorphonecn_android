package com.honeycomb.colorphone.wallpaper.util;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;

 import com.honeycomb.colorphone.R;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.Dimensions;
import com.superapps.view.SelectorDrawable;

public class RippleUtils {

    public static Drawable createRippleDrawable(int color) {
        return createRippleDrawable(color, 0);
    }

    public static Drawable createRippleDrawable(int shapeColor, int radius) {
        return createRippleDrawable(shapeColor, HSApplication.getContext().getResources().getColor(R.color.know_wallpaper_more_ripple), radius);
    }

    /**
     * Specifies radii for each of the 4 corners. For each corner, the array
     * contains 2 values, <code>[X_radius, Y_radius]</code>. The corners are
     * ordered top-left, top-right, bottom-right, bottom-left. This property
     * <p>
     * <strong>Note</strong>: changing this property will affect all instances
     * of a drawable loaded from a resource. It is recommended to invoke
     *
     * @param radiusArray an array of length >= 8 containing 4 pairs of X and Y
     *                    radius for each corner, specified in pixels
     * @see {@link GradientDrawable#setCornerRadii(float[])}
     */
    public static Drawable createRippleDrawable(int shapeColor, int rippleColor, float[] radiusArray) {
        return createRippleDrawable(new GradientDrawable(), shapeColor, rippleColor, 0, radiusArray);
    }

    public static Drawable createRippleDrawable(int shapeColor, int rippleColor, int radius) {
        return createRippleDrawable(new GradientDrawable(), shapeColor, rippleColor, radius, null);
    }

    private static Drawable createRippleDrawable(GradientDrawable shape, int shapeColor, int rippleColor, int radius, float[] radii) {
        shape.setColor(shapeColor);
        if (radius != 0) {
            shape.setCornerRadius(Dimensions.pxFromDp(radius));
        }
        if (radii != null) {
            shape.setCornerRadii(radii);
        }
        shape.setShape(GradientDrawable.RECTANGLE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            RippleDrawable rippleDrawable = new RippleDrawable(ColorStateList.valueOf(rippleColor), shape, null);
            return rippleDrawable;
        } else {
            return new SelectorDrawable(shape);
        }
    }

    public static Drawable createRippleDrawable(GradientDrawable shape, int shapeColor) {
        return createRippleDrawable(shape, shapeColor, HSApplication.getContext().getResources().getColor(R.color.know_wallpaper_more_ripple), 0, null);
    }
}
