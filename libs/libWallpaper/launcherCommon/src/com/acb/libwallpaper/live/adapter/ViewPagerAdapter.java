package com.acb.libwallpaper.live.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class ViewPagerAdapter extends PagerAdapter {

    private List<View> mItemViewList;

    public ViewPagerAdapter(List<View> itemViewList) {
        this.mItemViewList = itemViewList;
    }

    @Override
    public int getCount() {
        return mItemViewList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = mItemViewList.get(position);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(mItemViewList.get(position));
    }

    public void update(List<View> viewList) {
        mItemViewList.clear();
        mItemViewList.addAll(viewList);
        notifyDataSetChanged();
    }
}
