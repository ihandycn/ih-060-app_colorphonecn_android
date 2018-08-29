/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.honeycomb.colorphone.dialer.contact;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Trace;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.annotation.WorkerThread;
import android.telecom.TelecomManager;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.dialer.Assert;
import com.honeycomb.colorphone.dialer.PhoneNumberService;
import com.honeycomb.colorphone.dialer.call.DialerCall;
import com.honeycomb.colorphone.dialer.util.ContactsUtils;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class responsible for querying Contact Information for DialerCall objects. Can perform
 * asynchronous requests to the Contact Provider for information as well as respond synchronously
 * for any data that it currently has cached from previous queries. This class always gets called
 * from the UI thread so it does not need thread protection.
 */
@RequiresApi(api = Build.VERSION_CODES.M)
public class ContactInfoCache implements ContactsAsyncHelper.OnImageLoadCompleteListener {

    private static final String TAG = ContactInfoCache.class.getSimpleName();
    private static final int TOKEN_UPDATE_PHOTO_FOR_CALL_STATE = 0;
    private static ContactInfoCache cache = null;
    private final Context context;
    // Cache info map needs to be thread-safe since it could be modified by both main thread and
    // worker thread.
    private final ConcurrentHashMap<String, ContactCacheEntry> infoMap = new ConcurrentHashMap<>();
    private final Map<String, Set<ContactInfoCacheCallback>> callBacks = new ArrayMap<>();
    private int queryId;


    private ContactInfoCache(Context context) {
        Trace.beginSection("ContactInfoCache constructor");
        this.context = context;
        Trace.endSection();
    }

    public static synchronized ContactInfoCache getInstance(Context mContext) {
        if (cache == null) {
            cache = new ContactInfoCache(mContext.getApplicationContext());
        }
        return cache;
    }


    /**
     * Gets name strings based on some special presentation modes and the associated custom label.
     */
    private static String getPresentationString(
            Context context, int presentation, String customLabel) {
        String name = context.getString(R.string.unknown);
        if (!TextUtils.isEmpty(customLabel)
                && ((presentation == TelecomManager.PRESENTATION_UNKNOWN)
                || (presentation == TelecomManager.PRESENTATION_RESTRICTED))) {
            name = customLabel;
            return name;
        } else {
            if (presentation == TelecomManager.PRESENTATION_RESTRICTED) {
//        name = PhoneNumberHelper.getDisplayNameForRestrictedNumber(context);
            } else if (presentation == TelecomManager.PRESENTATION_PAYPHONE) {
                name = context.getString(R.string.payphone);
            }
        }
        return name;
    }

    ContactCacheEntry getInfo(String callId) {
        return infoMap.get(callId);
    }


    /**
     * Requests contact data for the DialerCall object passed in. Returns the data through callback.
     * If callback is null, no response is made, however the query is still performed and cached.
     *
     * @param callback The function to call back when the call is found. Can be null.
     */
    @MainThread
    public void findInfo(
            @NonNull final DialerCall call,
            final boolean isIncoming,
            @NonNull ContactInfoCacheCallback callback) {
        Trace.beginSection("ContactInfoCache.findInfo");
        Assert.isMainThread();
        Objects.requireNonNull(callback);

        Trace.beginSection("prepare callback");
        final String callId = call.getId();
        final ContactCacheEntry cacheEntry = infoMap.get(callId);
        Set<ContactInfoCacheCallback> callBacks = this.callBacks.get(callId);

        // We need to force a new query if phone number has changed.
        boolean forceQuery = needForceQuery(call, cacheEntry);
        Trace.endSection();
        Log.d(TAG, "findInfo: callId = " + callId + "; forceQuery = " + forceQuery);

        // If we have a previously obtained intermediate result return that now except needs
        // force query.
        if (cacheEntry != null && !forceQuery) {
            Log.d(
                    TAG,
                    "Contact lookup. In memory cache hit; lookup "
                            + (callBacks == null ? "complete" : "still running"));
            callback.onContactInfoComplete(callId, cacheEntry);
            // If no other callbacks are in flight, we're done.
            if (callBacks == null) {
                Trace.endSection();
                return;
            }
        }

        // If the entry already exists, add callback
        if (callBacks != null) {
            Log.d(TAG, "Another query is in progress, add callback only.");
            callBacks.add(callback);
            if (!forceQuery) {
                Log.d(TAG, "No need to query again, just return and wait for existing query to finish");
                Trace.endSection();
                return;
            }
        } else {
            Log.d(TAG, "Contact lookup. In memory cache miss; searching provider.");
            // New lookup
            callBacks = new ArraySet<>();
            callBacks.add(callback);
            this.callBacks.put(callId, callBacks);
        }

        Trace.beginSection("prepare query");
        /**
         * Performs a query for caller information. Save any immediate data we get from the query. An
         * asynchronous query may also be made for any data that we do not already have. Some queries,
         * such as those for voicemail and emergency call information, will not perform an additional
         * asynchronous query.
         */
        final CallerInfoQueryToken queryToken = new CallerInfoQueryToken(queryId, callId);
        queryId++;
//    final CallerInfo callerInfo =
//        CallerInfoUtils.getCallerInfoForCall(
//            context,
//            call,
//            new DialerCallCookieWrapper(callId, call.getNumberPresentation(), call.getCnapName()),
//            new FindInfoCallback(isIncoming, queryToken));
        Trace.endSection();

        if (cacheEntry != null) {
            // We should not override the old cache item until the new query is
            // back. We should only update the queryId. Otherwise, we may see
            // flicker of the name and image (old cache -> new cache before query
            // -> new cache after query)
            cacheEntry.queryId = queryToken.queryId;
            Log.d(TAG, "There is an existing cache. Do not override until new query is back");
        } else {
            // TODO load contact
            ContactCacheEntry initialCacheEntry = new ContactCacheEntry();
            sendInfoNotifications(callId, initialCacheEntry);
        }
        Trace.endSection();
    }

    /**
     * Implemented for ContactsAsyncHelper.OnImageLoadCompleteListener interface. Update contact photo
     * when image is loaded in worker thread.
     */
    @WorkerThread
    @Override
    public void onImageLoaded(int token, Drawable photo, Bitmap photoIcon, Object cookie) {
        Assert.isWorkerThread();
        CallerInfoQueryToken myCookie = (CallerInfoQueryToken) cookie;
        final String callId = myCookie.callId;
        final int queryId = myCookie.queryId;
        if (!isWaitingForThisQuery(callId, queryId)) {
            return;
        }
        loadImage(photo, photoIcon, cookie);
    }

    private void loadImage(Drawable photo, Bitmap photoIcon, Object cookie) {
        Log.d(TAG, "Image load complete with context: ");
        // TODO: may be nice to update the image view again once the newer one
        // is available on contacts database.
        CallerInfoQueryToken myCookie = (CallerInfoQueryToken) cookie;
        final String callId = myCookie.callId;
        ContactCacheEntry entry = infoMap.get(callId);

        if (entry == null) {
            Log.e(TAG, "Image Load received for empty search entry.");
            clearCallbacks(callId);
            return;
        }

        Log.d(TAG, "setting photo for entry: " + entry.toString());

        // Conference call icons are being handled in CallCardPresenter.
        if (photo != null) {
            Log.v(TAG, "direct drawable: ");
            entry.photo = photo;
            entry.photoType = ContactPhotoType.CONTACT;
        } else if (photoIcon != null) {
            Log.v(TAG, "photo icon: ");
            entry.photo = new BitmapDrawable(context.getResources(), photoIcon);
            entry.photoType = ContactPhotoType.CONTACT;
        } else {
            Log.v(TAG, "unknown photo");
            entry.photo = null;
            entry.photoType = ContactPhotoType.DEFAULT_PLACEHOLDER;
        }
    }

    /**
     * Implemented for ContactsAsyncHelper.OnImageLoadCompleteListener interface. make sure that the
     * call state is reflected after the image is loaded.
     */
    @MainThread
    @Override
    public void onImageLoadComplete(int token, Drawable photo, Bitmap photoIcon, Object cookie) {
        Assert.isMainThread();
        CallerInfoQueryToken myCookie = (CallerInfoQueryToken) cookie;
        final String callId = myCookie.callId;
        final int queryId = myCookie.queryId;
        if (!isWaitingForThisQuery(callId, queryId)) {
            return;
        }
        sendImageNotifications(callId, infoMap.get(callId));

        clearCallbacks(callId);
    }

    /**
     * Blows away the stored cache values.
     */
    public void clearCache() {
        infoMap.clear();
        callBacks.clear();
        queryId = 0;
    }

    /**
     * Sends the updated information to call the callbacks for the entry.
     */
    @MainThread
    private void sendInfoNotifications(String callId, ContactCacheEntry entry) {
        Trace.beginSection("ContactInfoCache.sendInfoNotifications");
        Assert.isMainThread();
        final Set<ContactInfoCacheCallback> callBacks = this.callBacks.get(callId);
        if (callBacks != null) {
            for (ContactInfoCacheCallback callBack : callBacks) {
                callBack.onContactInfoComplete(callId, entry);
            }
        }
        Trace.endSection();
    }

    @MainThread
    private void sendImageNotifications(String callId, ContactCacheEntry entry) {
        Trace.beginSection("ContactInfoCache.sendImageNotifications");
        Assert.isMainThread();
        final Set<ContactInfoCacheCallback> callBacks = this.callBacks.get(callId);
        if (callBacks != null && entry.photo != null) {
            for (ContactInfoCacheCallback callBack : callBacks) {
                callBack.onImageLoadComplete(callId, entry);
            }
        }
        Trace.endSection();
    }

    private void clearCallbacks(String callId) {
        callBacks.remove(callId);
    }

    /**
     * Callback interface for the contact query.
     */
    public interface ContactInfoCacheCallback {

        void onContactInfoComplete(String callId, ContactCacheEntry entry);

        void onImageLoadComplete(String callId, ContactCacheEntry entry);
    }

    /**
     * This is cached contact info, which should be the ONLY info used by UI.
     */
    public static class ContactCacheEntry {

        public String namePrimary;
        public String nameAlternative;
        public String number;
        public String location;
        public String label;
        public Drawable photo;
        @ContactPhotoType int photoType;
        boolean isSipCall;
        // Note in cache entry whether this is a pending async loading action to know whether to
        // wait for its callback or not.
        boolean hasPendingQuery;
        /**
         * Either a display photo or a thumbnail URI.
         */
        Uri displayPhotoUri;

        public Uri lookupUri; // Sent to NotificationMananger
        public String lookupKey;
        public long userType = ContactsUtils.USER_TYPE_CURRENT;
        public Uri contactRingtoneUri;
        /**
         * Query id to identify the query session.
         */
        int queryId;
        /**
         * The phone number without any changes to display to the user (ex: cnap...)
         */
        String originalPhoneNumber;

        boolean shouldShowLocation;

        public boolean isBusiness;
        boolean isEmergencyNumber;
        boolean isVoicemailNumber;
    }

    private static final class DialerCallCookieWrapper {
        final String callId;
        final int numberPresentation;
        final String cnapName;

        DialerCallCookieWrapper(String callId, int numberPresentation, String cnapName) {
            this.callId = callId;
            this.numberPresentation = numberPresentation;
            this.cnapName = cnapName;
        }
    }

    class PhoneNumberServiceListener implements PhoneNumberService.NumberLookupListener {

        private final String callId;
        private final int queryIdOfRemoteLookup;

        PhoneNumberServiceListener(String callId, int queryId) {
            this.callId = callId;
            queryIdOfRemoteLookup = queryId;
        }

        @Override
        public void onPhoneNumberInfoComplete(final PhoneNumberService.PhoneNumberInfo info) {
            Log.d(TAG, "PhoneNumberServiceListener.onPhoneNumberInfoComplete");
            if (!isWaitingForThisQuery(callId, queryIdOfRemoteLookup)) {
                return;
            }

            // If we got a miss, this is the end of the lookup pipeline,
            // so clear the callbacks and return.
            if (info == null) {
                Log.d(TAG, "Contact lookup done. Remote contact not found.");
                clearCallbacks(callId);
                return;
            }
            ContactCacheEntry entry = new ContactCacheEntry();
            entry.namePrimary = info.getDisplayName();
            entry.number = info.getNumber();
            entry.isBusiness = info.isBusiness();
            final int type = info.getPhoneType();
            final String label = info.getPhoneLabel();
            if (type == Phone.TYPE_CUSTOM) {
                entry.label = label;
            } else {
                final CharSequence typeStr = Phone.getTypeLabel(context.getResources(), type, label);
                entry.label = typeStr == null ? null : typeStr.toString();
            }
            final ContactCacheEntry oldEntry = infoMap.get(callId);
            if (oldEntry != null) {
                // Location is only obtained from local lookup so persist
                // the value for remote lookups. Once we have a name this
                // field is no longer used; it is persisted here in case
                // the UI is ever changed to use it.
                entry.location = oldEntry.location;
                entry.shouldShowLocation = oldEntry.shouldShowLocation;
                // Contact specific ringtone is obtained from local lookup.
                entry.contactRingtoneUri = oldEntry.contactRingtoneUri;
                entry.originalPhoneNumber = oldEntry.originalPhoneNumber;
            }

            // If no image and it's a business, switch to using the default business avatar.
            if (info.getImageUrl() == null && info.isBusiness()) {
                Log.d(TAG, "Business has no image. Using default.");
                entry.photoType = ContactPhotoType.BUSINESS;
            }

            Log.d(TAG, "put entry into map: " + entry);
            infoMap.put(callId, entry);
            sendInfoNotifications(callId, entry);

            entry.hasPendingQuery = info.getImageUrl() != null;

            // If there is no image then we should not expect another callback.
            if (!entry.hasPendingQuery) {
                // We're done, so clear callbacks
                clearCallbacks(callId);
            }
        }
    }

    private boolean needForceQuery(DialerCall call, ContactCacheEntry cacheEntry) {
        if (call == null || call.isConferenceCall()) {
            return false;
        }

        String newPhoneNumber = PhoneNumberUtils.stripSeparators(call.getNumber());
        if (cacheEntry == null) {
            // No info in the map yet so it is the 1st query
            Log.d(TAG, "needForceQuery: first query");
            return true;
        }
        String oldPhoneNumber = PhoneNumberUtils.stripSeparators(cacheEntry.originalPhoneNumber);

        if (!TextUtils.equals(oldPhoneNumber, newPhoneNumber)) {
            Log.d(TAG, "phone number has changed: " + oldPhoneNumber + " -> " + newPhoneNumber);
            return true;
        }

        return false;
    }

    private static final class CallerInfoQueryToken {
        final int queryId;
        final String callId;

        CallerInfoQueryToken(int queryId, String callId) {
            this.queryId = queryId;
            this.callId = callId;
        }
    }

    /**
     * Check if the queryId in the cached map is the same as the one from query result.
     */
    private boolean isWaitingForThisQuery(String callId, int queryId) {
        final ContactCacheEntry existingCacheEntry = infoMap.get(callId);
        if (existingCacheEntry == null) {
            // This might happen if lookup on background thread comes back before the initial entry is
            // created.
            Log.d(TAG, "Cached entry is null.");
            return true;
        } else {
            int waitingQueryId = existingCacheEntry.queryId;
            Log.d(TAG, "waitingQueryId = " + waitingQueryId + "; queryId = " + queryId);
            return waitingQueryId == queryId;
        }
    }
}
