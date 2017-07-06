package com.honeycomb.colorphone.download;

import android.view.View;

import com.airbnb.lottie.LottieAnimationView;
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.view.ProgressView;
import com.honeycomb.colorphone.view.TypefacedTextView;
import com.ihs.commons.utils.HSLog;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;

public class DownloadViewHolder implements DownloadHolder {
    private static final boolean DEBUG_PROGRESS = BuildConfig.DEBUG & true;
    private FileDownloadListener taskDownloadListener;

    /**
     * Progress display
     */
    protected ProgressView taskProgressBar;
    protected TypefacedTextView taskProgressTxt;
    protected LottieAnimationView taskSuccessAnim;
    /**
     * Control progress, start or pause download task.
     */
    protected View taskActionBtn;

    private DownloadHolder mProxy;

    private boolean canPaused;
    private boolean canStart;
    private int id;
    private long mDelayTime = 800;


    public DownloadViewHolder(View taskActionBtn, ProgressView progressView, TypefacedTextView progressTxt, LottieAnimationView successAnim) {
        this.taskProgressBar = progressView;
        this.taskProgressTxt = progressTxt;
        this.taskSuccessAnim = successAnim;
        this.taskActionBtn = taskActionBtn;
        this.taskActionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canPaused()) {
                    // to pause
                    FileDownloader.getImpl().pause(id);
                } else if (canStartDownload()) {
                    startDownloadDelay(mDelayTime);
                    mDelayTime = 0;
                }
            }
        });
    }

    public void bindTaskId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setFileDownloadListener(FileDownloadListener listener) {
        taskDownloadListener = listener;
    }

    public void setProxyHolder(DownloadHolder downloadHolder) {
        mProxy = downloadHolder;
    }

    public void startDownloadDelay(long delayTime) {
        final TasksManagerModel model = TasksManager.getImpl().getById(id);

        if (model == null) {
            if (BuildConfig.DEBUG) {
                throw new IllegalStateException("start download but get null taskModel");
            }
            return;
        }
        final BaseDownloadTask task = TasksManager.getImpl().getTask(model.getId());
        if (delayTime == 0) {
            doDownload(model);
        } else {
            taskProgressBar.onDownloadStart();
            taskActionBtn.postDelayed(new Runnable() {
                @Override
                public void run() {
                    doDownload(model);
                }
            }, delayTime);
        }
    }

    private void doDownload(TasksManagerModel model) {
        if (model != null) {
            FileDownloadListener listener;
            if (taskDownloadListener == null) {
                listener = FileDownloadMultiListener.getDefault();
            } else {
                listener = taskDownloadListener;
            }

            final BaseDownloadTask task = FileDownloader.getImpl().create(model.getUrl())
                    .setPath(model.getPath())
                    .setCallbackProgressTimes(100)
                    .setListener(listener);

            TasksManager.getImpl().addTaskForViewHolder(task);

            task.setTag(mProxy != null ? mProxy : this);

            task.start();
        } else {
            throw new IllegalStateException("Has no pending task to download!");
        }
    }

    public void updateDownloaded(boolean progressFlag) {
        taskProgressBar.setProgress(100);
        taskProgressTxt.setVisibility(View.INVISIBLE);
        taskSuccessAnim.setVisibility(View.VISIBLE);
        taskSuccessAnim.playAnimation();
        if (DEBUG_PROGRESS) {
            HSLog.d("sundxing", getId() + " download success!");
        }

    }

    public void updateNotDownloaded(final int status, final long sofar, final long total) {
        if (sofar > 0 && total > 0) {
            final int percent = (int) (100 * sofar / (float) total);
            taskProgressBar.setProgress(percent);
            taskProgressTxt.setText(percent + "%");
        } else {
            taskProgressBar.reset();
            taskProgressTxt.setVisibility(View.INVISIBLE);
        }

        canPaused = false;
        canStart = true;

        if (DEBUG_PROGRESS) {
            HSLog.d("sundxing", getId() + " download stopped, status = " + status);
        }
    }

    public void updateDownloading(final int status, final long sofar, final long total) {
        final int percent = (int) (100 * sofar / (float) total);
        taskProgressBar.setProgress(percent);
        if (percent > 0) {
            taskProgressTxt.setVisibility(View.VISIBLE);
            taskProgressTxt.setText(percent + "%");
        }

        canPaused = true;
        canStart = false;

        if (DEBUG_PROGRESS) {
            HSLog.d("sundxing", getId() + " download process, percent = " + percent + "%");
        }
    }

    public boolean canPaused() {
        return canPaused;
    }

    public boolean canStartDownload() {
        return canStart;
    }

}