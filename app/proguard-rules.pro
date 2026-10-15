# Keep line numbers, so crash reports in the Play Console point to the right lines.
-keepattributes SourceFile,LineNumberTable

# Moshi reads the JSON models with Kotlin reflection, so their names, fields and Kotlin metadata must stay.
-keep class com.humboshot.koillection.data.** { *; }
-keep class kotlin.Metadata { *; }

# Retrofit reads the service interfaces, their annotations and their generic return types at run time.
-keepattributes Signature, InnerClasses, EnclosingMethod, RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, AnnotationDefault
-keep class com.humboshot.koillection.api.services.** { *; }
-keep class kotlin.Result { *; }
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-dontwarn javax.annotation.**
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*
