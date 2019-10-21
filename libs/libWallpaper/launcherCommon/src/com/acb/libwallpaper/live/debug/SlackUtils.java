package com.acb.libwallpaper.live.debug;

import android.os.Handler;
import android.os.Looper;

import com.ihs.commons.connection.HSHttpConnection;
import com.ihs.commons.connection.HSHttpConnection.OnConnectionFinishedListener;
import com.ihs.commons.connection.httplib.HttpRequest.Method;
import com.ihs.commons.utils.HSError;
import com.ihs.commons.utils.HSLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SlackUtils {

    private static final String TAG = "SlackUtils";

    private static final String URL =
            "https://hooks.slack.com/services/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";

    static void sendLeak(String text) {
        send(text, ReportType.LEAK);
    }

    static void sendBlock(String text) {
        send(text, ReportType.BLOCK);
    }

    private static void send(String text, ReportType type) {
        JSONObject payload = null;
        try {
            payload = new JSONObject();
            JSONArray attachments = new JSONArray();
            JSONObject attachment = new JSONObject();

            attachment.put("title", type.title());
            attachment.put("color", type.colorHex());
            attachment.put("text", text);

            attachments.put(attachment);
            payload.put("attachments", attachments);
        } catch (JSONException var5) {
            var5.printStackTrace();
        }

        sendInner(payload.toString());
    }

    private static void sendInner(String body) {
        HSHttpConnection hsHttpConnection = new HSHttpConnection(URL, Method.POST);
        hsHttpConnection.setRequestBody(body);
        hsHttpConnection.setConnectTimeout('\uea60');
        hsHttpConnection.setReadTimeout('\uea60');
        hsHttpConnection.setConnectionFinishedListener(new OnConnectionFinishedListener() {
            @Override
            public void onConnectionFinished(HSHttpConnection conn) {
                String responseString = "getResponseMessage = " + conn.getResponseMessage()
                        + ", getResponseCode = " + conn.getResponseCode()
                        + ", getBodyString = " + conn.getBodyString();
                if (conn.getResponseCode() == 200) {
                    HSLog.i(TAG, responseString);
                } else {
                    HSLog.e(TAG, responseString);
                }
            }

            @Override
            public void onConnectionFailed(HSHttpConnection conn, HSError error) {
                String responseString = "getResponseMessage = " + conn.getResponseMessage()
                        + ", getResponseCode = " + conn.getResponseCode()
                        + ", getBodyString = " + conn.getBodyString();
                HSLog.e(TAG, responseString + ", error = " + error.toString());
            }
        });
        hsHttpConnection.startAsync(new Handler(Looper.getMainLooper()));
    }

    private enum ReportType {
        LEAK("LeakCanary", "#fbed3d"),
        BLOCK("BlockCanary", "#ff0000");

        String mTitle;
        String mColorHex;

        ReportType(String title, String colorHex) {
            mTitle = title;
            mColorHex = colorHex;
        }

        private String colorHex() {
            return mColorHex;
        }

        public String title() {
            return mTitle;
        }
    }
}
