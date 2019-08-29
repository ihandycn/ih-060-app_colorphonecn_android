package com.honeycomb.colorphone.news;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.colorphone.lock.util.ViewUtils;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.util.Analytics;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Dimensions;
import com.superapps.util.Toasts;

import net.appcloudbox.ads.base.AcbNativeAd;
import net.appcloudbox.ads.base.ContainerView.AcbNativeAdContainerView;
import net.appcloudbox.ads.base.ContainerView.AcbNativeAdIconView;
import net.appcloudbox.ads.base.ContainerView.AcbNativeAdPrimaryView;

public class ExitNewsPage extends NewsPage implements NewsManager.NewsLoadListener {

    public ExitNewsPage(@NonNull Context context) {
        this(context, null);
    }

    public ExitNewsPage(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override protected void init() {
        super.init();
        onSelected(true);
        logger = new ExitEventLogger();
    }

    @Override protected void onFinishInflate() {
        super.onFinishInflate();
        setEnabled(false);

        itemViewPadding = Dimensions.pxFromDp(16);

        Analytics.logEvent("Message_News_Show");
    }

    @Override
    protected void showToast(int toast) {
        if (getContext() instanceof Activity) {
            Toasts.showToast(toast);
        }
    }

    @Override
    protected void showToast(String toast) {
        if (getContext() instanceof Activity) {
            Toasts.showToast(toast);
        }
    }

    @Override
    protected void initAdapter() {
        adapter = new ExitNewsAdapter();
    }

    protected class ExitNewsAdapter extends NewsAdapter {
        static final int NEWS_TYPE_HEAD_AD      = 1000;
        static final int NEWS_TYPE_HEAD_TITLE   = 1001;
        static final int NEWS_TYPE_HEAD_NO_NEWS   = 1002;

        boolean isNoNews = false;

        @Override public int getItemViewType(int position) {
            if (position == 0) {
                return NEWS_TYPE_HEAD_AD;
            }

            if (position == 1) {
                return NEWS_TYPE_HEAD_TITLE;
            }

            if (isNoNews && position == 2) {
                return NEWS_TYPE_HEAD_NO_NEWS;
            }

            if (position == getItemCount() - 1) {
                return NEWS_TYPE_FOOT;
            }

            position = position - getHeadCount();

            return super.getItemViewType(position);
        }

        public int getHeadCount() {
            return 2;
        }

        @NonNull @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = null;
            if (viewType == NEWS_TYPE_HEAD_AD) {
                return new NewsHeadNativeHolder(new AcbNativeAdContainerView(getContext()));
            }

            if (viewType == NEWS_TYPE_HEAD_TITLE) {
                view = LayoutInflater.from(getContext()).inflate(R.layout.news_head_title, parent, false);
                return new NewsHeadTitleHolder(view);
            }

            if (isNoNews && viewType == NEWS_TYPE_HEAD_NO_NEWS) {
                view = LayoutInflater.from(getContext()).inflate(R.layout.news_no_network, parent, false);
                return new NewsNoNetworkHolder(view);
            }

            return super.onCreateViewHolder(parent, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            int viewType = getItemViewType(position);

            HSLog.i(NewsManager.TAG, "ENP onBindViewHolder is position: " + position + "   type: " + viewType);
            if (viewType == NEWS_TYPE_HEAD_AD) {
                NewsHeadNativeHolder newsHeadNativeHolder = (NewsHeadNativeHolder) holder;
                newsHeadNativeHolder.bindView(NewsManager.getInstance().getNativeAd());
                return;
            }

            if (itemViewPadding != 0) {
                holder.itemView.setPadding(itemViewPadding, holder.itemView.getPaddingTop(), itemViewPadding, holder.itemView.getPaddingBottom());
            }

            if (viewType == NEWS_TYPE_HEAD_TITLE) {
                return;
            }

            if (isNoNews && viewType == NEWS_TYPE_HEAD_NO_NEWS) {
                return;
            }

            onBindNewsHolder(holder, position - getHeadCount(), viewType);
        }

        @Override public int getItemCount() {
            int size = super.getItemCount();
            if (size == 0) {
                size = 1;
                isNoNews = true;
            } else {
                isNoNews = false;
            }
            size += getHeadCount();
            return size;
        }
    }

    private class NewsNoNetworkHolder extends RecyclerView.ViewHolder {
        NewsNoNetworkHolder(View itemView) {
            super(itemView);
        }
    }


    private class NewsHeadTitleHolder extends RecyclerView.ViewHolder {
        NewsHeadTitleHolder(View itemView) {
            super(itemView);
        }
    }

    private class NewsHeadNativeHolder extends RecyclerView.ViewHolder {
        AcbNativeAdContainerView adContainer;
        AcbNativeAdPrimaryView mAdImageContainer;
        ViewGroup mAdChoice;
        AcbNativeAdIconView mAdIconView;
        TextView mTitleTv;
        TextView mDescriptionTv;
        View mActionBtn;
        View mClickView;
        View mCloseBtn;

        NewsHeadNativeHolder(View root) {
            super(root);

            HSLog.i(NewsManager.TAG, "ENP NewsHeadNativeHolder");
            adContainer = (AcbNativeAdContainerView) root;
            View view = LayoutInflater.from(getContext()).inflate(R.layout.exit_page_ad, adContainer, false);

            mAdImageContainer = ViewUtils.findViewById(view, R.id.news_ad_image);
            mAdChoice = ViewUtils.findViewById(view, R.id.news_ad_choice_icon);
            mAdIconView = ViewUtils.findViewById(view, R.id.news_ad_icon);
            mTitleTv = ViewUtils.findViewById(view, R.id.news_ad_title);
            mDescriptionTv = ViewUtils.findViewById(view, R.id.news_ad_description);
            mActionBtn = ViewUtils.findViewById(view, R.id.news_ad_action_btn);
            mClickView = ViewUtils.findViewById(view, R.id.news_ad_click_view);
            mCloseBtn = ViewUtils.findViewById(view, R.id.news_ad_close);

            adContainer.addContentView(view);
            adContainer.setAdTitleView(mTitleTv);
            adContainer.setAdBodyView(mDescriptionTv);
            adContainer.setAdPrimaryView(mAdImageContainer);
            adContainer.setAdChoiceView(mAdChoice);
            adContainer.setAdIconView(mAdIconView);
            adContainer.setAdActionView(mActionBtn);
        }

        void bindView(AcbNativeAd acbNativeAd) {
            adContainer.fillNativeAd(acbNativeAd, "");
            acbNativeAd.setNativeClickListener(acbAd -> {
                Analytics.logEvent("Message_View_Wire_Ad_Click");
            });

            mDescriptionTv.setText(acbNativeAd.getTitle());
            String title = acbNativeAd.getBody();
            if (TextUtils.isEmpty(title)) {
                title = acbNativeAd.getSubtitle();
            }
            if (TextUtils.isEmpty(title)) {
                title = acbNativeAd.getTitle();
            }
            mTitleTv.setText(title);

            mClickView.setOnClickListener(view -> mActionBtn.performClick());

            mCloseBtn.setOnClickListener(view -> {
                if (getContext() instanceof Activity) {
                    ((Activity) getContext()).finish();
                }
            });

            HSLog.i(NewsManager.TAG, "ENP bindView h: " + adContainer.getHeight());
            adContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    adContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    int height = getHeadViewHeight();
                    int heightSize = height - adContainer.getHeight();

                    HSLog.i(NewsManager.TAG, "ENP bindView onGlobalLayout h == " + height + "  ch == " + heightSize);

                    adContainer.setMinimumHeight(getHeadViewHeight());

                    View imageLayout = adContainer.findViewById(R.id.news_ad_image_layout);
                    imageLayout.setPadding(imageLayout.getPaddingLeft(),
                            imageLayout.getPaddingTop() + heightSize / 3,
                            imageLayout.getPaddingRight(),
                            imageLayout.getPaddingBottom() + heightSize / 3);

                    mTitleTv.setPadding(mTitleTv.getPaddingLeft(),
                            mTitleTv.getPaddingTop() + heightSize / 6,
                            mTitleTv.getPaddingRight(),
                            mTitleTv.getPaddingBottom());

                    mDescriptionTv.setPadding(mDescriptionTv.getPaddingLeft(),
                            mDescriptionTv.getPaddingTop(),
                            mDescriptionTv.getPaddingRight(),
                            mDescriptionTv.getPaddingBottom() + heightSize / 6);
                }
            });
        }

        private int getHeadViewHeight() {
            return Dimensions.getPhoneHeight(getContext())
                    - Dimensions.pxFromDp(70)
                    - Dimensions.getNavigationBarHeight(getContext())
                    - Dimensions.getStatusBarHeight(getContext());
        }
    }

    protected class ExitEventLogger extends NewsPage.EventLogger {
        protected void logListSlide() {
        }

        protected void logNewsLoad(boolean isRefresh, boolean success) {
            if (!isRefresh) {
                Analytics.logEvent("Message_News_LoadMore", Analytics.FLAG_LOG_FABRIC|Analytics.FLAG_LOG_UMENG, "Result", (success ? "Success" : "Fail"));
            }
        }

        protected void logAdClick() {
        }

        protected void logAdShow() {
            Analytics.logEvent("Message_News_Ad_Show");
        }

        protected void logShowNewsDetail(boolean hasNetwork, boolean isVideo) {
            if (hasNetwork) {
                Analytics.logEvent("Message_News_Details_Show",
                        "NewsType", (isVideo ? "Video" : "News") );
            }
        }
    }

}
