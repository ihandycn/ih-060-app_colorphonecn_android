package com.honeycomb.colorphone.wallpaper.customize.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.LauncherAnalytics;
import com.honeycomb.colorphone.WallpaperAnalytics;
import com.honeycomb.colorphone.LauncherApplication;
import com.honeycomb.colorphone.wallpaper.customize.CustomizeService;
import com.honeycomb.colorphone.wallpaper.customize.OverlayInstaller;
import com.honeycomb.colorphone.wallpaper.customize.WallpaperInfo;
import com.honeycomb.colorphone.wallpaper.customize.WallpaperMgr;
import com.honeycomb.colorphone.wallpaper.customize.util.BottomNavigationViewHelper;
import com.honeycomb.colorphone.wallpaper.customize.view.CustomizeContentView;
import com.honeycomb.colorphone.wallpaper.customize.view.LayoutWrapper;
import com.honeycomb.colorphone.wallpaper.customize.view.OnlineWallpaperPage;
import com.honeycomb.colorphone.wallpaper.customize.wallpaper.WallpaperAwardDialog;
import com.honeycomb.colorphone.wallpaper.dialog.CustomAlertActivity;
import com.honeycomb.colorphone.wallpaper.theme.ThemeConstants;
import com.honeycomb.colorphone.wallpaper.util.CommonUtils;
import com.honeycomb.colorphone.wallpaper.util.FeatureStats;
import com.honeycomb.colorphone.wallpaper.util.ViewUtils;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.superapps.util.Dimensions;
import com.superapps.util.Fonts;
import com.superapps.util.HomeKeyWatcher;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Main activity for customize center.
 */
public class CustomizeActivity extends BaseCustomizeActivity
        implements INotificationObserver, OverlayInstaller, BottomNavigationView.OnNavigationItemSelectedListener {

    public static final String SCREEN_INTERACTIVE = "screen_interactivie";
    public static final String INTENT_EXTRA_SHOULD_SHOW_INTERSTITIAL_AD = "intent_extra_should_show_interstitial_ad";
    public static final String INTENT_EXTRA_IS_AUTO_START = "intent_extra_is_auto_start";

    public static final int REQUEST_CODE_SYSTEM_THEME_ALERT = 1;
    public static final int REQUEST_CODE_PICK_WALLPAPER = 2;
    public static final int REQUEST_CODE_APPLY_3D_WALLPAPER = 3;

    public static final String NOTIFICATION_CUSTOMIZE_ACTIVITY_ONPAUSE = "wallpaper_notification_customize_activity_onpause";
    public static final String NOTIFICATION_CUSTOMIZE_ACTIVITY_ONRESUME = "wallpaper_notification_customize_activity_onresume";
    public static final String NOTIFICATION_CUSTOMIZE_ACTIVITY_DESTROY = "wallpaper_notification_customize_activity_onDestroy";
    public static final String PREF_KEY_THEME_LAUNCHED_FROM_SHORTCUT = "wallpaper_theme_launched_from_shortcut";

    private static final SparseIntArray ITEMS_INDEX_MAP = new SparseIntArray(2);
    private static final SparseArray<String> ITEMS_FLURRY_NAME_MAP = new SparseArray<>(2);

    public static final int TAB_INDEX_WALLPAPER = 0;
    public static final int TAB_INDEX_LOCAL = 1;

    static {
        ITEMS_INDEX_MAP.put(R.id.customize_bottom_bar_wallpapers, TAB_INDEX_WALLPAPER);
        ITEMS_INDEX_MAP.put(R.id.customize_bottom_bar_local, TAB_INDEX_LOCAL);

        ITEMS_FLURRY_NAME_MAP.put(R.id.customize_bottom_bar_wallpapers, "Wallpaper");
        ITEMS_FLURRY_NAME_MAP.put(R.id.customize_bottom_bar_local, "Mine");
    }

    private Intent mIntent;

    private CustomizeContentView mContent;
    private BottomNavigationView mBottomBar;

    private List<ActivityResultHandler> mActivityResultHandlers = new ArrayList<>(1);
    private LayoutWrapper mLayoutWrapper;

    private int mViewIndex;
    public int mWallpaperTabIndex;
    public String mWallpaperTabItemName;
    public String mOpenFromSrc = "";
    private boolean mLastInteractiveState;
    private HomeKeyWatcher mHomeKeyWatcher;
    private boolean mFirstCome;
    private int[] mNormalBottomIcons = new int[]{
            R.drawable.customize_wallpaper,
            R.drawable.customize_me};

    private int[] mHighlightBottomIcons = new int[]{
            R.drawable.customize_wallpaper_h,
            R.drawable.customize_me_h};

    public static Intent getLaunchIntent(Context context, String flurryFrom, int tabSelected) {
        return getLaunchIntent(context, flurryFrom, tabSelected, false);
    }

    public static Intent getLaunchIntent(Context context, String flurryFrom, int tabSelected, boolean showInterstitialAd) {
        Intent intent = new Intent(context, CustomizeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(ThemeConstants.INTENT_KEY_FLURRY_FROM, flurryFrom);
        intent.putExtra(ThemeConstants.INTENT_KEY_TAB, tabSelected);
        intent.putExtra(INTENT_EXTRA_SHOULD_SHOW_INTERSTITIAL_AD, showInterstitialAd);
        if ("Auto start".equals(flurryFrom)) {
            intent.putExtra(INTENT_EXTRA_IS_AUTO_START, true);
        }
        return intent;
    }

    public static void bindScrollListener(Context context, RecyclerView recyclerView, boolean hasBottom) {
        if (context instanceof CustomizeActivity) {
            ((CustomizeActivity) context).getLayoutWrapper().attachToRecyclerView(recyclerView, hasBottom);
        }
    }

    public LayoutWrapper getLayoutWrapper() {
        return mLayoutWrapper;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mLayoutWrapper != null) {
            mLayoutWrapper.show();
        }
        HSBundle bundle = new HSBundle();
        bundle.putBoolean(SCREEN_INTERACTIVE, mLastInteractiveState);
        mLastInteractiveState = CommonUtils.isInteractive();
        HSGlobalNotificationCenter.sendNotification(NOTIFICATION_CUSTOMIZE_ACTIVITY_ONRESUME);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mHomeKeyWatcher = new HomeKeyWatcher(this);
        mHomeKeyWatcher.setOnHomePressedListener(new HomeKeyWatcher.OnHomePressedListener() {
            @Override
            public void onHomePressed() {
            }

            @Override
            public void onRecentsPressed() {
            }
        });
        mHomeKeyWatcher.startWatch();
        FeatureStats.recordStartTime("Personalization");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLastInteractiveState = CommonUtils.isInteractive();
        HSGlobalNotificationCenter.sendNotification(NOTIFICATION_CUSTOMIZE_ACTIVITY_ONPAUSE);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize);
        mContent = ViewUtils.findViewById(this, R.id.customize_content);
        mBottomBar = ViewUtils.findViewById(this, R.id.bottom_bar);
        mBottomBar.setOnNavigationItemSelectedListener(CustomizeActivity.this);
        BottomNavigationViewHelper.disableShiftMode(mBottomBar);
        BottomNavigationViewHelper.setTypeface(mBottomBar, Fonts.getTypeface(Fonts.Font.CUSTOM_FONT_REGULAR));
        mLayoutWrapper = new LayoutWrapper(mBottomBar, getResources().getDimensionPixelSize(R.dimen.bottom_bar_default_height), Dimensions.pxFromDp(3.3f));
        mWallpaperTabItemName = getIntent().getStringExtra(ThemeConstants.INTENT_KEY_WALLPAPER_TAB_ITEM_NAME);
        mIntent = getIntent();
        handleIntent(mIntent);
        OnlineWallpaperPage.TabsConfiguration galleryTabsConfig = new OnlineWallpaperPage.TabsConfiguration();
        mWallpaperTabIndex = getIntent().getIntExtra(ThemeConstants.INTENT_KEY_WALLPAPER_TAB, galleryTabsConfig.tabIndexHot);
        mOpenFromSrc = getIntent().getStringExtra(ThemeConstants.INTENT_KEY_FLURRY_FROM);
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addDataScheme("package");

        HSGlobalNotificationCenter.addObserver(WallpaperMgr.NOTIFICATION_WALLPAPER_GALLERY_SAVED, this);
        HSGlobalNotificationCenter.addObserver(WallpaperMgr.NOTIFICATION_REFRESH_LOCAL_WALLPAPER, this);

//        if (!showCampaignDialog()
//                && !LauncherApplication.isOneOfLauncherWallpaperVariants()) {
//            PromoteUtils.showPromoteGuideWithoutIcon(this,
//                    mViewIndex == TAB_INDEX_WALLPAPER ? "Wallpaper" : "Theme", PromoteGuideActivity.PromoteType.PERSONALIZED);
//        }

        HSGlobalNotificationCenter.addObserver(LauncherApplication.NOTIFICAITON_TRIM_MEMORY_COMPLETE, this);
//        CustomizeUtils.updateWallpaperTurn();

        WallpaperMgr.getInstance().initLocalWallpapers(new CustomizeService(), null);

        WallpaperAnalytics.logEvent("App_Open", true);
    }

    public ViewGroup getBottomBar() {
        return mBottomBar;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mIntent = intent;
        handleIntent(intent);
        if (mLayoutWrapper != null) {
            mLayoutWrapper.show();
        }
        WallpaperAnalytics.logEvent("App_Open", true);
    }

    private void handleIntent(Intent intent) {
        selectTabFromIntent(intent);
    }

    private int findBottomBarTypeIdByIndex(int index) {
        for (int i = 0; i < ITEMS_INDEX_MAP.size(); i++) {
            int id = ITEMS_INDEX_MAP.keyAt(i);
            if (ITEMS_INDEX_MAP.get(id) == index) {
                return id;
            }
        }

        return 0;
    }

    private void selectTabFromIntent(Intent intent) {
        int tabSelected = intent.getIntExtra(ThemeConstants.INTENT_KEY_TAB, 0);
        String from = intent.getStringExtra(ThemeConstants.INTENT_KEY_FLURRY_FROM);
        if (!TextUtils.isEmpty(from) && from.toLowerCase().contains("theme")) {
            // for fix issue #1246, because theme and wallpaper change tab index.
            tabSelected = TAB_INDEX_WALLPAPER;
        }
        if (mViewIndex == 0) {
            mFirstCome = true;
        }
        mViewIndex = tabSelected;
        Menu menu = mBottomBar.getMenu();
        MenuItem item = menu.findItem(findBottomBarTypeIdByIndex(tabSelected));
        if (item != null) item.setChecked(true);
        onNavigationItemSelected(menu.findItem(findBottomBarTypeIdByIndex(tabSelected)));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        int index = ITEMS_INDEX_MAP.get(itemId);
        boolean viewIndexUpdated = false;
        if (mViewIndex != index) {
            int tabSelected = getIntent().getIntExtra(ThemeConstants.INTENT_KEY_TAB, 0);

            mViewIndex = index;
            viewIndexUpdated = true;
        }
        mContent.setChildSelected(index);

        if (index != TAB_INDEX_WALLPAPER) {
            clearWallpaperData();
        }

        // reset icon to origins
        Menu menu = mBottomBar.getMenu();
        setMenuItemIconDrawable(menu, R.id.customize_bottom_bar_wallpapers, mNormalBottomIcons[0]);
        setMenuItemIconDrawable(menu, R.id.customize_bottom_bar_local, mNormalBottomIcons[1]);

        int i = item.getItemId();
        if (i == R.id.customize_bottom_bar_wallpapers) {
            if (viewIndexUpdated) {
                LauncherAnalytics.logEvent("Wallpaper_OpenFrom", "type", "Tab_Clicked");
            }

            item.setIcon(mHighlightBottomIcons[0]);

            if (viewIndexUpdated || mFirstCome) {
                showWallpaperAwardDialog();
            }
        } else if (i == R.id.customize_bottom_bar_local) {
            Threads.postOnSingleThreadExecutor(new Runnable() {
                @Override
                public void run() {
                    List<WallpaperInfo> infos = WallpaperMgr.getInstance().getLocalWallpapers();
                    if (!infos.isEmpty()) {
                        int size = 0;
                        for (WallpaperInfo info : infos) {
                            if (info.getType() != WallpaperInfo.WALLPAPER_TYPE_BUILT_IN) {
                                size++;
                            }
                        }
                    }
                }
            });
            item.setIcon(mHighlightBottomIcons[1]);
        }
        mFirstCome = false;
        return true;
    }


    private void clearWallpaperData() {
        mWallpaperTabIndex = 0;
        mWallpaperTabItemName = "";
    }

    private void showWallpaperAwardDialog() {
        if (TextUtils.isEmpty(mWallpaperTabItemName) && Preferences.getDefault().getBoolean(WallpaperMgr.PREF_KEY_SHOULD_SHOW_PACKAGE_BADGE, false)) {
            Preferences.getDefault().putBoolean(WallpaperMgr.PREF_KEY_SHOULD_SHOW_PACKAGE_BADGE, false);
            WallpaperAwardDialog dialog = WallpaperAwardDialog.newInstance();
            showDialogFragment(dialog);

        }
    }

    private void setMenuItemIconDrawable(Menu menu, @IdRes int itemId, @DrawableRes int drawableId) {
        MenuItem item = menu.findItem(itemId);
        if (item != null) {
            item.setIcon(drawableId);
        }
    }

    @Override
    public void installOverlay(View overlay, FrameLayout.LayoutParams params) {
        // Overlay must be installed onto decor view and add status / nav bar inset manually. Adding to activity content
        // root will not work.
        ((ViewGroup) getWindow().getDecorView()).addView(overlay, params);
    }

    @Override
    public void uninstallOverlay(View overlay) {
        ((ViewGroup) getWindow().getDecorView()).removeView(overlay);
    }

    @Override
    public View getOverlayee() {
        // We install overlay onto decor view, but returns activity content root here to exclude status / nav bar
        return ViewUtils.findViewById(this, android.R.id.content);
    }

    public void addActivityResultHandler(ActivityResultHandler handler) {
        Iterator<ActivityResultHandler> iter = mActivityResultHandlers.iterator();
        while (iter.hasNext()) {
            ActivityResultHandler registered = iter.next();
            if (registered.getClass().isInstance(handler)) {
                iter.remove();
            }
        }
        mActivityResultHandlers.add(handler);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SYSTEM_THEME_ALERT:
                int alertResult = CustomAlertActivity.RESULT_INTENT_VALUE_CANCEL;
                if (data != null) {
                    alertResult = data.getIntExtra(CustomAlertActivity.RESULT_INTENT_KEY_USER_SELECTED_INDEX,
                            CustomAlertActivity.RESULT_INTENT_VALUE_CANCEL);
                }
                if (alertResult == CustomAlertActivity.RESULT_INTENT_VALUE_OK) {
                    onCustomAlertPositiveAction(requestCode);
                } else {
                    onCustomAlertNegativeAction(requestCode);
                }
                break;
            default:
                // Dispatch to handlers only if we do not consume the result
                for (ActivityResultHandler handler : mActivityResultHandlers) {
                    handler.handleActivityResult(this, requestCode, resultCode, data);
                }
                break;
        }
    }

    private void onCustomAlertPositiveAction(int requestCode) {
        switch (requestCode) {
            case REQUEST_CODE_SYSTEM_THEME_ALERT:
                LauncherAnalytics.logEvent("Theme_Mine_MyTheme_LauncherDefault_Alert_BtnClicked", "Type", "OK");
                break;
        }
    }

    private void onCustomAlertNegativeAction(int requestCode) {
        switch (requestCode) {
            case REQUEST_CODE_SYSTEM_THEME_ALERT:
                LauncherAnalytics.logEvent("Theme_Mine_MyTheme_LauncherDefault_Alert_BtnClicked", "Type", "Cancel");
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (quitEditingMode()) {
            return;
        }

        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();
        quitEditingMode();
        mHomeKeyWatcher.stopWatch();

        FeatureStats.recordLastTime("Personalization");
    }

    private boolean quitEditingMode() {
        for (int index = 0; index < mContent.getChildCount(); index++) {
            View view = mContent.getChildAt(index);

            if (view instanceof OnlineWallpaperPage && ((OnlineWallpaperPage) view).isShowingCategories()) {
                ((OnlineWallpaperPage) view).hideCategoriesView();
                return true;
            }
        }

        return false;
    }

    public void setWallpaperIndex(int index) {
        if (mContent == null) {
            return;
        }

        for (int i = 0; i < mContent.getChildCount(); i++) {
            View view = mContent.getChildAt(i);
            if (view instanceof OnlineWallpaperPage) {
                ((OnlineWallpaperPage) view).setIndex(index);
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mActivityResultHandlers.clear();
        HSGlobalNotificationCenter.removeObserver(WallpaperMgr.NOTIFICATION_REFRESH_LOCAL_WALLPAPER, this);
        HSGlobalNotificationCenter.removeObserver(WallpaperMgr.NOTIFICATION_WALLPAPER_GALLERY_SAVED, this);

        HSGlobalNotificationCenter.sendNotification(NOTIFICATION_CUSTOMIZE_ACTIVITY_DESTROY);
        HSGlobalNotificationCenter.removeObserver(this);
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        if (s.equals(LauncherApplication.NOTIFICAITON_TRIM_MEMORY_COMPLETE)) {
            finish();
        }
    }

    public interface ActivityResultHandler {
        void handleActivityResult(Activity activity, int requestCode, int resultCode, Intent data);
    }

}
