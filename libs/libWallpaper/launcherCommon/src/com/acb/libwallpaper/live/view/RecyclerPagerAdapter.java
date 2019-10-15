package com.acb.libwallpaper.live.view;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.PriorityQueue;
import java.util.Queue;

/**
 * A {@link PagerAdapter} that put destroyed pages into a view pool for reuse. Pages recycled by this adapter must
 * implement {@link Comparable} interface as required by {@link Queue} implementation.
 *
 * Usage: override either {@link #instantiateItem(ViewGroup, int)},
 * or both {@link #onCreateItem(ViewGroup)} and {@link #onBindItem(T, int)}.
 */
public abstract class RecyclerPagerAdapter<T extends Comparable> extends PagerAdapter {

    protected Queue<T> mPagePool = new PriorityQueue<>(4);

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        T item;
        item = onCreateItem(container);
        onBindItem(item, position);
        container.addView((View) item);
        return item;
    }

    protected T onCreateItem(ViewGroup container) {
        return null;
    }

    protected void onBindItem(T item, int position) {
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (object instanceof Comparable) {
            //noinspection unchecked
            mPagePool.offer((T) object);
        }
        container.removeView((View) object);
    }
}
