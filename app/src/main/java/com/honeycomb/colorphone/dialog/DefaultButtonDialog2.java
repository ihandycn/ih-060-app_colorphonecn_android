package com.honeycomb.colorphone.dialog;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.colorphone.lock.util.ViewUtils;
import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.util.ColorPhoneCrashlytics;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Dimensions;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public abstract class DefaultButtonDialog2 {

    private static final String TAG = DefaultButtonDialog2.class.getSimpleName();

    private static List<Dialog> sDialogList = new ArrayList<>();

    private LayoutInflater mLayoutInflater;
    protected View mRootView;
    protected ImageView mTopImage;

    private Dialog mAlertDialog;
    private TextView mTitleTv;
    private TextView mDescTv;

    private int mThemeId;
    protected Activity mActivity;

    private int mDesiredWidth;
    /**
     * Dialog has content view (msg body), if false, we should not show dialog.
     */
    private boolean mContentViewReady;

    private boolean mInited;

    public DefaultButtonDialog2(Context activity) {
        this(activity, 0);
    }

    public DefaultButtonDialog2(Context activity, int themeId) {
        mActivity = (Activity) activity;
        mThemeId = themeId;

    }

    protected void configDialog(AlertDialog.Builder builder) {
        builder.setView(mRootView);
        builder.setCancelable(true);
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                HSLog.d(TAG, "onDismiss");
                sDialogList.remove(mAlertDialog);
                onDismissComplete();
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                HSLog.d(TAG, "onCancel");
                DefaultButtonDialog2.this.onCanceled();
            }
        });
        mAlertDialog = builder.create();
        mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                HSLog.d(TAG, "OnShow");
                DefaultButtonDialog2.this.onShow();
            }
        });

        mAlertDialog.setCanceledOnTouchOutside(false);

    }

    private void initIfNeed() {
        if (!mInited) {
            mInited = true;
            init();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void init() {
        mLayoutInflater = LayoutInflater.from(mActivity);
        if (getTopImageDrawable() == null) {
            // No top image: double button style
            mRootView = mLayoutInflater.inflate(R.layout.dialog_compact_button_no_img, null);
        } else {
            // Has top image: big button style
            mRootView = mLayoutInflater.inflate(R.layout.dialog_compact_button, null);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, mThemeId != 0 ? mThemeId : R.style.TransparentCompatDialog);
        configDialog(builder);

        int dialogElevation = mActivity.getResources().getDimensionPixelSize(R.dimen.dialog_elevation);
        if (Utils.ATLEAST_LOLLIPOP && enableElevation()) {
            mRootView.setElevation(dialogElevation);
        }

        final ViewGroup contentView = ViewUtils.findViewById(mRootView, R.id.content_view);

        contentView.addView(createContentView(mLayoutInflater, contentView));

        mTopImage = ViewUtils.findViewById(mRootView, R.id.horizontal_top_image);

        initTopImage(mTopImage);
        initButtons();
        mContentViewReady = true;
    }

    private boolean hasTopImage() {
        if (mTopImage.getBackground() != null) {
            mDesiredWidth = mTopImage.getBackground().getIntrinsicWidth();
            return true;
        }

        if (mTopImage.getDrawable() != null) {
            mDesiredWidth = mTopImage.getDrawable().getIntrinsicWidth();
            return true;
        }

        return false;
    }

    protected void initTopImage(ImageView imageView) {
        mTopImage.setImageDrawable(getTopImageDrawable());
    }

    protected void initButtons() {
        Button okButton = ViewUtils.findViewById(mRootView, R.id.ok_btn);
        View cancelButton = ViewUtils.findViewById(mRootView, R.id.cancel_btn);

        if (null != okButton) {
            okButton.setText(getPositiveButtonString());
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickPositiveButton(v);
                }
            });
        }

        if (null != cancelButton) {
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickNegativeButton(v);
                    cancel();
                }
            });
        }
    }

    protected View createContentView(LayoutInflater inflater, ViewGroup root) {
        View v = inflater.inflate(R.layout.dialog_content_default, root, false);
        mTitleTv = ViewUtils.findViewById(v, R.id.dialog_title);
        mDescTv = ViewUtils.findViewById(v, R.id.dialog_desc);

        final CharSequence title = getDialogTitle();
        setDialogTitle(title);

        final CharSequence desc = getDialogDesc();
        setDialogDesc(desc);
        return v;
    }

    protected final void setDialogTitle(CharSequence title) {
        if (!TextUtils.isEmpty(title)) {
            mTitleTv.setVisibility(VISIBLE);
            mTitleTv.setText(title);
        } else {
            mTitleTv.setVisibility(GONE);
        }
    }

    protected final void setDialogDesc(CharSequence desc) {
        if (!TextUtils.isEmpty(desc)) {
            mDescTv.setVisibility(VISIBLE);
            mDescTv.setText(desc);
        } else {
            mDescTv.setVisibility(GONE);
        }
    }

    protected boolean enableElevation() {
        return true;
    }

    protected Drawable getTopImageDrawable() {
        return null;
    }

    protected CharSequence getDialogTitle() {
        return "";
    }

    protected CharSequence getDialogDesc() {
        return "";
    }

    protected String getPositiveButtonString() {
        return getResources().getString(getPositiveButtonStringId());
    }

    protected int getPositiveButtonStringId() {
        return android.R.string.ok;
    }

    // Not displayed on UI
    @Deprecated
    protected int getNegativeButtonStringId() {
        return R.string.cancel;
    }

    //-------- Dialog life circle callbacks (START)---------

    protected void onClickPositiveButton(View v) {
        dismiss();
    }

    protected void onClickNegativeButton(View v) {

    }


    protected void onShow() {

    }

    protected void onCanceled() {

    }

    protected void onDismissComplete() {
    }

    //-------- Dialog life circle callbacks (END)---------

    /**
     * Dialog width fit image width(If has).
     *
     * @return
     */
    protected boolean fitImageWidth() {
        return true;
    }

    protected final View findViewById(int id) {
        if (id == 0 || mRootView == null) {
            return null;
        }
        return mRootView.findViewById(id);
    }

    public final Resources getResources() {
        return getContext().getResources();
    }

    public final Context getContext() {
        return mActivity.getApplicationContext();
    }

    public final boolean show() {
        if (mActivity == null || mActivity.isFinishing()) {
            return false;
        }

        initIfNeed();

        if (!mContentViewReady) {
            return false;
        }

        if (hasTopImage()) {
            mTopImage.setVisibility(VISIBLE);
        } else {
            mTopImage.setVisibility(GONE);
        }

        try {
            mAlertDialog.show();
        } catch (Exception e) {
            ColorPhoneCrashlytics.getInstance().logException(e);
            return false;
        }
        sDialogList.add(mAlertDialog);

        if (mDesiredWidth > 0 && fitImageWidth()) {
            mTopImage.setScaleType(ImageView.ScaleType.CENTER_CROP);

            View rootView = mAlertDialog.getWindow().getDecorView();
            int windowPadding = rootView != null ? rootView.getPaddingLeft() : 0;
            int width = mDesiredWidth + 2 * windowPadding;
            // SM-G3518 or other device may has no padding, so we shrink content.
            float ratio = windowPadding > 0 ? 0.95f : 0.86f;
            int maxWidth = (int) (Dimensions.getPhoneWidth(mActivity) * ratio + 0.5f);
            mAlertDialog.getWindow().setLayout(Math.min(width, maxWidth), ViewGroup.LayoutParams.WRAP_CONTENT);
            HSLog.d(TAG, "rootView padding = " + windowPadding);
        }
        return true;
    }

    /**
     * Don not call this when <b>click CANCEL</b> button!
     */
    public final void dismiss() {
        mAlertDialog.dismiss();
    }

    /**
     * Finally call {@link #dismiss()}
     */
    public final void cancel() {
        mAlertDialog.cancel();
    }

    public static void closeNow() {
        dismissDialogs();
    }

    public static void dismissDialogs() {
        for (Dialog dialog : sDialogList) {
            try {
                dialog.dismiss();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        sDialogList.clear();
    }
}
