package com.honeycomb.colorphone.feedback;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.boost.SafeWindowManager;
import com.honeycomb.colorphone.view.TranslatedImageSpan;
import com.superapps.util.Compats;
import com.superapps.util.Dimensions;
import com.superapps.util.Threads;

public abstract class FloatedRateGuideDialog extends BaseRateGuideDialog {

    private boolean isClosing = false;
    private View contentView;

    public FloatedRateGuideDialog(Context context) {
        this(context, null);
    }

    public FloatedRateGuideDialog(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatedRateGuideDialog(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init() {
        super.init();
        TextView textView = findViewById(getRateGuideContent());
        if (hasWriteCommentIcon()) {
            textView.setText(replaceFiveStarAndWriteComment());
        } else {
            textView.setText(replaceFiveStar());
        }
    }

    protected abstract boolean hasWriteCommentIcon();

    protected abstract int getLayoutResId();

    protected abstract int getRateGuideContent();

    protected abstract int getRateGuideContentString();

    @Override
    protected boolean IsInitStatusBarPadding() {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isClosing) {
            return true;
        }
        isClosing = true;
        if (contentView != null) {
            long delay = 100;
            contentView.setAlpha(1f);
            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(contentView, "alpha", 1f, 0f);
            alphaAnimator.setDuration(delay);
            alphaAnimator.start();
            Threads.postOnMainThreadDelayed(this::dismiss, delay);
        } else {
            dismiss();
        }
        return true;
    }

    @Override
    public void onAddedToWindow(SafeWindowManager windowManager) {
        contentView = findViewById(getRateGuideContent());
        contentView.setAlpha(0f);
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(contentView, "alpha", 0f, 1f);
        alphaAnimator.setStartDelay(40);
        alphaAnimator.setDuration(260);
        alphaAnimator.start();

        ObjectAnimator translationAnimator = ObjectAnimator.ofFloat(contentView, "translationY", Dimensions.pxFromDp(-48), 0f);
        translationAnimator.setDuration(300);
        translationAnimator.setInterpolator(PathInterpolatorCompat.create(0.32f, 0.99f, 0.6f, 1f));
        translationAnimator.start();

        ObjectAnimator floatAnimator = ObjectAnimator.ofFloat(contentView, "translationY", 0f, Dimensions.pxFromDp(-8), 0f);
        floatAnimator.setDuration(1000);
        floatAnimator.setStartDelay(300);
        floatAnimator.setRepeatCount(ValueAnimator.INFINITE);
        floatAnimator.setInterpolator(new LinearInterpolator());
        floatAnimator.start();
    }


    protected SpannableString replaceFiveStar() {
        String string = getContext().getString(getRateGuideContentString());
        String replaceString = getContext().getString(R.string.five_starts_replace_string);
        SpannableString spanString = new SpannableString(string);
        Drawable drawable = getContext().getDrawable(R.drawable.five_star_rate_guide_star_icon);
        if (drawable == null) {
            return spanString;
        }
        int iconIndex = string.indexOf(replaceString);
        drawable.setBounds(0, 0, Dimensions.pxFromDp(66), Dimensions.pxFromDp(11));
        TranslatedImageSpan imageSpan = new TranslatedImageSpan(drawable, ImageSpan.ALIGN_BASELINE);
        imageSpan.setTranslation(0, Dimensions.pxFromDp(Compats.IS_XIAOMI_DEVICE ? -1f : -6f));
        spanString.setSpan(imageSpan, iconIndex, iconIndex + replaceString.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        return spanString;
    }

    protected SpannableString replaceFiveStarAndWriteComment() {
        String string = getContext().getString(getRateGuideContentString());
        String replaceString = getContext().getString(R.string.write_comment_replace_string);
        SpannableString spanString = replaceFiveStar();
        Drawable drawable = getContext().getDrawable(R.drawable.five_star_rate_write_icon);
        int iconIndex = string.indexOf(replaceString);
        if (drawable == null || iconIndex - 4 < 0) {
            return spanString;
        }
        drawable.setBounds(0, 0, Dimensions.pxFromDp(13.6f), Dimensions.pxFromDp(13.6f));
        TranslatedImageSpan imageSpan = new TranslatedImageSpan(drawable, ImageSpan.ALIGN_BASELINE);
        imageSpan.setTranslation(0, Dimensions.pxFromDp(-4));
        spanString.setSpan(imageSpan, iconIndex, iconIndex + replaceString.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        spanString.setSpan(new ForegroundColorSpan(Color.parseColor("#1cc885")), iconIndex - 4, iconIndex - 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        spanString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), iconIndex - 4, iconIndex - 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        return spanString;
    }

}
