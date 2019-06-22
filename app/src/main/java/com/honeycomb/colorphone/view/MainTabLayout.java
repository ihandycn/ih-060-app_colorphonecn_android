package com.honeycomb.colorphone.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PointerIconCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.content.res.AppCompatResources;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static android.support.v4.view.ViewPager.SCROLL_STATE_DRAGGING;
import static android.support.v4.view.ViewPager.SCROLL_STATE_IDLE;
import static android.support.v4.view.ViewPager.SCROLL_STATE_SETTLING;

public class MainTabLayout extends LinearLayout {

    private final ArrayList<OnTabSelectedListener> mSelectedListeners = new ArrayList<>();
    private int mTabBackgroundResId;
    private int mSelectedTabPosition = -1;

    public MainTabLayout(Context context) {
        super(context);
        init();
    }

    public MainTabLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MainTabLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOrientation(HORIZONTAL);
    }

    public void setTabBackgroundResId(int tabBackgroundResId) {
        mTabBackgroundResId = tabBackgroundResId;
    }

    public void addTab(View view) {
        addTab(view, getTabCount() == 0);
    }

    public void addTab(View view, boolean isSelected) {
        TabView tabView = new TabView(getContext());
        tabView.addView(view);
        tabView.setPosition(getTabCount());
        addView(tabView, createLayoutParamsForTabs());

        if (isSelected) {
            selectTab(tabView.getPosition(), true);
        }
    }
    /**
     * Returns the number of tabs currently registered with the action bar.
     *
     * @return Tab count
     */
    public int getTabCount() {
        return getChildCount();
    }

    /**
     * Returns the tab at the specified index.
     */
    @Nullable
    public View getTabAt(int index) {
        return (index < 0 || index >= getTabCount()) ? null : getChildAt(index);
    }

    /**
     * Returns the position of the current selected tab.
     *
     * @return selected tab position, or {@code -1} if there isn't a selected tab.
     */
    public int getSelectedTabPosition() {
        return mSelectedTabPosition;
    }

    private LinearLayout.LayoutParams createLayoutParamsForTabs() {
        final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.MATCH_PARENT);
        lp.width = 0;
        lp.weight = 1;
        return lp;
    }

    public void setCurrentTab(int selectedTabPosition) {
        selectTab(selectedTabPosition, true);
    }

    private void selectTab(int position, boolean updateIndicator) {
        int curPos = mSelectedTabPosition;
        if (curPos == position) {
            if (curPos >= 0) {
                dispatchTabReselected(curPos);
            }
        } else {
            setSelectedTabView(position);

            if (curPos >= 0) {
                dispatchTabUnselected(curPos);
            }

            mSelectedTabPosition = position;
            dispatchTabSelected(position);
        }
    }

    private void setSelectedTabView(int position) {
        final int tabCount = getChildCount();
        if (position < tabCount) {
            for (int i = 0; i < tabCount; i++) {
                final View child = getChildAt(i);
                child.setSelected(i == position);
            }
        }
    }

    private void dispatchTabSelected(@NonNull final int pos) {
        for (int i = mSelectedListeners.size() - 1; i >= 0; i--) {
            mSelectedListeners.get(i).onTabSelected(pos);
        }
    }

    private void dispatchTabUnselected(@NonNull final int pos) {
        for (int i = mSelectedListeners.size() - 1; i >= 0; i--) {
            mSelectedListeners.get(i).onTabUnselected(pos);
        }
    }

    private void dispatchTabReselected(@NonNull final int pos) {
        for (int i = mSelectedListeners.size() - 1; i >= 0; i--) {
            mSelectedListeners.get(i).onTabReselected(pos);
        }
    }

    /**
     * Add a {@link TabLayout.OnTabSelectedListener} that will be invoked when tab selection
     * changes.
     *
     * <p>Components that add a listener should take care to remove it when finished via
     * {@link #removeOnTabSelectedListener(OnTabSelectedListener)}.</p>
     *
     * @param listener listener to add
     */
    public void addOnTabSelectedListener(@NonNull OnTabSelectedListener listener) {
        if (!mSelectedListeners.contains(listener)) {
            mSelectedListeners.add(listener);
        }
    }

    /**
     * Remove the given {@link TabLayout.OnTabSelectedListener} that was previously added via
     * {@link #addOnTabSelectedListener(OnTabSelectedListener)}.
     *
     * @param listener listener to remove
     */
    public void removeOnTabSelectedListener(@NonNull OnTabSelectedListener listener) {
        mSelectedListeners.remove(listener);
    }

    /**
     * Remove all previously added {@link TabLayout.OnTabSelectedListener}s.
     */
    public void clearOnTabSelectedListeners() {
        mSelectedListeners.clear();
    }

    /**
     * Callback interface invoked when a tab's selection state changes.
     */
    public interface OnTabSelectedListener {

        /**
         * Called when a tab enters the selected state.
         *
         * @param pos The tab position that was selected
         */
        public void onTabSelected(int pos);

        /**
         * Called when a tab exits the selected state.
         *
         * @param pos The tab position that was unselected
         */
        public void onTabUnselected(int pos);

        /**
         * Called when a tab that is already selected is chosen again by the user. Some applications
         * may use this action to return to the top level of a category.
         *
         * @param pos The tab position that was reselected.
         */
        public void onTabReselected(int pos);
    }


    /**
     * A {@link ViewPager.OnPageChangeListener} class which contains the
     * necessary calls back to the provided {@link TabLayout} so that the tab position is
     * kept in sync.
     *
     * <p>This class stores the provided TabLayout weakly, meaning that you can use
     * {@link ViewPager#addOnPageChangeListener(ViewPager.OnPageChangeListener)
     * addOnPageChangeListener(OnPageChangeListener)} without removing the listener and
     * not cause a leak.
     */
    public static class TabLayoutOnPageChangeListener implements ViewPager.OnPageChangeListener {
        private final WeakReference<MainTabLayout> mTabLayoutRef;
        private int mPreviousScrollState;
        private int mScrollState;

        public TabLayoutOnPageChangeListener(MainTabLayout tabLayout) {
            mTabLayoutRef = new WeakReference<>(tabLayout);
        }

        @Override
        public void onPageScrollStateChanged(final int state) {
            mPreviousScrollState = mScrollState;
            mScrollState = state;
        }

        @Override
        public void onPageScrolled(final int position, final float positionOffset,
                                   final int positionOffsetPixels) {
            final MainTabLayout tabLayout = mTabLayoutRef.get();
            if (tabLayout != null) {
                // Only update the text selection if we're not settling, or we are settling after
                // being dragged
                final boolean updateText = mScrollState != SCROLL_STATE_SETTLING ||
                        mPreviousScrollState == SCROLL_STATE_DRAGGING;
                // Update the indicator if we're not settling after being idle. This is caused
                // from a setCurrentItem() call and will be handled by an animation from
                // onPageSelected() instead.
                final boolean updateIndicator = !(mScrollState == SCROLL_STATE_SETTLING
                        && mPreviousScrollState == SCROLL_STATE_IDLE);
//                tabLayout.setScrollPosition(position, positionOffset, updateText, updateIndicator);
            }
        }

        @Override
        public void onPageSelected(final int position) {
            final MainTabLayout tabLayout = mTabLayoutRef.get();
            if (tabLayout != null && tabLayout.getSelectedTabPosition() != position
                    && position < tabLayout.getTabCount()) {
                // Select the tab, only updating the indicator if we're not being dragged/settled
                // (since onPageScrolled will handle that).
                final boolean updateIndicator = mScrollState == SCROLL_STATE_IDLE
                        || (mScrollState == SCROLL_STATE_SETTLING
                        && mPreviousScrollState == SCROLL_STATE_IDLE);
                tabLayout.selectTab(position, updateIndicator);
            }
        }

        void reset() {
            mPreviousScrollState = mScrollState = SCROLL_STATE_IDLE;
        }
    }

    /**
     * A {@link TabLayout.OnTabSelectedListener} class which contains the necessary calls back
     * to the provided {@link ViewPager} so that the tab position is kept in sync.
     */
    public static class ViewPagerOnTabSelectedListener implements TabLayout.OnTabSelectedListener {
        private final ViewPager mViewPager;

        public ViewPagerOnTabSelectedListener(ViewPager viewPager) {
            mViewPager = viewPager;
        }

        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            mViewPager.setCurrentItem(tab.getPosition());
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
            // No-op
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {
            // No-op
        }
    }


    class TabView extends LinearLayout {
        private int position;

        public TabView(@NonNull Context context) {
            super(context);
            if (mTabBackgroundResId != 0) {
                ViewCompat.setBackground(
                        this, AppCompatResources.getDrawable(context, mTabBackgroundResId));
            }
            setGravity(Gravity.CENTER);
            setClickable(true);
            ViewCompat.setPointerIcon(this,
                    PointerIconCompat.getSystemIcon(getContext(), PointerIconCompat.TYPE_HAND));
        }

        @Override
        public boolean performClick() {
            selectTab(position, true);
            return true;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public int getPosition() {
            return position;
        }
    }
}
