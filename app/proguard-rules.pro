# Keep Android's wallpaper service discoverable by the system wallpaper picker.
-keep class com.example.clockstudio.wallpaper.ClockWallpaperService { *; }

# Keep the Google Mobile Ads SDK metadata intact when the optional adapter is enabled.
-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.**
