package com.honeycomb.colorphone.customize;

import android.view.View;
import android.widget.FrameLayout;

/**
 * Interface for UI handlers (usually {@link android.app.Activity}) that can handle the job to install a
 * overlay view on top of itself.
 */
public interface OverlayInstaller {

    /**
     * Add the given overlayer to root of view hierarchy with given layout parameters.
     */
    void installOverlay(View overlay, FrameLayout.LayoutParams params);

    void uninstallOverlay(View overlay);

    /**
     * Obtain the view that is overlaid. This view can be used to obtain its blurred version.
     */
    View getOverlayee();
}
