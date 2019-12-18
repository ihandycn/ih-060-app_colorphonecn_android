package com.honeycomb.colorphone.autopermission;

import android.os.Build;
import android.support.annotation.IntDef;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.honeycomb.colorphone.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class RuntimePermissionItemHolder {
    private static final String TAG = RuntimePermissionItemHolder.class.getSimpleName();

    public static final int PERMISSION_STATUS_HIDE = -1;
    public static final int PERMISSION_STATUS_NOT_START = 0;
    public static final int PERMISSION_STATUS_FAILED = 2;
    public static final int PERMISSION_STATUS_OK = 4;

    @IntDef({PERMISSION_STATUS_HIDE,
            PERMISSION_STATUS_NOT_START,
            PERMISSION_STATUS_OK,
            PERMISSION_STATUS_FAILED
    })
    @Retention(RetentionPolicy.SOURCE)
    @interface PERMISSION_STATUS {
    }

    @RuntimePermissionViewListHolder.PERMISSION_TYPES
    String permissionType;

    TextView text;
    ImageView ok;

    RuntimePermissionItemHolder(View item, @RuntimePermissionViewListHolder.PERMISSION_TYPES String type) {
        permissionType = type;

        text = item.findViewById(R.id.runtime_permission_item_title);
        text.setText(RuntimePermissionViewListHolder.getItemTitle(type));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            text.setCompoundDrawablesRelativeWithIntrinsicBounds(RuntimePermissionViewListHolder.getItemDrawable(type), 0, 0, 0);
        } else {
            text.setCompoundDrawables(item.getContext().getResources().getDrawable(RuntimePermissionViewListHolder.getItemDrawable(type)), null, null, null);
        }

        ok = item.findViewById(R.id.runtime_permission_auto_start_ok);
        ok.setTag(type);

    }

    boolean checkGrantStatus() {
        boolean grant = RuntimePermissionViewListHolder.getItemGrant(permissionType);
        setStatus(grant ? PERMISSION_STATUS_OK : PERMISSION_STATUS_FAILED);
        return grant;
    }

    int getStatus() {
        return Integer.valueOf(ok.getTag().toString());
    }

    void setStatus(@PERMISSION_STATUS int status) {
        switch (status) {
            case PERMISSION_STATUS_FAILED:
                ok.setVisibility(View.VISIBLE);
                ok.setImageResource(R.drawable.runtime_confirm_alert_image);
                break;
            case PERMISSION_STATUS_NOT_START:
                ok.setVisibility(View.INVISIBLE);
                break;
            case PERMISSION_STATUS_OK:
                ok.setVisibility(View.VISIBLE);
                ok.setImageResource(R.drawable.runtime_confirm_ok_image);
                break;
            case PERMISSION_STATUS_HIDE:
                ok.setVisibility(View.INVISIBLE);
                break;
        }
        ok.setTag(status);
    }
}
