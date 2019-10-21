# ==== Required by dependencies ====
-keep public class com.google.android.gms.analytics.** { public *; }
-dontwarn com.google.android.gms.analytics.**

# Workaround for building project with Google Play Services
-keep class com.google.android.gms.iid.zzd { *; }
-keep class android.support.v4.content.ContextCompat { *; }

-keepattributes EnclosingMethod

# Flurry
-keep class com.flurry.** { *; }
-dontwarn com.flurry.**

# Tapjoy
-keep class com.tapjoy.** { *; }
-dontwarn com.tapjoy.**

-dontwarn com.google.firebase.**
-dontwarn com.amazon.**
-dontwarn com.appsflyer.FirebaseInstanceIdListener**

-dontwarn com.ihs.interstitialads.**
-dontwarn com.ihs.affiliateads.HeaderAds.AffiliateHeader**
-dontwarn net.appcloudbox.h5game.**
-dontwarn net.appcloudbox.apevent.**
-dontwarn com.call.assistant.**
-dontwarn com.acb.call.**
-dontwarn com.vertical.color.phone.**

# Data Binding
-dontwarn android.databinding.**
-keep class android.databinding.** { *; }

# Android SVG
-dontwarn com.caverock.androidsvg.**
-keep class com.caverock.androidsvg.** { *; }

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}

# ARouter
-keep public class com.alibaba.android.arouter.routes.**{*;}
-keep class * implements com.alibaba.android.arouter.facade.template.ISyringe{*;}
-dontwarn javax.lang.model.element.**

# Smart ads
-keep class com.smartadserver.android.** { *; }
-dontwarn com.smartadserver.android.**

# libZmoji
-keep class com.futurebits.zmoji.lib.data.avatar.AvatarInfo { *; }
-dontwarn com.futurebits.zmoji.lib.svg.**

# libCashCenter
-keep class com.acb.cashcenter.model.**{*;}

# ==== Air Launcher specific ====

# Required by Gradle Retrolambda plugin
-dontwarn java.lang.invoke.*

# Remove logs
-assumenosideeffects class com.ihs.commons.utils.HSLog {
    public static void v(...);
    public static void d(...);
    public static void i(...);
    public static void w(...);
    public static void e(...);
    public static void pt(...);
}

# Remove any unnecessary Bitmap#recycle() call
-assumenosideeffects class android.graphics.Bitmap {
    public void recycle();
}

# Fabric Crashlytics
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

-keepnames class com.acb.libwallpaper.live.model.DefaultAppFilter

-keepnames class com.acb.libwallpaper.live.dialog.LauncherFloatWindowManager

-keepnames class com.themelab.launcher.dialog.ThemeFloatWindowManager

-keepnames public class * extends com.acb.libwallpaper.live.schedule.SimpleBroadcastJob

-keepnames class com.acb.libwallpaper.live.livewallpaper.GLWallpaperService
-keepnames class com.acb.libwallpaper.live.livewallpaper.GLWallpaperService2
-keepnames class com.acb.libwallpaper.live.livewallpaper.GLWallpaperService3
-keepnames class com.acb.libwallpaper.live.livewallpaper.GLWallpaperService4
-keepnames class com.themelab.launcher.ThemeWallpaperService

-keep class com.acb.libwallpaper.live.badge.NotificationServiceV18 { <init>(...); }

-keep class com.acb.libwallpaper.live.desktop.allapps.AllAppsContainerView {
  public void setBackgroundAlpha(float);
}

-keep class com.acb.libwallpaper.live.customize.view.SuccessTickView{
  private void setTickPosition(float);
}

-keep class com.acb.libwallpaper.live.desktop.folder.SharedFolder {
  public void setBackgroundAlpha(float);
}

-keepnames class com.acb.libwallpaper.live.weather.widget.WeatherClockWidget

-keep class com.acb.libwallpaper.live.weather.HourlyForecastCurve {
  public void setProgress(float);
}

# Lucky

-keep class com.acb.libwallpaper.live.lucky.view.ChancesAnimationAdapter {
  public void set*(***);
}

-keep class com.acb.libwallpaper.live.lucky.MusicPlayer {
  public void setVolume(float);
}

-keep class com.acb.libwallpaper.live.lucky.view.FlyAwardBaseView {
  protected void setTranslationYProgress(float);
  protected void setFlipTranslationYProgress(float);
  protected void setFlipTranslationXProgress(float);
}

-keep class com.acb.libwallpaper.live.Icons { *; }


# ==== From AOSP Launcher 3 ====

-keep class com.acb.libwallpaper.live.desktop.DefaultFastScroller {
  public void setThumbWidth(int);
  public int getThumbWidth();
  public void setTrackWidth(int);
  public int getTrackWidth();
}

-keep class com.acb.libwallpaper.live.desktop.DefaultFastScrollerPopup {
  public void setAlpha(float);
  public float getAlpha();
}

-keep class com.acb.libwallpaper.live.desktop.BubbleTextView {
  public void setFastScrollFocus(float);
  public float getFastScrollFocus();
}

-keep class com.acb.libwallpaper.live.desktop.dragdrop.ButtonDropTarget {
  public int getTextColor();
}

-keep class com.acb.libwallpaper.live.desktop.CellLayout {
  public float getBackgroundAlpha();
  public void setBackgroundAlpha(float);
}

-keep class com.acb.libwallpaper.live.desktop.CellLayout$LayoutParams {
  public void setWidth(int);
  public int getWidth();
  public void setHeight(int);
  public int getHeight();
  public void setX(int);
  public int getX();
  public void setY(int);
  public int getY();
}

-keep class com.acb.libwallpaper.live.desktop.DragLayer$LayoutParams {
  public void setWidth(int);
  public int getWidth();
  public void setHeight(int);
  public int getHeight();
  public void setX(int);
  public int getX();
  public void setY(int);
  public int getY();
}

-keep class com.acb.libwallpaper.live.desktop.FastBitmapDrawable {
  public int getBrightness();
  public void setBrightness(int);
}

-keep class com.acb.libwallpaper.live.debug.MemoryDumpActivity {
  *;
}

-keep class com.acb.libwallpaper.live.desktop.PreloadIconDrawable {
  public float getAnimationProgress();
  public void setAnimationProgress(float);
}

-keep class com.acb.libwallpaper.live.desktop.Workspace {
  public float getBackgroundAlpha();
  public void setBackgroundAlpha(float);
}

-keep class * extends com.acb.libwallpaper.live.welcome.WelcomeScreens

# Customize
-keepclassmembers class android.support.design.internal.BottomNavigationMenuView {
    boolean mShiftingMode;
}


# Flash Screen
-keep class * extends android.os.IInterface
-keep class com.android.internal.** { *; }
-keepclassmembers class com.acb.call.utils.CallUtils {
	public *;
}

-keep class com.avl.engine.** { *; }

# Smart ads
-keep class com.smartadserver.android.** { *; }
-dontwarn com.smartadserver.android.**

# Local file log utils
-assumenosideeffects class com.acb.libwallpaper.live.util.LogUtils {
    public static void v(...);
    public static void i(...);
    public static void w(...);
    public static void d(...);
    public static void e(...);
    public static void file(...);
    public static void json(...);
    public static void xml(...);
    public static void defaultInit(...);

}

## ----------------------------------
##     Gson
## ----------------------------------
-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.** { *; }
-keep class com.google.gson.stream.** { *; }

## ----------------------------------
##     AdCaffe2
## ----------------------------------
-keep class com.ihandysoft.ad.adcaffe.Model.** { *; }
# ========================= Glide ========================
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
 **[] $VALUES;
 public *;
}

-keep class com.google.** { *; }

-keep class android.support.graphics.drawable.** { *; }

# ========== Umeng ===============
-keepclassmembers class **.R$* {
	public static <fields>;
}
-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-dontwarn com.umeng.**
-dontwarn com.taobao.**
-dontwarn anet.channel.**
-dontwarn anetwork.channel.**
-dontwarn org.android.**
-dontwarn org.apache.thrift.**
-dontwarn com.xiaomi.**
-dontwarn com.huawei.**
-dontwarn com.meizu.**
-keepattributes *Annotation*
-keep class com.taobao.** {*;}
-keep class org.android.** {*;}
-keep class anet.channel.** {*;}
-keep class com.umeng.** {*;}
-keep class com.xiaomi.** {*;}
-keep class com.huawei.** {*;}
-keep class com.meizu.** {*;}
-keep class org.apache.thrift.** {*;}
-keep class com.alibaba.sdk.android.**{*;}
-keep class com.ut.**{*;}
-keep class com.ta.**{*;}