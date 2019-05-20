package com.honeycomb.colorphone.cmgame;

import com.bytedance.sdk.openadsdk.TTAdConfig;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.cmcm.cmgame.CmGameSdk;
import com.cmcm.cmgame.gamedata.CmGameAppInfo;
import com.honeycomb.colorphone.AppMainInit;
import com.honeycomb.colorphone.BuildConfig;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;

/**
 * @author sundxing
 */
public class GameInit extends AppMainInit {

    @Override
    public void onInit(HSApplication application) {

        if (!CmGameUtil.canUseCmGame()) {
            return;
        }
        final String adAppId = "5011673";
        TTAdSdk.init(application,
                new TTAdConfig.Builder()
                        .appId(adAppId)
                        //使用TextureView控件播放视频,默认为SurfaceView,当有SurfaceView冲突的场景，可以使用TextureView
                        .useTextureView(false)
                        .appName("焕彩来电秀_android")
                        .titleBarTheme(TTAdConstant.TITLE_BAR_THEME_DARK)
                        //是否允许sdk展示通知栏提示
                        .allowShowNotify(true)
                        //是否在锁屏场景支持展示广告落地页
                        .allowShowPageWhenScreenLock(true)
                        //测试阶段打开，可以通过日志排查问题，上线时去除该调用
                        //允许直接下载的网络状态集合
                        .directDownloadNetworkType(TTAdConstant.NETWORK_STATE_WIFI,
                                TTAdConstant.NETWORK_STATE_3G,
                                TTAdConstant.NETWORK_STATE_4G)
                        //是否支持多进程，true支持
                        .supportMultiProcess(false)
                        .build());

        // 下面的 app id 要替换成小游戏官方分配的正式 app id
        final String appId = HSConfig.optString("","Application", "CmGameId");
        final String baseUrl = HSConfig.optString("","Application", "CmGameHost");

        CmGameAppInfo cmGameAppInfo = new CmGameAppInfo();
        cmGameAppInfo.setAppId(appId);
        cmGameAppInfo.setAppHost(baseUrl);
        CmGameAppInfo.TTInfo ttInfo = new CmGameAppInfo.TTInfo();
        ttInfo.setRewardVideoId("911673812");
        ttInfo.setFullVideoId("911673384");
        ttInfo.setBannerId("911673854");
        ttInfo.setInterId("911673989");
        ttInfo.setInterEndId("911673989");
        cmGameAppInfo.setTtInfo(ttInfo);

        CmGameSdk.INSTANCE.initCmGameSdk(application, cmGameAppInfo, new CmGameImageLoader(), BuildConfig.DEBUG);

        HSLog.d("cmgamesdk", "current sdk version : " + CmGameSdk.INSTANCE.getVersion());
    }
}
