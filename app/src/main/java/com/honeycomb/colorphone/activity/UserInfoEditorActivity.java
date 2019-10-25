package com.honeycomb.colorphone.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.http.HttpManager;
import com.honeycomb.colorphone.http.IHttpRequest;
import com.honeycomb.colorphone.http.bean.LoginUserBean;
import com.honeycomb.colorphone.http.lib.call.Callback;
import com.honeycomb.colorphone.util.Utils;
import com.honeycomb.colorphone.view.GlideApp;
import com.ihs.app.framework.activity.HSAppCompatActivity;

import org.jetbrains.annotations.NotNull;

import okhttp3.ResponseBody;

public class UserInfoEditorActivity extends HSAppCompatActivity implements View.OnClickListener {
    private LoginUserBean.UserInfoBean userInfo;
    private ImageView avatarView;
    private TextView avatarText;
    private EditText nickName;
    private TextView maleTicker;
    private TextView femaleTicker;
    private TextView birthdayEditor;
    private TextView signEditor;
    private TextView saveButton;

    public static void start(Context context,LoginUserBean.UserInfoBean userInfoBean) {
        Intent starter = new Intent(context, UserInfoEditorActivity.class);
        starter.putExtra("user_info",userInfoBean);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_user_info_editor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.edit_user_info);
        Utils.configActivityStatusBar(this, toolbar, R.drawable.back_dark);
        userInfo = (LoginUserBean.UserInfoBean) getIntent().getSerializableExtra("user_info");

        initView();
        initListener();
    }

    private void initView() {
        avatarView = findViewById(R.id.user_info_editor_avatar);
        avatarText = findViewById(R.id.user_info_editor_avatar_text);
        nickName = findViewById(R.id.nick_name_editor);
        maleTicker = findViewById(R.id.male_ticker);
        femaleTicker = findViewById(R.id.female_ticker);
        birthdayEditor = findViewById(R.id.birthday_editor);
        signEditor = findViewById(R.id.sign_editor);
        saveButton = findViewById(R.id.save_button);

        GlideApp.with(this)
                .asBitmap()
                .load(userInfo.getHead_image_url())
                .into(avatarView);
        nickName.setText(userInfo.getName());
    }

    private void initListener() {
        saveButton.setOnClickListener(this);
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
            case R.id.save_button:
                editUserInfo();
                finish();
                break;
            case R.id.user_info_editor_avatar:
            case R.id.user_info_editor_avatar_text:
                break;
        }
    }

    private void editUserInfo() {
        LoginUserBean.UserInfoBean userInfoBean = new LoginUserBean.UserInfoBean();
        userInfoBean.setName(nickName.getText().toString());
        userInfoBean.setGender(IHttpRequest.GENDER_MAN);
        userInfoBean.setSignature(signEditor.getText().toString());
        HttpManager.getInstance().editUserInfo(userInfoBean, null, new Callback<ResponseBody>() {
            @Override
            public void onFailure(String errorMsg) {
                Toast.makeText(UserInfoEditorActivity.this,"设置失败，请检查网络设置",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(ResponseBody responseBody) {
                finish();
                HttpManager.getInstance().refreshUserInfo();
                Toast.makeText(UserInfoEditorActivity.this,"设置成功",Toast.LENGTH_SHORT).show();
            }
        });
    }
}
