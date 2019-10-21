package com.honeycomb.colorphone.http;

import android.content.SharedPreferences;

import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.http.bean.AllThemeBean;
import com.honeycomb.colorphone.http.bean.AllUserThemeBean;
import com.honeycomb.colorphone.http.bean.LoginInfoBean;
import com.honeycomb.colorphone.http.bean.UserInfoBean;
import com.honeycomb.colorphone.http.bean.WeixinUserInfoBean;
import com.honeycomb.colorphone.http.lib.call.Callable;
import com.honeycomb.colorphone.http.lib.call.Callback;
import com.honeycomb.colorphone.http.lib.upload.FilesRequestBodyConverter;
import com.honeycomb.colorphone.http.lib.upload.UploadFileCallback;
import com.honeycomb.colorphone.http.lib.utils.HttpUtils;
import com.honeycomb.colorphone.http.lib.utils.RetrofitFactory;
import com.superapps.util.Preferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;

public final class HttpManager {

    private static final String PREF_USER_TOKEN = "pref_user_token";
    private static final String PREF_USER_ID = "pref_user_id";

    private Preferences preferences;
    private final Retrofit DEFAULT;

    private HttpManager() {
        preferences = Preferences.get(Constants.KEY_HTTP);
        DEFAULT = RetrofitFactory.getDefault();
    }

    private static class ClassHolder {
        private final static HttpManager INSTANCE = new HttpManager();
    }

    public static HttpManager getInstance() {
        return ClassHolder.INSTANCE;
    }

    public void login(WeixinUserInfoBean userInfoBean, Callback<LoginInfoBean> callback) {

        JSONObject params = new JSONObject();
        try {
            params.put("login_type", 1);

            JSONObject user = new JSONObject();
            user.put("name", userInfoBean.name);
            user.put("gender", userInfoBean.gender);
            user.put("province", userInfoBean.province);
            user.put("city", userInfoBean.city);
            user.put("country", userInfoBean.country);
            user.put("head_image_url", userInfoBean.head_image_url);
            user.put("unionid", userInfoBean.unionid);
            params.put("login_info", user);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = HttpUtils.getRequestBodyFromJson(params.toString());

        DEFAULT.create(IHttpRequest.class)
                .login(body)
                .enqueue(callback);
    }

    public void editUserInfo(UserInfoBean userInfo, String headImgFilePath, Callback<ResponseBody> callback) {
        File file = new File(headImgFilePath);
        if (!HttpUtils.isFileValid(file)) {
            return;
        }

        RequestBody fileBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        RequestBody body = new MultipartBody.Builder()
                .addFormDataPart("name", userInfo.name)
                .addFormDataPart("gender", userInfo.gender)
                .addFormDataPart("birthday", userInfo.brithday)
                .addFormDataPart("signature", userInfo.signature)
                .addFormDataPart("file", file.getName(), fileBody)
                .build();

        DEFAULT.create(IHttpRequest.class)
                .editUserInfo(getUserToken(), getSelfUserId(), body)
                .enqueue(callback);
    }

    public void getSelfUserInfo(Callback<UserInfoBean> callBack) {

        DEFAULT.create(IHttpRequest.class)
                .getUserInfo(getUserToken(), getSelfUserId())
                .enqueue(callBack);
    }

    public void getAllThemes(int pageIndex, Callback<AllThemeBean> callback) {
        DEFAULT.create(IHttpRequest.class)
                .getAllThemes(IHttpRequest.DEFAULT_PRE_PAGE, pageIndex)
                .enqueue(callback);
    }

    public Callable<ResponseBody> uploadVideos(String videoFilePath, String audioFilePath, String imageFilePath, String name, UploadFileCallback callback) {

        HashMap<String, String> fileMap = new HashMap<>();
        fileMap.put("video_file", videoFilePath);
        fileMap.put("audio_file", audioFilePath);
        fileMap.put("image_file", imageFilePath);

        HashMap<String, Object> map = new HashMap<>();
        map.put(FilesRequestBodyConverter.KEY_FILE_PATH_MAP, fileMap);
        map.put(FilesRequestBodyConverter.KEY_UPLOAD_FILE_CALLBACK, callback);
        map.put("file_name", name);
        Callable<ResponseBody> call = DEFAULT.create(IHttpRequest.class)
                .uploadVideos(getUserToken(), getSelfUserId(), map);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onFailure(String errorMsg) {
                failure(errorMsg);
            }

            @Override
            public void onSuccess(ResponseBody responseBody) {
                success();
            }

            private void success() {
                if (callback != null) {
                    callback.onSuccess();
                }
            }

            private void failure(String errorMsg) {
                if (callback != null) {
                    callback.onFailure(errorMsg);
                }
            }
        });
        return call;
    }

    public void getUserUploadedVideos(int pageIndex, Callback<AllUserThemeBean> callback) {
        DEFAULT.create(IHttpRequest.class)
                .getUserUploadedVideos(getUserToken(), getSelfUserId(), IHttpRequest.DEFAULT_PRE_PAGE, pageIndex)
                .enqueue(callback);
    }

    public void getUserPublishedVideos(int pageIndex, Callback<AllUserThemeBean> callback) {
        DEFAULT.create(IHttpRequest.class)
                .getUserPublishedVideos(getUserToken(), getSelfUserId(), IHttpRequest.DEFAULT_PRE_PAGE, pageIndex)
                .enqueue(callback);
    }

    public void deleteUserVideos(List<Long> themeIdList, Callback<ResponseBody> callback) {
        JSONObject params = new JSONObject();

        JSONArray array = new JSONArray();
        for (long id : themeIdList) {
            array.put(id);
        }
        try {
            params.put("show_id_list", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = HttpUtils.getRequestBodyFromJson(params.toString());
        DEFAULT.create(IHttpRequest.class)
                .deleteUserVideos(getUserToken(), getSelfUserId(), body)
                .enqueue(callback);
    }

    public void saveUserTokenAndUid(String token, String uid) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREF_USER_TOKEN, token);
        editor.putString(PREF_USER_ID, uid);
        editor.apply();
    }

    public String getUserToken() {
        return preferences.getString(PREF_USER_TOKEN, "");
    }

    public String getSelfUserId() {
        return preferences.getString(PREF_USER_ID, "");
    }


}
