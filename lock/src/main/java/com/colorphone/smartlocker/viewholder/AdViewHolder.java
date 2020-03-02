package com.colorphone.smartlocker.viewholder;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.colorphone.lock.R;

import net.appcloudbox.ads.base.ContainerView.AcbNativeAdContainerView;
import net.appcloudbox.ads.base.ContainerView.AcbNativeAdIconView;
import net.appcloudbox.ads.base.ContainerView.AcbNativeAdPrimaryView;

public class AdViewHolder extends RecyclerView.ViewHolder {

    public AcbNativeAdContainerView acbNativeAdContainerView;
    public RelativeLayout adContainer;

    public AdViewHolder(Context context, View itemView) {
        super(itemView);

        adContainer = itemView.findViewById(R.id.ad_container);

        acbNativeAdContainerView = new AcbNativeAdContainerView(context);
        View contentView = LayoutInflater.from(context).inflate(R.layout.item_toutiao_ad, null);
        acbNativeAdContainerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        acbNativeAdContainerView.addContentView(contentView);

        acbNativeAdContainerView.setAdTitleView((TextView) contentView.findViewById(R.id.ad_title));
        acbNativeAdContainerView.setAdBodyView((TextView) contentView.findViewById(R.id.ad_body));
        acbNativeAdContainerView.setAdChoiceView((FrameLayout) contentView.findViewById(R.id.ad_choice));

        TextView button = contentView.findViewById(R.id.button);

        acbNativeAdContainerView.setAdActionView(button);

        AcbNativeAdPrimaryView acbNativeAdPrimaryView = contentView.findViewById(R.id.big_view);
        acbNativeAdPrimaryView.setBitmapConfig(Bitmap.Config.RGB_565);
        acbNativeAdPrimaryView.setImageViewScaleType(ImageView.ScaleType.FIT_XY);
        acbNativeAdContainerView.setAdPrimaryView(acbNativeAdPrimaryView);

        AcbNativeAdIconView acbNativeAdIconView = contentView.findViewById(R.id.small_icon);
        acbNativeAdIconView.setBitmapConfig(Bitmap.Config.RGB_565);
        acbNativeAdContainerView.setAdIconView(acbNativeAdIconView);

        adContainer.addView(acbNativeAdContainerView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

}
