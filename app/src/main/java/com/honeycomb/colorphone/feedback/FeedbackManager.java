package com.honeycomb.colorphone.feedback;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

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

/**
 * Created by songliu on 3/11/16.
 */
public class FeedbackManager {

    private static final String PREF_KEY_FEEDBACK_CONTENT = "feedback_content";

    private static volatile HSServerAPIConnection sConnection = null;

    static void sendFeedback(Map<String, String> content) {
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
            JSONObject feedback = new JSONObject();
            for (Map.Entry<String, String> entry : content.entrySet()) {
                feedback.put(entry.getKey(), entry.getValue());
            }
            json.put("feedback", feedback);
            Preferences.getDefault().putString(PREF_KEY_FEEDBACK_CONTENT, json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendFeedbackToServerIfNeeded();
    }

    public static void sendFeedbackToServerIfNeeded() {
        if (sConnection != null) {
            return;
        }
        Preferences prefs = Preferences.getDefault();
        String feedback = prefs.getString(PREF_KEY_FEEDBACK_CONTENT, "");
        if (TextUtils.isEmpty(feedback)) {
            HSLog.d("feedback", "no need to send");
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
                    HSLog.d("feedback", "feedback sent");
                }
                FeedbackManager.sConnection = null;
            }

            @Override
            public void onConnectionFailed(HSHttpConnection hsHttpConnection, HSError hsError) {
                HSLog.w("feedback", "feedback failed with error: " + hsError);
                FeedbackManager.sConnection = null;
            }
        });
        sConnection.startAsync(new Handler(Looper.getMainLooper()));
    }
}
