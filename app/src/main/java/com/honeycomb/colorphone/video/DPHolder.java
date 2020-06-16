package com.honeycomb.colorphone.video;

import android.content.Context;

import com.bytedance.applog.AppLog;
import com.bytedance.applog.InitConfig;
import com.bytedance.applog.util.UriConfig;
import com.bytedance.sdk.dp.BuildConfig;
import com.bytedance.sdk.dp.DPSdk;
import com.bytedance.sdk.dp.DPSdkConfig;
import com.bytedance.sdk.dp.DPWidgetDrawParams;
import com.bytedance.sdk.dp.DPWidgetGridParams;
import com.bytedance.sdk.dp.IDPWidget;
import com.bytedance.sdk.dp.IDPWidgetFactory;
import com.ihs.commons.utils.HSLog;

public final class DPHolder {
    private static volatile DPHolder sInstance;

    public static DPHolder getInstance() {
        if (sInstance == null) {
            synchronized (DPHolder.class) {
                if (sInstance == null) {
                    sInstance = new DPHolder();
                }
            }
        }
        return sInstance;
    }

    private DPHolder() {
    }

    public void init(Context context) {
        //先初始化applog，一定要在DPSdk之前进行初始化
        AppLog.setEnableLog(true);
        final InitConfig appLogConfig = new InitConfig("185363", "hcldx");
        appLogConfig.setUriConfig(UriConfig.DEFAULT);
        appLogConfig.setAbEnable(false);
        appLogConfig.setAutoStart(true);
        AppLog.init(context, appLogConfig);


        //1. 初始化，最好放到application.onCreate()执行
        //2. partner和secureKey请务必替换成自己的数据
        //3. 【重要】如果needInitAppLog=false，请确保AppLog初始化一定要在合作sdk初始化前
        final DPSdkConfig config = new DPSdkConfig.Builder()
                .debug(BuildConfig.DEBUG)
                .needInitAppLog(false)
                .partner("video_hcldx_sdk")
                .secureKey("bfa58b199aa228fa444be885782d9042")
                .appId("185363")
                .initListener(new DPSdkConfig.InitListener() {
                    @Override
                    public void onInitComplete(boolean isSuccess) {
                        //注意：1如果您的初始化没有放到application，请确保使用时初始化已经成功
                        //     2如果您的初始化在application，可以忽略该初始化接口
                        //isSuccess=true表示初始化成功
                        //初始化失败，可以再次调用初始化接口（建议最多不要超过3次)

                        HSLog.e("DPHolder", "init result=" + isSuccess);
                    }
                })
                .build();

        DPSdk.init(context, config);
    }

    public IDPWidget buildDrawWidget(DPWidgetDrawParams params) {
        //创建draw视频流组件
        return getFactory().createDraw(params);
    }

    public IDPWidget buildGridWidget(DPWidgetGridParams params) {
        //创建宫格组件
        return getFactory().createGrid(params);
    }

    private IDPWidgetFactory getFactory() {
        //一定要初始化后才能调用，否则会发生异常问题
        return DPSdk.factory();
    }

}
