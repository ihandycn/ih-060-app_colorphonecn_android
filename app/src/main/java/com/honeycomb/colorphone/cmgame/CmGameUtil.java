package com.honeycomb.colorphone.cmgame;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.honeycomb.colorphone.util.Analytics;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Navigations;
import com.superapps.util.Preferences;

public class CmGameUtil {
    public final static String TAG = "CmGame";
    public final static String CM_GAME_OPEN_TYPE_CLICK = "click";
    public final static String CM_GAME_OPEN_TYPE_SLIDE = "slide";
    public final static String BACK_TO_DESKTOP_TIMES_FOR_CM_GAME = "back_to_desktop_times_for_cm_game";

    public static boolean canUseCmGame() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && Build.VERSION.SDK_INT <= Build.VERSION_CODES.P;
    }

    public static boolean shouldShowGuide(){
        return Preferences.getDefault().incrementAndGetInt(BACK_TO_DESKTOP_TIMES_FOR_CM_GAME) == 3;
    }

    public static Intent getCmGameIntent(Context context) {
        return new Intent(context, CmGameActivity.class);
    }

    public static void startCmGameActivity(Context context, String openType) {
        if (canUseCmGame()) {
            try {
                Navigations.startActivitySafely(context, getCmGameIntent(context));
                Analytics.logEvent("GameCenter_Shown", "Openway", openType);
            } catch (Exception e){
                HSLog.e("CmGameCenter", "failed to open cm game center");
                e.printStackTrace();
            }
        }
    }

}
