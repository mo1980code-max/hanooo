# Clock Studio – Live Wallpapers

A native Kotlin/Jetpack Compose live-wallpaper application. It contains four synchronized wallpaper galleries, real-time clock previews, a DataStore-backed customization flow, eight per-app languages, and an Android `WallpaperService` which can be opened in the system live-wallpaper preview.

## Requirements

- Android Studio with the Android Gradle plugin support for API 36
- JDK 17
- Android SDK Platform 36 and Build Tools installed
- Gradle 8.11.1 (the project wrapper distribution is configured in `gradle/wrapper/gradle-wrapper.properties`)

Open this repository in Android Studio and sync Gradle. The installable application uses package `com.example.clockstudio`, minimum SDK 26, target SDK 36, version code 1, and version name 1.0.0. Debug builds have the `.debug` application ID suffix.

## Build and test

From the project root:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
```

Install the debug build from Android Studio or with `adb install app/build/outputs/apk/debug/app-debug.apk`.

## Live wallpaper

Choose any design, open its detail page, optionally customize it, and tap **Set Wallpaper**. Clock Studio first launches `WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER`; if a device does not expose that preview action, it falls back to Android's live-wallpaper chooser. The wallpaper renderer only schedules frames while the Android wallpaper engine is visible. Minute-only clocks update at the next minute boundary, second hands update once per second, and smooth analog seconds use a capped 30 FPS.

Clock position is stored as normalized screen coordinates. Wallpaper selection, style options, language, settings, and favorites are stored locally using Preferences DataStore. The smart clock reads the current battery broadcast and shows a percentage only after Android supplies a real battery level.

## Original artwork and licensed replacements

The starter galleries draw original procedural gradients and geometry on Canvas. Replace a design with your own commercially licensed image by following [`WALLPAPER_ASSETS.md`](WALLPAPER_ASSETS.md). Artwork is resolved by the existing catalog, so clock and navigation code do not need to change.

## Languages

English, Arabic, Turkish, Spanish, Russian, French, Portuguese, and Indonesian are supported. Language selection uses AndroidX AppCompat per-app locales and is persisted locally. Arabic uses Android's normal RTL layout direction; Canvas clock hands and time values remain unmirrored.

## Optional advertising

The UI calls an `AdManager` abstraction, and the Google Mobile Ads adapter uses Google's official test application, banner, and interstitial IDs. An interstitial slot is available for an intentional future break point, but the current UI never invokes it. `BuildConfig.ADS_ENABLED` is `false` for both supplied build types, so no ad view is created by default. Before enabling ads for a public release, add a consent flow appropriate to your distribution regions, publish an accurate privacy disclosure, and implement a reviewed release configuration. Do not replace the source test IDs with production IDs in development builds.

## Play-ready release notes

1. Review the application ID, privacy text, artwork licenses, and target SDK against current Play policy.
2. Keep `versionCode` increasing for each upload and update `versionName` in `app/build.gradle.kts`.
3. In Android Studio choose **Build > Generate Signed Bundle / APK > Android App Bundle**, create or select a protected upload key, and build the `release` variant.
4. Store signing credentials outside this repository and configure Play App Signing in Play Console.
5. Test installation, language switching, system wallpaper preview, battery/time changes, gestures, and both gesture and three-button navigation on physical devices.

No account, contacts, location, camera, microphone, or storage access is required by the core application.
