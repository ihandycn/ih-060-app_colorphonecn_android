package com.colorphone.lock.fullscreen.core;

import android.app.Activity;
import android.view.Window;

import com.colorphone.lock.fullscreen.helper.NotchStatusBarUtils;


/**
 * @author zhangzhun
 * @date 2018/11/4
 */

public abstract class AbsNotchScreenSupport implements INotchSupport {

    @Override
    public int getStatusHeight(Window window) {
        return NotchStatusBarUtils.getStatusBarHeight(window.getContext());
    }

    @Override
    public void fullScreenDontUseStatus(Activity activity, OnNotchCallBack notchCallBack) {
        NotchStatusBarUtils.setFullScreenWithSystemUi(activity.getWindow(), false);
        onBindCallBackWithNotchProperty(activity.getWindow(), notchCallBack);
    }

    @Override
    public void fullScreenDontUseStatusForPortrait(Activity activity, OnNotchCallBack notchCallBack) {
        fullScreenDontUseStatus(activity, notchCallBack);
    }

    @Override
    public void fullScreenDontUseStatusForLandscape(Activity activity, OnNotchCallBack notchCallBack) {
        fullScreenDontUseStatus(activity, notchCallBack);
    }

    @Override
    public void fullScreenUseStatus(Activity activity, OnNotchCallBack notchCallBack) {
        NotchStatusBarUtils.setFullScreenWithSystemUi(activity.getWindow(), false);
        onBindCallBackWithNotchProperty(activity.getWindow(), notchCallBack);
    }

    protected void onBindCallBackWithNotchProperty(Window window, OnNotchCallBack notchCallBack) {
        if (notchCallBack != null) {
            NotchProperty notchProperty = new NotchProperty();
            notchProperty.setNotchHeight(getNotchHeight(window));
            notchProperty.setNotch(isNotchScreen(window));
            notchProperty.setMarginTop(notchProperty.geNotchHeight());
            notchCallBack.onNotchPropertyCallback(notchProperty);
        }
    }
}
