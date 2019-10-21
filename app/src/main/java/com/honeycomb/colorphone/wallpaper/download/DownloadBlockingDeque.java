package com.honeycomb.colorphone.wallpaper.download;

import android.text.TextUtils;

import com.ihs.commons.utils.HSLog;

import java.util.Iterator;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public class DownloadBlockingDeque extends LinkedBlockingDeque<Runnable> {

    private final Object mLock = new Object();
    private volatile boolean mPause;

    @Override
    public boolean offer(Runnable e) {
        removeOverLimitRunnableIfNeed(e);
        return isLifo(e) ? super.offerFirst(e) : super.offer(e);
    }

    @Override
    public boolean offer(Runnable e, long timeout, TimeUnit unit) throws InterruptedException {
        removeOverLimitRunnableIfNeed(e);
        return isLifo(e) ? super.offerFirst(e) : super.offer(e);
    }


    @Override
    public boolean add(Runnable e) {
        removeOverLimitRunnableIfNeed(e);
        return isLifo(e) ? super.offerFirst(e) : super.offer(e);
    }

    @Override
    public void put(Runnable e) throws InterruptedException {
        removeOverLimitRunnableIfNeed(e);
        if (isLifo(e)) {
            super.putLast(e);
        } else {
            super.put(e);
        }
    }

    @Override
    public Runnable poll(long timeout, TimeUnit unit) throws InterruptedException {
        synchronized (mLock) {
            while (mPause) {
                mLock.wait();
            }
        }
        return super.poll(timeout, unit);
    }

    @Override
    public Runnable take() throws InterruptedException {
        synchronized (mLock) {
            while (mPause) {
                mLock.wait();
            }
        }
        return super.take();
    }

    public void pause() {
        mPause = true;
    }

    public void resume() {
        mPause = false;
        try {
            synchronized (mLock) {
                mLock.notifyAll();
            }
        } catch (Exception e) {

        }
    }

    private boolean isLifo(Runnable r) {
        return r instanceof Downloader.DownloadRunnable && ((Downloader.DownloadRunnable) r).isLifo();
    }

    private boolean removeOverLimitRunnableIfNeed(Runnable r) {
        boolean removed = false;
        if (r instanceof Downloader.DownloadRunnable && ((Downloader.DownloadRunnable) r).isLimitedByCount()) {
            Downloader.DownloadRunnable downloadRunnable = (Downloader.DownloadRunnable) r;
            int limitedCount = downloadRunnable.getLimitedCount();
            String limitedGroupTag = downloadRunnable.getLimitedGroupTag();
            Iterator<Runnable> iterator = this.iterator();
            int currentCount = 0;
            while (iterator.hasNext()) {
                Runnable next = iterator.next();
                if (next instanceof Downloader.DownloadRunnable) {
                    Downloader.DownloadRunnable oldRunnable = (Downloader.DownloadRunnable) next;
                    if (TextUtils.equals(oldRunnable.getLimitedGroupTag(), limitedGroupTag)) {
                        if (currentCount < limitedCount) {
                            currentCount++;
                        } else {
                            iterator.remove();
                            HSLog.d("Downloader", "Remove: " + limitedGroupTag);
                            removed = true;
                        }
                    }
                }
            }
        }
        return removed;
    }
}
