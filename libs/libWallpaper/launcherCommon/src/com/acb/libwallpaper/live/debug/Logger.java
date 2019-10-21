package com.acb.libwallpaper.live.debug;

import com.ihs.commons.utils.HSLog;

/**
 * Simple wrapper over {@link HSLog#d(String, String)} that prints extra "calling from" info.
 */
public class Logger {

    public static void log(String tag, String msg) {
        StackTraceElement stackTraceElement = new Throwable().getStackTrace()[2];
        HSLog.d(tag, "[Calling from " + stackTraceElement.getFileName() + ":" + stackTraceElement.getLineNumber() + "] " + msg);
    }
}
