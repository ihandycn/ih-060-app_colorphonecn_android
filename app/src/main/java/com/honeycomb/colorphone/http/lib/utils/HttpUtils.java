package com.honeycomb.colorphone.http.lib.utils;

import java.io.File;
import java.nio.charset.Charset;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class HttpUtils {

    public static final Charset UTF8 = Charset.forName("UTF-8");

    public static boolean isFileValid(File file) {
        return file.exists() && file.length() > 0;
    }

    public static RequestBody getRequestBodyFromJson(String jsonStr) {
        return RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonStr);
    }
}
