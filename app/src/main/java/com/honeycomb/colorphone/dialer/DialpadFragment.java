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

package com.honeycomb.colorphone.dialer;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.telephony.PhoneNumberUtils;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.dialer.dialpadview.DialpadKeyButton;
import com.honeycomb.colorphone.dialer.dialpadview.DialpadView;
import com.superapps.util.Dimensions;

import java.util.Map;

/** Fragment for call control buttons */
@RequiresApi(api = Build.VERSION_CODES.M)
public class DialpadFragment implements DialpadPresenter.DialpadUi,
        View.OnKeyListener,
        View.OnClickListener,
        DialpadKeyButton.OnPressedListener, ValueAnimator.AnimatorUpdateListener {


  public static String TAG = "DialpadFragment";
  /** Hash Map to map a view id to a character */
  private static final Map<Integer, Character> displayMap = new ArrayMap<>();

  /** Set up the static maps */
  static {
    // Map the buttons to the display characters
    displayMap.put(R.id.one, '1');
    displayMap.put(R.id.two, '2');
    displayMap.put(R.id.three, '3');
    displayMap.put(R.id.four, '4');
    displayMap.put(R.id.five, '5');
    displayMap.put(R.id.six, '6');
    displayMap.put(R.id.seven, '7');
    displayMap.put(R.id.eight, '8');
    displayMap.put(R.id.nine, '9');
    displayMap.put(R.id.zero, '0');
    displayMap.put(R.id.pound, '#');
    displayMap.put(R.id.star, '*');
  }

  private final int[] buttonIds =
          new int[] {
                  R.id.zero,
                  R.id.one,
                  R.id.two,
                  R.id.three,
                  R.id.four,
                  R.id.five,
                  R.id.six,
                  R.id.seven,
                  R.id.eight,
                  R.id.nine,
                  R.id.star,
                  R.id.pound
          };
  private EditText dtmfDialerField;
  // KeyListener used with the "dialpad digits" EditText widget.
  private DtmfKeyListener dtmfKeyListener;
  private DialpadView dialpadView;
  private int currentTextColor;
  private View endCallSpace;
  private boolean shouldShowEndCallSpace = true;
  private DialpadPresenter mPresenter;

  private InCallActivity mActivity;
  private ViewGroup mContainer;
  private DialpadSlidingLinearLayout mRootView;


  public void init(InCallActivity inCallActivity) {
    mPresenter = new DialpadPresenter();
    mActivity = inCallActivity;
  }

  @Override
  public void onClick(View v) {
    if (v.getId() == R.id.dialpad_back) {
      getActivity().onBackPressed();
    }
  }

  @Override
  public boolean onKey(View v, int keyCode, KeyEvent event) {
    LogUtil.d("", "onKey:  keyCode " + keyCode + ", view " + v);

    if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
      int viewId = v.getId();
      if (displayMap.containsKey(viewId)) {
        switch (event.getAction()) {
          case KeyEvent.ACTION_DOWN:
            if (event.getRepeatCount() == 0) {
              getPresenter().processDtmf(displayMap.get(viewId));
            }
            break;
          case KeyEvent.ACTION_UP:
            getPresenter().stopDtmf();
            break;
          default: // fall out
        }
        // do not return true [handled] here, since we want the
        // press / click animation to be handled by the framework.
      }
    }
    return false;
  }


  public DialpadPresenter getPresenter() {
    return mPresenter;
  }

  public DialpadPresenter.DialpadUi getUi() {
    return this;
  }

  // TODO(klp) Adds hardware keyboard listener
  public View onCreateView(
          @NonNull InCallActivity activity, @NonNull ViewGroup container) {
    mActivity = activity;
    Context contextThemeWrapper = new ContextThemeWrapper(mActivity, R.style.Dialer_ThemeBase);
    LayoutInflater layoutInflater = mActivity.getLayoutInflater().cloneInContext(contextThemeWrapper);
    final View parent = layoutInflater.inflate(R.layout.incall_dialpad_fragment, container, false);
    mContainer = container;
    underneathNavigationBar(container);

    mRootView = (DialpadSlidingLinearLayout) parent;
    dialpadView = (DialpadView) parent.findViewById(R.id.dialpad_view);
    dialpadView.setCanDigitsBeEdited(false);
    dtmfDialerField = (EditText) parent.findViewById(R.id.digits);
    if (dtmfDialerField != null) {
      LogUtil.i("DialpadFragment.onCreateView", "creating dtmfKeyListener");
      dtmfKeyListener = new DtmfKeyListener(getPresenter());
      dtmfDialerField.setKeyListener(dtmfKeyListener);
      // remove the long-press context menus that support
      // the edit (copy / paste / select) functions.
      dtmfDialerField.setLongClickable(false);
      dtmfDialerField.setElegantTextHeight(false);
      configureKeypadListeners();
    }
    View backButton = dialpadView.findViewById(R.id.dialpad_back);
    backButton.setVisibility(View.VISIBLE);
    backButton.setOnClickListener(this);
    endCallSpace = dialpadView.findViewById(R.id.end_call_space);

    mPresenter = new DialpadPresenter();
    mPresenter.onUiReady(this);

    return parent;
  }

  public void underneathNavigationBar(View container) {
    ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mContainer.getLayoutParams();
    marginLayoutParams.bottomMargin = - Dimensions.getNavigationBarHeight(getActivity());
  }

  public void onResume() {
    updateColors();
    endCallSpace.setVisibility(shouldShowEndCallSpace ? View.VISIBLE : View.GONE);
  }


  public void updateColors() {
    int textColor = Color.parseColor("#ff3f3f3f");

    if (currentTextColor == textColor) {
      return;
    }

    DialpadKeyButton dialpadKey;
    for (int i = 0; i < buttonIds.length; i++) {
      dialpadKey = (DialpadKeyButton) dialpadView.findViewById(buttonIds[i]);
      ((TextView) dialpadKey.findViewById(R.id.dialpad_key_number)).setTextColor(textColor);
    }

    currentTextColor = textColor;
  }

  public void onDestroyView() {
    if (mPresenter != null) {
      mPresenter.onUiUnready(this);
    }
    dtmfKeyListener = null;
  }


  private Activity getActivity() {
    return mActivity;
  }

  /**
   * Getter for Dialpad text.
   *
   * @return String containing current Dialpad EditText text.
   */
  public String getDtmfText() {
    return dtmfDialerField.getText().toString();
  }

  /**
   * Sets the Dialpad text field with some text.
   *
   * @param text Text to set Dialpad EditText to.
   */
  public void setDtmfText(String text) {
    dtmfDialerField.setText(PhoneNumberUtils.createTtsSpannable(text));
  }

  /** Starts the slide up animation for the Dialpad keys when the Dialpad is revealed. */
  public void animateShowDialpad() {
    dialpadView.animateShow();
  }

  public void appendDigitsToField(char digit) {
    if (dtmfDialerField != null) {
      // TODO: maybe *don't* manually append this digit if
      // mDialpadDigits is focused and this key came from the HW
      // keyboard, since in that case the EditText field will
      // get the key event directly and automatically appends
      // whetever the user types.
      // (Or, a cleaner fix would be to just make mDialpadDigits
      // *not* handle HW key presses.  That seems to be more
      // complicated than just setting focusable="false" on it,
      // though.)
      dtmfDialerField.getText().append(digit);
    }
  }

  /** Called externally (from InCallScreen) to play a DTMF Tone. */
  /* package */ boolean onDialerKeyDown(KeyEvent event) {
    Log.d(TAG, "Notifying dtmf key down.");
    if (dtmfKeyListener != null) {
      return dtmfKeyListener.onKeyDown(event);
    } else {
      return false;
    }
  }

  /** Called externally (from InCallScreen) to cancel the last DTMF Tone played. */
  public boolean onDialerKeyUp(KeyEvent event) {
    Log.d(TAG, "Notifying dtmf key up.");
    if (dtmfKeyListener != null) {
      return dtmfKeyListener.onKeyUp(event);
    } else {
      return false;
    }
  }

  private void configureKeypadListeners() {
    DialpadKeyButton dialpadKey;
    for (int i = 0; i < buttonIds.length; i++) {
      dialpadKey = (DialpadKeyButton) dialpadView.findViewById(buttonIds[i]);
      dialpadKey.setOnKeyListener(this);
      dialpadKey.setOnClickListener(this);
      dialpadKey.setOnPressedListener(this);
    }
  }

  @Override
  public void onPressed(View view, boolean pressed) {
    if (pressed && displayMap.containsKey(view.getId())) {
      Log.d(TAG, "onPressed: " + pressed + " " + displayMap.get(view.getId()));
      getPresenter().processDtmf(displayMap.get(view.getId()));
    }
    if (!pressed) {
      Log.d(TAG, "onPressed: " + pressed);
      getPresenter().stopDtmf();
    }
  }

  public void setShouldShowEndCallSpace(boolean show) {
    shouldShowEndCallSpace = show;
  }

  public boolean isVisible() {
    return mRootView != null
            && mRootView.isAttachedToWindow()
            && mRootView.getVisibility() == View.VISIBLE;
  }

  void attach() {
    mContainer.addView(mRootView);
  }

  void detach() {
    mContainer.removeView(mRootView);
  }

  public void startAnimation(ValueAnimator animation) {
    animation.removeAllUpdateListeners();
    animation.addUpdateListener(this);
    animation.start();
  }

  @Override
  public void onAnimationUpdate(ValueAnimator animation) {
    mRootView.setYFraction((Float) animation.getAnimatedValue());
  }

  /**
   * LinearLayout with getter and setter methods for the translationY property using floats, for
   * animation purposes.
   */
  public static class DialpadSlidingLinearLayout extends FrameLayout {

    private int navBarHeight;
    private View navBarCover;

    public DialpadSlidingLinearLayout(Context context) {
      super(context);
    }

    public DialpadSlidingLinearLayout(Context context, AttributeSet attrs) {
      super(context, attrs);
    }

    public DialpadSlidingLinearLayout(Context context, AttributeSet attrs, int defStyle) {
      super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
      super.onFinishInflate();

      init();

      View dialpadView = findViewById(R.id.dialpad_view);
      dialpadView.setPadding(0, 0 ,0 , navBarHeight);

      navBarCover = findViewById(R.id.nav_bottom_cover);
      ViewGroup.LayoutParams lp = navBarCover.getLayoutParams();
      lp.height = navBarHeight;
      navBarCover.setLayoutParams(lp);
    }

    private void init() {
      navBarHeight = Dimensions.getNavigationBarHeight(getContext());
    }

    public float getYFraction() {
      final int height = getHeight();
      if (height == 0) {
        return 0;
      }
      return (getTranslationY()) / height;
    }

    public void setYFraction(float yFraction) {
      float transY = yFraction * getHeight();
      float transYCover = transY -(getHeight() - navBarHeight);
      if (transYCover < 0f) {
        transYCover = -transY;
      }
      navBarCover.setTranslationY(transYCover);
      setTranslationY(transY);
    }
  }
}
