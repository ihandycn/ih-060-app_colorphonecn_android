package com.honeycomb.colorphone.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.http.HttpManager;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.Threads;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.jetbrains.annotations.NotNull;

public class LoginActivity extends HSAppCompatActivity implements View.OnClickListener {

    public static final String APP_ID = "wx5b4bb859043c5e3d";
    private IWXAPI api;
    public static void start(Context context) {
        Intent starter = new Intent(context, LoginActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.login_title);
        Utils.configActivityStatusBar(this, toolbar, R.drawable.back_dark);

        LinearLayout weixinLoginButton = findViewById(R.id.weixin_login_button);
        weixinLoginButton.setOnClickListener(this);
        weixinLoginButton.setBackground(BackgroundDrawables.createBackgroundDrawable(0xff19ad3c, Dimensions.pxFromDp(21),true));

        api = WXAPIFactory.createWXAPI(this,APP_ID,true);
        api.registerApp(APP_ID);
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.weixin_login_button:

                SendAuth.Req req = new SendAuth.Req();
                req.scope = "snsapi_userinfo";
                req.state = System.currentTimeMillis()+"";
                api.sendReq(req);
                Threads.postOnMainThreadDelayed(this::finish,500);
                break;
            default:
                break;
        }
    }
}
