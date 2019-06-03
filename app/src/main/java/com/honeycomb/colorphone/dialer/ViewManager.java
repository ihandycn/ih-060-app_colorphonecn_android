package com.honeycomb.colorphone.dialer;

import android.view.View;

public interface ViewManager {
    void onViewInit(InCallActivity activity, View root);
    void onViewDestroy();
}
