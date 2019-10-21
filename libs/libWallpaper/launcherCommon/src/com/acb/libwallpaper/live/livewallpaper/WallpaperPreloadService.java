package com.acb.libwallpaper.live.livewallpaper;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;


/**
 * Created by sundxing on 2018/6/1.
 */

public class WallpaperPreloadService extends Service {

    public static void prepareLiveWallpaper(Context context) {
        try {
            context.startService(new Intent(context, WallpaperPreloadService.class));
        } catch (IllegalStateException e) {
            //CrashlyticsCore.getInstance().logException(new IllegalStateException("WallpaperPreloadService"));
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LiveWallpaperManager.getInstance().prepareWallpaper();
        return super.onStartCommand(intent, flags, startId);
    }
}
