package com.colorphone.ringtones.view;
 
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

public class ExpandableViewHoldersUtil {

    /**
     *     1、holder对象 2、展开部分的View，由holder.getExpandView()方法获取 3、animate参数为true，则有动画效果
     */
    public static void openHolder(final RecyclerView.ViewHolder holder, final Expandable expandTarget, final boolean animate) {
        final View expandView = expandTarget.getExpandView();
        int[] forcedHeight = expandTarget.getItemForcedHeight();
        if (animate) {
            expandView.setVisibility(View.VISIBLE);
            //改变高度的动画
            final Animator animator = ViewHolderAnimator.ofItemViewHeight(holder, forcedHeight, false);
            //扩展的动画，结束后透明度动画开始
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    final ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(expandView, View.ALPHA, 1);
                    alphaAnimator.addListener(new ViewHolderAnimator.ViewHolderAnimatorListener(holder));
                    alphaAnimator.start();
                }
            });
            animator.start();
        } else { //为false时直接显示
            expandView.setVisibility(View.VISIBLE);
            expandView.setAlpha(1);
            if (forcedHeight != null && forcedHeight.length == 2) {
                ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
                layoutParams.height = forcedHeight[1];
                holder.itemView.setLayoutParams(layoutParams);
            }
        }
    }
 
    //
    public static void closeHolder(final RecyclerView.ViewHolder holder, final Expandable expandTarget, final boolean animate) {
        final View expandView = expandTarget.getExpandView();
        int[] forcedHeight = expandTarget.getItemForcedHeight();
        if (animate) {
            expandView.setVisibility(View.GONE);
            final Animator animator = ViewHolderAnimator.ofItemViewHeight(holder, forcedHeight, true);
            expandView.setVisibility(View.VISIBLE);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    expandView.setVisibility(View.GONE);
                    expandView.setAlpha(0);
                }
 
                @Override
                public void onAnimationCancel(Animator animation) {
                    expandView.setVisibility(View.GONE);
                    expandView.setAlpha(0);
                }
            });
            animator.start();
        } else {
            expandView.setVisibility(View.GONE);
            expandView.setAlpha(0);
            if (forcedHeight != null && forcedHeight.length == 2) {
                ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
                layoutParams.height = forcedHeight[0];
                holder.itemView.setLayoutParams(layoutParams);
            }
        }
    }
 
    public interface Expandable {
        View getExpandView();
        int[] getItemForcedHeight();
    }
 
    @SuppressWarnings("deprecation")
    public static class KeepOneHolder<VH extends RecyclerView.ViewHolder & Expandable> {
        //-1表示所有item是关闭状态，opend为pos值的表示pos位置的item为展开的状态
        private int opened = -1;
 
        /**
         * 此方法是在Adapter的onBindViewHolder()方法中调用
         *
         * @param holder holder对象
         * @param pos    下标
         */
        public void bind(VH holder, int pos) {
            if (pos == opened) {
                ExpandableViewHoldersUtil.openHolder(holder, holder, false);
            } else {
                ExpandableViewHoldersUtil.closeHolder(holder, holder, false);
            }
        }

        public void reset() {
            opened = -1;
        }

        public int getExpandedPos() {
            return opened;
        }

        public boolean isExpanded(VH holder) {
            return opened == holder.getAdapterPosition();
        }

        @SuppressWarnings("unchecked")
        public void toggle(VH holder) {
            if (opened == holder.getAdapterPosition()) {
                //点击的就是打开的Item，则关闭item，并将opend置为-1
                opened = -1;
                ExpandableViewHoldersUtil.closeHolder(holder, holder, true);
            } else {
                int previous = opened;
                opened = holder.getAdapterPosition();
                ExpandableViewHoldersUtil.openHolder(holder, holder, true);
                //动画关闭之前打开的Item
                final VH oldHolder = (VH) ((RecyclerView) holder.itemView.getParent()).findViewHolderForAdapterPosition(previous);
                if (oldHolder != null) {
                    ExpandableViewHoldersUtil.closeHolder(oldHolder, oldHolder, true);
                }
            }
        }
    }
 
}
