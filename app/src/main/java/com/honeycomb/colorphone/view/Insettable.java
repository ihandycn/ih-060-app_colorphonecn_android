package com.honeycomb.colorphone.view;

import android.graphics.Rect;

/**
 * Allows the implementing {@link View} to not draw underneath system bars.
 * e.g., notification bar on top and home key area on the bottom.
 */
public interface Insettable {

    void setInsets(Rect insets);
}
