package com.acb.libwallpaper.live.update;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

 import com.honeycomb.colorphone.R;
import com.acb.libwallpaper.live.dialog.DefaultButtonDialog2;
import com.acb.libwallpaper.live.util.ViewUtils;
import com.ihs.commons.config.HSConfig;
import com.superapps.util.Dimensions;

import java.util.List;
import java.util.Map;

@SuppressLint("ViewConstructor")
public class ApkUpdateConfirmDialog extends DefaultButtonDialog2 {

    public ApkUpdateConfirmDialog(Context context) {
        super(context);

    }

    public static boolean show(Context context) {
        return new ApkUpdateConfirmDialog(context).show();
    }

    @Override
    protected View createContentView(LayoutInflater inflater, ViewGroup root) {
        View v = inflater.inflate(R.layout.apk_update_dialog_content, root, false);
        final TextView textView = ViewUtils.findViewById(v, R.id.hint_text);
        final TextView titleTextView = ViewUtils.findViewById(v, R.id.title_text);
        final ScrollView scrollView = ViewUtils.findViewById(v, R.id.update_scroll);
        titleTextView.setText(getContentTitle());
        textView.setText(getContentText());
        textView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.getLayoutParams().height = getFitHeight(scrollView.getMeasuredHeight(), Dimensions.getPhoneHeight(getContext()));
                scrollView.requestLayout();
            }
        });
        return v;
    }

    protected CharSequence getContentTitle() {
        return mActivity.getResources().getText(R.string.update_dialog_title);
    }

    @NonNull
    protected CharSequence getContentText() {
        List<Map<String,?>> descList = (List<Map<String, ?>>)HSConfig.getList("Application", "Update", "Description");
        StringBuilder sb = new StringBuilder();
        for (Map map : descList) {
            String str = UpdateUtils.getStringForCurrentLanguage(map);
            sb.append(str);
            sb.append("\n");
        }
        if (sb.length() > 0) {
            sb.delete(sb.length() - "\n".length(), sb.length());
        }
        return sb.toString();
    }

    private int getFitHeight(int measuredHeight, int phoneHeight) {
        return Math.min(measuredHeight, phoneHeight/3);
    }

    @Override
    protected int getPositiveButtonStringId() {
        return R.string.download_capital;
    }

    @Override
    protected int getNegativeButtonStringId() {
        return R.string.online_wallpaper_edit_cancel_btn;
    }

    @Override
    protected void onClickPositiveButton(View v) {
        UpdateUtils.doUpdate();
        super.onClickPositiveButton(v);
    }

    @Override
    protected void onClickNegativeButton(View v) {
        super.onClickNegativeButton(v);
    }

    @Override
    protected Drawable getTopImageDrawable() {
        if (mTopImageDrawable == null) {
            mTopImageDrawable =  ContextCompat.getDrawable(mActivity, R.drawable.update_dialog_top);
        }
        return mTopImageDrawable;
    }
}
