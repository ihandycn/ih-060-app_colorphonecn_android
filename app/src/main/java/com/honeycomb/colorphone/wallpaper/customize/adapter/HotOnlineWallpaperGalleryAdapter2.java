package com.honeycomb.colorphone.wallpaper.customize.adapter;

import android.content.Context;
import android.graphics.Outline;
import android.graphics.Rect;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewOutlineProvider;

import com.superapps.util.Dimensions;

public class HotOnlineWallpaperGalleryAdapter2 extends OnlineWallpaperGalleryAdapter {

    private ViewOutlineProvider mOutlineProvider;

    public HotOnlineWallpaperGalleryAdapter2(Context context) {
        super(context);
    }

    @Override
    public RecyclerView.ItemDecoration getItemDecoration() {
        return new CustomItemDecoration();
    }

    @Override
    protected void clipHotOnlineItemView(RecyclerView.ViewHolder holder, int position) {
        super.clipHotOnlineItemView(holder, position);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.itemView.setClipToOutline(true);
            if (mOutlineProvider == null) {
                mOutlineProvider = new ViewOutlineProvider() {
                    @Override
                    public void getOutline(View view, Outline outline) {
                        outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), Dimensions.pxFromDp(8));
                    }
                };
            }
            holder.itemView.setOutlineProvider((ViewOutlineProvider) mOutlineProvider);
        } else {
            // do nothing.
        }
    }

    private static class CustomItemDecoration extends RecyclerView.ItemDecoration {
        CustomItemDecoration() {
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int itemPosition = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewAdapterPosition();
            if (itemPosition == 0 || itemPosition == 1) {
                outRect.top = Dimensions.pxFromDp(8);
            } else {
                outRect.top = 0;
            }

            outRect.bottom = Dimensions.pxFromDp(8);
            if (itemPosition % 2 == 0) {
                outRect.left = Dimensions.pxFromDp(8);
                outRect.right = Dimensions.pxFromDp(4);
            } else {
                outRect.right = Dimensions.pxFromDp(8);
                outRect.left = Dimensions.pxFromDp(4);
            }
        }
    }
}
