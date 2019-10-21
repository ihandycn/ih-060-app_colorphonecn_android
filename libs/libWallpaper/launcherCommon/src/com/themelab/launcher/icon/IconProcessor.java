package com.themelab.launcher.icon;

import android.graphics.Bitmap;

/**
 * Interface for strategy object used by {@link com.themelab.launcher.IconProcessProvider}.
 */
public interface IconProcessor {

    /**
     * Notice: this method shall be implemented in a way that handles concurrent invocation correctly and efficiently.
     * By "efficiently" a lock-free implementation is preferred as a simple global lock will cancel all performance
     * gain from concurrency. Use a {@link ThreadLocal} for expensive objects that needs to be shared across multiple calls.
     */
    Bitmap processIcon(Bitmap original);
}
