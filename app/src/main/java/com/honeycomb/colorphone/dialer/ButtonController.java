/*
 * Copyright (C) 2016 The Android Open Source Project
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

import android.os.Build;
import android.support.annotation.CallSuper;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.annotation.StringRes;
import android.view.View;
import android.view.View.OnClickListener;

import com.honeycomb.colorphone.R;

/** Manages a single button. */
@RequiresApi(api = Build.VERSION_CODES.M)
interface ButtonController {

  boolean isEnabled();

  void setEnabled(boolean isEnabled);

  boolean isAllowed();

  void setAllowed(boolean isAllowed);

  void setChecked(boolean isChecked);

  @InCallButtonIds
  int getInCallButtonId();

  void setButton(CheckableLabeledButton button);

  final class Controllers {

    private static void resetButton(CheckableLabeledButton button) {
      if (button != null) {
        button.setOnCheckedChangeListener(null);
        button.setOnClickListener(null);
      }
    }
  }

  abstract class CheckableButtonController implements ButtonController, CheckableLabeledButton.OnCheckedChangeListener {

    @NonNull protected final InCallButtonUiDelegate delegate;
    @InCallButtonIds protected final int buttonId;
    @StringRes protected final int checkedDescription;
    @StringRes protected final int uncheckedDescription;
    protected boolean isEnabled;
    protected boolean isAllowed;
    protected boolean isChecked;
    protected CheckableLabeledButton button;

    protected CheckableButtonController(
        @NonNull InCallButtonUiDelegate delegate,
        @InCallButtonIds int buttonId,
        @StringRes int checkedContentDescription,
        @StringRes int uncheckedContentDescription) {
      Assert.isNotNull(delegate);
      this.delegate = delegate;
      this.buttonId = buttonId;
      this.checkedDescription = checkedContentDescription;
      this.uncheckedDescription = uncheckedContentDescription;
    }

    @Override
    public boolean isEnabled() {
      return isEnabled;
    }

    @Override
    public void setEnabled(boolean isEnabled) {
      this.isEnabled = isEnabled;
      if (button != null) {
        button.setEnabled(isEnabled);
      }
    }

    @Override
    public boolean isAllowed() {
      return isAllowed;
    }

    @Override
    public void setAllowed(boolean isAllowed) {
      this.isAllowed = isAllowed;
      if (button != null) {
        button.setVisibility(isAllowed ? View.VISIBLE : View.INVISIBLE);
      }
    }

    @Override
    public void setChecked(boolean isChecked) {
      this.isChecked = isChecked;
      if (button != null) {
        button.setChecked(isChecked);
      }
    }

    @Override
    @InCallButtonIds
    public int getInCallButtonId() {
      return buttonId;
    }

    @Override
    @CallSuper
    public void setButton(CheckableLabeledButton button) {
      Controllers.resetButton(this.button);

      this.button = button;
      if (button != null) {
        button.setEnabled(isEnabled);
        button.setVisibility(isAllowed ? View.VISIBLE : View.INVISIBLE);
        button.setChecked(isChecked);
        button.setOnClickListener(null);
        button.setOnCheckedChangeListener(this);
        button.setContentDescription(
            button.getContext().getText(isChecked ? checkedDescription : uncheckedDescription));
        button.setShouldShowMoreIndicator(false);
      }
    }

    @Override
    public void onCheckedChanged(CheckableLabeledButton checkableLabeledButton, boolean isChecked) {
      button.setContentDescription(
          button.getContext().getText(isChecked ? checkedDescription : uncheckedDescription));
      doCheckedChanged(isChecked);
    }

    protected abstract void doCheckedChanged(boolean isChecked);
  }

  abstract class SimpleCheckableButtonController extends CheckableButtonController {

    @StringRes private final int label;
    @DrawableRes private final int icon;
    @DrawableRes private final int iconPressed;

    protected SimpleCheckableButtonController(
        @NonNull InCallButtonUiDelegate delegate,
        @InCallButtonIds int buttonId,
        @StringRes int checkedContentDescription,
        @StringRes int uncheckedContentDescription,
        @StringRes int label,
        @DrawableRes int icon,
        @DrawableRes int iconPress) {
      super(
          delegate,
          buttonId,
          checkedContentDescription == 0 ? label : checkedContentDescription,
          uncheckedContentDescription == 0 ? label : uncheckedContentDescription);
      this.label = label;
      this.icon = icon;
      this.iconPressed = iconPress;
    }

    @Override
    public void onCheckedChanged(CheckableLabeledButton checkableLabeledButton, boolean isChecked) {
      super.onCheckedChanged(checkableLabeledButton, isChecked);
      if (iconPressed != 0) {
        // has pressed state.
        button.setIconDrawable(isChecked ? icon : iconPressed);
      }
    }

    @Override
    @CallSuper
    public void setButton(CheckableLabeledButton button) {
      super.setButton(button);
      if (button != null) {
        button.setLabelText(label);
        button.setIconDrawable(icon);
      }
    }
  }

  abstract class NonCheckableButtonController implements ButtonController, OnClickListener {

    protected final InCallButtonUiDelegate delegate;
    @InCallButtonIds protected final int buttonId;
    @StringRes protected final int contentDescription;
    protected boolean isEnabled;
    protected boolean isAllowed;
    protected CheckableLabeledButton button;

    protected NonCheckableButtonController(
        InCallButtonUiDelegate delegate,
        @InCallButtonIds int buttonId,
        @StringRes int contentDescription) {
      this.delegate = delegate;
      this.buttonId = buttonId;
      this.contentDescription = contentDescription;
    }

    @Override
    public boolean isEnabled() {
      return isEnabled;
    }

    @Override
    public void setEnabled(boolean isEnabled) {
      this.isEnabled = isEnabled;
      if (button != null) {
        button.setEnabled(isEnabled);
      }
    }

    @Override
    public boolean isAllowed() {
      return isAllowed;
    }

    @Override
    public void setAllowed(boolean isAllowed) {
      this.isAllowed = isAllowed;
      if (button != null) {
        button.setVisibility(isAllowed ? View.VISIBLE : View.INVISIBLE);
      }
    }

    @Override
    public void setChecked(boolean isChecked) {
      Assert.fail();
    }

    @Override
    @InCallButtonIds
    public int getInCallButtonId() {
      return buttonId;
    }

    @Override
    @CallSuper
    public void setButton(CheckableLabeledButton button) {
      Controllers.resetButton(this.button);

      this.button = button;
      if (button != null) {
        button.setEnabled(isEnabled);
        button.setVisibility(isAllowed ? View.VISIBLE : View.INVISIBLE);
        button.setChecked(false);
        button.setOnCheckedChangeListener(null);
        button.setOnClickListener(this);
        button.setContentDescription(button.getContext().getText(contentDescription));
        button.setShouldShowMoreIndicator(false);
      }
    }
  }

  abstract class SimpleNonCheckableButtonController extends NonCheckableButtonController {

    @StringRes private final int label;
    @DrawableRes private final int icon;

    protected SimpleNonCheckableButtonController(
        InCallButtonUiDelegate delegate,
        @InCallButtonIds int buttonId,
        @StringRes int contentDescription,
        @StringRes int label,
        @DrawableRes int icon) {
      super(delegate, buttonId, contentDescription == 0 ? label : contentDescription);
      this.label = label;
      this.icon = icon;
    }

    @Override
    @CallSuper
    public void setButton(CheckableLabeledButton button) {
      super.setButton(button);
      if (button != null) {
        button.setLabelText(label);
        button.setIconDrawable(icon);
      }
    }
  }

  class MuteButtonController extends SimpleCheckableButtonController {

    public MuteButtonController(InCallButtonUiDelegate delegate) {
      super(
              delegate,
              InCallButtonIds.BUTTON_MUTE,
              R.string.incall_content_description_muted,
              R.string.incall_content_description_unmuted,
              R.string.incall_label_mute,
              R.drawable.incall_mute_normal,
              R.drawable.incall_mute_pressed);
    }

    @Override
    public void doCheckedChanged(boolean isChecked) {
      delegate.muteClicked(isChecked, true /* clickedByUser */);
    }
  }

  class SpeakerButtonController extends SimpleCheckableButtonController {

    public SpeakerButtonController(InCallButtonUiDelegate delegate) {
      super(
              delegate,
              InCallButtonIds.BUTTON_AUDIO_SPEAKER,
              R.string.incall_content_description_muted,
              R.string.incall_content_description_unmuted,
              R.string.incall_content_description_speaker,
              R.drawable.incall_speaker_normal,
              R.drawable.incall_speaker_pressed);
    }

    @Override
    public void doCheckedChanged(boolean isChecked) {
      delegate.speakerClicked(isChecked, true /* clickedByUser */);
    }
  }

  class BluetoothButtonController extends SimpleCheckableButtonController {

    public BluetoothButtonController(InCallButtonUiDelegate delegate) {
      super(
              delegate,
              InCallButtonIds.BUTTON_AUDIO_SPEAKER,
              R.string.incall_content_description_muted,
              R.string.incall_content_description_unmuted,
              R.string.incall_content_description_speaker,
              R.drawable.incall_bluetooth_normal,
              R.drawable.incall_bluetooth_pressed);
    }

    @Override
    public void doCheckedChanged(boolean isChecked) {
      delegate.bluetoothClicked(isChecked, true /* clickedByUser */);
    }
  }


  class DialpadButtonController extends SimpleCheckableButtonController {

    public DialpadButtonController(@NonNull InCallButtonUiDelegate delegate) {
      super(
          delegate,
          InCallButtonIds.BUTTON_DIALPAD,
          0,
          0,
          R.string.incall_label_dialpad,
          R.drawable.incall_dialpad,
              0);
    }

    @Override
    public void doCheckedChanged(boolean isChecked) {
      delegate.showDialpadClicked(isChecked);
    }
  }


}
