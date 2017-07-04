package com.honeycomb.colorphone.download;

public interface DownloadStateListener {

    void updateDownloaded(boolean progressFlag);

    void updateNotDownloaded(final int status, final long sofar, final long total);

    void updateDownloading(final int status, final long sofar, final long total);
}