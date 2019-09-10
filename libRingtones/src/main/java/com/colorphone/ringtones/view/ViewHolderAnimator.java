package com.colorphone.ringtones.view;
 
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
 
public class ViewHolderAnimator {
 
    public static class ViewHolderAnimatorListener extends AnimatorListenerAdapter {
        private final RecyclerView.ViewHolder mHolder; //holder对象
 
        //设定在动画开始结束和取消状态下是否可以被回收
        public ViewHolderAnimatorListener(RecyclerView.ViewHolder holder) {
            mHolder = holder;
        }
 
        @Override
        public void onAnimationStart(Animator animation) { //开始时
            mHolder.setIsRecyclable(false);
        }
 
        @Override
        public void onAnimationEnd(Animator animation) { //结束时
            mHolder.setIsRecyclable(true);
        }
 
        @Override
        public void onAnimationCancel(Animator animation) { //取消时
            mHolder.setIsRecyclable(true);
        }
    }
 
    public static class LayoutParamsAnimatorListener extends AnimatorListenerAdapter {
        private final View mView;
        private final int mParamsWidth;
        private final int mParamsHeight;
 
        public LayoutParamsAnimatorListener(View view, int paramsWidth, int paramsHeight) {
            mView = view;
            mParamsWidth = paramsWidth;
            mParamsHeight = paramsHeight;
        }
 
        @Override
        public void onAnimationEnd(Animator animation) {
            final ViewGroup.LayoutParams params = mView.getLayoutParams();
            params.width = mParamsWidth;
            params.height = mParamsHeight;
            mView.setLayoutParams(params);
        }
    }
 
    public static Animator ofItemViewHeight(RecyclerView.ViewHolder holder, int[] itemForcedHeight, boolean close) {
        View parent = (View) holder.itemView.getParent();
        if (parent == null) {
            throw new IllegalStateException("Cannot animate the layout of a view that has no parent");
        }

        //测量扩展动画的起始高度和结束高度
        int start, end;
        boolean forceHeightSize = itemForcedHeight != null && itemForcedHeight.length == 2;

        if (forceHeightSize) {
            start = itemForcedHeight[close ? 1 : 0];
            end = itemForcedHeight[close ? 0 : 1];
        } else {
            start = holder.itemView.getMeasuredHeight();
            holder.itemView.measure(View.MeasureSpec.makeMeasureSpec(parent.getMeasuredWidth(),
                    View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            end = holder.itemView.getMeasuredHeight();
        }

        final Animator animator = LayoutAnimator.ofHeight(holder.itemView, start, end);
        //具体的展开动画
        //设定该Item在动画开始结束和取消时能否被recycle
        animator.addListener(new ViewHolderAnimatorListener(holder));
        //设定结束时这个Item的宽高
        animator.addListener(new LayoutParamsAnimatorListener(holder.itemView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                forceHeightSize ? end : ViewGroup.LayoutParams.WRAP_CONTENT));
        return animator;
    }
 
}
