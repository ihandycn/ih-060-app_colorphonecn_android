package com.honeycomb.colorphone.customize.view;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.honeycomb.colorphone.ICustomizeService;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.customize.activity.CustomizeActivity;
import com.honeycomb.colorphone.customize.util.ServiceHolder;
import com.honeycomb.colorphone.customize.util.ServiceListener;

public class CustomizeContentView extends FrameLayout implements ServiceListener {

    private CustomizeContentAdapter mAdapter;

    public CustomizeContentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mAdapter = new CustomizeContentAdapter(this);
    }

    @Override
    public void onServiceConnected(ICustomizeService service) {
        mAdapter.onServiceConnected(service);
    }

    public void setChildSelected(int position) {
        removeAllViews();
        addView(mAdapter.getView(position));
    }

    private static class CustomizeContentAdapter implements ServiceListener {
        private CustomizeContentView mView;
        private Context mContext;
        private ServiceHolder mServiceHolder;
        private LayoutInflater mLayoutInflater;

        private View mCurrentView;

        private int[] CONTENT_VIEW_IDS = new int[]{
                R.layout.online_wallpaper_page,
//                R.layout.local_customize_page,
        };

        CustomizeContentAdapter(CustomizeContentView view) {
            mView = view;
            mContext = view.getContext();
            mServiceHolder = (ServiceHolder) view.getContext();
            mLayoutInflater = LayoutInflater.from(mContext);
        }

        @Override
        public void onServiceConnected(ICustomizeService service) {
            if (mCurrentView instanceof ServiceListener) {
                ((ServiceListener) mCurrentView).onServiceConnected(service);
            }
        }

        public int getCount() {
            return CONTENT_VIEW_IDS.length;
        }

        View getView(int position) {
            int layoutId = CONTENT_VIEW_IDS[position];
            View child = mLayoutInflater.inflate(layoutId, mView, false);
            setupWithInitialTabIndex(layoutId, child);
            ICustomizeService service = mServiceHolder.getService();
            if (child instanceof ServiceListener && service != null) {
                ((ServiceListener) child).onServiceConnected(service);
            }
            mCurrentView = child;

            return child;
        }

        private void setupWithInitialTabIndex(@LayoutRes int layoutId, View child) {
           if (layoutId == R.layout.online_wallpaper_page) {
                CustomizeActivity activity = (CustomizeActivity) mContext;
                int index = activity.mWallpaperTabIndex;
                if (index >= 0) {
                    OnlineWallpaperPage page = (OnlineWallpaperPage) child;
                    page.setup(index);
                    if (!TextUtils.isEmpty(activity.mWallpaperTabItemName)) {
                        page.loadWallpaper(index, activity.mWallpaperTabItemName);
                        activity.mWallpaperTabItemName = "";
                    }
                }
            }
        }
    }
}