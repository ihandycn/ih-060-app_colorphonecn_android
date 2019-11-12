package com.honeycomb.colorphone.wallpaper.util;

import java.util.Arrays;

/**
 * Helper class to generate bucketed description for numerical (integer) parameters in event logs.
 *
 * Eg. < 1, 1, 2, 3 ~ 5, 6 ~ 9, 10 ~ 19, 20+ (intervals are inclusive)
 */
public class IntegerBuckets {

    private int[] mSplits;

    public IntegerBuckets(int... splits) {
        sortAndValidate(splits);
        mSplits = splits;
    }

    private void sortAndValidate(int[] splits) {
        Arrays.sort(splits);
        for (int i = 1; i < splits.length; i++) {
            if (splits[i] == splits[i - 1]) {
                throw new IllegalArgumentException("Illegal split array with duplicated item: "
                        + Arrays.toString(splits));
            }
        }
    }

    public String getBucket(int value) {
        if (mSplits == null || mSplits.length == 0) {
            return "--";
        }
        int splitCount = mSplits.length;
        int left = 0, right = splitCount - 1;
        if (value < mSplits[left]) {
            return "< " + mSplits[left];
        }
        if (value >= mSplits[right]) {
            return mSplits[right] + "+";
        }
        while (right - left > 1) {
            int mid = (left + right) / 2;
            if (value < mSplits[mid]) {
                right = mid;
            } else {
                left = mid;
            }
        }
        boolean singlePoint = (mSplits[right] - mSplits[left] == 1);
        if (singlePoint) {
            return Integer.toString(mSplits[left]);
        } else {
            return mSplits[left] + " ~ " + (mSplits[right] - 1);
        }
    }
}
