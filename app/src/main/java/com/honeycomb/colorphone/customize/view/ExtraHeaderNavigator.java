package com.honeycomb.colorphone.customize.view;

import android.content.Context;

import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;

/**
 * @author sundxing
 */
public class ExtraHeaderNavigator extends CommonNavigator {
    private int headSize;
    private int curPosition;


    public ExtraHeaderNavigator(Context context) {
        super(context);
    }

    public void setHeadSize(int headSize) {
        this.headSize = headSize;
    }

    private boolean inHeaderPosition(int position) {
        return position < headSize;
    }

    @Override
    public void onPageSelected(int position) {
        curPosition = position;
        if (inHeaderPosition(position)) {
            return;
        }
        super.onPageSelected(position - headSize);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (inHeaderPosition(position)) {
            return;
        }
        super.onPageScrolled(position - headSize, positionOffset, positionOffsetPixels);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (inHeaderPosition(curPosition)) {
            return;
        }
        super.onPageScrollStateChanged(state);
    }
}
