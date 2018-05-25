package com.honeycomb.colorphone.gdpr;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.honeycomb.colorphone.R;


public final class DataUsageAlertDialog extends AlertDialog {
    private static final String TAG = "DataUsageAlertDialog";

    interface OnClickListener {
        void onClick();
    }

    private OnClickListener positiveListener;
    private OnClickListener negativeListener;


    public DataUsageAlertDialog(Context context) {
        super(context);
    }

    public void setPositiveClickListener(OnClickListener listener) {
        positiveListener = listener;
    }

    public void setNegativeClickListener(OnClickListener listener) {
        negativeListener = listener;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_data_usage_confirm_dialog);
        findViewById(R.id.tv_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (positiveListener != null) {
                    positiveListener.onClick();
                }
            }
        });

        findViewById(R.id.tv_turn_off).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (negativeListener != null) {
                    negativeListener.onClick();
                }
            }
        });
    }
}

