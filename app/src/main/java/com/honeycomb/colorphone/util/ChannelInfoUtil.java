package com.honeycomb.colorphone.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.Key;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.SecretKeySpec;

public class ChannelInfoUtil {

    private static final String KEY_STORE = "Store";
    private static final String KEY_MEDIA = "Media";
    private static final String KEY_CHANNEL = "Channel";
    private static final String KEY_AGENCY = "Agency";
    private static final String KEY_CUSTOM = "Custom";

    private static final String DEFAULT_CUSTOM_VALUE = "0";
    private static final String DEFAULT_VALUE = "None";

    private static final String FILE_CHANNEL_INFO = "cidata";

    private static Map<String, String> channelMessageCache;

    public static String getChannelInfo(Context context) {
        return getStore(context) + "_" + getMedia(context) + "_" + getChannel(context) + "_" + getAgency(context) + "_" + getCustom(context);
    }

    public static String getStore(Context context) {
        return getValue(context, KEY_STORE);
    }

    public static String getMedia(Context context) {
        return getValue(context, KEY_MEDIA);
    }

    public static String getChannel(Context context) {
        return getValue(context, KEY_CHANNEL);
    }

    public static String getAgency(Context context) {
        return getValue(context, KEY_AGENCY);
    }

    public static String getCustom(Context context) {
        return getValue(context, KEY_CUSTOM);
    }

    private static String getValue(Context context, String key) {

        if (TextUtils.isEmpty(key)) {
            return null;
        }

        if (channelMessageCache != null && channelMessageCache.containsKey(key)) {
            return channelMessageCache.get(key);
        }

        channelMessageCache = getChannelMap(context);

        if (!channelMessageCache.containsKey(KEY_STORE)) {
            channelMessageCache.put(KEY_STORE, DEFAULT_VALUE);
        }

        if (!channelMessageCache.containsKey(KEY_MEDIA)) {
            channelMessageCache.put(KEY_MEDIA, DEFAULT_VALUE);
        }

        if (!channelMessageCache.containsKey(KEY_CHANNEL)) {
            channelMessageCache.put(KEY_CHANNEL, DEFAULT_VALUE);
        }

        if (!channelMessageCache.containsKey(KEY_AGENCY)) {
            channelMessageCache.put(KEY_AGENCY, DEFAULT_VALUE);
        }

        if (!channelMessageCache.containsKey(KEY_CUSTOM)) {
            channelMessageCache.put(KEY_CUSTOM, DEFAULT_CUSTOM_VALUE);
        }

        return channelMessageCache.get(key);
    }

    private static Map<String, String> getChannelMap(Context context) {

        File file = new File(context.getFilesDir().getAbsolutePath()
                + "/" + FILE_CHANNEL_INFO);
        if (!file.exists()) {
            copyMetaFile(context);
        }

        return getChannelByDataFile(context);
    }

    private static void copyMetaFile(Context context) {
        //从apk包中获取
        ApplicationInfo appInfo = context.getApplicationInfo();
        String sourceDir = appInfo.sourceDir;
        //默认放在meta-inf/里， 所以需要再拼接一下
        String key = "META-INF/" + FILE_CHANNEL_INFO;

        try {
            ZipFile zipfile = new ZipFile(sourceDir);
            Enumeration<?> entries = zipfile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = ((ZipEntry) entries.nextElement());
                String entryName = entry.getName();
                if (entryName.startsWith(key)) {
                    long size = entry.getSize();
                    if (size > 0) {
                        InputStream zipfileInputStream = zipfile.getInputStream(entry);
                        File file = new File(context.getFilesDir().getAbsolutePath()
                                + "/" + FILE_CHANNEL_INFO);
                        if (!file.exists()) {
                            file.createNewFile();
                        }

                        FileOutputStream outStream = new FileOutputStream(file);
                        int i;
                        while ((i = zipfileInputStream.read()) != -1) {
                            outStream.write(i);
                        }

                        zipfileInputStream.close();
                        outStream.flush();
                        outStream.close();
                    }
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static Map<String, String> getChannelByDataFile(Context context) {

        Map<String, String> channelInfo = new HashMap<>();
        BufferedReader bufferedReader = null;
        try {
            File file = new File(context.getFilesDir().getAbsolutePath()
                    + "/" + FILE_CHANNEL_INFO);
            FileInputStream inputStream = new FileInputStream(file);
            bufferedReader = new BufferedReader(new InputStreamReader(decode(inputStream)));
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();

                if (TextUtils.isEmpty(line)) {
                    continue;
                }

                String[] keyAndValue = line.split("=");
                if (keyAndValue.length < 2) {
                    continue;
                }

                channelInfo.put(keyAndValue[0], keyAndValue[1]);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return channelInfo;
    }


    private static InputStream decode(InputStream inputStream) {

        if (null == inputStream) {
            return null;

        } else {

            StringBuffer aesKeyStringBuffer = new StringBuffer();
            aesKeyStringBuffer.append("Iu[Ki}96TZp]pri/".subSequence(4, 8));
            aesKeyStringBuffer.append("Iu[Ki}96TZp]pri/".subSequence(0, 4));
            aesKeyStringBuffer.append("Iu[Ki}96TZp]pri/".subSequence(12, 16));
            aesKeyStringBuffer.append("Iu[Ki}96TZp]pri/".subSequence(8, 12));

            try {
                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
                Key key = new SecretKeySpec(aesKeyStringBuffer.toString().getBytes("UTF-8"), "AES");
                cipher.init(Cipher.DECRYPT_MODE, key);
                return new CipherInputStream(inputStream, cipher);
            } catch (Exception var5) {
                var5.printStackTrace();
                return null;
            }
        }
    }

}