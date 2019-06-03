package colorphone.acb.com.libweather.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;

import colorphone.acb.com.libweather.R;

public class InsettableFrameLayout extends FrameLayout implements
        ViewGroup.OnHierarchyChangeListener, Insettable, INotificationObserver {

    public static final String NOTIFICATION_INSET_LOCK_SCREEN = "inset_lock_screen";
    public static final String NOTIFICATION_INSET_UNLOCK_SCREEN = "inset_unlock_screen";

    private boolean mScreenLocked = false;
    protected Rect mInsets = new Rect();

    public Rect getInsets() {
        return mInsets;
    }

    public InsettableFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnHierarchyChangeListener(this);
    }

    public void setFrameLayoutChildInsets(View child, Rect newInsets, Rect oldInsets) {
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();

        if (child instanceof Insettable) {
            ((Insettable) child).setInsets(newInsets);
        } else if (lp.mInsetWay == LayoutParams.InsetWay.MARGIN) {
            lp.topMargin += (newInsets.top - oldInsets.top);
            lp.leftMargin += (newInsets.left - oldInsets.left);
            lp.rightMargin += (newInsets.right - oldInsets.right);
            lp.bottomMargin += (newInsets.bottom - oldInsets.bottom);
        } else if (lp.mInsetWay == LayoutParams.InsetWay.PADDING) {
            child.setPadding(child.getPaddingLeft(),
                    child.getPaddingTop() + (newInsets.top - oldInsets.top),
                    child.getPaddingRight(),
                    child.getPaddingBottom() + (newInsets.bottom - oldInsets.bottom));
        } else if (lp.mInsetWay == LayoutParams.InsetWay.NONE) {
            //nothing
        } else {
            //nothing
        }
    }

    @Override
    public void setInsets(Rect insets) {
        if (mScreenLocked) {
            return;
        }
        final int n = getChildCount();
        for (int i = 0; i < n; i++) {
            final View child = getChildAt(i);
            setFrameLayoutChildInsets(child, insets, mInsets);
        }
        mInsets.set(insets);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new InsettableFrameLayout.LayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    // Override to allow type-checking of LayoutParams.
    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof InsettableFrameLayout.LayoutParams;
    }

    @Override
    public LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    public static class LayoutParams extends FrameLayout.LayoutParams {

        private static final int DEFAULT_WAY = 2;

        public enum InsetWay {
            NONE,
            MARGIN,
            PADDING
        }

        public InsetWay mInsetWay = InsetWay.MARGIN;

        @SuppressLint("CustomViewStyleable")
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.InsetAttr);
            int value = a.getInt(R.styleable.InsetAttr_layout_insetWay, DEFAULT_WAY);
            a.recycle();
            mInsetWay = getInsetWay(value);
        }

        private InsetWay getInsetWay(int value) {
            switch (value) {
                case 0:
                    return InsetWay.NONE;
                case 1:
                    return InsetWay.PADDING;
                case 2:
                default:
                    return InsetWay.MARGIN;
            }
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams lp) {
            super(lp);
        }
    }

    @Override
    public void onChildViewAdded(View parent, View child) {
        setFrameLayoutChildInsets(child, mInsets, new Rect());
    }

    @Override
    public void onChildViewRemoved(View parent, View child) {
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        HSGlobalNotificationCenter.addObserver(NOTIFICATION_INSET_LOCK_SCREEN, this);
        HSGlobalNotificationCenter.addObserver(NOTIFICATION_INSET_UNLOCK_SCREEN, this);
    }

    @Override
    protected void onDetachedFromWindow() {
        HSGlobalNotificationCenter.removeObserver(this);
        super.onDetachedFromWindow();
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        switch (s) {
            case NOTIFICATION_INSET_LOCK_SCREEN:
                mScreenLocked = true;
                break;
            case NOTIFICATION_INSET_UNLOCK_SCREEN:
                mScreenLocked = false;
                break;
            default:
                break;
        }
    }
}
