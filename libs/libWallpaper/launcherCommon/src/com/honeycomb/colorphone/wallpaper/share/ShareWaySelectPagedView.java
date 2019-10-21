package com.honeycomb.colorphone.wallpaper.share;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.wallpaper.desktop.PagedView;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;

import java.io.File;
import java.util.List;

/**
 * A paged view displayed when pressing an add button in the shared folder.
 */
public class ShareWaySelectPagedView extends PagedView implements View.OnClickListener {

    private Activity activity;

    private int mItemPerPage;
    private int mLastPageIndex = -1;

    private File mShareFile;
    private String mSource;

    public ShareWaySelectPagedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        activity = (Activity) context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // Set default cell content
        setCellCountAndPageSpacing(3, 3, 0);
    }

    public void setShareFile(File file) {
        mShareFile = file;
    }

    public void setSource(String source) {
        mSource = source;
    }

    void setCellCountAndPageSpacing(int countX, int countY, int pageSpacing) {
        mCellCountX = countX;
        mCellCountY = countY;
        mItemPerPage = mCellCountX * mCellCountY;
        mPageSpacing = pageSpacing;
        requestLayout();
    }

    private void reset() {
        removeAllViews();
        mCurrentPage = 0;
        mLastPageIndex = -1;
    }

    // This method must be called synchronously from container activity onCreate, before onAttachedToWindow is called
    void bind(List<ResolveInfo> items) {
        reset();

        for (int i = 0; i < items.size(); i++) {
            ResolveInfo item = items.get(i);
            int pageIndex = i / mItemPerPage;
            int indexOnPage = i - mItemPerPage * pageIndex;
            int cellX = indexOnPage % mCellCountX;
            int cellY = indexOnPage / mCellCountX;
            ShareCellLayout page;
            if (pageIndex > mLastPageIndex) {
                page = new ShareCellLayout(getContext());
                page.setLineNum(mCellCountY);
                page.setColumnNum(mCellCountX);
                LayoutParams params = new LayoutParams(getWidth(), getHeight());
                addView(page, pageIndex, params);
            } else {
                page = (ShareCellLayout) getPageAt(pageIndex);
            }
            View shortcut = createShareAppCell(item);
            page.addView(shortcut, cellX);
            mLastPageIndex = pageIndex;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        // Hide page indicator when there is only one page. This must be called in onAttachedToWindow() because
        // PagedView get its page indicator reference in this method.
        mPageIndicator.setVisibility(mLastPageIndex > 0 ? VISIBLE : INVISIBLE);
    }

    private View createShareAppCell(ResolveInfo info) {
        View cell = activity.getLayoutInflater().inflate(R.layout.share_cell, null, false);
        TextView iconTextView = (TextView) cell.findViewById(R.id.share_app_name);
        cell.setOnClickListener(this);
        iconTextView.setText(getAppLabel(info.activityInfo.packageName));

        ImageView iconButton = (ImageView) cell.findViewById(R.id.share_app_icon);

        Drawable icon = null;
        try {
            icon = HSApplication.getContext().getPackageManager().getApplicationIcon(info.activityInfo.packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        iconButton.setImageDrawable(icon);
        cell.setTag(info);
        return cell;
    }

    @Override
    public void onClick(View v) {
        ResolveInfo info = (ResolveInfo) v.getTag();
        HSLog.e("clickView", "" + v);
        HSAnalytics.logEvent("Alert_ShareBy_IconClicked");
        ShareUtils.shareToFriends(info.activityInfo.packageName, activity, mShareFile, mSource);
        activity.finish();
    }

    @Override
    protected void onUnhandledTap(MotionEvent ev) {
    }

    @Override
    protected void getEdgeVerticalPosition(int[] pos) {
    }

    private String getAppLabel(String packageName) {
        PackageManager packageManager = HSApplication.getContext().getPackageManager();
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = packageManager.getApplicationInfo(packageName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
        }
        return (String) ((null != applicationInfo) ? packageManager.getApplicationLabel(applicationInfo) : "???");
    }

}
