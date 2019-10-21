package com.acb.libwallpaper.live.customize.adapter;

import android.content.Context;
import android.graphics.Outline;
import android.graphics.Rect;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewOutlineProvider;

import com.superapps.util.Dimensions;

public class HotOnlineWallpaperGalleryAdapter3 extends HotOnlineWallpaperGalleryAdapter4 {

    public HotOnlineWallpaperGalleryAdapter3(Context context) {
        super(context);
        mNormalMargin = Dimensions.pxFromDp(4f);
        mBigItemWidth = (int) ((mScreenWidth - Dimensions.pxFromDp(8) * 2 - mNormalMargin) * 0.6667f + 0.5f);
        mBigItemHeight = (int) (mBigItemWidth * 0.8981f + 0.5f);
        mSmallItemWidth = mScreenWidth - mBigItemWidth - mNormalMargin - Dimensions.pxFromDp(8) * 2;
        mSmallItemHeight = (mBigItemHeight - mNormalMargin) / 2;
    }

    @Override
    public RecyclerView.ItemDecoration getItemDecoration() {
        return new CustomItemDecoration();
    }

    private ViewOutlineProvider mOutlineProvider;

    @Override
    protected void updateItemLayout(HotOnlineViewHolder holder, int position) {
        super.updateItemLayout(holder, position);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.mV1.setClipToOutline(true);
            holder.mV2.setClipToOutline(true);
            holder.mV3.setClipToOutline(true);
            if (mOutlineProvider == null) {
                mOutlineProvider = new ViewOutlineProvider() {
                    @Override
                    public void getOutline(View view, Outline outline) {
                        outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), Dimensions.pxFromDp(8));
                    }
                };
            }
            holder.mV1.setOutlineProvider((ViewOutlineProvider) mOutlineProvider);
            holder.mV2.setOutlineProvider((ViewOutlineProvider) mOutlineProvider);
            holder.mV3.setOutlineProvider((ViewOutlineProvider) mOutlineProvider);
        } else {
            // do nothing
        }
    }

    private static class CustomItemDecoration extends RecyclerView.ItemDecoration {
        CustomItemDecoration() {
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.left = Dimensions.pxFromDp(8);
            outRect.top = 0;
            outRect.right = outRect.left;
            outRect.bottom = Dimensions.pxFromDp(4);
            int itemPosition = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewAdapterPosition();
            if (itemPosition == 0) {
                outRect.top = Dimensions.pxFromDp(8);
            }
        }
    }
}
