package com.honeycomb.colorphone.http;

import com.honeycomb.colorphone.http.bean.AllThemeBean;
import com.honeycomb.colorphone.http.bean.AllUserThemeBean;
import com.honeycomb.colorphone.http.bean.LoginInfoBean;
import com.honeycomb.colorphone.http.bean.UserBean;
import com.honeycomb.colorphone.http.lib.call.Callable;
import com.honeycomb.colorphone.http.lib.upload.UploadMoreFiles;

import java.util.HashMap;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface IHttpRequest {

    String BASE_URL = "http://3.225.45.136/";

    String GENDER_MAN = "man";
    String GENDER_WOMAN = "woman";

    int DEFAULT_PRE_PAGE = 20;

    @Headers({"Content-Type: application/json", "Accept: application/json"})
    @POST("user/login")
    Callable<LoginInfoBean> login(@Body RequestBody body);

    @PUT("user/{uid}/profile")
    Callable<ResponseBody> editUserInfo(@Header("X-ColorPhone-Session-Token") String token, @Path("uid") String uid, @Body RequestBody body);

    @GET("user/{uid}/profile")
    Callable<UserBean> getUserInfo(@Header("X-ColorPhone-Session-Token") String token, @Path("uid") String uid);

    @GET("shows")
    Callable<AllThemeBean> getAllThemes(@Query("per_page") int perPage, @Query("page_index") int pageIndex);

    @UploadMoreFiles
    @POST("user/{uid}/uploaded_shows")
    Callable<ResponseBody> uploadVideos(@Header("X-ColorPhone-Session-Token") String token, @Path("uid") String uid, @Body HashMap<String, Object> params);

    @GET("user/{uid}/uploaded_shows")
    Callable<AllUserThemeBean> getUserUploadedVideos(@Header("X-ColorPhone-Session-Token") String token, @Path("uid") String uid, @Query("per_page") int perPage, @Query("page_index") int pageIndex);

    @GET("user/{uid}/reviewed_shows")
    Callable<AllUserThemeBean> getUserPublishedVideos(@Header("X-ColorPhone-Session-Token") String token, @Path("uid") String uid, @Query("per_page") int perPage, @Query("page_index") int pageIndex);

    @Headers({"Content-Type: application/json", "Accept: application/json"})
    @HTTP(method = "DELETE", path = "user/{uid}/uploaded_shows", hasBody = true)
    Callable<ResponseBody> deleteUserVideos(@Header("X-ColorPhone-Session-Token") String token, @Path("uid") String uid, @Body RequestBody body);
}

