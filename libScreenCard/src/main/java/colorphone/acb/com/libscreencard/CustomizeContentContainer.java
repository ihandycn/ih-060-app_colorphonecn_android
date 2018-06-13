package colorphone.acb.com.libscreencard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Commons;
import com.superapps.util.Dimensions;
import com.superapps.util.Navigations;
import com.superapps.util.Networks;
import com.superapps.util.Preferences;

import net.appcloudbox.h5game.AcbH5GameInfo;

import colorphone.acb.com.libscreencard.game.GameManager;
import colorphone.acb.com.libscreencard.gif.AutoPilotUtils;
import colorphone.acb.com.libscreencard.gif.GifCacheUtils;
import colorphone.acb.com.libscreencard.gif.GifCenterActivity;
import colorphone.acb.com.libscreencard.gif.CardConfig;

public class CustomizeContentContainer extends FrameLayout {

    private static final String TAG = CustomizeContentContainer.class.getSimpleName();
    private static final int RISE_OFFSET = Dimensions.pxFromDp(30);
    private boolean mVisible;
    private boolean DEBUG_MODE = true;
    private Runnable mDismissCallback;

    public void setDismissCallback(Runnable dismissCallback) {
        mDismissCallback = dismissCallback;
    }


    public enum ContentType {
        GAME,
        GIF,
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
        GameManager.getInstance().prepare();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        HSLog.d(TAG, "onAttachedToWindow");
        GifCacheUtils.cacheGif();

        // TODO
        mRecommendInterval = 6;
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
        }

        if (mRiseAnimator.isRunning()) {
            mRiseAnimator.cancel();
        }
        mRiseAnimator.start();
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
        if (type == ContentType.GAME && AutoPilotUtils.gameCardEnable()) {
            View gameCard = View.inflate(getContext(), R.layout.sc_layout_card_game_issue_custom, null);
            ImageView imageView = gameCard.findViewById(R.id.security_protection_card_game_issue_bg);
            TextView titleTv = gameCard.findViewById(R.id.security_protection_card_game_issue_title);
            TextView subTitleTv = gameCard.findViewById(R.id.security_protection_card_game_issue_subtitle);
            imageView.setImageResource(R.drawable.game_card_bg_basketball);
            AcbH5GameInfo gameInfo = GameManager.getInstance().getBasketBallInfo();
            // Use local for test.
            titleTv.setText(getContext().getString(R.string.game_card_title));
            subTitleTv.setText(getContext().getString(R.string.game_card_desc));

            gameCard.findViewById(R.id.container_view).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    AutoPilotUtils.gameClick();
                    CardCustomConfig.getLogger().logEvent("Colorphone_Charging_View_Game_Card_Clicked");

                    CustomizeContentContainer.this.dismiss();
                    GameManager.getInstance().startGame();
                }
            });
            AutoPilotUtils.gameShow();
            CardCustomConfig.getLogger().logEvent("Colorphone_Charging_View_Game_Card_Show");
            return gameCard;

        } else if (type == ContentType.GIF && AutoPilotUtils.gifCardEnable()) {

            View gifCard = View.inflate(getContext(), R.layout.sc_layout_card_gif, null);
            gifCard.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(HSApplication.getContext(), GifCenterActivity.class);
                            intent.putExtra(GifCenterActivity.INTENT_EXTRA_DATA_KEY_INIT_POSITION, GifCacheUtils.getCurrentViewedGifKey() - 1);
                            Navigations.startActivitySafely(CustomizeContentContainer.this.getContext(), intent);
                        }
                    };
                    if (Commons.isKeyguardLocked(CustomizeContentContainer.this.getContext(), false)) {
                        ScreenStatus.setPresentRunnable(runnable);
                    } else {
                        runnable.run();
                    }

                    CustomizeContentContainer parent = (CustomizeContentContainer) view.getParent();
                    parent.notifyClicked();
                    AutoPilotUtils.logRecommendCardGIFClick();
                    CardCustomConfig.getLogger().logEvent("Colorphone_Charging_View_Gif_Card_Clicked");
                    CustomizeContentContainer.this.dismiss();

                }
            });
            View more = gifCard.findViewById(R.id.more);
            Drawable backgroundDrawable = BackgroundDrawables.createBackgroundDrawable(0x80000000, Dimensions.pxFromDp(14), false);
            more.setBackgroundDrawable(backgroundDrawable);
            TextureVideoView videoView = gifCard.findViewById(R.id.video_view);
            videoView.setVideoPath(GifCacheUtils.getCachedGifPath());
            videoView.play();
            GifCacheUtils.markCachedGifViewedState(true);
            AutoPilotUtils.logRecommendCardGIFShow();
            CardCustomConfig.getLogger().logEvent("Colorphone_Charging_View_Gif_Card_Show");
            return gifCard;
        }
        return null;
    }

    private void dismiss() {
        if (mDismissCallback != null) {
            mDismissCallback.run();
        }
    }

    private boolean validType(Enum type) {
        if (type == ContentType.GAME) {
            long lastShowTime = Preferences.get(CardConfig.CARD_MODULE_PREFS).getLong(CardConfig.PREF_KEY_GAME_SHOW_TIME, 0);
            if (System.currentTimeMillis() - lastShowTime < CardConfig.GAME_SHOW_INTERVAL_MIN_HOUR * DateUtils.HOUR_IN_MILLIS) {
                return false;
            }

            boolean isGameCached = GameManager.getInstance().isGameReady();

            boolean isAutopilotSatisfied = mAutopilotRecommendCardType == AutopilotRecommendCard.GAME ||
                    mAutopilotRecommendCardType == AutopilotRecommendCard.ALL;
            boolean isNetworkAvailable = Networks.isNetworkAvailable(-1);

            return isAutopilotSatisfied && isNetworkAvailable && isGameCached;
        } else if (type == ContentType.GIF) {
            boolean isAutopilotSatisfied = mAutopilotRecommendCardType == AutopilotRecommendCard.ALL
                    || mAutopilotRecommendCardType == AutopilotRecommendCard.GIF;
            boolean cached = GifCacheUtils.haveValidCached();
            HSLog.d(TAG, "Gif valid: " + (cached && isAutopilotSatisfied));
            return cached && isAutopilotSatisfied;
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
}
