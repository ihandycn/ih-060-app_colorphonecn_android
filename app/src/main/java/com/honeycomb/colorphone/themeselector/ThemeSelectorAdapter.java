package com.honeycomb.colorphone.themeselector;

import android.os.Build;
import android.os.Handler;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.acb.call.CPSettings;
import com.acb.call.themes.Type;
import com.acb.call.utils.CallUtils;
import com.acb.call.views.InCallActionView;
import com.acb.call.views.ThemePreviewWindow;
import com.airbnb.lottie.LottieAnimationView;
import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.ColorPhoneApplication;
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

import static com.honeycomb.colorphone.Utils.pxFromDp;

public class ThemeSelectorAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Theme> data = null;
    private GridLayoutManager layoutManager;

    public static final int THEME_SELECTOR_ITEM_TYPE_THEME = 1;
    public static final int THEME_SELECTOR_ITEM_TYPE_STATEMENT = 2;

    private INotificationObserver observer = new INotificationObserver() {
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
    };

    public ThemeSelectorAdapter(final ArrayList<Theme> data) {
        this.data = data;
        GridLayoutManager.SpanSizeLookup spanSizeLookup = new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch (getItemViewType(position)) {
                    case THEME_SELECTOR_ITEM_TYPE_THEME:
                        return 1;
                    case THEME_SELECTOR_ITEM_TYPE_STATEMENT:
                        return 2;
                    default:
                        return 1;
                }
            }
        };
        layoutManager = new GridLayoutManager(HSApplication.getContext(), 2);
        layoutManager.setSpanSizeLookup(spanSizeLookup);
    }

    public GridLayoutManager getLayoutManager() {
        return layoutManager;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        HSGlobalNotificationCenter.addObserver(ThemePreviewActivity.NOTIFY_THEME_SELECT, observer);

    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        HSGlobalNotificationCenter.removeObserver(observer);
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        if(viewType == THEME_SELECTOR_ITEM_TYPE_THEME) {
            View cardViewContent = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_theme_selector, null);
            final ThemeCardViewHolder holder = new ThemeCardViewHolder(cardViewContent);

            cardViewContent.findViewById(R.id.card_view).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = holder.getPositionTag();
                    Theme theme = data.get(pos);
                    Type type = CallUtils.getTypeByThemeId(theme.getThemeId());
                    if (type != null && type.isGif()) {
                        holder.getDownloadHolder().startDownloadDelay(0);
                    }
                    ThemePreviewActivity.start(parent.getContext(), theme);
                }
            });
            // Disable theme original bg. Use our own
            holder.mThemeFlashPreviewWindow.setBgDrawable(null);
            holder.mThemeSelectLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getPositionTag();
                    if (onSelectedTheme(pos)) {
                        CPSettings.putInt(CPSettings.PREFS_SCREEN_FLASH_THEME_ID, data.get(pos).getThemeId());
                    }
                }
            });

            return holder;
        } else  {
            View stateViewContent = LayoutInflater.from((parent.getContext())).inflate(R.layout.card_view_contains_ads_statement, null);
            return new StatementViewHolder(stateViewContent);
        }
    }

    private boolean onSelectedTheme(int pos) {
        int prePos = 0;
        // Clear before.
        for (int i = 0; i < data.size(); i++) {
            Theme t = data.get(i);
            if (t.isSelected()) {
                prePos = i;
                break;
            }
        }
        if (prePos == pos) {
            return false;
        } else {
            Theme t = data.get(prePos);
            t.setSelected(false);
            notifyItemChanged(prePos);
        }
        // Reset current.
        Type type = CallUtils.getTypeByThemeId( data.get(pos).getThemeId());


        HSGlobalNotificationCenter.sendNotification(ThemePreviewActivity.NOTIFY_THEME_SELECT);
        ColorPhoneApplication.getConfigLog().getEvent().onChooseTheme(type.name().toLowerCase());
        data.get(pos).setSelected(true);
        notifyItemChanged(pos);
        return true;
    }

    // TODO Use bitmap to improve draw performance

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ThemeCardViewHolder) {
            ((ThemeCardViewHolder) holder).setPositionTag(position);

            if (position % 2 == 0) {
                ((ThemeCardViewHolder) holder).getContentView().setTranslationX(pxFromDp(9));
            } else {
                ((ThemeCardViewHolder) holder).getContentView().setTranslationX(pxFromDp(-9));
            }
            final Theme curTheme = data.get(position);
            final Type type = CallUtils.getTypeByThemeId(curTheme.getThemeId());

            String name = curTheme.getName();
            ((ThemeCardViewHolder) holder).setTxt(name);
            ((ThemeCardViewHolder) holder).mThemeLikeCount.setText(String.valueOf(curTheme.getDownload()));
            if (curTheme.getImageRes() > 0) {
                ((ThemeCardViewHolder) holder).mThemePreviewImg.setImageResource(curTheme.getImageRes());
            } else {
                ((ThemeCardViewHolder) holder).mThemePreviewImg.setImageResource(R.drawable.card_bg_round_dark);
            }

            ((ThemeCardViewHolder) holder).mThemeFlashPreviewWindow.updateThemeLayout(type);
            ((ThemeCardViewHolder) holder).mCallActionView.setTheme(type);
            if (!curTheme.isSelected()) {
                ((ThemeCardViewHolder) holder).mThemeFlashPreviewWindow.stopAnimations();
                ((ThemeCardViewHolder) holder).mCallActionView.setAutoRun(false);
                ((ThemeCardViewHolder) holder).mThemeFlashPreviewWindow.setAutoRun(false);
            } else {
                ((ThemeCardViewHolder) holder).mThemeFlashPreviewWindow.playAnimation(type);
                ((ThemeCardViewHolder) holder).mThemeFlashPreviewWindow.setAutoRun(true);
                ((ThemeCardViewHolder) holder).mCallActionView.setAutoRun(true);
            }

            ((ThemeCardViewHolder) holder).setSelected(curTheme.isSelected());
            ((ThemeCardViewHolder) holder).setHotTheme(curTheme.isHot());

            // Download progress
            final TasksManagerModel model = TasksManager.getImpl().getByThemeId(curTheme.getThemeId());

            if (model != null) {
                ((ThemeCardViewHolder) holder).update(model.getId(), position);
                boolean fileExist = updateTaskHolder((ThemeCardViewHolder) holder, model);
                ((ThemeCardViewHolder) holder).switchToReadyState(fileExist);
            } else {
                ((ThemeCardViewHolder) holder).switchToReadyState(true);
            }
        } else {
            HSLog.d("onBindVieHolder","contains ads statement.");
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(position < data.size()){
            return THEME_SELECTOR_ITEM_TYPE_THEME;
        } else {
            return THEME_SELECTOR_ITEM_TYPE_STATEMENT;
        }
//        return super.getItemViewType(position);
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
        // +1 for statement
        return data.size() + 1;
    }

    static class ThemeCardViewHolder extends RecyclerView.ViewHolder implements DownloadHolder {
        private static final boolean DEBUG_PROGRESS = BuildConfig.DEBUG & true;
        ImageView mThemePreviewImg;
        TextView mThemeTitle;
        TextView mThemeLikeCount;
        ThemePreviewWindow mThemeFlashPreviewWindow;
        InCallActionView mCallActionView;

        final LottieAnimationView mDownloadFinishedAnim;
        final LottieAnimationView mThemeSelectedAnim;
        View mThemeSelectLayout;

        DownloadViewHolder mDownloadViewHolder;

        private int mPositionTag;
        private View mContentView;
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

        public View getContentView() {
            return mContentView;
        }

        ThemeCardViewHolder(View itemView) {
            super(itemView);

            mContentView = itemView;
            mThemePreviewImg = (ImageView) itemView.findViewById(R.id.card_preview_img);
            mThemeTitle = (TextView) itemView.findViewById(R.id.card_title);
            mThemeLikeCount = (TextView) itemView.findViewById(R.id.card_like_count_txt);
            mThemeFlashPreviewWindow = (ThemePreviewWindow) itemView.findViewById(R.id.card_flash_preview_window);
            mCallActionView = (InCallActionView) itemView.findViewById(R.id.card_in_call_action_view);
            mCallActionView.setAutoRun(false);
            mThemeHotMark = itemView.findViewById(R.id.theme_hot_mark);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mThemeHotMark.setElevation(Utils.pxFromDp(2));
                mThemeHotMark.setTranslationX(pxFromDp(-1));
            }

            mDownloadTaskProgressTxt = (TypefacedTextView) itemView.findViewById(R.id.card_downloading_progress_txt);
            mDownloadTaskProgressTxt.setVisibility(View.GONE);
            mDownloadFinishedAnim = (LottieAnimationView) itemView.findViewById(R.id.card_download_finished_anim);
            mDownloadFinishedAnim.setVisibility(View.GONE);
            mThemeSelectedAnim = (LottieAnimationView) itemView.findViewById(R.id.card_theme_selected_anim);
            mThemeSelectLayout = itemView.findViewById(R.id.card_theme_selected_layout);

            DownloadProgressBar pb = (DownloadProgressBar) itemView.findViewById(R.id.card_downloading_progress_bar);

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

        private Runnable mAniamtionEndStateRunnable = new Runnable() {
            @Override
            public void run() {
                switchToReadyState(true);
            }
        };

        public void update(final int id, final int position) {
            this.id = id;
            this.position = position;
            this.mDownloadViewHolder.bindTaskId(id);
        }

        @Override
        public void updateDownloaded(final boolean progressFlag) {
            // If file already downloaded, not play animation

            mDownloadViewHolder.updateDownloaded(progressFlag);
            mDownloadTaskProgressBar.removeCallbacks(mAniamtionEndStateRunnable);
            if (progressFlag) {
                mDownloadTaskProgressBar.postDelayed(mAniamtionEndStateRunnable, 600);
            }
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
            mThemeSelectLayout.setVisibility(ready ? View.VISIBLE : View.GONE);
            if (ready) {
                mDownloadFinishedAnim.setVisibility(View.GONE);
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

    static class StatementViewHolder extends RecyclerView.ViewHolder {

        public StatementViewHolder(View itemView) {
            super(itemView);
        }
    }
}