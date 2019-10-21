package com.acb.libwallpaper.live.customize.adapter;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

 import com.honeycomb.colorphone.R;
import com.acb.libwallpaper.live.customize.CategoryItem;
import com.acb.libwallpaper.live.util.Utils;
import com.superapps.util.Fonts;

import java.util.ArrayList;
import java.util.List;


public class CategoryViewAdapter extends BaseAdapter {

    final private List<CategoryItem> mData = new ArrayList<>();
    private Context mContext;
    private LayoutInflater mInflater;
    private DisplayMetrics mMetrics;

    private boolean mIsTextAnimationEnabled = false;

    public CategoryViewAdapter(Context context, List<CategoryItem> data) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mMetrics = context.getResources().getDisplayMetrics();
        setCategoryItemData(data);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mData.get(position).getItemName().hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.online_wallpaper_category_item, parent, false);
            holder = new ViewHolder();
            holder.titleView = (TextView) convertView.findViewById(R.id.category_text_title);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        CategoryItem item = mData.get(position);
        holder.titleView.setText(item.getItemName());
        if (item.isSelected()) {
            holder.titleView.setTextColor(Color.BLACK);
            convertView.setBackgroundResource(R.drawable.online_wallpaper_category_btn_hl);
            holder.titleView.setTypeface(Fonts.getTypeface(Fonts.Font.CUSTOM_FONT_SEMIBOLD));
        } else {
            holder.titleView.setTextColor(Color.WHITE);
            holder.titleView.setTypeface(Fonts.getTypeface(Fonts.Font.CUSTOM_FONT_REGULAR));
            convertView.setBackgroundResource(R.drawable.online_wallpaper_category_btn_bg);
        }

        if (mIsTextAnimationEnabled) {
            holder.titleView.setAlpha(1.0f);
            holder.titleView.setTranslationY(Utils.pxFromDp(R.dimen.online_category_title_view_translationY, mMetrics));
            AnimatorSet content = (AnimatorSet) AnimatorInflater.loadAnimator(mContext,
                    R.animator.online_wallpaper_category_content_in);
            content.setTarget(holder.titleView);
            content.start();
        } else {
            ObjectAnimator transaction = ObjectAnimator.ofFloat(holder.titleView, "alpha", 1.0f, 0.0f);
            transaction.setDuration(100);
            transaction.start();
        }

        return convertView;
    }

    private void setCategoryItemData(List<CategoryItem> data) {
        mData.clear();
        mData.addAll(data);
        notifyDataSetChanged();
    }

    public void setTextAnimationEnabled(boolean isEnabled) {
        mIsTextAnimationEnabled = isEnabled;
    }

    private static class ViewHolder {
        TextView titleView;
    }
}
