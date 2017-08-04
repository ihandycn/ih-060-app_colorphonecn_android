package com.honeycomb.colorphone.util;

import android.content.Intent;

import com.honeycomb.colorphone.PermanentService;
import com.ihs.app.framework.HSApplication;

/**
 * 进程保活，主要保活方式有：
 *
 * <p>##### Force Stop 后失效：
 * 1. Service
 * Api 5 及以上, 死后自动重启
 * 2. Foreground Service
 * Api 25(7.1) 以下,自动 Foreground，可以极大提升进程优先级，
 * Api 25(7.1),会弹出一个 常驻 Notification：
 * 如果 App 本身有常驻 Notification，可以将 id 传入，并使用 Foreground 方式；
 * 否则 Permanent 不会使用 Foreground 方式；
 * 3. Static broadcast
 * All Api, 利用静态广播保活
 * </p>
 * <p>
 * ##### Force Stop 后依然能保活：
 * 4. nativeProcess
 * Api 21(5.0) 以下，由 Native 进程保活
 * 5. account sync
 * Api 5 及以上, 利用 Account 保活
 * </p>
 */
public class HSPermanentUtils {
    public static final boolean proxyGuardByAccountSync = false, proxyGuardByNativeProcess = false;
    public static String proxyUninstallFeedbackUrl;

    /**
     * startKeepAlive()
     * 任何进程都可随意调用，但必须确保在需要保活的进程上调用过
     *
     * @param guardByAccountSync
     *         是否利用 Account 进行保活
     * @param guardByNativeProcess
     *         是否利用 Native Process 进行保活,Api 21(5.0) 以下才有效
     * @param uninstallFeedbackUrl
     *         Nullable , 为 Null 时，卸载后什么都不做
     *         不为 Null 时，卸载后自动打开 uninstallFeedbackUrl 对应的网页， Api 21(5.0) 以下才有效
     * @param listener
     *         {@link PermanentServiceListener}
     */
    public static void startKeepAlive(boolean guardByAccountSync, boolean guardByNativeProcess, String uninstallFeedbackUrl,
            PermanentServiceListener listener) {

        Intent serviceIntent = new Intent(HSApplication.getContext(), PermanentService.class);
        serviceIntent.setAction(PermanentService.ACTION_REFRESH_NOTIFICATION);
        HSApplication.getContext().startService(serviceIntent);
    }


    /**
     * 任何进程随意调用，但必须确保在调用进程上调用过 {@link #startKeepAlive}
     * 如果被保活进程死了，调用此接口可以让保活进程重新启动
     */
    public static void keepAlive() {
        startKeepAlive(proxyGuardByAccountSync, proxyGuardByNativeProcess, proxyUninstallFeedbackUrl,
                       null);
    }

    private static class PermanentServiceListener {
    }
}
