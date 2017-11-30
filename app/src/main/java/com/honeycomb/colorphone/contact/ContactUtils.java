package com.honeycomb.colorphone.contact;

import java.text.Collator;

/**
 * Created by sundxing on 17/11/29.
 */

public class ContactUtils {

    public static String getSectionName(CharSequence target) {
        return AlphabeticIndexCompat.getInstance().getSectionName(target);
    }

    /**
     * Compares two titles with the same return value semantics as Comparator.
     */
    public static int compareTitles(String titleA, String titleB) {
        // Ensure that we de-prioritize any titles that don't start with a linguistic letter or digit
        boolean aStartsWithLetter = (titleA.length() > 0) &&
                Character.isLetterOrDigit(titleA.codePointAt(0));
        boolean bStartsWithLetter = (titleB.length() > 0) &&
                Character.isLetterOrDigit(titleB.codePointAt(0));
        if (aStartsWithLetter && !bStartsWithLetter) {
            return -1;
        } else if (!aStartsWithLetter && bStartsWithLetter) {
            return 1;
        }

        // Order by the title in the current locale
        return Collator.getInstance().compare(titleA, titleB);
    }

}
