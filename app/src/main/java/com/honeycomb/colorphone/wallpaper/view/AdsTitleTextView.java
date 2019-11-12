package com.honeycomb.colorphone.wallpaper.view;

import android.content.Context;
import android.util.AttributeSet;

import com.superapps.view.TypefacedTextView;

public class AdsTitleTextView extends TypefacedTextView {

    private static final String NEW_LINE_CHARACTER = "\n";
    private static final String SPACE_CHARACTER = "\t";

    public AdsTitleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (text != null) {
            String oriString = (String) text;
            String newString = oriString.replaceAll(NEW_LINE_CHARACTER, SPACE_CHARACTER);
            super.setText(newString, type);
        } else {
            super.setText(null, type);
        }
    }
}
