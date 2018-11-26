package com.honeycomb.colorphone;

import android.os.Handler;
import android.os.Looper;

import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSMapUtils;
import com.superapps.util.Commons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"WeakerAccess", "unused"})
public final class CrashGuard {

    private static final String TAG = CrashGuard.class.getSimpleName();

    private static List<Crash> sIgnoredCrashes = new ArrayList<>(8);
    private static Thread.UncaughtExceptionHandler sUncaughtExceptionHandler;

    private CrashGuard() {
    }

    private static boolean sInstalled = false;

    public static synchronized void install() {
        if (sInstalled) {
            // Block canary's stacktrace recorded is useless as it stacks at enterInnerLoop(), so CrashGuard is disabled
            // when block canary is enabled.
            return;
        }
        sInstalled = true;

        updateIgnoredCrashes();

        new Handler(Looper.getMainLooper()).postAtFrontOfQueue(new Runnable() {
            @SuppressWarnings("InfiniteLoopStatement")
            @Override
            public void run() {
                while (true) {
                    try {
                        // Inner loop
                        Looper.loop();
                    } catch (Throwable e) {
                        if (checkIgnorable(e)) {
                            HSLog.w(TAG, "Main thread crash caught and ignored: " + e);
                            e.printStackTrace();
                        } else {
                            if (Commons.isBinderSizeError(e)) {
                                e.printStackTrace();
                            } else if (isOOM(e)) {
                                recordProcessLifespan();
                            }

                            List<StackTraceElement> oTraces = new ArrayList<>(Arrays.asList(e.getStackTrace()));

                            Iterator<StackTraceElement> iterator = oTraces.iterator();
                            while(iterator.hasNext()) {
                                StackTraceElement element = iterator.next();
                                if (element.getClassName().contains(CrashGuard.class.getName())){
                                    iterator.remove();
                                    HSLog.d(TAG, "Remove guard stacktrace :" + element.toString());
                                }
                            }
                            e.setStackTrace(oTraces.toArray(new StackTraceElement[0]));
                            throw e;
                        }
                    }
                }
            }
        });

        sUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                if (checkIgnorable(e)) {
                    HSLog.w(TAG, "Non-main thread crash caught and ignored: " + e);
                    e.printStackTrace();
                } else {
                    sUncaughtExceptionHandler.uncaughtException(t, e);
                }
                if (isOOM(e)) {
                    recordProcessLifespan();
                }
            }
        });
    }

    private static void recordProcessLifespan() {
//        long currentRealTime = SystemClock.elapsedRealtime();
//        long mainProcessLifespan = (currentRealTime - HSApplication.getCurrentLaunchInfo().launchId) / 1000 / 60;
    }


    private static boolean isOOM(Throwable e) {
        return e instanceof OutOfMemoryError;
    }

    @SuppressWarnings("unchecked")
    public static synchronized void updateIgnoredCrashes() {
        List<Map<String, ?>> configs = (List<Map<String, ?>>) HSConfig.getList("Application", "IgnoredCrashes");
        if (configs == null) {
            return;
        }
        List<Crash> ignoredCrashes = sIgnoredCrashes;
        ignoredCrashes.clear();
        for (int i = 0; i < configs.size(); i++) {
            Object configObj = configs.get(i);
            Map<String, ?> config;
            try {
                config = (Map<String, ?>) configObj;
            } catch (ClassCastException e) {
                continue;
            }
            Crash ignoredCrash = Crash.parseConfig(config);
            if (ignoredCrash != null) {
                ignoredCrashes.add(ignoredCrash);
            }
        }
    }

    static boolean checkIgnorable(Throwable t) {
        String message = t.getMessage();
        if (message == null) {
            return false;
        }
        for (Crash ignoredCrash : sIgnoredCrashes) {
            if (ignoredCrash.matches(t)) {
                return true;
            }
        }
        return false;
    }

    private static class Crash {
        String exceptionType;
        String messagePattern;
        StackTrace stackTrace;

        static Crash parseConfig(Map<String, ?> config) {
            if (config == null) {
                return null;
            }
            Crash crash = new Crash();
            crash.exceptionType = HSMapUtils.optString(config, null, "Type");
            String messagePattern = HSMapUtils.optString(config, null, "MessagePattern");
            if (messagePattern != null) {
                crash.messagePattern = messagePattern;
            }
            Object stackTraceConfig = config.get("StackTrace");
            Map<String, ?> stackTraceConfigMap = null;
            try {
                //noinspection unchecked
                stackTraceConfigMap = (Map<String, ?>) stackTraceConfig;
            } catch (ClassCastException ignored) {
            }
            crash.stackTrace = StackTrace.parseConfig(stackTraceConfigMap);
            if (crash.exceptionType == null && crash.messagePattern == null && crash.stackTrace == null) {
                // We do not allow a wildcard that matches all crashes
                return null;
            }
            return crash;
        }

        @SuppressWarnings("RedundantIfStatement")
        boolean matches(Throwable t) {
            if (t == null) {
                return false;
            }
            if (exceptionType != null && !exceptionType.equals(t.getClass().getSimpleName())) {
                return false;
            }
            if (messagePattern != null && (t.getMessage() == null || !(t.getMessage().contains(messagePattern)))) {
                return false;
            }
            StackTraceElement[] stackTraceElements = t.getStackTrace();
            if (stackTrace != null && (stackTraceElements == null || !stackTrace.matches(stackTraceElements))) {
                return false;
            }
            return true;
        }
    }

    private static class StackTrace {
        // Currently we match only one element in the stack, TODO: match multiple elements if necessary
        int elementIndex;
        String methodName;
        String fileName;
        String className;
        int lineNumber;

        static StackTrace parseConfig(Map<String, ?> config) {
            if (config == null) {
                return null;
            }
            StackTrace stackTrace = new StackTrace();
            stackTrace.elementIndex = HSMapUtils.optInteger(config, Integer.MAX_VALUE, "ElementIndex");
            stackTrace.methodName = HSMapUtils.optString(config, null, "MethodName");
            stackTrace.fileName = HSMapUtils.optString(config, null, "FileName");
            stackTrace.className = HSMapUtils.optString(config, null, "ClassName");
            stackTrace.lineNumber = HSMapUtils.optInteger(config, -1, "LineNumber");
            return stackTrace;
        }

        @SuppressWarnings("RedundantIfStatement")
        boolean matches(StackTraceElement[] stackTraceElements) {
            if (stackTraceElements == null || stackTraceElements.length <= elementIndex) {
                return false;
            }
            StackTraceElement element = stackTraceElements[elementIndex];
            if (methodName != null && !methodName.equals(element.getMethodName())) {
                return false;
            }
            if (fileName != null && !fileName.equals(element.getFileName())) {
                return false;
            }
            if (className != null && !className.equals(element.getClassName())) {
                return false;
            }
            if (lineNumber != -1 && lineNumber == element.getLineNumber()) {
                return false;
            }
            return true;
        }
    }
}
