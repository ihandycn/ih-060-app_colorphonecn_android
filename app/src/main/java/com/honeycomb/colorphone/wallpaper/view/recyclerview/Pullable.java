package com.honeycomb.colorphone.wallpaper.view.recyclerview;

/**
 * Imported from library https://github.com/lynnchurch/PullToRefresh with code formatting and implementation tweaks.
 *
 * License: https://github.com/lynnchurch/PullToRefresh/blob/master/LICENSE
 */
public interface Pullable {

    /**
     * 判断是否可以下拉，如果不需要下拉功能可以直接 return false
     *
     * @return true 如果可以下拉否则返回 false
     */
    boolean canPullDown();

    /**
     * 判断是否可以上拉，如果不需要上拉功能可以直接 return false
     *
     * @return true 如果可以上拉否则返回 false
     */
    boolean canPullUp();
}
