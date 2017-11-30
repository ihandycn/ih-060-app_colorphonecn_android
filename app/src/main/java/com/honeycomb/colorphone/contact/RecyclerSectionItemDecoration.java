package com.honeycomb.colorphone.contact;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.colorphone.lock.util.CommonUtils;
import com.colorphone.lock.util.FontUtils;
import com.honeycomb.colorphone.R;

import java.util.HashMap;


public class RecyclerSectionItemDecoration extends RecyclerView.ItemDecoration {

    private static final boolean DEBUG_SECTION = false;
    private final SectionCallback sectionCallback;
    private final Paint mSectionTextPaint;
    private final int mRowCount;
    private HashMap<String, PointF> mCachedSectionBounds = new HashMap<>();
    private Rect mTmpBounds = new Rect();

    private boolean mIsRtl = false;
    private float mSectionNamesMargin;

    public RecyclerSectionItemDecoration(Resources resources, int rowCount, @NonNull SectionCallback sectionCallback) {
        this.sectionCallback = sectionCallback;
        mRowCount = rowCount;
        mSectionNamesMargin = resources.getDimensionPixelSize(R.dimen.recycler_section_header_Margin);
        mSectionTextPaint = new Paint();
        mSectionTextPaint.setTextSize(resources.getDimensionPixelOffset(R.dimen.contact_section_txt));
        mSectionTextPaint.setColor(Color.parseColor("#ff818181"));
        mSectionTextPaint.setAntiAlias(true);
        mSectionTextPaint.setTypeface(FontUtils.getTypeface(FontUtils.Font.PROXIMA_NOVA_SEMIBOLD));
        mIsRtl = CommonUtils.isRtl();
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect,
                view,
                parent,
                state);
        // Nothing
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c,
                parent,
                state);

        if (DEBUG_SECTION) {
            Paint p = new Paint();
            p.setColor(0x33ff0000);
            c.drawRect(0, 0, mSectionNamesMargin,
                    parent.getMeasuredHeight(), p);
        }

        CharSequence previousHeader = "";
        int lastSectionTop = 0;
        int lastSectionHeight = 0;

        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            final int position = parent.getChildAdapterPosition(child);

            RecyclerView.ViewHolder holder = parent.getChildViewHolder(child);
            if (!isValidHolderAndChild(holder, child)) {
                continue;
            }

            String sectionName = (String) sectionCallback.getSectionHeader(position);
            if (!previousHeader.equals(sectionName) || sectionCallback.isSection(position)) {
                PointF sectionBounds = getAndCacheSectionBounds(sectionName);

                int viewTopOffset = (int) ((child.getHeight() - sectionBounds.y) * 0.5f);
                int sectionBaseline = (int) (viewTopOffset + sectionBounds.y);
                int x = mIsRtl ?
                        (int) (parent.getWidth() - mSectionNamesMargin) : 0;
                x += (int) ((mSectionNamesMargin - sectionBounds.x) / 2f);
                int y = child.getTop() + sectionBaseline;

                int nextPos = Math.min(mRowCount - 1, position + 1);
                String nextSectionName = (String) sectionCallback.getSectionHeader(nextPos);
                boolean fixedToRow = !sectionName.equals(nextSectionName);
                if (!fixedToRow) {
                    y = Math.max(sectionBaseline, y);
                }

                // In addition, if it overlaps with the last section that was drawn, then
                // offset it so that it does not overlap
                if (lastSectionHeight > 0 && y <= (lastSectionTop + lastSectionHeight)) {
                    y += lastSectionTop - y + lastSectionHeight;
                }

                lastSectionTop = y;
                lastSectionHeight = (int) (sectionBounds.y);
                c.drawText(sectionName, x, y, mSectionTextPaint);
                previousHeader = sectionName;
            }
        }
    }

    /**
     * Returns whether we consider this a valid view holder for us to draw a divider or section for.
     */
    private boolean isValidHolderAndChild(RecyclerView.ViewHolder holder, View child) {
        // Ensure item is not already removed
        RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams)
                child.getLayoutParams();
        if (lp.isItemRemoved()) {
            return false;
        }
        // Ensure we have a valid holder
        if (holder == null) {
            return false;
        }
        // Ensure we have a holder position
        int pos = holder.getPosition();
        if (pos < 0 || pos >= mRowCount) {
            return false;
        }
        return true;
    }

    /**
     * Given a section name, return the bounds of the given section name.
     */
    private PointF getAndCacheSectionBounds(String sectionName) {
        PointF bounds = mCachedSectionBounds.get(sectionName);
        if (bounds == null) {
            if (!sectionName.isEmpty()) {
                mSectionTextPaint.getTextBounds(sectionName, 0, sectionName.length(), mTmpBounds);
                bounds = new PointF(mSectionTextPaint.measureText(sectionName), mTmpBounds.height());
            }
            mCachedSectionBounds.put(sectionName, bounds);
        }
        return bounds;
    }

    public interface SectionCallback {

        boolean isSection(int position);

        CharSequence getSectionHeader(int position);
    }
}


