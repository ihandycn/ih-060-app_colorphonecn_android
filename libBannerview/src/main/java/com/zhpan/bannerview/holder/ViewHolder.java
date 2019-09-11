package com.zhpan.bannerview.holder;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by zhpan on 2017/10/30.
 * Description:
 */

public interface ViewHolder<T> {
    View createView(ViewGroup viewGroup,Context context, int position);
   // void onBind(Context context, int position, T data);
    /**
     * @param view return by createView()
     * @param data 实体类对象
     * @param position 当前位置
     * @param size 页面个数
     */
    void onBind(View view,T data,int position,int size);
}
