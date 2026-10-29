# Architecture

A small single-module Android app. Keep it simple until a feature needs more. The detailed, always-current guide is [../AGENTS.md](../AGENTS.md); this page explains the structure.

## How a launcher works

Android treats any app with an activity that declares these categories as a possible home screen:

```xml
<intent-filter>
    <action android:name="android.intent.action.MAIN" />
    <category android:name="android.intent.category.HOME" />
    <category android:name="android.intent.category.DEFAULT" />
</intent-filter>
```

`MainActivity` declares it (plus `LAUNCHER`). The user picks it under **Settings, Apps, Default apps, Home app**. `launchMode="singleTask"` means pressing Home while open sends `onNewIntent`, which bumps a counter the UI watches to return to the first page.

## Listing apps

`data/AppRepository.kt` asks `PackageManager` for every activity handling `MAIN` + `LAUNCHER`. A narrow `<queries>` block in the manifest makes this work on Android 11+ without the restricted `QUERY_ALL_PACKAGES` permission.

## Layers

```
MainActivity            scales density so the screen is 390 design units wide, hosts WidgetHost
  LauncherApp           routing, pager, overlays, Back/Home handling, app launching
    ui/<screen>         composables that take LauncherState + Nav
      ui/components     design-system pieces      ui/theme  colours, fonts, Motion
    data/LauncherState  the single state holder (Compose state + SharedPreferences)
    data/Logic.kt       pure Kotlin rules (unit tested)
    service/*           overlay timer, red button, noise player
```

- **Navigation:** `Route` (sealed interface in `ui/Nav.kt`) plus a tiny stack in `LauncherApp`. `Route.Main` is a two-page `HorizontalPager` (Home, Study). All apps, Quick actions and the bottom bar are overlays on Main.
- **State:** one `LauncherState` instance, created in `MainActivity`, passed down. Fields are `mutableStateOf` and persisted in the SharedPreferences file `launcher`.
- **Logic without Android** goes in `data/Logic.kt` (or similar plain Kotlin) so it can be tested on the JVM.
- **Services:** `SessionOverlayService` (floating gated-app timer), `UrgeButtonService` (red dot), `NoiseService` (foreground media service that synthesises noise with `AudioTrack`).
- **Widgets from other apps:** `WidgetHost` wraps `AppWidgetHost`; ids are stored as `ext:<id>` in `widgets`.

## Permissions policy

| Permission | State |
| --- | --- |
| `INTERNET` | Never requested |
| `QUERY_ALL_PACKAGES` | Not used. Use `<queries>` |
| Usage access (`PACKAGE_USAGE_STATS`) | Optional, explained in-app |
| `SYSTEM_ALERT_WINDOW` | Optional (floating timer, red button) |
| Do Not Disturb access | Optional, granted in system settings |
| `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK`, `POST_NOTIFICATIONS` | Noise player only |
| `READ_CALENDAR` | Unused leftover; can be removed |

Any new permission needs an issue and a clear user benefit.

## Build and CI

- Gradle Kotlin DSL, versions in `gradle/libs.versions.toml`. `compileSdk` and `targetSdk` are 36 (Google Play requirement since 31 August 2026; check before each release).
- `build.yml`: debug APK, unit tests and lint on every push and PR. `release.yml`: signed release build. `screenshots.yml`: emulator screenshots pushed to the `shots-out` branch (see AGENTS.md section 9).
