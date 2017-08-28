# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/willi/android-sdk/tools/proguard/proguard-android.txt
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

#error : Note: the configuration refers to the unknown class 'com.google.vending.licensing.ILicensingService'
#solution : @link http://stackoverflow.com/a/14463528
-dontnote com.google.vending.licensing.ILicensingService
-dontnote **ILicensingService

# http://stackoverflow.com/questions/33047806/proguard-duplicate-definition-of-library-class
-dontnote android.support.annotation.Keep
-dontnote org.apache.http.**
-dontnote android.net.http.**

# google proguard rules add this
-dontnote com.google.android.gms.**
-dontnote android.widget.Toolbar
-dontnote android.support.v7.widget.Toolbar

-keep class org.spongycastle.** { *; }
-dontwarn javax.naming.**

# zeroturnaround
-dontwarn org.slf4j.**
-dontwarn org.zeroturnaround.zip.**

# retrolambda
-dontwarn java.lang.invoke.*

# squareup.okhttp3
-dontwarn okhttp3.**
-dontnote okhttp3.**

# afollestad.material-dialogs
-dontnote com.afollestad.materialdialogs.**
