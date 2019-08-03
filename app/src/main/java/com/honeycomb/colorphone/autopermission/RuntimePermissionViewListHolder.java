package com.honeycomb.colorphone.autopermission;

import android.Manifest;
import android.support.annotation.StringDef;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.honeycomb.colorphone.R;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.RuntimePermissions;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.List;

public class RuntimePermissionViewListHolder {
    private static final String AUTO_PERMISSION_FAILED = "auto_permission_failed";

    @StringDef({Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.WRITE_EXTERNAL_STORAGE })

    @Retention(RetentionPolicy.SOURCE)
    @interface PERMISSION_TYPES {}

    private static final String TAG = "AutoPermission";

    private View container;

    private ViewGroup permissionLayout;

    private HashMap<String, RuntimePermissionItemHolder> permissionList = new HashMap<>();

    RuntimePermissionViewListHolder(View root, List<String> permissions) {
        container = root;

        permissionLayout = container.findViewById(R.id.permission_list);

        View item;
        RuntimePermissionItemHolder itemHolder;
        for (int i = 0; i < permissions.size(); i++) {
            item = LayoutInflater.from(container.getContext()).inflate(R.layout.runtime_premission_item, null, false);
            String pType = permissions.get(i);
            itemHolder = new RuntimePermissionItemHolder(item, pType);
            permissionList.put(pType, itemHolder);
            permissionLayout.addView(item);
        }
    }

    boolean refreshHolder(@PERMISSION_TYPES String pType) {
        RuntimePermissionItemHolder holder = permissionList.get(pType);
        if (holder != null) {
            return holder.checkGrantStatus();
        }
        return false;
    }

    boolean isAllGrant() {
        boolean isAllGrant = true;
        for (RuntimePermissionItemHolder holder : permissionList.values()) {
            isAllGrant &= holder.checkGrantStatus();
        }
        return isAllGrant;
    }

    static int getItemTitle(@PERMISSION_TYPES String type) {
        switch (type) {
            case Manifest.permission.READ_CONTACTS:
                return R.string.acb_phone_permission_read_contact;
            case Manifest.permission.WRITE_CONTACTS:
                return R.string.acb_phone_permission_write_contact;
            case Manifest.permission.READ_CALL_LOG:
                return R.string.start_guide_permission_call_log;
            default:
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                return R.string.acb_phone_permission_read_storage;
        }
    }

    static int getItemDrawable(@PERMISSION_TYPES String type) {
        switch (type) {
            case Manifest.permission.READ_CONTACTS:
                return R.drawable.acb_phone_permission_read_contact;
            case Manifest.permission.WRITE_CONTACTS:
                return R.drawable.acb_phone_permission_write_contact;
            case Manifest.permission.READ_CALL_LOG:
                return R.drawable.acb_phone_permission_read_call_log;
            default:
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                return R.drawable.acb_phone_permission_read_storage;
        }
    }

    static boolean getItemGrant(@PERMISSION_TYPES String type) {
        return RuntimePermissions.checkSelfPermission(HSApplication.getContext(), type) == RuntimePermissions.PERMISSION_GRANTED;
    }
}
