package com.colorphone.smartlocker.h5;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;

import com.colorphone.smartlocker.utils.MD5Utils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;

import net.appcloudbox.AcbAds;

import static android.content.Context.TELEPHONY_SERVICE;

public class BaiduH5Config {
    private static final String TAG = "BaiduH5Config";

    @SuppressLint("HardwareIds")
    private static String ANDROID = Settings.Secure.getString(HSApplication.getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    private static final String RSA_TRANSFORMATION = "RSA/None/PKCS1Padding";

    /**
     * form
     * 1. IMEI: https://cpu.baidu.com/1001/d77e414?im=${imei}&imMd5=${imeiMD5}&aid=${Android}
     * 2. OAID: https://cpu.baidu.com/1001/d77e414?oaid=${OAID}&oaidMd5=${oaidMd5}
     */
    public static String getH5UrlConfig() {
        int keySize = HSConfig.optInteger(2048, "Application", "NewsH5Locker", "CableH5RSAKeySize");
        String publicKey = HSConfig.optString("", "Application", "NewsH5Locker", "CableH5RSAKey");
        String url = HSConfig.optString("https://cpu.baidu.com/1022/af04b057?scid=69014",
                "Application", "NewsH5Locker", "CableH5Url");

        if (TextUtils.isEmpty(publicKey)) {
            return url;
        }

        StringBuilder sb = new StringBuilder(url);

        String imei = getImei();
        if (imei != null && !TextUtils.isEmpty(imei)) {
            sb.append(url.contains("?scid") ? "&im=" : "?im=").append(encrypt(imei, publicKey, keySize));
            sb.append("&imMd5=").append(encrypt(MD5Utils.md5Hex(imei), publicKey, keySize));
            if (ANDROID != null && !TextUtils.isEmpty(ANDROID)) {
                sb.append("&aid=").append(encrypt(ANDROID, publicKey, keySize));
            }
            return sb.toString();
        }

        String oaid = AcbAds.getOaid();
        if (!TextUtils.isEmpty(oaid)) {
            sb.append(url.contains("?scid") ? "&oaid=" : "?oaid=").append(encrypt(oaid, publicKey, keySize));
            sb.append("&oaidMd5=").append(encrypt(MD5Utils.md5Hex(oaid), publicKey, keySize));
            return sb.toString();
        }

        return url;
    }

    private static String encrypt(@NonNull String content, String publicKey, int keySize) {
        HSLog.d(TAG, "encrypt content=" + content);

        byte[] encryptByte = CryptoUtils.encryptRSA(content.getBytes(),
                Base64.decode(publicKey, Base64.DEFAULT), keySize, RSA_TRANSFORMATION);
        if (encryptByte == null) {
            return "";
        }

        String encryptResult = Base64.encodeToString(encryptByte, Base64.NO_WRAP);
        if (encryptResult == null) {
            return "";
        }
        HSLog.d(TAG, "encrypt encryptResult=" + encryptResult);
        return encryptResult;
    }

    @SuppressLint("HardwareIds")
    private static String getImei() {
        Context context = HSApplication.getContext();

        String imei = null;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            imei = ((TelephonyManager) context.getSystemService(TELEPHONY_SERVICE)).getDeviceId();
        }

        return imei;
    }
}
