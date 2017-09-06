package com.colorphone.lock.util;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.support.annotation.NonNull;

import com.ihs.commons.utils.HSLog;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An utility class for concurrent works.
 */
public class ConcurrentUtils {

    private static final String TAG = ConcurrentUtils.class.getSimpleName();

    private static final String THREAD_TAG_POOL = "launcher-pool-thread-";
    private static final String THREAD_TAG_SERIAL = "launcher-serial-thread";

    private static final int NUMBER_OF_ALIVE_CORES = Runtime.getRuntime().availableProcessors();
    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

    private static final ThreadPoolExecutor sExecutor;
    private static final ThreadFactory sDefaultThreadFactory = Executors.defaultThreadFactory();

    private static final Executor sSingleThreadExecutor;

    private static final Handler sMainHandler = new Handler(Looper.getMainLooper());

    static {
        int poolSize = Math.max(2, NUMBER_OF_ALIVE_CORES * 2 - 1);
        sExecutor = new ThreadPoolExecutor(
                poolSize, // Initial pool size
                poolSize, // Max pool size, not used as we are providing an unbounded queue to the executor
                KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT,
                new LinkedBlockingDeque<Runnable>(),
                new ThreadFactory() {
                    private AtomicInteger mThreadCount = new AtomicInteger(0);

                    @Override
                    public Thread newThread(@NonNull Runnable r) {
                        Thread thread = sDefaultThreadFactory.newThread(r);
                        thread.setName(THREAD_TAG_POOL + mThreadCount.getAndIncrement());
                        thread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
                        return thread;
                    }
                }
        );
        sSingleThreadExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(@NonNull Runnable r) {
                Thread thread = sDefaultThreadFactory.newThread(r);
                thread.setName(THREAD_TAG_SERIAL);
                thread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
                return thread;
            }
        });
    }

    public static void postOnThreadPoolExecutor(Runnable r) {
        sExecutor.execute(r);
    }

    public static void postOnSingleThreadExecutor(Runnable r) {
        sSingleThreadExecutor.execute(r);
    }

    public static void postOnMainThread(Runnable r) {
        sMainHandler.post(r);
    }

    public static void postDelayedOnMainThread(Runnable r, long delay) {
        sMainHandler.postDelayed(r, delay);
    }

    /**
     * Runs the specified runnable immediately if called from the main thread, otherwise it is
     * posted on the main thread handler.
     */
    public static void runOnMainThread(Runnable r) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            // If we are on the worker thread, post onto the main handler
            postOnMainThread(r);
        } else {
            r.run();
        }
    }

    @SuppressWarnings("unchecked")
    public static void execute(AsyncTask task) {
        task.executeOnExecutor(sExecutor, (Object[]) null);
    }

    public static ThreadPoolExecutor getPoolExecutor() {
        return sExecutor;
    }

    @SuppressWarnings("TryWithIdenticalCatches")
    public static Object callWithTimeout(Callable<Object> callable, long timeout, TimeUnit unit) throws TimeoutException {
        Future<Object> future = sExecutor.submit(callable);
        Object returnValue = null;
        try {
            returnValue = future.get(timeout, unit);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            HSLog.i(TAG, "Invocation timed out, interrupt");
            future.cancel(true);
            throw e;
        }
        return returnValue;
    }
}
