# Keep AdMob Mediation Adapter entry points and mapper classes from R8 shrinking/obfuscation
-keep class com.vpadn.mediation.VpadnAdapter { *; }
-keep class com.vpadn.mediation.VponObstruction { *; }
-keep class com.vpadn.mediation.VponNativeAdMapper { *; }
