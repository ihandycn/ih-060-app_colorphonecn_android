package com.honeycomb.colorphone.http;

import com.honeycomb.colorphone.http.bean.AllCategoryBean;
import com.honeycomb.colorphone.http.bean.AllThemeBean;
import com.honeycomb.colorphone.http.bean.AllUserThemeBean;
import com.honeycomb.colorphone.http.bean.LoginUserBean;
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
import retrofit2.http.Url;

public interface IHttpRequest {

    String BASE_URL = "https://colorphone-service.atcloudbox.com";
    String DEBUG_BASE_URR = "https://colorphone-service.atcloudbox.com";

    String GENDER_MAN = "man";
    String GENDER_WOMAN = "woman";

    int DEFAULT_PRE_PAGE = 20;

    @Headers({"Content-Type: application/json", "Accept: application/json"})
    @HTTP(method = "POST", path = "user/login", hasBody = true)
    Callable<LoginUserBean> login(@Body RequestBody body);

    @PUT("user/{uid}/profile")
    Callable<ResponseBody> editUserInfo(@Header("X-ColorPhone-Session-Token") String token, @Path("uid") String uid, @Body RequestBody body);

    @GET("user/{uid}/profile")
    Callable<LoginUserBean> getUserInfo(@Header("X-ColorPhone-Session-Token") String token, @Path("uid") String uid);

    @GET("shows")
    Callable<AllThemeBean> getAllThemes(@Query("per_page") int perPage, @Query("page_index") int pageIndex);

    @UploadMoreFiles
    @Headers("LogLevel:BASIC")
    @POST("user/{uid}/uploaded_shows")
    Callable<ResponseBody> uploadVideos(@Header("X-ColorPhone-Session-Token") String token, @Path("uid") String uid, @Body HashMap<String, Object> params);

    @GET("user/{uid}/uploaded_shows")
    Callable<AllUserThemeBean> getUserUploadedVideos(@Header("X-ColorPhone-Session-Token") String token, @Path("uid") String uid, @Query("per_page") int perPage, @Query("page_index") int pageIndex);

    @GET("user/{uid}/reviewed_shows")
    Callable<AllUserThemeBean> getUserPublishedVideos(@Header("X-ColorPhone-Session-Token") String token, @Path("uid") String uid, @Query("per_page") int perPage, @Query("page_index") int pageIndex);

    @Headers({"Content-Type: application/json", "Accept: application/json"})
    @HTTP(method = "DELETE", path = "user/{uid}/uploaded_shows", hasBody = true)
    Callable<ResponseBody> deleteUserVideos(@Header("X-ColorPhone-Session-Token") String token, @Path("uid") String uid, @Body RequestBody body);

    @GET("categories")
    Callable<AllCategoryBean> getAllCategories();


    @GET("category/{category_id}/shows")
    Callable<AllThemeBean> getCategoryThemes(@Path("category_id") String categoryId, @Query("per_page") int perPage, @Query("page_index") int pageIndex);

    @GET
    Callable<ResponseBody> getCallerAddress(@Url String url);
}

