# Proguard rules for MobileDashboard
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepclassmembers class * {
    @org.jetbrains.kotlinx.serialization.Serializable <fields>;
}
