# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

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

# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.-KotlinExtensions

# Gson
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keep class com.google.gson.** { *; }
-keep class com.gymbaswara.app.core.network.** { *; }
# DTO JSON backup/restore, Drive, sync, dan model domain dipakai via
# refleksi Gson. Tanpa ini R8 menghapus generic Signature List<T> sehingga
# elemen terdeserialisasi jadi LinkedTreeMap -> ClassCastException di release.
-keep class com.gymbaswara.app.core.backup.** { *; }
-keep class com.gymbaswara.app.core.drive.** { *; }
-keep class com.gymbaswara.app.core.sync.** { *; }
-keep class com.gymbaswara.app.domain.model.** { *; }

# Hilt / Dagger
-keep class dagger.hilt.** { *; }
-keep class dagger.** { *; }
-dontwarn dagger.internal.codegen.**
-dontwarn dagger.hilt.processor.internal.**
