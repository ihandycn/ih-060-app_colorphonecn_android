package com.colorphone.lock.util;

import android.support.annotation.NonNull;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSPreferenceHelper;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This helper class wraps {@link HSPreferenceHelper} and:
 * <p/>
 * (1) Provides access to multiple preferences files with cached helper instances.
 * (2) Is thread safe.
 *
 * This class is only for (a) preferences files used in single process, (b) the default preferences file. Don't use this
 * for preferences file used in multi-process.
 */
public class PreferenceHelper {
    public static final String DEFAULT_PREFS = "default_preferences"; // Multi-process

    private static Map<String, PreferenceHelper> sHelpersCache = new HashMap<>();

    private HSPreferenceHelper mFrameworkHelper;

    /**
     * {@link Integer} has an internal cache to support the object identity semantics of autoboxing for values between
     * -128 and 127 (inclusive) as required by JLS. We use the non-negative half of these cached objects as segmented
     * locks by mapping pref key strings to them. This is to avoid applying a global lock for all (unrelated) keys.
     */
    private static final int INTEGER_CACHE_UPPER_BOUND = 128;

    /**
     * Get default PreferenceHelper object.
     */
    public static PreferenceHelper getDefault() {
        return get(DEFAULT_PREFS);
    }

    /**
     * Get PreferenceHelper by filename.
     */
    public synchronized static PreferenceHelper get(String filename) {
        // Elements in HashMap's backing array do not have a volatile semantic.
        // So double checked locking is not safe here.
        PreferenceHelper prefs = sHelpersCache.get(filename);
        if (prefs == null) {
            if (DEFAULT_PREFS.equals(filename)) {
                prefs = new PreferenceHelper(HSPreferenceHelper.getDefault());
            } else {
                prefs = new PreferenceHelper(HSPreferenceHelper.create(HSApplication.getContext(), filename));
            }
            sHelpersCache.put(filename, prefs);
        }
        return prefs;
    }

    private PreferenceHelper(HSPreferenceHelper preferenceHelper) {
        mFrameworkHelper = preferenceHelper;
    }

    /**
     * Execute the given action only once for the given token.
     *
     * Note that this method is thread safe only with token consumption.
     * Action execution should be synchronized by caller if necessary.
     *
     * @param action The action to perform.
     * @param token  The identifier on which the action can be performed only once.
     * @return {@code true} if the action is performed. {@code false} if the action has already been done before and
     * not performed this time.
     */
    public boolean doOnce(@NonNull Runnable action, String token) {
        boolean run = false;
        synchronized (getLock(token)) {
            if (!mFrameworkHelper.getBoolean(token, false)) {
                mFrameworkHelper.putBoolean(token, true);
                run = true;
            }
        }
        if (run) {
            action.run();
        }
        return run;
    }

    /**
     * Increment an integer on the given key by 1.
     *
     * @return The value of the integer *AFTER* incrementation. Integer overflow is handled.
     */
    public int incrementAndGetInt(String key) {
        int incremented;
        synchronized (getLock(key)) {
            incremented = unsignedIncrement(mFrameworkHelper.getInt(key, 0), 1);
            mFrameworkHelper.putInt(key, incremented);
        }
        return incremented;
    }

    /**
     * Increment {@code original} by the quantity of {@code incrementBy} with integer overflow handled.
     * In case of an overflow, return value is folded to restart from 0 as if it was an unsigned integer.
     *
     * Both parameters should be non-negative, or return value is undefined.
     */
    public static int unsignedIncrement(int original, int incrementBy) {
        int maxIncrementWithoutOverflow = Integer.MAX_VALUE - original;
        if (incrementBy > maxIncrementWithoutOverflow) {
            return incrementBy - maxIncrementWithoutOverflow - 1;
        }
        return original + incrementBy;
    }


    public boolean contains(String key) {
        synchronized (getLock(key)) {
            return mFrameworkHelper.contains(key);
        }
    }

    public void remove(String key) {
        synchronized (getLock(key)) {
            mFrameworkHelper.remove(key);
        }
    }

    public int getInt(String key, int defaultValue) {
        synchronized (getLock(key)) {
            return mFrameworkHelper.getInt(key, defaultValue);
        }
    }

    public void putInt(String key, int value) {
        synchronized (getLock(key)) {
            mFrameworkHelper.putInt(key, value);
        }
    }

    public long getLong(String key, long defaultValue) {
        synchronized (getLock(key)) {
            return mFrameworkHelper.getLong(key, defaultValue);
        }
    }

    public void putLong(String key, long value) {
        synchronized (getLock(key)) {
            mFrameworkHelper.putLong(key, value);
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        synchronized (getLock(key)) {
            return mFrameworkHelper.getBoolean(key, defaultValue);
        }
    }

    public void putBoolean(String key, boolean value) {
        synchronized (getLock(key)) {
            mFrameworkHelper.putBoolean(key, value);
        }
    }

    public String getString(String key, String defaultValue) {
        synchronized (getLock(key)) {
            return mFrameworkHelper.getString(key, defaultValue);
        }
    }

    public void putString(String key, String value) {
        synchronized (getLock(key)) {
            mFrameworkHelper.putString(key, value);
        }
    }


    public void addMap(String key, Map<String,String> inputMap){
        synchronized (getLock(key)) {
            Map<String,String> savedMap = getMap(key);
            for (Map.Entry<String, String> entry : inputMap.entrySet()) {
                savedMap.put(entry.getKey(), entry.getValue());
            }

            JSONObject jsonObject = new JSONObject(savedMap);
            String jsonString = jsonObject.toString();
            mFrameworkHelper.putString(key, jsonString);
        }
    }

    public void putMap(String key, Map<String,String> inputMap){
        synchronized (getLock(key)) {
            JSONObject jsonObject = new JSONObject(inputMap);
            String jsonString = jsonObject.toString();
            mFrameworkHelper.putString(key, jsonString);
        }
    }

    public Map<String,String> getMap(String key){
        synchronized (getLock(key)) {
            Map<String,String> outputMap = new HashMap<>();
            try{
                String jsonString = mFrameworkHelper.getString(key, (new JSONObject()).toString());
                JSONObject jsonObject = new JSONObject(jsonString);
                Iterator<String> keysItr = jsonObject.keys();
                while(keysItr.hasNext()) {
                    String k = keysItr.next();
                    String v = (String) jsonObject.get(k);
                    outputMap.put(k,v);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            return outputMap;
        }
    }

    private Object /* Integer */ getLock(String key) {
        return key.hashCode() % INTEGER_CACHE_UPPER_BOUND;
    }

}
