package com.honeycomb.colorphone.themeselector;

import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.acb.call.themes.Type;
import com.acb.call.views.InCallActionView;
import com.acb.call.views.ThemePreviewWindow;
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.ThemePreviewActivity;
import com.honeycomb.colorphone.download.TasksManager;
import com.honeycomb.colorphone.download.TasksManagerModel;
import com.honeycomb.colorphone.view.DownloadProgressBar;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloadSampleListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.liulishuo.filedownloader.util.FileDownloadUtils;

import java.io.File;
import java.util.ArrayList;

import static com.liulishuo.filedownloader.model.FileDownloadStatus.pending;

public class ThemeSelectorAdapter extends RecyclerView.Adapter<ThemeSelectorAdapter.ThemeCardViewHolder> {

    private ArrayList<Theme> data = null;

    public ThemeSelectorAdapter(ArrayList<Theme> data) {
        this.data = data;
    }

    @Override
    public ThemeCardViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View cardViewContent = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_theme_selector, null);
        final ThemeCardViewHolder holder = new ThemeCardViewHolder(cardViewContent);

        cardViewContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = holder.getPositionTag();
                ThemePreviewActivity.start(parent.getContext(), data.get(pos));
                Toast.makeText(HSApplication.getContext(), holder.getPositionTag() + " clicked", Toast.LENGTH_SHORT).show();
            }
        });
        // Disable theme original bg. Use our own
        holder.previewWindow.setBgDrawable(null);
        holder.taskActionBtn.setOnClickListener(taskActionOnClickListener);
        holder.apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Apply
            }
        });

        return holder;
    }

    // TODO Use bitmap to improve draw performance

    @Override
    public void onBindViewHolder(ThemeCardViewHolder holder, int position) {
        holder.setPositionTag(position);

        final Theme curTheme = data.get(position);
        String name = curTheme.getName();
        holder.setTxt(name);
        holder.downloadTxt.setText(String.valueOf(curTheme.getDownload()));
        if (curTheme.getImageRes() > 0) {
            holder.img.setImageResource(curTheme.getImageRes());
        } else {
            holder.img.setImageDrawable(null);
        }

        holder.previewWindow.playAnimation(Type.values()[curTheme.getThemeId()]);
        if (!curTheme.isSelected()) {
            holder.previewWindow.stopAnimations();
            holder.callActionView.setAutoRun(false);

        } else {
            holder.previewWindow.setAutoRun(true);
            holder.callActionView.setAutoRun(true);
        }

        holder.setSelected(curTheme.isSelected());
        holder.setHotTheme(curTheme.isHot());

        // Download progress
        final TasksManagerModel model = TasksManager.getImpl().getByThemeId(curTheme.getThemeId());

        if (model != null) {
            holder.showOpen(false);
            holder.update(model.getId(), position);
            updateTaskHolder(holder, model);
        } else {
            holder.showOpen(true);
        }

    }

    private void updateTaskHolder(ThemeCardViewHolder holder, TasksManagerModel model) {
        holder.taskActionBtn.setTag(holder);

        final BaseDownloadTask task = TasksManager.getImpl()
                .getTask(holder.id);
        if (task != null) {
            task.setTag(holder);
        }

        holder.taskActionBtn.setEnabled(true);


        if (TasksManager.getImpl().isReady()) {
            final int status = TasksManager.getImpl().getStatus(model.getId(), model.getPath());
            if (status == pending || status == FileDownloadStatus.started ||
                    status == FileDownloadStatus.connected) {
                // start task, but file not created yet
                holder.updateDownloading(status, TasksManager.getImpl().getSoFar(model.getId())
                        , TasksManager.getImpl().getTotal(model.getId()));
            } else if (!new File(model.getPath()).exists() &&
                    !new File(FileDownloadUtils.getTempPath(model.getPath())).exists()) {
                // not exist file
                holder.updateNotDownloaded(status, 0, 0);
            } else if (TasksManager.getImpl().isDownloaded(status)) {
                // already downloaded and exist
                holder.updateDownloaded();
            } else if (status == FileDownloadStatus.progress) {
                // downloading
                holder.updateDownloading(status, TasksManager.getImpl().getSoFar(model.getId())
                        , TasksManager.getImpl().getTotal(model.getId()));
            } else {
                // not start
                holder.updateNotDownloaded(status, TasksManager.getImpl().getSoFar(model.getId())
                        , TasksManager.getImpl().getTotal(model.getId()));
            }
        } else {
//            holder.taskStatusTv.setText(R.string.tasks_manager_demo_status_loading);
            holder.taskActionBtn.setEnabled(false);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private FileDownloadListener taskDownloadListener = new FileDownloadSampleListener() {

        private ThemeCardViewHolder checkCurrentHolder(final BaseDownloadTask task) {
            final ThemeCardViewHolder tag = (ThemeCardViewHolder) task.getTag();
            if (tag.id != task.getId()) {
                return null;
            }

            return tag;
        }

        @Override
        protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            super.pending(task, soFarBytes, totalBytes);
            final ThemeCardViewHolder tag = checkCurrentHolder(task);
            if (tag == null) {
                return;
            }

            tag.updateDownloading(pending, soFarBytes
                    , totalBytes);
//            tag.taskStatusTv.setText(R.string.tasks_manager_demo_status_pending);
        }

        @Override
        protected void started(BaseDownloadTask task) {
            super.started(task);
            final ThemeCardViewHolder tag = checkCurrentHolder(task);
            if (tag == null) {
                return;
            }

//            tag.taskStatusTv.setText(R.string.tasks_manager_demo_status_started);
        }

        @Override
        protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
            super.connected(task, etag, isContinue, soFarBytes, totalBytes);
            final ThemeCardViewHolder tag = checkCurrentHolder(task);
            if (tag == null) {
                return;
            }

            tag.updateDownloading(FileDownloadStatus.connected, soFarBytes
                    , totalBytes);
//            tag.taskStatusTv.setText(R.string.tasks_manager_demo_status_connected);
        }

        @Override
        protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            super.progress(task, soFarBytes, totalBytes);
            final ThemeCardViewHolder tag = checkCurrentHolder(task);
            if (tag == null) {
                return;
            }

            tag.updateDownloading(FileDownloadStatus.progress, soFarBytes
                    , totalBytes);
        }

        @Override
        protected void error(BaseDownloadTask task, Throwable e) {
            super.error(task, e);
            final ThemeCardViewHolder tag = checkCurrentHolder(task);
            if (tag == null) {
                return;
            }

            tag.updateNotDownloaded(FileDownloadStatus.error, task.getLargeFileSoFarBytes()
                    , task.getLargeFileTotalBytes());
            TasksManager.getImpl().removeTaskForViewHolder(task.getId());
        }

        @Override
        protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            super.paused(task, soFarBytes, totalBytes);
            final ThemeCardViewHolder tag = checkCurrentHolder(task);
            if (tag == null) {
                return;
            }

            tag.updateNotDownloaded(FileDownloadStatus.paused, soFarBytes, totalBytes);
//            tag.taskStatusTv.setText(R.string.tasks_manager_demo_status_paused);
            TasksManager.getImpl().removeTaskForViewHolder(task.getId());
        }

        @Override
        protected void completed(BaseDownloadTask task) {
            super.completed(task);
            final ThemeCardViewHolder tag = checkCurrentHolder(task);
            if (tag == null) {
                return;
            }

            tag.updateDownloaded();
            TasksManager.getImpl().removeTaskForViewHolder(task.getId());
        }
    };

    private View.OnClickListener taskActionOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getTag() == null) {
                return;
            }

            final ThemeCardViewHolder holder = (ThemeCardViewHolder) v.getTag();

            if (holder.canPaused()) {
                // to pause
                FileDownloader.getImpl().pause(holder.id);
            } else if (holder.canStartDownload()) {
                holder.startDownload(new Runnable() {
                    @Override
                    public void run() {
                        startDownload(holder);
                    }
                });
            }
        }

        private void startDownload(ThemeCardViewHolder holder) {
            // to start
            final TasksManagerModel model = TasksManager.getImpl().getById(holder.id);
            if (model != null) {
                final BaseDownloadTask task = FileDownloader.getImpl().create(model.getUrl())
                        .setPath(model.getPath())
                        .setCallbackProgressTimes(100)
                        .setListener(taskDownloadListener);

                TasksManager.getImpl()
                        .addTaskForViewHolder(task);

                task.setTag(holder);

                task.start();
            } else {
                throw new IllegalStateException("Has no pending task to download!");
            }
        }
    };


    static class ThemeCardViewHolder extends RecyclerView.ViewHolder {
        private static final boolean DEBUG_PROGRESS = BuildConfig.DEBUG & true;
        ImageView img;
        TextView txt;
        View apply;
        TextView downloadTxt;
        ThemePreviewWindow previewWindow;
        InCallActionView callActionView;

        private int positionTag;
        private final View hotView;
        private final View selectedView;

        private int mDownloadStatus;
        private boolean canPaused;
        private boolean canStart;

        private Handler mHandler = new Handler();
        private boolean pendingToOpen;

        public void setPositionTag(int position) {
            positionTag = position;
        }

        public void setTxt(String string) {
            txt.setText(string);
        }

        public int getPositionTag() {
            return positionTag;
        }

        ThemeCardViewHolder(View itemView) {
            super(itemView);

            img = (ImageView) itemView.findViewById(R.id.card_view_img);
            txt = (TextView) itemView.findViewById(R.id.card_name);
            apply = itemView.findViewById(R.id.applyBtn);

            selectedView = itemView.findViewById(R.id.theme_selected);
            hotView = itemView.findViewById(R.id.theme_hot);
            downloadTxt = (TextView)itemView.findViewById(R.id.theme_download_txt);
            previewWindow = (ThemePreviewWindow) itemView.findViewById(R.id.flash_view);
            callActionView = (InCallActionView) itemView.findViewById(R.id.in_call_view);
            callActionView.setAutoRun(false);

//            taskNameTv = (TextView) findViewById(R.id.task_name_tv);
//            taskStatusTv = (TextView) findViewById(R.id.task_status_tv);
            taskPb = (DownloadProgressBar) itemView.findViewById(R.id.progressBar);
            taskPb.setOnProgressUpdateListener(new DownloadProgressBar.OnProgressUpdateListener() {
                @Override
                public void onProgressUpdate(float currentPlayTime) {

                }

                @Override
                public void onAnimationStarted() {

                }

                @Override
                public void onAnimationEnded() {
                    showOpen(pendingToOpen);
                }

                @Override
                public void onAnimationSuccess() {

                }

                @Override
                public void onAnimationError() {

                }

                @Override
                public void onManualProgressStarted() {

                }

                @Override
                public void onManualProgressEnded() {

                }
            });
            taskActionBtn = taskPb;
//            taskActionBtn = (Button) findViewById(R.id.task_action_btn);

        }

        public void setSelected(boolean selected) {
            if (selectedView != null) {
                selectedView.setVisibility(selected ? View.VISIBLE : View.INVISIBLE);
            }
        }

        public void setHotTheme(boolean hot) {
            if (hotView != null) {
                hotView.setVisibility(hot ? View.VISIBLE : View.INVISIBLE);
            }
        }

        //---------------- For progress ---------
        /**
         * viewHolder position
         */
        private int position;
        /**
         * com.honeycomb.colorphone.download id
         */
        private int id;

//
//        private TextView taskNameTv;
//        private TextView taskStatusTv;
        private DownloadProgressBar taskPb;
        private View taskActionBtn;

        public void update(final int id, final int position) {
            this.id = id;
            this.position = position;
        }

        public void startDownload(Runnable playAnimEnd) {
            taskPb.playManualProgressAnimation();
            if (playAnimEnd != null) {
                txt.postDelayed(playAnimEnd, 1000);
            }
        }

        public void updateDownloaded() {
            // If file already downloaded, not play animation
            if (mDownloadStatus != FileDownloadStatus.INVALID_STATUS) {
                taskPb.setProgress(100);
                pendingToOpen = true;
            } else {
                showOpen(true);
            }

            if (DEBUG_PROGRESS) {
                HSLog.d("sundxing", "download success!");
            }

            mDownloadStatus = FileDownloadStatus.completed;
//            taskStatusTv.setText(R.string.tasks_manager_demo_status_completed);
//            taskActionBtn.setText(R.string.delete);
        }

        public void updateNotDownloaded(final int status, final long sofar, final long total) {

            if (DEBUG_PROGRESS) {
                HSLog.d("sundxing", "download stopped, status = " + status);
            }
            if (sofar > 0 && total > 0) {
                final float percent = sofar
                        / (float) total;
                taskPb.setProgress((int) (percent * 100));
            } else {
                taskPb.reset();
            }

            mDownloadStatus = status;
            canPaused = false;
            canStart = true;
//            switch (status) {
//                case FileDownloadStatus.error:
//                    taskStatusTv.setText(R.string.tasks_manager_demo_status_error);
//                    break;
//                case FileDownloadStatus.paused:
//                    taskStatusTv.setText(R.string.tasks_manager_demo_status_paused);
//                    break;
//                default:
//                    taskStatusTv.setText(R.string.tasks_manager_demo_status_not_downloaded);
//                    break;
//            }
//            taskActionBtn.setText(R.string.start);
        }

        public void updateDownloading(final int status, final long sofar, final long total) {

            final float percent = sofar
                    / (float) total;
            taskPb.setProgress((int) (percent * 100));
            if (DEBUG_PROGRESS) {
                HSLog.d("sundxing", "download process, percent = " + percent);
            }
            mDownloadStatus = status;
            canPaused = true;
            canStart = false;
//            switch (status) {
//                case FileDownloadStatus.pending:
//                    taskStatusTv.setText(R.string.tasks_manager_demo_status_pending);
//                    break;
//                case FileDownloadStatus.started:
//                    taskStatusTv.setText(R.string.tasks_manager_demo_status_started);
//                    break;
//                case FileDownloadStatus.connected:
//                    taskStatusTv.setText(R.string.tasks_manager_demo_status_connected);
//                    break;
//                case FileDownloadStatus.progress:
//                    taskStatusTv.setText(R.string.tasks_manager_demo_status_progress);
//                    break;
//                default:
//                    taskStatusTv.setText(DemoApplication.CONTEXT.getString(
//                            R.string.tasks_manager_demo_status_downloading, status));
//                    break;
//            }
//
//            taskActionBtn.setText(R.string.pause);
        }

        public boolean canPaused() {
            return canPaused;

        }

        public boolean canStartDownload() {
            return canStart;
        }

        public void showOpen(boolean valid) {
            taskPb.setVisibility(valid ? View.GONE : View.VISIBLE);
            apply.setVisibility(valid ? View.VISIBLE : View.GONE);
        }
    }
}