package com.honeycomb.colorphone.download;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.ColorPhoneApplication;
import com.honeycomb.colorphone.ConfigLog;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.themeselector.ThemeSelectorAdapter;
import com.honeycomb.colorphone.util.ApplyInfoAutoPilotUtils;
import com.honeycomb.colorphone.view.ProgressView;
import com.honeycomb.colorphone.view.TypefacedTextView;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.model.FileDownloadStatus;

public class DownloadViewHolder implements DownloadHolder {

    /**
     * Progress display
     */
    protected LottieAnimationView taskProgressBar;
    protected LottieAnimationView taskSuccessAnim;
    protected LottieAnimationView taskStartAnim;

    private TextView applyText;

    /**
     * Control progress, start or pause download task.
     */
    protected View taskActionBtn;

    private DownloadHolder mProxy;

    private boolean canPaused;
    private boolean canStart;
    private int id;
    private int ringtoneId;
    private long mDelayTime = 600;
    private boolean enablePause = false;
    private ThemeSelectorAdapter.ThemeCardViewHolder.DownloadedUpdateListener listener;

    public DownloadViewHolder(View taskActionBtn, LottieAnimationView progressView, LottieAnimationView successAnim) {
        this.taskProgressBar = progressView;
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
                ApplyInfoAutoPilotUtils.logApplyButtonClicked();
            }
        });
    }

    public void bindTaskId(int id) {
        this.id = id;
    }

    public void bindRingtoneTaskId(int id) {
        this.ringtoneId = id;
    }

    public int getId() {
        return id;
    }

    public void setProxyHolder(DownloadHolder downloadHolder) {
        mProxy = downloadHolder;
    }

    public void setDownloadUpdateListener(ThemeSelectorAdapter.ThemeCardViewHolder.DownloadedUpdateListener listener) {
        this.listener = listener;
    }

    public void startDownload() {
        final TasksManagerModel model = TasksManager.getImpl().getById(id);
        final TasksManagerModel ringtoneModel = TasksManager.getImpl().getById(ringtoneId);

        if (ringtoneModel != null) {
            final boolean fileReady = TasksManager.getImpl().isDownloaded(ringtoneModel);
            if (!fileReady) {
                TasksManager.doDownload(ringtoneModel, null);
            }
        }

        if (model == null) {
            if (BuildConfig.DEBUG) {
                throw new IllegalStateException("start download but get null taskModel");
            }
            return;
        }

        final boolean fileReady = model != null && TasksManager.getImpl().isDownloaded(model);
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
                        taskStartAnim.setProgress(0f);
                        taskProgressBar.setVisibility(fileReady ? View.GONE : View.VISIBLE);
                        v.setVisibility(View.VISIBLE);
                        doDownload(model);
                        if (listener != null) {
                            listener.onStartDownload();
                        }

                        taskStartAnim.removeAnimatorListener(this);
                    }
                });
                taskStartAnim.playAnimation();
                applyText.animate().alpha(0f).setDuration(100L).start();
            } else {
                // animation handle by task progress bar.
                taskProgressBar.setProgress(0f);
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
        ColorPhoneApplication.getConfigLog().getEvent().onThemeDownloadStart(model.getName().toLowerCase(), ConfigLog.FROM_LIST);

    }

    private void doDownload(TasksManagerModel model) {
        TasksManager.doDownload(model, mProxy != null ? mProxy : this);
    }

    @Override
    public void updateDownloaded(boolean progressFlag) {
        taskProgressBar.setProgress(1.0f);
        taskProgressBar.setVisibility(View.GONE);
        if (progressFlag) {
            taskSuccessAnim.setVisibility(View.VISIBLE);
            taskSuccessAnim.playAnimation();
        }
        if (TasksManager.DEBUG_PROGRESS) {
            HSLog.d("sundxing", getId() + " download success!");
        }
    }

    @Override
    public void updateNotDownloaded(final int status, final long sofar, final long total) {
        if (sofar > 0 && total > 0) {
            updateProgressView(sofar, total);
        } else {
            taskProgressBar.setProgress(0f);
        }
        taskProgressBar.setVisibility(View.GONE);
        if (status == FileDownloadStatus.error && BuildConfig.DEBUG) {
            Toast.makeText(HSApplication.getContext(), R.string.network_err, Toast.LENGTH_SHORT).show();
        }

        canPaused = false;
        canStart = true;

        if (TasksManager.DEBUG_PROGRESS) {
            HSLog.d("sundxing", getId() + " download stopped, status = " + status);
        }
    }

    @Override
    public void updateDownloading(final int status, final long sofar, final long total) {
        if (sofar >= 0 && total >= 0) {
            taskProgressBar.setVisibility(View.VISIBLE);
            final int percent = updateProgressView(sofar, total == 0 ? Long.MAX_VALUE : total);
            if (TasksManager.DEBUG_PROGRESS) {
                HSLog.d("sundxing", getId() + " download process, percent = " + percent + "%");
            }
        }

        canPaused = true;
        canStart = false;
    }

    private int updateProgressView(long sofar, float total) {
        final int percent = (int) (100 * sofar / total);
        float p = sofar / total;
        taskProgressBar.setProgress(p);
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

    public void setApplyText(TextView applyText) {
        this.applyText = applyText;
    }

}