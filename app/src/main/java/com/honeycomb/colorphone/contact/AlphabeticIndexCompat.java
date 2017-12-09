package com.honeycomb.colorphone.contact;

import android.annotation.TargetApi;
import android.content.Context;
import android.icu.text.AlphabeticIndex;
import android.os.Build;
import android.os.LocaleList;

import com.acb.utils.ReflectionHelper;
import com.honeycomb.colorphone.util.Utils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;

public class AlphabeticIndexCompat {
    private static final String TAG = "AlphabeticIndexCompat";

    private static final String MID_DOT = "...";
    private static final String DIGIT_LABEL = "#";
    private final BaseIndex mBaseIndex;
    private final String mDefaultMiscLabel;

    private final HashMap<CharSequence, String> mCachedSectionNames = new HashMap<>();

    public boolean isSectionNameMiscOrDigit(String sectionName) {
        return MID_DOT.equals(sectionName) || DIGIT_LABEL.equals(sectionName) || mDefaultMiscLabel.equals(mDefaultMiscLabel);
    }

    private static final class SingletonHolder {
        static final AlphabeticIndexCompat sInstance = new AlphabeticIndexCompat(HSApplication.getContext());
    }

    public static AlphabeticIndexCompat getInstance() {
        return SingletonHolder.sInstance;
    }

    private AlphabeticIndexCompat(Context context) {
        BaseIndex index = null;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                index = new AlphabeticIndexVN(context);
            }
        } catch (Exception e) {
            HSLog.d(TAG, "Unable to load the system index: " + e);
        }
        if (index == null) {
            try {
                index = new AlphabeticIndexV16(context);
            } catch (Exception e) {
                HSLog.d(TAG, "Unable to load the system index: " + e);
            }
        }

        mBaseIndex = index == null ? new BaseIndex() : index;

        if (context.getResources().getConfiguration().locale
                .getLanguage().equals(Locale.JAPANESE.getLanguage())) {
            // Japanese character 他 ("misc")
            mDefaultMiscLabel = "\u4ed6";
            // TODO(winsonc, omakoto): We need to handle Japanese sections better, especially the kanji
        } else {
            // Dot
            mDefaultMiscLabel = MID_DOT;
        }
    }

    /**
     * Gets the section name for an given string {@param cs}. Use cache if present.
     */
    public String getSectionName(CharSequence cs) {
        String cached = mCachedSectionNames.get(cs);
        if (cached == null) {
            cached = computeSectionName(cs);
            mCachedSectionNames.put(cs, cached);
        }
        return cached;
    }

    /**
     * Computes the section name for an given string {@param cs}.
     */
    private String computeSectionName(CharSequence cs) {
        String s = Utils.trim(cs);
        String sectionName = mBaseIndex.getBucketLabel(mBaseIndex.getBucketIndex(s));
        if (Utils.trim(sectionName).isEmpty() && s.length() > 0) {
            int c = s.codePointAt(0);
            boolean startsWithDigit = Character.isDigit(c);
            if (startsWithDigit) {
                // Digit section
                return DIGIT_LABEL;
            } else {
                boolean startsWithLetter = Character.isLetter(c);
                if (startsWithLetter) {
                    return mDefaultMiscLabel;
                } else {
                    // In languages where these differ, this ensures that we differentiate
                    // between the misc section in the native language and a misc section
                    // for everything else.
                    return MID_DOT;
                }
            }
        }
        return sectionName;
    }

    /**
     * Base class to support Alphabetic indexing if not supported by the framework.
     * TODO(winsonc): disable for non-english locales
     */
    private static class BaseIndex {

        private static final String BUCKETS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-";
        private static final int UNKNOWN_BUCKET_INDEX = BUCKETS.length() - 1;

        /**
         * Returns the index of the bucket in which the given string should appear.
         */
        protected int getBucketIndex(String s) {
            if (s.isEmpty()) {
                return UNKNOWN_BUCKET_INDEX;
            }
            int index = BUCKETS.indexOf(s.substring(0, 1).toUpperCase());
            if (index != -1) {
                return index;
            }
            return UNKNOWN_BUCKET_INDEX;
        }

        /**
         * Returns the label for the bucket at the given index (as returned by getBucketIndex).
         */
        protected String getBucketLabel(int index) {
            return BUCKETS.substring(index, index + 1);
        }
    }

    /**
     * Reflected libcore.icu.AlphabeticIndex implementation, falls back to the base
     * alphabetic index.
     */
    private static class AlphabeticIndexV16 extends BaseIndex {

        private Object mAlphabeticIndex;
        private Method mGetBucketIndexMethod;
        private Method mGetBucketLabelMethod;

        AlphabeticIndexV16(Context context) throws Exception {
            Locale curLocale = context.getResources().getConfiguration().locale;
            Class clazz = ReflectionHelper.getClass("libcore.icu.AlphabeticIndex");
            mGetBucketIndexMethod = ReflectionHelper.getDeclaredMethod(clazz, "getBucketIndex", String.class);
            mGetBucketLabelMethod = ReflectionHelper.getDeclaredMethod(clazz, "getBucketLabel", int.class);
            mAlphabeticIndex = ReflectionHelper.getConstructor(clazz, Locale.class).newInstance(curLocale);

            if (!curLocale.getLanguage().equals(Locale.ENGLISH.getLanguage())) {
                ReflectionHelper.getDeclaredMethod(clazz, "addLabels", Locale.class)
                        .invoke(mAlphabeticIndex, Locale.ENGLISH);
            }
        }

        /**
         * Returns the index of the bucket in which {@param s} should appear.
         * Function is synchronized because underlying routine walks an iterator
         * whose state is maintained inside the index object.
         */
        protected int getBucketIndex(String s) {
            try {
                return (Integer) mGetBucketIndexMethod.invoke(mAlphabeticIndex, s);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return super.getBucketIndex(s);
        }

        /**
         * Returns the label for the bucket at the given index (as returned by getBucketIndex).
         */
        protected String getBucketLabel(int index) {
            try {
                return (String) mGetBucketLabelMethod.invoke(mAlphabeticIndex, index);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return super.getBucketLabel(index);
        }
    }

    /**
     * Implementation based on {@link AlphabeticIndex}.
     */
    @TargetApi(Build.VERSION_CODES.N)
    private static class AlphabeticIndexVN extends BaseIndex {

        private final AlphabeticIndex.ImmutableIndex mAlphabeticIndex;

        AlphabeticIndexVN(Context context) {
            LocaleList locales = context.getResources().getConfiguration().getLocales();
            int localeCount = locales.size();

            Locale primaryLocale = localeCount == 0 ? Locale.ENGLISH : locales.get(0);
            AlphabeticIndex indexBuilder = new AlphabeticIndex(primaryLocale);
            for (int i = 1; i < localeCount; i++) {
                indexBuilder.addLabels(locales.get(i));
            }
            indexBuilder.addLabels(Locale.ENGLISH);

            mAlphabeticIndex = indexBuilder.buildImmutableIndex();
        }

        /**
         * Returns the index of the bucket in which {@param s} should appear.
         */
        protected int getBucketIndex(String s) {
            return mAlphabeticIndex.getBucketIndex(s);
        }

        /**
         * Returns the label for the bucket at the given index
         */
        protected String getBucketLabel(int index) {
            return mAlphabeticIndex.getBucket(index).getLabel();
        }
    }
}
