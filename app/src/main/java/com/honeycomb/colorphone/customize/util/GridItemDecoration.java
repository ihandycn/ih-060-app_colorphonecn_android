package com.honeycomb.colorphone.customize.util;

import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import com.honeycomb.colorphone.customize.adapter.OnlineWallpaperGalleryAdapter;
import com.superapps.util.Dimensions;

import java.util.List;

public class GridItemDecoration extends RecyclerView.ItemDecoration {

    private int mSizeGridSpacingPx;

    private boolean mNeedLeftSpacing = false;

    private OnlineWallpaperGalleryAdapter mAdapter;

    public GridItemDecoration(int gridSpacingPx) {
        mSizeGridSpacingPx = gridSpacingPx;
    }

    public void setAdapter(OnlineWallpaperGalleryAdapter adapter) {
        mAdapter = adapter;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

        Object tag = view.getTag();
        if (tag != null && tag instanceof String && tag.equals(OnlineWallpaperGalleryAdapter.AD_TAG)) {
            outRect.top = Dimensions.pxFromDp(8);
            outRect.bottom = Dimensions.pxFromDp(8);
            return;
        }
        outRect.bottom = mSizeGridSpacingPx;

        int mGridSize = getSpanCount(parent);
        int frameWidth = (int) ((parent.getWidth() - (float) mSizeGridSpacingPx * (mGridSize - 1)) / mGridSize);
        int padding = parent.getWidth() / mGridSize - frameWidth;
        int itemPosition = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewAdapterPosition();
        if (mAdapter != null) {
            List<Integer> adCount = mAdapter.getAdCount();
            if (adCount.size() > 0 && adCount.size() > itemPosition) {
                if (itemPosition >= 1 && adCount.get(itemPosition) > adCount.get(itemPosition - 1)) {
                    // this is a ad, just return
                    return;
                }
                itemPosition = itemPosition - adCount.get(itemPosition);
            }
        }
        if (itemPosition == -1) {
            return;
        }
        if (itemPosition % mGridSize == 0) {
            outRect.left = 0;
            outRect.right = padding;
            mNeedLeftSpacing = true;
        } else if ((itemPosition + 1) % mGridSize == 0) {
            mNeedLeftSpacing = false;
            outRect.right = 0;
            outRect.left = padding;
        } else if (mNeedLeftSpacing) {
            mNeedLeftSpacing = false;
            outRect.left = mSizeGridSpacingPx - padding;
            if ((itemPosition + 2) % mGridSize == 0) {
                outRect.right = mSizeGridSpacingPx - padding;
            } else {
                outRect.right = mSizeGridSpacingPx / 2;
            }
        } else if ((itemPosition + 2) % mGridSize == 0) {
            mNeedLeftSpacing = false;
            outRect.left = mSizeGridSpacingPx / 2;
            outRect.right = mSizeGridSpacingPx - padding;
        } else {
            mNeedLeftSpacing = false;
            outRect.left = mSizeGridSpacingPx / 2;
            outRect.right = mSizeGridSpacingPx / 2;
        }
    }

    /**
     * Get column count of the grid.
     */
    private int getSpanCount(RecyclerView parent) {
        int spanCount = -1;
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            spanCount = ((GridLayoutManager) layoutManager).getSpanCount();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            spanCount = ((StaggeredGridLayoutManager) layoutManager).getSpanCount();
        }
        return spanCount;
    }
}
