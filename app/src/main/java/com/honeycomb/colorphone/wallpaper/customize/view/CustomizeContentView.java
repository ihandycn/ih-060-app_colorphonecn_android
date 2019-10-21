package com.honeycomb.colorphone.wallpaper.customize.view;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.honeycomb.colorphone.R;

public class CustomizeContentView extends FrameLayout {

    private CustomizeContentAdapter mAdapter;

    public CustomizeContentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mAdapter = new CustomizeContentAdapter(this);
    }

    public MineCustomizePage getLocalCustomizePage() {
        return mAdapter.getLocalCustomizePage();
    }

    public void setChildSelected(int position) {
        removeAllViews();
        addView(mAdapter.getView(position));
    }

    private static class CustomizeContentAdapter {
        private CustomizeContentView mView;
        private Context mContext;
        private LayoutInflater mLayoutInflater;

        private View mCurrentView;

        private int[] CONTENT_VIEW_IDS = new int[]{
                R.layout.online_wallpaper_page
        };

        CustomizeContentAdapter(CustomizeContentView view) {
            mView = view;
            mContext = view.getContext();
            mLayoutInflater = LayoutInflater.from(mContext);
        }

        public MineCustomizePage getLocalCustomizePage() {
            if (mCurrentView instanceof MineCustomizePage) {
                return (MineCustomizePage) mCurrentView;
            }
            return null;
        }


        public int getCount() {
            return CONTENT_VIEW_IDS.length;
        }

        View getView(int position) {
            int layoutId = CONTENT_VIEW_IDS[position];
            View child = mLayoutInflater.inflate(layoutId, mView, false);
            setupWithInitialTabIndex(layoutId, child);
            mCurrentView = child;

            return child;
        }

        private void setupWithInitialTabIndex(@LayoutRes int layoutId, View child) {
            if (layoutId == R.layout.online_wallpaper_page) {
                int index = 0;
                if (index >= 0) {
                    OnlineWallpaperPage page = (OnlineWallpaperPage) child;
                    page.setup(index);
                    String mWallpaperTabItemName = null;
                    if (!TextUtils.isEmpty(mWallpaperTabItemName)) {
                        page.loadWallpaper(index, mWallpaperTabItemName);
                    }
                }
            }
        }
    }
}