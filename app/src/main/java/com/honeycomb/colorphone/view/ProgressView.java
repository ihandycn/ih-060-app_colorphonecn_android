package com.honeycomb.colorphone.view;

/**
 * Created by sundxing on 17/7/4.
 */

public interface ProgressView {
    void setProgress(int i);

    void reset();

    void onDownloadStart();
}
