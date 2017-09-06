package com.colorphone.lock.lockscreen;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by lz on 01/09/2017.
 */

public class LockScreenRootView extends FrameLayout {

    public LockScreenRootView(Context context) {
        this(context, null);
    }

    public LockScreenRootView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LockScreenRootView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void init() {

    }

    @Override protected void onFinishInflate() {
        super.onFinishInflate();

        int viewFlag = getSystemUiVisibility();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            viewFlag |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            viewFlag |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        }
        viewFlag |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        setSystemUiVisibility(viewFlag);
    }
}
