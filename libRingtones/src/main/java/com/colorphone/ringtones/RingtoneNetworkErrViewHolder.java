package com.colorphone.ringtones;

import android.graphics.Color;
import android.view.View;
import android.view.ViewStub;

import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;

public class RingtoneNetworkErrViewHolder {

    private View mRootView;
    private View frameView;
    private View retryButton;
    private View.OnClickListener retryListener;
    public RingtoneNetworkErrViewHolder(View rootView, View.OnClickListener retryListener) {
        mRootView = rootView;
        this.retryListener = retryListener;
    }

    public void show() {
        if (frameView == null) {
            ViewStub stub = mRootView.findViewById(R.id.stub_ringtone_network_err);
            if (stub != null) {
                stub.inflate();
            } else {
                throw new IllegalStateException("activity must include stub_ringtone_set_frame");
            }
            frameView = mRootView.findViewById(R.id.frame_no_network);
            retryButton = frameView.findViewById(R.id.no_network_action);
            retryButton.setBackground(BackgroundDrawables.createBackgroundDrawable(Color.parseColor("#ff696681"),
                    Dimensions.pxFromDp(22), true));
            retryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    retryListener.onClick(view);
                }
            });
        }
        frameView.setVisibility(View.VISIBLE);
    }

    public void hide() {
        if (frameView != null) {
            frameView.setVisibility(View.GONE);
        }
    }
}
