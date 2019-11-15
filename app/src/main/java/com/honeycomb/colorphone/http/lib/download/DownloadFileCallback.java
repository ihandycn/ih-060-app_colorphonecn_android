package com.honeycomb.colorphone.http.lib.download;

import org.jetbrains.annotations.Nullable;

import java.io.File;

import okhttp3.ResponseBody;

public interface DownloadFileCallback {

    void onSuccess(File file);

    @Nullable File convert(ResponseBody responseBody);

    void onDownload(long length, long current, boolean isDone);

    void onFailure();
}
