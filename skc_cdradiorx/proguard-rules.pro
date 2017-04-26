# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\android-sdk-studio/tools/proguard/proguard-android.txt
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

-optimizations !code/simplification/arithmetic
-allowaccessmodification
-repackageclasses ''
-dontpreverify
-ignorewarning
-dontwarn android.support.**
#-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod
-keepattributes Exceptions,InnerClasses,*Annotation*
-dontskipnonpubliclibraryclasses
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}
#-libraryjars libs\SKC_CDRadioRx.jar

-keep public class com.skycaster.skc_cdradiorx.bases.*
-keep public class com.skycaster.skc_cdradiorx.abstr.* { *; }
-keep public class com.skycaster.skc_cdradiorx.service.*
-keep public class com.skycaster.skc_cdradiorx.utils.LogUtils{public *;}
-keep public class com.skycaster.skc_cdradiorx.beans.AckBean{public *;}
-keep public class com.skycaster.skc_cdradiorx.beans.DSP{public *;}
-keep public class com.skycaster.skc_cdradiorx.beans.DataQuality{public *;}
-keep public class com.skycaster.skc_cdradiorx.manager.DSPManager{public *;}
-keep public interface com.skycaster.skc_cdradiorx.manager.DSPManager$*{*;}
-keep public class com.skycaster.skc_cdradiorx.manager.DSPManager$*{*;}
-keep public interface com.skycaster.skc_cdradiorx.intface.*
-keep class * extends java.lang.annotation.Annotation { *; }

#-keep public class * extends android.app.Activity
#-keep public class * extends android.app.Application
#-keep public class * extends android.view.View {
#    public <init>(android.content.Context);
#    public <init>(android.content.Context,android.util.AttributeSet);
#    public <init>(android.content.Context,android.util.AttributeSet,int);
#    public void set*(...);
#}
#

-keepclasseswithmembers class * {
    public <init>(android.content.Context,android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context,android.util.AttributeSet,int);
}

-keepclassmembers class * extends android.content.Context {
    public void *(android.view.View);
    public void *(android.view.MenuItem);
}

-keepclassmembers class * extends android.os.Parcelable {
    static ** CREATOR;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

-keepclassmembers class * {
    abstract <methods>;
    public ** get*(...);
    public ** is*(...);
    protected void showLog(...);
}
