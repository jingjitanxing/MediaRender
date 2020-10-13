# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/zhangyang/Library/Android/sdk/tools/proguard/proguard-android.txt
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

# 保留Annotation不混淆
-keepattributes *Annotation*,InnerClasses

# 避免混淆泛型
-keepattributes Signature

# 抛出异常时保留代码行号
-keepattributes SourceFile,LineNumberTable


# 保留本地native方法不被混淆
-keepclasseswithmembernames class * {
    native <methods>;
}

-keep class tv.danmaku.ijk.media.widget.media.IMediaController{
    public *;
}

-keep class tv.danmaku.ijk.media.widget.media.IjkVideoView{
     public *;
 }

-keep class tv.danmaku.ijk.media.player.IMediaPlayer {
     *;
}