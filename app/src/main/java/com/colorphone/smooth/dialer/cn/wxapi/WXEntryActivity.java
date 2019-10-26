package com.colorphone.smooth.dialer.cn.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.honeycomb.colorphone.activity.LoginActivity;
import com.honeycomb.colorphone.http.HttpManager;
import com.honeycomb.colorphone.http.bean.LoginUserBean;
import com.honeycomb.colorphone.http.lib.call.Callback;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSBundle;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
    public static final String NOTIFY_REFRESH_USER_INFO = "notify_refresh_user_info";
    public static final String KEY_USER_INFO = "key_user_info";
    private IWXAPI api;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = WXAPIFactory.createWXAPI(this, LoginActivity.APP_ID, true);
        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq baseReq) {
    }

    @Override
    public void onResp(BaseResp resp) {
        if (resp.getType() == ConstantsAPI.COMMAND_SENDAUTH) {
            SendAuth.Resp authResp = (SendAuth.Resp) resp;
            final String code = authResp.code;
            HttpManager.getInstance().login(code, new Callback<LoginUserBean>() {
                @Override
                public void onFailure(String errorMsg) {
                    failure(errorMsg);
                }

                @Override
                public void onSuccess(LoginUserBean loginInfoBean) {
                    if (loginInfoBean != null && loginInfoBean.getUser_info() != null) {
                        HttpManager.getInstance().saveUserTokenAndUid(loginInfoBean.getToken(), loginInfoBean.getUser_info().getUser_id());
                    }
                    success();
                    HSBundle bundle = new HSBundle();
                    LoginUserBean.UserInfoBean userInfo=null;
                    if (loginInfoBean!=null){
                        userInfo = loginInfoBean.getUser_info();
                    }
                    bundle.putObject(KEY_USER_INFO,userInfo);
                    HSGlobalNotificationCenter.sendNotification(NOTIFY_REFRESH_USER_INFO,bundle);
                }
            });
            Toast.makeText(this, "正在登录", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void success() {
        Toast.makeText(this, "登录成功", Toast.LENGTH_LONG).show();
    }

    private void failure(String msg) {
        Toast.makeText(this, "登录失败, msg = " + msg, Toast.LENGTH_LONG).show();
    }
}
