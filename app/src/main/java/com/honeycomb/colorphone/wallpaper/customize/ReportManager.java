package com.honeycomb.colorphone.wallpaper.customize;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.honeycomb.colorphone.wallpaper.model.LauncherFiles;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.utils.HSVersionControlUtils;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.connection.HSHttpConnection;
import com.ihs.commons.connection.HSServerAPIConnection;
import com.ihs.commons.connection.httplib.HttpRequest;
import com.ihs.commons.utils.HSError;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Preferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Map;

public class ReportManager {

    public static final String PREF_KEY_FEEDBACK_CONTENT = "feedback_content";

    private static volatile HSServerAPIConnection sConnection = null;

    public static void report(Map<String, String> content) {
        if (content == null || content.size() == 0)
            return;
        JSONObject json = new JSONObject();
        Context context = HSApplication.getContext();
        String appName = context.getApplicationInfo().loadLabel(context.getPackageManager()).toString();
        try {
            JSONObject appData = new JSONObject();
            appData.put("version_name", HSVersionControlUtils.getAppVersionName()
                    + "(" + HSVersionControlUtils.getAppVersionCode() + ")");
            appData.put("os_version", Build.VERSION.RELEASE);
            appData.put("device_model", Build.MODEL == null ? "" : Build.MODEL);
            appData.put("country", Locale.getDefault().getCountry());
            appData.put("language", Locale.getDefault().getLanguage());
            json.put("appdata", appData);
            json.put("app", appName);
            json.put("platform", "android");
            json.put("bundle_id", HSApplication.getContext().getPackageName());
            json.put("timestamp", System.currentTimeMillis());
            JSONObject report = new JSONObject();
            for (Map.Entry<String, String> entry : content.entrySet()) {
                report.put(entry.getKey(), entry.getValue());
            }
            json.put("feedback", report);
            Preferences.get(LauncherFiles.CUSTOMIZE_PREFS).putString(PREF_KEY_FEEDBACK_CONTENT, json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendReportToServerIfNeeded();
    }

    public static void sendReportToServerIfNeeded() {
        if (sConnection != null) {
            return;
        }
        Preferences prefs = Preferences.get(LauncherFiles.CUSTOMIZE_PREFS);
        String feedback = prefs.getString(PREF_KEY_FEEDBACK_CONTENT, "");
        if (TextUtils.isEmpty(feedback)) {
            HSLog.d("report", "no need to send");
            return;
        }
        JSONObject json = null;
        try {
            json = new JSONObject(feedback);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (json == null) {
            return;
        }
        String url = HSConfig.optString("", "Application", "FeedbackURL");
        if (TextUtils.isEmpty(url)) {
            return;
        }
        sConnection = new HSServerAPIConnection(url, HttpRequest.Method.POST, json);
        sConnection.setConnectionFinishedListener(new HSHttpConnection.OnConnectionFinishedListener() {
            @Override
            public void onConnectionFinished(HSHttpConnection hsHttpConnection) {
                if (hsHttpConnection.isSucceeded()) {
                    prefs.remove(PREF_KEY_FEEDBACK_CONTENT);
                    HSLog.d("report", "report sent");
                }
                sConnection = null;
            }

            @Override
            public void onConnectionFailed(HSHttpConnection hsHttpConnection, HSError hsError) {
                HSLog.w("report", "report failed with error: " + hsError);
                sConnection = null;
            }
        });
        sConnection.startAsync(new Handler(Looper.getMainLooper()));
    }
}

