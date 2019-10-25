package com.honeycomb.colorphone.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
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
    private LoginUserBean.UserInfoBean userInfoEdited;
    private ImageView avatarView;
    private TextView avatarText;
    private EditText nickName;
    private TextView maleTicker;
    private TextView femaleTicker;
    private TextView birthdayEditor;
    private TextView signEditor;
    private TextView saveButton;

    public static void start(Context context, LoginUserBean.UserInfoBean userInfoBean) {
        Intent starter = new Intent(context, UserInfoEditorActivity.class);
        starter.putExtra("user_info", userInfoBean);
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
        userInfoEdited = cloneUserInfoBean(userInfo);

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
        setGender(!userInfo.getGender().equalsIgnoreCase(IHttpRequest.GENDER_WOMAN));

    }

    private void initListener() {
        saveButton.setOnClickListener(this);
        maleTicker.setOnClickListener(this);
        femaleTicker.setOnClickListener(this);
        nickName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                userInfoEdited.setName(editable.toString());
                if (editable.toString().length()==15){
                    Toast.makeText(UserInfoEditorActivity.this,"昵称不超过15个字...",Toast.LENGTH_SHORT).show();
                }
                refreshButton();
            }
        });
        signEditor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                userInfoEdited.setSignature(editable.toString());
                if (editable.toString().length()==20){
                    Toast.makeText(UserInfoEditorActivity.this,"签名不超过20个字...",Toast.LENGTH_SHORT).show();
                }
                refreshButton();
            }
        });
    }

    private void refreshButton() {
        saveButton.setEnabled(isInfoChanged());
        saveButton.setTextColor(isInfoChanged()? Color.parseColor("#181818"):Color.parseColor("#73718f"));
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
        switch (view.getId()) {
            case R.id.save_button:
                editUserInfo();
                finish();
                break;
            case R.id.male_ticker:
                setGender(true);
                break;
            case R.id.female_ticker:
                setGender(false);
                break;
            case R.id.user_info_editor_avatar:
            case R.id.user_info_editor_avatar_text:
                break;
        }
    }

    private void editUserInfo() {
        HttpManager.getInstance().editUserInfo(userInfoEdited, null, new Callback<ResponseBody>() {
            @Override
            public void onFailure(String errorMsg) {
                Toast.makeText(UserInfoEditorActivity.this, "设置失败，请检查网络设置", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(ResponseBody responseBody) {
                finish();
                HttpManager.getInstance().refreshUserInfo();
                Toast.makeText(UserInfoEditorActivity.this, "设置成功", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private LoginUserBean.UserInfoBean cloneUserInfoBean(LoginUserBean.UserInfoBean userInfoBean) {
        LoginUserBean.UserInfoBean userInfoBeanNew = new LoginUserBean.UserInfoBean();
        userInfoBeanNew.setName(userInfoBean.getName());
        userInfoBeanNew.setGender(userInfoBean.getGender());
        userInfoBeanNew.setBirthday(userInfoBean.getBirthday());
        userInfoBeanNew.setSignature(userInfoBean.getSignature());

        return userInfoBeanNew;
    }

    private boolean isInfoChanged() {
        return !(userInfo.getName().equals(userInfoEdited.getName()) &&
                userInfo.getGender().equalsIgnoreCase(userInfoEdited.getGender()) &&
                userInfo.getBirthday().equals(userInfoEdited.getBirthday()) &&
                userInfo.getSignature().equals(userInfoEdited.getSignature()));
    }

    private void setGender(boolean isMan) {
        Drawable drawableMale;
        Drawable drawableFemale;
        if (isMan) {
            drawableMale = getResources().getDrawable(R.drawable.icon_information_selected);
            drawableFemale = getResources().getDrawable(R.drawable.icon_information_unselected);
            userInfoEdited.setGender(IHttpRequest.GENDER_MAN);
        } else {
            drawableMale = getResources().getDrawable(R.drawable.icon_information_unselected);
            drawableFemale = getResources().getDrawable(R.drawable.icon_information_selected);
            userInfoEdited.setGender(IHttpRequest.GENDER_WOMAN);
        }
        maleTicker.setCompoundDrawablesWithIntrinsicBounds(null, null, drawableMale, null);
        femaleTicker.setCompoundDrawablesWithIntrinsicBounds(null, null, drawableFemale, null);
        refreshButton();
    }
}
