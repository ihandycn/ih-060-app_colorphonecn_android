/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.honeycomb.colorphone.dialer;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.telecom.Call;
import android.telecom.CallAudioState;
import android.telecom.InCallService;
import android.util.Log;

import com.honeycomb.colorphone.dialer.call.CallList;
import com.honeycomb.colorphone.dialer.call.ExternalCallList;
import com.honeycomb.colorphone.dialer.call.TelecomAdapter;
import com.honeycomb.colorphone.dialer.contact.ContactInfoCache;
import com.ihs.commons.utils.HSLog;

/**
 * Used to receive updates about calls from the Telecom component. This service is bound to Telecom
 * while there exist calls which potentially require UI. This includes ringing (incoming), dialing
 * (outgoing), and active calls. When the last call is disconnected, Telecom will unbind to the
 * service triggering InCallActivity (via CallList) to finish soon after.
 */
@RequiresApi(api = Build.VERSION_CODES.M)
public class InCallServiceImpl extends InCallService {

  private ReturnToCallController returnToCallController;
  private CallList.Listener feedbackListener;
  // We only expect there to be one speakEasyCallManager to be instantiated at a time.
  // We did not use a singleton SpeakEasyCallManager to avoid holding on to state beyond the
  // lifecycle of this service, because the singleton is associated with the state of the
  // Application, not this service.

  @Override
  public void onCallAudioStateChanged(CallAudioState audioState) {
    AudioModeProvider.getInstance().onAudioStateChanged(audioState);
  }

  @Override
  public void onBringToForeground(boolean showDialpad) {
    HSLog.d("InCallServiceImpl.onBringToForeground");
    InCallPresenter.getInstance().onBringToForeground(showDialpad);
  }

  @Override
  public void onCallAdded(Call call) {
    HSLog.d("InCallServiceImpl.onCallAdded");
    InCallPresenter.getInstance().onCallAdded(call);
    if (call.getState() == Call.STATE_RINGING) {
      EventLogger.get().onIncomingCall(TelecomCallUtil.getNumber(call));
    } else {
      EventLogger.get().onOutGoing(TelecomCallUtil.getNumber(call));
    }
  }

  @Override
  public void onCallRemoved(Call call) {
    HSLog.d("InCallServiceImpl.onCallRemoved");

    InCallPresenter.getInstance().onCallRemoved(call);
    EventLogger.get().onEnd();
  }

  @Override
  public void onCanAddCallChanged(boolean canAddCall) {
    HSLog.d("InCallServiceImpl.onCanAddCallChanged");
    InCallPresenter.getInstance().onCanAddCallChanged(canAddCall);
  }

  @Override
  public void onCreate() {
    super.onCreate();
  }

  @Override
  public IBinder onBind(Intent intent) {
    HSLog.d("InCallServiceImpl.onBind");
    final Context context = getApplicationContext();
    final ContactInfoCache contactInfoCache = ContactInfoCache.getInstance(context);
    AudioModeProvider.getInstance().initializeAudioState(this);
    InCallPresenter.getInstance()
        .setUp(
            context,
            CallList.getInstance(),
            new ExternalCallList(),
            new StatusBarNotifier(context, contactInfoCache),
            new ExternalCallNotifier(context, contactInfoCache),
            contactInfoCache,
            new ProximitySensor(
                context, AudioModeProvider.getInstance(), new AccelerometerListener(context)));
    InCallPresenter.getInstance().onServiceBind();
    InCallPresenter.getInstance().maybeStartRevealAnimation(intent);
    TelecomAdapter.getInstance().setInCallService(this);
    returnToCallController =
        new ReturnToCallController(this, ContactInfoCache.getInstance(context));
//    feedbackListener = FeedbackComponent.get(context).getCallFeedbackListener();
//    CallList.getInstance().addListener(feedbackListener);
    // TODO

    IBinder iBinder = super.onBind(intent);
    return iBinder;
  }

  @Override
  public boolean onUnbind(Intent intent) {
    HSLog.d("InCallServiceImpl.onUnbind");
    super.onUnbind(intent);

    InCallPresenter.getInstance().onServiceUnbind();
    tearDown();

    return false;
  }

  private void tearDown() {
    HSLog.d("InCallServiceImpl.tearDown");
    Log.v(this.getClass().getSimpleName(), "tearDown");
    // Tear down the InCall system
    InCallPresenter.getInstance().tearDown();
    TelecomAdapter.getInstance().clearInCallService();
    if (returnToCallController != null) {
      returnToCallController.tearDown();
      returnToCallController = null;
    }
    if (feedbackListener != null) {
      CallList.getInstance().removeListener(feedbackListener);
      feedbackListener = null;
    }
  }
}
