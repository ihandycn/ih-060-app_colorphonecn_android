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
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.honeycomb.colorphone.menu.SettingsPage;
import com.honeycomb.colorphone.util.Utils;
import com.honeycomb.colorphone.view.GlideApp;
import com.ihs.app.framework.activity.HSAppCompatActivity;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Calendar;

import okhttp3.ResponseBody;

public class UserInfoEditorActivity extends HSAppCompatActivity implements View.OnClickListener {
    private static final int RESULT_SELECT_IMAGE = 100;
    private static final int RESULT_CROP_IMAGE = 200;
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
    private File tempImageFile;
    private boolean imageCroped;

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
        if (userInfo != null) {
            userInfoEdited = cloneUserInfoBean(userInfo);
        } else {
            finish();
        }
        initView();
        initListener();

        imageCroped = false;
        tempImageFile = new File(getTempImagePath());
    }

    public static String getTempImagePath() {
        String tempImagePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (!tempImagePath.endsWith(File.separator)) {
            tempImagePath += File.separator;
        }
        tempImagePath += "temp_avatar_image.jpg";
        return tempImagePath;
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
                saveButton.setEnabled(false);
                Toast.makeText(this, "正在上传", Toast.LENGTH_SHORT).show();
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
        if (requestCode == RESULT_SELECT_IMAGE) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                cropPhoto(uri);
            }
        } else if (requestCode == RESULT_CROP_IMAGE) {
            GlideApp.with(this)
                    .clear(avatarView);
            String tempImagePath = tempImageFile.getAbsolutePath();
            avatarView.setImageBitmap(BitmapFactory.decodeFile(tempImagePath));
            headImagePath = tempImagePath;
            imageCroped = true;
            refreshButton();
        }
    }

    private void cropPhoto(Uri inputUri) {
        // 调用系统裁剪图片的 Action
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 900);
        intent.putExtra("outputY", 900);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", false);
        intent.setDataAndType(inputUri, "image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempImageFile));
        startActivityForResult(intent, RESULT_CROP_IMAGE);
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
        startActivityForResult(intent, RESULT_SELECT_IMAGE);
    }

    private void editUserInfo() {
        HttpManager.getInstance().editUserInfo(userInfoEdited, headImagePath, new Callback<ResponseBody>() {
            @Override
            public void onFailure(String errorMsg) {
                saveButton.setEnabled(true);
                Toast.makeText(UserInfoEditorActivity.this, "设置失败，请检查网络设置", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(ResponseBody responseBody) {
                finish();
                if (imageCroped && tempImageFile.exists()) {
                    imageCroped = false;
                    SettingsPage.avatarCropFinishied = true;
                }
                HttpManager.getInstance().refreshUserInfo(null);
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
