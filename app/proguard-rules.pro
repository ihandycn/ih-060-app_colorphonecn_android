# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/ruoyu.li/Dev/Software/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-printconfiguration "build/outputs/mapping/configuration.txt"

#-keep public class * extends android.app.Activity
#-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
#-keep public class * extends android.app.backup.BackupAgentHelper
#-keep public class * extends android.preference.Preference

-keep class com.honeycomb.colorphone.activity.WelcomeActivity {
  *;
}

-keepnames class com.honeycomb.colorphone.PermanentService$* {
    public <fields>;
    public <methods>;
}
-keep class com.honeycomb.colorphone.PermanentService {
*;
}
-keepnames class com.honeycomb.colorphone.LockJobService

-keepnames class com.honeycomb.colorphone.notification.NotificationServiceV18

-keep public class com.android.vending.licensing.ILicensingService
-keep class com.acb.cashcenter.model.* {
*;
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    publåic <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class fqcn.of.javascript.interface.for.webview {
   public *;
}

-keep class com.android.webkit.** { *; }
-dontwarn android.webkit.**


-keepattributes *Annotation*,EnclosingMethod

-keep class android.support.v7.** { *; }
-dontwarn android.support.v7.**

-keep class android.net.** { *; }
-dontwarn android.net.**

-keep class org.apache.** { *; }
-dontwarn org.apache.**

-dontwarn com.ihs.affiliateads.**
-keep class * implements Serializable {*;}
-keep class * implements android.os.Parcelable {*;}
-keep class * extends android.os.IInterface
-keep class com.android.internal.** { *; }

-keep class com.inmobi.** { *; }

-keep class com.android.webkit.** { *; }


-keep class com.millennialmedia.android.** {*;}
-keep class com.nuance.nmdp.** {*;}
-keep class com.jumptap.adtag.** { *; }

-keep class com.mdotm.android.** { *; }

-keep class com.amazon.** { *; }
-keep class com.facebook.**{ *; }

-keep class com.mopub.** { *; }

-dontwarn com.inneractive.**
-dontwarn android.webkit.**

# libNative ad
-dontwarn net.pubnative.** #to delete when update libNativeAds

# ========== Pubnative ===============
-keep class net.pubnative.** { *; }


# ========== StartApp ===============
-keep class com.startapp.** {
      *;
}
-dontwarn android.webkit.JavascriptInterface
-dontwarn com.startapp.**


# ========== Leadbolt ===============
-dontwarn android.support.v4.**
-keep public class com.google.android.gms.* { public *; }
-dontwarn com.google.android.gms.**

-keep class com.google.firebase.**
-dontwarn com.google.firebase.**


-keep class com.apptracker.** { *; }
-dontwarn com.apptracker.**
-keepclassmembers class **.R$* {
	public static <fields>;
}


-keep class **.R$*

-dontwarn com.ihs.affiliateads.**

# ========== Umeng ===============
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