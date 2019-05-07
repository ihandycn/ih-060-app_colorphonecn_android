package com.colorphone.lock.fullscreen.phone;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.annotation.RequiresApi;
import android.view.DisplayCutout;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;

import com.colorphone.lock.fullscreen.core.AbsNotchScreenSupport;
import com.colorphone.lock.fullscreen.core.OnNotchCallBack;
import com.colorphone.lock.fullscreen.helper.NotchStatusBarUtils;

/**
 * targetApi>=28才能使用API，有的手机厂商在P上会放弃O适配方案，暂时针对P手机不做特殊处理
 * @author zhangzhun
 * @date 2018/11/5
 */

@RequiresApi(api = 28)
public class PVersionNotchScreen extends AbsNotchScreenSupport {

    @Override
    public boolean isNotchScreen(Window window) {
        WindowInsets windowInsets = window.getDecorView().getRootWindowInsets();
        if (windowInsets == null) {
            return false;
        }

        DisplayCutout displayCutout = windowInsets.getDisplayCutout();
        if(displayCutout == null || displayCutout.getBoundingRects() == null){
            return false;
        }

        return true;
    }

    @Override
    public int getNotchHeight(Window window) {
        int notchHeight = 0;
        WindowInsets windowInsets = window.getDecorView().getRootWindowInsets();
        if (windowInsets == null) {
            return 0;
        }

        DisplayCutout displayCutout = windowInsets.getDisplayCutout();
        if(displayCutout == null || displayCutout.getBoundingRects() == null){
            return 0;
        }

        notchHeight = displayCutout.getSafeInsetTop();

        return notchHeight;
    }

    @Override
    public void fullScreenDontUseStatus(Activity activity, OnNotchCallBack notchCallBack) {
        super.fullScreenDontUseStatus(activity, notchCallBack);
        WindowManager.LayoutParams attributes = activity.getWindow().getAttributes();
        attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        activity.getWindow().setAttributes(attributes);
        NotchStatusBarUtils.setFakeNotchView(activity.getWindow());
    }

    @Override
    public void fullScreenUseStatus(Activity activity, OnNotchCallBack notchCallBack) {
        super.fullScreenUseStatus(activity, notchCallBack);
        WindowManager.LayoutParams attributes = activity.getWindow().getAttributes();
        attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        activity.getWindow().setAttributes(attributes);
    }

    @Override
    protected void onBindCallBackWithNotchProperty(final Window window, final OnNotchCallBack notchCallBack) {
        window.getDecorView().setOnApplyWindowInsetsListener( new View.OnApplyWindowInsetsListener() {
            @SuppressLint("NewApi")
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                window.getDecorView().setOnApplyWindowInsetsListener(null);
                PVersionNotchScreen.super.onBindCallBackWithNotchProperty(window, notchCallBack);
                return v.onApplyWindowInsets(insets);
            }
        });
    }
}
