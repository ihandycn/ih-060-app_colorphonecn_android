package com.acb.libwallpaper.live.share;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.acb.libwallpaper.R;
import com.acb.libwallpaper.live.util.Utils;

/**
 * A view for adding more apps to a folder.
 */
public class ShareWaySelectView extends FrameLayout implements View.OnClickListener {

    private Activity mLauncher;

    private GestureDetector mGestureDetector;

    private View mContentWrapper;
    private ShareWaySelectPagedView mContent;

    public ShareWaySelectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mLauncher = (Activity) context;
        mGestureDetector = new GestureDetector(context, new BackgroundTapListener());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mContentWrapper = findViewById(R.id.share_wrapper_view_content);
        mContent = (ShareWaySelectPagedView) findViewById(R.id.share_way_select_paged_view);

        Resources resources = getContext().getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float ratio = (float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT;

        measureContent(Utils.getScreenSize(mLauncher), ratio);
    }

    private void measureContent(Point screenSize, float densityRatio) {
        LayoutParams wrapperParams = (LayoutParams) mContentWrapper.getLayoutParams();
        Resources res = mLauncher.getResources();

        float preOccupiedHeightMargin = 52 * densityRatio;
        float preOccupiedHeight = preOccupiedHeightMargin +
                res.getDimension(R.dimen.paged_pad_view_title_height) +
                res.getDimension(R.dimen.paged_pad_view_indicator_height) +
                res.getDimension(R.dimen.paged_pad_view_buttons_height);
        float preOccupiedWidth = 2 * res.getDimension(R.dimen.paged_pad_view_page_spacing);

        float cellHeight = res.getDimension(R.dimen.paged_pad_view_cell_height);
        float cellWidth = res.getDimension(R.dimen.paged_pad_view_cell_width);
        int cellY = (int) (Math.floor(0.70 * screenSize.y - preOccupiedHeight) / cellHeight);
        int heightRequired = (int) (Math.ceil(cellHeight * cellY + preOccupiedHeight));
        int cellX = (int) (Math.floor(0.95 * screenSize.x - preOccupiedWidth) / cellWidth);
        int widthRequired = (int) (Math.ceil(cellWidth * cellX) + preOccupiedWidth);

        if (cellX < 1) cellX = 1;
        if (cellY < 1) cellY = 1;

        int widthPx = Math.round(widthRequired);
        int heightPx = Math.round(heightRequired);

        // Map width / height to margins
        int lrMargin = (screenSize.x - widthPx) / 2;
        int tbMargin = (screenSize.y - heightPx) / 2;
        wrapperParams.setMargins(lrMargin, tbMargin, lrMargin, tbMargin);

        mContentWrapper.setLayoutParams(wrapperParams);
        mContent.setCellCountAndPageSpacing(cellX, cellY,
                res.getDimensionPixelSize(R.dimen.paged_pad_view_page_spacing));
    }

    public void bind(String picUrl, final String source) {
        ShareUtils.getSharePicFile(getContext(), picUrl, file -> {
            boolean isImageShare = false;
            if (file != null && file.exists()) {
                isImageShare = true;
                mContent.setShareFile(file);
            }
            mContent.setSource(source);
            mContent.setVisibility(INVISIBLE);
            mContent.setVisibility(VISIBLE);
            mContent.bind(ShareUtils.getShareWays(isImageShare));
        });
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);

        // We need to handle touch events to prevent them from falling through to the settings activity below
        return true;
    }

    private class BackgroundTapListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (!isPointInContent((int) e.getX(), (int) e.getY())) {
                dismiss();
                return true;
            }
            return false;
        }
    }

    private boolean isPointInContent(int x, int y) {
        int[] l = new int[2];
        mContentWrapper.getLocationOnScreen(l);
        Rect rect = new Rect(l[0], l[1], l[0] + mContentWrapper.getWidth(), l[1] + mContentWrapper.getHeight());
        return rect.contains(x, y);
    }

    private void dismiss() {
        mContent.setCurrentPage(0);
    }
}
