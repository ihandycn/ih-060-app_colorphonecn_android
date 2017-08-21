package com.honeycomb.colorphone.download;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.ColorPhoneApplication;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.view.ProgressView;
import com.honeycomb.colorphone.view.TypefacedTextView;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.model.FileDownloadStatus;

public class DownloadViewHolder implements DownloadHolder {
    private static final boolean DEBUG_PROGRESS = BuildConfig.DEBUG & true;

    /**
     * Progress display
     */
    protected ProgressView taskProgressBar;
    protected TypefacedTextView taskProgressTxt;
    protected LottieAnimationView taskSuccessAnim;
    protected LottieAnimationView taskStartAnim;
    /**
     * Control progress, start or pause download task.
     */
    protected View taskActionBtn;

    private DownloadHolder mProxy;

    private boolean canPaused;
    private boolean canStart;
    private int id;
    private long mDelayTime = 600;
    private boolean enablePause = false;


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
                    startDownload();
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


    public void setProxyHolder(DownloadHolder downloadHolder) {
        mProxy = downloadHolder;
    }

    public void startDownload() {
        final TasksManagerModel model = TasksManager.getImpl().getById(id);

        if (model == null) {
            if (BuildConfig.DEBUG) {
                throw new IllegalStateException("start download but get null taskModel");
            }
            return;
        }
        boolean needPrologue = taskActionBtn.getVisibility() == View.VISIBLE;
        if (needPrologue) {
            if (taskStartAnim != null) {
                taskStartAnim.setVisibility(View.VISIBLE);
                final View v = (View) taskProgressBar;
                v.setVisibility(View.INVISIBLE);

                taskStartAnim.addAnimatorListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        taskStartAnim.setVisibility(View.GONE);
                        taskProgressBar.setProgress(3);
                        v.setVisibility(View.VISIBLE);
                        doDownload(model);
                    }
                });
                taskStartAnim.playAnimation();
            } else {
                // animation handle by task progress bar.
                taskProgressBar.onDownloadStart();
                taskActionBtn.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doDownload(model);
                    }
                }, mDelayTime);
            }
        } else {
            doDownload(model);
        }
        ColorPhoneApplication.getConfigLog().getEvent().onThemeDownloadStart(model.getName().toLowerCase());

    }

    public void doDownload(TasksManagerModel model) {
        doDownload(model, mProxy != null ? mProxy : this);
    }

    public static void doDownload(TasksManagerModel model, Object tag) {
        if (model != null) {
            FileDownloadListener listener;
            listener = FileDownloadMultiListener.getDefault();

            final BaseDownloadTask task = FileDownloader.getImpl().create(model.getUrl())
                    .setPath(model.getPath())
                    .setCallbackProgressTimes(100)
                    .setListener(listener);
            TasksManager.getImpl().addTaskForViewHolder(task);

            if (tag != null) {
                task.setTag(tag);
            }

            task.start();

        } else {
            throw new IllegalStateException("Has no pending task to download!");
        }
    }

    public void updateDownloaded(boolean progressFlag) {
        taskProgressBar.setProgress(100);
        taskProgressTxt.setVisibility(View.INVISIBLE);
        if (progressFlag) {
            taskSuccessAnim.setVisibility(View.VISIBLE);
            taskSuccessAnim.playAnimation();
        }
        if (DEBUG_PROGRESS) {
            HSLog.d("sundxing", getId() + " download success!");
        }

    }

    public void updateNotDownloaded(final int status, final long sofar, final long total) {
        if (sofar > 0 && total > 0) {
            updateProgressView(sofar, total);
        } else {
            taskProgressBar.reset();
            taskProgressTxt.setVisibility(View.INVISIBLE);
        }
        if (status == FileDownloadStatus.error) {
            Toast.makeText(HSApplication.getContext(), R.string.network_err, Toast.LENGTH_SHORT).show();
        }

        canPaused = false;
        canStart = true;

        if (DEBUG_PROGRESS) {
            HSLog.d("sundxing", getId() + " download stopped, status = " + status);
        }
    }

    public void updateDownloading(final int status, final long sofar, final long total) {
        if (sofar > 0 && total > 0) {
            final int percent = updateProgressView(sofar, total);
            if (DEBUG_PROGRESS) {
                HSLog.d("sundxing", getId() + " download process, percent = " + percent + "%");
            }
        }

        canPaused = true;
        canStart = false;
    }

    private int updateProgressView(long sofar, float total) {
        final int percent = (int) (100 * sofar / total);
        taskProgressBar.setProgress(percent);
        taskProgressTxt.setVisibility(View.VISIBLE);
        taskProgressTxt.setText(percent + "%");
        mDelayTime = 0;
        return percent;
    }

    public boolean canPaused() {
        return canPaused && enablePause;
    }

    public boolean canStartDownload() {
        return canStart;
    }

    public void setStartAnim(LottieAnimationView startAnim) {
        taskStartAnim = startAnim;
    }
}