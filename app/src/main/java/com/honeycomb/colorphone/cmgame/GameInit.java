package com.honeycomb.colorphone.cmgame;

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

        // 下面的 app id 要替换成小游戏官方分配的正式 app id
        final String appId = HSConfig.optString("huancailaidianxiu","Application", "CmGameId");
        final String baseUrl = HSConfig.optString("https://hcldx-xyx-sdk-svc.beike.cn","Application", "CmGameHost");

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

    @Override
    public boolean afterAppFullyDisplay() {
        return false;
    }
}
