package com.honeycomb.colorphone.wallpaper.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.wallpaper.adapter.ViewPagerAdapter;
import com.superapps.util.Threads;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public abstract class AutoScrollCircleLayout<T> extends RelativeLayout {

    private static final boolean DEFAULT_AUTO_PLAY = false;
    private static final long DURATION = 3000;

    private ViewPager mViewPager;
    private ViewPagerAdapter mViewPagerAdapter;
    private AdvancedPageIndicator mIndicator;
    private boolean mAutoPlay;
    protected int mCount;
    private int mCurrentPosition;
    private boolean mDragged;
    private boolean mTaskRunning;
    private OnSelectItemListener mSelectListener;
    private OnSlideListener mSlideListener;

    private final Runnable mAutoScrollTask = new Runnable() {
        @Override
        public void run() {
            if (mTaskRunning && mCount > 1 && mAutoPlay) {
                mTaskRunning = false;
                int position = mCurrentPosition % (mCount + 1) + 1;
                setCurrentItemWithAnim(position);
                play();
            }
        }
    };

    public AutoScrollCircleLayout(Context context) {
        this(context, null);
    }

    public AutoScrollCircleLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoScrollCircleLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1) {
            setClipChildren(false);
        }
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.AnimAttar, defStyleAttr, 0);
        mAutoPlay = ta.getBoolean(R.styleable.AnimAttar_autoPlay, DEFAULT_AUTO_PLAY);
        ta.recycle();
        LayoutInflater.from(context).inflate(R.layout.auto_scroll_circle_layout, this);
        initView();
    }

    private void initView() {
        mViewPager = findViewById(R.id.view_pager);
        mViewPager.setOffscreenPageLimit(3);
        initViewPagerScroll();
        mIndicator = findViewById(R.id.indicator);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position > 0 && position < mCount) {
                    mIndicator.onScrolling(position - 1, positionOffset);
                }
            }

            @Override
            public void onPageSelected(int position) {
                if (mDragged && mSlideListener != null) {
                    if (mCurrentPosition < position) {
                        mSlideListener.slideLeft(position);
                    } else if (mCurrentPosition > position) {
                        mSlideListener.slideRight(position);
                    }
                }
                mCurrentPosition = position;
                resetIndicatorPosition();
                if (mSelectListener != null) {
                    mSelectListener.onSelect(calculateRealPosition());
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                switch (state) {
                    case ViewPager.SCROLL_STATE_IDLE:
                        resetPositionForCircleScroll();
                        couldAutoPlay();
                        break;
                    case ViewPager.SCROLL_STATE_DRAGGING:
                        mDragged = true;
                        removeTask();
                        resetPositionForCircleScroll();
                        break;
                    case ViewPager.SCROLL_STATE_SETTLING:
                        break;
                }
            }
        });
    }

    public void setIndicatorVisible(int visible) {
        mIndicator.setVisibility(visible);
    }

    public void setPageMargin(int margin) {
        mViewPager.setPageMargin(margin);
    }

    public void setPageTransformer(ViewPager.PageTransformer pageTransformer) {
        if (mViewPager != null) {
            mViewPager.setPageTransformer(true, pageTransformer);
        }
    }

    private void resetIndicatorPosition() {
        if (mCurrentPosition == 0) {
            mIndicator.setIndex(mCount - 1);
        }

        if (mCurrentPosition == (mCount + 1)) {
            mIndicator.setIndex(0);
        }
    }

    private int calculateRealPosition() {
        if (mCurrentPosition == 0) {
            return mCount - 1;
        } else if (mCurrentPosition == (mCount + 1)) {
            return 0;
        } else {
            return mCurrentPosition - 1;
        }
    }

    private void initViewPagerScroll() {
        try {
            Field mField = ViewPager.class.getDeclaredField("mScroller");
            mField.setAccessible(true);
            BannerScroller scroller = new BannerScroller(getContext());
            mField.set(mViewPager, scroller);
        } catch (NoSuchFieldException ignored) {

        } catch (IllegalAccessException ignored) {

        }
    }

    private void resetPositionForCircleScroll() {
        if (mCurrentPosition == mCount + 1) {
            setCurrentItem(1);
        } else if (mCurrentPosition == 0) {
            setCurrentItem(mCount);
        }
    }

    private void couldAutoPlay() {
        if (mDragged && mAutoPlay) {
            mDragged = false;
            play();
        }
    }

    protected void setAdapter(List<View> viewList) {
        if (mViewPagerAdapter == null) {
            mViewPagerAdapter = new ViewPagerAdapter(viewList);
            mViewPager.setAdapter(mViewPagerAdapter);
        } else {
            mViewPagerAdapter.update(viewList);
        }
        setCurrentItem(1);
    }

    private void setIndicatorMarkers() {
        mIndicator.setVisibility(VISIBLE);
        List<IndicatorMark.MarkerType> markerTypes = new ArrayList<>();
        for (int i = 0; i < mCount; i++) {
            markerTypes.add(IndicatorMark.MarkerType.CIRCLE);
        }
        mIndicator.addMarkers(markerTypes);
    }

    public void start() {
        if (mAutoPlay) {
            play();
        }
    }

    public void stop() {
        if (mAutoPlay) {
            removeTask();
        }
    }

    private void play() {
        if (!mTaskRunning) {
            mTaskRunning = true;
            Threads.postOnMainThreadDelayed(mAutoScrollTask, DURATION);
        }
    }

    private void removeTask() {
        if (mTaskRunning) {
            mTaskRunning = false;
            Threads.removeOnMainThread(mAutoScrollTask);
        }
    }

    protected void setCurrentItem(int position) {
        mViewPager.setCurrentItem(position, false);
    }

    private void setCurrentItemWithAnim(int position) {
        mViewPager.setCurrentItem(position, true);
    }

    protected List<T> handleData(List<T> data) {
        List<T> resultList = new ArrayList<>();
        if (data.size() > 1) {
            resultList.add(data.get(data.size() - 1));
            resultList.addAll(data);
            resultList.add(data.get(0));
        } else {
            resultList.addAll(data);
        }
        return resultList;
    }

    public void setData(List<T> data) {
        if (data.size() == 0) {
            return;
        }
        mCount = data.size();
        setIndicatorMarkers();
        List<T> resultList = handleData(data);
        List<View> viewList = getItemData(resultList);
        setAdapter(viewList);
        start();
    }

    public void resetViewPager() {
        if (mViewPager != null) {
            mViewPager.requestLayout();
        }
    }

    public void setOnSelectItemListener(OnSelectItemListener selectItemListener) {
        mSelectListener = selectItemListener;
    }

    public void setOnSlideListener(OnSlideListener slideListener) {
        mSlideListener = slideListener;
    }

    protected abstract List<View> getItemData(List<T> data);

    public interface OnSelectItemListener {
        void onSelect(int position);
    }

    public interface OnSlideListener {
        void slideRight(int position);

        void slideLeft(int position);
    }
}
