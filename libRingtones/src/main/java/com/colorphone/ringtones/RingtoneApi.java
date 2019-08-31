package com.colorphone.ringtones;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.colorphone.ringtones.bean.ColumnResultBean;
import com.colorphone.ringtones.bean.RingtoneListResultBean;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.connection.HSHttpConnection;
import com.ihs.commons.utils.HSError;
import com.ihs.commons.utils.HSLog;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RingtoneApi {
    public static final String TAG = RingtoneApi.class.getName();
    public static final String BASE_URL = "http://api.kuyinyun.com/p";
    public static final String URL_SEARCH = "/search";
    public static final String URL_COLUM_RES = "/q_colres";
    public static final String URL_COLUM = "/q_cols";

    // TODO
    private static final boolean DEBUG_REQUEST = false;
    private final Gson gson;

    private int pageSize = 20;

    public RingtoneApi() {
        gson = new GsonBuilder().registerTypeHierarchyAdapter(List.class, new ArraySecAdapter()).create();
    }

    public void search(String txt, int pageIndex, ResultCallback<RingtoneListResultBean> resultCallback) {
        if (TextUtils.isEmpty(txt)) {
            // Invalid
            return;
        }
        HashMap<String, String> map = new HashMap<>(1);
        map.put(RequestKeys.SEARCH_KEY, txt);
        map.put(RequestKeys.PAGE_INDEX, String.valueOf(pageIndex));
        map.put(RequestKeys.PAGE_SIZE, String.valueOf(pageSize));

        String url = buildUrl(URL_SEARCH, map);
        doRequest(url, RingtoneListResultBean.class, resultCallback);
    }

    public void requestBanners(ResultCallback<ColumnResultBean> resultCallback) {
        HashMap<String, String> map = new HashMap<>(1);
        map.put(RequestKeys.COLUMN_ID, getColumnId("Banner"));
        String url = buildUrl(URL_COLUM, map);
        doRequest(url, ColumnResultBean.class, resultCallback);
    }

    public void requestSubColumns(ResultCallback<ColumnResultBean> resultCallback) {
        HashMap<String, String> map = new HashMap<>(1);
        map.put(RequestKeys.COLUMN_ID, getColumnId("SubColumn"));
        String url = buildUrl(URL_COLUM, map);
        doRequest(url, ColumnResultBean.class, resultCallback);
    }

    public void requestRingtoneListById(String id, int pageIndex, ResultCallback<RingtoneListResultBean> resultCallback) {
        HashMap<String, String> map = new HashMap<>(1);
        map.put(RequestKeys.COLUMN_ID, id);
        map.put(RequestKeys.PAGE_INDEX, String.valueOf(pageIndex));
        map.put(RequestKeys.PAGE_SIZE, String.valueOf(pageSize));

        String url = buildUrl(URL_COLUM_RES, map);
        doRequest(url, RingtoneListResultBean.class, resultCallback);
    }

    private <T> void doRequest(final String url, final Class<T> clazz, final ResultCallback<T> callback) {
        HSHttpConnection connection = new HSHttpConnection(url);
        connection.setConnectionFinishedListener(new HSHttpConnection.OnConnectionFinishedListener() {
            @Override
            public void onConnectionFinished(HSHttpConnection hsHttpConnection) {
                HSLog.i(TAG, "responseCode: " + hsHttpConnection.getResponseCode() + "  msg: " + hsHttpConnection.getResponseMessage());
                if (hsHttpConnection.isSucceeded()) {
                    String jsonBody = hsHttpConnection.getBodyString();
                    if (DEBUG_REQUEST) {
                        HSLog.d(TAG, "【Request】" + url);
                        HSLog.d(TAG,"【Response】" + jsonBody);
                    }
                    T bean = gson.fromJson(jsonBody, clazz);
                    if (callback != null) {
                        callback.onFinish(bean);
                    }
                } else {
                    if (callback != null) {
                        callback.onFinish(null);
                    }
                }
            }

            @Override
            public void onConnectionFailed(HSHttpConnection hsHttpConnection, HSError hsError) {
                HSLog.i(TAG, "responseCode: " + hsHttpConnection.getResponseCode() + "  msg: " + hsHttpConnection.getResponseMessage());
                HSLog.i(TAG, "HSError: " + hsError);
                if (callback != null) {
                    callback.onFinish(null);
                }
            }
        });
        connection.startAsync();
    }

    public static String getSubscriptionUrl(String ringtoneToken) {
        return "https://iring.diyring.cc/friend/" + getAppId() + "?wno=" + ringtoneToken + "#login";
    }

    private static String buildUrl(String path, Map<String, String> params) {
        StringBuilder sb = new StringBuilder(BASE_URL);
        sb.append(path);
        sb.append("?");
        sb.append(RequestKeys.APP_KEY);
        sb.append("=");
        sb.append(getAppId());
        if (params != null) {
            for (Map.Entry entry : params.entrySet()) {
                sb.append("&");
                sb.append(entry.getKey());
                sb.append("=");
                sb.append(entry.getValue());
            }
        }
        String result = sb.toString();
        HSLog.d(TAG, "build url = " + result);
        return result;
    }


    public int getPageSize() {
        return pageSize;
    }

    public static String getAppId() {
        return HSConfig.optString("", "Application", "Ringtone", "AppId");
    }

    public static String getColumnId(String name) {
        return HSConfig.optString("", "Application", "Ringtone", "Column", name);
    }

    private static class ArraySecAdapter implements JsonDeserializer<List<?>> {

        @Override
        public List<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonArray()) {
                return new Gson().fromJson(json, typeOfT);
            } else {
                return Collections.emptyList();
            }
        }
    }

    public interface ResultCallback<T> {
        void onFinish(@Nullable  T bean);
    }
}
