package colorphone.acb.com.libweather;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;


import com.ihs.commons.utils.HSLog;

import java.util.List;

import colorphone.acb.com.libweather.background.BaseWeatherAnimBackground;

public class WeatherAnimView extends View implements ViewPager.OnPageChangeListener {

    private ViewPager mCityPager;
    private SparseArray<List<BaseWeatherAnimBackground>> mAnims = new SparseArray<>(12);
    private int mCurPosition;
    private int mPosition1;
    private int mPosition2;
    private float mHorizontalAlpha;
    private float mVerticalAlpha = 1;
    private int mScrollState = ViewPager.SCROLL_STATE_IDLE;

    public WeatherAnimView(Context context) {
        this(context, null);
    }

    public WeatherAnimView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeatherAnimView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void bindView(ViewPager viewPager) {
        mCityPager = viewPager;
        mCityPager.addOnPageChangeListener(this);
        mCurPosition = viewPager.getCurrentItem();

        refresh();
    }

    public void release() {
        HSLog.d("AnimationLeak", "Release WeatherAnimView");
        for (int i = 0, size = mAnims.size(); i < size; i++) {
            List<BaseWeatherAnimBackground> pageAnims = mAnims.valueAt(i);
            if (pageAnims != null) {
                for (BaseWeatherAnimBackground anim : pageAnims) {
                    // Cancel all running animations to avoid memory leak
                    anim.reset();
                }
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mAnims.size() == 0) {
            return;
        }

        if (mScrollState == ViewPager.SCROLL_STATE_IDLE) {
            drawCurrentPage(canvas);
        } else {
            drawScrollPage(canvas);
        }
    }

    private void drawScrollPage(Canvas canvas) {
        List<BaseWeatherAnimBackground> strategyList1 = null;
        List<BaseWeatherAnimBackground> strategyList2 = null;
        if (mPosition1 < mAnims.size()) {
            strategyList1 = mAnims.get(mPosition1);
        }
        if (mPosition2 < mAnims.size()) {
            strategyList2 = mAnims.get(mPosition2);
        }
        if (strategyList1 != null) {
            for (BaseWeatherAnimBackground strategy : strategyList1) {
                float alpha;
                if (mPosition1 == mCurPosition && strategy.shouldFadeOutOnVerticalScroll()) {
                    alpha = mHorizontalAlpha * mVerticalAlpha;
                } else {
                    alpha = mHorizontalAlpha;
                }
                strategy.setAlpha(alpha);
                strategy.onDraw(canvas);
            }
        }

        if (strategyList2 != null) {
            for (BaseWeatherAnimBackground strategy : strategyList2) {
                float alpha;
                if (mPosition2 == mCurPosition && strategy.shouldFadeOutOnVerticalScroll()) {
                    alpha = (1 - mHorizontalAlpha) * mVerticalAlpha;
                } else {
                    alpha = 1 - mHorizontalAlpha;
                }
                strategy.setAlpha(alpha);
                strategy.onDraw(canvas);
            }
        }
    }

    private void drawCurrentPage(Canvas canvas) {
        if (mCurPosition < mAnims.size()) {
            List<BaseWeatherAnimBackground> strategyList = mAnims.get(mCurPosition);
            if (strategyList != null) {
                for (int i = 0, count = strategyList.size(); i < count; i++) {
                    BaseWeatherAnimBackground animation = strategyList.get(i);
                    if (animation.shouldFadeOutOnVerticalScroll()) {
                        animation.setAlpha(mVerticalAlpha);
                    }
                    animation.onDraw(canvas);
                }
            }
        }
    }

    public void setAnimationList(int pos, List<BaseWeatherAnimBackground> anims) {
        mAnims.put(pos, anims);
    }

    public void clearAnimationList() {
        release();
        mAnims.clear();
    }

    public void setVerticalAlpha(float alpha) {
        mVerticalAlpha = alpha;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mPosition1 = (int) (position + positionOffset + 1);
        mPosition2 = (int) (position + positionOffset);
        mHorizontalAlpha = positionOffset;
        invalidate();
    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        mScrollState = state;
        if (state == ViewPager.SCROLL_STATE_IDLE) {
            mVerticalAlpha = 1;
            if (mCurPosition != mCityPager.getCurrentItem()) {
                mCurPosition = mCityPager.getCurrentItem();
                showSelectedPage(mCurPosition);
            }
        }
    }

    public void refresh() {
        showSelectedPage(mCityPager.getCurrentItem());
    }

    private void showSelectedPage(int position) {
        if (mAnims.size() == 0 || position > mAnims.size()) {
            return;
        }
        if (position == mAnims.size()) {
            // last page is add city page, draw nothing
            invalidate();
        }
        if (position < mAnims.size()) {
            List<BaseWeatherAnimBackground> strategyList = mAnims.get(position);

            if (strategyList != null) {
                for (BaseWeatherAnimBackground strategy : strategyList) {
                    strategy.startBeginAnimation();
                }
            }
        }
        for (int i = 0, size = mAnims.size(); i < size; i++) {
            int pageIndex = mAnims.keyAt(i);
            if (pageIndex == position) {
                continue;
            }
            List<BaseWeatherAnimBackground> animSet = mAnims.get(pageIndex);
            for (BaseWeatherAnimBackground animation : animSet) {
                animation.reset();
            }
        }
    }
}
