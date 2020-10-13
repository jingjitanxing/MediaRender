# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in E:\sofeware\android-sdk-L/tools/proguard/proguard-android.txt
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

-keepattributes *JavascriptInterface*

-optimizationpasses 5
-dontskipnonpubliclibraryclassmembers
-printmapping proguardMapping.txt
-optimizations !code/simplification/cast,!field/*,!class/merging/*
-keepattributes *Annotation*,InnerClasses
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable
-keep class com.tencent.bugly.*{*;}
-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}

-dontwarn demo.**
-keep class demo.** { *;}

#=========================== mta
-keep class com.tencent.stat.*{*;}
-keep class com.tencent.mid.*{*;}

# XLog
-keep class com.tencent.mars.** { *; }
-keepclassmembers class com.tencent.mars.** { *; }
-dontwarn com.tencent.mars.**


-keep class com.geniusgithub.mediarender.** { *; }
-dontwarn com.geniusgithub.mediarender.**

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.support.multidex.MultiDexApplication
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View
-keep public class com.android.vending.licensing.ILicensingService
-keep class android.support.** {*;}
-keep public class * extends android.database.sqlite.SQLiteOpenHelper{*;}

-keep public class * extends android.view.View{
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
#这个主要是在layout 中写的onclick方法android:onclick="onClick"，不进行混淆
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
-keep class **.R$* {
 *;
}

-keepclassmembers class * {
    void *(*Event);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
#// natvie 方法不混淆
-keepclassmembers class * {
    native <methods>;
}

-keepclasseswithmembernames class * {
    native <methods>;
}

#保持 Parcelable 不被混淆
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

#----------------------------手动启用support keep注解------------------------
-dontskipnonpubliclibraryclassmembers
-printconfiguration
-keep,allowobfuscation @interface android.support.annotation.Keep
-keep @android.support.annotation.Keep class *
-keepclassmembers class * {
    @android.support.annotation.Keep *;
}

-keep class org.apache.httpp.**
-keep interface org.apache.http.**
-dontwarn org.apache.**

#v4包不混淆
-keep class android.support.v4.app.** { *; }
-keep interface android.support.v4.app.** { *; }

#忽略 libiary 混淆
-keep class io.vov.vitamio.** { *; }

-keep class com.teprinciple.mailsender.** { *; }
-dontwarn com.teprinciple.mailsender.**


-keep class com.geniusgithub.mediarender.center.** { *; }
-dontwarn com.geniusgithub.mediarender.center.**

-keep class com.gxh.dlnalib.** { *; }
-dontwarn com.gxh.dlnalib.**

-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}

# ServiceLoader support
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}

# Most of volatile fields are updated with AFU and should not be mangled
-keep class kotlinx.** { *; }
-dontwarn kotlinx.**
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

-keep class javax.activation.** {*;}
-keep class com.sun.activation.registries.** {*;}
-keep class com.sun.mail.** {*;}
-keep class javax.mail.** {*;}

#==============================================log4j
-dontwarn org.apache.log4j.**
-keep class  org.apache.log4j.** { *;}

-dontwarn org.apache.commons.logging.**

#ijkplayer
-keep class tv.danmaku.ijk.media.player.** {*; }
-keep class tv.danmaku.ijk.media.player.IjkMediaPlayer{*;}
-keep class tv.danmaku.ijk.media.player.ffmpeg.FFmpegApi{*;}
-keep class tv.danmaku.ijk.media.widget.** {*; }

-dontnote com.google.android.exoplayer2.ext.ima.ImaAdsLoader
-keepclassmembers class com.google.android.exoplayer2.ext.ima.ImaAdsLoader {
  <init>(android.content.Context, android.net.Uri);
}
