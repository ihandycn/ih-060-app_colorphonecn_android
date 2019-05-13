package com.honeycomb.colorphone.cmgame;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.bytedance.sdk.openadsdk.TTAdConfig;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.cmcm.cmgame.CmGameSdk;
import com.cmcm.cmgame.gamedata.CmGameAppInfo;
import com.honeycomb.colorphone.BuildConfig;
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
                Analytics.logEvent("GameCenter_Shown", true, "Openway", openType);
            } catch (Exception e){
                HSLog.e("CmGameCenter", "failed to open cm game center");
                e.printStackTrace();
            }
        }
    }

    public static void initGame(Application context) {
        final String adAppId = "5011513";

        TTAdSdk.init(context,
                new TTAdConfig.Builder()
                        .appId(adAppId)
                        //使用TextureView控件播放视频,默认为SurfaceView,当有SurfaceView冲突的场景，可以使用TextureView
                        .useTextureView(false)
                        .appName("焕彩桌面_android")
                        .titleBarTheme(TTAdConstant.TITLE_BAR_THEME_DARK)
                        //是否允许sdk展示通知栏提示
                        .allowShowNotify(true)
                        //是否在锁屏场景支持展示广告落地页
                        .allowShowPageWhenScreenLock(true)
                        //测试阶段打开，可以通过日志排查问题，上线时去除该调用
                        .debug(BuildConfig.DEBUG)
                        //允许直接下载的网络状态集合
                        .directDownloadNetworkType(TTAdConstant.NETWORK_STATE_WIFI, TTAdConstant.NETWORK_STATE_3G)
                        //是否支持多进程，true支持
                        .supportMultiProcess(false)
                        .build());

        // 下面的 app id 要替换成小游戏官方分配的正式 app id
        final String appId = "huancaizhuomian";
        final String baseUrl = "https://hczm-xyx-sdk-svc.cmcm.com";

        CmGameAppInfo cmGameAppInfo = new CmGameAppInfo();
        cmGameAppInfo.setAppId(appId);
        cmGameAppInfo.setAppHost(baseUrl);
        CmGameAppInfo.TTInfo ttInfo = new CmGameAppInfo.TTInfo();
        ttInfo.setRewardVideoId("911513342");
        ttInfo.setFullVideoId("911513706");
        ttInfo.setBannerId("911513290");
        ttInfo.setInterId("911513683");
        ttInfo.setInterEndId("911513683");//??
        cmGameAppInfo.setTtInfo(ttInfo);

        CmGameSdk.INSTANCE.initCmGameSdk(context, cmGameAppInfo, new CmGameImageLoader(), BuildConfig.DEBUG);

        Log.d("cmgamesdk", "current sdk version : " + CmGameSdk.INSTANCE.getVersion());
    }
}
