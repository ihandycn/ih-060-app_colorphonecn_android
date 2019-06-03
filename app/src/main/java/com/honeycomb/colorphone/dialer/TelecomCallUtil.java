/*
 * Copyright (C) 2015 The Android Open Source Project
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
 * limitations under the License.
 */

package com.honeycomb.colorphone.dialer;

import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.telecom.Call;
import android.telephony.PhoneNumberUtils;


/**
 * Class to provide a standard interface for obtaining information from the underlying
 * android.telecom.Call. Much of this should be obtained through the incall.Call, but on occasion we
 * need to interact with the telecom.Call directly (eg. call blocking, before the incall.Call has
 * been created).
 */
@RequiresApi(api = Build.VERSION_CODES.M)
public class TelecomCallUtil {

  /** Returns Whether the call handle is an emergency number. */
  public static boolean isEmergencyCall(@NonNull Call call) {
    Uri handle = call.getDetails().getHandle();
    return PhoneNumberUtils.isEmergencyNumber(handle == null ? "" : handle.getSchemeSpecificPart());
  }

  /**
   * Returns The phone number which the {@code Call} is currently connected, or {@code null} if the
   * number is not available.
   */
  @Nullable
  public static String getNumber(@Nullable Call call) {
    if (call == null) {
      return null;
    }
    if (call.getDetails().getGatewayInfo() != null) {
      return call.getDetails().getGatewayInfo().getOriginalAddress().getSchemeSpecificPart();
    }
    Uri handle = getHandle(call);
    return handle == null ? null : handle.getSchemeSpecificPart();
  }

  /**
   * Returns The handle (e.g., phone number) to which the {@code Call} is currently connected, or
   * {@code null} if the number is not available.
   */
  @Nullable
  public static Uri getHandle(@Nullable Call call) {
    return call == null ? null : call.getDetails().getHandle();
  }
  
}
