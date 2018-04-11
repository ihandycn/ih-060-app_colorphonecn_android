package com.honeycomb.colorphone.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.honeycomb.colorphone.R;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

public class AdLoadingView extends InsettableFrameLayout {

    private Animation rotatingAnimation;
    private ImageView progressView;

    public AdLoadingView(Context context) {
        this(context, null);
    }

    public AdLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.ad_loading_view, this);
        rotatingAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.rotate);
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        findViewById(R.id.bg_view).setBackgroundDrawable(BackgroundDrawables.createBackgroundDrawable(0x80000000, Dimensions.pxFromDp(4), false));
        progressView = findViewById(R.id.dialog_loading_image_view);
        progressView.startAnimation(rotatingAnimation);
    }

    @Override protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        progressView.clearAnimation();
        rotatingAnimation = null;
    }
}

