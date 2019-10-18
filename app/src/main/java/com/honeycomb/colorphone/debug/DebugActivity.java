package com.honeycomb.colorphone.debug;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.http.HttpManager;
import com.honeycomb.colorphone.http.IHttpRequest;
import com.honeycomb.colorphone.http.bean.AllThemeBean;
import com.honeycomb.colorphone.http.bean.AllUserThemeBean;
import com.honeycomb.colorphone.http.bean.LoginInfoBean;
import com.honeycomb.colorphone.http.bean.UserInfoBean;
import com.honeycomb.colorphone.http.bean.WeixinUserInfoBean;
import com.honeycomb.colorphone.http.lib.call.Callable;
import com.honeycomb.colorphone.http.lib.call.Callback;
import com.honeycomb.colorphone.http.lib.upload.UploadFileCallback;
import com.ihs.commons.utils.HSLog;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;

public class DebugActivity extends Activity {

    private static final String TAG = "DebugActivity";

    private Callable<ResponseBody> uploadCall = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug_layout);

        findViewById(R.id.login_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        findViewById(R.id.edit_user_info_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editUserInfo();
            }
        });
        findViewById(R.id.get_user_info_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUserInfo();
            }
        });
        findViewById(R.id.get_show_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getShow();
            }
        });
        findViewById(R.id.upload_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload();
            }
        });
        findViewById(R.id.cancel_upload_btn).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                cancelUpload();
            }
        });
        findViewById(R.id.get_upload_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUpload();
            }
        });
        findViewById(R.id.get_publish_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPublish();
            }
        });

        findViewById(R.id.delete_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete();
            }
        });

    }

    private void login() {

        WeixinUserInfoBean bean = new WeixinUserInfoBean();
        bean.name = "ihandy";
        bean.city = "hhhh";
        bean.country = "ddd";
        bean.province = "dddd";
        bean.gender = IHttpRequest.GENDER_MAN;
        bean.head_image_url = "https://img-my.csdn.net/uploads/201407/26/1406383166_3407.jpg";
        bean.unionid = "o6_bmasdasdsad6_2sgVt7hMZOPfL";

        HttpManager.getInstance().login(bean, new Callback<LoginInfoBean>() {
            @Override
            public void onFailure(String errorMsg) {
                failure(errorMsg);
            }

            @Override
            public void onSuccess(LoginInfoBean loginInfoBean) {
                // Must to save token and uid
                if (loginInfoBean != null && loginInfoBean.user_info != null) {
                    HttpManager.getInstance().saveUserTokenAndUid(loginInfoBean.token, loginInfoBean.user_info.uid);
                }
                success();

            }
        });
    }

    private void editUserInfo() {

        UserInfoBean userInfoBean = new UserInfoBean();
        userInfoBean.name = "hhhhh";
        userInfoBean.brithday = "1993-09-23";
        userInfoBean.gender = IHttpRequest.GENDER_MAN;
        userInfoBean.signature = "fadj fslkdfsaf fasdfa";

        String headImgFilePath = "";

        HttpManager.getInstance().editUserInfo(userInfoBean, headImgFilePath, new Callback<ResponseBody>() {
            @Override
            public void onFailure(String errorMsg) {
                failure(errorMsg);
            }

            @Override
            public void onSuccess(ResponseBody responseBody) {
                success();
            }
        });
    }

    private void getUserInfo() {
        HttpManager.getInstance().getSelfUserInfo(new Callback<UserInfoBean>() {
            @Override
            public void onFailure(String errorMsg) {
                failure(errorMsg);
            }

            @Override
            public void onSuccess(UserInfoBean bean) {
                success();
            }
        });
    }

    private void getShow() {
        HttpManager.getInstance().getAllThemes(1, new Callback<AllThemeBean>() {
            @Override
            public void onFailure(String errorMsg) {
                failure(errorMsg);
            }

            @Override
            public void onSuccess(AllThemeBean allThemeBean) {
                success();
            }
        });
    }

    private void upload() {

        String videoFilePath = "";
        String audioFilePath = "";
        String imageFilePath = "";
        String name = "";

        uploadCall = HttpManager.getInstance().uploadVideos(videoFilePath, audioFilePath, imageFilePath, name, new UploadFileCallback() {
            @Override
            public void onSuccess() {
                success();
            }

            @Override
            public void onUpload(long length, long current, boolean isDone) {
                HSLog.e(TAG, "oUpload: length = " + ", current = " + current + ", isDone = " + isDone);
            }

            @Override
            public void onFailure(String errorMsg) {
                failure(errorMsg);
            }
        });
    }

    private void cancelUpload() {
        if (uploadCall != null) {
            uploadCall.cancel();
            uploadCall = null;
        }
    }

    private void getUpload() {
        HttpManager.getInstance().getUserUploadedVideos(1, new Callback<AllUserThemeBean>() {
            @Override
            public void onFailure(String errorMsg) {
                failure(errorMsg);
            }

            @Override public void onSuccess(AllUserThemeBean allUserThemeBean) {
                success();
            }
        });
    }

    private void getPublish() {
        HttpManager.getInstance().getUserPublishedVideos(1, new Callback<AllUserThemeBean>() {
            @Override
            public void onFailure(String errorMsg) {
                failure(errorMsg);
            }

            @Override
            public void onSuccess(AllUserThemeBean allUserThemeBean) {
                success();
            }
        });
    }

    private void delete() {
        List<Long> themeIdList = new ArrayList<>();
        themeIdList.add(10003L);
        HttpManager.getInstance().deleteUserVideos(themeIdList, new Callback<ResponseBody>() {
            @Override
            public void onFailure(String errorMsg) {
                failure(errorMsg);
            }

            @Override
            public void onSuccess(ResponseBody responseBody) {
                success();
            }
        });

    }

    private void success() {
        Toast.makeText(DebugActivity.this, "Successfully!!!", Toast.LENGTH_LONG).show();
    }

    private void failure(String msg) {
        Toast.makeText(DebugActivity.this, "Failure!!!, msg = " + msg, Toast.LENGTH_LONG).show();
    }
}
