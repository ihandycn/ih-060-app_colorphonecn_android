package com.honeycomb.colorphone.themeselector;

import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.acb.call.CPSettings;
import com.acb.call.themes.Type;
import com.acb.call.views.InCallActionView;
import com.acb.call.views.ThemePreviewWindow;
import com.airbnb.lottie.LottieAnimationView;
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.ThemePreviewActivity;
import com.honeycomb.colorphone.Utils;
import com.honeycomb.colorphone.download.DownloadHolder;
import com.honeycomb.colorphone.download.DownloadViewHolder;
import com.honeycomb.colorphone.download.TasksManager;
import com.honeycomb.colorphone.download.TasksManagerModel;
import com.honeycomb.colorphone.view.DownloadProgressBar;
import com.honeycomb.colorphone.view.TypefacedTextView;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.util.FileDownloadUtils;

import java.io.File;
import java.util.ArrayList;

public class ThemeSelectorAdapter extends RecyclerView.Adapter<ThemeSelectorAdapter.ThemeCardViewHolder> {

    private ArrayList<Theme> data = null;

    public ThemeSelectorAdapter(final ArrayList<Theme> data) {
        this.data = data;
        HSGlobalNotificationCenter.addObserver(ThemePreviewActivity.NOTIFY_THEME_SELECT, new INotificationObserver() {
            @Override
            public void onReceive(String s, HSBundle hsBundle) {
                if (hsBundle != null) {
                    int themeId = hsBundle.getInt(ThemePreviewActivity.NOTIFY_THEME_SELECT_KEY);
                    for (Theme theme : data) {
                        if (theme.getThemeId() == themeId) {
                            onSelectedTheme(data.indexOf(theme));
                        }
                    }
                }
            }
        });
    }

    @Override
    public ThemeCardViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View cardViewContent = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_theme_selector, null);
        final ThemeCardViewHolder holder = new ThemeCardViewHolder(cardViewContent);

        cardViewContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = holder.getPositionTag();
                Theme theme = data.get(pos);
                Type type = Utils.getTypeByThemeId(theme.getThemeId());
                if (type != null && type.isGif()) {
                    holder.getDownloadHolder().startDownloadDelay(0);
                }
                ThemePreviewActivity.start(parent.getContext(), theme);
                Toast.makeText(HSApplication.getContext(), holder.getPositionTag() + " clicked", Toast.LENGTH_SHORT).show();
            }
        });
        // Disable theme original bg. Use our own
        holder.mThemeFlashPreviewWindow.setBgDrawable(null);
        holder.mThemeSelectedAnim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.mThemeSelectedAnim.playAnimation();
                int pos = holder.getPositionTag();
                onSelectedTheme(pos);
                CPSettings.putInt(CPSettings.PREFS_SCREEN_FLASH_SELECTOR_INDEX, data.get(pos).getThemeId());
            }
        });

        return holder;
    }

    private void onSelectedTheme(int pos) {
        // Clear before.
        for (int i = 0; i < data.size(); i++) {
            Theme t = data.get(i);
            if (t.isSelected()) {
                t.setSelected(false);
                notifyItemChanged(i);
                break;
            }
        }
        // Reset current.
        data.get(pos).setSelected(true);
        notifyItemChanged(pos);
    }

    // TODO Use bitmap to improve draw performance

    @Override
    public void onBindViewHolder(ThemeCardViewHolder holder, int position) {
        holder.setPositionTag(position);

        final Theme curTheme = data.get(position);
        final Type type = Utils.getTypeByThemeId(curTheme.getThemeId());

        String name = curTheme.getName();
        holder.setTxt(name);
        holder.mThemeLikeCount.setText(String.valueOf(curTheme.getDownload()));
        if (curTheme.getImageRes() > 0) {
            holder.mThemePreviewImg.setImageResource(curTheme.getImageRes());
        } else {
            holder.mThemePreviewImg.setImageResource(R.drawable.card_bg_round_dark);
        }

        holder.mThemeFlashPreviewWindow.updateThemeLayout(type);
        holder.mCallActionView.setTheme(type);
        if (!curTheme.isSelected()) {
            holder.mThemeFlashPreviewWindow.stopAnimations();
            holder.mCallActionView.setAutoRun(false);
            holder.mThemeFlashPreviewWindow.setAutoRun(false);
        } else {
            holder.mThemeFlashPreviewWindow.playAnimation(type);
            holder.mThemeFlashPreviewWindow.setAutoRun(true);
            holder.mCallActionView.setAutoRun(true);
        }

        holder.setSelected(curTheme.isSelected());
        holder.setHotTheme(curTheme.isHot());

        // Download progress
        final TasksManagerModel model = TasksManager.getImpl().getByThemeId(curTheme.getThemeId());

        if (model != null) {
            holder.update(model.getId(), position);
            boolean fileExist = updateTaskHolder(holder, model);
            holder.switchToReadyState(fileExist);
        } else {
            holder.switchToReadyState(true);
        }

    }

    private boolean updateTaskHolder(ThemeCardViewHolder holder, TasksManagerModel model) {
        final BaseDownloadTask task = TasksManager.getImpl()
                .getTask(holder.id);
        if (task != null) {
            task.setTag(holder);
        }

        holder.setActionEnabled(true);
        boolean showOpen = false;

        if (TasksManager.getImpl().isReady()) {
            final int status = TasksManager.getImpl().getStatus(model.getId(), model.getPath());
            HSLog.d("sundxing", "position " + holder.position + ",download task status: " + status);
            if (TasksManager.getImpl().isDownloading(status)) {
                // start task, but file not created yet
                // Or just downloading
                holder.updateDownloading(status, TasksManager.getImpl().getSoFar(model.getId())
                        , TasksManager.getImpl().getTotal(model.getId()));
            } else if (!new File(model.getPath()).exists() &&
                    !new File(FileDownloadUtils.getTempPath(model.getPath())).exists()) {
                // not exist file
                holder.updateNotDownloaded(status, 0, 0);
            } else if (TasksManager.getImpl().isDownloaded(status)) {
                // already downloaded and exist
                holder.updateDownloaded(false);
                showOpen = true;
            } else {
                // not start
                holder.updateNotDownloaded(status, TasksManager.getImpl().getSoFar(model.getId())
                        , TasksManager.getImpl().getTotal(model.getId()));
            }
        } else {
            holder.setActionEnabled(false);
        }

        return showOpen;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ThemeCardViewHolder extends RecyclerView.ViewHolder implements DownloadHolder {
        private static final boolean DEBUG_PROGRESS = BuildConfig.DEBUG & true;
        ImageView mThemePreviewImg;
        TextView mThemeTitle;
        TextView mThemeLikeCount;
        ThemePreviewWindow mThemeFlashPreviewWindow;
        InCallActionView mCallActionView;

        LottieAnimationView mDownloadFinishedAnim;
        LottieAnimationView mThemeSelectedAnim;

        DownloadViewHolder mDownloadViewHolder;

        private int mPositionTag;
        private final View mThemeHotMark;

        private Handler mHandler = new Handler();
        private boolean pendingToOpen;

        public void setPositionTag(int position) {
            mPositionTag = position;
        }

        public void setTxt(String string) {
            mThemeTitle.setText(string);
        }

        public int getPositionTag() {
            return mPositionTag;
        }

        ThemeCardViewHolder(View itemView) {
            super(itemView);

            mThemePreviewImg = (ImageView) itemView.findViewById(R.id.card_preview_img);
            mThemeTitle = (TextView) itemView.findViewById(R.id.card_title);
            mThemeLikeCount = (TextView) itemView.findViewById(R.id.card_like_count_txt);
            mThemeFlashPreviewWindow = (ThemePreviewWindow) itemView.findViewById(R.id.card_flash_preview_window);
            mCallActionView = (InCallActionView) itemView.findViewById(R.id.card_in_call_action_view);
            mCallActionView.setAutoRun(false);
            mThemeHotMark = itemView.findViewById(R.id.theme_hot_mark);

            mDownloadTaskProgressTxt = (TypefacedTextView) itemView.findViewById(R.id.card_downloading_progress_txt);
            mDownloadTaskProgressTxt.setVisibility(View.INVISIBLE);
            mDownloadFinishedAnim = (LottieAnimationView) itemView.findViewById(R.id.card_download_finished_anim);
            mThemeSelectedAnim = (LottieAnimationView) itemView.findViewById(R.id.card_theme_selected_anim);

            DownloadProgressBar pb = (DownloadProgressBar) itemView.findViewById(R.id.card_downloading_progress_bar);
//            pb.setOnProgressUpdateListener(new DownloadProgressBar.SampleOnProgressUpdateListener() {
//                @Override
//                public void onAnimationEnded() {
//                    super.onAnimationEnded();
//                    switchToReadyState(pendingToOpen);
//                }
//            });
            mDownloadViewHolder = new DownloadViewHolder(pb, pb, mDownloadTaskProgressTxt, mDownloadFinishedAnim);
            mDownloadViewHolder.setProxyHolder(this);
            mDownloadTaskProgressBar = pb;
        }

        public void setSelected(boolean selected) {
            if (mThemeSelectedAnim != null) {
                if (selected) {
                    mThemeSelectedAnim.playAnimation();
                } else {
                    mThemeSelectedAnim.cancelAnimation();
                    mThemeSelectedAnim.setProgress(0f);
                }
            }
        }

        public void setHotTheme(boolean hot) {
            if (mThemeHotMark != null) {
                mThemeHotMark.setVisibility(hot ? View.VISIBLE : View.INVISIBLE);
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

        private View mDownloadTaskProgressBar;
        private TypefacedTextView mDownloadTaskProgressTxt;

        public void update(final int id, final int position) {
            this.id = id;
            this.position = position;
            this.mDownloadViewHolder.bindTaskId(id);
        }

        @Override
        public void updateDownloaded(boolean progressFlag) {
            // If file already downloaded, not play animation

            switchToReadyState(true);

            mDownloadViewHolder.updateDownloaded(progressFlag);
            mDownloadFinishedAnim.playAnimation();

            if (DEBUG_PROGRESS) {
                HSLog.d("sundxing", position + " download success!");
            }
        }

        public void updateNotDownloaded(final int status, final long sofar, final long total) {

            if (DEBUG_PROGRESS) {
                HSLog.d("sundxing", position + " download stopped, status = " + status);
            }
            mDownloadViewHolder.updateNotDownloaded(status, sofar, total);
        }

        public void updateDownloading(final int status, final long sofar, final long total) {

            if (DEBUG_PROGRESS) {
                final float percent = sofar
                        / (float) total;
                HSLog.d("sundxing", position + " download process, percent = " + percent);
            }
            mDownloadViewHolder.updateDownloading(status, sofar, total);

        }

        public void switchToReadyState(boolean ready) {
            mDownloadTaskProgressBar.setVisibility(ready ? View.GONE : View.VISIBLE);
            if (ready) {
                mDownloadTaskProgressTxt.setVisibility(View.GONE);
            }
            if (ready) {
                mThemeSelectedAnim.setVisibility(View.VISIBLE);
                mThemeSelectedAnim.setProgress(0f);
            } else {
                mThemeSelectedAnim.setVisibility(View.GONE);
            }
        }

        public DownloadViewHolder getDownloadHolder() {
            return mDownloadViewHolder;
        }

        public void setActionEnabled(boolean enable) {
            mDownloadTaskProgressBar.setEnabled(enable);
        }

        @Override
        public int getId() {
            return id;
        }
    }
}