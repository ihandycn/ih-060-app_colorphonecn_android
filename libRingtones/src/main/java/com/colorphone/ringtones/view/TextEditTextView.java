package com.colorphone.ringtones.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

/**
 * @author sundxing
 */
public class TextEditTextView extends android.support.v7.widget.AppCompatEditText {
    public TextEditTextView(Context context) {
        super(context);
    }

    public TextEditTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextEditTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private OnKeyBoardHideListener onKeyBoardHideListener;

    public void setOnKeyBoardHideListener(OnKeyBoardHideListener onKeyBoardHideListener) {
        this.onKeyBoardHideListener = onKeyBoardHideListener;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == 1) {
            super.onKeyPreIme(keyCode, event);
            onKeyBoardHideListener.onKeyHide(keyCode, event);
            return false;
        }
        return super.onKeyPreIme(keyCode, event);
    }

    public interface OnKeyBoardHideListener{
        void onKeyHide(int keyCode, KeyEvent event);
    }
}