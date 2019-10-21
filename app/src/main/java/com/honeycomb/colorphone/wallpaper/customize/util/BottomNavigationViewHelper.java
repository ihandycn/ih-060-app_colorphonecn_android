package com.honeycomb.colorphone.wallpaper.customize.util;

import android.graphics.Typeface;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;

import com.honeycomb.colorphone.util.Utils;
import com.ihs.commons.utils.HSLog;

import java.lang.reflect.Field;

public class BottomNavigationViewHelper {

    public static void disableShiftMode(BottomNavigationView view) {
        if (view == null) {
            return;
        }

        BottomNavigationMenuView menuView = (BottomNavigationMenuView) view.getChildAt(0);
        try {
            Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
            shiftingMode.setAccessible(true);
            shiftingMode.setBoolean(menuView, false);
            shiftingMode.setAccessible(false);
            for (int i = 0; i < menuView.getChildCount(); i++) {
                BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
                //noinspection RestrictedApi
                item.setShiftingMode(false);
                // set once again checked value, so view will be updated
                //noinspection RestrictedApi
                item.setChecked(item.getItemData().isChecked());
            }
        } catch (NoSuchFieldException e) {
            HSLog.e("BNVHelper", "Unable to get shift mode field: " + e);
        } catch (IllegalAccessException e) {
            HSLog.e("BNVHelper", "Unable to change value of shift mode: " + e);
        }
    }

    public static void setTypeface(BottomNavigationView navigationView, Typeface typeface) {
        com.honeycomb.colorphone.wallpaper.util.Utils.setTypefaceRecursive(navigationView, typeface);
    }
}