package com.honeycomb.colorphone.debug;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.acb.call.utils.FileUtils;
import com.honeycomb.colorphone.http.HttpManager;
import com.honeycomb.colorphone.util.EventUtils;
import com.honeycomb.colorphone.util.Utils;
import com.liulishuo.filedownloader.util.FileDownloadUtils;

import java.io.File;

public class DebugActions {

    private static final String TAG = "DebugActions";

    public static void onVolumeDown(Activity activity) {
        File file = FileUtils.getMediaDirectory();
        String path = FileDownloadUtils.generateFilePath(file.getAbsolutePath(), "dddd");
        Log.e(TAG, "path = " + path);
    }

    public static void onVolumeUp(Activity activity) {
        File ringtoneFile = Utils.getRingtoneFile();
        String voiceFilePath = FileDownloadUtils.generateFilePath(ringtoneFile.getAbsolutePath(), "ddddddd");
        Log.e(TAG, "voiceFilePath = " + voiceFilePath);
    }
}
