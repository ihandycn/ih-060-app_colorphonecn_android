package colorphone.acb.com.libscreencard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

import com.ihs.commons.utils.HSLog;
import com.superapps.util.Dimensions;
import com.superapps.util.Networks;
import com.superapps.util.Preferences;

import colorphone.acb.com.libscreencard.game.GameCardHelper;
import colorphone.acb.com.libscreencard.game.GameManager;
import colorphone.acb.com.libscreencard.gif.AutoPilotUtils;
import colorphone.acb.com.libscreencard.gif.CardConfig;
import colorphone.acb.com.libscreencard.gif.GifCacheUtils;

@Deprecated
public class CustomizeContentContainer extends FrameLayout {

    private static final String TAG = CustomizeContentContainer.class.getSimpleName();
    private static final int RISE_OFFSET = Dimensions.pxFromDp(30);
    private boolean mVisible;
    private boolean DEBUG_MODE = true;
    private Runnable mDismissCallback;
    private CardDisplayListener mCardDisplayListener;
    public void setDismissCallback(Runnable dismissCallback) {
        mDismissCallback = dismissCallback;
    }

    public void addCardDisplayListener(CardDisplayListener displayListener) {
        mCardDisplayListener = displayListener;
    }

    public enum ContentType {
        GAME,
        GIF,
        FM_GAME,
    }

    private ContentType mCurrentType;

    private enum AutopilotRecommendCard {
        PHONE_ISSUE,
        GAME,
        GIF,
        ALL,
        NONE,
    }

    private static final long NOT_START = -1L;

    private ValueAnimator mRiseAnimator;

    private int mRecommendInterval;
    private long mStartShowTime;
    private boolean mClicked;
    private AutopilotRecommendCard mAutopilotRecommendCardType = AutopilotRecommendCard.NONE;

    public CustomizeContentContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setClipChildren(false);
        initAnim();
        initAutopilotData();
        GameManager.getInstance().init();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        HSLog.d(TAG, "onAttachedToWindow");
        GifCacheUtils.cacheGif();

        // TODO
        mRecommendInterval = 0;
        mStartShowTime = Preferences.get(CardConfig.CARD_MODULE_PREFS).getLong(CardConfig.PREF_KEY_START_SHOW_TIME, NOT_START);
        mClicked = Preferences.get(CardConfig.CARD_MODULE_PREFS).getBoolean(CardConfig.PREF_KEY_CONTENT_CLICKED, false);
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        boolean isScreenOn = ScreenStatus.isScreenOn();
        boolean visible = isScreenOn && (visibility == VISIBLE);
        if (visible != mVisible) {
            mVisible = visible;
        }
    }

    public void onVisibilityChange(boolean visibility) {
        if (visibility) {
            onVisible();
        } else {
            onInVisible();
        }
    }

    void notifyClicked() {
        mClicked = true;
        Preferences.get(CardConfig.CARD_MODULE_PREFS).putBoolean(CardConfig.PREF_KEY_CONTENT_CLICKED, mClicked);
        removeAllViews();
        Enum currentType = getCurrentType();
        CardCustomConfig.getLogger().logEvent("RecommendCard_Click", "Type", currentType.name());
    }

    private void initAnim() {
        mRiseAnimator = ValueAnimator.ofFloat(0f, 1f);
        mRiseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mRiseAnimator.setDuration(250);
        mRiseAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                View child = CustomizeContentContainer.this.getChildAt(0);
                if (child != null) {
                    child.setTranslationY((RISE_OFFSET + CustomizeContentContainer.this.getHeight()) * (1 - animation.getAnimatedFraction()));
                } else {
                    mRiseAnimator.cancel();
                }
            }
        });
        mRiseAnimator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                View child = getChildAt(0);
                if (child != null) {
                    child.setTranslationY(0);
                }
            }
        });
    }

    private void initAutopilotData() {
        String recommendCard = AutoPilotUtils.getSecurityProtectionRecommendCard();
        if (TextUtils.equals(recommendCard, "phoneproblem")) {
            mAutopilotRecommendCardType = AutopilotRecommendCard.PHONE_ISSUE;
        } else if (TextUtils.equals(recommendCard, "game")) {
            mAutopilotRecommendCardType = AutopilotRecommendCard.GAME;
        } else if (TextUtils.equals(recommendCard, "gif")) {
            mAutopilotRecommendCardType = AutopilotRecommendCard.GIF;
        } else if (TextUtils.equals(recommendCard, "none")) {
            mAutopilotRecommendCardType = AutopilotRecommendCard.NONE;
        } else if (TextUtils.equals(recommendCard, "all")) {
            mAutopilotRecommendCardType = AutopilotRecommendCard.ALL;
        }
    }

    private void onVisible() {
        HSLog.d(TAG, "onVisible");
        if (Networks.isNetworkAvailable(-1)) {
            showOrSwitchContent();
        }
    }

    private void onInVisible() {
        HSLog.d(TAG, "onInVisible");
        removeAllViews();
    }

    private void showOrSwitchContent() {
        HSLog.d(TAG, "mStartShowTime: " + mStartShowTime + " mClicked: " + mClicked);
        if (mStartShowTime == NOT_START &&
                !mClicked) { // Have not shown & clicked
            Enum currentValidType = getCurrentValidType();
            if (currentValidType != null) {
                mStartShowTime = System.currentTimeMillis();
                Preferences.get(CardConfig.CARD_MODULE_PREFS).putLong(CardConfig.PREF_KEY_START_SHOW_TIME, mStartShowTime);
            }
            showContent(currentValidType);
            HSLog.d(TAG, "ShowContent: " + currentValidType);
        } else if (mStartShowTime != NOT_START &&
                !mClicked) { // Already shown
            Enum currentValidType = null;
            ContentType before = getCurrentValidType();
            if (timeIntervalValid()) { // Overshot interval, Switch to next type
                onSwitchContent(before);
                move2Next();
                currentValidType = getCurrentValidType();
                if (currentValidType != null) {
                    mStartShowTime = System.currentTimeMillis();
                } else {
                    mStartShowTime = NOT_START;
                }
                Preferences.get(CardConfig.CARD_MODULE_PREFS).putLong(CardConfig.PREF_KEY_START_SHOW_TIME, mStartShowTime);
                HSLog.d(TAG, "switchContent: " + currentValidType);
            } else {
                currentValidType = getCurrentValidType();
                HSLog.d(TAG, "ShowContent: " + currentValidType);
            }
            showContent(currentValidType);
        } else if (mClicked && timeIntervalValid()) { // Clicked & Overshot interval
            Enum before = getCurrentValidType();
            onSwitchContent(before);
            move2Next();
            mClicked = false;
            Preferences.get(CardConfig.CARD_MODULE_PREFS).putBoolean(CardConfig.PREF_KEY_CONTENT_CLICKED, mClicked);
            Enum currentValidType = getCurrentValidType();
            if (currentValidType != null) {
                mStartShowTime = System.currentTimeMillis();
            } else {
                mStartShowTime = NOT_START;
            }
            Preferences.get(CardConfig.CARD_MODULE_PREFS).putLong(CardConfig.PREF_KEY_START_SHOW_TIME, mStartShowTime);
            HSLog.d(TAG, "switchContent: " + currentValidType);
            showContent(currentValidType);
        }
    }

    private boolean timeIntervalValid() {
        if (DEBUG_MODE) {
            return (System.currentTimeMillis() - mStartShowTime) > 5000;
        }
        return (System.currentTimeMillis() - mStartShowTime) / 1000 / 60 >= mRecommendInterval;
    }

    private void onSwitchContent(Enum before) {
        HSLog.d(TAG, "onSwitchContent before: " + before);
        if (before == ContentType.GIF) {
            GifCacheUtils.increaseCurrentViewedGifKey();
            GifCacheUtils.cacheGif();
        }
    }

    private void showContent(Enum type) {
        setBackgroundColor(Color.TRANSPARENT);
        if (type == null) {
            return;
        }
        View currentContent = getCurrentContent(type);
        if (currentContent == null) {
            return;
        }
        currentContent.setTranslationY(RISE_OFFSET + getHeight());
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        removeAllViews();
        addView(currentContent, lp);
        if (type == ContentType.GAME) {
            Preferences.get(CardConfig.CARD_MODULE_PREFS).putLong(CardConfig.PREF_KEY_GAME_SHOW_TIME, System.currentTimeMillis());
        } else if (type == ContentType.FM_GAME) {
            Preferences.get(CardConfig.CARD_MODULE_PREFS).putLong(CardConfig.PREF_KEY_FM_GAME_SHOW_TIME, System.currentTimeMillis());
        }

        if (mRiseAnimator.isRunning()) {
            mRiseAnimator.cancel();
        }
        mRiseAnimator.start();
        if (mCardDisplayListener != null) {
            mCardDisplayListener.onCardDisplay(type.ordinal());
        }
    }

    private @Nullable
    ContentType getCurrentValidType() {
        if (mCurrentType != null) {
            return mCurrentType;
        }

        for (int i = 0; i < ContentType.values().length; i++) {
            if (!validType(getCurrentType())) {
                move2Next();
                if (i == ContentType.values().length - 1) {
                    return null;
                }
            } else {
                break;
            }
        }

        mCurrentType = getCurrentType();
        return mCurrentType;
    }

    public ContentType getCurrentType() {
        ContentType currentContentType = ContentType.valueOf(Preferences.get(CardConfig.CARD_MODULE_PREFS).getString(CardConfig.PREF_KEY_CONTENT_TYPE_CURSOR, "GAME"));
        return currentContentType;
    }

    private View getCurrentContent(Enum type) {
        CardCustomConfig.getLogger().logEvent("RecommendCard_Show", "Type", type.name());
        return null;
    }

    private void dismiss() {
        if (mDismissCallback != null) {
            mDismissCallback.run();
        }
    }

    private boolean validType(Enum type) {
        if (type == ContentType.FM_GAME) {
            long lastShowTime = Preferences.get(CardConfig.CARD_MODULE_PREFS).getLong(CardConfig.PREF_KEY_FM_GAME_SHOW_TIME, 0);
            if (System.currentTimeMillis() - lastShowTime < CardConfig.GAME_FM_SHOW_INTERVAL_MIN_HOUR * DateUtils.HOUR_IN_MILLIS) {
                GameCardHelper.debugToast(type.name(), "Time interval valid");
                return false;
            }
            return Networks.isNetworkAvailable(-1);

        } else if (type == ContentType.GAME) {
            long lastShowTime = Preferences.get(CardConfig.CARD_MODULE_PREFS).getLong(CardConfig.PREF_KEY_GAME_SHOW_TIME, 0);
            if (System.currentTimeMillis() - lastShowTime < CardConfig.GAME_SHOW_INTERVAL_MIN_HOUR * DateUtils.HOUR_IN_MILLIS) {
                GameCardHelper.debugToast(type.name(), "Time interval valid");
                return false;
            }

            boolean isGameCached = GameManager.getInstance().isGameReady();
            if (!isGameCached) {
                GameCardHelper.debugToast(type.name(), "game Zip not cached");
            }
            boolean isNetworkAvailable = Networks.isNetworkAvailable(-1);

            return isNetworkAvailable && isGameCached;
        } else if (type == ContentType.GIF) {

            boolean cached = GifCacheUtils.haveValidCached();
            HSLog.d(TAG, "Gif valid: " + (cached));
            GameCardHelper.debugToast(type.name(), "Gif not cached");

            return cached;
        }
        return false;
    }

    private void move2Next() {
        Enum before = getCurrentType();
        int ordinal = 0;
        ordinal = before.ordinal();
        mCurrentType = null;
        ContentType[] contentTypes = ContentType.values();
        ordinal = (ordinal + 1) % contentTypes.length;
        Preferences.get(CardConfig.CARD_MODULE_PREFS).putString(CardConfig.PREF_KEY_CONTENT_TYPE_CURSOR, contentTypes[ordinal].name());
        Enum after = getCurrentType();
        HSLog.d(TAG, "move from: " + before + " to: " + after);
    }

    public interface CardDisplayListener {
        void onCardDisplay(int type);
    }
}
