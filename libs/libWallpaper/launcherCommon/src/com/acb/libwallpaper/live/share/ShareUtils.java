package com.acb.libwallpaper.live.share;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
 import com.honeycomb.colorphone.R;
import com.acb.libwallpaper.live.customize.theme.data.ShareDataProvider;
import com.acb.libwallpaper.live.util.Utils;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;
import com.superapps.util.Threads;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ShareUtils {

    private static Uri getShareImageUri(File file) {
        if (file.exists()) {
            return Uri.fromFile(file);
        }
        return null;
    }

    public static void getSharePicFile(Context context, String picUrl, LoadPicFileFromGlideListener listener) {

        Threads.postOnThreadPoolExecutor(() -> {
            File file = null;
            String picFormat = "";
            try {
                if (!TextUtils.isEmpty(picUrl)) {
                    picFormat = picUrl.substring(picUrl.length() - 4);
                    file = Glide.with(context)
                            .load(picUrl)
                            .downloadOnly(Dimensions.pxFromDp(200), Dimensions.pxFromDp(200)).get();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            final File resultFile = prepareForSharing(context, file, picFormat);
            Threads.postOnMainThread(() -> {
                if (listener != null) {
                    listener.onSuccess(resultFile);
                }
            });
        });
    }

    static File getTempShareDirectory(Context context) {
        return new File(context.getExternalFilesDir(null), "share_temp");
    }

    private static File prepareForSharing(Context context, File originalFile, String picFormat) {
        if (originalFile == null) {
            return null;
        }
        File shareDir = getTempShareDirectory(context);
        if (!shareDir.exists()) {
            shareDir.mkdirs();
        }
        String fileName = context.getResources().getString(R.string.app_name);
        File sharedFile = new File(shareDir, fileName + picFormat);
        try {
            Utils.copyFile(originalFile, sharedFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return sharedFile;
    }

    public interface LoadPicFileFromGlideListener {
        void onSuccess(File file);
    }

    public static List<ResolveInfo> getShareWays(boolean isImageShare) {
        PackageManager pm = HSApplication.getContext().getPackageManager();
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        if (isImageShare) {
            sendIntent.setType("image/*");
        } else {
            sendIntent.setType("text/plain");
        }

        List<ResolveInfo> resInfo = pm.queryIntentActivities(sendIntent, 0);

        //noinspection unchecked
        List<String> shareApps = ShareDataProvider.getApps();

        List<ResolveInfo> shareWays = new ArrayList<>();
        if (resInfo != null) {
            for (String way : shareApps) {
                for (ResolveInfo info : resInfo) {
                    if (TextUtils.equals(way, info.activityInfo.packageName)) {
                        shareWays.add(info);
                        break;
                    }
                }
            }
        }
        return shareWays;
    }

    public static void shareToFriends(String packageName, Activity context, File file, String source) {
        String parameter = "source=" + source;
        String msg = context.getResources().getString(getStrResId(source)) + ShareDataProvider.getLauncherUrl() + "&referrer=" + Uri.encode(parameter);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, msg);
        if (packageName.contains("mail") || packageName.contains("android.gm")) {
            intent.putExtra(Intent.EXTRA_SUBJECT, ShareDataProvider.getSubject());
        }
        if (couldShareImageByApp(packageName) && file != null && file.exists() && !packageName.contains("mms")) { // only share text if it is a SMS app
            intent.putExtra(Intent.EXTRA_STREAM, getShareImageUri(file));
            intent.setType("image/*");
        } else {
            intent.setType("text/plain");
        }
        intent.setPackage(packageName);
        Navigations.startActivitySafely(context, intent);
    }

    private static int getStrResId(String source) {
        int strResId = R.string.wallpaper_share_msg;
        if (TextUtils.isEmpty(source)) {
            strResId = R.string.wallpaper_share_msg;
        } else if (source.equals("3D")) {
            strResId = R.string.wallpaper_share_3d_image_msg;
        } else if (source.equals("Live")) {
            strResId = R.string.wallpaper_share_live_image_msg;
        }
        return strResId;
    }

    private static boolean couldShareImageByApp(String packageName) {
        if (packageName.equals("com.facebook.orca")
                || packageName.equals("com.facebook.katana")
                || packageName.equals("com.snapchat.android")
                || packageName.equals("com.instagram.android")
                || packageName.equals("com.tencent.mm")
                || packageName.equals("com.tencent.mobileqq")
                || packageName.equals("jp.naver.line.android")) {
            return false;
        } else {
            return true;
        }
    }
}
