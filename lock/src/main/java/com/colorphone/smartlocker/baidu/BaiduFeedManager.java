package com.colorphone.smartlocker.baidu;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.support.annotation.StringDef;
import android.support.v4.BuildConfig;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.colorphone.smartlocker.utils.MD5Utils;
import com.colorphone.smartlocker.utils.NetworkUtils;
import com.google.gson.Gson;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.connection.HSHttpConnection;
import com.ihs.commons.connection.httplib.HttpRequest;
import com.ihs.commons.utils.HSError;
import com.ihs.commons.utils.HSLog;

import org.json.JSONObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.TELEPHONY_SERVICE;

public class BaiduFeedManager {

    private static final String TAG = "BaiduFeedNewsManager";

    public interface DataBackListener {
        void onDataBack(JSONObject response);
    }

    //    private static final String TOKEN = HSConfig.getString("Application", "BaiduFeed", "Secret");
//    private static final String APPSID = HSConfig.getString("Application", "BaiduFeed", "Appsid");
    private static final String TOKEN = "73cd34c8ff2f1d3895835cdf5";
    private static final String APPSID = "f0c1081d";
    private static final String URL = "https://cpu-openapi.baidu.com/api/v2/data/list";

    private static String ANDROID = Settings.Secure.getString(HSApplication.getContext().getContentResolver(),
            Settings.Secure.ANDROID_ID);

    public static final int LOAD_FIRST = 0;
    public static final int LOAD_MORE = 1;
    public static final int LOAD_REFRESH = 2;

    private String category;

    @StringDef({
            CATEGORY_ALL,
            CATEGORY_NEWS_HOT,
            CATEGORY_NEWS_LOCAL,
            CATEGORY_NEWS_ENTERTAINMENT,
            CATEGORY_NEWS_TECH,
            CATEGORY_NEWS_CAR,
            CATEGORY_NEWS_FINANCE,
            CATEGORY_NEWS_MILITARY,
            CATEGORY_NEWS_SPORTS,
            CATEGORY_NEWS_HEALTH,
            CATEGORY_NEWS_HOUSE
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface BaiduCategory {
    }

    public static final String CATEGORY_ALL = "0"; // 推荐
    public static final String CATEGORY_NEWS_HOT = "3"; // 热点
    public static final String CATEGORY_NEWS_LOCAL = "6"; // 本地
    public static final String CATEGORY_NEWS_ENTERTAINMENT = "1001"; // 娱乐
    public static final String CATEGORY_NEWS_TECH = "1013"; // 科技
    public static final String CATEGORY_NEWS_CAR = "1007"; // 懂车帝
    public static final String CATEGORY_NEWS_FINANCE = "1006"; // 财经
    public static final String CATEGORY_NEWS_MILITARY = "1012"; // 军事
    public static final String CATEGORY_NEWS_SPORTS = "1002"; // 体育
    public static final String CATEGORY_NEWS_HEALTH = "1014"; // 健康
    public static final String CATEGORY_NEWS_HOUSE = "1008"; // 房产

    private int pageIndex;

    private static BaiduFeedManager manager;

    private BaiduNewsRequest baiduNewsRequest;

    private BaiduFeedManager() {
        baiduNewsRequest = new BaiduNewsRequest();
    }

    public static BaiduFeedManager getInstance() {
        if (manager == null) {
            synchronized (BaiduFeedManager.class) {
                if (manager == null) {
                    manager = new BaiduFeedManager();
                }
            }
        }

        return manager;
    }

    public void loadNews(@BaiduCategory final String category, int loadType, final DataBackListener listener) {

        this.category = category;

        switch (loadType) {
            case LOAD_MORE:
                pageIndex++;
                break;
            case LOAD_FIRST:
            case LOAD_REFRESH:
            default:
                pageIndex = 1;
                break;
        }

        HSHttpConnection connection = new HSHttpConnection(URL, HttpRequest.Method.POST);
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.addHeader("Content-Type", "application/json");
        connection.setRequestBody(initJson());
        connection.setConnectionFinishedListener(new HSHttpConnection.OnConnectionFinishedListener() {
            @Override
            public void onConnectionFinished(HSHttpConnection hsHttpConnection) {
                listener.onDataBack(hsHttpConnection.getBodyJSON());
                HSLog.i(TAG, category + " " + "hsHttpConnection.getBodyJSON() = " + hsHttpConnection.getBodyJSON());
            }

            @Override
            public void onConnectionFailed(HSHttpConnection hsHttpConnection, HSError hsError) {
                HSLog.i(TAG, category + " " + "hsError.getCode() = " + hsError.getCode() + " hsError.getMessage() = " + hsError.getMessage());
                listener.onDataBack(null);
            }
        });
        HSLog.i(TAG, category + " start request ");
        connection.startAsync();
    }

    private String initJson() {
        baiduNewsRequest.setTimestamp(System.currentTimeMillis());
        baiduNewsRequest.setToken(TOKEN);
        baiduNewsRequest.setAppsid(APPSID);

        BaiduNewsRequest.DataBean data = baiduNewsRequest.getData();
        if (data == null) {
            data = new BaiduNewsRequest.DataBean();
            baiduNewsRequest.setData(data);
        }

        BaiduNewsRequest.DataBean.DeviceBean device = data.getDevice();
        if (device == null) {
            device = new BaiduNewsRequest.DataBean.DeviceBean();
            device.setDeviceType(1);
            device.setOsType(1);
            device.setOsVersion(android.os.Build.VERSION.RELEASE);
            device.setVendor(android.os.Build.MANUFACTURER);
            device.setModel(android.os.Build.MODEL);
            data.setDevice(device);
        }

        BaiduNewsRequest.DataBean.DeviceBean.ScreenSizeBean screenSize = device.getScreenSize();
        if (screenSize == null) {
            screenSize = new BaiduNewsRequest.DataBean.DeviceBean.ScreenSizeBean();
            screenSize.setHeight(HSApplication.getContext().getResources().getDisplayMetrics().heightPixels);
            screenSize.setWidth(HSApplication.getContext().getResources().getDisplayMetrics().widthPixels);
            device.setScreenSize(screenSize);
        }

        BaiduNewsRequest.DataBean.DeviceBean.UdidBean udid = device.getUdid();
        if (udid == null) {
            udid = new BaiduNewsRequest.DataBean.DeviceBean.UdidBean();
            String imei = getImei();
            if (ANDROID != null) {
                udid.setAndroidId(ANDROID);
            }
            if (imei != null) {
                udid.setImei(imei);
                udid.setImeiMd5(MD5Utils.md5Hex(imei));

            }
            device.setUdid(udid);
        }

        BaiduNewsRequest.DataBean.NetworkBean network = data.getNetwork();
        if (network == null) {
            network = new BaiduNewsRequest.DataBean.NetworkBean();
        }

        int networkType = NetworkUtils.getNetworkType(HSApplication.getContext());
        int connectionType;
        switch (networkType) {
            case NetworkUtils.NETWORK_2G:
                connectionType = 2;
                break;
            case NetworkUtils.NETWORK_3G:
                connectionType = 3;
                break;
            case NetworkUtils.NETWORK_4G:
                connectionType = 4;
                break;
            case NetworkUtils.NETWORK_WIFI:
                connectionType = 100;
                break;
            default:
                connectionType = 1;
        }
        network.setConnectionType(connectionType);
        network.setOperatorType(NetworkUtils.getSimOperator(HSApplication.getContext()));
        network.setIpv4(NetworkUtils.getIPAddress(true));
        data.setNetwork(network);

        BaiduNewsRequest.DataBean.ContentParamsBean contentParams = data.getContentParams();
        if (contentParams == null) {
            contentParams = new BaiduNewsRequest.DataBean.ContentParamsBean();
        }
        contentParams.setPageIndex(pageIndex);
        contentParams.setContentType(0);
        contentParams.setPageSize(13);

        if (TextUtils.equals(category, CATEGORY_NEWS_HOT) || TextUtils.equals(category, CATEGORY_ALL) || TextUtils.equals(category, CATEGORY_NEWS_LOCAL)) {
            contentParams.setListScene(Integer.parseInt(category));
        } else {
            List<Integer> catIds = new ArrayList<>();
            catIds.add(Integer.parseInt(category));
            contentParams.setCatIds(catIds);
        }

        data.setContentParams(contentParams);

        StringBuilder stringBuilder = new StringBuilder();
        String singatureString = MD5Utils.md5Hex(stringBuilder.append(baiduNewsRequest.getTimestamp())
                .append(baiduNewsRequest.getToken()).append(new Gson().toJson(data)).toString());
        baiduNewsRequest.setSignature(singatureString);
        HSLog.d(TAG, "json : " + new Gson().toJson(baiduNewsRequest));
        return new Gson().toJson(baiduNewsRequest);
    }

    @SuppressLint("HardwareIds")
    private String getImei() {
        Context context = HSApplication.getContext();

        String imei = null;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            imei = ((TelephonyManager) context.getSystemService(TELEPHONY_SERVICE)).getDeviceId();
        }
        if (imei == null || imei.length() < 1) {
            imei = ANDROID;
        }
        return imei;
    }

    public String getBaiduCategoryParam(int position) {
        switch (position) {
            case 0:
                return CATEGORY_ALL;
            case 1:
                return CATEGORY_NEWS_HOT;
            case 2:
                return CATEGORY_NEWS_LOCAL;
            case 3:
                return CATEGORY_NEWS_ENTERTAINMENT;
            case 4:
                return CATEGORY_NEWS_TECH;
            case 5:
                return CATEGORY_NEWS_CAR;
            case 6:
                return CATEGORY_NEWS_FINANCE;
            case 7:
                return CATEGORY_NEWS_MILITARY;
            case 8:
                return CATEGORY_NEWS_SPORTS;
            case 9:
                return CATEGORY_NEWS_HEALTH;
            case 10:
                return CATEGORY_NEWS_HOUSE;
            default:
                break;
        }

        if (BuildConfig.DEBUG) {
            throw new RuntimeException("参数错误 position = " + position);
        }

        return CATEGORY_ALL;
    }
}
