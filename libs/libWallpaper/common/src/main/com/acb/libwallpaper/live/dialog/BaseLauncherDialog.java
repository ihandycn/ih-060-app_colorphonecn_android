package com.acb.libwallpaper.live.dialog;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.acb.libwallpaper.live.LauncherAnalytics;
import com.acb.libwallpaper.live.LauncherConstants;
 import com.honeycomb.colorphone.R;
import com.acb.libwallpaper.live.util.ActivityUtils;
import com.acb.libwallpaper.live.util.CommonUtils;
import com.acb.libwallpaper.live.util.ViewUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.ConflictLogger;
import com.superapps.util.Fonts;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public abstract class BaseLauncherDialog {
    private static final String TAG = BaseLauncherDialog.class.getSimpleName();

    public static final int DIALOG_ONE_BTN = 1;
    public static final int DIALOG_TWO_BTN = 2;
    public static final int DIALOG_TWO_BTN_BIASED = 3;
    public static final int DIALOG_NO_BTN = 4;
    public static final int DIALOG_FLASH_BTN = 5;
    public static final int DIALOG_WITH_HEADER = 6;

    public static final int DIALOG_CONTENT_TEXT = 0;
    public static final int DIALOG_CONTENT_CHOICE = 1;
    public static final int DIALOG_CONTENT_TEXT_WITH_ICON = 2;
    public static final int LIGHT_DIALOG_CONTENT_TEXT = 3;

    protected abstract int getDialogType();

    protected abstract int getDialogContentType();

    protected abstract BaseLauncherDialogText getTitleInfo();

    protected abstract BaseLauncherDialogText getTextInfo();

    protected abstract BaseLauncherDialogButton getPositiveBtn();

    protected abstract Drawable getCloseIconDrawable();

    protected BaseLauncherDialogText getDescInfo() {
        return null;
    }

    protected int getDialogColor() {
        return Color.WHITE;
    }

    protected Drawable getTopImageDrawable() {
        return null;
    }

    protected String getDialogHeaderString() {
        return null;
    }

    protected Drawable getDialogHeaderDrawable() {
        return null;
    }

    /**
     * this method is working for dialog which not extends BaseLauncherDialog
     * but use BaseLauncherDialog Style (layout)
     */
    public static void initBaseDialogStyle(ViewGroup rootView, Context context, BaseLauncherDialogResIds resIds,
                                           BaseLauncherDialogText title, BaseLauncherDialogText content,
                                           BaseLauncherDialogText desc, int dialogContentType) {
        //set dialog color and corner
        LinearLayout background = rootView.findViewById(R.id.linearLayout);
        TextView button = rootView.findViewById(R.id.ok_btn);
        ImageView image = rootView.findViewById(R.id.horizontal_top_image);

        assert background != null;
        background.setBackground(BackgroundDrawables.createBackgroundDrawable(
                context.getResources().getColor(resIds.getBackgroundColor()),
                context.getResources().getDimension(R.dimen.dialog_corner_radius),
                false));

        if (resIds.getLottieSet() != 0 && context.getResources().getBoolean(resIds.getLottieSet())) {
            image.setVisibility(GONE);
            LottieAnimationView lottie = rootView.findViewById(R.id.horizontal_top_lottie);
            configLottie(context, lottie, rootView, resIds.getLottiePath());
            if (resIds.getLottieImgPath() != 0) {
                lottie.setImageAssetsFolder(context.getResources().getString(resIds.getLottieImgPath()));
            }
        } else if (resIds.getTopImage() != 0) {
            image.setImageDrawable(ContextCompat.getDrawable(context, resIds.getTopImage()));
        }


        // config btn
        if (null != button) {
            button.setText(context.getResources().getString(R.string.ok));
            button.setTextColor(context.getResources().getColor(resIds.getBackgroundColor()));
            Typeface font = Fonts.getTypeface(context.getResources().getString(R.string.dialog_ok_btn_font));
            button.setTypeface(font);
            button.setBackground(BackgroundDrawables.createBackgroundDrawable(
                    context.getResources().getColor(resIds.getButtonColor()),
                    adjustAlpha(context.getResources().getColor(resIds.getBackgroundColor()),
                            context.getResources().getColor(resIds.getButtonColor())),
                    (int) context.getResources().getDimension(R.dimen.dialog_btn_corner_radius),
                    false, true));
        }

        ViewGroup contentView = ViewUtils.findViewById(rootView, R.id.content_view);
        contentView.addView(createContentView(context, rootView, title, content, desc, dialogContentType));
    }

    public static void cleanTopMargin(View rootView){
        View p = rootView.findViewById(R.id.content_view);
        View v = p.findViewById(R.id.dialog_content);
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) v.getLayoutParams();
        lp.topMargin = 0;
        v.setLayoutParams(lp);
    }

    protected LottieAnimationView mLottie;
    protected Dialog mDialog;

    protected Drawable mTopImageDrawable;
    protected Activity mActivity;
    protected boolean mLottieSet = false;
    private FrameLayout mContentView;
    private int mDesiredWidth;

    protected View mRootView;
    protected ImageView mTopImage;
    private View dialogRoot, dialogContent;

    public BaseLauncherDialog(Context activity) {
        mActivity = CommonUtils.getActivity(activity);
    }

    protected void configDialog(Dialog builder) {
        mDialog = builder;
        builder.setContentView(mRootView);
        builder.setCancelable(true);
        builder.setOnDismissListener(dialog -> {
            HSLog.d(TAG, "onDismiss");
            onDismissComplete();
            HSGlobalNotificationCenter.sendNotification(LauncherConstants.NOTIFICATION_TIP_DISMISS);
        });
        builder.setOnCancelListener(dialog -> {
            HSLog.d(TAG, "onCancel");
            dismissSafely();
        });
        mDialog.setOnShowListener(dialog -> HSLog.d(TAG, "OnShow"));
        mDialog.setCanceledOnTouchOutside(false);
    }

    @SuppressLint("InflateParams")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void init() {
        LayoutInflater mLayoutInflater = LayoutInflater.from(mActivity);
        Dialog builder = new Dialog(mActivity, R.style.NewDialogTheme);

        switch (getDialogType()) {
            case DIALOG_ONE_BTN:
                mRootView = mLayoutInflater.inflate(R.layout.dialog_one_btn, null);
                break;
            case DIALOG_TWO_BTN:
                mRootView = mLayoutInflater.inflate(R.layout.dialog_two_btn, null);
                break;
            case DIALOG_TWO_BTN_BIASED:
                mRootView = mLayoutInflater.inflate(R.layout.dialog_two_btn_biased, null);
                break;
            case DIALOG_NO_BTN:
                mRootView = mLayoutInflater.inflate(R.layout.dialog_no_btn, null);
                break;
            case DIALOG_WITH_HEADER:
                mRootView = mLayoutInflater.inflate(R.layout.dialog_one_btn_with_header, null);
                break;
        }

        configDialog(builder);
        mContentView = ViewUtils.findViewById(mRootView, R.id.content_view);
        mContentView.addView(createContentView(mLayoutInflater, mContentView));

        //set dialog color and corner
        dialogContent = mRootView.findViewById(R.id.linearLayout);
        assert dialogContent != null;
        dialogContent.setBackground(BackgroundDrawables.createBackgroundDrawable(getDialogColor(),
                getResources().getDimension(R.dimen.dialog_corner_radius), false));
        TextView fromTv = ViewUtils.findViewById(mRootView, R.id.dialog_header);
        ImageView fromImg = ViewUtils.findViewById(mRootView, R.id.weak_dialog_from_img);
        if (fromTv != null) {
            fromTv.setText(getDialogHeaderString());
            fromImg.setImageDrawable(getDialogHeaderDrawable());
        }
        mTopImage = ViewUtils.findViewById(mRootView, R.id.horizontal_top_image);
        dialogRoot = mRootView.findViewById(R.id.dialog_root_view);

        animateOpen();
        initButtons();
    }

    public final boolean show() {
        if (mActivity == null || mActivity.isFinishing() || ActivityUtils.isDestroyed(mActivity)) {
            return false;
        }
        init();
        if (hasTopImage()) {
            mTopImage.setVisibility(VISIBLE);
        } else {
            mTopImage.setVisibility(GONE);
        }
        try {
            if (mDialog.getWindow() != null) {
                mDialog.getWindow().getAttributes().windowAnimations = Animation.ABSOLUTE;
            }
            mDialog.show();
        } catch (Exception e) {
            return false;
        }

        if (mDesiredWidth > 0) {
            mTopImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        mDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        onShow();
        return true;
    }

    protected void initTopImage(ImageView imageView) {
        imageView.setImageDrawable(getTopImageDrawable());
    }

    protected void initButtons() {
        TextView positiveBtnView = ViewUtils.findViewById(mRootView, R.id.ok_btn);
        TextView negativeBtnView = ViewUtils.findViewById(mRootView, R.id.negative_btn);
        TextView textBtnView = ViewUtils.findViewById(mRootView, R.id.text_btn);
        TextView negativeBtnVerticalView = ViewUtils.findViewById(mRootView, R.id.negative_btn_vertical);
        TextView positiveBtnVerticalView = ViewUtils.findViewById(mRootView, R.id.ok_btn_vertical);
        LinearLayout btnGroupHorizontal = ViewUtils.findViewById(mRootView, R.id.btn_group_horizontal);
        LinearLayout btnGroupVertical = ViewUtils.findViewById(mRootView, R.id.btn_group_vertical);
        ImageView cancelBtnView = ViewUtils.findViewById(mRootView, R.id.cancel_btn);

        BaseLauncherDialogButton positiveBtn = getPositiveBtn();
        BaseLauncherDialogButton negativeBtn = getNegativeBtn();
        BaseLauncherDialogButton biasedBtn = getBiasedBtn();

        try {
            switch (getDialogType()) {
                case DIALOG_TWO_BTN:
                    // default is horizontal
                    configButton(positiveBtnView, positiveBtn);
                    positiveBtnView.setOnClickListener(this::onClickPositiveButton);
                    configButton(negativeBtnView, negativeBtn);
                    negativeBtnView.setOnClickListener(v -> {
                        onClickNegativeButton(v);
                        dismiss();
                    });

                    negativeBtnView.post(() -> {
                        if (negativeBtnView.getLineCount() > 1 || positiveBtnView.getLineCount() > 1) {
                            // vertical
                            btnGroupHorizontal.setVisibility(GONE);
                            btnGroupVertical.setVisibility(VISIBLE);

                            configButton(positiveBtnVerticalView, positiveBtn);
                            positiveBtnVerticalView.setOnClickListener(this::onClickPositiveButton);
                            configButton(negativeBtnVerticalView, negativeBtn);
                            negativeBtnVerticalView.setOnClickListener(v -> {
                                onClickNegativeButton(v);
                                dismiss();
                            });
                        }
                    });
                    break;
                case DIALOG_TWO_BTN_BIASED:
                    configButton(positiveBtnView, positiveBtn);
                    positiveBtnView.setOnClickListener(this::onClickPositiveButton);
                    configButton(textBtnView, biasedBtn);
                    textBtnView.setOnClickListener(v -> {
                        onClickNegativeButton(v);
                        dismiss();
                    });
                    break;
                case DIALOG_NO_BTN:
                    break;
                default:
                    configButton(positiveBtnView, positiveBtn);
                    positiveBtnView.setOnClickListener(this::onClickPositiveButton);
                    break;
            }

            if (null != cancelBtnView) {
                cancelBtnView.setImageDrawable(getCloseIconDrawable());
                cancelBtnView.setOnClickListener(v -> {
                    onClickNegativeButton(v);
                    dismiss();
                });
            }
        } catch (Exception e) {
        }
    }

    protected View createContentView(LayoutInflater inflater, ViewGroup root) {
        View v;
        switch (getDialogContentType()) {
            case DIALOG_CONTENT_TEXT:
                v = inflater.inflate(R.layout.dialog_content_text, root, false);
                break;
            case DIALOG_CONTENT_CHOICE:
                v = inflater.inflate(R.layout.dialog_content_choice, root, false);
                configRadioGroup(v);
                break;
            case DIALOG_CONTENT_TEXT_WITH_ICON:
                v = inflater.inflate(R.layout.dialog_content_text_with_icon, root, false);
                setContentIcon(v);
                break;
            case LIGHT_DIALOG_CONTENT_TEXT:
                v = inflater.inflate(R.layout.light_dialog_content_text, root, false);
                setContentIcon(v);
                break;
            default:
                v = inflater.inflate(R.layout.dialog_content_text, root, false);
                break;
        }
        TextView mTitleTv = ViewUtils.findViewById(v, R.id.dialog_title);
        TextView mDescTv = ViewUtils.findViewById(v, R.id.dialog_desc);
        TextView mContentTv = ViewUtils.findViewById(v, R.id.dialog_content);


        final BaseLauncherDialogText title = getTitleInfo();
        configDialogContent(mTitleTv, title);

        final BaseLauncherDialogText desc = getDescInfo();
        configDialogContent(mDescTv, desc);

        final BaseLauncherDialogText content = getTextInfo();
        configDialogContent(mContentTv, content);
        return v;
    }

    protected BaseLauncherDialogButton getNegativeBtn() {
        return null;
    }

    protected BaseLauncherDialogButton getBiasedBtn() {
        return null;
    }

    protected void configRadioGroup(View view) {
    }

    protected void configLottie() {
        View v = mRootView.findViewById(R.id.content_view);
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) v.getLayoutParams();
        lp.topMargin = (int) mActivity.getResources().getDimension(R.dimen.dialog_content_margin_top_img);
        v.setLayoutParams(lp);
        mLottie = mRootView.findViewById(R.id.horizontal_top_lottie);
    }

    protected void setContentIcon(View v) {
    }

    protected void onClickPositiveButton(View v) {
        dismiss();
    }

    protected void onClickNegativeButton(View v) {
        dismiss();
    }

    protected void onShow() {
        ConflictLogger.markDialogShow(getClass().getSimpleName(), (dialogWillShow, dialogsShowing) -> {
            LauncherAnalytics.logEvent("Launcher_PopUp_Conflict", true,
                    "DialogWillShow", dialogWillShow,
                    "DialogsShowing", dialogsShowing.toString());
        });
    }

    protected void onCanceled() {

    }

    protected void onDismissComplete() {
        ConflictLogger.markDialogDismiss(getClass().getSimpleName());
    }

    /**
     * Don not call this when <b>click CANCEL</b> button!
     */
    public final void dismiss() {
        if (!ActivityUtils.isDestroyed(mActivity)) {
            if (dialogRoot != null && dialogContent != null) {
                dialogRoot.animate().setDuration(320).alpha(0).start();
                dialogContent.animate().scaleX(0.97f).scaleY(0.97f).alpha(0).setDuration(280).start();
                new Handler().postDelayed(() -> {
                    if (mDialog.isShowing()) {
                        dismissSafely();
                    }
                }, 320);
            } else {
                dismissSafely();
            }
        }
    }

    private void dismissSafely() {
        try {
            mDialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final Resources getResources() {
        return getApplicationContext().getResources();
    }

    public final Context getApplicationContext() {
        return HSApplication.getContext();
    }

    // for ripple color adjust
    public static int adjustAlpha(@ColorInt int color, int background) {
        float factor = 0.5f;
        if (background == Color.WHITE) {
            factor = 0.3f;
        }
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
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
        return mLottieSet;
    }

    private void animateOpen() {
        dialogRoot.setAlpha(0);
        dialogRoot.animate()
                .setDuration(320)
                .alpha(1f)
                .start();
        dialogContent.setAlpha(0);
        dialogContent.setScaleX(0.97f);
        dialogContent.setScaleY(0.97f);
        dialogContent.animate()
                .alpha(1)
                .setDuration(280)
                .scaleX(1f)
                .scaleY(1f)
                .setInterpolator(PathInterpolatorCompat.create(0.06f, 0.73f, 0.42f, 0.72f))
                .start();
        initTopImage(mTopImage);
    }

    private void configDialogContent(TextView content, BaseLauncherDialogText dialog) {
        if (content != null && dialog != null && !TextUtils.isEmpty(dialog.getText())) {
            content.setVisibility(VISIBLE);
            content.setText(dialog.getText());
            content.setTextColor(dialog.getColor());
            content.setAlpha(dialog.getAlpha());
        } else if (content != null) {
            content.setVisibility(GONE);
        }
    }

    private void configButton(TextView view, BaseLauncherDialogButton button) {
        view.setActivated(true);
        view.setText(button.getButtonText());
        view.setTextColor(button.getButtonTextColor());
        view.setBackground(BackgroundDrawables.createBackgroundDrawable(
                button.getColor(),
                button.getRippleColor(),
                button.getButtonRadius(),
                false, true));
    }

    private static void configLottie(Context context, LottieAnimationView lottie, View rootView, int resID) {
        View v = rootView.findViewById(R.id.content_view);
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) v.getLayoutParams();
        lp.topMargin = (int) context.getResources().getDimension(R.dimen.dialog_content_margin_top_img);
        v.setLayoutParams(lp);
        lottie.setAnimation(context.getResources().getString(resID));

        ViewGroup.LayoutParams ls = lottie.getLayoutParams();
        ls.height = (int) context.getResources().getDimension(R.dimen.boost_lottie_height);
        lottie.setLayoutParams(ls);
        lottie.setRepeatCount(-1);
    }

    private static View createContentView(Context context, ViewGroup root, BaseLauncherDialogText title,
                                          BaseLauncherDialogText content, BaseLauncherDialogText desc,
                                          int dialogContentType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v;
        switch (dialogContentType) {
            case DIALOG_CONTENT_TEXT:
                v = inflater.inflate(R.layout.dialog_content_text, root, false);
                break;
            case DIALOG_CONTENT_CHOICE:
                v = inflater.inflate(R.layout.dialog_content_choice, root, false);
                break;
            case DIALOG_CONTENT_TEXT_WITH_ICON:
                v = inflater.inflate(R.layout.dialog_content_text_with_icon, root, false);
                break;
            case LIGHT_DIALOG_CONTENT_TEXT:
                v = inflater.inflate(R.layout.light_dialog_content_text, root, false);
                break;
            default:
                v = inflater.inflate(R.layout.dialog_content_text, root, false);
                break;
        }
        TextView mTitleTv = ViewUtils.findViewById(v, R.id.dialog_title);
        TextView mContentTv = ViewUtils.findViewById(v, R.id.dialog_content);
        TextView mDescTv = ViewUtils.findViewById(v, R.id.dialog_desc);

        if (title != null) {
            mTitleTv.setVisibility(VISIBLE);
            mTitleTv.setText(title.getText());
            mTitleTv.setTextColor(title.getColor());
            mTitleTv.setAlpha(title.getAlpha());
        }

        if (content != null) {
            mContentTv.setVisibility(VISIBLE);
            mContentTv.setText(content.getText());
            mContentTv.setTextColor(content.getColor());
            mContentTv.setAlpha(content.getAlpha());
        }

        if (desc != null) {
            mDescTv.setVisibility(VISIBLE);
            mDescTv.setText(desc.getText());
            mDescTv.setTextColor(desc.getColor());
            mDescTv.setAlpha(desc.getAlpha());
        }
        return v;
    }
}
