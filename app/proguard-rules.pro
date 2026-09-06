# Add project specific ProGuard rules here.
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep Firebase Database Models (reflection-based serialization)
-keep class com.pgshare.studentroomsharingapp.model.** { *; }
-keepclassmembers class com.pgshare.studentroomsharingapp.model.** { *; }

# Keep Razorpay Checkout SDK
-keep class com.razorpay.** { *; }
-dontwarn com.razorpay.**

# Keep Glide
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep class com.bumptech.glide.** { *; }