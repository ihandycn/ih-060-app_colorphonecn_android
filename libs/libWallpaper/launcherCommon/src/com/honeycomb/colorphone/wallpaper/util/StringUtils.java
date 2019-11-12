package com.honeycomb.colorphone.wallpaper.util;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;

public class StringUtils {

    public static String format(String text){
        if (TextUtils.isEmpty(text)) {
            return text;
        }

        text = text.trim();
        String symbols[] = {":", "：", "-", "–"};
        for (String symbol : symbols) {
            if (text.contains(symbol)) {
                text = text.substring(0, text.lastIndexOf(symbol)).trim();
                break;
            }
        }
        return text;
    }

    public static String toCamelCase(String original, String splitRegex) {
        String[] splits = original.split(splitRegex);
        StringBuilder out = new StringBuilder();
        for (String split : splits) {
            out.append(capitalizeFirstLetter(split));
        }
        return out.toString();
    }

    public static String capitalizeFirstLetter(String original) {
        return original.substring(0, 1).toUpperCase() + original.substring(1).toLowerCase();
    }

    public static Spannable getTextWithBoldSpan(String fullString, String boldSubstring) {
        int start = fullString.indexOf(boldSubstring);
        if (start == -1) {
            throw new IllegalArgumentException("boldSubstring is not a substring of fullString.");
        }
        Spannable spannable = new SpannableString(fullString);
        spannable.setSpan(new StyleSpan(Typeface.BOLD), start, start + boldSubstring.length(),
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    /**
     * Return an HTML string with matched string emphasized by corresponding color (default: black).
     *
     * @param strSource String to be emphasized.
     * @param strFilter Filter string.
     *
     * @return Emphasized string in HTML.
     */
    public static String emphasizeMatchedString(String strSource, String strFilter) {
        return emphasizeMatchedStringWithColor(strSource, strFilter, "black");
    }

    public static String emphasizeMatchedStringWithColor(String strSource, String strFilter, String color) {
        if (TextUtils.isEmpty(strSource)) {
            return null;
        }
        String strPrefix = "<font color='" + color + "'>";
        String strSuffix = "</font>";

        String strSourceLowerCase = strSource.toLowerCase();
        String strFilterLowerCase = strFilter.toLowerCase();
        int i = strSourceLowerCase.indexOf(strFilterLowerCase);
        if (i >= 0) {
            StringBuilder builder = new StringBuilder(strSource.length());
            int len = strFilter.length();
            builder.append(strSource, 0, i).append(strPrefix).append(strSource, i, i + len).append(strSuffix);
            i += len;
            int j = i;
            while ((i = strSourceLowerCase.indexOf(strFilterLowerCase, i)) > 0) {
                builder.append(strSource, j, i).append(strPrefix).append(strSource, i, i + len).append(strSuffix);
                i += len;
                j = i;
            }
            builder.append(strSource, j, strSource.length());
            return builder.toString();
        }
        return strSource;
    }

    public static boolean containsIgnoreCase(String src, String what) {
        final int length = what.length();
        if (length == 0)
            return true; // Empty string is contained

        final char firstLo = Character.toLowerCase(what.charAt(0));
        final char firstUp = Character.toUpperCase(what.charAt(0));

        for (int i = src.length() - length; i >= 0; i--) {
            // Quick check before calling the more expensive regionMatches() method:
            final char ch = src.charAt(i);
            if (ch != firstLo && ch != firstUp)
                continue;

            if (src.regionMatches(true, i, what, 0, length))
                return true;
        }

        return false;
    }
}
