package com.honeycomb.colorphone.activity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.DatePicker;
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

import java.util.Calendar;

import okhttp3.ResponseBody;

public class UserInfoEditorActivity extends HSAppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_SELECT_IMAGE = 100;
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
    private String headImagePath;

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
        if (TextUtils.isEmpty(userInfo.getBirthday())) {
            birthdayEditor.setText("请选择你的生日");
        } else {
            birthdayEditor.setText(userInfo.getBirthday());
        }
        if (!TextUtils.isEmpty(userInfo.getSignature())) {
            signEditor.setText(userInfo.getSignature());
        }
        nickName.setSelection(nickName.getText().toString().length());
    }

    private void initListener() {
        avatarView.setOnClickListener(this);
        avatarText.setOnClickListener(this);
        saveButton.setOnClickListener(this);
        maleTicker.setOnClickListener(this);
        femaleTicker.setOnClickListener(this);
        birthdayEditor.setOnClickListener(this);
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
                if (editable.toString().length() == 15) {
                    Toast.makeText(UserInfoEditorActivity.this, "昵称不超过15个字...", Toast.LENGTH_SHORT).show();
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
                if (editable.toString().length() == 20) {
                    Toast.makeText(UserInfoEditorActivity.this, "签名不超过20个字...", Toast.LENGTH_SHORT).show();
                }
                refreshButton();
            }
        });
    }

    private void refreshButton() {
        saveButton.setEnabled(isInfoChanged());
        saveButton.setTextColor(isInfoChanged() ? Color.parseColor("#181818") : Color.parseColor("#73718f"));
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
            case R.id.user_info_editor_avatar:
            case R.id.user_info_editor_avatar_text:
                selectFromAlbumSingle();
                break;
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
            case R.id.birthday_editor:
                selectDate();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SELECT_IMAGE) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                String filePath = uri.toString();
                if (filePath.contains("content://")) {
                    filePath = getRealPathFromURI(uri);
                }
                avatarView.setImageBitmap(BitmapFactory.decodeFile(filePath));
                headImagePath = filePath;
                refreshButton();
            }
        }
    }

    private String getRealPathFromURI(Uri contentUri) { //传入图片uri地址
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(this, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private void selectFromAlbumSingle() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_SELECT_IMAGE);
    }

    private void editUserInfo() {
        HttpManager.getInstance().editUserInfo(userInfoEdited, headImagePath, new Callback<ResponseBody>() {
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

    private void selectDate() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                String dateString = "" + year + "-";
                if (month < 9) {
                    dateString += "0" + (month + 1);
                } else {
                    dateString += (month + 1);
                }
                if (dayOfMonth < 10) {
                    dateString += "-0" + dayOfMonth;
                } else {
                    dateString += "-" + dayOfMonth;
                }
                userInfoEdited.setBirthday(dateString);
                birthdayEditor.setText(dateString);
                refreshButton();
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
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
                userInfo.getSignature().equals(userInfoEdited.getSignature()) &&
                headImagePath == null);
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
